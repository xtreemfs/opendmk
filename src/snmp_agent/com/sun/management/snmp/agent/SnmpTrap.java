/*
 * @(#)file      SnmpTrap.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.45
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
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.IOException;

// RI import
//
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpTimeticks;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpPduRequest;
import com.sun.management.snmp.SnmpPduTrap;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpString;
import com.sun.management.snmp.SnmpIpAddress;
import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpCounter64;
import com.sun.management.snmp.SnmpScopedPduRequest;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpUnknownModelException;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpUnknownModelException;
import com.sun.management.snmp.manager.SnmpPeer;
import com.sun.management.snmp.manager.SnmpParameters;
import com.sun.management.snmp.manager.usm.SnmpUsmPeer;
import com.sun.management.snmp.manager.usm.SnmpUsmParameters;

// jdmk import
import com.sun.jdmk.internal.ClassLogger;
import com.sun.management.comm.SnmpAdaptorServer;
import com.sun.management.comm.SnmpV3AdaptorServer;
import com.sun.management.snmp.usm.SnmpUsm;
import com.sun.management.snmp.usm.SnmpUsmSecurityParameters;

import com.sun.management.internal.snmp.SnmpEngineImpl;

/**
 * This class represents an SNMP trap. It is not protocol-dependent. 
 * Once created, any SNMP trap can be sent in either SNMP V1, V2 or V3.
 * SNMP traps use a {@link com.sun.management.comm.SnmpAdaptorServer} to send, 
 * which you must provide. ({@link com.sun.management.comm.SnmpAdaptorServer} 
 * implements the notification dispatching interface).
 * <P> An instance of SnmpTrap can be reused multiple time with the same 
 * parameters.</P>
 *
 * @since Java DMK 5.1
 */

public class SnmpTrap implements Serializable
{
    private static final long serialVersionUID = 4545122574911175203L;
    /**
     * coldStart Oid as defined in RFC 1907.
     */
    public static final SnmpOid coldStartOid = new 
        SnmpOid("1.3.6.1.6.3.1.1.5.1");

    /**
     * warmStart Oid as defined in RFC 1907.
     */
    public static final SnmpOid warmStartOid = new 
        SnmpOid("1.3.6.1.6.3.1.1.5.2");

    /**
     * linkDown Oid as defined in RFC 1907.
     */
    public static final SnmpOid linkDownOid = new 
        SnmpOid("1.3.6.1.6.3.1.1.5.3");
    
     /**
     * linkUp Oid as defined in RFC 1907.
     */
    public static final SnmpOid linkUpOid = new 
        SnmpOid("1.3.6.1.6.3.1.1.5.4");

     /**
     * authenticationFailure Oid as defined in RFC 1907.
     */
    public static final SnmpOid authenticationFailureOid = new 
        SnmpOid("1.3.6.1.6.3.1.1.5.5");

     /**
     * egpNeighborLoss Oid as defined in RFC 1907.
     */
    public static final SnmpOid egpNeighborLossOid = new 
        SnmpOid("1.3.6.1.6.3.1.1.5.6");

    /**
     * Well known Oid radical as defined in RFC 1907.
     */
    private static final SnmpOid snmpTrapRadicalOid = new 
        SnmpOid("1.3.6.1.6.3.1.1.5");

    /**
     * snmpSysUpTime Oid as defined in RFC 1907.
     */
    public static final SnmpOid snmpTrapSysUpTimeOid = new 
        SnmpOid("1.3.6.1.2.1.1.3.0");

    /**
     * snmpTrapOid Oid as defined in RFC 1907.
     */
    public static final SnmpOid snmpTrapOid = new 
        SnmpOid("1.3.6.1.6.3.1.1.4.1.0");

    /**
     * snmpTrapAddress Oid as defined in RFC 2576.
     */
    public static final SnmpOid snmpTrapAddressOid = new 
        SnmpOid("1.3.6.1.6.3.18.1.3.0");

    /**
     * snmpTrapEnterprise Oid as defined in RFC 1907.
     */
    public static final SnmpOid snmpTrapEnterpriseOid = new 
        SnmpOid("1.3.6.1.6.3.1.1.4.3.0");

