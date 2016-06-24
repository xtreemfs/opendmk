/*
 * @(#)file      SnmpSecuritySubSystem.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.22
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

import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpSecurityException;
import com.sun.management.snmp.SnmpSecurityParameters;

/**
 * Security sub system interface. To allow engine integration, a security sub system must implement this interface.
 *
 * @since Java DMK 5.1
 */
public interface SnmpSecuritySubSystem extends SnmpSubSystem {
     /**
     * Instantiates an <CODE>SnmpSecurityCache</CODE> that is dependent to the model implementation. This call is routed to the dedicated model according to the model ID.
     * @param id The model ID.
     * @return The model dependent security cache.
     */
    public SnmpSecurityCache createSecurityCache(int id) throws SnmpUnknownSecModelException;
    /**
     * To release the previously created cache. This call is routed to the dedicated model according to the model ID.
     * @param id The model ID.
     * @param cache The security cache to release.
     */ 
    public void releaseSecurityCache(int id, 
				     SnmpSecurityCache cache) throws SnmpUnknownSecModelException;

     /**
     * Called when a request is to be sent to the network. It must be secured. This call is routed to the dedicated model according to the model ID.
     * <BR>The specified parameters are defined in RFC 2572 (see also the {@link com.sun.management.snmp.SnmpV3Message} class).
     * @param cache The cache that has been created by calling <CODE>createSecurityCache</CODE> on this model.
     * @param version The SNMP protocol version.
     * @param msgID The current request id.
     * @param msgMaxSize The message max size.
     * @param msgFlags The message flags (reportable, authentication and privacy).
     * @param msgSecurityModel This current security model.
     * @param params The security parameters that contain the model dependent parameters.
     * @param contextEngineID The context engine ID.
     * @param contextName The context name.
     * @param data The marshalled varbind list
     * @param dataLength The marshalled varbind list length.
     * @param outputBytes The buffer to fill with secured request. This is a representation independent marshalled format. This buffer will be sent to the network.
     * @return The marshalled byte number.
     */
    public int generateRequestMsg(SnmpSecurityCache cache,
				  int version,
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
	throws SnmpTooBigException, SnmpStatusException, SnmpSecurityException, SnmpUnknownSecModelException;

    /**
     * Called when a response is to be sent to the network. It must be secured. This call is routed to the dedicated model according to the model ID.
     * <BR>The specified parameters are defined in RFC 2572 (see also the {@link com.sun.management.snmp.SnmpV3Message} class).
     * @param cache The cache that has been created by calling <CODE>createSecurityCache</CODE> on this model.
     * @param version The SNMP protocol version.
     * @param msgID The current request id.
     * @param msgMaxSize The message max size.
     * @param msgFlags The message flags (reportable, authentication and privacy).
     * @param msgSecurityModel This current security model.
     * @param params The security parameters that contain the model dependent parameters.
     * @param contextEngineID The context engine ID.
     * @param contextName The context name.
     * @param data The marshalled varbind list
     * @param dataLength The marshalled varbind list length.
     * @param outputBytes The buffer to fill with secured request. This is a representation independent marshalled format. This buffer will be sent to the network.
     * @return The marshalled byte number.
     */
    public int generateResponseMsg(SnmpSecurityCache cache,
				   int version,
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
	throws SnmpTooBigException, SnmpStatusException, 
	       SnmpSecurityException, SnmpUnknownSecModelException;
      /**
     * Called when a request is received from the network. It handles authentication and privacy. This call is routed to the dedicated model according to the model ID.
     * <BR>The specified parameters are defined in RFC 2572 (see also the {@link com.sun.management.snmp.SnmpV3Message} class).
     * @param cache The cache that has been created by calling <CODE>createSecurityCache</CODE> on this model.
     * @param version The SNMP protocol version.
     * @param msgID The current request id.
     * @param msgMaxSize The message max size.
     * @param msgFlags The message flags (reportable, authentication and privacy)
     * @param msgSecurityModel This current security model.
     * @param params The security parameters in a marshalled format. The informations contained in this array are model dependent.
     * @param contextEngineID The context engine ID or null if encrypted.
     * @param contextName The context name or null if encrypted.
     * @param data The marshalled varbind list or null if encrypted.
     * @param encryptedPdu The encrypted pdu or null if not encrypted.
     * @param decryptedPdu The decrypted pdu. If no decryption is to be done, the passed context engine ID, context name and data could be used to fill this object.
     * @return The decoded security parameters.
     
     */
    public SnmpSecurityParameters 
	processIncomingRequest(SnmpSecurityCache cache,
			       int version,
			       int msgID,
			       int msgMaxSize,
			       byte msgFlags,
			       int msgSecurityModel,
			       byte[] params,
			       byte[] contextEngineID,
			       byte[] contextName,
			       byte[] data,
			       byte[] encryptedPdu,
			       SnmpDecryptedPdu decryptedPdu)
	throws SnmpStatusException, SnmpSecurityException, SnmpUnknownSecModelException;
          /**
     * Called when a response is received from the network. It handles authentication and privacy. This call is routed to the dedicated model according to the model ID.
     * <BR>The specified parameters are defined in RFC 2572 (see also the {@link com.sun.management.snmp.SnmpV3Message} class).
     * @param cache The cache that has been created by calling <CODE>createSecurityCache</CODE> on this model.
     * @param version The SNMP protocol version.
     * @param msgID The current request id.
     * @param msgMaxSize The message max size.
     * @param msgFlags The message flags (reportable, authentication and privacy).
     * @param msgSecurityModel This current security model.
     * @param params The security parameters in a marshalled format. The informations contained in this array are model dependent.
     * @param contextEngineID The context engine ID or null if encrypted.
     * @param contextName The context name or null if encrypted.
     * @param data The marshalled varbind list or null if encrypted.
     * @param encryptedPdu The encrypted pdu or null if not encrypted.
     * @param decryptedPdu The decrypted pdu. If no decryption is to be done, the passed context engine ID, context name and data could be used to fill this object.
     * @return The security parameters.
     
     */
    public SnmpSecurityParameters processIncomingResponse(SnmpSecurityCache cache,
							  int version,
							  int msgID,
							  int msgMaxSize,
							  byte msgFlags,
							  int msgSecurityModel,
							  byte[] params,
							  byte[] contextEngineID,
							  byte[] contextName,
							  byte[] data,
							  byte[] encryptedPdu,
							  SnmpDecryptedPdu decryptedPdu)
	throws SnmpStatusException, SnmpSecurityException, SnmpUnknownSecModelException;
}
