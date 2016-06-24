/*
 * @(#)file	SnmpRequestHandler.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.36
 * @(#)date      07/10/01
 *
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright (c) 2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL")(collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://opendmk.dev.java.net/legal_notices/licenses.txt or in the 
 * LEGAL_NOTICES folder that accompanied this code. See the License for the 
 * specific language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file found at
 *     http://opendmk.dev.java.net/legal_notices/licenses.txt
 * or in the LEGAL_NOTICES folder that accompanied this code.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.
 * 
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * 
 *       "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding
 * 
 *       "[Contributor] elects to include this software in this distribution
 *        under the [CDDL or GPL Version 2] license."
 * 
 * If you don't indicate a single choice of license, a recipient has the option
 * to distribute your version of this file under either the CDDL or the GPL
 * Version 2, or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the
 * GPL Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 * 
 *
 */


package com.sun.management.comm;



// java import
//
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.io.InterruptedIOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;

// jmx imports
//
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.sun.management.snmp.SnmpMessage;
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpPduBulk;
import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpPduRequest;
import com.sun.management.snmp.SnmpPduTrap;
import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpDataTypeEnums;

// RI imports
//
import com.sun.jdmk.internal.ClassLogger;


// jdmk import
//
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpUserDataFactory;
//import com.sun.management.snmp.IPAcl.IPAcl;
import com.sun.management.snmp.InetAddressAcl;


class SnmpRequestHandler extends ClientHandler implements SnmpDefinitions {

    private transient DatagramSocket      socket = null ;
    private transient DatagramPacket      packet = null ;
    private transient Vector              mibs = null ;
  
    /**
     * Contains the list of sub-requests associated to the current request.
     */
    private transient Hashtable subs = null ;
    
    /**
     * Reference on the MIBS
     */
    private transient SnmpMibTree root;
 
    private transient Object              ipacl = null ;
    private transient SnmpPduFactory      pduFactory = null ;  
    private transient SnmpUserDataFactory userDataFactory = null ;  
    private transient SnmpAdaptorServer adaptor = null;
    /**
     * Full constructor
     */
    public SnmpRequestHandler(SnmpAdaptorServer server, int id, 
                              DatagramSocket s, DatagramPacket p,
                              SnmpMibTree tree, Vector m, Object a, 
                              SnmpPduFactory factory, 
                              SnmpUserDataFactory dataFactory,
                              MBeanServer f, ObjectName n) 
    {
        super(server, id, f, n);
	
	// Need a reference on SnmpAdaptorServer for getNext & getBulk, 
	// in case of oid equality (mib overlapping).
	//
	adaptor = server;
        socket = s;
        packet = p;
        root= tree;
        mibs = (Vector) m.clone();
        subs= new Hashtable(mibs.size());
        ipacl = a;
        pduFactory = factory ;
        userDataFactory = dataFactory ;
        //thread.start();
    }
    
    /**
     * Treat the request available in 'packet' and send the result
     * back to the client.
     * Note: we overwrite 'packet' with the response bytes.
     */
    public void doRun() {
  
        // log the input packet
        //
        if (logger.finerOn()) {
            logger.finer("doRun", "Packet received:\n" + 
			 SnmpMessage.dumpHexBuffer(packet.getData(), 0,
						   packet.getLength()));
        }

        // Let's build the response packet
        //
        DatagramPacket respPacket = makeResponsePacket(packet) ;
    
        // Log the output packet
        //
        if (logger.finerOn() && (respPacket != null)) {
            logger.finer("doRun", "Packet to be sent:\n" + 
			 SnmpMessage.dumpHexBuffer(respPacket.getData(), 0, 
						   respPacket.getLength()));
        }
    
        // Send the response packet if any
        //
        if (respPacket != null) {
            try {
                socket.send(respPacket) ;
            }
            catch (SocketException e) {
                if (logger.finestOn()) {
                    if (e.getMessage().equals(InterruptSysCallMsg))
                        logger.finest("doRun", "interrupted");
                    else {
                        logger.finest("doRun", "i/o exception");
                        logger.finest("doRun", e);
                    }
                }
            }
            catch(InterruptedIOException e) {
                if (logger.finestOn()) {
                    logger.finest("doRun", "interrupted");
                }
            }
            catch(Exception e) {
                if (logger.finestOn()) {
                    logger.finest("doRun", "failure when sending response");
                    logger.finest("doRun", e);
                }
            }
        }
    }