    /**
     * snmpTraps Oid as defined in RFC 1907.
     */
    public static final SnmpOid snmpTrapsOid = new 
        SnmpOid("1.3.6.1.6.3.1.1.5");

    /**
     * snmpTrapCommunity Oid as defined in RFC 2576.
     */
    public static final SnmpOid snmpTrapCommunityOid = new 
        SnmpOid("1.3.6.1.6.3.18.1.4.0");

    String dbgTag = "SnmpTrap";
    InetAddress address = null;
    String communityString = null;
    //SnmpVarBindList varBindList = null;
    SnmpOid trapOid = null;
    Integer gen = null;
    Integer specific = null;
    SnmpAdaptorServer server = null;
    SnmpOid enterpriseOid = null;
    SnmpPduTrap pduTrap = null;
    SnmpPduRequest pduRequest = null;
    SnmpVarBindList pduTrapList = null;
    int bufferSize = 1024;
    int port = -1;
    SnmpScopedPduRequest scopedTrap = null;
    
    private boolean originator = true;
    private InetAddress pduAddr = null;
    
    // CONSTRUCTORS
    //===============
    //
    /**
     * Initializes this SNMP trap with SNMP V1 style parameters 
     *
     * @param gen The generic number of the trap.
     * @param specific The specific number of the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE>.
     */
    public SnmpTrap(int gen,
                    int specific,
                    SnmpVarBindList varBindList) {
        this.gen = new Integer(gen);
        this.specific = new Integer(specific);
        initialize(null, null, varBindList);
    }

    /**
     * Initializes this SNMP trap with SNMP V1 style parameters.
     *
     * @param addr The <CODE>InetAddress</CODE> destination of the trap.
     * @param cs The community string to be used for the trap.
     * @param gen The generic number of the trap.
     * @param specific The specific number of the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE>.
     */

    public SnmpTrap(InetAddress addr,
                    String cs,
                    int gen,
                    int specific,
                    SnmpVarBindList varBindList) {
        this.gen = new Integer(gen);
        this.specific = new Integer(specific);
        initialize(addr, cs, varBindList);
    }
    
    /**
     * Initializes this SNMP trap with SNMP V2 style parameters.
     *
     * @param trapOid The trap oid.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE>.
     */

    public SnmpTrap(SnmpOid trapOid,
                    SnmpVarBindList varBindList) {
        this.trapOid = trapOid;
        initialize(null, null, varBindList);
    }

    /**
     * Initializes this SNMP trap with SNMP V2 style parameters.
     *
     * @param addr The <CODE>InetAddress</CODE> destination of the trap.
     * @param cs The community string to be used for the trap.
     * @param trapOid The trap oid.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE>.
     */

    public SnmpTrap(InetAddress addr,
                    String cs,
                    SnmpOid trapOid,
                    SnmpVarBindList varBindList) {
        this.trapOid = trapOid;
        initialize(addr, cs, varBindList);
    }
    
    /**
     * Initializes this SNMP trap with an SNMP V1 trap PDU. Trap PDUs are 
     * received by {@link com.sun.management.snmp.manager.SnmpTrapListener}.
     *
     * @param trap The SNMP V1 trap pdu.
     */
    public SnmpTrap(SnmpPduTrap trap) {
        pduTrap = trap;
        convertPduList(trap.varBindList);
	
    }

    /**
     * Initializes this SNMP trap with an SNMP V3 scoped PDU. Trap PDUs are
     * received by {@link com.sun.management.snmp.manager.SnmpTrapListener}.
     * 
     * @param scopedTrap The SNMP V3 trap pdu.
     */
    public SnmpTrap(SnmpScopedPduRequest scopedTrap) {
        this.scopedTrap = scopedTrap;
        convertPduList(scopedTrap.varBindList);
    }
    
    /**
     * Initializes this SNMP trap with an SNMP request PDU. SNMP request 
     * PDUs are received by {@link 
     * com.sun.management.snmp.manager.SnmpTrapListener}.
     *
     * @param trap The SNMP request pdu.
     */

    public SnmpTrap(SnmpPduRequest trap) {
        pduRequest = trap;
        convertPduList(trap.varBindList);
    }
    
