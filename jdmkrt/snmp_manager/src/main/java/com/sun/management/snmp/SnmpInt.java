/*
 * @(#)file      SnmpInt.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.15
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



import com.sun.jdmk.Enumerated;

/**
 * Represents an SNMP integer.
 *
 *
 * @since Java DMK 5.1
 */

public class SnmpInt extends SnmpValue {
    private static final long serialVersionUID = 995087837429041659L;

    // CONSTRUCTORS
    //-------------
    /**
     * Constructs a new <CODE>SnmpInt</CODE> from the specified integer value.
     * @param v The initialization value.
     * @exception IllegalArgumentException The specified value is smaller than <CODE>Integer.MIN_VALUE</CODE>
     * or larger than <CODE>Integer.MAX_VALUE</CODE>. 
     */
    public SnmpInt(int v) throws IllegalArgumentException {
        if ( isInitValueValid(v) == false ) {
            throw new IllegalArgumentException() ;
        }
        value = (long)v ;
    }

    /**
     * Constructs a new <CODE>SnmpInt</CODE> from the specified <CODE>Integer</CODE> value.
     * @param v The initialization value.
     * @exception IllegalArgumentException The specified value is smaller than <CODE>Integer.MIN_VALUE</CODE>
     * or larger than <CODE>Integer.MAX_VALUE</CODE>. 
     */
    public SnmpInt(Integer v) throws IllegalArgumentException {
        this(v.intValue()) ;
    }

    /**
     * Constructs a new <CODE>SnmpInt</CODE> from the specified long value.
     * @param v The initialization value.
     * @exception IllegalArgumentException The specified value is smaller than <CODE>Integer.MIN_VALUE</CODE>
     * or larger than <CODE>Integer.MAX_VALUE</CODE>. 
     */
    public SnmpInt(long v) throws IllegalArgumentException {
        if ( isInitValueValid(v) == false ) {
            throw new IllegalArgumentException() ;
        }
        value = v ;
    }

    /**
     * Constructs a new <CODE>SnmpInt</CODE> from the specified <CODE>Long</CODE> value.
     * @param v The initialization value.
     * @exception IllegalArgumentException The specified value is smaller than <CODE>Integer.MIN_VALUE</CODE>
     * or larger than <CODE>Integer.MAX_VALUE</CODE>. 
     */
    public SnmpInt(Long v) throws IllegalArgumentException {
        this(v.longValue()) ;
    }

    /**
     * Constructs a new <CODE>SnmpInt</CODE> from the specified <CODE>Enumerated</CODE> value.
     * @param v The initialization value.
     * @exception IllegalArgumentException The specified value is smaller than <CODE>Integer.MIN_VALUE</CODE>
     * or larger than <CODE>Integer.MAX_VALUE</CODE>. 
     * @see Enumerated
     */
    public SnmpInt(Enumerated v) throws IllegalArgumentException {
        this(v.intValue()) ;
    }

    /**
     * Constructs a new <CODE>SnmpInt</CODE> from the specified boolean value.
     * This constructor applies rfc1903 rule:
     * <p><blockquote><pre>
     * TruthValue ::= TEXTUAL-CONVENTION
     *     STATUS       current
     *     DESCRIPTION
     *             "Represents a boolean value."
     *     SYNTAX       INTEGER { true(1), false(2) }
     * </pre></blockquote>
     * @param v The initialization value.
     */
    public SnmpInt(boolean v) {
        value = v ? 1 : 2 ;
    }

    // PUBLIC METHODS
    //---------------
    /**
     * Returns the long value of this <CODE>SnmpInt</CODE>.
     * @return The value.
     */
    public long longValue() {
        return value ;
    }

    /**
     * Converts the integer value to its <CODE>Long</CODE> form.
     * @return The <CODE>Long</CODE> representation of the value.
     */
    public Long toLong() {
        return new Long(value) ;
    }

    /**
     * Converts the integer value to its integer form.
     * @return The integer representation of the value.
     */
    public int intValue() {
        return (int) value ;
    }

    /**
     * Converts the integer value to its <CODE>Integer</CODE> form.
     * @return The <CODE>Integer</CODE> representation of the value.
     */
    public Integer toInteger() {
        return new Integer((int)value) ;
    }
  
    /**
     * Converts the integer value to its <CODE>String</CODE> form.
     * @return The <CODE>String</CODE> representation of the value.
     */
    public String toString() {
        return String.valueOf(value) ;
    }

    /**
     * Converts the integer value to its <CODE>SnmpOid</CODE> form.
     * @return The OID representation of the value.
     */
    public SnmpOid toOid() {
        return new SnmpOid(value) ;
    }
  
    /**
     * Extracts the integer from an index OID and returns its
     * value converted as an <CODE>SnmpOid</CODE>.
     * @param index The index array.
     * @param start The position in the index array.
     * @return The OID representing the integer value.
     * @exception SnmpStatusException There is no integer value
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
     * Scans an index OID, skips the integer value and returns the position
     * of the next value.
     * @param index The index array.
     * @param start The position in the index array.
     * @return The position of the next value.
     * @exception SnmpStatusException There is no integer value
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
     * Appends an <CODE>SnmpOid</CODE> representing an <CODE>SnmpInt</CODE> to another OID.
     * @param source An OID representing an <CODE>SnmpInt</CODE> value.
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
     * @return The <CODE>SnmpValue</CODE> clone.
     */
    final synchronized public SnmpValue duplicate() {
        return (SnmpValue) clone() ;
    }

    /**
     * Clones the <CODE>SnmpInt</CODE> object, making a copy of its data.
     * @return The object clone.
     */
    final synchronized public Object clone() {
        SnmpInt  newclone = null ;
        try {
            newclone = (SnmpInt) super.clone() ;
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
    public String getTypeName() {
        return name ;
    }

    /**
     * This method has been defined to allow the sub-classes
     * of SnmpInt to perform their own control at initialization time.
     */
    boolean isInitValueValid(int v) {
        if ((v < Integer.MIN_VALUE) || (v > Integer.MAX_VALUE)) {
            return false;
        }
        return true;
    }

    /**
     * This method has been defined to allow the sub-classes
     * of SnmpInt to perform their own control at initialization time.
     */
    boolean isInitValueValid(long v) {
        if ((v < Integer.MIN_VALUE) || (v > Integer.MAX_VALUE)) {
            return false;
        }
        return true;
    }

    // VARIABLES
    //----------
    /**
     * Name of the type.
     */
    final static String name = "Integer32" ;

    /**
     * This is where the value is stored. This long is signed.
     * @serial
     */
    protected long value = 0 ;
}