    /**
     * Here we make a response packet from a request packet.
     * We return null if there no response packet to sent.
     */
    private DatagramPacket makeResponsePacket(DatagramPacket reqPacket) {
        DatagramPacket respPacket = null ;
    
        // Transform the request packet into a request SnmpMessage
        //
        SnmpMessage reqMsg = new SnmpMessage() ;
        try {
            reqMsg.decodeMessage(reqPacket.getData(), reqPacket.getLength()) ;
            reqMsg.address = reqPacket.getAddress() ;
            reqMsg.port = reqPacket.getPort() ;
        }
        catch(SnmpStatusException x) {
            if (logger.finestOn()) {
                logger.finest("makeResponsePacket", "packet decoding failed");
		logger.finest("makeResponsePacket", x);
            }
            reqMsg = null ;
            ((SnmpAdaptorServer)adaptorServer).incSnmpInASNParseErrs(1) ;
        }
    
        // Make the response SnmpMessage if any
        //
        SnmpMessage respMsg = null ;
        if (reqMsg != null) {
            respMsg = makeResponseMessage(reqMsg) ;
        }
    
        // Try to transform the response SnmpMessage into response packet.
        // NOTE: we overwrite the request packet.
        //
        if (respMsg != null) {
            try {
                reqPacket.
		    setLength(respMsg.encodeMessage(reqPacket.getData())) ;
                respPacket = reqPacket ;
            }
            catch(SnmpTooBigException x) {
                if (logger.finestOn()) {
                    logger.finest("makeResponsePacket", 
				  "response message is too big");
                }
                try {
                    respMsg = newTooBigMessage(reqMsg) ;
                    reqPacket.
			setLength(respMsg.encodeMessage(reqPacket.getData())) ;
                    respPacket = reqPacket ;
                }
                catch(SnmpTooBigException xx) {
                    if (logger.finestOn()) {
                        logger.finest("makeResponsePacket", 
				      "'too big' is 'too big' !!!");
                    }
		    adaptor.incSnmpSilentDrops(1);
                }
            }
        }
    
        return respPacket ;
    }
  
    /**
     * Here we make a response message from a request message.
     * We return null if there is no message to reply.
     */
    private SnmpMessage makeResponseMessage(SnmpMessage reqMsg) {
        SnmpMessage respMsg = null ;
    
        // Transform the request message into a request pdu
        //
        SnmpPduPacket reqPdu = null ;
        Object userData = null;
        try {
            reqPdu = (SnmpPduPacket)pduFactory.decodeSnmpPdu(reqMsg) ;
            if (reqPdu != null && userDataFactory != null)
                userData = userDataFactory.allocateUserData(reqPdu);
        }
        catch(SnmpStatusException x) {
            reqPdu = null ;
            SnmpAdaptorServer snmpServer = (SnmpAdaptorServer)adaptorServer ;
            snmpServer.incSnmpInASNParseErrs(1) ;
            if (x.getStatus()== SnmpDefinitions.snmpWrongSnmpVersion)
                snmpServer.incSnmpInBadVersions(1) ;
            if (logger.finestOn()) {
                logger.finest("makeResponseMessage", 
			      "message decoding failed");
            }
        }

        // Make the response pdu if any
        //
        SnmpPduPacket respPdu = null ;
        if (reqPdu != null) {
            respPdu = makeResponsePdu(reqPdu,userData) ;
            try {
                if (userDataFactory != null) 
                    userDataFactory.releaseUserData(userData,respPdu);
            } catch (SnmpStatusException x) {
                respPdu = null;
            }
        }
    
        // Try to transform the response pdu into a response message if any
        //
        if (respPdu != null) {
            try {
                respMsg = (SnmpMessage)
		    pduFactory.encodeSnmpPdu(respPdu, packet.getData().length);
            }
            catch(SnmpStatusException x) {
                respMsg = null ;
                if (logger.finestOn()) {
                    logger.finest("makeResponseMessage", 
			   "failure when encoding the response message");
                    logger.finest("makeResponseMessage", x);
                }
            }
            catch(SnmpTooBigException x) {
                if (logger.finestOn()) {
                    logger.finest("makeResponseMessage", 
				  "response message is too big");
		    logger.finest("makeResponseMessage",
				  "caught "+x,x);
                }

                try {
                    // if the PDU is too small, why should we try to do 
		    // recovery ? 
                    // 
                    if (packet.getData().length <=32) 
                        throw x; 
                    int pos= x.getVarBindCount();
                    if (logger.finestOn()) {
                        logger.finest("makeResponseMessage", 
				      "fail on element " + pos);
                    }
                    int old= 0;
                    while (true) {
                        try {
                            respPdu = reduceResponsePdu(reqPdu, respPdu, pos) ;
                            //respMsg = (SnmpMessage)
			    //	pduFactory.encodeSnmpPdu(respPdu, 
			    //			packet.getData().length -32) ;
                            respMsg = (SnmpMessage)
				pduFactory.encodeSnmpPdu(respPdu, 
						packet.getData().length) ;
                            break;
                        } catch (SnmpTooBigException xx) {
                            if (logger.finestOn()) {
                                logger.finest("makeResponseMessage", 
					 "response message is still too big");
                            }
                            old= pos;
                            pos= xx.getVarBindCount();
                            if (logger.finestOn()) {
                                logger.finest("makeResponseMessage", 
					      "fail on element" + pos);
                            }
                            if (pos == old) {
                                // we can not go any further in trying to 
				// reduce the message !
                                //
                                throw xx;
                            }
                        }
                    }// end of loop
                } catch(SnmpStatusException xx) {
                    respMsg = null ;
                    if (logger.finestOn()) {
                        logger.finest("makeResponseMessage", 
			    "failure when encoding the response message");
                        logger.finest("makeResponseMessage", xx);
                    }
                }
                catch(SnmpTooBigException xx) {
                    try {
                        respPdu = newTooBigPdu(reqPdu) ;
                        respMsg = (SnmpMessage)
			    pduFactory.encodeSnmpPdu(respPdu, 
						     packet.getData().length) ;
                    }
                    catch(SnmpTooBigException xxx) {
                        respMsg = null ;
                        if (logger.finestOn()) {
                            logger.finest("makeResponseMessage", 
					  "'too big' is 'too big' !!!");
                        }
			adaptor.incSnmpSilentDrops(1);
                    }
                    catch(Exception xxx) {
                        respMsg = null ;
			if (logger.finerOn()) {
			    logger.finer("makeResponseMessage", 
					 "[" + Thread.currentThread() + 
					 "]: Unexpected exception: " + xxx);
			}
			logger.finest("makeResponseMessage",xxx);
                    }
                }
                catch(Exception xx) {
                    respMsg = null ;
		    if (logger.finerOn()) {
			logger.finer("makeResponseMessage", 
				      "[" + Thread.currentThread() + 
				      "]: Unexpected exception: " + xx);
		    }
		    logger.finest("makeResponseMessage",xx);
                }
            }
        }
        return respMsg ;
    }
  