    /**
     * If the trap has been created with a PDU (SNMP V1 / V2 or
     * V3), the source address located in this PDU is returned.  Null
     * is returned if no source address can be defined.
     * @return The trap source address or null if no source address defined
     */
    public InetAddress getSourceAddress() {
	if(scopedTrap != null) return scopedTrap.address;
	if(pduRequest != null) return pduRequest.address;
	if(pduTrap != null) return pduTrap.address;
	return null;
    }

    /**
     * If the trap has been created with a PDU (SNMP V1 / V2 or V3), the 
     * received PDU is returned. Otherwise null is returned. The returned 
     * <CODE>SnmpPdu</CODE> can be of the class : <CODE>SnmpPduRequest</CODE>,
     * <CODE>SnmpScopedPduRequest</CODE> or <CODE>SnmpPduTrap</CODE>.
     * @return the PDU or null.
     */
    public SnmpPdu getPdu() {
	if(scopedTrap != null) return scopedTrap;
	if(pduRequest != null) return pduRequest;
	if(pduTrap != null) return pduTrap;
	return null;
    }
    
    /**
     * Sets the enterprise OID. It will be used when sending the trap.
     *
     * @param oid The OID in string format "x.x.x.x".
     *
     * @exception IllegalArgumentException The string format is incorrect
     */

    public void setEnterpriseOid(String oid) {
        enterpriseOid = new SnmpOid(oid);
    }
    
    /**
     * Sets the trap destination address. It will be used when sending the 
     * trap.
     *
     * @param address The trap destination address.
     *
     */

    public void setDestinationAddress(InetAddress address) {
        this.address = address;
    }
    
    /**
     * Sets the trap destination port. It will be used when sending the trap.
     * If no port is provided, the SNMP adaptor's port is used.
     *
     * @param port The trap destination port.
     *
     */
    
    public void setDestinationPort(int port) {
        this.port = port;
    }

    /**
     * Sets the community string. It will be used when sending the trap.
     *
     * @param cs The community string to use.
     *
     */
    
    public void setCommunityString(String cs) {
        this.communityString = cs;
    }

    /**
     * Send as a V3 trap. The passed SNMP adaptor is used by the trap to send
     * itself. This method can only be called if the current 
     * <CODE>SnmpTrap</CODE> has been instantiated with the constructor 
     * {@link com.sun.management.snmp.agent.SnmpTrap#SnmpTrap(SnmpScopedPduRequest) }
     * @param server The SNMP V3 adaptor to use.
     */
    public void sendV3(SnmpV3AdaptorServer server) 
	throws IOException, SnmpStatusException {
	if(scopedTrap == null)
	    throw new SnmpStatusException(
                  "No scoped trap. Should use sendV3Usm method");
	if(port != -1) {
	    SnmpUsmPeer peer = null;
	    if(getAddress() != null) {
		try {
		    peer = new SnmpUsmPeer(server.getEngine(),
					   getAddress().getHostAddress(),
					   port,
					   null);
		}catch(SnmpUnknownModelException e) {
		    throw new SnmpStatusException("Unknown model: "+e);
		}
		server.snmpV3Trap(peer, scopedTrap);
		return;
	    }
	}
	
	server.snmpV3Trap(getAddress(), scopedTrap);
    }

