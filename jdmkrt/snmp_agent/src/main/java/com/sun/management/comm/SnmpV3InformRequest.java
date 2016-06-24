/*
 * @(#)file      SnmpV3InformRequest.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.32
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

package com.sun.management.comm ;


// JAVA imports
//
import java.io.Serializable;
import java.net.InetAddress;
import java.util.Vector;
import java.util.Date;

// JMX imports
//
import com.sun.management.snmp.SnmpMessage;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpPduRequest;
import com.sun.management.snmp.SnmpPduBulk;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpScopedPduRequest;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpPduRequestType;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpSecurityException;
import com.sun.management.snmp.SnmpV3Message;
// Java DMK imports
//
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.internal.snmp.SnmpOutgoingRequest;
import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpBadSecurityLevelException;
import com.sun.management.snmp.SnmpUnknownMsgProcModelException;
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;

/**
 * This class is used by the {@link com.sun.management.comm.SnmpV3AdaptorServer 
 * SNMP adaptor server} to send inform requests
 * to an SNMP V3 manager and receive inform responses.
 * <P>
 * This class provides basic functions that enable you to fire inform requests,
 * handle retries, timeouts, and process responses from the manager.
 * <BR>
 * The SNMP adaptor server specifies the destination of the inform request 
 * and controls the size of a single inform request/response to fit into its 
 * <CODE>bufferSize</CODE>.
 * It specifies the maximum number of tries and the timeout to be used for 
 * the inform requests.
 * <P>
 * Each inform request, when ready to be sent, is assigned a unique 
 * identifier which helps in identifying the inform request with matching 
 * inform responses to the protocol engine lying transparently underneath.
 * The engine does the job of retrying the inform requests when the timer 
 * expires and calls the SNMP adaptor server when a timeout occurs after 
 * exhausting the maximum number of tries.
 * <P>
 * The inform request object provides the method, 
 * {@link #waitForCompletion waitForCompletion(long time)},
 * which enables a user to operate in a synchronous mode with an inform 
 * request.
 * This is done by blocking the user thread for the desired time interval.
 * The user thread gets notified whenever a request reaches completion, 
 * independently of the status of the response. 
 * <P>
 * If an {@link com.sun.management.comm.SnmpInformHandler inform callback} is 
 * provided when sending the inform request, the user operates in an 
 * asynchronous mode with the inform request. The user thread is not blocked
 * and the specific inform callback implementation provided by the user is 
 * invoked when the inform response is received.
 *
 *
 * @since Java DMK 5.1
 */

public class SnmpV3InformRequest extends SnmpInformRequest {
    //The SNMP V3 response handler
    private SnmpV3ResponseHandler respHandler = null;
    //The SNMP V3 pdu.
    private SnmpScopedPduRequest v3pdu = null;
    //The SNMP V3 adaptor.
    private SnmpV3AdaptorServer v3adaptor = null;
    private SnmpVarBindList reportVarBindList = null;
      
    private String dbgTag = "SnmpV3InformRequest";