    /**
     * Here we make a response pdu from a request pdu.
     * We return null if there is no pdu to reply.
     */
    private SnmpPduPacket makeResponsePdu(SnmpPduPacket reqPdu, 
                                          Object userData) {
        
        SnmpAdaptorServer snmpServer = (SnmpAdaptorServer)adaptorServer ;
        SnmpPduPacket respPdu = null ;
    
        snmpServer.updateRequestCounters(reqPdu.type) ;
        if (reqPdu.varBindList != null)
            snmpServer.updateVarCounters(reqPdu.type, 
                                         reqPdu.varBindList.length) ; 
    
        if (checkPduType(reqPdu)) {
            respPdu = checkAcl(reqPdu) ;
            if (respPdu == null) { // reqPdu is accepted by ACLs
                if (mibs.size() < 1) {
                    if (logger.finerOn()) {
                        logger.finer("makeResponsePdu", "Request " + 
                              reqPdu.requestId + 
                              " received but no MIB registered.");
                    }
		    return makeNoMibErrorPdu((SnmpPduRequest)reqPdu, userData);
                }
                switch(reqPdu.type) {
                case SnmpPduPacket.pduGetRequestPdu:
                case SnmpPduPacket.pduGetNextRequestPdu:
                case SnmpPduPacket.pduSetRequestPdu:
                    respPdu = makeGetSetResponsePdu((SnmpPduRequest)reqPdu,
                                                    userData) ;
                    break ;
            
                case SnmpPduPacket.pduGetBulkRequestPdu:
                    respPdu = makeGetBulkResponsePdu((SnmpPduBulk)reqPdu,
                                                     userData) ;
                    break ;
                }
            }
            else { // reqPdu is rejected by ACLs
                // respPdu contains the error response to be sent.
                // We send this response only if authResEnabled is true.
                if (!snmpServer.getAuthRespEnabled()) { 
		    // No response should be sent
                    respPdu = null ;
                }
                if (snmpServer.getAuthTrapEnabled()) { // A trap must be sent
                    try {
                        snmpServer.snmpV1Trap(
			    SnmpPduTrap.trapAuthenticationFailure, 0, 
			    new SnmpVarBindList()) ;
                    }
                    catch(Exception x) {
                        if (logger.finestOn()) {
                            logger.finest("makeResponsePdu", 
			       "failure when sending authentication trap");
                            logger.finest("makeResponsePdu", x);
                        }
                    }
                }
            }
        }
        return respPdu ;
    }
  
