/*
 * @(#)file      SnmpPduBulk.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.19
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
 * Represents a <CODE>get-bulk</CODE> PDU as defined in RFC 1448.
 * <P>
 * You will not usually need to use this class, except if you
 * decide to implement your own 
 * {@link com.sun.management.snmp.SnmpPduFactory SnmpPduFactory} object.
 * <P>
 * The <CODE>SnmpPduBulk</CODE> extends {@link com.sun.management.snmp.SnmpPduPacket SnmpPduPacket}
 * and defines attributes specific to the <CODE>get-bulk</CODE> PDU (see RFC 1448).
 *
 *
 * @since Java DMK 5.1
 */

public class SnmpPduBulk extends SnmpPduPacket 
    implements SnmpPduBulkType {
    private static final long serialVersionUID = 1353423561029187652L;

    /**
     * The <CODE>non-repeaters</CODE> value.
     * @serial
     */
    public int            nonRepeaters ;
  

    /**
     * The <CODE>max-repetitions</CODE> value.
     * @serial
     */
    public int            maxRepetitions ;


    /**
     * Builds a new <CODE>get-bulk</CODE> PDU.
     * <BR><CODE>type</CODE> and <CODE>version</CODE> fields are initialized with
     * {@link com.sun.management.snmp.SnmpDefinitions#pduGetBulkRequestPdu pduGetBulkRequestPdu}
     * and {@link com.sun.management.snmp.SnmpDefinitions#snmpVersionTwo snmpVersionTwo}.
     */
    public SnmpPduBulk() {
  	type = pduGetBulkRequestPdu ;
	version = snmpVersionTwo ;
    }
    /**
     * Implements the <CODE>SnmpPduBulkType</CODE> interface.
     *
     */
    public void setMaxRepetitions(int i) { 
	maxRepetitions = i;
    }
    /**
     * Implements the <CODE>SnmpPduBulkType</CODE> interface.
     *
     */
    public void setNonRepeaters(int i) {
	nonRepeaters = i;
    }
    /**
     * Implements the <CODE>SnmpPduBulkType</CODE> interface.
     *
     */
    public int getMaxRepetitions() { return maxRepetitions; }
    /**
     * Implements the <CODE>SnmpPduBulkType</CODE> interface.
     *
     */
    public int getNonRepeaters() { return nonRepeaters; }
    /**
     * Implements the <CODE>SnmpAckPdu</CODE> interface.
     *
     */
    public SnmpPdu getResponsePdu() {
	SnmpPduRequest result = new SnmpPduRequest();
	result.address = address;
	result.port = port;
	result.version = version;
	result.community = community;
	result.type = SnmpDefinitions.pduGetResponsePdu;
	result.requestId = requestId;
	result.errorStatus = SnmpDefinitions.snmpRspNoError;
	result.errorIndex = 0;
	
 	return result;
    }
}




