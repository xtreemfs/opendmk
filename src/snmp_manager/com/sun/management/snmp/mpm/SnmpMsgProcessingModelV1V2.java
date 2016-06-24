/*
 * @(#)file      SnmpMsgProcessingModelV1V2.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.28
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
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.internal.snmp.SnmpSubSystem;
import com.sun.management.internal.snmp.SnmpModelImpl;
import com.sun.management.internal.snmp.SnmpSecuritySubSystem;
import com.sun.management.internal.snmp.SnmpIncomingRequest;
import com.sun.management.internal.snmp.SnmpOutgoingRequest;
import com.sun.management.internal.snmp.SnmpIncomingResponse;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;

import com.sun.management.snmp.manager.SnmpParameters;
import com.sun.management.snmp.manager.SnmpParams;
import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpMessage;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpPduRequest;
import com.sun.management.snmp.SnmpPduBulk;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpPduRequest;

import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpPduFactory;
/**
 * Defines an implementation of the {@link com.sun.management.internal.snmp.SnmpMsgProcessingModel} interface. 
 * <P>This message processing model allows previous supported SNMP protocols to be integrated in the SNMP V3 engine / model framework.
 * It uses a dedicated translator object that infers some V3 parameters and make use of the data types that were used in Jdmk 4.2 (<CODE>SnmpPduPacket</CODE>, <CODE>SnmpMessage</CODE>...).
 *
 * @since Java DMK 5.1
 */
public class SnmpMsgProcessingModelV1V2  extends SnmpModelImpl 
    implements SnmpMsgProcessingModel {
    /**
     *The translator dedicated to V1 and V2 protocol.
     */
    SnmpMsgTranslator translator = null;


    /**
     * In order to change the behavior of the translator, set it.
     */
    public synchronized void setMsgTranslator(SnmpMsgTranslator translator) {
	this.translator = translator;
    }

    public synchronized SnmpMsgTranslator getMsgTranslator() {
	return translator;
    }
    
    /**
     * This method is called by <CODE>SnmpMsgProcessingModelV1V2</CODE> at 
     * construction time. The translator make the translation between 
     * SNMP protocol parameters.
     * @param sys The SNMP SubSystem.
     * @return The <CODE>SnmpMsgTranslatorV1V2</CODE> translator.
     */
    private SnmpMsgTranslator createMsgTranslator(SnmpSubSystem sys) {
	return new SnmpMsgTranslatorV1V2(sys.getEngine());
    }
    
    /**
     * Constructor. The translator make the translation between SNMP protocol
     * parameters. If the passed translator is null, 
     * <CODE>SnmpMsgTranslatorV1V2</CODE> is the default one used.
     * @param sys The Msg processing subSystem.
     * @param translat The SNMP protocol translator.
     */
    public SnmpMsgProcessingModelV1V2(SnmpMsgProcessingSubSystem sys,
				      SnmpMsgTranslator translat) {
	super(sys, "Snmp V1/V2");
	if(translat == null)
	    translator = createMsgTranslator(sys);
	else
	    translator = translat;
	// Register for SNMP V1 and V2
	sys.addModel(SnmpDefinitions.snmpVersionOne, this);
	sys.addModel(SnmpDefinitions.snmpVersionTwo, this);
    }
    
     private SnmpMsg getMessage() {
	return new SnmpMessage();
     }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public SnmpIncomingRequest getIncomingRequest(SnmpPduFactory factory) {
	SnmpSecuritySubSystem secure = 
	    ((SnmpMsgProcessingSubSystem) getSubSystem()).getSecuritySubSystem();

	return new SnmpIncomingRequestImpl(secure,
					   factory,
					   getMessage(),
					   getMessage(),
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
					   getMessage(),
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
					    getMessage(),
					    getMsgTranslator());
    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public SnmpPdu getRequestPdu(SnmpParams p, int command) 
	throws SnmpStatusException {
	SnmpPduPacket reqpdu = null ;
	SnmpParameters params = (SnmpParameters) p;
	 if (command == SnmpDefinitions.pduGetBulkRequestPdu) {
	     SnmpPduBulk bulkPdu = new SnmpPduBulk();
	     reqpdu = bulkPdu;
	 }
	 else {
	     SnmpPduRequest simplePdu = new SnmpPduRequest() ;
	     reqpdu = simplePdu ;
	     reqpdu.type = command;
	 }
	 // We allow an SNMP v1 manager to send inform request
	 // but the protocol version PDU must be version 2.
	 //
	 if (command == SnmpDefinitions.pduInformRequestPdu) {
	     reqpdu.version = SnmpDefinitions.snmpVersionTwo ;
	 } 
	 else {
	     reqpdu.version = params.getProtocolVersion() ;
	 }
	 
	 reqpdu.community = params.encodeAuthentication(command);

	 return reqpdu;
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
	 //No privacy in V1V2.
	 return 0;
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
		  ", params: " + params);
	}
	SnmpMessage msg = new SnmpMessage();
	msg.community = contextName;
	msg.version = version;
	msg.data = data;
	msg.dataLength = dataLength;
	return msg.encodeMessage(outputBytes);
    }
    /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification.
     */
    public SnmpDecryptedPdu decode(byte[] pdu) throws SnmpStatusException {
	//No decryption and encryption in V1V2.
	return null;
    }
     /**
     * Look for <CODE> SnmpMsgProcessingModel </CODE> interface specification
     */
    public int encode(SnmpDecryptedPdu pdu,
		      byte[] outputBytes) throws SnmpTooBigException {
	//No decryption and encryption in V1V2.
	return 0;
    } 

    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpMsgProcessingModelV1V2");

    String dbgTag = "SnmpMsgProcessingModelV1V2"; 
}
