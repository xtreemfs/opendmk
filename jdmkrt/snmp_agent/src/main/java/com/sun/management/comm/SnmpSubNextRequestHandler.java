/*
 * @(#)file      SnmpSubNextRequestHandler.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.28
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

// java imports
//
import java.util.Vector;

// jmx imports
//
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
// jdmk import
//
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpMibRequest;
import com.sun.management.internal.snmp.SnmpIncomingRequest;

/* NPCTE fix for bugId 4492741, esc 0 */
import com.sun.jdmk.ThreadContext;
/* end of NPCTE fix for bugId 4492741 */

class SnmpSubNextRequestHandler extends SnmpSubRequestHandler {
    private SnmpMibTree root = null;
    /**
     * The constructor initialize the subrequest with the whole varbind
     * list contained in the original request.
     */
    protected SnmpSubNextRequestHandler(SnmpMibAgent agent, 
					SnmpPdu req,
					SnmpMibTree root) {
        super(agent,req);
	init(req, root);
    }
    
    protected SnmpSubNextRequestHandler(SnmpEngine engine,
					SnmpIncomingRequest incRequest,
					SnmpMibAgent agent, 
					SnmpPdu req,
					SnmpMibTree root) {
	super(engine, incRequest, agent, req);
	init(req, root);
	if(logger.finestOn())
	    logger.finest("SnmpSubNextRequestHandler", "Constructor :" + this);
    }
    
    private void init(SnmpPdu req, SnmpMibTree root) {
	this.root = root;
	
        // The translation table is easy in this case ...
        //
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

    public void run() {
    
        try {
	    /* NPCTE fix for bugId 4492741, esc 0, 16-August-2001 */
     	    final ThreadContext oldContext =
                ThreadContext.push("SnmpUserData",data);
	    try { 
		if (logger.finerOn()) {
		    logger.finer("run", "[" + Thread.currentThread() + 
			  "]:getNext operation on " + agent.getMibName());
		}
      
		// Always call with V2. So the merge of the responses will 
		// be easier.
		//
		agent.getNext(createMibRequest(varBind, snmpVersionTwo, data));
	    } finally {
                ThreadContext.restore(oldContext);
            }
	    /* end of NPCTE fix for bugId 4492741 */
      
      
        } catch(SnmpStatusException x) {
            errorStatus = x.getStatus() ;
            errorIndex=  x.getErrorIndex();
            if (logger.finestOn()) {
                logger.finest("run", "[" + Thread.currentThread() + 
		      "]:a Snmp error occured during the operation");
		logger.finest("run",x);
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

    /**
     * The method updates the varbind list of the subrequest.
     */
    protected  void updateRequest(SnmpVarBind var, int pos) {
	if(logger.finestOn())
	    logger.finest("updateRequest", "Copy :" + var);
        int size= varBind.size();
        translation[size]= pos;
	final SnmpVarBind newVarBind = 
	    new SnmpVarBind(var.getOid(), var.getSnmpValue());
	if(logger.finestOn())
	    logger.finest("updateRequest", "Copied :" + newVarBind);
	
	varBind.addElement(newVarBind);
    }
    /**
     * The method updates a given var bind list with the result of a 
     * previously invoked operation.
     * Prior to calling the method, one must make sure that the operation was
     * successful. As such the method getErrorIndex or getErrorStatus should be
     * called.
     */
    protected void updateResult(SnmpVarBind[] result) {
  
        final int max=varBind.size();
        for(int i= 0; i< max ; i++) {
            // May be we should control the position ...
            //
            final int index= translation[i];
            final SnmpVarBind elmt= 
		(SnmpVarBind)((NonSyncVector)varBind).elementAtNonSync(i);

            final SnmpVarBind vb= result[index];
            if (vb == null) {
                result[index]= elmt;
		/* NPCTE fix for bugid 4381195 esc 0. <J.C.> < 17-Oct-2000> */
	        // if ((elmt != null) &&  (elmt.value == null) && 
		//    (version == snmpVersionTwo)) 
		//    elmt.value = SnmpVarBind.endOfMibView;
 		/* end of NPCTE fix for bugid 4381195 */
                continue;
            }

            final SnmpValue val= vb.getSnmpValue();
            if ((val == null)|| (val == SnmpVarBind.endOfMibView)){
		/* NPCTE fix for bugid 4381195 esc 0. <J.C.> < 17-Oct-2000> */
		if ((elmt != null) &&
		    (elmt.getSnmpValue() != SnmpVarBind.endOfMibView)) 
		    result[index]= elmt;
		// else if ((val == null) && (version == snmpVersionTwo)) 
		//    vb.value = SnmpVarBind.endOfMibView;
		continue;
 		/* end of NPCTE fix for bugid 4381195 */
            }
     
	    /* NPCTE fix for bugid 4381195 esc 0. <J.C.> < 17-Oct-2000> */
	    if (elmt == null) continue;
	    /* end of NPCTE fix for bugid 4381195 */

	    if (elmt.getSnmpValue() == SnmpVarBind.endOfMibView) continue;


            // Now we need to take the smallest oid ...
            //
	    int comp = elmt.getOid().compareTo(vb.getOid());
            if (comp < 0) {
	      // Take the smallest (lexicographically)
                //
                result[index]= elmt;
            }
	    else {
		if(comp == 0) {
		    // Must compare agent used for reply
		    // Take the deeper within the reply
		    if(logger.finestOn()) {
			logger.finer("updateResult",
				     " oid overlapping. Oid : " + 
				     elmt.getOid() + "value :" + 
				     elmt.getSnmpValue());
			logger.finer("updateResult",
				     "Already present varBind : " + vb);
		    }
		    
		    SnmpOid oid = vb.getOid();
		    SnmpMibAgent deeperAgent = root.getAgentMib(oid);

		    if(logger.finestOn())
			logger.finer("updateResult","Deeper agent : " + 
				     deeperAgent);
		    if(deeperAgent == agent) {
			if(logger.finestOn())
			    logger.finer("updateResult",
				     "The current agent is the deeper one. "+
				     "Update the value with the current one");
			result[index].setSnmpValue(elmt.getSnmpValue());
		    }
		}
	    }
	}
    }    

    protected String makeDebugTag() {
        return "SnmpSubNextRequestHandler";
    }
}
