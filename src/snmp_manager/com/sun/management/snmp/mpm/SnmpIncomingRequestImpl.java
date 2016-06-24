/*
 * @(#)file      SnmpIncomingRequestImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.34
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

package com.sun.management.snmp.mpm;

// java imports
//
import java.util.Vector;
import java.net.InetAddress;

// import debug stuff
//
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.SnmpScopedPduPacket;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpScopedPduRequest;
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpSecurityException;

import com.sun.management.internal.snmp.SnmpIncomingRequest;
import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.internal.snmp.SnmpSecuritySubSystem;
import com.sun.management.internal.snmp.SnmpSecurityModel;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;
import com.sun.management.internal.snmp.SnmpSecurityCache;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;
import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpBadSecurityLevelException;

/**
 * FOR INTERNAL USE ONLY. Implements SnmpIncomingRequest interface.
 *
 * @since Java DMK 5.1
 */
class SnmpIncomingRequestImpl implements SnmpIncomingRequest {
    SnmpMsg req = null;
    SnmpMsg resp = null;
    SnmpSecuritySubSystem secureSubSys = null;
    SnmpMsgTranslator translator = null;
    String principal = null;
    SnmpSecurityCache cache = null;
    boolean isReport = false;
    boolean isResponse = true;
    SnmpSecurityParameters secParams = null;
    SnmpPduFactory factory = null;
    /**
     * Constructor.
     */
    public SnmpIncomingRequestImpl(SnmpSecuritySubSystem secureSubSys,
				   SnmpPduFactory factory,
				   SnmpMsg req,
				   SnmpMsg resp,
				   SnmpMsgTranslator translator) {
	this.secureSubSys = secureSubSys;
	this.factory = factory;
	this.req = req;
	this.resp = resp;
	this.translator = translator;
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public SnmpSecurityParameters getSecurityParameters() {
	return secParams;
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public boolean isReport() { return isReport; }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public boolean isResponse() { return isResponse; }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public void noResponse() { isResponse = false; }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public int getSecurityLevel() {
	return translator.getSecurityLevel(req);
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public byte[] getContextEngineId() {
	return translator.getContextEngineId(req);
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public int getSecurityModel() {
	return translator.getMsgSecurityModel(req);
    }
    /**
     * When a report is to be sent (flag set), a Request pdu is constructed to store the report data.
     */
    private void handleReport(SnmpMsg msg,
			      SnmpSecurityException e) 
	throws SnmpStatusException {
	//If it is encrypted, can't decode the pdu. So Check the flag only.
	//
	if((translator.getMsgFlags(msg) & SnmpDefinitions.privMask) != 0) {
	    if ((translator.getMsgFlags(msg) & SnmpDefinitions.reportableFlag) != 0) {
		if(logger.finestOn())
		    logger.finest("handleReport", "Report to send. Encryption + flag set");
		constructReport(msg, e, null);
		return;
	    }
	    else {
		if(logger.finestOn())
		    logger.finest("handleReport", "No Report to send. No flag set");
		throw new SnmpStatusException("Security failed");
	    }
	    
	}
	
	//We can decode the pdu
	//
	SnmpPdu pdu = factory.decodeSnmpPdu(msg);
	//Can't decode the pdu.
	if(pdu == null)
	    //If the flag is set, send a report.
	    if((translator.getMsgFlags(msg) & SnmpDefinitions.reportableFlag) != 0) {
		if(logger.finestOn())
		    logger.finest("handleReport", "Can't decode pdu. Flag set");
		constructReport(msg, e, null);
		return;
	    }

	if(pdu.type == SnmpDefinitions.pduGetRequestPdu ||
	   pdu.type == SnmpDefinitions.pduGetNextRequestPdu  ||
	   pdu.type == SnmpDefinitions.pduSetRequestPdu  || 
	   pdu.type == SnmpDefinitions.pduGetBulkRequestPdu || 
	   pdu.type == SnmpDefinitions.pduInformRequestPdu) {
	    //It is of the confirmed class, send the report.
	    constructReport(req, e, pdu);
	}
	else {
	    if(logger.finestOn())
		logger.finest("handleReport", "Not a confirmed pdu class [ " + pdu.type + "]. No report to send"); 
	    throw new SnmpStatusException("Security failed");
	}
    }
    
    /*
     * Construct the report to send
     */
    private void constructReport(SnmpMsg msg,
				 SnmpSecurityException e,
				 SnmpPdu p) 
	throws SnmpStatusException {
	if(logger.finerOn())
	    logger.finer("constructReport", "Report to send. Exception: " + 
		  e + " Security parameters : " + e.params);

	isReport = true;
	SnmpScopedPduRequest pdu = new SnmpScopedPduRequest();
	pdu.address = msg.address;
	pdu.port = msg.port;
	if(p != null)
	    pdu.requestId = p.requestId;
	pdu.version = SnmpDefinitions.snmpVersionThree;
	pdu.msgId = translator.getMsgId(msg);
	pdu.type = SnmpDefinitions.pduReportPdu;
	pdu.varBindList = e.list;
	pdu.contextEngineId = e.contextEngineId;
	pdu.contextName = e.contextName;
	//Remove the reportable flag.
	pdu.msgFlags = (byte) (e.flags & (int)SnmpDefinitions.authPriv);
	pdu.msgMaxSize = translator.getMsgMaxSize(msg);
	pdu.msgSecurityModel = translator.getMsgSecurityModel(msg);
	pdu.securityParameters = e.params;
	if(logger.finerOn())
	    logger.finer("constructReport", " Security parameters : " + pdu.securityParameters);
	pdu.setErrorIndex(0);
	pdu.setErrorStatus(0);
	try {
	    resp.encodeSnmpPdu(pdu, translator.getMsgMaxSize(msg));
	}catch(SnmpTooBigException ex) {
	    throw new SnmpStatusException("Too Big when sending report");
	}

    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public byte[] getContextName() {
	return translator.getContextName(req);
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public byte[] getAccessContext() {
	return translator.getAccessContext(req);
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public String getPrincipal() {
	return secParams.getPrincipal();
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public int encodeMessage(byte[] outputBytes)
	throws SnmpTooBigException {
	int encodingLength = 0;
	try {
	    //When encoding the response message, the reportable flag must be erased.
	    encodingLength = 
		secureSubSys.generateResponseMsg(cache,
						 resp.version,
						 translator.getMsgId(resp),
						 translator.getMsgMaxSize(resp),
						 (byte) (translator.getMsgFlags(resp) & (int) SnmpDefinitions.authPriv),
						 translator.getMsgSecurityModel(resp),
						 translator.getSecurityParameters(resp),
						 translator.getContextEngineId(resp),
						 translator.getRawContextName(resp),
						 resp.data,
						 resp.dataLength,
						 outputBytes);
	secureSubSys.releaseSecurityCache(translator.getMsgSecurityModel(req),
					  cache);
	}catch(SnmpStatusException e) {
	    isResponse = false; 
	}
	catch(SnmpSecurityException x) {
	    isResponse = false; 
	}
	catch(SnmpUnknownSecModelException y) {
	    isResponse = false; 
	}
	return encodingLength;
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public void decodeMessage(byte[] inputBytes, 
			      int byteCount, 
			      InetAddress address,
			      int port) 
        throws SnmpStatusException, SnmpUnknownSecModelException, 
	       SnmpBadSecurityLevelException {
	req.address = address;
	req.port = port;
	req.decodeMessage(inputBytes, byteCount);
	cache = secureSubSys.createSecurityCache(translator.getMsgSecurityModel(req));
	SnmpDecryptedPdu decrPdu = new SnmpDecryptedPdu();

	SnmpEngineImpl.checkSecurityLevel(translator.getMsgFlags(req));
	try {
	    secParams = 
		secureSubSys.processIncomingRequest(cache,
						    req.version,
						    translator.getMsgId(req),
						    translator.getMsgMaxSize(req),
						    translator.getMsgFlags(req),
						    translator.
						    getMsgSecurityModel(req),
						    translator.
						    getFlatSecurityParameters(req),
						    translator.
						    getContextEngineId(req),
						    translator.getContextName(req),
						    req.data,
						    translator.
						    getEncryptedPdu(req),
						    decrPdu);
	}catch(SnmpSecurityException e) {
	    if (logger.finestOn()) {
                logger.finest("decodeMessage", "Security error: " + 
		      e + ", Msg flags: " + translator.getMsgFlags(req));
            }
	    isResponse = false;
	    handleReport(req, e);
	    return;
	}
	catch(SnmpStatusException ex) {
	    isResponse = false;
	    throw ex;
	}
	req.securityParameters = secParams;
	if(decrPdu.data != null) {
	    req.data = decrPdu.data;
	    translator.setContextName(req, decrPdu.contextName);
	    translator.setContextEngineId(req, decrPdu.contextEngineId);
	}
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public SnmpMsg encodeSnmpPdu(SnmpPdu p, 
				 int maxDataLength) 
        throws SnmpStatusException, SnmpTooBigException {
	resp = factory.encodeSnmpPdu(p, maxDataLength);
	return resp;
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public SnmpPdu decodeSnmpPdu() 
	throws SnmpStatusException {
	return factory.decodeSnmpPdu(req);
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public String printRequestMessage() {
	return req.printMessage();
    }
    /**
     * See SnmpIncomingRequest interface doc.
     */
    public String printResponseMessage() {
	return resp.printMessage();
    }

    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,
			"SnmpIncomingRequestImpl");

    String dbgTag = "SnmpIncomingRequestImpl";
}
