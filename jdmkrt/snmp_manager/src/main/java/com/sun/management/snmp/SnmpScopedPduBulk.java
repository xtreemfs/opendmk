/*
 * @(#)file      SnmpScopedPduBulk.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.16
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
 * <P>
 * The <CODE>SnmpSocpedPduBulk</CODE> extends {@link com.sun.management.snmp.SnmpScopedPduPacket SnmpScopedPduPacket}
 * and defines attributes specific to the <CODE>get-bulk</CODE> PDU (see RFC 1448).
 *
 *
 * @since Java DMK 5.1
 */

public class SnmpScopedPduBulk extends SnmpScopedPduPacket 
    implements SnmpPduBulkType {
    private static final long serialVersionUID = -2432571604056696144L;

    /**
     * The <CODE>non-repeaters</CODE> value.
     * @serial
     */
    int            nonRepeaters;
  

    /**
     * The <CODE>max-repetitions</CODE> value.
     * @serial
     */
    int            maxRepetitions;

    public SnmpScopedPduBulk() {
  	type = pduGetBulkRequestPdu;
	version = snmpVersionThree;
    }

    /**
     * The <CODE>max-repetitions</CODE> setter.
     * @param max Maximum repetition.
     */
    public void setMaxRepetitions(int max) { 
	maxRepetitions = max;
    }

    /**
     * The <CODE>non-repeaters</CODE> setter.
     * @param nr Non repeaters.
     */
    public void setNonRepeaters(int nr) {
	nonRepeaters = nr;
    }

    /**
     * The <CODE>max-repetitions</CODE> getter.
     * @return Maximum repetition.
     */
    public int getMaxRepetitions() { return maxRepetitions; }

    /**
     * The <CODE>non-repeaters</CODE> getter.
     * @return Non repeaters.
     */
    public int getNonRepeaters() { return nonRepeaters; }
    
    /**
     * Generates the pdu to use for response.
     * @return Response pdu.
     */
    public SnmpPdu getResponsePdu() {
 	SnmpScopedPduRequest result = new SnmpScopedPduRequest();
	result.address = address ;
	result.port = port ;
	result.version = version ;
 	result.requestId = requestId;
	result.msgId = msgId;
 	result.msgMaxSize = msgMaxSize;
 	result.msgFlags = msgFlags;
 	result.msgSecurityModel = msgSecurityModel;
 	result.contextEngineId = contextEngineId;
 	result.contextName = contextName;
 	result.securityParameters = securityParameters;
	result.type = pduGetResponsePdu ;
	result.errorStatus = SnmpDefinitions.snmpRspNoError ;
	result.errorIndex = 0 ;
 	return result;
    }
}
