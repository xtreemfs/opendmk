/*
 * @(#)file      SnmpOpaque.java
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



/**
 * Is used to represent an SNMP value.
 * The <CODE>Opaque</CODE> type is defined in RFC 1155.
 *
 *
 * @since Java DMK 5.1
 */

public class SnmpOpaque extends SnmpString {
    private static final long serialVersionUID = -4528903626829210227L;

    // CONSTRUCTORS
    //-------------
    /**
     * Constructs a new <CODE>SnmpOpaque</CODE> from the specified bytes array.
     * @param v The bytes composing the opaque value.
     */
    public SnmpOpaque(byte[] v) {
	super(v) ;
    }

    /**
     * Constructs a new <CODE>SnmpOpaque</CODE> with the specified <CODE>Bytes</CODE> array.
     * @param v The <CODE>Bytes</CODE> composing the opaque value.
     */
    public SnmpOpaque(Byte[] v) {
	super(v) ;
    }

    /**
     * Constructs a new <CODE>SnmpOpaque</CODE> from the specified <CODE>String</CODE> value.
     * @param v The initialization value.
     */
    public SnmpOpaque(String v) {
	super(v) ;
    }

    // PUBLIC METHODS
    //---------------
    /**
     * Converts the opaque to its <CODE>String</CODE> form, that is, a string of
     * bytes expressed in hexadecimal form.
     * @return The <CODE>String</CODE> representation of the value.
     */
    public String toString() {
	StringBuffer result = new StringBuffer() ;
	for (int i = 0 ; i < value.length ; i++) {
	    byte b = value[i] ;
	    int n = (b >= 0) ? b : b + 256 ;
	    result.append(Character.forDigit(n / 16, 16)) ;
	    result.append(Character.forDigit(n % 16, 16)) ;
	}
	return result.toString() ;
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
    final static String name = "Opaque" ;
}
