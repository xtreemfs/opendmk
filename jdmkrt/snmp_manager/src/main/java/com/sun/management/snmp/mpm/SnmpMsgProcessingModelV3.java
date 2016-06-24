/*
 * @(#)file      SnmpMsgProcessingModelV3.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.37
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
import com.sun.jdmk.internal.ClassLogger;


import com.sun.management.internal.snmp.SnmpMsgProcessingModel;
import com.sun.management.internal.snmp.SnmpSubSystem;
import com.sun.management.internal.snmp.SnmpSecuritySubSystem;
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.internal.snmp.SnmpSecurityCache;
import com.sun.management.internal.snmp.SnmpModelImpl;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;
import com.sun.management.internal.snmp.SnmpIncomingResponse;
import com.sun.management.internal.snmp.SnmpIncomingRequest;
import com.sun.management.internal.snmp.SnmpOutgoingRequest;


import com.sun.management.snmp.SnmpEngineId;

import com.sun.management.snmp.manager.SnmpV3Parameters;
import com.sun.management.snmp.manager.SnmpParams;
import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpScopedPduBulk;
import com.sun.management.snmp.BerException;
import com.sun.management.snmp.BerEncoder;
import com.sun.management.snmp.BerDecoder;
import com.sun.management.snmp.SnmpV3Message;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpScopedPduRequest;
import com.sun.management.snmp.SnmpScopedPduPacket;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpDefinitions;

import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpPduFactory;
/**
 * Defines an implementation of the {@link com.sun.management.internal.snmp.SnmpMsgProcessingModel} interface. 
 * <P>This message processing model is responsible for dealing with SNMP V3 protocol marshalling and unmarshalling.
 * It uses a dedicated translator object that handle V3 parameters.
 *
 * @since Java DMK 5.1
 */
