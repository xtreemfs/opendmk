/*
 * @(#)file      SnmpNull.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.12
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
 * Represents an SNMP null value.
 *
 * @since Java DMK 5.1
 */

public class SnmpNull extends SnmpValue {
    private static final long serialVersionUID = 5241472845263112765L;

    // CONSTRUCTORS
    //-------------
    /**
     * Constructs a new <CODE>SnmpNull</CODE>.
     */
    public SnmpNull() {
        tag = NullTag ;
    }
  
    /**
     * Constructs a new <CODE>SnmpNull</CODE>.
     * <BR>For mibgen private use only.
     */
    public SnmpNull(String dummy) {
        this();
    }
   
    /**
     * Constructs a new <CODE>SnmpNull</CODE> from the specified tag value.
     * @param t The initialization value.
     */
    public SnmpNull(int t) {
        tag = t ;
    }
  
    // PUBLIC METHODS
    //---------------
    /**
     * Returns the tag value of this <CODE>SnmpNull</CODE>.
     * @return The value.
     */
    public int getTag() {
        return tag ;
    }
  
    /**
     * Converts the <CODE>NULL</CODE> value to its ASN.1 <CODE>String</CODE> form.
     * When the tag is not the universal one, it is prepended
     * to the <CODE>String</CODE> form.
     * @return The <CODE>String</CODE> representation of the value.
     */
    public String toString() {
        String result = "" ;
        if (tag != 5) {
            result += "[" + tag + "] " ;
        }
        result += "NULL" ;
        switch(tag) {
    	case errNoSuchObjectTag :
            result += " (noSuchObject)" ;
            break ;
        
    	case errNoSuchInstanceTag :
            result += " (noSuchInstance)" ;
            break ;
        
    	case errEndOfMibViewTag :
            result += " (endOfMibView)" ;
            break ;
        }
        return result ;
    }

    /**
     * Converts the <CODE>NULL</CODE> value to its <CODE>SnmpOid</CODE> form.
     * Normally, a <CODE>NULL</CODE> value cannot be used as an index value,
     * this method triggers an exception.
     * @return The OID representation of the value.
     */
    public SnmpOid toOid() {
        throw new IllegalArgumentException() ;
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
     * Clones the <CODE>SnmpNull</CODE> object, making a copy of its data.
     * @return The object clone.
     */
    final synchronized public Object clone() {
        SnmpNull  newclone = null ;
        try {
            newclone = (SnmpNull) super.clone() ;
            newclone.tag = tag ;
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

    /**
     * Checks if this <CODE>SnmpNull</CODE> object corresponds to a <CODE>noSuchObject</CODE> value. 
     * @return <CODE>true</CODE> if the tag equals {@link com.sun.management.snmp.SnmpDataTypeEnums#errNoSuchObjectTag},
     * <CODE>false</CODE> otherwise.
     */
    public boolean isNoSuchObjectValue() {
        return (tag == SnmpDataTypeEnums.errNoSuchObjectTag);
    }

    /**
     * Checks if this <CODE>SnmpNull</CODE> object corresponds to a <CODE>noSuchInstance</CODE> value. 
     * @return <CODE>true</CODE> if the tag equals {@link com.sun.management.snmp.SnmpDataTypeEnums#errNoSuchInstanceTag},
     * <CODE>false</CODE> otherwise.
     */
    public boolean isNoSuchInstanceValue() {
        return (tag == SnmpDataTypeEnums.errNoSuchInstanceTag);
    }

    /**
     * Checks if this <CODE>SnmpNull</CODE> object corresponds to an <CODE>endOfMibView</CODE> value. 
     * @return <CODE>true</CODE> if the tag equals {@link com.sun.management.snmp.SnmpDataTypeEnums#errEndOfMibViewTag},
     * <CODE>false</CODE> otherwise.
     */
    public boolean isEndOfMibViewValue() {
        return (tag == SnmpDataTypeEnums.errEndOfMibViewTag);
    }
    
    // VARIABLES
    //----------
    /**
     * Name of the type.
     */
    final static String name = "Null" ;

    /**
     * This is the tag of the NULL value. By default, it is the universal tag value.
     */
    private int tag = 5 ;
}
