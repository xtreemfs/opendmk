/*
 * @(#)file      SnmpMsgProcessingSubSystem.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.20
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
package com.sun.management.internal.snmp;

import java.util.Vector;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.manager.SnmpParams;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpSecurityParameters;

import com.sun.management.snmp.SnmpUnknownMsgProcModelException;

/**
 * Message processing sub system interface. To allow engine integration, a message processing sub system must implement this interface. This sub system is called by the dispatcher when receiving or sending calls.
 *
 * @since Java DMK 5.1
 */
public interface SnmpMsgProcessingSubSystem extends SnmpSubSystem {

    /**
     * Attaches the security sub system to this sub system. Message processing model are making usage of various security sub systems. This direct attachment avoid the need of accessing the engine to retrieve the Security sub system.
     * @param security The security sub system.
     */
    public void setSecuritySubSystem(SnmpSecuritySubSystem security);
    /** Gets the attached security sub system.
     * @return The security sub system.
     */
    public SnmpSecuritySubSystem getSecuritySubSystem();
    
    /**
     * This method is called when a call is received from the network.
     * @param model The model ID.
     * @param factory The pdu factory to use to encode and decode pdu.
     * @return The object that will handle every steps of the receiving (mainly unmarshalling and security).
     */
    public SnmpIncomingRequest getIncomingRequest(int model,
						  SnmpPduFactory factory) 
	throws SnmpUnknownMsgProcModelException;	
    /**
     * This method is called when a call is to be sent to the network. The sub system routes the call to the dedicated model according to the model ID.
     * @param model The model ID.
     * @param factory The pdu factory to use to encode and decode pdu.
     * @return The object that will handle every steps of the sending (mainly marshalling and security).
     */
    public SnmpOutgoingRequest getOutgoingRequest(int model,
						  SnmpPduFactory factory) throws SnmpUnknownMsgProcModelException ;
    /**
     * This method is called to instantiate a pdu according to the passed pdu type and parameters. The sub system routes the call to the dedicated model according to the model ID.
     * @param model The model ID.
     * @param p The request parameters.
     * @param type The pdu type.
     * @return The pdu.
     */
    public SnmpPdu getRequestPdu(int model, SnmpParams p, int type) throws SnmpUnknownMsgProcModelException, SnmpStatusException ;
     /**
     * This method is called when a call is received from the network. The sub system routes the call to the dedicated model according to the model ID.
     * @param model The model ID.
     * @param factory The pdu factory to use to decode pdu.
     * @return The object that will handle every steps of the receiving (mainly marshalling and security).
     */
    public SnmpIncomingResponse getIncomingResponse(int model,
						    SnmpPduFactory factory) throws SnmpUnknownMsgProcModelException;
    /**
     * This method is called to encode a full scoped pdu that as not been encrypted. <CODE>contextName</CODE>, <CODE>contextEngineID</CODE> and data are known. It will be routed to the dedicated model according to the version value.
     * <BR>The specified parameters are defined in RFC 2572 (see also the {@link com.sun.management.snmp.SnmpV3Message} class).
     * @param version The SNMP protocol version.
     * @param msgID The SNMP message ID.
     * @param msgMaxSize The max message size.
     * @param msgFlags The message flags.
     * @param msgSecurityModel The message security model.
     * @param params The security parameters.
     * @param contextEngineID The context engine ID.
     * @param contextName The context name.
     * @param data The encoded data.
     * @param dataLength The encoded data length.
     * @param outputBytes The buffer containing the encoded message.
     * @return The encoded bytes number.
     */
    public int encode(int version,
		      int msgID,
		      int msgMaxSize,
		      byte msgFlags,
		      int msgSecurityModel,
		      SnmpSecurityParameters params,
		      byte[] contextEngineID,
		      byte[] contextName,
		      byte[] data,
		      int dataLength,
		      byte[] outputBytes) 
	throws SnmpTooBigException, 
	       SnmpUnknownMsgProcModelException ;
    /**
     * This method is called to encode a full scoped pdu that as been encrypted. <CODE>contextName</CODE>, <CODE>contextEngineID</CODE> and data are not known. It will be routed to the dedicated model according to the version value. 
     * <BR>The specified parameters are defined in RFC 2572 (see also the {@link com.sun.management.snmp.SnmpV3Message} class).
     * @param version The SNMP protocol version.
     * @param msgID The SNMP message ID.
     * @param msgMaxSize The max message size.
     * @param msgFlags The message flags.
     * @param msgSecurityModel The message security model.
     * @param params The security parameters.
     * @param encryptedPdu The encrypted pdu.
     * @param outputBytes The buffer containing the encoded message.
     * @return The encoded bytes number.
     */
    public int encodePriv(int version,
			  int msgID,
			  int msgMaxSize,
			  byte msgFlags,
			  int msgSecurityModel,
			  SnmpSecurityParameters params,
			  byte[] encryptedPdu,
			  byte[] outputBytes) throws SnmpTooBigException, SnmpUnknownMsgProcModelException;

     /**
     * This method returns a decoded scoped pdu. This method decodes only the <CODE>contextEngineID</CODE>, <CODE>contextName</CODE> and data. It is needed by the <CODE>SnmpSecurityModel</CODE> after decryption. It will be routed to the dedicated model according to the version value. 
     * @param version The SNMP protocol version.
     * @param pdu The encoded pdu.
     * @return the partially scoped pdu.
     */
    public SnmpDecryptedPdu decode(int version,
				   byte[] pdu) 
	throws SnmpStatusException, SnmpUnknownMsgProcModelException;

      /**
     * This method returns an encoded scoped pdu. This method encodes only the <CODE>contextEngineID</CODE>, <CODE>contextName</CODE> and data. It is needed by the <CODE>SnmpSecurityModel</CODE> for decryption. It will be routed to the dedicated model according to the version value. 
     * @param version The SNMP protocol version.
     * @param pdu The pdu to encode.
     * @param outputBytes The partially scoped pdu.
     * @return The encoded bytes number.
     */
    public int encode(int version,
		      SnmpDecryptedPdu pdu,
		      byte[] outputBytes) 
	throws SnmpTooBigException, SnmpUnknownMsgProcModelException;
}