public class SnmpMsgProcessingModelV3 extends SnmpModelImpl
    implements SnmpMsgProcessingModel {
    //This size as been set arbitrarily. This is the max Security Paraemters size. It seems much than enough.
    private int bufferSize = 1024;
    private SnmpMsgTranslator translator = null;
    
    /**
     * In order to change the behavior of the translator, set it.
     * @param translator The translator that will be used.
     */
    public synchronized void setMsgTranslator(SnmpMsgTranslator translator) {
	this.translator = translator;
    }

    /**
     * Returns the current translator.
     * @return The current translator.
     */
    public synchronized SnmpMsgTranslator getMsgTranslator() {
	return translator;
    }
    /**
     * This method is called by <CODE>SnmpMsgProcessingModelV3</CODE> at 
     * construction time. The translator make the translation between SNMP 
     * protocol parameters.
     * @param sys The SNMP SubSystem.
     * @return The <CODE>SnmpMsgTranslatorV3</CODE> translator.
     */
    private SnmpMsgTranslator createMsgTranslator(SnmpSubSystem sys) {
	return new SnmpMsgTranslatorV3();
    }

    /**
     * Constructor. The translator make the translation between SNMP protocol
     * parameters. If the passed translator is null, 
     * <CODE>SnmpMsgTranslatorV3</CODE> is the default one used.
     * @param sys The Msg processing subSystem.
     * @param translat The SNMP protocol translator.
     */
    public SnmpMsgProcessingModelV3(SnmpMsgProcessingSubSystem sys,
				    SnmpMsgTranslator translat) {
	super(sys, "Snmp V3");

	if(translat == null)
	    translator = createMsgTranslator(sys);
	else
	    translator = translat;

	//Register for SNMP V3 protocol.
	sys.addModel(SnmpDefinitions.snmpVersionThree, this);
    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public SnmpIncomingRequest getIncomingRequest(SnmpPduFactory factory) {
	SnmpSecuritySubSystem secure = 
	    ((SnmpMsgProcessingSubSystem) getSubSystem()).getSecuritySubSystem();
	return new SnmpIncomingRequestImpl(secure,
					   factory,
					   new SnmpV3Message(),
					   new SnmpV3Message(),
					   getMsgTranslator());
    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public SnmpOutgoingRequest getOutgoingRequest(SnmpPduFactory factory) {
	SnmpSecuritySubSystem secure = 
	    ((SnmpMsgProcessingSubSystem) getSubSystem()).getSecuritySubSystem();
	return new SnmpOutgoingRequestImpl(secure,
					   factory,
					   new SnmpV3Message(),
					   getMsgTranslator());
    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public SnmpIncomingResponse getIncomingResponse(SnmpPduFactory factory) {
	SnmpSecuritySubSystem secure = 
	    ((SnmpMsgProcessingSubSystem) getSubSystem()).getSecuritySubSystem();
	return new SnmpIncomingResponseImpl(secure,
					    factory,
					    new SnmpV3Message(),
					    getMsgTranslator());
    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public SnmpPdu getRequestPdu(SnmpParams p, int command) 
	throws SnmpStatusException {
	SnmpScopedPduPacket reqpdu = null ;
	SnmpV3Parameters params = (SnmpV3Parameters) p;
	if (command == SnmpDefinitions.pduGetBulkRequestPdu) {
	    SnmpScopedPduBulk bulkPdu = new SnmpScopedPduBulk();
	    reqpdu = bulkPdu;	
	}
	else {
	    SnmpScopedPduRequest simplePdu = new SnmpScopedPduRequest();
	    reqpdu = simplePdu ;
	    reqpdu.type = command;
	}

	reqpdu.version = params.getProtocolVersion();
	
	reqpdu.contextEngineId = params.getContextEngineId();
	reqpdu.contextName = params.getContextName();
	reqpdu.msgFlags = (byte)params.getSecurityLevel();
	reqpdu.msgMaxSize = params.getMsgMaxSize();
	reqpdu.msgSecurityModel = params.getMsgSecurityModel();
	reqpdu.securityParameters = params.getSecurityParameters();
	
	return reqpdu;
    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    byte[] encodeParameters(SnmpSecurityParameters params) 
	throws SnmpTooBigException {
	
	byte[] res = null;
	try {
	    byte[] encoded = new byte[bufferSize];;
	    int len = 0;
	    len = params.encode(encoded);
	    res = new byte[len];
	    
	    for(int i = 0; i < len; i++)
		res[i] = encoded[i];
	}
	catch(IndexOutOfBoundsException e) {
	    throw new SnmpTooBigException(); 
	}
	return res;
    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public int encodePriv(int version,
			  int msgId,
			  int msgMaxSize,
			  byte msgFlags,
			  int msgSecurityModel,
			  SnmpSecurityParameters params,
			  byte[] encryptedPdu,
			  byte[] outputBytes) throws SnmpTooBigException {
	BerEncoder benc = null;
	int authLength = 0;
	benc = new BerEncoder(outputBytes);
	int encodingLength = 0;
	benc.openSequence();
	benc.putOctetString(encryptedPdu);
	
	byte[] securityParams = encodeParameters(params);
	
	benc.putOctetString(securityParams);
	
	benc.openSequence();
	benc.putInteger(msgSecurityModel);
	byte[] b = new byte[1];
	b[0] = msgFlags;
	benc.putOctetString(b);
	benc.putInteger(msgMaxSize);
	benc.putInteger(msgId);
	benc.closeSequence();
	benc.putInteger(version);
	benc.closeSequence();
	encodingLength = benc.trim();
	return encodingLength;

    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public int encode(int version,
		      int msgId,
		      int msgMaxSize,
		      byte msgFlags,
		      int msgSecurityModel,
		      SnmpSecurityParameters params,
		      byte[] contextEngineId,
		      byte[] contextName,
		      byte[] data,
		      int dataLength,
		      byte[] outputBytes) throws SnmpTooBigException {
	if(logger.finestOn()) {
	    logger.finest("encode", "Version: " + version +
		  ", msgId: " + msgId +
		  ", msgMaxSize: " + msgMaxSize +
		  ", msgFlags: " + msgFlags +
		  ", msgSecurityModel: " + msgSecurityModel +
		  ", params: " + params +
		  ", contextEngineId: " + (contextEngineId == null ? null : SnmpEngineId.createEngineId(contextEngineId) )+
		  ", contextName: " + (contextName == null ? null : new String(contextName)) +
		  ", data len: " + dataLength);
	}
	BerEncoder benc = null;
	benc = new BerEncoder(outputBytes);
	int encodingLength = 0;
	benc.openSequence();
	benc.openSequence();
	benc.putAny(data, dataLength);
	benc.putOctetString((contextName != null) ? 
			    contextName : new byte[0]) ;
	benc.putOctetString((contextEngineId != null) ? 
			    contextEngineId : new byte[0]) ;
	benc.closeSequence();
	
	byte[] securityParams = encodeParameters(params);
	
	benc.putOctetString(securityParams);
	
	benc.openSequence();
	benc.putInteger(msgSecurityModel);
	byte[] b = new byte[1];
	b[0] = msgFlags;
	benc.putOctetString(b);
	benc.putInteger(msgMaxSize);
	benc.putInteger(msgId);
	benc.closeSequence();
	benc.putInteger(version);
	benc.closeSequence();
	encodingLength = benc.trim();
	return encodingLength;
    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public SnmpDecryptedPdu decode(byte[] buff) throws SnmpStatusException {
	SnmpDecryptedPdu pdu = new SnmpDecryptedPdu();
	try {
            BerDecoder bdec = new BerDecoder(buff);
	    bdec.openSequence();
	    pdu.contextEngineId = bdec.fetchOctetString();
	    pdu.contextName = bdec.fetchOctetString();
	    pdu.data = bdec.fetchAny();
	    bdec.closeSequence();
        }
        catch(BerException x) {
            throw new SnmpStatusException("Invalid encoding : "+ x.toString()) ;
        }
	return pdu;
    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public int encode(SnmpDecryptedPdu pdu,
		      byte[] outputBytes) throws SnmpTooBigException {
	if(logger.finestOn()) {
	    logger.finest("encode", "pdu: " + pdu +
		  ", output bytes: " + outputBytes);
	}
	BerEncoder bencPdu = new BerEncoder(outputBytes);
	bencPdu.openSequence();
	bencPdu.putAny(pdu.data, pdu.dataLength);
	bencPdu.putOctetString((pdu.contextName != null) ? 
			       pdu.contextName : new byte[0]) ;
	bencPdu.putOctetString((pdu.contextEngineId != null) ? 
			       pdu.contextEngineId : new byte[0]) ;
	bencPdu.closeSequence();
	return bencPdu.trim();
    }

    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpMsgProcessingModelV3");
    
    String dbgTag = "SnmpMsgProcessingModelV3"; 

}