    /**
     * For Java DMK internal use only.
     * Constructor for creating new inform request. This object can be 
     * created only by an SNMP adaptor object. 
     * @param session <CODE>SnmpSession</CODE> object for this inform request.
     * @param adp <CODE>SnmpAdaptorServer</CODE> object for this inform 
     *             request.
     * @param addr The <CODE>InetAddress</CODE> destination for this 
     *             inform request.
     * @param v3pdu The pdu to be used for the inform request.
     * @param requestCB Callback interface for the inform request.
     * @exception SnmpStatusException SNMP adaptor is not ONLINE or
     *            session is dead.
     */
    SnmpV3InformRequest(SnmpV3Session session, 
			SnmpV3ResponseHandler respHandler,
			SnmpV3AdaptorServer adp, 
			InetAddress addr, 
			SnmpScopedPduRequest v3pdu, 
			SnmpInformHandler requestCB) 
        throws SnmpStatusException {
        super(session, adp, addr, null, v3pdu.port, requestCB);
	this.respHandler = respHandler;
        v3adaptor = adp;
	this.v3pdu = v3pdu;
	//v3pdu.port = v3adaptor.getInformPort();
	v3pdu.type = pduInformRequestPdu;
	v3pdu.version = snmpVersionThree;
	v3pdu.address = addr;
    }
    /**
     * Gets the report <CODE>SnmpVarBindList</CODE>. The contents of it are 
     * not guaranteed to be consistent when the request is active. 
     * It should be called if <CODE>isReport()</CODE> is true.
     * @return The <CODE>SnmpVarBindList</CODE> received in the report.
     */
    public SnmpVarBindList getReportVarBindList() {
	return reportVarBindList;
    }
    /**
     * Parses the inform response packet. If the agent responds with error set,
     * it does not parse any further.
     */
    synchronized void parsePduPacket(SnmpPduRequestType rpdu) {
        
        if (rpdu == null)
            return;

	if (logger.finerOn())
            logger.finer("parsePduPacket", "received inform response. "+ 
			 "ErrorStatus/ErrorIndex = " + errorStatus + "/" + 
			 errorIndex);
	
	errorStatus = rpdu.getErrorStatus();
        errorIndex = rpdu.getErrorIndex();
	
        if (errorStatus == snmpRspNoError) {
	    if(((SnmpPdu)rpdu).type == SnmpDefinitions.pduReportPdu) {
		if (logger.finerOn())
		    logger.finer("parsePduPacket", "received inform report.");
		updateReportVarBindList(((SnmpPdu)rpdu).varBindList);
	    }
	    else {
		if (logger.finerOn())
		    logger.finer("parsePduPacket", "received inform reponse.");
		updateInternalVarBindWithResult(((SnmpPdu)rpdu).varBindList);
	    }
            return;
        }
	
        if (errorStatus != snmpRspNoError)  
            --errorIndex;  // rationalize for index to start with 0.
	
        if (logger.finerOn()) {
            logger.finer("parsePduPacket", "received inform response. "+
			 "ErrorStatus/ErrorIndex = " + errorStatus + "/" + 
			 errorIndex);
        }
    }
    private void updateReportVarBindList(SnmpVarBind[] list) {
        reportVarBindList = new SnmpVarBindList() ;
        for(int i=0; i < list.length; i++) {
            SnmpVarBind bind= list[i];
            SnmpVarBind var = new SnmpVarBind(bind.getOid(), 
					      bind.getSnmpValue()) ;
            reportVarBindList.addVarBind(var) ;
        }
    }

    //Overloaded from SnmpInformRequest
    // The scoped pdu has been provided at construction time.
    synchronized SnmpPdu constructPduPacket() { 
	v3pdu.varBindList = internalVarBind;
	v3pdu.requestId = getRequestId();
	v3pdu.msgFlags |= (int) SnmpDefinitions.reportableFlag;
	return v3pdu;
    }
    
    /**
     * Test if the received reply is an SNMP report.
     * @return The test result. <CODE>true</CODE> if a report is received, 
     * <CODE>false</CODE> otherwise.
     */
    public boolean isReport() {
	if(responsePdu == null) {
	    if (logger.finestOn())
		logger.finest("isReport", "responsePdu is null");
	    return false;
	}
	else
	    if (logger.finestOn())
		logger.finest("isReport", "responsePdu not null");
	
	return (((SnmpPdu)responsePdu).type == SnmpDefinitions.pduReportPdu);
    }
    /**
     * Test if the received reply is an SNMP response.
     * @return The test result. <CODE>true</CODE> if a response is received,
     * <CODE>false</CODE> otherwise.
     */
    public boolean isResponse() {
	if(responsePdu == null) { 
	    if (logger.finestOn())
		logger.finest("isResponse", "responsePdu is null");
	    return false;
	}
	else
	    if (logger.finestOn())
		logger.finest("isResponse", "responsePdu not null");
	
	return (((SnmpPdu)responsePdu).type == 
		SnmpDefinitions.pduGetResponsePdu);
    }

