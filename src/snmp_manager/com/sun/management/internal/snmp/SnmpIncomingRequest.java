/*
 * @(#)file      SnmpIncomingRequest.java
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

import java.net.InetAddress;

import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpMsg;

import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpBadSecurityLevelException;

/**
<P> An <CODE>SnmpIncomingRequest</CODE> handles both sides of an incoming SNMP request:
<ul>
<li> The request. Unmarshalling of the received message. </li>
<li> The response. Marshalling of the message to send. </li>
</ul>
 *
 * @since Java DMK 5.1
 */
public interface SnmpIncomingRequest {
    /**
     * Once the incoming request decoded, returns the decoded security parameters.
     * @return The decoded security parameters.
     */
    public SnmpSecurityParameters getSecurityParameters();
     /**
     * Tests if a report is expected.
     * @return boolean indicating if a report is to be sent.
     */
    public boolean isReport();
    /**
     * Tests if a response is expected.
     * @return boolean indicating if a response is to be sent.
     */
    public boolean isResponse();
    
    /**
     * Tells this request that no response will be sent.
     */
    public void noResponse();
    /**
     * Gets the incoming request principal.
     * @return The request principal.
     **/
    public String getPrincipal();
    /**
     * Gets the incoming request security level. This level is defined in 
     * {@link com.sun.management.snmp.SnmpEngine SnmpEngine}.
     * @return The security level.
     */
    public int getSecurityLevel();
    /**
     * Gets the incoming request security model.
     * @return The security model.
     */
    public int getSecurityModel();
    /**
     * Gets the incoming request context name.
     * @return The context name.
     */
    public byte[] getContextName();
    /**
     * Gets the incoming request context engine Id.
     * @return The context engine Id.
     */
    public byte[] getContextEngineId(); 
    /**
     * Gets the incoming request context name used by Access Control Model in order to allow or deny the access to OIDs.
     */
    public byte[] getAccessContext();
    /**
     * Encodes the response message to send and puts the result in the specified byte array.
     * 
     * @param outputBytes An array to receive the resulting encoding.
     *
     * @exception ArrayIndexOutOfBoundsException If the result does not fit
     *                                           into the specified array.
     */
    public int encodeMessage(byte[] outputBytes)
	throws SnmpTooBigException;
    
    /**
     * Decodes the specified bytes and initializes the request with the incoming message.
     * 
     * @param inputBytes The bytes to be decoded.
     *
     * @exception SnmpStatusException If the specified bytes are not a valid encoding or if the security applied to this request failed and no report is to be sent (typically trap PDU).
     */
    public void decodeMessage(byte[] inputBytes, 
			      int byteCount, 
			      InetAddress address,
			      int port) 
        throws SnmpStatusException, SnmpUnknownSecModelException, 
	       SnmpBadSecurityLevelException;

     /**
     * Initializes the response to send with the passed Pdu.
     * <P>
     * If the encoding length exceeds <CODE>maxDataLength</CODE>, 
     * the method throws an exception.
     * 
     * @param pdu The PDU to be encoded.
     * @param maxDataLength The maximum length permitted for the data field.
     *
     * @exception SnmpStatusException If the specified <CODE>pdu</CODE> is 
     *     not valid.
     * @exception SnmpTooBigException If the resulting encoding does not fit
     *     into <CODE>maxDataLength</CODE> bytes.
     * @exception ArrayIndexOutOfBoundsException If the encoding exceeds 
     *     <CODE>maxDataLength</CODE>.
     */
    public SnmpMsg encodeSnmpPdu(SnmpPdu pdu, 
				 int maxDataLength) 
        throws SnmpStatusException, SnmpTooBigException;

    /**
     * Gets the request PDU encoded in the received message.
     * <P>
     * This method decodes the data field and returns the resulting PDU.
     * 
     * @return The resulting PDU.
     * @exception SnmpStatusException If the encoding is not valid.
     */
    public SnmpPdu decodeSnmpPdu() 
	throws SnmpStatusException;
    
    /**
     * Returns a string form of the received message.
     * @return The message state string.
     */
    public String printRequestMessage();
    /**
     * Returns a string form of the message to send.
     * @return The message state string.
     */
    public String printResponseMessage();
}
