/*
 * @(#)file      PermissionImpl.java
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


package com.sun.management.snmp.IPAcl;



import java.io.Serializable;


/**
 * Permission is represented as a String.
 *
 * @see java.security.acl.Permission
 *
 * @since Java DMK 5.1
 */

class PermissionImpl implements java.security.acl.Permission, Serializable {
    private static final long serialVersionUID = 4852094599955129196L;
    private String perm = null;
  
    /**
     * Constructs a permission.
     *
     * @param s the string representing the permission.
     */
    public PermissionImpl(String s) {
	perm = s;
    }
    
    public int hashCode() {
	if (perm == null) return 0; 
	return perm.hashCode();
    }
  
    /**
     * Returns true if the object passed matches the permission 
     * represented in. 
     *
     * @param p the Permission object to compare with. 
     * @return true if the Permission objects are equal, false otherwise.
     */
    public boolean equals(Object p){
	if (p instanceof PermissionImpl){
	    return perm.equals(((PermissionImpl)p).getString());
	} else {
	    return false;
	}
    }
  
    /**
     * Prints a string representation of this permission. 
     *
     * @return a string representation of this permission. 
     */
    public String toString(){
	return perm.toString();
    }
  
    /**
     * Prints the permission. 
     *
     * @return a string representation of this permission. 
     */
    public String getString(){
	return perm;
    }
}

