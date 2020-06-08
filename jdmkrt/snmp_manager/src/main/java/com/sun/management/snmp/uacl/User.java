/*
 * @(#)file      User.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.12
 * @(#)lastedit  07/03/08
 * @(#)build     @BUILD_TAG_PLACEHOLDER@
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
 */


package com.sun.management.snmp.uacl;



// java import
//
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Vector;
import com.sun.jdmk.security.acl.NotOwnerException;

// jdmk import
//
import com.sun.jdmk.internal.ClassLogger;


/**
 * The class defines an abstract representation of a host.
 *
 *
 * @since Java DMK 5.1
 */
abstract class User extends SimpleNode implements Serializable {

    public User(int id) {
        super(id);
    }

    public User(Parser p, int id) {
        super(p, id);
    }

    protected abstract PrincipalImpl createAssociatedPrincipal();

    protected abstract String getName();

    public void buildAclEntries(PrincipalImpl owner, AclImpl acl) {
        // Create a principal
        //
        PrincipalImpl p=null;
	p = createAssociatedPrincipal();

        // Create an AclEntry
        //
        AclEntryImpl entry= null;
	entry = new AclEntryImpl(p);
	// Add permission
	//
	registerPermission(entry);
	try {
	    acl.addEntry(owner, entry);
	} catch(NotOwnerException a) {
            if (logger.finestOn()) {
                logger.finest("buildAclEntries", "Not owner of ACL " +
			      a);
            }
            return;
        }
    }

    private void registerPermission(AclEntryImpl entry) {
        //JDMUsers user = (JDMUser) jjtGetParent();
        JDMUsers users= (JDMUsers) jjtGetParent();
        JDMAclItem acl= (JDMAclItem) users.jjtGetParent();
        JDMAccess access= (JDMAccess) acl.getAccess();
        access.putPermission(entry);
	JDMSecurityLevel secLevel = (JDMSecurityLevel) acl.getSecurityLevel();
	secLevel.setSecurityLevel(entry);
        JDMContextNames comm= (JDMContextNames) acl.getContextNames();
        comm.buildContextNames(entry);
    }

    // Logging
    //--------
    private static final ClassLogger logger =
	new ClassLogger(ClassLogger.LOGGER_SNMP,"User");

    String dbgTag = "User";
}
