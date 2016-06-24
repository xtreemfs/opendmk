/*
 * @(#)file      SnmpPduFactoryBER.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   3.31
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


package com.sun.management.snmp;


// java imports
//
import java.io.Serializable;

// jmx import
//
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpMessage;
import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpDefinitions;

// jdmk import
//
import com.sun.management.snmp.SnmpV3Message;

/**
 * Default implementation of the {@link com.sun.management.snmp.SnmpPduFactory SnmpPduFactory} interface.
 * <BR>It uses the BER (basic encoding rules) standardized encoding scheme associated with ASN.1.
 * <P>
 * This implementation of the <CODE>SnmpPduFactory</CODE> is very
 * basic: it simply calls encoding and decoding methods from 
 * {@link com.sun.management.snmp.SnmpMsg}.
 * <BLOCKQUOTE>
 * <PRE>
 * public SnmpPdu decodeSnmpPdu(SnmpMsg msg) 
 * throws SnmpStatusException {
 *   return msg.decodeSnmpPdu() ;
 * }
 * 
 * public SnmpMsg encodeSnmpPdu(SnmpPdu pdu, int maxPktSize)
 * throws SnmpStatusException, SnmpTooBigException {
 *   SnmpMsg result = new SnmpMessage() ;       // for SNMP v1/v2
 * <I>or</I>
 *   SnmpMsg result = new SnmpV3Message() ;     // for SNMP v3
 *   result.encodeSnmpPdu(pdu, maxPktSize) ;
 *   return result ;
 * }
 * </PRE>
 * </BLOCKQUOTE>
 * To implement your own object, you can implement <CODE>SnmpPduFactory</CODE>
 * or extend <CODE>SnmpPduFactoryBER</CODE>.
 *
 * @since Java DMK 5.1
 */

public class SnmpPduFactoryBER implements SnmpPduFactory, Serializable {
    private static final long serialVersionUID = -6397398073339620L;

   /**
     * Calls {@link com.sun.management.snmp.SnmpMsg#decodeSnmpPdu SnmpMsg.decodeSnmpPdu}
     * on the specified message and returns the resulting <CODE>SnmpPdu</CODE>.
     *
     * @param msg The SNMP message to be decoded.
     * @return The resulting SNMP PDU packet.
     * @exception SnmpStatusException If the encoding is invalid.
     *
     */
    public SnmpPdu decodeSnmpPdu(SnmpMsg msg) throws SnmpStatusException {
	return msg.decodeSnmpPdu();
    }

    /**
     * Encodes the specified <CODE>SnmpPdu</CODE> and
     * returns the resulting <CODE>SnmpMsg</CODE>. If this
     * method returns null, the specified <CODE>SnmpPdu</CODE> 
     * will be dropped and the current SNMP request will be
     * aborted.
     *
     * @param pdu The <CODE>SnmpPdu</CODE> to be encoded.
     * @param maxPktSize The size limit of the resulting encoding.
     * @return Null or a fully encoded <CODE>SnmpMsg</CODE>.
     * @exception SnmpStatusException If <CODE>pdu</CODE> contains
     *            illegal values and cannot be encoded.
     * @exception SnmpTooBigException If the resulting encoding does not
     *            fit into <CODE>maxPktSize</CODE> bytes.
     *
     */
    public SnmpMsg encodeSnmpPdu(SnmpPdu pdu, int maxPktSize) 
	throws SnmpStatusException, SnmpTooBigException {
	switch(pdu.version) {
	case SnmpDefinitions.snmpVersionOne:
	case SnmpDefinitions.snmpVersionTwo: {
	    SnmpMessage result = new SnmpMessage();
	    result.encodeSnmpPdu((SnmpPduPacket) pdu, maxPktSize);
	    return result;
	}
	case SnmpDefinitions.snmpVersionThree: {
	    SnmpV3Message result = new SnmpV3Message();
	    result.encodeSnmpPdu(pdu, maxPktSize);
	    return result;
	}
	default:
	    return null;
	}
    }
}

