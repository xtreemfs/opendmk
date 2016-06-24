/*
 * @(#)file      SnmpPduFactory.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.25
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




/**
 * Defines the interface of the object in charge of encoding and decoding SNMP packets.
 * <P>
 * You will not usually need to use this interface, except if you
 * decide to replace the default implementation <CODE>SnmpPduFactoryBER</CODE>.
 * <P>
 * An <CODE>SnmpPduFactory</CODE> object is attached to an 
 * {@link com.sun.management.comm.SnmpAdaptorServer SNMP protocol adaptor}
 * or an {@link com.sun.management.snmp.manager.SnmpPeer SnmpPeer}.
 * It is used each time an SNMP packet needs to be encoded or decoded.
 * <BR>{@link com.sun.management.snmp.SnmpPduFactoryBER SnmpPduFactoryBER} is the default 
 * implementation.
 * It simply applies the standard ASN.1 encoding and decoding
 * on the bytes of the SNMP packet.
 * <P>
 * It's possible to implement your own <CODE>SnmpPduFactory</CODE>
 * object and to add authentication and/or encryption to the
 * default encoding/decoding process.
 *
 * @see SnmpPduFactory
 * @see SnmpPduPacket
 * @see SnmpMessage
 *
 *
 * @since Java DMK 5.1
 */

public interface SnmpPduFactory {
     /**
     * Decodes the specified <CODE>SnmpMsg</CODE> and returns the 
     * resulting <CODE>SnmpPdu</CODE>. If this method returns
     * <CODE>null</CODE>, the message will be considered unsafe 
     * and will be dropped.
     *
     * @param msg The <CODE>SnmpMsg</CODE> to be decoded.
     * @return Null or a fully initialized <CODE>SnmpPdu</CODE>.
     * @exception SnmpStatusException If the encoding is invalid.
     *
     */
    public SnmpPdu decodeSnmpPdu(SnmpMsg msg) throws SnmpStatusException ;

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
	throws SnmpStatusException, SnmpTooBigException ;
}
