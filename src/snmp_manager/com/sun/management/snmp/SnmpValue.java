/*
 * @(#)file      SnmpValue.java
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



import java.io.Serializable;

/**
 * Is an abstract representation of an SNMP Value.
 * All classes provided for dealing with SNMP types should derive from this
 * class.
 *
 *
 * @since Java DMK 5.1
 */

public abstract class SnmpValue implements Cloneable, Serializable, SnmpDataTypeEnums {
  
    /**
     * Returns a <CODE>String</CODE> form containing ASN.1 tagging information.
     * @return The <CODE>String</CODE> form.
     */
    public String toAsn1String() {
	return "[" + getTypeName() + "] " + toString();
    }
  
    /**
     * Returns the value encoded as an OID.
     * The method is particularly useful when dealing with indexed table made of
     * several SNMP variables.
     * @return The value encoded as an OID.
     */
    public abstract SnmpOid toOid() ;
  
    /**
     * Returns a textual description of the object.
     * @return ASN.1 textual description.
     */
    public abstract String getTypeName() ;
  
    /**
     * Same as clone, but you cannot perform cloning using this object because
     * clone is protected. This method should call <CODE>clone()</CODE>.
     * @return The <CODE>SnmpValue</CODE> clone.
     */
    public abstract SnmpValue duplicate() ;  

    /**
     * This method returns <CODE>false</CODE> by default and is redefined
     * in the {@link com.sun.management.snmp.SnmpNull} class. 
     */
    public boolean isNoSuchObjectValue() {
        return false;
    }

    /**
     * This method returns <CODE>false</CODE> by default and is redefined
     * in the {@link com.sun.management.snmp.SnmpNull} class. 
     */
    public boolean isNoSuchInstanceValue() {
        return false;
    }

    /**
     * This method returns <CODE>false</CODE> by default and is redefined
     * in the {@link com.sun.management.snmp.SnmpNull} class. 
     */
    public boolean isEndOfMibViewValue() {
        return false;
    }
}