    /**
     * Send as a V3 trap. The passed SNMP adaptor is used by the trap to 
     * send itself. USM is used in order to secure the trap.
     * @param server The SNMP V3 adaptor to use.
     * @param principal The user name to use.
     * @param securityLevel The V3 message flags.
     * @param contextName The V3 context name. If null is provided and if 
     *   a community string has been provided, it will be reused as the 
     *   contextName. The <CODE>contextEngineId</CODE> is the local engine Id.
     */
    public void sendV3Usm(SnmpV3AdaptorServer server,
			  String principal,
			  int securityLevel,
			  String contextName) 
        throws IOException, SnmpStatusException {
	int sendPort;
        if (logger.finestOn()) {
            logger.finest("sendV3Trap", "trapOid= " + getTrapOid());
        }
	
	if(port == -1)
	    sendPort = server.getTrapPort().intValue();
	else
	    sendPort = port;
	
	SnmpEngineImpl engine = (SnmpEngineImpl) server.getEngine();
	// Create an authoritative peer.
	SnmpUsmPeer peer = null;
	SnmpUsmParameters p = null;
	if(getAddress() != null) {
	    if (logger.finestOn())
		logger.finest("sendV3Trap", 
		      "Create Peer " + getAddress() +"/" + sendPort);
	    try {
		peer = 
		    new SnmpUsmPeer(engine,
				    getAddress().getHostAddress(), 
				    sendPort, null);
		p = new SnmpUsmParameters(engine);
	    }catch(SnmpUnknownModelException e) {
		throw new SnmpStatusException("Unknown model: "+e);
	    }
	    p.setPrincipal(principal);
	    p.setSecurityLevel(securityLevel);
	    byte[] ctx = getContext(contextName);
	    if(ctx != null)
		p.setContextName(ctx);
	    
	    peer.setParams(p);
	}
	
	// If we can't define the time stamp, 
	// must let the adaptor calculate one.
	if(peer != null) {
	    server.snmpV3UsmTrap(peer,
				 getTrapOid(),
				 getVarBindList(SnmpDefinitions.snmpVersionThree),
				  getTimeStamp());
	}else {
	    byte[] ctx = getContext(contextName);
	    server.snmpV3UsmTrap(getAddress(),
				 principal,
				 securityLevel,
				 ctx == null ? null : new String(ctx),
				 getTrapOid(),
				 getVarBindList(SnmpDefinitions.snmpVersionThree),
				  getTimeStamp());
	    
	}
    }
    
    /**
     * Send as a V2 trap. The passed SNMP adaptor is used by the trap to send 
     * itself.
     *
     * @param server The SNMP adaptor to use.
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit 
     *    defined by <CODE>bufferSize</CODE>.
     */
    public void sendV2(SnmpAdaptorServer server) 
        throws IOException, SnmpStatusException {
	int sendPort;
        if (logger.finestOn()) {
            logger.finest("sendV2Trap", "trapOid= " + getTrapOid());
        }
	
	if(port == -1)
	    sendPort = server.getTrapPort().intValue();
	else
	    sendPort = port;
	
	SnmpPeer peer = null;
	SnmpParameters p = null;
	if(getAddress() != null) {
	    if(logger.finestOn())
		logger.finest("sendV2Trap", 
			      "Create Peer " + getAddress() +"/" + sendPort);
	    
	    peer = new SnmpPeer(getAddress().getHostAddress(), sendPort);
	    p = new SnmpParameters();
	    p.setRdCommunity(getCommunity());
	    peer.setParams(p);
	}
	
        //Define every parameters and send the pdu
	// If we can't define the time stamp, 
	// must let the adaptor calculate one.
	if (logger.finestOn())
	    logger.finest("sendV2Trap", "pduRequest is not provided");
	
	if(peer == null) {
	    server.snmpV2Trap(getAddress(),
			      getCommunity(),
			      getTrapOid(),
			      getVarBindList(SnmpDefinitions.snmpVersionTwo),
			      getTimeStamp());
	}
	else {
	    server.snmpV2Trap(peer,
			      getTrapOid(),
			      getVarBindList(SnmpDefinitions.snmpVersionTwo),
			      getTimeStamp());
	    
	}
    }
    
    /**
     * Send this trap. The passed SNMP adaptor is used by the trap to send 
     * itself.
     *
     * @param server The SNMP adaptor to use.
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *     by <CODE>bufferSize</CODE>.
     */
    public void sendV1(SnmpAdaptorServer server) 
        throws IOException, SnmpStatusException {
	int sendPort;
        if (logger.finestOn()) {
            logger.finest("snmpV1Trap", "generic = " + getGeneric());
        }
	
	if(port == -1)
	    sendPort = server.getTrapPort().intValue();
	else
	    sendPort = port;
	
	SnmpPeer peer = null;
	SnmpParameters p = null;
	final int version = SnmpDefinitions.snmpVersionOne;
	
	if(getAddress() != null) {
	    if (logger.finestOn())
		logger.finest("sendV1Trap", 
		      "Create Peer " + getAddress() +"/" + sendPort);
	    peer = new SnmpPeer(getAddress().getHostAddress(), sendPort);
	    p = new SnmpParameters();
	    p.setRdCommunity(getCommunity());
	    peer.setParams(p);
	}

	if(peer == null)
	    server.snmpV1Trap(getAddress(),
			      getAgentAddr(),
			      getCommunity(),
			      getEnterpriseOid(),
			      getGeneric(),
			      getSpecific(),
			      getVarBindList(SnmpDefinitions.snmpVersionOne),
			      getTimeStamp());
	else
	    server.snmpV1Trap(peer,
			      getAgentAddr(),
			      getEnterpriseOid(),
			      getGeneric(),
			      getSpecific(),
			      getVarBindList(SnmpDefinitions.snmpVersionOne),
			      getTimeStamp());
    }
    