    //
    // Generates a response packet, filling the values in the
    // varbindlist with one of endOfMibView, noSuchObject, noSuchInstance
    // according to the value of <code>status</code>
    // 
    // @param statusTag should be one of:
    //        <li>SnmpDataTypeEnums.errEndOfMibViewTag</li>
    //        <li>SnmpDataTypeEnums.errNoSuchObjectTag</li>
    //        <li>SnmpDataTypeEnums.errNoSuchInstanceTag</li>
    //
    SnmpPduPacket makeErrorVarbindPdu(SnmpPduPacket req, int statusTag) {

        final SnmpVarBind[] vblist = req.varBindList;
        final int length = vblist.length;

        switch (statusTag) {
        case SnmpDataTypeEnums.errEndOfMibViewTag:
            for (int i=0 ; i<length ; i++) 
                vblist[i].setSnmpValue(SnmpVarBind.endOfMibView);
            break;
        case SnmpDataTypeEnums.errNoSuchObjectTag:
            for (int i=0 ; i<length ; i++) 
                vblist[i].setSnmpValue(SnmpVarBind.noSuchObject);
            break;
        case SnmpDataTypeEnums.errNoSuchInstanceTag:
            for (int i=0 ; i<length ; i++) 
                vblist[i].setSnmpValue(SnmpVarBind.noSuchInstance);
            break;
        default:
            return newErrorResponsePdu(req,snmpRspGenErr,1);
        }
        return newValidResponsePdu(req,vblist);
    }

    // Generates an appropriate response when no mib is registered in
    // the adaptor.
    //
    // <li>If the version is V1:</li>
    // <ul><li>Generates a NoSuchName error V1 response PDU</li></ul>
    // <li>If the version is V2:</li>
    // <ul><li>If the request is a GET, fills the varbind list with
    //         NoSuchObject's</li>
    //     <li>If the request is a GET-NEXT/GET-BULK, fills the varbind 
    //         list with EndOfMibView's</li>
    //     <li>If the request is a SET, generates a NoAccess error V2 
    //          response PDU</li>
    // </ul>
    // 
    //
    SnmpPduPacket makeNoMibErrorPdu(SnmpPduRequest req, Object userData) {
        // There is no agent registered
        //
        if (req.version == SnmpDefinitions.snmpVersionOne) {
            // Version 1: => NoSuchName
            return 
                newErrorResponsePdu(req,snmpRspNoSuchName,1);
        } else if (req.version == SnmpDefinitions.snmpVersionTwo) {
            // Version 2: => depends on PDU type
            switch (req.type) {
            case pduSetRequestPdu :
            case pduWalkRequest : 
                // SET request => NoAccess
                return 
                    newErrorResponsePdu(req,snmpRspNoAccess,1);
            case pduGetRequestPdu : 
                // GET request => NoSuchObject
                return 
                    makeErrorVarbindPdu(req,
					SnmpDataTypeEnums.errNoSuchObjectTag);
            case pduGetNextRequestPdu :
            case pduGetBulkRequestPdu : 
                // GET-NEXT or GET-BULK => EndOfMibView
                return 
                    makeErrorVarbindPdu(req,
					SnmpDataTypeEnums.errEndOfMibViewTag);
            default:
            }
        }
        // Something wrong here: => snmpRspGenErr
        return newErrorResponsePdu(req,snmpRspGenErr,1);
    }

    /**
     * Here we make the response pdu from a get/set request pdu.
     * At this level, the result is never null.
     */
    private SnmpPduPacket makeGetSetResponsePdu(SnmpPduRequest req,
                                                Object userData) {
   
        // Create the trhead group specific for handling sub-requests 
	// associated to the current request. Use the invoke id
        //
        // Nice idea to use a thread group on a request basis. 
	// However the impact on performance is terrible !
        // theGroup= new ThreadGroup(thread.getThreadGroup(), "request " + 
	//                           String.valueOf(req.requestId));
    
        // Let's build the varBindList for the response pdu
        //
   
        if (req.varBindList == null) {
            // Good ! Let's make a full response pdu.
            //
            return newValidResponsePdu(req, null) ;
        }

        // First we need to split the request into subrequests
        //
        splitRequest(req);
        int nbSubRequest= subs.size();
        if (nbSubRequest == 1)
            return turboProcessingGetSet(req,userData);
  
    
        // Execute all the subrequests resulting from the split of the 
	// varbind list.
        //
        SnmpPduPacket result= executeSubRequest(req,userData);
        if (result != null)
            // It means that an error occured. The error is already 
	    // formatted by the executeSubRequest method.
            return result;
        
        // So far so good. So we need to concatenate all the answers.
        //
        if (logger.finerOn()) {
            logger.finer("makeGetSetResponsePdu", 
			 "Build the unified response for request " + 
			 req.requestId);
        }
        return mergeResponses(req);
    }
  
