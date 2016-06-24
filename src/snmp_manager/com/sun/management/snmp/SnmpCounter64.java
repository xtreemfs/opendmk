/*
 * @(#)file      SnmpCounter64.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.11
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

import com.sun.jdmk.UnsignedLong;

/**
 * Represents an SNMP 64bits counter.
 *
 * @since Java DMK 5.1
 */

public class SnmpCounter64 extends SnmpValue {
    // NPCTE fix for bugId 4692891, esc 537693, MR,  June 2002  
    private static final long serialVersionUID = 6119804398925703166L;
    // end of NPCTE fix for bugId 4692891

    // CONSTRUCTORS
    //-------------
    /**
     * Constructs a new <CODE>SnmpCounter64</CODE> from the specified long value.
     * @param v The initialization value.
     * @exception IllegalArgumentException The specified value is negative
     * or larger than <CODE>Long.MAX_VALUE</CODE>. 
     */
    public SnmpCounter64(long v) throws IllegalArgumentException {

	// NOTE:
	// The max value for a counter64 variable is 2^64 - 1.
	// The max value for a Long is 2^63 - 1.
	// All the allowed values for a conuter64 variable cannot be covered !!!
	//
	if ((v < 0) || (v > Long.MAX_VALUE)) {
	    throw new IllegalArgumentException() ;
	}
	value = v ;
    }
  
    /**
     * Constructs a new <CODE>SnmpCounter64</CODE> from the specified <CODE>Long</CODE> value.
     * @param v The initialization value.
     * @exception IllegalArgumentException The specified value is negative
     * or larger than <CODE>Long.MAX_VALUE</CODE>. 
     */
    public SnmpCounter64(Long v) throws IllegalArgumentException {
	this(v.longValue()) ;
    }
    
    // NPCTE fix for bugId 4692891, esc 537693, MR,  June 2002
    /**
     * Constructs a new <CODE>SnmpCounter64</CODE> from the specified UnsignedLong value.
     * @param v The initialization value.
     * @exception IllegalArgumentException The specified value is 
     * larger than <CODE>Long.MAX_VALUE</CODE>.
     */
    public SnmpCounter64(UnsignedLong v) throws IllegalArgumentException {
        value = v.longValue() ;
    }
    // end of NPCTE fix for bugId 4692891

    // PUBLIC METHODS
    //---------------
    /**
     * Returns the counter value of this <CODE>SnmpCounter64</CODE>.
     * @return The value.
     */
    public long longValue() {
	return value ;
    }

    /**
     * Converts the counter value to its <CODE>Long</CODE> form.
     * @return The <CODE>Long</CODE> representation of the value.
     */
    public Long toLong() {
	return new Long(value) ;
    }
    
    /**
     * Converts the counter value to its integer form.
     * @return The integer representation of the value.
     */
    public int intValue() {
	return (int)value ;
    }
    
    // NPCTE fix for bugId 4692891, esc 537693, MR,  June 2002
    /**
     * Converts the counter value to its UnsignedLong form.
     * @return The UnsignedLong representation of the value.
     */
    public UnsignedLong toUnsignedLong() {
        return UnsignedLong.make(value);
    }
    // end of NPCTE fix for bugId 4692891
    
    /**
     * Converts the counter value to its <CODE>Integer</CODE> form.
     * @return The <CODE>Integer</CODE> representation of the value.
     */
    public Integer toInteger() {
	return new Integer((int)value) ;
    }

    /**
     * Converts the counter value to its <CODE>String</CODE> form.
     * @return The <CODE>String</CODE> representation of the value.
     */
    public String toString() {
	// NPCTE fix for bugId 4692891, esc 537693, MR,  June 2002
        if (value >=  0)
            return String.valueOf(value) ;
        else
            return (UnsignedLong.make(value)).toString();
	// end of NPCTE fix for bugId 4692891
    }
  
    /**
     * Converts the counter value to its <CODE>SnmpOid</CODE> form.
     * @return The OID representation of the value.
     */
    public SnmpOid toOid() {
	return new SnmpOid(value) ;
    }
  
    /**
     * Extracts the counter from an index OID and returns its
     * value converted as an <CODE>SnmpOid</CODE>.
     * @param index The index array.
     * @param start The position in the index array.
     * @return The OID representing the counter value.
     * @exception SnmpStatusException There is no counter value
     * available at the start position.
     */
    public static SnmpOid toOid(long[] index, int start) throws SnmpStatusException {
	try {
	    return new SnmpOid(index[start]) ;
	}
	catch(IndexOutOfBoundsException e) {
	    throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
	}
    }

    /**
     * Scans an index OID, skips the counter value and returns the position
     * of the next value.
     * @param index The index array.
     * @param start The position in the index array.
     * @return The position of the next value.
     * @exception SnmpStatusException There is no counter value
     * available at the start position.
     */
    public static int nextOid(long[] index, int start) throws SnmpStatusException {
	if (start >= index.length) {
	    throw new SnmpStatusException(SnmpStatusException.noSuchName) ;
	}
	else {
	    return start + 1 ;
	}
    }
    
    /**
     * Appends an <CODE>SnmpOid</CODE> representing an <CODE>SnmpCounter64</CODE> to another OID.
     * @param source An OID representing an <CODE>SnmpCounter64</CODE> value.
     * @param dest Where source should be appended.
     */
    public static void appendToOid(SnmpOid source, SnmpOid dest) {
	if (source.getLength() != 1) {
	    throw new IllegalArgumentException() ;
	}
	dest.append(source) ;
    }
  
    /**
     * Performs a clone action. This provides a workaround for the
     * <CODE>SnmpValue</CODE> interface.
     * @return The SnmpValue clone.
     */
    final synchronized public SnmpValue duplicate() {
	return (SnmpValue)clone() ;
    }

    /**
     * Clones the <CODE>SnmpCounter64</CODE> object, making a copy of its data.
     * @return The object clone.
     */
    final synchronized public Object clone() {
        SnmpCounter64  newclone = null ;
        try {
	    newclone = (SnmpCounter64) super.clone() ;
	    newclone.value = value ;
        } catch (CloneNotSupportedException e) {
	    throw new InternalError() ; // vm bug.
        }
        return newclone ;
    }

    /**
     * Returns a textual description of the type object.
     * @return ASN.1 textual description.
     */
    final public String getTypeName() {
        return name ;
    }

    // VARIABLES
    //----------
    /**
     * Name of the type.
     */
    final static String name = "Counter64" ;
  
    /**
     * This is where the value is stored. This long is positive.
     * @serial
     */
    private long value = 0 ;
}