    /**
     * Define which trapOid to use.
     */
    private SnmpOid getTrapOid() { 
        if(trapOid != null)  //Has been provided
            return trapOid;
        else
            if(pduRequest != null ||
	       scopedTrap != null) { //Use it from PDU Request one.
                SnmpVarBindList l = pduTrapList;
                int pos = l.indexOfOid(snmpTrapOid);
                SnmpVarBind var = l.getVarBindAt(pos);
                return (SnmpOid) var.getSnmpValue();
            }
            else
                return calculateTrapOid(); // Must calculate it.
    }
    
    /**
     * Define the generic value to use. Only in V1.
     */
    private int getGeneric() {
        if(gen != null)  //Has been provided
            return gen.intValue();
        else
            if(pduTrap != null) //Use it from PDU Trap one.
                return pduTrap.genericTrap;
            else
                return calculateGeneric(); // Must calculate it.
    }
    
    /**
     * Define the specific value to use.  Only in V1.
     */
    private int getSpecific() {
        if(specific != null)  //Has been provided
            return specific.intValue();
        else
            if(pduTrap != null) //Use it from PDU Trap one.
                return pduTrap.specificTrap;
            else 
                return calculateSpecific(); // Must calculate it.
    }
    
    /**
     * Define the destination address to use.
     */
    private InetAddress getAddress() {  
        // The destination address is the one passed at construct time.
        // Can't be calculated at this level.
        return address; 
    }
    
    /**
     * Define the community string to use.
     */
    private String getCommunity() { 
        // The community string is the one passed at construct time.
	if(communityString != null)
	    return communityString;
	//V1 PDU provided
	if(pduTrap != null)
	    if(pduTrap.community != null)
		return new String(pduTrap.community);
	//V2 PDU provided
	if(pduRequest != null)
	    if(pduRequest.community != null)
		return new String(pduRequest.community);
	//V3 PDU provided
	if(scopedTrap != null)
	    if(scopedTrap.contextName != null)
		return new String(scopedTrap.contextName);
	//No community string set, return null;
	return null;
    }
    
    private byte[] getContext(String contextName) {
	if(contextName != null) 
	return contextName.getBytes();
	//V1 PDU provided
	if(pduTrap != null)
	return pduTrap.community;

	//V2 PDU provided
	if(pduRequest != null)
	return pduRequest.community;
	   
	//V3 PDU provided
	if(scopedTrap != null)
	return scopedTrap.contextName;
	
	return null;
    }

    /**
     * Define the enterprise oid to use.
     */
    
    private SnmpOid getEnterpriseOid() {
        if(enterpriseOid != null) //Has been provided
            return enterpriseOid;
        else
            if(pduTrap != null) //Use it from PDU Trap one.
                return pduTrap.enterprise;
            else
                return calculateEnterpriseOid(); // Must calculate it.
    }
    
    /**
     * Define the VarBindList to use according to the protocol to use 
     * when sending the trap.
     */
    private SnmpVarBindList getVarBindList(int vers) 
	throws SnmpStatusException {
        //if(pduTrapList != null) //Has been provided
	//  return pduTrapList;
        //else
	if(logger.finestOn())
	    logger.finest("getVarBindList", 
			  "Calculate varbindlist for version : " + vers);
	switch(vers) {
	case SnmpDefinitions.snmpVersionThree:
	case SnmpDefinitions.snmpVersionTwo:
	    return calculateV2VarBindList();
	case SnmpDefinitions.snmpVersionOne:
	    return calculateV1VarBindList();
	default:
	    throw new SnmpStatusException("getVarBindList: " +
					  "Unable to define varBindList.");
	}
    }

