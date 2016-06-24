/*
 * @(#)file      SnmpPdu.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.11
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


import java.io.Serializable;
import java.net.InetAddress;
/**
 * Is the fully decoded representation of an SNMP packet.
 * <P>
 * Classes are derived from <CODE>SnmpPdu</CODE> to
 * represent the different forms of SNMP packets
 * ({@link com.sun.management.snmp.SnmpPduPacket SnmpPduPacket},
 * {@link com.sun.management.snmp.SnmpScopedPduPacket SnmpScopedPduPacket})
 * <BR>The <CODE>SnmpPdu</CODE> class defines the attributes 
 * common to every form of SNMP packets.
 * 
 * 
 * @see SnmpMessage
 * @see SnmpPduFactory
 *
 *
 * @since Java DMK 5.1
 */
public abstract class SnmpPdu implements SnmpDefinitions, Serializable {
 
    /**
     * PDU type. Types are defined in 
     * {@link com.sun.management.snmp.SnmpDefinitions SnmpDefinitions}.
     * @serial
     */
    public int type=0 ;
  
    /**
     * Protocol version. Versions are defined in 
     * {@link com.sun.management.snmp.SnmpDefinitions SnmpDefinitions}.
     * @serial
     */
    public int version=0 ;
  
    /**
     * List of variables.
     * @serial
     */
    public SnmpVarBind[] varBindList ;


    /**
     * Request identifier.
     * Note that this field is not used by <CODE>SnmpPduTrap</CODE>.
     * @serial
     */
    public int requestId=0 ;

    /**
     * Source or destination address.
     * <P>For an incoming PDU it's the source.
     * <BR>For an outgoing PDU it's the destination.
     * @serial
     */
    public InetAddress address ;
  
    /**
     * Source or destination port.
     * <P>For an incoming PDU it's the source.
     * <BR>For an outgoing PDU it's the destination.
     * @serial
     */
    public int port=0 ;
    
    /**
     * Returns the <CODE>String</CODE> representation of a PDU type. 
     * For instance, if the PDU type is <CODE>SnmpDefinitions.pduGetRequestPdu</CODE>, 
     * the method will return "SnmpGet".
     * @param cmd The integer representation of the PDU type.
     * @return The <CODE>String</CODE> representation of the PDU type.
     */
    public static String pduTypeToString(int cmd) {
	switch (cmd) {
	case pduGetRequestPdu :
	    return "SnmpGet" ;
	case pduGetNextRequestPdu :
	    return "SnmpGetNext" ;
	case pduWalkRequest :
	    return "SnmpWalk(*)" ;
	case pduSetRequestPdu :
	    return "SnmpSet" ;
	case pduGetResponsePdu :
	    return "SnmpResponse" ;
	case pduV1TrapPdu :
	    return "SnmpV1Trap" ;
	case pduV2TrapPdu :
	    return "SnmpV2Trap" ;
	case pduGetBulkRequestPdu :
	    return "SnmpGetBulk" ;
	case pduInformRequestPdu :
	    return "SnmpInform" ;
	}
	return "Unknown Command = " + cmd ;
    }  
}
