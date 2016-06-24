/*
 * @(#)file      Host.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.17
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



// java import
//
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Vector;
import java.security.acl.NotOwnerException;

// jdmk import
//
import com.sun.jdmk.internal.ClassLogger;

  
/**
 * The class defines an abstract representation of a host.
 *
 *
 * @since Java DMK 5.1
 */
abstract class Host extends SimpleNode implements Serializable {
  
    public Host(int id) {
        super(id);
    }

    public Host(Parser p, int id) {
        super(p, id);
    }
  
    protected abstract PrincipalImpl createAssociatedPrincipal()
        throws UnknownHostException;
  
    protected abstract String getHname();

    public void buildAclEntries(PrincipalImpl owner, AclImpl acl) {
        // Create a principal
        //
        PrincipalImpl p=null;
        try {
            p = createAssociatedPrincipal();
        } catch(UnknownHostException e) {
            if (logger.finestOn()) {
                logger.finest("buildAclEntries", "Cannot create ACL entry for " + e.getMessage());
            }
	    throw new IllegalArgumentException("Cannot create ACL entry for " + e.getMessage());
        }

        // Create an AclEntry
        //
        AclEntryImpl entry= null;
        try {
            entry = new AclEntryImpl(p);
            // Add permission
            //
            registerPermission(entry);
            acl.addEntry(owner, entry);
        } catch(UnknownHostException e) {
            if (logger.finestOn()) {
                logger.finest("buildAclEntries", "Cannot create ACL entry for " + e.getMessage());
            }
            return;
        } catch(NotOwnerException a) {
            if (logger.finestOn()) {
                logger.finest("buildAclEntries", "Not owner of ACL " + a.getMessage());
            }
            return;
        }
    }
    
    private void registerPermission(AclEntryImpl entry) {
        JDMHost host= (JDMHost) jjtGetParent(); 
        JDMManagers manager= (JDMManagers) host.jjtGetParent();
        JDMAclItem acl= (JDMAclItem) manager.jjtGetParent();
        JDMAccess access= (JDMAccess) acl.getAccess();
        access.putPermission(entry);
        JDMCommunities comm= (JDMCommunities) acl.getCommunities();
        comm.buildCommunities(entry);
    } 

    public void buildTrapEntries(Hashtable dest) {
        
        JDMHostTrap host= (JDMHostTrap) jjtGetParent(); 
        JDMTrapInterestedHost hosts= (JDMTrapInterestedHost) host.jjtGetParent();
        JDMTrapItem trap = (JDMTrapItem) hosts.jjtGetParent();
        JDMTrapCommunity community = (JDMTrapCommunity) trap.getCommunity();
        String comm = community.getCommunity();
	
        InetAddress add = null;
        try {
            add = java.net.InetAddress.getByName(getHname());
        } catch(UnknownHostException e) {
            if (logger.finestOn()) {
                logger.finest("buildTrapEntries", "Cannot create TRAP entry for " + e.getMessage());
            }
            return;
        }
	
        Vector list = null;
        if (dest.containsKey(add)){
            list = (Vector) dest.get(add);
            if (!list.contains(comm)){
                list.addElement(comm);
            }
        } else {
            list = new Vector();
            list.addElement(comm);
            dest.put(add,list);
        }
    }
    
    public void buildInformEntries(Hashtable dest) {
    
        JDMHostInform host= (JDMHostInform) jjtGetParent(); 
        JDMInformInterestedHost hosts= (JDMInformInterestedHost) host.jjtGetParent();
        JDMInformItem inform = (JDMInformItem) hosts.jjtGetParent();
        JDMInformCommunity community = (JDMInformCommunity) inform.getCommunity();
        String comm = community.getCommunity();
	
        InetAddress add = null;
        try {
            add = java.net.InetAddress.getByName(getHname());
        } catch(UnknownHostException e) {
            if (logger.finestOn()) {
                logger.finest("buildInformEntries", "Cannot create INFORM entry for " + e.getMessage());
            }
            return;
        }
	
        Vector list = null;
        if (dest.containsKey(add)){
            list = (Vector) dest.get(add);
            if (!list.contains(comm)){
                list.addElement(comm);
            }
        } else {
            list = new Vector();
            list.addElement(comm);
            dest.put(add,list);
        }
    }
    
    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"Host");
    
    String dbgTag = "Host";
}