    private SnmpVarBindList getFullV2VarBindList() {
	SnmpVarBindList lst = calculateV2VarBindList();
	int found = lst.indexOfOid(snmpTrapOid);
        if(found == -1) {
	    if(logger.finestOn())
		logger.finest("getFullV2VarBindList", "Must add trapOid : " + 
		      getTrapOid());
            lst.insertElementAt(new SnmpVarBind(snmpTrapOid,
						getTrapOid()), 0);
        }
	found = lst.indexOfOid(snmpTrapSysUpTimeOid);
	if(found == -1) {
	    if(logger.finestOn())
		logger.finest("getFullV2VarBindList","Must add timeStamp : "+ 
		      getTimeStamp());
	    lst.insertElementAt(new SnmpVarBind(snmpTrapSysUpTimeOid,
						getTimeStamp()), 0);
	}
	if(logger.finestOn())
	    logger.finest("getFullV2VarBindList", "List updated");
	return lst;
    }

    /**
     * Define the time stamp to use.
     */
    private SnmpTimeticks getTimeStamp() {
        if(pduTrap != null)
            return new SnmpTimeticks(pduTrap.timeStamp);//Has been provided 
        else {
            if(pduRequest != null || 
	       scopedTrap != null) {
                int pos = pduTrapList.indexOfOid(snmpTrapSysUpTimeOid);
                SnmpVarBind var = pduTrapList.getVarBindAt(pos);
                return (SnmpTimeticks) var.getSnmpValue(); //Use it from PDU Trap one.
            }
            else
                return null; //No time stamps. The dispatcher will use one.
        }
    }
    

    /**
     * By default a trap is sent as a notification originator.
     * The difference between acting as a notification originator or a proxy
     * when forwarding the trap is detailed in rfc 2576 section 3.1 and 3.2. 
     */
    public void sendAsNotificationOriginator() {
	originator = true;
    }
    
    /**
     * By default a trap is sent as a notification originator.
     * The difference between acting as a notification originator or a proxy
     * when forwarding the trap is detailed in rfc 2576 section 3.1 and 3.2. 
     */
    public void sendAsProxy() {
	originator = false;
    }
    
    /**
     * By default a trap is sent as a notification originator.
     * The difference between acting as a notification originator or a proxy
     * when forwarding the trap is detailed in rfc 2576 section 3.1 and 3.2. 
     */
    public boolean isOriginatorWay() {
	return originator;
    }
    
    /**
     * By default a trap is sent as a notification originator.
     * The difference between acting as a notification originator or a proxy
     * when forwarding the trap is detailed in rfc 2576 section 3.1 and 3.2. 
     */
    public boolean isProxyWay() {
	return !isOriginatorWay();
    }
    
    /**
     * When a trap is received, localhost is set as the source of the sent 
     * trap. In case you want a specific address, call this method providing
     * it an IP address. If <CODE>isProxyWay</CODE> returns true, the source 
     * address is the one received.
     * @param addr The source address to use when forwarding the trap.
     */
    public void setPduSourceAddress(InetAddress addr) {
	this.pduAddr = addr;
    }
    
    private SnmpIpAddress handleMultipleIpVersion(byte[] address) {
	if(address.length == 4)
	    return new SnmpIpAddress(address);
	else {
	    if(logger.finestOn())
		logger.finest("handleMultipleIPVersion", 
			      "Not an IPv4 address, return null");
	    return null;
	}
    }
    
