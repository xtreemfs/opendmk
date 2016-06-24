/*
 * @(#)file      SnmpUsmPeer.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.36
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
package com.sun.management.snmp.manager.usm;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.sun.management.snmp.usm.SnmpUsmEnginePeer;
import com.sun.management.snmp.usm.SnmpUsm;
import com.sun.management.snmp.usm.SnmpUsmSecurityParameters;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.internal.snmp.SnmpSecuritySubSystem;
import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.snmp.SnmpUnknownModelException;
import com.sun.jdmk.internal.ClassLogger;


import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpDefinitions;

import com.sun.management.snmp.manager.SnmpPeer;
import com.sun.management.snmp.manager.SnmpParams;
import com.sun.management.snmp.manager.SnmpV3Parameters;
import com.sun.management.snmp.manager.SnmpRequest;
import com.sun.management.snmp.manager.SnmpSession;

/**
 * This class models a distant SNMP V3 agent using the User based Security Model for V3 requests.
 *
 * @since Java DMK 5.1
 */
public class SnmpUsmPeer extends SnmpPeer {
    private static final long serialVersionUID = -4395909104949922299L;
    /**
     * The internal peer that is located in the Usm model. The SnmpUsmPeer delegates the discovery and the biggest part of timeliness parameters to this object.
     */
    SnmpUsmEnginePeer peer = null;
    /**
     * A reference to the local Engine.
     */
    SnmpEngineImpl engine = null;
    /**
     * A reference to the local Usm. Use to access its SnmpUsmEnginePeer object.
     */
    SnmpUsm model = null;
    
    private boolean authoritative = false;
    // CONSTRUCTORS
    //-------------
    
    /**
     * Creates an SNMP peer object for a device. The default port is 161. The passed parameters are used to discover the authoritative engine Id.
     * @param engine The SNMP engine.
     * @param host The peer name.
     * @exception UnknownHostException If the host name cannot be resolved.
     * @exception SnmpUnknownModelException If Usm is not present in the passed engine.
     * @exception SnmpStatusException If an error occurred when discovering the distant engine Id.
     */
    public SnmpUsmPeer(SnmpEngine engine, String host) 
	throws UnknownHostException, SnmpUnknownModelException, SnmpStatusException {
        super(host);
	init(engine, null, true);
    }

    /**
     * Creates an SNMP peer object for a device. The default port is 161. The passed parameters are used to discover the authoritative engine Id.
     * @param engine The SNMP engine.
     * @param netaddr The peer <CODE>InetAddress</CODE>.
     * @exception SnmpUnknownModelException If Usm is not present in the passed engine.
     * @exception SnmpStatusException If an error occurred when discovering the distant engine Id.
     */
    public SnmpUsmPeer(SnmpEngine engine, InetAddress netaddr) 
	throws SnmpUnknownModelException, SnmpStatusException {
        super(netaddr);
	init(engine, null, true);
    }

    /**
     * Creates an SNMP peer object for a device with the specified port. The passed parameters are used to discover the authoritative engine Id.
     * @param engine The SNMP engine.
     * @param host The peer name.
     * @param port The port number.
     * @exception UnknownHostException If the host name cannot be resolved.
     * @exception SnmpUnknownModelException If Usm is not present in the passed engine.
     * @exception SnmpStatusException If an error occurred when discovering the distant engine Id.
     */
    public SnmpUsmPeer(SnmpEngine engine, String host, int port) 
	throws UnknownHostException, SnmpUnknownModelException, SnmpStatusException {
	super(host, port);
	init(engine, null, true);
    }
  /**
     * Creates an SNMP peer object for a device. The default port is 161.
     * @param engine The SNMP engine.
     * @param host The peer name.
     * @param id The remote agent engine Id. If no discovery is to be done and the engine Id is useless (when sending trap from the <CODE> SnmpV3AdaptorServer </CODE>, null can be provided as the engineId. Having null as engineId will make the other requests to fail (thrown of a <CODE>SnmpStatusException/CODE> when sending get, set, inform, ...).
     * @exception UnknownHostException If the host name cannot be resolved.
     * @exception SnmpUnknownModelException If Usm is not present in the passed engine.
     * @exception SnmpStatusException If the passed engine Id is equal to the local engine Id.
     */

    public SnmpUsmPeer(SnmpEngine engine, String host, SnmpEngineId id) 
	throws UnknownHostException, SnmpUnknownModelException, SnmpStatusException {
        super(host);
	if(id == null) authoritative = true;
	init(engine, id, false);
    }