    //A typical engine framework pdu sending.
    boolean sendPdu() {
        try {
            responsePdu = null;
	    reportVarBindList = null;
	    SnmpPduFactory pduFactory = v3adaptor.getPduFactory();

	    // Access the engine.
	    //
	    SnmpEngineImpl eng = (SnmpEngineImpl) v3adaptor.getEngine();

	    // Access the msg processing sub system.
	    //
	    SnmpMsgProcessingSubSystem msgsys = 
		eng.getMsgProcessingSubSystem();

	    // Ask the subsys to return the appropriate SnmpOutgoingRequest 
	    // that will be used to send the call.
	    //
	    SnmpOutgoingRequest req = 
		msgsys.getOutgoingRequest(snmpVersionThree,
					  pduFactory);
	    
            SnmpMsg msg = req.encodeSnmpPdu(requestPdu, 
				       v3adaptor.getBufferSize().intValue());

            if (msg == null) {
                if (logger.finestOn()) {
                    logger.finest("sendPdu", 
				  "pdu factory returned a null value");
                }
                throw new SnmpStatusException(snmpReqUnknownError);

                // This exception will caught hereafter and reported as 
		// an snmpReqUnknownError
                // FIXME: may be it's not the best behavior ?
            }

	    //We need to find out the msgId for the response
	    //
	    ((SnmpV3Message)msg).msgId = getRequestId();
	    
            int maxPktSize = v3adaptor.getBufferSize().intValue();
            byte[] encoding = new byte[maxPktSize];
            int encodingLength = req.encodeMessage(encoding);
      
            if (logger.finerOn()) {
                logger.finer("sendPdu", "Dump : \n" + msg.printMessage());
            }
	    //We need to store the security cache in the response handler
	    //
	    respHandler.setSecurityCache(req.getSecurityCache());

            sendPduPacket(encoding, encodingLength);
            return true;
        } catch (SnmpTooBigException ar) {
    
            if (logger.finestOn()) {
                logger.finest("sendPdu", ar);
            }
      
            setErrorStatusAndIndex(snmpReqPacketOverflow, 
				   ar.getVarBindCount());
            requestPdu = null;
            reason = ar.getMessage();
            if (logger.finestOn()) {
                logger.finest("sendPdu", 
			      "Packet Overflow while building inform request");
            }
        } catch (java.io.IOException e) {
	    if (logger.finestOn()) {
                logger.finest("sendPdu", e.toString());
            }
            setErrorStatusAndIndex(snmpReqSocketIOError, 0);
            reason = e.getMessage();
        } catch (SnmpSecurityException e) {
            if (logger.finestOn()) {
                logger.finest("sendPdu", e.toString());
            }
            setErrorStatusAndIndex(e.status, 0);
            reason = e.getMessage();
        }
	catch (SnmpUnknownMsgProcModelException e) {
	    if (logger.finestOn()) {
                logger.finest("sendPdu", e.toString());
            }
            setErrorStatusAndIndex(snmpReqUnknownError, 0);
            reason = e.getMessage();
	}
	catch(SnmpUnknownSecModelException e) {
	    if(logger.finerOn()) 
		logger.finer("sendPdu", e.toString());
	    setErrorStatusAndIndex(snmpReqUnknownError, 0);
            reason = e.getMessage();
        }
        catch(SnmpBadSecurityLevelException e) {
	    if(logger.finerOn()) 
		logger.finer("sendPdu", e.toString());
            setErrorStatusAndIndex(snmpBadSecurityLevel, 0);
	    reason = e.getMessage();
        }
	catch(SnmpStatusException e) {
	    if(logger.finerOn()) 
		logger.finer("sendPdu", e.toString());
            setErrorStatusAndIndex(e.getStatus(), 0);
	    reason = e.getMessage();
	}
        return false;
    }

    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_ADAPTOR_SNMP,
			"SnmpV3InformRequest");
}
