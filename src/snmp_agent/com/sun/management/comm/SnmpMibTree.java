/*
 * @(#)file      SnmpMibTree.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.18
 * @(#)lastedit  07/03/08
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


package com.sun.management.comm;



// java imports
//
import java.util.Vector;
import java.util.Enumeration;

// jmx imports
//
import com.sun.management.snmp.SnmpOid;

// jdmk imports
//
import com.sun.management.snmp.agent.SnmpMibAgent;
  
/**
 * The class is used for building a tree representation of the different
 * root oids of the supported MIBs. Each node is associated to a specific MIB.
 *
 * @since Java DMK 5.1
 */
final class SnmpMibTree {
    
    public SnmpMibTree() {
	defaultAgent= null;
	root= new TreeNode(-1, null);
    }
    
    public boolean isMibReferenced(SnmpMibAgent agent) {
	return root.isMibReferenced(agent);
    }
    

    public void setDefaultAgent(SnmpMibAgent def) {
        defaultAgent= def;
        root.agent= def;
    }
  
    public SnmpMibAgent getDefaultAgent() {
        return defaultAgent;
    }
  
    public void register(SnmpMibAgent agent) {
        root.registerNode(agent);
    }

    public void register(SnmpMibAgent agent, long[] oid) {
	root.registerNode(oid, 0, agent);
    }
  
    public SnmpMibAgent getAgentMib(SnmpOid oid) {
        TreeNode node= root.retrieveMatchingBranch(oid.longValue(), 0);
	
        if (node == null)
            return defaultAgent;
        else
	    if(node.getAgentMib() == null)
		return defaultAgent;
	    else
		return node.getAgentMib();
    }
    
    public void unregister(SnmpMibAgent agent, SnmpOid[] oids) {
	for(int i = 0; i < oids.length; i++) {
	    long[] oid = oids[i].longValue();
	    TreeNode node = root.retrieveMatchingBranch(oid, 0);
	    if (node == null)
		continue;
	    node.removeAgent(agent);
	}
    }

    
    public void unregister(SnmpMibAgent agent) {
	
	root.removeAgentFully(agent);
    }
    
    private SnmpMibAgent defaultAgent;    
    private TreeNode root;
    
    // A SnmpMibTree object is a tree of TreeNode
    //
    final class TreeNode {
	
        void registerNode(SnmpMibAgent agent) {
            long[] oid= agent.getRootOid();
            registerNode(oid, 0, agent);
        }
	
        TreeNode retrieveMatchingBranch(long[] oid, int cursor) {
	    TreeNode node= retrieveChild(oid, cursor);
            if (node == null)
                return this;
            if (children.size() == 0) {
                // In this case, the node does not have any children. So no point to
                // continue the search ...
                return node;
            }
            if( cursor + 1 == oid.length) {
                // In this case, the oid does not have any more element. So the search
                // is over.
                return node;
            }
	    
            TreeNode n = node.retrieveMatchingBranch(oid, cursor + 1);
	    //If the returned node got a null agent, we have to replace it by 
	    //the current one (in case it is not null)
	    //
	    return n.agent == null ? this : n;
        }
	
        SnmpMibAgent getAgentMib() {
            return agent;
        }
	
        // PRIVATE STUFF
        //--------------
      
        /**
         * Only the treeNode class can create an instance of treeNode.
         * The creation occurs when registering a new oid.
         */
        private TreeNode(long nodeValue, TreeNode sup) {
            this.nodeValue= nodeValue;
            this.parent= sup;
        }
	
	private boolean isMibReferenced(SnmpMibAgent agent) {
	    if(this.agent == agent) return true;
	    for(Enumeration e = children.elements(); e.hasMoreElements(); ) {
                TreeNode node = (TreeNode) e.nextElement();
		if (node.isMibReferenced(agent)) return true;
            }
	    return false;
	}

	private void removeAgentFully(SnmpMibAgent agent) {
	    for(Enumeration e= children.elements(); e.hasMoreElements(); ) {
		TreeNode node= (TreeNode) e.nextElement();
                node.removeAgentFully(agent);
            }
            removeAgent(agent);
	    
	}

    private void removeAgent(SnmpMibAgent mib) {
	    if(agent != mib)
		return;
	    
	    agent = null;
        }
	
	private void setAgent(SnmpMibAgent agent) {
	    this.agent = agent;
	}

        private void registerNode(long[] oid, int cursor, SnmpMibAgent agent) {

            if (cursor >= oid.length)
                //That's it !
                //
                return;	    
            TreeNode child = retrieveChild(oid, cursor);
            if (child == null) {
                long theValue= oid[cursor];
		child= new TreeNode(theValue, this);
                children.addElement(child);
            }
	    
	    // We have to set the agent attribute
	    //
	    if(cursor == (oid.length - 1)) {
		child.setAgent(agent);
	    }
	    else
		child.registerNode(oid, cursor+1, agent);
        }
	
        private TreeNode retrieveChild(long[] oid, int current) {
            long theValue= oid[current];
	
            for(Enumeration e= children.elements(); e.hasMoreElements(); ) {
                TreeNode node= (TreeNode) e.nextElement();
                if (node.match(theValue)) {
                    return node;
		}
            }
            return null;
        }
      
        final private boolean match(long value) {
            return (nodeValue == value) ? true : false;
        }
      
        private Vector children = new Vector();
	long nodeValue;
        protected SnmpMibAgent agent;
    private TreeNode parent;
    
}; // end of class TreeNode
}