    /**
     * Creates an SNMP peer object for a device. The default port is 161.
     * @param engine The SNMP engine.
     * @param netaddr The peer <CODE>InetAddress</CODE>.
     * @param id The remote agent engine Id. If no discovery is to be done and the engine Id is useless (when sending trap from the <CODE> SnmpV3AdaptorServer </CODE>, null can be provided as the engineId. Having null as engineId will make the other requests to fail (thrown of a <CODE>SnmpStatusException</CODE> when sending get, set, inform, ...).
     * @exception SnmpUnknownModelException If Usm is not present in the passed engine.
     * @exception SnmpStatusException If the passed engine Id is equal to the local engine Id.
     */
    public SnmpUsmPeer(SnmpEngine engine, InetAddress netaddr, SnmpEngineId id) 
	throws SnmpUnknownModelException, SnmpStatusException {
        super(netaddr);
	if(id == null) authoritative = true;
	init(engine, id, false);
    }

    /**
     * Creates an SNMP peer object for a device with the specified port.
     * @param engine The SNMP engine.
     * @param host The peer name.
     * @param port The port number.
     * @param id The remote agent engine Id. If no discovery is to be done and the engine Id is useless (when sending trap from the <CODE> SnmpV3AdaptorServer </CODE>) you should provide null for this parameter. Doing so will make the <CODE>SnmpUsmPeer</CODE> an authoritative one. Sending get,set,getnext,getbulk,informs, ... with an authoritative peer will make the requests to fail.
     * @exception UnknownHostException If the host name cannot be resolved.
     * @exception SnmpUnknownModelException If Usm is not present in the passed engine.
     * @exception SnmpStatusException If the passed engine Id is equal to the local engine Id.
     */
    public SnmpUsmPeer(SnmpEngine engine, String host, int port, SnmpEngineId id) 
	throws UnknownHostException, SnmpUnknownModelException, SnmpStatusException {
	super(host, port);
	if(id == null) authoritative = true;
	init(engine, id, false);
    }

    /**
     * Initialize its state with the local engine and model. The Usm engine peer object can't be initialized because NO engine Id is known at this time.
     */
    private void init(SnmpEngine eng, SnmpEngineId id, boolean discovery) 
	throws SnmpUnknownModelException, SnmpStatusException {
	engine = (SnmpEngineImpl) eng;
	model = (SnmpUsm) engine.getSecuritySubSystem().getModel(SnmpUsm.ID);
	if(discovery)
	    processEngineIdDiscovery();
	else 
	    setEngineId(id);
	if(engine.getEngineId().equals(getEngineId())) {
	    if(logger.finerOn()) {
		logger.finer("init","Warning, discovered engine id ["+ 
		      getEngineId() +
		      "] is equal to local one [" + 
		      engine.getEngineId() + 
		      "] \n");
	    }
	    throw new SnmpStatusException("Discovered engine id [ "+ 
					  getEngineId() +
					  "] is equal to local one [" + 
					  engine.getEngineId() + 
					  "]");
	}
    }

    /**
     * Test is the peer is synchronized with distant Engine time and boots.
     * @return True synchronized, {@link #processUsmTimelinessDiscovery} has been successfully called. False not synchronized.
     */
    public boolean isTimelinessSynchronized() {
	if(peer == null) return false;
	if((peer.getAuthoritativeEngineBoots() == 0) && 
	   (peer.getAuthoritativeEngineTime() == 0) )
	    return false;
	else
	    return true;

    }
    
    /**
     * A peer is authoritative if the remote engine Id is useless when making requests. This is true when using <CODE>SnmpUsmPeer</CODE> for trap sending. Every <CODE>SnmpUsmPeer</CODE> created with a null <CODE>SnmpEngineId</CODE> is an authoritative peer.
     * @return true if authoritative, false otherwise.
     */
    public boolean isAuthoritative() {
	return authoritative;
    }

    /**
     * Returns the nb boots of the distant engine.
     * @return The nb boots.
     */
    public int getEngineBoots() {
	if(peer == null) return 0;
	return peer.getAuthoritativeEngineBoots();
    }
    /**
     * Returns the time of the distant engine.
     * @return The engine time.
     */
    public int getEngineTime() {
	if(peer == null) return 0;
	return peer.getAuthoritativeEngineTime();
    }
    /**
     * Sets explicitly the engine Id of the distant engine. Be aware that the passed value MUST be IDENTICAL to the engine this peer is linked to. You should better use the discovery process.
     * @param engineId The engine Id to use.
     */
    private void setEngineId(SnmpEngineId engineId) {
	if(engineId != null)
	    peer = model.getEnginePeer(engineId);
    }
    /**
     * Returns the distant engine Id or null if not yet known.
     * @return The engine Id.
     */
    public SnmpEngineId getEngineId() {
	if(peer == null) return null;
	return peer.getAuthoritativeEngineId();
    }
    
