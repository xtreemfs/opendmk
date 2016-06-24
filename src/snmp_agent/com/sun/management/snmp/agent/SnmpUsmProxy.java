/*
 * @(#)file      SnmpUsmProxy.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.31
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

package com.sun.management.snmp.agent;

// java import
//
import java.net.UnknownHostException;

// RI import
//
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.manager.SnmpPeer;
import com.sun.management.snmp.manager.usm.SnmpUsmPeer;
import com.sun.management.snmp.manager.usm.SnmpUsmParameters;
import com.sun.management.snmp.manager.SnmpParams;
import com.sun.management.snmp.manager.SnmpRequest;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpScopedPduPacket;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpUnknownModelException;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngine;
// jdmk import
//
import com.sun.management.snmp.usm.SnmpUsm;
import com.sun.jdmk.internal.ClassLogger;

/**
 * The SnmpUsmProxy class provides an implementation of 
 * an SNMP V3 proxy. It uses the Usm based manager API in order to forward 
 * calls to distant peer. This is the object to instantiate in order to 
 * proxy an SNMP V3 agent that is Usm based.
 *
 * @since Java DMK 5.1
 */
public class SnmpUsmProxy extends SnmpV3Proxy {
    private static final long serialVersionUID = -6442234160949252976L;
    String user = null;
    boolean timelinessRetry = true;
    // CONSTRUCTORS
    //===============
    //

    /**
     * Initializes this SNMP proxy with a SnmpPeer to which calls are 
     * forwarded. The passed root oid is used when registering the proxy 
     * within the adaptor. The proxy name default value is "SnmpUsmProxy".
     * @param engine The SNMP adaptor engine.
     * @param peer The peer representing the proxied agent.
     * @param rootOid The proxy root oid.
     * @exception IllegalArgumentException if the passed engine is null.
     * @exception SnmpStatusException An error occurred while accessing
     *            a MIB node.
     */

    public SnmpUsmProxy(SnmpEngine engine,
			SnmpUsmPeer peer,
			String rootOid) 
	throws  SnmpStatusException
    {
	super(engine,
	      peer,
	      rootOid,
	      "SnmpUsmProxy");
    }

    /**
     * Initializes this SNMP proxy with a SnmpPeer to which calls are
     * forwarded. The passed root oid is used when registering the proxy 
     * within the adaptor. The name is used when registering the proxy in 
     * the <CODE> SnmpAdaptorServer </CODE>
     * @param engine The SNMP adaptor engine.
     * @param peer The peer representing the proxied agent.
     * @param rootOid The proxy root oid.
     * @param name The proxy name.
     * @exception IllegalArgumentException if the passed engine is null.
     * @exception SnmpStatusException An error occurred while accessing
     *            a MIB node.
     */
    public SnmpUsmProxy(SnmpEngine engine,
			SnmpUsmPeer peer,
			String rootOid,
			String name) 
	throws SnmpStatusException
    {
	super(engine,
	      peer,
	      rootOid,
	      name);
    }
    
    /**
     * The distant EngineId.
     * @return The peer engine Id.
     */
    public SnmpEngineId getEngineId() {
	return ((SnmpUsmPeer)getPeer()).getEngineId();
    }
    
    /**
     * When receiving a report, the proxy will resynchronize if the report 
     * is due to a timeliness desynchronization. Default value is true.
     * @param retry true will retry, false will not.
     */
    public void resyncOnTimelinessReport(boolean retry) {
	timelinessRetry = retry;
    }
    
    // PROTECTED METHODS, SnmpProxy extensible API.
    //=============================================
    //
    /**
     * Called when a report is received when forwarding a request. If the 
     * returned value is true, the proxy will retry the current request.
     * @param request The request containing the received report.
     * @return <code>true</code> means retry the call, the report has been 
     *    handled; <code>false</code> means don't retry, the report 
     *    wasn't fixed.
     *    <P> If the received report is  NotInTimeWindow, the 
     *    <CODE>SnmpUsmPeer</CODE> is resynchronized. 
     *    If the synchronization succeeded, <code>true</code> is returned,
     *    <code>false</code> otherwise.
     */
    protected boolean handleReport(SnmpRequest request) {
	if(logger.finestOn()) {
	    logger.finest("handleReport", "Received a report");
	}
	
	if(!timelinessRetry) {
	    if(logger.finestOn())
		logger.finest("handleReport", "No retrying, return false");
	    return false;
	}
	
	SnmpVarBindList vbl = request.getReportVarBindList();
	SnmpOid notInTime = null;
	try {
	    notInTime = new SnmpOid(SnmpUsm.usmStatsNotInTimeWindows);
	}catch(Exception e) {
	    return false;
	}
	if(vbl.indexOfOid(notInTime) != -1) {
	    if(logger.finestOn()) 
		logger.finest("handleReport", 
			      "Received a Not in Time Window report");
	    
	    SnmpUsmPeer peer = (SnmpUsmPeer) request.getPeer();
	    try {
		peer.processUsmTimelinessDiscovery();
		 if(logger.finestOn()) 
		     logger.finest("handleReport", 
				   " synchro done. EngineId : " + 
				   peer.getEngineId() + " time : " + 
				   peer.getEngineTime() + " boot : " +
				   peer.getEngineBoots());
	    }catch(SnmpStatusException e) {
		if(logger.finestOn())
		    logger.finest("handleReport", 
				  "Time Window synchro failed.");
		return false;
	    }
	    return true;
	}else {
	    if(logger.finestOn()) {
		logger.finest("handleReport", 
		   "Received a report that is not usmStatsNotInTimeWindows");
	    }
	    return false;
	}
    }

    /**
     * Factory parameters method. Overload this method in order to create 
     * your own parameters.
     * @param pdu The pdu received in the <CODE>SnmpMibRequest</CODE> or null 
     *    if received pdu was null.
     * @return The Usm parameters to use when forwarding the call.
     */
    final protected SnmpParams createParameters(SnmpPdu pdu) 
	throws SnmpStatusException {
	SnmpUsmPeer p =  (SnmpUsmPeer) getPeer();
	//If the pdu is null, use the default user,
	if(pdu == null || !(pdu instanceof SnmpScopedPduPacket) ) {
	    if(logger.finestOn())
		logger.finest("createParameters", "pdu :"+ pdu);
	    return p.getParams();
	}
	else {
	    SnmpScopedPduPacket pack = (SnmpScopedPduPacket) pdu; 
	    if(logger.finestOn())
		logger.finest("createParameters", "pdu security parameters :" 
		      + pack.securityParameters);
	    String principal = pack.securityParameters.getPrincipal();
	    if(logger.finestOn())
		logger.finest("createParameters", "pdu principal :" + 
			      principal);
	    SnmpUsmParameters pa = null;
	    try {
		pa = new SnmpUsmParameters(getEngine(), principal);
	    }catch(SnmpUnknownModelException e) {
		if(logger.finerOn())
		    logger.finer("createParameters", "Failed " + e);
		throw new SnmpStatusException(e.toString());
	    }
	    pa.setContextEngineId(p.getEngineId().getBytes());
	    return pa;
	}
    }

    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_PROXY_SNMP,"SnmpUsmProxy");

    String dbgTag = "SnmpUsmProxy";
}
