/*
 * @(#)file      SnmpSubBulkRequestHandler.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.25
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


package com.sun.management.comm;



// java import
//
import java.util.Enumeration;
import java.util.Vector;

// jmx imports
//
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpEngine;
// jdmk import
//
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpMibRequest;
import com.sun.jdmk.ThreadContext;
import com.sun.management.internal.snmp.SnmpIncomingRequest;
import com.sun.jdmk.ThreadContext;

class SnmpSubBulkRequestHandler extends SnmpSubRequestHandler {
    private SnmpMibTree root = null;

    /**
     * The constructor initialize the subrequest with the whole varbind list 
     * contained in the original request.
     */
    protected SnmpSubBulkRequestHandler(SnmpEngine engine,
					SnmpIncomingRequest incRequest,
					SnmpMibAgent agent, 
					SnmpPdu req, 
					int nonRepeat,
                                        int maxRepeat, 
					int R,
					SnmpMibTree root) {
	super(engine, incRequest, agent, req);
	init(req, nonRepeat, maxRepeat, R, root);
    }
    
    /**
     * The constructor initialize the subrequest with the whole varbind list 
     * contained in the original request.
     */
    protected SnmpSubBulkRequestHandler(SnmpMibAgent agent, 
					SnmpPdu req, 
					int nonRepeat,
                                        int maxRepeat, 
					int R,
					SnmpMibTree root) {
	super(agent, req);
	init(req, nonRepeat, maxRepeat, R, root);
    }
    
    public void run() {
    
        size= varBind.size();
    
        try {
            // Invoke a getBulk operation
            //
	    /* NPCTE fix for bugId 4492741, esc 0, 16-August-2001 */
	    final ThreadContext oldContext =
                ThreadContext.push("SnmpUserData",data);
	    try {
		if (logger.finerOn()) {
		    logger.finer("run", "[" + Thread.currentThread() + 
				 "]:getBulk operation on " + 
				 agent.getMibName());
		}
		agent.getBulk(createMibRequest(varBind,version,data), 
			      nonRepeat, maxRepeat);
	    } finally {
                ThreadContext.restore(oldContext);
            }  
	    /* end of NPCTE fix for bugId 4492741 */
	    
        } catch(SnmpStatusException x) {
            errorStatus = x.getStatus() ;
            errorIndex=  x.getErrorIndex();
            if (logger.finestOn()) {
                logger.finest("run", "[" + Thread.currentThread() + 
		      "]:an Snmp error occured during the operation");
            }
        }
        catch(Exception x) {
            errorStatus = SnmpDefinitions.snmpRspGenErr ;
            if (logger.finestOn()) {
                logger.finest("run", "[" + Thread.currentThread() + 
		      "]: Unexpected exception: " + x);
		logger.finest("run",x);
            }
            if (logger.finerOn()) {
                logger.finer("run", "[" + Thread.currentThread() + 
		      "]:a generic error occured during the operation");
            }
        }
        if (logger.finerOn()) {
            logger.finer("run", "[" + Thread.currentThread() + 
		  "]:operation completed");
        }
    }
    
    private void init(SnmpPdu req,
		      int nonRepeat,
		      int maxRepeat, 
		      int R,
		      SnmpMibTree root) {
	this.root = root;
        this.nonRepeat= nonRepeat;
        this.maxRepeat= maxRepeat;  
        this.globalR= R;
	
	final int max= translation.length;
        final SnmpVarBind[] list= req.varBindList;
        final NonSyncVector nonSyncVarBind = ((NonSyncVector)varBind);
        for(int i=0; i < max; i++) {
            translation[i]= i;
            // we need to allocate a new SnmpVarBind. Otherwise the first
            // sub request will modify the list...
            //
	    final SnmpVarBind newVarBind = 
		new SnmpVarBind(list[i].getOid(), list[i].getSnmpValue());
            nonSyncVarBind.addNonSyncElement(newVarBind);
        }
    }
    
    /**
     * The method updates find out which element to use at update time. 
     * Handle oid overlapping as well
     */
    private SnmpVarBind findVarBind(SnmpVarBind element, 
				    SnmpVarBind result) {
	
	if (element == null) return null;

	if (result.getOid() == null) {
	     return element;
	}

	if (element.getSnmpValue() == SnmpVarBind.endOfMibView) return result;

	if (result.getSnmpValue() == SnmpVarBind.endOfMibView) return element;

	final SnmpValue val = result.getSnmpValue();

	int comp = element.getOid().compareTo(result.getOid());
	if(logger.finestOn()) {
	    logger.finest("findVarBind","Comparing OID element : " + 
			  element.getOid() + " with result : " + 
			  result.getOid());
	    logger.finest("findVarBind","Values element : " + 
			  element.getSnmpValue() +
			  " result : " + result.getSnmpValue());
	}
	if (comp < 0) {
	    // Take the smallest (lexicographically)
	    //
	    return element;
	}
	else {
	    if(comp == 0) {
		// Must compare agent used for reply
		// Take the deeper within the reply
		if(logger.finestOn()) {
		    logger.finest("findVarBind"," oid overlapping. Oid : " + 
				  element.getOid() + "value :" + 
				  element.getSnmpValue());
		    logger.finest("findVarBind","Already present varBind : " + 
				  result);
		}
		SnmpOid oid = result.getOid();
		SnmpMibAgent deeperAgent = root.getAgentMib(oid);

		if(logger.finestOn())
		    logger.finest("findVarBind","Deeper agent : " + 
				  deeperAgent);
		if(deeperAgent == agent) {
		    if(logger.finestOn())
			logger.finest("updateResult",
				      "The current agent is the deeper one. "+
				      "Update the value with the current one");
		    return element;
		} else {
		    if(logger.finestOn())
			logger.finest("updateResult",
			"Current is not the deeper, return the previous one.");
		    return result;
		}
		    
	    }
	    else {
		if(logger.finestOn())
		    logger.finest("findVarBind",
			  "The right varBind is the already present one");
		return result;
	    }
	}
    }
    /**
     * The method updates a given var bind list with the result of a 
     * previously invoked operation.
     * Prior to calling the method, one must make sure that the operation was
     * successful. As such the method getErrorIndex or getErrorStatus should be
     * called.
     */
    protected void updateResult(SnmpVarBind[] result) {
	// we can assume that the run method is over ...
        // 

        final Enumeration e= varBind.elements();
        final int max= result.length;

        // First go through all the values once ...
        for(int i=0; i < size; i++) {
            // May be we should control the position ...
            //
            if (e.hasMoreElements() == false)
                return;

	    // bugId 4641694: must check position in order to avoid 
	    //       ArrayIndexOutOfBoundException
	    final int pos=translation[i];
	    if (pos >= max) {
		if (logger.finestOn())
		    logger.finest("updateResult",
				  "Position `"+pos+"' is out of bound...");
		continue;
	    }

	    final SnmpVarBind element= (SnmpVarBind) e.nextElement();
	    
	    if (element == null) continue;
	    if (logger.finestOn())
		logger.finest("updateResult", 
			      "Non repeaters Current element : " + 
			      element + " from agent : " + agent);
	    final SnmpVarBind res = findVarBind(element,result[pos]);
	    
	    if(res == null) continue;
	    
	    result[pos] = res;
	}
 
        // Now update the values which have been repeated
        // more than once.
        int localR= size - nonRepeat;
        for (int i = 2 ; i <= maxRepeat ; i++) {
            for (int r = 0 ; r < localR ; r++) {
                final int pos = (i-1)* globalR + translation[nonRepeat + r] ;
                if (pos >= max)
                    return;
                if (e.hasMoreElements() ==false)
                    return;
                final SnmpVarBind element= (SnmpVarBind) e.nextElement();
		
		if (element == null) continue;
		if (logger.finestOn())
		    logger.finest("updateResult", 
				  "Repeaters Current element : " + 
				  element + " from agent : " + agent);
		final SnmpVarBind res = findVarBind(element, result[pos]);
		
		if(res == null) continue;
		
		result[pos] = res;
            }
        }
    }
  
    protected String makeDebugTag() {
        return "SnmpSubBulkRequestHandler";
    }
    
    // PROTECTED VARIABLES
    //------------------

    /**
     * Specific to the sub request
     */
    protected int nonRepeat=0;
  
    protected int maxRepeat=0;
  
    /**
     * R as defined in RCF 1902 for the global request the sub-request is 
     * associated to.
     */
    protected int globalR=0;

    protected int size=0;
}