    /**
     * Call this method in order to discover Usm timeliness needed infos. You should call this method only if you do some authenticated requests.
     * @exception SnmpStatusException If an error occurs during timeliness discovery. Check <CODE> SnmpDefinitions </CODE> in order to define error status.
     */
    public void processUsmTimelinessDiscovery() throws SnmpStatusException {
	if(peer == null) 
	    throw new IllegalArgumentException("No engine Id, this peer can't be used for timeliness discovery.");

	if(getParams() instanceof SnmpUsmParameters) {
	    SnmpUsmParameters usm = (SnmpUsmParameters) getParams();
	    if( (usm.getSecurityLevel() & SnmpDefinitions.authNoPriv) != 0) {
		if(usm.getPrincipal() != null) {
		    processUsmDiscovery(usm.getPrincipal());
		    return;
		}
	    }
	    else {
		if(logger.finestOn()) 
		    logger.finest("processUsmTimelinessDiscovery", "Discovery must be done in an authenticated way.");
		throw new SnmpStatusException(SnmpDefinitions.snmpBadSecurityLevel, 0);
	    }
	}
	if(logger.finestOn()) 
	    logger.finest("processUsmTimelinessDiscovery", "Parameters are not SnmpUsmParameters. Can't process discovery");
	throw new SnmpStatusException("Parameters are not SnmpUsmParameters. Can't process discovery");
    }
    
    /**
     * Set the params used when making requests.
     * @param p The params to set.
     */
    public void setParams(SnmpParams p) {
	if(p instanceof SnmpUsmParameters) {
	    SnmpUsmParameters usm = (SnmpUsmParameters) p;
	    usm.setEngineId(getEngineId());
	}
	super.setParams(p);
    }

