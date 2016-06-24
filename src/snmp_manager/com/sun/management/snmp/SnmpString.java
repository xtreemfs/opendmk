/*
 * @(#)file      SnmpString.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.18
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

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Represents an SNMP string.
 *
 * @since Java DMK 5.1
 */

public class SnmpString extends SnmpValue {
    private static final long serialVersionUID = -7995335496739639922L;

    // CONSTRUCTORS
    //-------------
    /**
     * Constructs a new empty <CODE>SnmpString</CODE>.
     */
    public SnmpString() {
        value = new byte[0] ;
    }

    /**
     * Constructs a new <CODE>SnmpString</CODE> from the specified bytes array.
     * @param v The bytes composing the string value.
     */
    public SnmpString(byte[] v) {
        value = (byte[])v.clone() ;
    }

    /**
     * Constructs a new <CODE>SnmpString</CODE> from the specified <CODE>Bytes</CODE> array.
     * @param v The <CODE>Bytes</CODE> composing the string value.
     */
    public SnmpString(Byte[] v) {
        value = new byte[v.length] ;
        for (int i = 0 ; i < v.length ; i++) {
            value[i] = v[i].byteValue() ;
        }
    }

    /**
     * Constructs a new <CODE>SnmpString</CODE> from the specified <CODE>String</CODE> value.
     * @param v The initialization value.
     */
    public SnmpString(String v) {
        value = v.getBytes() ;
    }

    /**
     * Constructs a new <CODE>SnmpString</CODE> from the specified <CODE> InetAddress </Code>.
     * @param address The <CODE>InetAddress </CODE>.
     *
     */
    public SnmpString(InetAddress address) {
	value = address.getAddress();
    }

    // PUBLIC METHODS
    //---------------
    
    /**
     * Converts the string value to its <CODE> InetAddress </CODE> form.
     * @return an {@link InetAddress} defined by the string value.
     * @exception UnknownHostException If string value is not a legal address format.
     *
     */
    public InetAddress inetAddressValue() throws UnknownHostException {
	return InetAddress.getByAddress(value);
    }
    
    /**
     * Converts the specified binary string into a character string.
     * @param bin The binary string value to convert.
     * @return The character string representation.
     */
    public static String BinToChar(String bin) {
        char value[] = new char[bin.length()/8]; 
        int binLength = value.length;
        for (int i = 0; i < binLength; i++) 
            value[i] = (char)Integer.parseInt(bin.substring(8*i, 8*i+8), 2); 
        return new String(value); 
    }

    /**
     * Converts the specified hexadecimal string into a character string.
     * @param hex The hexadecimal string value to convert.
     * @return The character string representation.
     */
    public static String HexToChar(String hex) {
        char value[] = new char[hex.length()/2]; 
        int hexLength = value.length;
        for (int i = 0; i < hexLength; i++) 
            value[i] = (char)Integer.parseInt(hex.substring(2*i, 2*i+2), 16); 
        return new String(value); 
    }
    
    /**
     * Returns the bytes array of this <CODE>SnmpString</CODE>.
     * @return The value.
     */
    public byte[] byteValue() {
        return value ;
    }
  
    /**
     * Converts the string value to its array of <CODE>Bytes</CODE> form.
     * @return The array of <CODE>Bytes</CODE> representation of the value.
     */
    public Byte[] toByte() {
        Byte[] result = new Byte[value.length] ;
        for (int i = 0 ; i < value.length ; i++) {
            result[i] = new Byte(value[i]) ;
        }
        return result ;
    }
  
    /**
     * Converts the string value to its <CODE>String</CODE> form.
     * @return The <CODE>String</CODE> representation of the value.
     */
    public String toString() {
        return new String(value) ;
    }

    /**
     * Converts the string value to its <CODE>SnmpOid</CODE> form.
     * @return The OID representation of the value.
     */
    public SnmpOid toOid() {
        long[] ids = new long[value.length] ;
        for (int i = 0 ; i < value.length ; i++) {
            ids[i] = (long)(value[i] & 0xFF) ;
        }
        return new SnmpOid(ids) ;
    }
 
    /**
     * Extracts the string from an index OID and returns its
     * value converted as an <CODE>SnmpOid</CODE>.
     * @param index The index array.
     * @param start The position in the index array.
     * @return The OID representing the string value.
     * @exception SnmpStatusException There is no string value
     * available at the start position.
     */
    public static SnmpOid toOid(long[] index, int start) throws SnmpStatusException {
        try {
            if (index[start] > Integer.MAX_VALUE) {
                throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
            }
            int strLen = (int)index[start++] ;
            long[] ids = new long[strLen] ;
            for (int i = 0 ; i < strLen ; i++) {
                ids[i] = index[start + i] ;
            }
            return new SnmpOid(ids) ;
        }
        catch(IndexOutOfBoundsException e) {
            throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
        }
    }

    /**
     * Scans an index OID, skips the string value and returns the position
     * of the next value.
     * @param index The index array.
     * @param start The position in the index array.
     * @return The position of the next value.
     * @exception SnmpStatusException There is no string value
     * available at the start position.
     */
    public static int nextOid(long[] index, int start) throws SnmpStatusException {
        try {
            if (index[start] > Integer.MAX_VALUE) {
                throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
            }
            int strLen = (int)index[start++] ;
            start += strLen ;
            if (start <= index.length) {
                return start ;
            }
            else {
                throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
            }
        }
        catch(IndexOutOfBoundsException e) {
            throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
        }
    }
  
    /**
     * Appends an <CODE>SnmpOid</CODE> representing an <CODE>SnmpString</CODE> to another OID.
     * @param source An OID representing an <CODE>SnmpString</CODE> value.
     * @param dest Where source should be appended.
     */
    public static void appendToOid(SnmpOid source, SnmpOid dest) {
        dest.append(source.getLength()) ;
        dest.append(source) ;
    }

    /**
     * Performs a clone action. This provides a workaround for the
     * <CODE>SnmpValue</CODE> interface.
     * @return The SnmpValue clone.
     */
    final synchronized public SnmpValue duplicate() {
        return (SnmpValue) clone() ;
    }

    /**
     * Clones the <CODE>SnmpString</CODE> object, making a copy of its data.
     * @return The object clone.
     */
    synchronized public Object clone() {
        SnmpString newclone = null ;

        try {
            newclone = (SnmpString) super.clone() ;
            newclone.value = new byte[value.length] ;
            System.arraycopy(value, 0, newclone.value, 0, value.length) ;
        } catch (CloneNotSupportedException e) {
            throw new InternalError() ; // vm bug.
        }
        return newclone ;
    }

    /**
     * Returns a textual description of the type object.
     * @return ASN.1 textual description.
     */
    public String getTypeName() {
        return name ;
    }

    // VARIABLES
    //----------
    /**
     * Name of the type.
     */
    final static String name = "String" ;

    /**
     * This is the bytes array of the string value.
     * @serial
     */
    protected byte[] value = null ;
}