    /**
     * Define the agent that sent the trap (Trap forwarding context only).
     */
    private SnmpIpAddress getAgentAddr() {
	if(isOriginatorWay()) {
	    //A source address has been providedd. Just reuse it.
	    if(pduAddr != null)
		return handleMultipleIpVersion(pduAddr.getAddress());
	    
	    if(logger.finestOn())
		logger.finest("getAgentAddr", "Originator way, let the SnmpAdaptor" +
		      " handling the source address");
	    
	    //Let the SnmpAdaptorServer compute it.
	    return null;
	}
	
	// The proxy way.
        if(pduTrap != null) //Use it from PDU Trap one. 
            return pduTrap.agentAddr;
        else
            if(pduRequest != null || 
	       scopedTrap != null) { //Use it from PDU Trap one.
                int pos = pduTrapList.indexOfOid(snmpTrapAddressOid);
                if(pos != -1) { //The VarBind is present
                    SnmpVarBind var = pduTrapList.getVarBindAt(pos);
                    return (SnmpIpAddress) var.getSnmpValue();
                }
                else //The VarBind is NOT present
                    return new SnmpIpAddress("0.0.0.0");
            }
            else
                return null; //No agent address. It is the local one.
    }

    // The real V1<==> V2 translation rules.
    // See "Coexistence between SNMP versions" document for translation 
    // explanation. 
    //
    private int calculateGeneric() {
        SnmpOid oid = null;
	
        if (trapOid != null)
            oid = trapOid;
        else {
            SnmpVarBindList l = pduTrapList;
            int pos = l.indexOfOid(snmpTrapOid);
            SnmpVarBind var = l.getVarBindAt(pos);
            oid = (SnmpOid) var.getSnmpValue();
        }
	
        if(oid.equals(coldStartOid)) 
            return SnmpDefinitions.trapColdStart;
        if(oid.equals(warmStartOid)) 
            return SnmpDefinitions.trapWarmStart;
        if(oid.equals(linkDownOid))
            return SnmpDefinitions.trapLinkDown;
        if(oid.equals(linkUpOid))
            return SnmpDefinitions.trapLinkUp;
        if(oid.equals(authenticationFailureOid))
            return SnmpDefinitions.trapAuthenticationFailure;
        if(oid.equals(egpNeighborLossOid))
            return SnmpDefinitions.trapEgpNeighborLoss;
	
        return SnmpDefinitions.trapEnterpriseSpecific;
    }

    private int calculateSpecific() {
        SnmpOid oid = null;
	
        if (trapOid != null)
            oid = trapOid;
        else {
            SnmpVarBindList l = pduTrapList;
            int pos = l.indexOfOid(snmpTrapOid);
            SnmpVarBind var = l.getVarBindAt(pos);
            oid = (SnmpOid) var.getSnmpValue();
        }
	
        if(getGeneric() == SnmpDefinitions.trapEnterpriseSpecific) {
            int val = 0;
            try{
                val = (int) oid.getOidArc(oid.getLength() - 1); 
            }catch(SnmpStatusException e){
                // What can we do??????
            }
            return val; 
        }
        else
            return 0;
    }
    
    private SnmpOid calculateTrapOid() {
        SnmpOid trap = null;
        int g = 0;
        int spec = 0;

        if(pduTrap != null) {
            g = pduTrap.genericTrap;
            spec = pduTrap.specificTrap;
        }
        else {
            g = gen.intValue();
            spec = specific.intValue();
        }

        switch(g) {
        case SnmpDefinitions.trapColdStart:
        case SnmpDefinitions.trapWarmStart:
        case SnmpDefinitions.trapLinkDown:
        case SnmpDefinitions.trapLinkUp:
        case SnmpDefinitions.trapAuthenticationFailure:
        case SnmpDefinitions.trapEgpNeighborLoss:
            trap = new SnmpOid(snmpTrapRadicalOid.longValue());
            trap.append(g + 1);
            break;
        default: //SnmpDefinitions.trapEntrepriseSpecific
            if(enterpriseOid != null)
                trap = new SnmpOid(snmpTrapEnterpriseOid.longValue());
            else
                trap = new SnmpOid(pduTrap.enterprise.longValue());

            trap.append(0);
            trap.append(spec);  
        }
        return trap;
    }

