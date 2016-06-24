/*
 * @(#)file      SnmpV3RequestHandler.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.46
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
import java.util.Hashtable;
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
import com.sun.management.snmp.SnmpMsg;

import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpPduRequestType;
import com.sun.management.snmp.SnmpMessage;
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpPduBulkType;
import com.sun.management.snmp.SnmpDataTypeEnums;

import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpPduTrap;
import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpDataTypeEnums;
import com.sun.management.snmp.SnmpNull;
import com.sun.management.snmp.SnmpAckPdu;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.InetAddressAcl;
// RI imports
//
import com.sun.jdmk.internal.ClassLogger;

// jdmk import
//
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpRequestForwarder;
import com.sun.management.snmp.agent.SnmpUserDataFactory;
import com.sun.management.internal.snmp.SnmpIncomingRequest;
import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.internal.snmp.SnmpMsgProcessingModel;
import com.sun.management.internal.snmp.SnmpAccessControlSubSystem;
import com.sun.management.internal.snmp.SnmpAccessControlModel;
import com.sun.management.internal.snmp.SnmpTools;
import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpUnknownMsgProcModelException;
import com.sun.management.snmp.SnmpUnknownAccContrModelException;
import com.sun.management.snmp.SnmpBadSecurityLevelException;

class SnmpV3RequestHandler extends ClientHandler implements SnmpDefinitions {

    private transient DatagramSocket      socket = null;
    private transient DatagramPacket      packet = null;
    private transient Vector              mibs = null;
   
    private transient Hashtable           contexts = null;
    private transient Hashtable           forwarders = null;
    /**
     * Contains the list of sub-requests associated to the current request.
     */
    private transient Hashtable subs = null ;
    
    /**
     * Reference on the MIBS
     */
    private transient SnmpMibTree root;
    //This one are evaluated when the context is known. Via getCurrentRoot 
    //and getCurrentMibs methods.
    private transient Vector              currentMibs = null;
    private transient SnmpMibTree         currentRoot = null;
    private transient Object              ipacl = null ;
    private transient SnmpPduFactory      pduFactory = null ;  
    private transient SnmpUserDataFactory userDataFactory = null ;  
    private transient SnmpEngineImpl engine = null;
    private transient SnmpMsgProcessingSubSystem msgProcSubSys = null;
    private transient SnmpAccessControlSubSystem accessSubSys = null;
    private transient SnmpV3AdaptorServer adaptor = null;
    /**
     * Full constructor
     */
    public SnmpV3RequestHandler(SnmpV3AdaptorServer server, 
				SnmpEngine eng, 
				int id, 
				DatagramSocket s, DatagramPacket p,
				Hashtable contexts, 
				Hashtable forwarders,
				SnmpMibTree tree,
				Vector m, Object a, 
				SnmpPduFactory factory, 
				SnmpUserDataFactory dataFactory,
				MBeanServer f, ObjectName n) 
    {
        super(server, id, f, n);
	adaptor = server;
	this.engine = (SnmpEngineImpl) eng;
	msgProcSubSys = engine.getMsgProcessingSubSystem();
	accessSubSys = engine.getAccessControlSubSystem();
	this.contexts = contexts;
	this.forwarders = forwarders;
	socket = s;
        packet = p;
        root = tree;
        mibs = (Vector) m.clone();
	//This initialization will be done when context will be known.
        //subs = new Hashtable(mibs.size());
        ipacl = a;
        pduFactory = factory ;
        userDataFactory = dataFactory ;
	if(logger.finestOn()) {
	    logger.finest("SnmpV3RequestHandler", 
		  "userDataFactory : " + userDataFactory);
	}
    }
    /**
     * Treat the request available in 'packet' and send the result
     * back to the client.
     * Note: we overwrite 'packet' with the response bytes.
     */
    public void doRun() {
  
        // Log the input packet
        //
        if (logger.finerOn()) {
            logger.finer("doRun", "Packet received:\n" + 
			 SnmpMsg.dumpHexBuffer(packet.getData(), 0, 
					       packet.getLength()));
        }

        // Let's build the response packet
        //
        DatagramPacket respPacket = makeResponsePacket(packet) ;
    
        // Log the output packet
        //
        if (logger.finerOn() && (respPacket != null)) {
            logger.finer("doRun", "Packet to be sent:\n" + 
			 SnmpMsg.dumpHexBuffer(respPacket.getData(), 0, 
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
     * Check if the contextEngineId is a known one. If not the message is 
     * ABORTED.
     */
    private SnmpRequestForwarder handleContextEngineId(
                                 SnmpIncomingRequest pendingReq) {
	// Every array[] is formated in a SnmpEngineId object in order to 
	// avoid String mismatch.
	SnmpEngineId eng = 
	    SnmpEngineId.createEngineId(pendingReq.getContextEngineId());
	SnmpRequestForwarder forwarder = null;
	if(!engine.getEngineId().equals(eng)) {
	    if(logger.finerOn())
		logger.finer("handleContextEngineId", 
			     " The received contextEngineId is UNKNOWN:[" + 
			     eng + "]. Forward to registered proxy.");
	    if(eng != null)
		forwarder = (SnmpRequestForwarder) 
		    forwarders.get(eng.toString());
	    if(forwarder == null) {
		if(logger.finerOn())
		    logger.finer("handleContextEngineId", 
			  " The received contextEngineId is not handled:[" + 
				 eng + "]");
		pendingReq.noResponse();
	    }
	}
	return forwarder;
    }
    
    /**
     * Handle the received context name.
     */
    private void handleContextName(SnmpIncomingRequest pendingReq) {
	if(pendingReq.getContextName() != null) { //V1 and V2 calls.
	    String name =  new String(pendingReq.getContextName());
	    if(pendingReq.getContextName().length != 0 && 
	       !name.equals("default") ) {
		 if(logger.finerOn())
			logger.finer("handleContextName", 
			      " The received contextName is :[" + 
			      name + "]");
		SnmpMibContextName context = 
		    (SnmpMibContextName) contexts.get(name);
		if(context != null) {
		    currentRoot = context.root;
		    currentMibs = context.mibs;
		}
		else {
		    if(logger.finerOn())
			logger.finer("handleContextName", 
			      " The received contextName is UNKNOWN:[" + 
			      name + "]");
		    //Increment the dedicated counter
		    adaptor.incSnmpUnknownContexts(1);
		    
		    pendingReq.noResponse();
		}
	    }
	    else { // No context name set, it is the Default one.
		currentRoot = root;
		currentMibs = mibs; 
	    }
	}
	else {
	    currentRoot = root;
	    currentMibs = mibs; 
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
	SnmpIncomingRequest pendingReq = null;
	int version = 0;
	try {
	    version = SnmpMsg.getProtocolVersion(reqPacket.getData());
	    
	    pendingReq = msgProcSubSys.getIncomingRequest(version, 
							  pduFactory);
            pendingReq.decodeMessage(reqPacket.getData(), 
				     reqPacket.getLength(),
				     reqPacket.getAddress(),
				     reqPacket.getPort()) ;
        }
	catch(SnmpUnknownMsgProcModelException x) {
	    if (logger.finestOn()) {
                logger.finest("makeResponsePacket", 
			      "Unknown Msg Processing Model:" +
			      version);
		logger.finest("makeResponsePacket", x);
            }
	    //Increment unsupported Snmp Version.
	    adaptor.incSnmpInBadVersions(1);
	    adaptor.incSnmpInvalidMsgs(1);
	    return null;
	}
	catch(SnmpUnknownSecModelException x) {
	    if (logger.finestOn()) {
                logger.finest("makeResponsePacket", "Unknown Security Model:" +
		      version);
		logger.finest("makeResponsePacket",  x);
            }
	    adaptor.incSnmpUnknownSecurityModels(1);
	    return null;
	}
	catch(SnmpBadSecurityLevelException x) {
	    if (logger.finestOn()) {
                logger.finest("makeResponsePacket", "Bad Security Level:" +
		      version);
		logger.finest("makeResponsePacket", x);
            }
	    adaptor.incSnmpInvalidMsgs(1);
	    return null;
	}
        catch(SnmpStatusException x) {
	    if (logger.finestOn()) {
                logger.finest("makeResponsePacket", "packet decoding failed");
            }
	    adaptor.incSnmpInASNParseErrs(1);
	    return null;
	}

	if(pendingReq.isReport()) {
	    try {
		reqPacket.setLength(pendingReq.encodeMessage(
					       reqPacket.getData()));
                respPacket = reqPacket ;
            }
            catch(SnmpTooBigException x) {
                if (logger.finestOn()) {
                    logger.finest("makeResponsePacket", 
				  "response message is too big");
                }
                try {
		    newTooBigMessage(pendingReq);
		    reqPacket.setLength(pendingReq.encodeMessage(
						   reqPacket.getData()));
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
	    
	    return respPacket;
	}

	if(pendingReq.isResponse()) {
	    //First handling context EngineId:
	    //Handling context Name. currentMibs and currentRoot 
	    //attributes are set.
	    SnmpRequestForwarder forwarder = handleContextEngineId(pendingReq);
	    if(forwarder != null) {
		makeResponseMessage(pendingReq, forwarder);
	    }
	    else {
		//Initialize subs only if the request can be handled.
		if(pendingReq.isResponse()) {
		    //Define the request context name.
		    handleContextName(pendingReq);
		    if(pendingReq.isResponse()) {
			subs = new Hashtable(currentMibs.size());
			makeResponseMessage(pendingReq, null);
		    }
		}
	    }
	}
	
        // Try to transform the response SnmpMessage into response packet.
        // NOTE: we overwrite the request packet.
        //
	if (pendingReq.isResponse()) {
            try {
		reqPacket.setLength(pendingReq.encodeMessage(
					       reqPacket.getData()));
                respPacket = reqPacket ;
            }
            catch(SnmpTooBigException x) {
                if (logger.finestOn()) {
                    logger.finest("makeResponsePacket", 
				  "response message is too big");
                }
                try {
		    newTooBigMessage(pendingReq);
		    reqPacket.setLength(pendingReq.encodeMessage(
                                                   reqPacket.getData()));
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

    private void makeResponseMessage(SnmpIncomingRequest pendingReq,
				     SnmpRequestForwarder forwarder) {
    
        // Transform the request message into a request pdu
        //
	SnmpPdu reqPdu = null ;
        Object userData = null;
        try {
	    reqPdu = pendingReq.decodeSnmpPdu();

            if (reqPdu != null && userDataFactory != null)
                userData = userDataFactory.allocateUserData(reqPdu);
        }
        catch(SnmpStatusException x) {
            reqPdu = null ;      
            adaptor.incSnmpInASNParseErrs(1) ;
            if (x.getStatus()== SnmpDefinitions.snmpWrongSnmpVersion)
                adaptor.incSnmpInBadVersions(1) ;
            if (logger.finestOn()) {
                logger.finest("makeResponseMessage", 
			      "message decoding failed");
            }
        }

        // Make the response pdu if any
        //
	SnmpPdu respPdu = null ;
        if (reqPdu != null) {
            respPdu = makeResponsePdu(pendingReq, 
				      reqPdu, 
				      userData,
				      forwarder);
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
		pendingReq.encodeSnmpPdu(respPdu, 
					 packet.getData().length);
            }
            catch(SnmpStatusException x) {
		pendingReq.noResponse();
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
				      "fail on element" + pos);
                    }
                    int old= 0;
                    while (true) {
                        try {
                            respPdu = 
				reduceResponsePdu(reqPdu, respPdu, pos) ;
			    pendingReq.encodeSnmpPdu(respPdu,
						packet.getData().length -32) ;
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
		    pendingReq.noResponse();
                    if (logger.finestOn()) {
                        logger.finest("makeResponseMessage", 
				"failure when encoding the response message");
                        logger.finest("makeResponseMessage", xx);
                    }
                }
                catch(SnmpTooBigException xx) {
                    try {
                        respPdu = newTooBigPdu(reqPdu);
			pendingReq.encodeSnmpPdu(respPdu, 
						 packet.getData().length);
                    }
                    catch(SnmpTooBigException xxx) {
			pendingReq.noResponse();

                        if (logger.finestOn()) {
                            logger.finest("makeResponseMessage", 
					  "'too big' is 'too big' !!!");
                        }
			adaptor.incSnmpSilentDrops(1);
                    }
                    catch(Exception xxx) {
			pendingReq.noResponse();
                    }
                }
                catch(Exception xx) {
		    pendingReq.noResponse();
                }
            }
	}
    }
  
    /**
     * Here we make a response pdu from a request pdu.
     * We return null if there is no pdu to reply.
     */
    private SnmpPdu makeResponsePdu(SnmpIncomingRequest pendingReq,
				    SnmpPdu reqPdu, 
				    Object userData,
				    SnmpRequestForwarder forwarder) {
        SnmpPdu respPdu = null ;

	//The counter are not impacted by forwarding feature. Must forward now.
	if(forwarder != null) {
	    return makeForwardResponsePdu(pendingReq,
					  reqPdu,
					  forwarder);
	}

        adaptor.updateRequestCounters(reqPdu.type) ;
        if (reqPdu.varBindList != null)
            adaptor.updateVarCounters(reqPdu.type, 
				      reqPdu.varBindList.length); 
	
        if (checkPduType(reqPdu)) {
	    if (currentMibs.size() < 1) {
		if (logger.finerOn()) {
		    logger.finer("makeResponsePdu", "Request " + 
			  reqPdu.requestId + 
			  " received but no MIB registered.");
		}
		return makeNoMibErrorPdu((SnmpPdu) reqPdu,userData);
	    }
	    
	    if(logger.finestOn())
		logger.finer("makeResponsePdu", "Testing access for : " 
		  + "Principal : " + pendingReq.getPrincipal() + " \n"
		  + "Snmp version : " + ((SnmpPdu)reqPdu).version + " \n"
		  + "Security level : " + pendingReq.getSecurityLevel() +" \n"
		  + "Pdu type : " + ((SnmpPdu)reqPdu).type +" \n"
		  + "Security model : " + pendingReq.getSecurityModel() +" \n"
		  + " Access context : "+ pendingReq.getAccessContext()+" \n");
	    try {
		accessSubSys.checkPduAccess(reqPdu.version,
					    pendingReq.getPrincipal(),
					    pendingReq.getSecurityLevel(),
					    reqPdu.type,
					    pendingReq.getSecurityModel(),
					    pendingReq.getAccessContext(),
					    reqPdu);
	    }
	    catch(SnmpStatusException e) {
		int err = -1;
		if (logger.finestOn())
		    logger.finest("makeResponsePdu", 
				  "authorization failed : " + e.getStatus());
		switch(e.getStatus()) {
		case SnmpDefinitions.snmpRspNoSuchName:
		    if (logger.finestOn())
			logger.finest("makeResponsePdu",
				      " update noSuchName counter");
		    adaptor.updateErrorCounters(
                            SnmpDefinitions.snmpRspNoSuchName);
		   
		case SnmpDefinitions.snmpRspReadOnly:
		    if (logger.finestOn())
			logger.finest("makeResponsePdu", 
				      " update bad community uses");
		    adaptor.incSnmpInBadCommunityUses(1);
		    
		    InetAddressAcl acl = adaptor.getInetAddressAcl();
		    final String ctxtString = 
			new String(pendingReq.getAccessContext());
		    if(acl.checkCommunity(ctxtString) == false) {
			if (logger.finestOn())
			    logger.finest("makeResponsePdu", 
					  "update bad community "+
					  "names");
			adaptor.incSnmpInBadCommunityNames(1);
		    }
		    
		    
		    // reqPdu is rejected by ACLs
		    // respPdu contains the error response to be sent.
		    // We send this response only if authResEnabled is true.
		    // No response should be sent
		    if (!adaptor.getAuthRespEnabled()) {
			respPdu = null;
			pendingReq.noResponse();
		    }
		    else {
			err = SnmpSubRequestHandler.mapErrorStatus(
			          SnmpDefinitions.snmpRspAuthorizationError,
				  reqPdu.version, reqPdu.type);

			respPdu = newErrorResponsePdu(reqPdu, err, 0);
		    }
		    
		    if (adaptor.getAuthTrapEnabled()) { // A trap must be sent
			try {
			    adaptor.snmpV1Trap(
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
		    return respPdu;
		case SnmpDefinitions.noSuchContext:
		    if (logger.finestOn())
			logger.finest("makeResponsePdu", 
			      "No such context context. Dropping the request");
		    pendingReq.noResponse();
		    // What about counter increment?
		    // adaptor.incSnmpUnknownContexts(1);
		    return null;
		case SnmpDefinitions.snmpRspAuthorizationError:
		case SnmpDefinitions.noSuchView:
		case SnmpDefinitions.noGroupName:
		default:
		    if (logger.finestOn())
			logger.finest("makeResponsePdu", 
				      "AuthorizationError.");
		    err = SnmpSubRequestHandler.mapErrorStatus(
				SnmpDefinitions.snmpRspAuthorizationError,
				reqPdu.version, reqPdu.type);
		}
		
		if (logger.finerOn())
		    logger.finer("makeResponsePdu", "Mapped status : " + err);
		respPdu = newErrorResponsePdu(reqPdu, err, 0);
		return respPdu;
	    }
	    catch(SnmpUnknownAccContrModelException x) {
		if (logger.finerOn())
		    logger.finer("makeResponsePdu", 
			  "SnmpUnknownAccContrModelException");
		respPdu = newErrorResponsePdu(reqPdu, 
					      SnmpDefinitions.snmpRspGenErr,0);
		return respPdu;
	    }
	    
	    if (logger.finerOn())
		logger.finer("makeResponsePdu", "Access Granted to " 
		      + pendingReq.getPrincipal());
	    
	    switch(reqPdu.type) {
	    case SnmpPdu.pduGetRequestPdu:
	    case SnmpPdu.pduGetNextRequestPdu:
	    case SnmpPdu.pduSetRequestPdu:
		respPdu = makeGetSetResponsePdu(pendingReq,
						(SnmpPdu)reqPdu,
						userData) ;
		break ;
		
	    case SnmpPdu.pduGetBulkRequestPdu:
		respPdu = makeGetBulkResponsePdu(pendingReq,
						 (SnmpPduBulkType) reqPdu,
						 userData) ;
		break ;
	    }
	}
        return respPdu ;
    }
    
    /**
     * Here we make the response pdu from a  request to forward.
     */
    private SnmpPdu makeForwardResponsePdu(SnmpIncomingRequest pendingReq,
					   SnmpPdu req,
					   SnmpRequestForwarder forwarder) {
	try {
	    return forwarder.forward(req);
	} catch(SnmpStatusException e) {
	    if (logger.finestOn()) {
		logger.finest("makeForwardResponsePdu", 
			      "Failure when forwarding, return null.");
	    }
	    return null;
	}
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
	SnmpPdu makeErrorVarbindPdu(SnmpPdu req, int statusTag) {

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
    SnmpPdu makeNoMibErrorPdu(SnmpPdu req, Object userData) {
        // There is no agent registered
        //
        if (req.version == SnmpDefinitions.snmpVersionOne) {
            // Version 1: => NoSuchName
            return 
                newErrorResponsePdu(req,snmpRspNoSuchName,1);
        } else if ((req.version == SnmpDefinitions.snmpVersionTwo) || 
		   (req.version == SnmpDefinitions.snmpVersionThree) ) {
            // Version 2 (and 3): => depends on PDU type
            switch (req.type) {
            case pduSetRequestPdu :
            case pduWalkRequest : 
                // SET request => NoAccess
                return 
                    newErrorResponsePdu(req,snmpRspNoAccess,1);
            case pduGetRequestPdu : 
                // GET request => NoSuchObject
                return makeErrorVarbindPdu(req,
			   SnmpDataTypeEnums.errNoSuchObjectTag);
            case pduGetNextRequestPdu :
            case pduGetBulkRequestPdu : 
                // GET-NEXT or GET-BULK => EndOfMibView
                return makeErrorVarbindPdu(req,
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
    private SnmpPdu makeGetSetResponsePdu(SnmpIncomingRequest pendingReq,
					  SnmpPdu req,
					  Object userData) {
	
        // Create the trhead group specific for handling sub-requests 
	// associated to the current request. Use the invoke id
        //
        // Nice idea to use a thread group on a request basis. 
	// However the impact on performance is terrible !
        // theGroup= new ThreadGroup(thread.getThreadGroup(), "request " + 
	//           String.valueOf(req.requestId));
    
        // Let's build the varBindList for the response pdu
        //
   
        if (req.varBindList == null) {
            // Good ! Let's make a full response pdu.
            //
            return newValidResponsePdu(req, null) ;
        }

        // First we need to split the request into subrequests
        //
	SnmpPdu result = null;
        result = splitRequest(pendingReq,
			      req);

	if(result != null) 
	    return result;
	    
        int nbSubRequest= subs.size();
        if (nbSubRequest == 1)
            return turboProcessingGetSet(req,userData);
	
	
        // Execute all the subrequests resulting from the split of the 
	// varbind list.
        //
        result = executeSubRequest(req,userData);
        if (result != null)
            // It means that an error occured. The error is already 
	    // formatted by the executeSubRequest method.
	    //
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
    private SnmpPdu executeSubRequest(SnmpPdu req, Object userData) {
    
        int errorStatus = SnmpDefinitions.snmpRspNoError ;
        int nbSubRequest= subs.size();
             
        // If it's a set request, we must first check any varBind
        //
        if (req.type == pduSetRequestPdu) {
     
            int i=0;
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
                    return newErrorResponsePdu(req, errorStatus,
					       sub.getErrorIndex() + 1) ;
                }
            }
        }// end processing check operation for a set PDU.
    
        // Let's start the sub-requests.
        // 
        int i=0;
        for(Enumeration e= subs.elements(); e.hasMoreElements() ; i++) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
	    sub.setUserData(userData);

	    sub.run();

            if (sub.getErrorStatus() != SnmpDefinitions.snmpRspNoError) {
                // No point to go any further.
                //
                if (logger.finestOn()) {
                    logger.finest("executeSubRequest", "an error occurs");
                }
                int realIndex= sub.getErrorIndex() + 1;
                //SnmpSubRequestHandler.recycleSubRequestHandler(sub);
                return newErrorResponsePdu(req, errorStatus, realIndex) ;
            }
        } // everybody has completed successfully the request.
    
        // everything is ok
        //
        return null;
    }
  
    /**
     * Optimize when there is only one sub request
     */
    private SnmpPdu turboProcessingGetSet(SnmpPdu req,
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
    private SnmpPdu makeGetBulkResponsePdu(SnmpIncomingRequest pendingReq,
					   SnmpPduBulkType req, 
					   Object userData) {
   
        SnmpVarBind[] respVarBindList = null ;
	SnmpVarBind[] list = ((SnmpPdu)req).varBindList;

	if (list == null) {
            // Good ! Let's make a full response pdu.
            //
            return newValidResponsePdu((SnmpPdu) req, null) ;
        }
	
        // RFC 1905, Section 4.2.3, p14
        int L = list.length ;
        int N = Math.max(Math.min(req.getNonRepeaters(), L), 0) ;
        int M = Math.max(req.getMaxRepetitions(), 0) ;
        int R = L - N ;
    
        // Split the request into subrequests.
        //
        SnmpPdu result = splitBulkRequest(pendingReq, req, N, M, R);

	if(result != null) 
	    return result;

        result= executeSubRequest((SnmpPdu) req,userData);
        if (result != null)
            return result;
    
        respVarBindList= mergeBulkResponses(N + (M * R));

        // Now we remove useless trailing endOfMibView.
        //
        int m2 ; // respVarBindList[m2] item and next are going to be removed
        int t = respVarBindList.length ;
        while ((t > N) && 
	       (respVarBindList[t-1].getSnmpValue().
		equals(SnmpVarBind.endOfMibView))) {
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
        return newValidResponsePdu((SnmpPdu)req, respVarBindList) ;
    }
  
    /**
     * Check the type of the pdu: only the get/set/bulk request
     * are accepted.
     */
    private boolean checkPduType(SnmpPdu pdu) {

        boolean result = true ;

	if(pdu.type == SnmpDefinitions.pduGetBulkRequestPdu &&
	   pdu.version == SnmpDefinitions.snmpVersionOne) {
	    if (logger.finestOn()) {
		logger.finest("checkPduType", 
		      "Received a getbulk in V1, rejecting " + 
		      "the request");
		return false;
	    }
	}      
	
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
     * Make a response pdu with the specified error status and index.
     * NOTE: the response pdu share its varBindList with the request pdu. 
     */
    private SnmpPdu newValidResponsePdu(SnmpPdu reqPdu, 
					SnmpVarBind[] varBindList) {
	SnmpPdu result = null;
	SnmpAckPdu ackpdu = (SnmpAckPdu) reqPdu;
	result = ackpdu.getResponsePdu(); 
	result.varBindList = varBindList;
	
	adaptor.updateErrorCounters(((SnmpPduRequestType)result).
				    getErrorStatus()) ;
        return result ;
    }
  
    /**
     * Make a response pdu with the specified error status and index.
     * NOTE: the response pdu share its varBindList with the request pdu. 
     */
    private SnmpPdu newErrorResponsePdu(SnmpPdu req, int s, int i) {
        SnmpPduRequestType result = (SnmpPduRequestType) 
	    newValidResponsePdu(req, null) ;

	result.setErrorStatus(s);
        result.setErrorIndex(i);
        ((SnmpPdu) result).varBindList = req.varBindList ;

        adaptor.updateErrorCounters(result.getErrorStatus()) ;
    
        return (SnmpPdu) result ;
    }

    private void newTooBigMessage(SnmpIncomingRequest pendingReq) 
	throws SnmpTooBigException {
        SnmpMsg result = null ;
        SnmpPdu reqPdu = null ;
    
        try {
            reqPdu = pendingReq.decodeSnmpPdu();
            if (reqPdu != null) {
                SnmpPdu respPdu = newTooBigPdu(reqPdu);
		
		pendingReq.encodeSnmpPdu(respPdu, packet.getData().length);
            }
        }
        catch(SnmpStatusException x) {
            // This should not occur because decodeIncomingRequest has normally
            // been successfully called before.
            throw new InternalError() ;
        }
    
    }
  
    private SnmpPdu newTooBigPdu(SnmpPdu req) {
        SnmpPdu result = 
	    newErrorResponsePdu(req, SnmpDefinitions.snmpRspTooBig, 0) ;
        result.varBindList = null ;
        return result ;
    }

    private SnmpPdu reduceResponsePdu(SnmpPdu req, SnmpPdu resp, 
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
        //     position (-1 to be safe),
        //   * when it is 1, we only put one (big?) item in the varbindlist
        //   * when it is 0 (in fact, acceptedVbCount is not available), 
        //     we split the varbindlist by 2.
        //
        int vbCount = resp.varBindList.length ;
        if (acceptedVbCount >= 3)
            vbCount = Math.min(acceptedVbCount - 1, resp.varBindList.length) ;
        else if (acceptedVbCount == 1)
            vbCount = 1 ;
        else // acceptedCount == 0 ie it is unknown
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


    private SnmpPdu handleAccessException(int status,
					  int index,
					  SnmpPdu req,
					  SnmpVarBind var,
					  SnmpAdaptorServer snmpServer) {
	SnmpPdu pdu = null;
	int err = 0;
	if(req.version != SnmpDefinitions.snmpVersionThree) {
	    if (!snmpServer.getAuthRespEnabled()) { 
		// No response should be sent
	    }
	    if (snmpServer.getAuthTrapEnabled()) { 
		// A trap must be sent
		try {
		    snmpServer.snmpV1Trap(
			       SnmpPduTrap.trapAuthenticationFailure, 0, 
			       new SnmpVarBindList()) ;
		}
		catch(Exception x) {
		    if (logger.finestOn()) {
			logger.finest("handleAccessException", 
				 "failure when sending authentication trap");
			logger.finest("handleAccessException", x);
		    }
		}
	    }
	    if(req.version == SnmpDefinitions.snmpVersionOne) {
		err = SnmpSubRequestHandler.mapErrorStatus(status,
							   req.version,
							   req.type);
		pdu = newErrorResponsePdu(req, err, index);
	    }
	    else {
		err = SnmpSubRequestHandler.mapErrorStatus(status,
							   req.version,
							   req.type);
		if(req.type == SnmpDefinitions.pduSetRequestPdu)
		    pdu = newErrorResponsePdu(req, err, index);
		else {
		    var.setSnmpValue(new SnmpNull(err));
		}
	    }
	}
	else {
	    err = SnmpSubRequestHandler.mapErrorStatus(status,
						       req.version,
						       req.type);
	    if(req.type == SnmpDefinitions.pduSetRequestPdu)
		pdu = newErrorResponsePdu(req, err, index);
	    else
		var.setSnmpValue(new SnmpNull(err));
	}
	return pdu;
    }
    

    private Object checkOidAccess(SnmpIncomingRequest pendingReq,
				  SnmpPdu req,
				  SnmpVarBind varBind,
				  int index) {
	SnmpPdu pdu = null;
	try {
	    accessSubSys.checkAccess(req.version,
				     pendingReq.getPrincipal(),
				     pendingReq.getSecurityLevel(),
				     req.type,
				     pendingReq.getSecurityModel(),
				     pendingReq.getAccessContext(),
				     varBind.getOid());
	}
	catch(SnmpStatusException e) {
	    switch(e.getStatus()) {
	    case SnmpDefinitions.snmpRspAuthorizationError:
	    case SnmpDefinitions.notInView:
	    default:
		if (logger.finerOn())
		    logger.finer("checkOidAccess", "SnmpStatusException : "
			  + e.getStatus());
		if(req.version == SnmpDefinitions.snmpVersionOne) {
		    pdu = newErrorResponsePdu(req, 
					   SnmpDefinitions.snmpRspNoSuchName, 
					   index + 1);
		    return pdu;
		} else {
		    return SnmpVarBind.noSuchInstance;
		}
	    }
	}
	catch(SnmpUnknownAccContrModelException x) {
	    pdu =  newErrorResponsePdu(req, SnmpDefinitions.snmpRspGenErr, 
				       index + 1);
	}
	return pdu;
    }
    
    /**
     * The method takes the incoming requests and split it into subrequests.
     */

    private SnmpPdu splitRequest(SnmpIncomingRequest pendingReq,
				 SnmpPdu req) {
	SnmpPdu pdu = null;
	
        int nbAgents= currentMibs.size();
	
	if (logger.finerOn())
	    logger.finer("splitRequest", "Nb agents : " + nbAgents);
	
	SnmpSubRequestHandler sub = null;
	int nbReqs= req.varBindList.length;
        SnmpVarBind[] vars= req.varBindList;
	SnmpMibAgent agent = null;

	// For the get next operation we are going to send the varbind list
	// to all agents
	//
	if (req.type == pduGetNextRequestPdu) {
	    if (logger.finerOn())
		logger.finer("splitRequest", 
			     "Doing getNext on muliple agents");
	    for(Enumeration e= currentMibs.elements(); e.hasMoreElements(); ){
		SnmpSubNextRequestHandler subnext = null;
		SnmpMibAgent ag= (SnmpMibAgent) e.nextElement();
		subnext = new SnmpSubNextRequestHandler(engine,
							pendingReq, 
							ag, 
							req,
							currentRoot);
		subs.put(ag, subnext);
		if (logger.finerOn())
		    logger.finer("splitRequest", "Doing getNext on agent [" + 
			  ag + "]");
		
	    }
	    return null;
	}
	
	if (logger.finerOn())
	    logger.finer("splitRequest", "Access Granted to " 
		  + pendingReq.getPrincipal() + nbReqs);
	Object err = null;
	for(int i = 0; i < nbReqs; i++) {
	    err = checkOidAccess(pendingReq, req, vars[i], i);
	    // Error handling
	    if(err != null) {
		if(logger.finestOn())
		    logger.finest("splitRequest", "Access NOT " +
			  "granted for OID:"
			  + vars[i].getOid());
		//Snmp V1 error
		if(err instanceof SnmpPdu)
		    return (SnmpPdu) err;
		else { //Snmp V2 error
		    vars[i].setSnmpValue((SnmpValue) err);
		    continue;
		}
	    }
	    
	    if (logger.finestOn())
		logger.finest("splitRequest", "Access granted for OID : " 
		      + vars[i].getOid());
	    
	    agent = currentRoot.getAgentMib(vars[i].getOid());
	    
	    if(logger.finerOn())
		logger.finer("splitRequest","Found mib [" + agent + 
			     "] for oid " + vars[i].getOid());
	    
	    sub= (SnmpSubRequestHandler) subs.get(agent);
	    if (sub == null) {
		sub = new SnmpSubRequestHandler(engine, 
						pendingReq, 
						agent, 
						req);
		subs.put(agent, sub);
	    }
	    sub.updateRequest(vars[i], i);
	}
	return null; 
    }
    
    /**
     * The method takes the incoming get bulk requests and split it into 
     * subrequests.
     */
    private SnmpPdu splitBulkRequest(SnmpIncomingRequest pendingReq,
				     SnmpPduBulkType req, 
				     int nonRepeaters, 
				     int maxRepetitions, 
				     int R) {
        int nbAgents= currentMibs.size();
	
	if(nbAgents == 1) {
            // Take all the oids contained in the request and 
            //
	    SnmpMibAgent agent= (SnmpMibAgent) currentMibs.firstElement();
            subs.put(agent, new SnmpSubBulkRequestHandler(engine,
							  pendingReq,
							  agent, 
							  (SnmpPdu)req, 
							  nonRepeaters, 
							  maxRepetitions, 
							  R,
							  currentRoot));
            return null;
	}
	
	for(Enumeration e= currentMibs.elements(); e.hasMoreElements(); ){
	    SnmpSubBulkRequestHandler subbulk = null;
	    SnmpMibAgent ag= (SnmpMibAgent) e.nextElement();
	    subbulk = new SnmpSubBulkRequestHandler(engine,
						    pendingReq,
						    ag, 
						    (SnmpPdu)req, 
						    nonRepeaters,
						    maxRepetitions, 
						    R,
						    currentRoot);
	    subs.put(ag, subbulk);
	}
	
	return null;
    }
    
    private SnmpPdu mergeResponses(SnmpPdu req) {
    
        if (req.type == pduGetNextRequestPdu) {
            return mergeNextResponses(req);
        }
      
        SnmpVarBind[] result= req.varBindList;
  
        // Go through the list of subrequests and concatenate. Hopefully, 
	// by now all the sub-requests should be finished
        //
        for(Enumeration e= subs.elements(); e.hasMoreElements();) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
            sub.updateResult(result);
        }
        return newValidResponsePdu(req,result);
    }
  
    private SnmpPdu mergeNextResponses(SnmpPdu req) {
        int max= req.varBindList.length;
        SnmpVarBind[] result= new SnmpVarBind[max];
    
        // Go through the list of subrequests and concatenate. Hopefully, 
	// by now all the sub-requests should be finished
        //
        for(Enumeration e= subs.elements(); e.hasMoreElements();) {
            SnmpSubRequestHandler sub= (SnmpSubRequestHandler) e.nextElement();
            sub.updateResult(result);
        }
    
        if (req.version == snmpVersionTwo ||
	    req.version == snmpVersionThree) {
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
        return "SnmpV3RequestHandler[" + adaptorServer.getProtocol() + ":" +
	    adaptorServer.getPort() + "]";
    }

    ClassLogger makeLogger(String dbgTag) {
	return new ClassLogger(ClassLogger.LOGGER_ADAPTOR_SNMP, dbgTag);
    }

    
    Thread createThread(Runnable r) {
	return null;
    }


    static final private String InterruptSysCallMsg = 
	"Interrupted system call";

    static final private SnmpStatusException noSuchNameException =
        new SnmpStatusException(SnmpDefinitions.snmpRspNoSuchName) ;
}

