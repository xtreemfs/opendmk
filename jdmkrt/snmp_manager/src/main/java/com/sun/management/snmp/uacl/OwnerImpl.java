/*
 * @(#)OwnerImpl.java	1.9
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.9
 * @(#)date      07/03/08
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


package com.sun.management.snmp.uacl;



import java.util.Vector;
import java.io.Serializable;

import java.security.Principal;
import java.security.acl.Owner; 
import java.security.acl.LastOwnerException; 
import java.security.acl.NotOwnerException; 


/**
 * Owner of Access Control Lists (ACLs).
 * The initial owner Principal should be specified as an
 * argument to the constructor of the class AclImpl.
 *
 * @see java.security.acl.Owner
 *
 * @since Java DMK 5.1
 */

class OwnerImpl implements Owner, Serializable {
    private static final long serialVersionUID = 6554169470971658903L;
    private Vector ownerList = null;
  
    /**
     * Constructs an empty list of owner.
     */
    public OwnerImpl (){
	ownerList = new Vector();
    }
  
    /**
     * Constructs a list of owner with the specified principal as first 
     * element.
     *
     * @param owner the principal added to the owner list.
     */
    public OwnerImpl (PrincipalImpl owner){
	ownerList = new Vector();
	ownerList.addElement(owner);
    }
  
    /**
     * Adds an owner. Only owners can modify ACL contents. The caller principal
     * must be an owner of the ACL in order to invoke this method. That is, 
     * only an owner can add another owner. The initial owner is configured at
     * ACL construction time.
     *
     * @param caller the principal invoking this method. It must be an owner 
     *        of the ACL.
     * @param owner the owner that should be added to the list of owners.
     * @return true if successful, false if owner is already an owner. 
     * @exception NotOwnerException if the caller principal is not an owner of 
     *  the ACL. 
     */
    public boolean addOwner(Principal caller, Principal owner) 
	throws NotOwnerException {
	if (!ownerList.contains(caller)) 
	    throw new NotOwnerException();
	
	if (ownerList.contains(owner)) {
	    return false;
	} else {
	    ownerList.addElement(owner);
	    return true;
	}
    }
  
    /**
     * Deletes an owner. If this is the last owner in the ACL, an exception is 
     * raised.
     *<P> The caller principal must be an owner of the ACL in order to invoke 
     * this method.</P> 
     *
     * @param caller the principal invoking this method. It must be an owner of
     *        the ACL. 
     * @param owner the owner to be removed from the list of owners. 
     * @return true if successful, false if owner is already an owner. 
     * @exception NotOwnerException if the caller principal is not an owner of 
     *            the ACL. 
     * @exception LastOwnerException if there is only one owner left, so that 
     *            deleteOwner would leave the ACL owner-less.
     */
    public boolean deleteOwner(Principal caller, Principal owner)
	throws NotOwnerException,LastOwnerException {

	if (!ownerList.contains(caller))
	    throw new NotOwnerException();
	
	if (!ownerList.contains(owner)){
	    return false;
	} else {
	    if (ownerList.size() == 1)
		throw new LastOwnerException();
	    
	    ownerList.removeElement(owner);
	    return true;
	}
    }
  
    /**
     * Returns true if the given principal is an owner of the ACL.
     *
     * @param owner the principal to be checked to determine whether
     *        or not it is an owner. 
     * @return true if the given principal is an owner of the ACL.
     */
    public boolean isOwner(Principal owner){
	return ownerList.contains(owner);
    }
}