    /**
     * The method runs all the sub-requests associated to the current 
     * instance of SnmpRequestHandler.
     */
    private SnmpPduPacket executeSubRequest(SnmpPduPacket req,
                                            Object userData) {
    
        int errorStatus = SnmpDefinitions.snmpRspNoError ;
        int nbSubRequest= subs.size();
            
	int i=0;
        // If it's a set request, we must first check any varBind
        //
        if (req.type == pduSetRequestPdu) {
     
	    i=0;
            for(Enumeration e= subs.elements(); e.hasMoreElements() ; i++) {
                // Indicate to the sub request that a check must be invoked ...
                // OK we should have defined out own tag for that !
                //
                SnmpSubRequestHandler sub= (SnmpSubRequestHandler) 
		    e.nextElement();
                sub.setUserData(userData);
                sub.type= pduWalkRequest;

		sub.run();

		sub.type= pduSetRequestPdu;

		if (sub.getErrorStatus() != SnmpDefinitions.snmpRspNoError) {
		    // No point to go any further.
		    //
		    if (logger.finestOn()) {
			logger.finest("executeSubRequest", "an error occurs");
		    }

		    return newErrorResponsePdu(req, errorStatus, 
					       sub.getErrorIndex() + 1) ;
		}
            }
        }// end processing check operation for a set PDU.
    
        // Let's start the sub-requests.
        // 
	i=0;
        for(Enumeration e= subs.elements(); e.hasMoreElements() ;i++) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
        /* NPCTE fix for bugId 4492741, esc 0, 16-August 2001 */ 
	    sub.setUserData(userData);
	/* end of NPCTE fix for bugId 4492741 */      

	    sub.run();

            if (sub.getErrorStatus() != SnmpDefinitions.snmpRspNoError) {
                // No point to go any further.
                //
                if (logger.finestOn()) {
                    logger.finest("executeSubRequest", "an error occurs");
                }

                return newErrorResponsePdu(req, errorStatus, 
					   sub.getErrorIndex() + 1) ;
            }
	}
    
