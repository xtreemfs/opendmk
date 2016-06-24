/*
 * @(#)file      SnmpIpAddress.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.13
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
 * Represents an SNMP IpAddress.
 *
 *
 * @since Java DMK 5.1
 */

public class SnmpIpAddress extends SnmpOid {
    private static final long serialVersionUID = -844289641772067624L;

    // CONSTRUCTORS
    //-------------
    /**
     * Constructs a new <CODE>SnmpIpAddress</CODE> from the specified bytes array.
     * @param bytes The four bytes composing the address.
     * @exception IllegalArgumentException The length of the array is not equal to four.
     */
    public SnmpIpAddress(byte[] bytes) throws IllegalArgumentException {
	buildFromByteArray(bytes);
    }

    /**
     * Constructs a new <CODE>SnmpIpAddress</CODE> from the specified long value.
     * @param addr The initialization value.
     */
    public SnmpIpAddress(long addr) {
	int address = (int)addr ;
	byte[] ipaddr = new byte[4];

	ipaddr[0] = (byte) ((address >>> 24) & 0xFF);
	ipaddr[1] = (byte) ((address >>> 16) & 0xFF);
	ipaddr[2] = (byte) ((address >>> 8) & 0xFF);
	ipaddr[3] = (byte) (address & 0xFF);
    
	buildFromByteArray(ipaddr);
    }

    /**
     * Constructs a new <CODE>SnmpIpAddress</CODE> from a dot-formatted <CODE>String</CODE>.
     * The dot-formatted <CODE>String</CODE> is formulated x.x.x.x .
     * @param dotAddress The initialization value.
     * @exception IllegalArgumentException The string does not correspond to an ip address.
     */
    public SnmpIpAddress(String dotAddress) throws IllegalArgumentException {
	super(dotAddress) ;
	if ((componentCount > 4) ||
	    (components[0] > 255) ||
	    (components[1] > 255) ||
	    (components[2] > 255) ||
	    (components[3] > 255)) {
	    throw new IllegalArgumentException(dotAddress) ;
	}
    }

    /**
     * Constructs a new <CODE>SnmpIpAddress</CODE> from four long values.
     * @param b1 Byte 1.
     * @param b2 Byte 2.
     * @param b3 Byte 3.
     * @param b4 Byte 4.
     * @exception IllegalArgumentException A value is outside of [0-255].
     */
    public SnmpIpAddress(long b1, long b2, long b3, long b4) {
	super(b1, b2, b3, b4) ;
	if ((components[0] > 255) ||
	    (components[1] > 255) ||
	    (components[2] > 255) ||
	    (components[3] > 255)) {
	    throw new IllegalArgumentException() ;
	}
    }
  
    // PUBLIC METHODS
    //---------------
    /**
     * Converts the address value to its byte array form.
     * @return The byte array representation of the value.
     */
    public byte[] byteValue() {
	byte[] result = new byte[4] ;
	result[0] = (byte)components[0] ;
	result[1] = (byte)components[1] ;
	result[2] = (byte)components[2] ;
	result[3] = (byte)components[3] ;
    
	return result ;
    }
  
    /**
     * Converts the address to its <CODE>String</CODE> form.
     * Same as <CODE>toString()</CODE>. Exists only to follow a naming scheme.
     * @return The <CODE>String</CODE> representation of the value.
     */
    public String stringValue() {
	return toString() ;
    }

    /**
     * Extracts the ip address from an index OID and returns its
     * value converted as an <CODE>SnmpOid</CODE>.
     * @param index The index array.
     * @param start The position in the index array.
     * @return The OID representing the ip address value.
     * @exception SnmpStatusException There is no ip address value
     * available at the start position.
     */
    public static SnmpOid toOid(long[] index, int start) throws SnmpStatusException {
	if (start + 4 <= index.length) {
	    try {
		return new SnmpOid(
				   index[start],
				   index[start+1],
				   index[start+2],
				   index[start+3]) ;
	    }
	    catch(IllegalArgumentException e) {
		throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
	    }
	}
	else {
	    throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
	}
    }

    /**
     * Scans an index OID, skips the address value and returns the position
     * of the next value.
     * @param index The index array.
     * @param start The position in the index array.
     * @return The position of the next value.
     * @exception SnmpStatusException There is no address value
     * available at the start position.
     */
    public static int nextOid(long[] index, int start) throws SnmpStatusException {
	if (start + 4 <= index.length) {
	    return start + 4 ;
	}
	else {
	    throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
	}
    }
  
    /**
     * Appends an <CODE>SnmpOid</CODE> representing an <CODE>SnmpIpAddress</CODE> to another OID.
     * @param source An OID representing an <CODE>SnmpIpAddress</CODE> value.
     * @param dest Where source should be appended.
     */
    public static void appendToOid(SnmpOid source, SnmpOid dest) {
	if (source.getLength() != 4) {
	    throw new IllegalArgumentException() ;
	}
	dest.append(source) ;
    }

    /**
     * Returns a textual description of the type object.
     * @return ASN.1 textual description.
     */
    final public String getTypeName() {
	return name ;
    }

    // PRIVATE METHODS
    //----------------
    /**
     * Build Ip address from byte array.
     */   
    private void buildFromByteArray(byte[] bytes) {
	if (bytes.length != 4) {
	    throw new IllegalArgumentException() ;
	}
	components = new long[4] ;
	componentCount= 4;
	components[0] = (bytes[0] >= 0) ? bytes[0] : bytes[0] + 256 ;
	components[1] = (bytes[1] >= 0) ? bytes[1] : bytes[1] + 256 ;
	components[2] = (bytes[2] >= 0) ? bytes[2] : bytes[2] + 256 ;
	components[3] = (bytes[3] >= 0) ? bytes[3] : bytes[3] + 256 ;
    }
  
    // VARIABLES
    //----------
    /**
     * Name of the type.
     */
    final static String name = "IpAddress" ;
}
