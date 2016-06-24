/*
 * @(#)file      SnmpStringFixed.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.14
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


// @(#)SnmpStringFixed.java 4.14 07/03/08 SMI

// java imports
//
import java.lang.Math;

/**
 * Represents an SNMP String defined with a fixed length. 
 * The class is mainly used when dealing with table indexes for which one of the keys
 * is defined as a <CODE>String</CODE>.
 *
 *
 * @since Java DMK 5.1
 */

public class SnmpStringFixed extends SnmpString {
    private static final long serialVersionUID = -2800002500347958885L;

    // CONSTRUCTORS
    //-------------
    /**
     * Constructs a new <CODE>SnmpStringFixed</CODE> from the specified bytes array.
     * @param v The bytes composing the fixed-string value.
     */
    public SnmpStringFixed(byte[] v) {
        super(v) ;
    }

    /**
     * Constructs a new <CODE>SnmpStringFixed</CODE> with the specified <CODE>Bytes</CODE> array.
     * @param v The <CODE>Bytes</CODE> composing the fixed-string value.
     */
    public SnmpStringFixed(Byte[] v) {
        super(v) ;
    }

    /**
     * Constructs a new <CODE>SnmpStringFixed</CODE> from the specified <CODE>String</CODE> value.
     * @param v The initialization value.
     */
    public SnmpStringFixed(String v) {
        super(v) ;
    }

    /**
     * Constructs a new <CODE>SnmpStringFixed</CODE> from the specified <CODE>bytes</CODE> array
     * with the specified length.
     * @param l The length of the fixed-string.
     * @param v The <CODE>bytes</CODE> composing the fixed-string value.
     * @exception IllegalArgumentException Either the length or the <CODE>byte</CODE> array is not valid.
     */
    public SnmpStringFixed(int l, byte[] v) throws IllegalArgumentException {
        if ((l <= 0) || (v == null)) {
            throw new IllegalArgumentException() ;
        }
        int length = Math.min(l, v.length);
        value = new byte[l] ;
        for (int i = 0 ; i < length ; i++) {
            value[i] = v[i] ;
        }
        for (int i = length ; i < l ; i++) {
            value[i] = 0 ;
        }
    }
      
    /**
     * Constructs a new <CODE>SnmpStringFixed</CODE> from the specified <CODE>Bytes</CODE> array
     * with the specified length.
     * @param l The length of the fixed-string.
     * @param v The <CODE>Bytes</CODE> composing the fixed-string value.
     * @exception IllegalArgumentException Either the length or the <CODE>Byte</CODE> array is not valid.
     */
    public SnmpStringFixed(int l, Byte[] v) throws IllegalArgumentException {
        if ((l <= 0) || (v == null)) {
            throw new IllegalArgumentException() ;
        }
        int length = Math.min(l, v.length);
        value = new byte[l] ;
        for (int i = 0 ; i < length ; i++) {
            value[i] = v[i].byteValue() ;
        }
        for (int i = length ; i < l ; i++) {
            value[i] = 0 ;
        }
    }
      
    /**
     * Constructs a new <CODE>SnmpStringFixed</CODE> from the specified <CODE>String</CODE>
     * with the specified length.
     * @param l The length of the fixed-string.
     * @param s The <CODE>String</CODE> composing the fixed-string value.
     * @exception IllegalArgumentException Either the length or the <CODE>String</CODE> is not valid.
     */
    public SnmpStringFixed(int l, String s) throws IllegalArgumentException {
        if ((l <= 0) || (s == null)) {
            throw new IllegalArgumentException() ;
        }
        byte[] v = s.getBytes();
        int length = Math.min(l, v.length);
        value = new byte[l] ;
        for (int i = 0 ; i < length ; i++) {
            value[i] = v[i] ;
        }
        for (int i = length ; i < l ; i++) {
            value[i] = 0 ;
        }
    }
      
    // PUBLIC METHODS
    //---------------
    /**
     * Extracts the fixed-string from an index OID and returns its
     * value converted as an <CODE>SnmpOid</CODE>.
     * @param l The number of successive array elements to be retrieved
     * in order to construct the OID.
     * These elements are retrieved starting at the <CODE>start</CODE> position.
     * @param index The index array.
     * @param start The position in the index array.
     * @return The OID representing the fixed-string value.
     * @exception SnmpStatusException There is no string value
     * available at the start position.
     */
    public static SnmpOid toOid(int l, long[] index, int start) throws SnmpStatusException {
        try {
            long[] ids = new long[l] ;
            for (int i = 0 ; i < l ; i++) {
                ids[i] = index[start + i] ;
            }
            return new SnmpOid(ids) ;
        }
        catch(IndexOutOfBoundsException e) {
            throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
        }
    }

    /**
     * Scans an index OID, skip the string value and returns the position
     * of the next value.
     * @param l The number of successive array elements to be passed 
     * in order to get the position of the next value.
     * These elements are passed starting at the <CODE>start</CODE> position.
     * @param index The index array.
     * @param start The position in the index array.
     * @return The position of the next value.
     * @exception SnmpStatusException There is no string value
     * available at the start position.
     */
    public static int nextOid(int l, long[] index, int start) throws SnmpStatusException {
        int result = start + l ;
        if (result > index.length) {
            throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
        }
        return result ;
    }

    /**
     * Appends an <CODE>SnmpOid</CODE> representing an <CODE>SnmpStringFixed</CODE> to another OID.
     * @param l Unused.
     * @param source An OID representing an <CODE>SnmpStringFixed</CODE> value.
     * @param dest Where source should be appended.
     */
    public static void appendToOid(int l, SnmpOid source, SnmpOid dest) {
        dest.append(source) ;
    }
}