        // everything is ok
        //
        return null;
    }
  
    /**
     * Optimize when there is only one sub request
     */
    private SnmpPduPacket turboProcessingGetSet(SnmpPduRequest req,
                                                Object userData) {
  
        int errorStatus = SnmpDefinitions.snmpRspNoError ;
        SnmpSubRequestHandler sub= (SnmpSubRequestHandler) 
	    subs.elements().nextElement();
        sub.setUserData(userData);

        // Indicate to the sub request that a check must be invoked ...
        // OK we should have defined out own tag for that !
        //
        if (req.type == SnmpDefinitions.pduSetRequestPdu) {
            sub.type= pduWalkRequest;
            sub.run();    
            sub.type= pduSetRequestPdu;
       
            // Check the error status. 
            //
            errorStatus= sub.getErrorStatus();
            if (errorStatus != SnmpDefinitions.snmpRspNoError) {
                // No point to go any further.
                //
                return newErrorResponsePdu(req, errorStatus, 
					   sub.getErrorIndex() + 1) ;
            }
        }
  
        // process the operation
        //
   
        sub.run();
        errorStatus= sub.getErrorStatus();
        if (errorStatus != SnmpDefinitions.snmpRspNoError) {
            // No point to go any further.
            //
            if (logger.finestOn()) {
                logger.finest("turboProcessingGetSet", "an error occurs");
            }
            int realIndex= sub.getErrorIndex() + 1;
            return newErrorResponsePdu(req, errorStatus, realIndex) ;
        }
    
        // So far so good. So we need to concatenate all the answers.
        //
    
        if (logger.finerOn()) {
            logger.finer("turboProcessingGetSet", 
			 "build the unified response for request " + 
			 req.requestId);
        }
        return mergeResponses(req);
    }
  
    /**
     * Here we make the response pdu for a bulk request.
     * At this level, the result is never null.
     */
    private SnmpPduPacket makeGetBulkResponsePdu(SnmpPduBulk req, 
                                                 Object userData) {
   
        SnmpVarBind[] respVarBindList = null ;
    
        // RFC 1905, Section 4.2.3, p14
        int L = req.varBindList.length ;
        int N = Math.max(Math.min(req.nonRepeaters, L), 0) ;
        int M = Math.max(req.maxRepetitions, 0) ;
        int R = L - N ;
    
        if (req.varBindList == null) {
            // Good ! Let's make a full response pdu.
            //
            return newValidResponsePdu(req, null) ;
        }
    
        // Split the request into subrequests.
        //
        splitBulkRequest(req, N, M, R);
        SnmpPduPacket result= executeSubRequest(req,userData);
        if (result != null)
            return result;
    
        respVarBindList= mergeBulkResponses(N + (M * R));

        // Now we remove useless trailing endOfMibView.
        //
        int m2 ; // respVarBindList[m2] item and next are going to be removed
        int t = respVarBindList.length ;
        while ((t > N) && 
	       (respVarBindList[t-1].getSnmpValue().equals(SnmpVarBind.endOfMibView))) {
            t-- ;
        }
        if (t == N)
            m2 = N + R ;
        else
            m2 = N + ((t -1 -N) / R + 2) * R ; // Trivial, of course...
        if (m2 < respVarBindList.length) {
            SnmpVarBind[] truncatedList = new SnmpVarBind[m2] ;
            for (int i = 0 ; i < m2 ; i++) {
                truncatedList[i] = respVarBindList[i] ;
            }
            respVarBindList = truncatedList ;
        }

        // Good ! Let's make a full response pdu.
        //
        return newValidResponsePdu(req, respVarBindList) ;
    }
  
    /**
     * Check the type of the pdu: only the get/set/bulk request
     * are accepted.
     */
    private boolean checkPduType(SnmpPduPacket pdu) {

        boolean result = true ;

        switch(pdu.type) {
    
        case SnmpDefinitions.pduGetRequestPdu:
        case SnmpDefinitions.pduGetNextRequestPdu:
        case SnmpDefinitions.pduSetRequestPdu:
        case SnmpDefinitions.pduGetBulkRequestPdu:
            result = true ;
            break;

        default:
            if (logger.finestOn()) {
                logger.finest("checkPduType", 
			      "cannot respond to this kind of PDU");
            }
            result = false ;
            break;
        }
    
        return result ;
    }
  
    /**
     * Check if the specified pdu is conform to the ACL.
     * This method returns null if the pdu is ok. If not, it returns
     * the response pdu to be replied.
     */
    private SnmpPduPacket checkAcl(SnmpPduPacket pdu) {
        SnmpPduPacket response = null ;
        String community = new String(pdu.community) ;
    
        // We check the pdu type and create an error response if
        // the check failed.
        //
        if (ipacl != null) {
            if (pdu.type == SnmpDefinitions.pduSetRequestPdu) {
                if (!((InetAddressAcl)ipacl).
		    checkWritePermission(pdu.address, community)) {
                    if (logger.finerOn()) {
                        logger.finer("checkAcl", "sender is " + 
				     pdu.address + " with " + community);
                        logger.finer("checkAcl", 
				     "sender has no write permission");
                    }
		    int err = SnmpSubRequestHandler.mapErrorStatus(
			      SnmpDefinitions.snmpRspAuthorizationError, 
			      pdu.version, pdu.type);
                    response = newErrorResponsePdu(pdu, err, 0) ;
                }
                else {
                    if (logger.finerOn()) {
                        logger.finer("checkAcl", "sender is " + 
				     pdu.address + " with " + community);
                        logger.finer("checkAcl", 
				     "sender has write permission");
                    }
                }
            }
            else {
                if (!((InetAddressAcl)ipacl).
		    checkReadPermission(pdu.address, community)) {
                    if (logger.finerOn()) {
                        logger.finer("checkAcl", "sender is " + 
				     pdu.address + " with " + community);
                        logger.finer("checkAcl", 
				     "sender has no read permission");
                    }
		    int err = SnmpSubRequestHandler.mapErrorStatus(
			      SnmpDefinitions.snmpRspAuthorizationError, 
			      pdu.version, pdu.type);
                    response = newErrorResponsePdu(pdu, 
						   err, 
						   0);
		    SnmpAdaptorServer snmpServer = 
			(SnmpAdaptorServer)adaptorServer;
		    snmpServer.updateErrorCounters(
			       SnmpDefinitions.snmpRspNoSuchName);
                }
		else {
                    if (logger.finerOn()) {
                        logger.finer("checkAcl", "sender is " + 
				     pdu.address + " with " + community);
                        logger.finer("checkAcl", "sender has read permission");
                    }
                }
            }
        }
    
        // If the response is not null, this means the pdu is rejected.
        // So let's update the statistics.
        //
        if (response != null) {
            SnmpAdaptorServer snmpServer = (SnmpAdaptorServer)adaptorServer ;
            snmpServer.incSnmpInBadCommunityUses(1) ;
            if (((InetAddressAcl)ipacl).checkCommunity(community) == false)
                snmpServer.incSnmpInBadCommunityNames(1) ;
        }
    
        return response ;
    }
    
    /**
     * Make a response pdu with the specified error status and index.
     * NOTE: the response pdu share its varBindList with the request pdu. 
     */
    private SnmpPduRequest newValidResponsePdu(SnmpPduPacket reqPdu, 
					       SnmpVarBind[] varBindList) {
        SnmpPduRequest result = new SnmpPduRequest() ;
    
        result.address = reqPdu.address ;
        result.port = reqPdu.port ;
        result.version = reqPdu.version ;
        result.community = reqPdu.community ;
        result.type = result.pduGetResponsePdu ;
        result.requestId = reqPdu.requestId ;
        result.errorStatus = SnmpDefinitions.snmpRspNoError ;
        result.errorIndex = 0 ;
        result.varBindList = varBindList ;
    
        ((SnmpAdaptorServer)adaptorServer).
	    updateErrorCounters(result.errorStatus) ;
    
        return result ;
    }
  
    /**
     * Make a response pdu with the specified error status and index.
     * NOTE: the response pdu share its varBindList with the request pdu. 
     */
    private SnmpPduRequest newErrorResponsePdu(SnmpPduPacket req, int s, 
					       int i) {
        SnmpPduRequest result = newValidResponsePdu(req, null) ;
        result.errorStatus = s ;
        result.errorIndex = i ;
        result.varBindList = req.varBindList ;

        ((SnmpAdaptorServer)adaptorServer).
	    updateErrorCounters(result.errorStatus) ;
    
        return result ;
    }

    private SnmpMessage newTooBigMessage(SnmpMessage reqMsg) 
	throws SnmpTooBigException {
        SnmpMessage result = null ;
        SnmpPduPacket reqPdu = null ;
    
        try {
            reqPdu = (SnmpPduPacket)pduFactory.decodeSnmpPdu(reqMsg) ;
            if (reqPdu != null) {
                SnmpPduPacket respPdu = newTooBigPdu(reqPdu) ;
                result = (SnmpMessage)pduFactory.encodeSnmpPdu(respPdu, 
						   packet.getData().length) ;
            }
        }
        catch(SnmpStatusException x) {
            // This should not occur because decodeIncomingRequest has normally
            // been successfully called before.
	    logger.finest("newTooBigMessage","Internal Error: ", x);
            throw new InternalError() ;
        }
    
        return result ;
    }
  
    private SnmpPduPacket newTooBigPdu(SnmpPduPacket req) {
        SnmpPduRequest result = 
	    newErrorResponsePdu(req, SnmpDefinitions.snmpRspTooBig, 0) ;
        result.varBindList = null ;
        return result ;
    }

    private SnmpPduPacket reduceResponsePdu(SnmpPduPacket req, 
					    SnmpPduPacket resp, 
					    int acceptedVbCount) 
        throws SnmpTooBigException {
  
        // Reduction can be attempted only on bulk response
        //
        if (req.type != req.pduGetBulkRequestPdu) {
            if (logger.finestOn()) {
                logger.finest("reduceResponsePdu", "cannot remove anything");
            }
            throw new SnmpTooBigException(acceptedVbCount) ;
        }
    
        // We're going to reduce the varbind list.
        // First determine which items should be removed.
        // Next duplicate and replace the existing list by the reduced one.
        //
        // acceptedVbCount is the number of varbind which have been
        // successfully encoded before reaching bufferSize:
        //   * when it is >= 2, we split the varbindlist at this 
        //     position // suppress this (-1 to be safe),
        //   * when it is 1, we only put one (big?) item in the varbindlist
        //   * when it is 0 (in fact, acceptedVbCount is not available), 
        //     we split the varbindlist by 2.
        //
        int vbCount = resp.varBindList.length ;
        if (acceptedVbCount >= 2)
            vbCount = Math.min(acceptedVbCount, resp.varBindList.length-1);
        else if (acceptedVbCount == 1)
            vbCount = 1 ;
        else // acceptedCount == 0 ie it is unknown
	     // REVISIT: or the first varbind
	     // is itself toobig => probably should try with vbCount=1
             vbCount = resp.varBindList.length / 2 ;
    
        if (vbCount < 1) {
            if (logger.finestOn()) {
                logger.finest("reduceResponsePdu", "cannot remove anything");
            }
            throw new SnmpTooBigException(acceptedVbCount) ;
        }
        else {
            SnmpVarBind[] newVbList = new SnmpVarBind[vbCount] ;
            for (int i = 0 ; i < vbCount ; i++) {
                newVbList[i] = resp.varBindList[i] ;
            }
            if (logger.finestOn()) {
                logger.finest("reduceResponsePdu", 
			      (resp.varBindList.length - newVbList.length) + 
			      " items have been removed");
            }
            resp.varBindList = newVbList ;
        }
    
        return resp ;
    }

    /**
     * The method takes the incoming requests and split it into subrequests.
     */
    private void splitRequest(SnmpPduRequest req) {
    
        int nbAgents= mibs.size();
        SnmpMibAgent agent= (SnmpMibAgent) mibs.firstElement();
    
        // For the get next operation we are going to send the varbind list
        // to all agents
        //
        if (req.type == pduGetNextRequestPdu) {
            for(Enumeration e= mibs.elements(); e.hasMoreElements(); ) {
                SnmpMibAgent ag= (SnmpMibAgent) e.nextElement();
                subs.put(ag, new SnmpSubNextRequestHandler(ag, req, root));
            }
            return;
        }

        int nbReqs= req.varBindList.length;
        SnmpVarBind[] vars= req.varBindList;
        SnmpSubRequestHandler sub;
        for(int i=0; i < nbReqs; i++) {
            agent= root.getAgentMib(vars[i].getOid());
	    
	    if(logger.finerOn())
		logger.finer("splitRequest","Found mib [" + agent + 
			     "] for oid " + vars[i].getOid());
	    
            sub= (SnmpSubRequestHandler) subs.get(agent);
            if (sub == null) {
                // We need to create the sub request handler and update 
		// the hashtable
                //
                sub= new SnmpSubRequestHandler(agent, req);
                subs.put(agent, sub);
            }
      
            // Update the translation table within the subrequest
            //
            sub.updateRequest(vars[i], i);
        }
    }
  
    /**
     * The method takes the incoming get bulk requests and split it into 
     * subrequests.
     */
    private void splitBulkRequest(SnmpPduBulk req, 
				  int nonRepeaters, 
				  int maxRepetitions, 
				  int R) {
	// Send the getBulk to all agents
	//
	for(Enumeration e= mibs.elements(); e.hasMoreElements(); ) {
	    SnmpMibAgent agent = (SnmpMibAgent) e.nextElement();

	    if(logger.finestOn())
		logger.finer("splitBulkRequest", "Create a sub with : " + 
		      agent + " " + nonRepeaters + " " +
		      maxRepetitions + " " + R);

	    subs.put(agent, 
		     new SnmpSubBulkRequestHandler(agent, 
						   req, 
						   nonRepeaters, 
						   maxRepetitions, 
						   R,
						   root));
	}
	return;
    }
  
    private SnmpPduPacket mergeResponses(SnmpPduRequest req) {
    
        if (req.type == pduGetNextRequestPdu) {
            return mergeNextResponses(req);
        }
      
        SnmpVarBind[] result= req.varBindList;
  
        // Go through the list of subrequests and concatenate.
	// Hopefully, by now all the sub-requests should be finished
        //
        for(Enumeration e= subs.elements(); e.hasMoreElements();) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
            sub.updateResult(result);
        }
        return newValidResponsePdu(req,result);
    }
  
    private SnmpPduPacket mergeNextResponses(SnmpPduRequest req) {
        int max= req.varBindList.length;
        SnmpVarBind[] result= new SnmpVarBind[max];
    
        // Go through the list of subrequests and concatenate. 
	// Hopefully, by now all the sub-requests should be finished
        //
        for(Enumeration e= subs.elements(); e.hasMoreElements();) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
            sub.updateResult(result);
        }
    
        if (req.version == snmpVersionTwo) {
            return newValidResponsePdu(req,result);
        }
    
        // In v1 make sure there is no endOfMibView ...
        //
        for(int i=0; i < max; i++) {
            SnmpValue val= result[i].getSnmpValue();
            if (val == SnmpVarBind.endOfMibView)
                return newErrorResponsePdu(req, 
			       SnmpDefinitions.snmpRspNoSuchName, i+1);
        }
    
        // So far so good ...
        //
        return newValidResponsePdu(req,result);
    }
  
    private SnmpVarBind[] mergeBulkResponses(int size) {
        // Let's allocate the array for storing the result
        //
        SnmpVarBind[] result= new SnmpVarBind[size];
        for(int i= size-1; i >=0; --i) {
            result[i]= new SnmpVarBind();
            result[i].setSnmpValue(SnmpVarBind.endOfMibView);
        }
    
        // Go through the list of subrequests and concatenate. 
	// Hopefully, by now all the sub-requests should be finished
        //
        for(Enumeration e= subs.elements(); e.hasMoreElements();) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
            sub.updateResult(result);
        }
   
        return result;
    }
    
    
    protected String makeDebugTag() {
        return "SnmpRequestHandler[" + adaptorServer.getProtocol() + 
	    ":" + adaptorServer.getPort() + "]";
    }

    ClassLogger makeLogger(String dbgTag) {
	return new ClassLogger(ClassLogger.LOGGER_ADAPTOR_SNMP,dbgTag);
    }

    Thread createThread(Runnable r) {
	return null;
    }  

    static final private String InterruptSysCallMsg = 
	"Interrupted system call";

    static final private SnmpStatusException noSuchNameException =
        new SnmpStatusException(SnmpDefinitions.snmpRspNoSuchName) ;
}

