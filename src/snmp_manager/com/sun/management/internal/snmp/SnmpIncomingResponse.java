/*
 * @(#)file      SnmpIncomingResponse.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.18
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
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpSecurityException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpMsg;

import com.sun.management.internal.snmp.SnmpSecurityCache;
import com.sun.management.snmp.SnmpBadSecurityLevelException;
/**
 * <P> An <CODE>SnmpIncomingResponse</CODE> handles the unmarshalling of the received response.</P>
 *
 * @since Java DMK 5.1
 */

public interface SnmpIncomingResponse {
    /**
     * Returns the source address.
     * @return The source address.
     */
    public InetAddress getAddress();

    /**
     * Returns the source port.
     * @return The source port.
     */
    public int getPort();

    /**
     * Gets the incoming response security parameters.
     * @return The security parameters.
     **/
    public SnmpSecurityParameters getSecurityParameters();
    /**
     * Call this method in order to reuse <CODE>SnmpOutgoingRequest</CODE> cache.
     * @param cache The security cache.
     */
    public void setSecurityCache(SnmpSecurityCache cache);
    /**
     * Gets the incoming response security level. This level is defined in 
     * {@link com.sun.management.snmp.SnmpEngine SnmpEngine}.
     * @return The security level.
     */
    public int getSecurityLevel();
    /**
     * Gets the incoming response security model.
     * @return The security model.
     */
    public int getSecurityModel();
    /**
     * Gets the incoming response context name.
     * @return The context name.
     */
    public byte[] getContextName();
    
    /**
     * Decodes the specified bytes and initializes itself with the received 
     * response.
     * 
     * @param inputBytes The bytes to be decoded.
     *
     * @exception SnmpStatusException If the specified bytes are not a valid encoding.
     */
    public SnmpMsg decodeMessage(byte[] inputBytes, 
				 int byteCount, 
				 InetAddress address,
				 int port) 
        throws SnmpStatusException, SnmpSecurityException;
   
    /**
     * Gets the request PDU encoded in the received response.
     * <P>
     * This method decodes the data field and returns the resulting PDU.
     * 
     * @return The resulting PDU.
     * @exception SnmpStatusException If the encoding is not valid.
     */
    public SnmpPdu decodeSnmpPdu() 
	throws SnmpStatusException;

    /**
     * Returns the response request Id.
     * @param data The flat message.
     * @return The request Id.
     */
    public int getRequestId(byte[] data) throws SnmpStatusException;

    /**
     * Returns a string form of the message to send.
     * @return The message state string.
     */
    public String printMessage();
}