    /**
     * The Usm security model object.
     * @return The User based Security Model.
     */
    SnmpUsm getSecurityModel() {
	return model;
    }
    /**
     * If no engineId has been provided with <CODE>setEngineId()</CODE>, calling this method will activate the distant SNMP agent Engine Id discovery.
     */
    private void processEngineIdDiscovery() throws SnmpStatusException {
	SnmpParams current = null;
	SnmpSession discoverySession = null;
	if(peer == null) {
	    try {
		current = getParams();
		SnmpUsmParameters v3params = null;
		try {
		    v3params = new SnmpUsmParameters(engine);
		}catch(SnmpUnknownModelException e) {
		    if(logger.finerOn())
			logger.finer("processEngineIdDiscovery", "Failed " + e);
		    return;
	}
		SnmpUsmSecurityParameters usmp = 
		    (SnmpUsmSecurityParameters) v3params.getSecurityParameters();
		setParams(v3params);
		discoverySession = 
		    new SnmpSession(engine, 
				    "Discovery session", 
				    this);
		discoverEngineId(discoverySession, v3params);
		if(logger.finerOn())
		    logger.finer("processEngineIdDiscovery", "Engine Id : " + 
			  getEngineId());
	    }finally{
		// Set the params that where there before the call.
		setParams(current);
		discoverySession.destroySession();
	    }
	}
    }
    /**
     * This method should be called once address and port are set. It will discover distant nb boots and time parameters as well as engine Id if needed.
     * @param principal The principal to use in order to make the <CODE>authNoPriv</CODE> discovery call.
     */
    private void processUsmDiscovery(String principal) 
	throws SnmpStatusException {
	//If some params have been previously set, must safe then.
	// This method should be synchronized. But it doesn't work if synchronized. The deadlock to investigate...
	SnmpParams current = getParams();
	SnmpUsmParameters v3params = null;
	try {
	    v3params = new SnmpUsmParameters(engine,
					     principal);
	}catch(SnmpUnknownModelException e) {
	    if(logger.finerOn())
		logger.finer("processUsmDiscovery", "Failed. Can't happen." + e);
	    return;
	}
	SnmpUsmSecurityParameters usmp = 
	    (SnmpUsmSecurityParameters) v3params.getSecurityParameters();
	setParams(v3params);
	SnmpSession discoverySession = 
	    new SnmpSession(engine, "Discovery session", this);

	try {
	    if(peer == null) 
		discoverEngineId(discoverySession, v3params);
	    else 
		peer.reset();
	    
	    usmp.setAuthoritativeEngineId(getEngineId());
	    discoverTimelinessParams(discoverySession, v3params);
	    if(logger.finerOn())
		logger.finer("processUsmDiscovery","Result :\n" +
		      "Engine Id : " + getEngineId() + "\n" +
		      "Engine nb boots : " +  getEngineBoots() + "\n" +
		      "Engine time : " + getEngineTime());
	}finally{
	    // Set the params that where ther before the call.
	    setParams(current);
	    discoverySession.destroySession();
	}
    }
    /**
     * make the engine id discovery work.
     */
    private void discoverEngineId(SnmpSession discoverySession,
				  SnmpV3Parameters v3params) 
	throws SnmpStatusException {
	SnmpVarBindList l = new SnmpVarBindList();
	SnmpUsmSecurityParameters secParams = (SnmpUsmSecurityParameters)
	    v3params.getSecurityParameters();
	secParams.setAuthoritativeEngineBoots(0);
	secParams.setAuthoritativeEngineTime(0);
	v3params.setSecurityLevel(SnmpDefinitions.noAuthNoPriv);
	SnmpRequest req = discoverySession.snmpGetRequest(this, 
							  null, 
							  l);
	
	boolean completed = req.waitForCompletion(getTimeout());

	if( (completed == false) || 
	    (req.getErrorStatus() != SnmpDefinitions.snmpRspNoError) ||
	    !req.isReport() ) {
	    if(logger.finestOn()) 
		logger.finest("discoverEngineId", "An error occurred when processing Engine discovery. Request status : " + req.snmpErrorToString(req.getErrorStatus()));
	    if(req.getErrorStatus() == SnmpDefinitions.snmpUsmBadEngineId) {
		if(logger.finestOn()) 
		    logger.finest("discoverEngineId", "The received engine Id is invalid or is equal to the local one.");
	    }
	    
	    throw new SnmpStatusException(req.getErrorStatus());
	}
	
	SnmpUsmSecurityParameters usmp = (SnmpUsmSecurityParameters) 
	    req.getResponseSecurityParameters();
	setEngineId(usmp.getAuthoritativeEngineId());

	if(logger.finestOn()) {
	    logger.finest("discoverEngineId","Result :\n" +
		  "Engine Id : " + getEngineId());
	}
    }
  /**
     * make the engine nb boots and time discovery work.
     */
    private void discoverTimelinessParams(SnmpSession discoverySession,
					  SnmpV3Parameters v3params) 
	throws SnmpStatusException {
	SnmpVarBindList l = new SnmpVarBindList();
	SnmpUsmSecurityParameters secParams = (SnmpUsmSecurityParameters)
	    v3params.getSecurityParameters();
	secParams.setAuthoritativeEngineBoots(0);
	secParams.setAuthoritativeEngineTime(0);
	v3params.setSecurityLevel(SnmpDefinitions.authNoPriv);
	SnmpRequest req = discoverySession.snmpGetRequest(this, null, l);
	boolean completed = req.waitForCompletion(getTimeout());
	if( (completed == false) || 
	    (req.getErrorStatus() != SnmpDefinitions.snmpRspNoError)||
	    !req.isReport() ) {
	    if(logger.finestOn())
		logger.finest("discoverTimelinessParams","An error occurred when processing Engine discovery. Request status : " + req.snmpErrorToString(req.getErrorStatus()));
	    throw new SnmpStatusException(req.getErrorStatus());
	}
	if(req.isReport()) {
	    SnmpVarBindList list = req.getReportVarBindList();
	    if(list.size() == 0)
		throw new SnmpStatusException("Received an empty report");
	    if(logger.finestOn())
		logger.finest("discoverTimelinessParams","Received a report, list size is : " + list.size());
	    SnmpOid oid = new SnmpOid("1.3.6.1.6.3.15.1.1.2.0");
	    
	    SnmpVarBind var = list.getVarBindAt(0);
	    SnmpOid o = var.getOid();
	    if(oid.equals(o)) {
		if(logger.finestOn())
		    logger.finest("discoverTimelinessParams","Received a notInTimeWindow report. Discovery suceeded.");
	    }
	    else
		throw new SnmpStatusException("An error occurred when processing Engine discovery. Not received the right report : " + o + ". Expected notInTimeWindow [1.3.6.1.6.3.15.1.1.2.0]");
	}
	
	if(logger.finestOn()) {
	    logger.finest("discoverTimelinessParams","Result :\n" +
		  "Engine nb boots : " +  getEngineBoots() + "\n" +
		  "Engine time : " + getEngineTime());
	}
    }

    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpUsmPeer");
    
    String dbgTag = "SnmpUsmPeer"; 
    
}