    private SnmpOid calculateEnterpriseOid() {
        int pos = pduTrapList.indexOfOid(snmpTrapOid);
	if(pos == -1) return null;
        SnmpVarBind var = pduTrapList.getVarBindAt(pos);
        SnmpOid trapOid = (SnmpOid) var.getSnmpValue();

        if(isStandardTrap(trapOid)) {
            int p = pduTrapList.indexOfOid(snmpTrapEnterpriseOid);
            if(p != -1) {
                SnmpVarBind v = pduTrapList.getVarBindAt(p);
                return (SnmpOid) v.getSnmpValue();
            }
            else
                return snmpTrapsOid;
        }
        else {
            long []tabOid = trapOid.longValue();
            long key = tabOid[tabOid.length -2];
            int limit = 0;
	    
            if(key == 0)
                limit = 2;
            else
                limit = 1;
	    
            long []target = new long[tabOid.length - limit];
            for(int i = 0; i < target.length; i++)
                target[i] = tabOid[i];
	    
            return new SnmpOid(target);
        }
    }

    private static boolean isStandardTrap(SnmpOid oid) {
        if(oid.equals(coldStartOid) ||
           oid.equals(warmStartOid) ||
	   oid.equals(linkUpOid) ||
           oid.equals(linkDownOid) ||
           oid.equals(authenticationFailureOid) ||
           oid.equals(egpNeighborLossOid) ) 
            return true;
        else
            return false;
    }

    private SnmpVarBindList calculateV1VarBindList() 
        throws SnmpStatusException {
        SnmpVarBindList list = new SnmpVarBindList();
	
        for(int i = 0; i < pduTrapList.size(); i++) {
            
	    SnmpVarBind var = pduTrapList.getVarBindAt(i);
            //if( (var.oid.equals(snmpTrapSysUpTimeOid)) ||
	    //  (var.oid.equals(snmpTrapOid)) ||
	    //  (var.oid.equals(snmpTrapAddressOid)) )
	    //  continue;

            if(var.getSnmpValue() instanceof SnmpCounter64)
                throw new SnmpStatusException(SnmpStatusException.noSuchName, 
                                              i);
	    
            list.addVarBind(var);
        }
        return list;
    }

    private SnmpVarBindList calculateV2VarBindList() {
	//The trap was instantiated with a V2 or V3 list. No translation. 
	//Just reuse it.
	if(pduTrap == null) return pduTrapList;
	
	if(isOriginatorWay()) return pduTrapList;

	// The proxy way.
        SnmpVarBindList list = new SnmpVarBindList(pduTrapList);
        boolean toadd = false;
	
        int found = pduTrapList.indexOfOid(snmpTrapAddressOid);
        if(found == -1) {
            toadd = true;
        }
        found = pduTrapList.indexOfOid(snmpTrapCommunityOid);
        if(found == -1) { 
            toadd = true;
        }
        found = pduTrapList.indexOfOid(snmpTrapEnterpriseOid);
        if(found == -1) {
            toadd = true;
        }
	
        if(toadd) {
	    if(logger.finestOn())
		logger.finest("calculateV2VarBindList", "Must add varbind");
            list.addVarBind( new SnmpVarBind(snmpTrapAddressOid,
                                             pduTrap.agentAddr));
            SnmpString co = new SnmpString(pduTrap.community);
            list.addVarBind(new SnmpVarBind(snmpTrapCommunityOid, co));
            list.addVarBind(new SnmpVarBind(snmpTrapEnterpriseOid,
                                            pduTrap.enterprise));
        }
        return list;
    }

    //Much easyer to use SnmpVarBindList than SnmpVarBind[]
    private void convertPduList(SnmpVarBind[] vars) {
        pduTrapList = new SnmpVarBindList();
        for(int i = 0; i < vars.length; i++)
            pduTrapList.addVarBind(vars[i]);
    }
    
    private SnmpVarBind[] toVarBindArray(SnmpVarBindList l) {
	SnmpVarBind[] arr = new SnmpVarBind[l.size()];
	for(int i = 0; i < l.size(); i++) 
	arr[i] = l.getVarBindAt(i);
	return arr;
    }
    /**
     * Called by constructors in order to initialize trap fields.
     */

    private void initialize(InetAddress address,
                            String cs,
                            SnmpVarBindList varBindList) {
        this.address = address;
        this.communityString = cs;
        this.pduTrapList = varBindList;
    }

    
    // TRACES & DEBUG
    //---------------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpTrap");

}
