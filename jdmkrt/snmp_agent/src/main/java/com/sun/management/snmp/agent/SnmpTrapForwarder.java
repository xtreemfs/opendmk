/*
 * @(#)file      SnmpTrapForwarder.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.38
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
import java.net.SocketException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;
// RI import
//
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.manager.SnmpTrapListener;
import com.sun.management.snmp.manager.SnmpEventReportDispatcher;
import com.sun.management.snmp.SnmpPduRequest;
import com.sun.management.snmp.SnmpPduTrap;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpScopedPduRequest;
import com.sun.management.snmp.SnmpPdu;
// jdmk import
//
import com.sun.management.snmp.agent.SnmpTrap;
import com.sun.management.comm.SnmpV3AdaptorServer;
import com.sun.management.snmp.SnmpEngine;
import com.sun.jdmk.internal.ClassLogger;
import com.sun.jdmk.defaults.Utils;

class SnmpTarget {
    InetAddress address = null;
    Integer port = null;
    SnmpTarget(String address, int port) {
	if(port == 0)
	    throw new IllegalArgumentException("Port can't be == 0");
	
	this.port = new Integer(port);
	
	try { 
	    this.address = InetAddress.getByName(address);
	}catch(UnknownHostException e) {
	    throw (IllegalArgumentException) 
		Utils.initCause(new IllegalArgumentException(e.toString()), 
				e);
	}
    }
    public boolean equals(Object a) {
	if(!(a instanceof SnmpTarget) ) return false;
	SnmpTarget t = (SnmpTarget) a;
	if(!address.equals(t.address)) return false;
	if(port != t.port) return false;
	return true;
    }
}

class SnmpV1V2Target extends SnmpTarget {
    String community = null;
    SnmpV1V2Target(String address, int port, String community) {
	super(address, port);
	this.community = community;
    }
}

class SnmpV3Target extends SnmpTarget {
    String principal = null;
    String contextName = null;
    int msgFlags = 0;
    SnmpV3Target(String address, 
		 int port, 
		 String principal, 
		 String contextName,
		 int securityLevel) {
	super(address, port);
	if(principal == null)
	    throw new IllegalArgumentException("Principal can't be null");
	this.principal = principal;
	this.contextName = contextName;
	if(securityLevel != SnmpDefinitions.noAuthNoPriv &&
	   securityLevel != SnmpDefinitions.authNoPriv &&
	   securityLevel != SnmpDefinitions.authPriv) 
	    throw new IllegalArgumentException("Invalid security level [" +
					       securityLevel + "]");
	this.msgFlags = securityLevel;
    }
    
}


/**
 * This MBean allows you to receive traps of subagents and forward them to 
 * managers.
 * <p> This trap forwarder listen on a dedicated port and forward traps 
 * according to its configuration.
 * <p> If you activate V1, V2 and V3 protocols, every received trap 
 * (in V1 / V2 or V3) will be forwarded in all SNMP protocols. It can be 
 * useful when you don't know which protocol the managers are using.
 * <p> You can pass specific manager addresses when forwarding for a 
 * specific protocol. By default the <CODE>InetAddressAcl</CODE> is also
 * parsed. So the set of actual manager addresses is the trap blocks located
 * in the acl file and the set of added targets. You can disable the use of 
 * <CODE>InetAddressAcl</CODE> by calling the method 
 * <CODE>{@link #setInetAddressAclUsed setInetAddressAclUsed(false)}</CODE>.
 * <P> To enable trap forwarding you must start the forwarder.
 * <P> WARNING : In case you have some manager addresses in your set of 
 * targets that are also present in InetAddressAcl (or no InetAddressAcl 
 * activated but a target equals to localhost), your manager will receive 
 * the trap twice. In order to protect yourself against this behavior, 
 * configure carefully the <CODE>SnmpTrapForwarder</CODE>. You can, for 
 * example, disable the InetAddressAcl parsing by calling 
 * <CODE>{@link #setInetAddressAclUsed setInetAddressAclUsed(false)}</CODE>.
 *
 * @since Java DMK 5.1
 */
public class SnmpTrapForwarder extends SnmpTrapReceiver 
    implements SnmpTrapForwarderMBean {
    
    // Will be used to forward traps.
    private SnmpV3AdaptorServer server = null;
    private boolean forwardV1 = false;
    private boolean forwardV2 = false;
    private boolean forwardV3 = false;
    private Vector v1targets = null;
    private Vector v2targets = null;
    private Vector v3targets = null;

    private boolean originator = true;
    private InetAddress addr = null;
    private String communityString = null;
    private String principal = null;
    private int msgFlags = 0;
    private String contextName = null;
    private boolean usm = false;
    private boolean useAcl = true;
    /**
     * Instantiate a <CODE> SnmpTrapForwarder</CODE> that will forward 
     * received traps according to its configuration.
     * @param server The current adaptor.
     * @param port The port the forwarder is listening to.
     */
    public SnmpTrapForwarder(SnmpV3AdaptorServer server, 
			     int port) {
	super(server.getEngine(), port, null);
	this.server = server;
	receiveAsGeneric(true);
    }
    
    /**
     * Instantiate a <CODE> SnmpTrapForwarder</CODE> that will 
     * forward received traps according to its configuration.
     * @param server The current adaptor.
     * @param port The port the forwarder is listening to.
     * @param address The address used to listen for incoming traps.
     */
    public SnmpTrapForwarder(SnmpV3AdaptorServer server, 
			     int port,
			     InetAddress address) {
	super(server.getEngine(), port, address);
	this.server = server;
	receiveAsGeneric(true);
    }
    
    /**
     * When forwarding in SNMP V1 and or V2,
     * if useInetAddressAcl is activated and no ACL file or 
     * no destinations are available, the trap is sent to the local host 
     * using this specified community string.
     *
     * @param cs The default community string to use.
     */
    public void setCommunityString(String cs) {
	communityString = cs;
    }

    /**
     * When forwarding in Snmp V3, the default principal, context and 
     * security level to use. These parameters are used for the targets
     * specified in the InetAddressAcl.
     * @param principal Will be used by Usm in order to find security 
     *     parameters.
     * @param contextName The forwarded trap contextName.
     * @param securityLevel The message security level.
     */
  public void setV3Parameters(String principal,
			      String contextName,
			      int  securityLevel) {
	usm = true;
	this.principal = principal;
	this.contextName = contextName;
	this.msgFlags = securityLevel;
    }
    
    /**
     * By default if an <CODE>InetAddressAcl</CODE> file is loaded by the 
     * SNMP adaptor, it will be parsed in order to find manager IP addresses.
     * This parsing is done when forwarding in SNMP V1 V2 and V3.  In the 
     * case of SNMP V3 forwarding, the parsed targets are used only if the 
     * SNMP V3 parameters have been provided (via setV3Parameters method).
     * <P> If you provided some targets (addV1/2/3Target methods) they are 
     * also used when forwarding traps. The set of targets is 
     * <CODE>InetAddressAcl</CODE> trap blocks + added targets.
     * If you don't use <CODE>InetAddressAcl</CODE> (passing false to this 
     * method), only the set of added targets is used.
     * @param use The use of <CODE> InetAddressAcl</CODE>
     */
    public void setInetAddressAclUsed(boolean use) {
	useAcl = use;
    }
    
    /**
     * Says if The <CODE>SnmpTrapForwarder</CODE> parses or not the 
     * <CODE>InetAddressAcl</CODE> when forwarding traps.
     * @return true the file is parsed, false the file is not parsed.
     */
    public boolean isInetAddressAclUsed() {
	return useAcl;
    }
    /**
     * Return true if Snmp V1 forwarding is activated.
     */
    public synchronized boolean isV1Activated() {
	return forwardV1;
    }
    /**
     * Return true if Snmp V2 forwarding is activated.
     */
    public synchronized boolean isV2Activated() {
	return forwardV2;
    }
    /**
     * Return true if Snmp V3 forwarding is activated.
     */
    public synchronized boolean isV3Activated() {
	return forwardV3;
    }

    /**
     * Activate or deactivate SNMP V1 trap forwarding without providing 
     * targets (using the method <CODE>addV1Target</CODE>). InetAddressAcl 
     * or localhost are used as the default targets.
     * @param status True activate it, false deactivate it.
     */
    public synchronized void snmpV1forwarding(boolean status) {
	forwardV1 = status;
    }
    
    /**
     * Activate or deactivate SNMP V2 trap forwarding without providing 
     * targets (using the method <CODE>addV2Target</CODE>). InetAddressAcl 
     * or localhost are used as the default targets.
     * @param status True activate it, false deactivate it.
     */
    public synchronized void snmpV2forwarding(boolean status) {
	forwardV2 = status;
    }
    
   /**
    * Activate or deactivate SNMP V3 trap forwarding without providing 
    * targets (using the method <CODE>addV3Target</CODE>). InetAddressAcl 
    * or localhost are used as the default targets.
    * @param status True activate it, false deactivate it.
    */
    public synchronized void snmpV3forwarding(boolean status) {
	forwardV3 = status;
    }

    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @return True removed, false target not present.
     */
    public boolean removeV1Target(String address) {
	return removeV1Target(address, -1);
    }
    
    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @return True removed, false target not present.
     */
    public boolean removeV2Target(String address) {
	return removeV2Target(address, -1);
    }
    
    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @return True removed, false target not present.
     */
    public boolean removeV3Target(String address) {
	return removeV3Target(address, -1);
    }
    
    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @param port The port to which the trap is sent.
     * @return True removed, false target not present.
     */
    public boolean removeV1Target(String address, int port) {
	SnmpTarget t = new SnmpTarget(address, port);
	if(v1targets != null)
	    return v1targets.remove(t);
	else
	    return false;
    }
    
    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @param port The port to which the trap is sent.
     * @return True removed, false target not present.
     */
    public boolean removeV2Target(String address, int port) {
	SnmpTarget t = new SnmpTarget(address, port);
	if(v2targets != null)
	    return v2targets.remove(t);
	else
	    return false;
    }
    
    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @param port The port to which the trap is sent.
     * @return True removed, false target not present.
     */
    public boolean removeV3Target(String address, int port) {
	SnmpTarget t = new SnmpTarget(address, port);
	if(v3targets != null)
	    return v3targets.remove(t);
	else
	    return false;
    }
    /**
     * Add a target to which SNMP V1 traps will be forwarded to.
     * WARNING: When specifying a port to send trap, the whole 
     * <CODE>SnmpV3AdaptorServer</CODE> is impacted. The port changing is 
     * effective when sending the trap. Concurrent call to 
     * <CODE>SnmpV3AdaptorServer</CODE> trap API can lead to unpredictable 
     * behavior.
     * <P>The same target can be added multiple time.</P>
     * @param address The target address (IP or name);
     * @param port The port to which the trap is sent. If port == -1, the
     * adaptor port is used.
     * @param communityString The associated community string.
     *
     * @throws IllegalArgumentException If the the port is == 0 or 
     * the address is invalid (cause field set to 
     * <CODE>java.net.UnknownHostException</CODE> if JDK Version >= 1.4). 
     */
    public synchronized void addV1Target(String address,
					 int port,
					 String communityString) {
	if(address == null) return;
	if(v1targets == null) v1targets = new Vector();
	snmpV1forwarding(true);
	SnmpV1V2Target t = new SnmpV1V2Target(address, port, communityString);
	v1targets.add(t);
    }
    
    /**
     * Add a target to which SNMP V2 traps will be forwarded to.
     * WARNING: When specifying a port to send trap, the whole 
     * <CODE>SnmpV3AdaptorServer</CODE> is impacted. The port changing is 
     * effective when sending the trap. Concurrent call to 
     * <CODE>SnmpV3AdaptorServer</CODE> trap API can lead to unpredictable 
     * behavior.
     * <P> The same target can be added multiple time. </P>
     * @param address The target address (IP or name);
     * @param port The port to which the trap is sent. If port == -1, the
     * adaptor port is used.
     * @param communityString The associated community string.
     *
     * @throws IllegalArgumentException If the the port is == 0 or 
     * the address is invalid (cause field set to 
     * <CODE>java.net.UnknownHostException</CODE> if JDK Version >= 1.4). 
     */
    public synchronized void addV2Target(String address,
					 int port,
					 String communityString) {
	if(address == null) return;
	if(v2targets == null) v2targets = new Vector();
	snmpV2forwarding(true);
	SnmpV1V2Target t = new SnmpV1V2Target(address, port, communityString);
	v2targets.add(t);
    }
    
   /**
     * Add a target to which SNMP V3 traps will be forwarded to. 
     * WARNING: When specifying a port to send trap, the whole 
     * <CODE>SnmpV3AdaptorServer</CODE> is impacted. The port changing is 
     * effective when sending the trap. Concurrent call to 
     * <CODE>SnmpV3AdaptorServer</CODE> trap API can lead to unpredictable 
     * behavior.
     * <P> The same target can be added multiple time. </P>
     * @param address The target address (IP or name);
     * @param port The port to which the trap is sent. If port == -1, the
     * adaptor port is used.
     * @param principal The user.
     * @param contextName The context name.
     * @param securityLevel The security level.
     *
     * @throws IllegalArgumentException If the the port is == 0, 
     * if the principal is null, if the security level is not valid or if  
     * the address is invalid (cause field set to 
     * <CODE>java.net.UnknownHostException</CODE> if JDK Version >= 1.4). 
     */
    public synchronized void addV3Target(String address,
					 int port,
					 String principal,
					 String contextName,
					 int securityLevel) {
	if(address == null) return;
	if(v3targets == null) v3targets = new Vector();
	snmpV3forwarding(true);
	SnmpV3Target t = new SnmpV3Target(address, 
					  port, 
					  principal, 
					  contextName, 
					  securityLevel);
	v3targets.add(t);
    }
    
    /**
     * Add a target to which SNMP V1 traps will be forwarded to. The port 
     * used is the <CODE>SnmpV3AdaptorServer</CODE> one.
     * <P> The same target can be added multiple time. </P>
     * @param address The target address (IP or name);
     * @param communityString The associated community string.
     *
     * @throws IllegalArgumentException If the address is invalid 
     * (cause field set to <CODE>java.net.UnknownHostException</CODE> 
     * if JDK Version >= 1.4). 
     */
    public synchronized void addV1Target(String address,
					 String communityString) {
	addV1Target(address, -1, communityString);
    }
    
    /**
     * Add a target to which SNMP V2 traps will be forwarded to. 
     * The port used is the <CODE>SnmpV3AdaptorServer</CODE> one.
     * <P> The same target can be added multiple time. </P>
     * @param address The target address (IP or name);
     * @param communityString The associated community string.
     *
     * @throws IllegalArgumentException If the address is invalid 
     * (cause field set to <CODE>java.net.UnknownHostException</CODE> 
     * if JDK Version >= 1.4).  
     */
    public synchronized void addV2Target(String address,
					 String communityString) {
	addV2Target(address, -1, communityString);
    }
    
    /**
     * By default a trap is sent as a notification originator.
     * The difference between acting as a notification originator or a proxy
     * when forwarding the trap is detailed in rfc 2576 section 3.1 and 3.2.
     * 
     */
    public void forwardAsNotificationOriginator() {
	originator = true;
    }
    
    /** 
     * By default a trap is sent as a notification originator.
     * The difference between acting as a notification originator or a proxy
     * when forwarding the trap is detailed in rfc 2576 section 3.1 and 3.2. 
     */
    public void forwardAsProxy() {
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
     * When a trap is received, localhost is set as the source of the 
     * forwarded trap. In case you want a specific address, call this method
     * providing it an ip address. If <CODE>isProxyWay()</CODE> returns 
     * true, the source address is the received one.
     * @param addr The source address to use when forwarding the trap.
     */
    public void setSourceIpAddress(InetAddress addr) {
	this.addr = addr;
    }

   /**
     * Add a target to which SNMP V3 traps will be forwarded to. 
     * The port used is the <CODE>SnmpV3AdaptorServer</CODE> one.
     * <P> The same target can be added multiple time. </P>
     * @param address The target address (IP or name);
     * @param principal The user.
     * @param contextName The context name.
     * @param securityLevel The security level.
     * 
     * @throws IllegalArgumentException If the principal is null, 
     * if the security level is not valid or if the address is invalid
     * (cause field set to <CODE>java.net.UnknownHostException</CODE> 
     * if JDK Version >= 1.4).
     */
    public synchronized void addV3Target(String address,
					 String principal,
					 String contextName,
					 int securityLevel) {
	addV3Target(address, -1, principal, contextName, securityLevel);
    }

    

    protected synchronized void receivedTrap(SnmpTrap trap) {
	try {
	    if(isOriginatorWay()) {
		trap.setPduSourceAddress(addr);
		trap.sendAsNotificationOriginator();
	    } else
		trap.sendAsProxy();
	    
	    if(isV1Activated()) {
		//Forward using IpAcl.
		if(isInetAddressAclUsed()) {
		    if(logger.finestOn())
			logger.finest("receivedTrap", 
			   " Sending SNMP V1 trap using InetAddressAcl conf");
		    try {
			trap.sendV1(server); 
		    }catch(Exception e) {
			if(logger.finestOn())
			    logger.finest("receivedTrap", 
					  "Fail sending trap using acl");
		    }
		}
		trap.setDestinationAddress(null);
		if(v1targets != null) {
		    for(Enumeration e = v1targets.elements() ; 
			e.hasMoreElements() ;) {
			SnmpV1V2Target t = null;
			try {
			    t = (SnmpV1V2Target) e.nextElement();
			    trap.setCommunityString(t.community);
			    trap.setDestinationAddress(t.address);
			    if(logger.finestOn()){
				logger.finest("receivedTrap", 
				      "Sending SNMP V1 trap to : " + 
				      t.address + " on port : " + 
				      t.port.intValue());
			    }
			    
			    if(t.port.intValue() != -1)
				trap.setDestinationPort(t.port.intValue());
			    
			    trap.sendV1(server);
			}catch(Exception ex) {
			    if(logger.finestOn())
				logger.finest("receivedTrap", 
				      "Fail sending trap to target : " + 
				      t.address +"/"+t.port.intValue());
			}
		    }	
		}
	    }
	    
	    if(isV2Activated()) {
		//Forward using IpAcl.
		if(isInetAddressAclUsed()) {
		    if(logger.finestOn())
			logger.finest("receivedTrap", 
			    "Sending SNMP V2 trap using InetAddressAcl conf");
		    trap.sendV2(server);
		}
		trap.setDestinationAddress(null);
		if(v2targets != null) {
		    for(Enumeration e = v2targets.elements() ; 
			e.hasMoreElements() ;) {
			SnmpV1V2Target t = null;
			try {
			    t = (SnmpV1V2Target) e.nextElement();
			    trap.setCommunityString(t.community);
			    trap.setDestinationAddress(t.address);
			    if(logger.finestOn()){
				logger.finest("receivedTrap", 
				      "Sending SNMP V2 trap to : " + 
				      t.address + " on port : " + 
				      t.port.intValue());
			    }
			    
			    if(t.port.intValue() != -1)
				trap.setDestinationPort(t.port.intValue());
			    
			    trap.sendV2(server);
			}catch(Exception ex) {
			    if(logger.finestOn())
				logger.finest("receivedTrap", 
				      "Fail sending trap to target : " + 
				      t.address +"/"+t.port.intValue());
			}
		    }	
		}
	    }
	    
	    if(isV3Activated()) {
		if(isInetAddressAclUsed())
		    if(principal != null) {
			if(logger.finestOn())
			    logger.finest("receivedTrap", 
			    "Sending SNMP V3 trap using InetAddressAcl conf");
			trap.sendV3Usm(server, 
				       principal, 
				       (byte)msgFlags, 
				       contextName);
		    }
		trap.setDestinationAddress(null);
		if(v3targets != null) {
		    for(Enumeration e = v3targets.elements() ; 
			e.hasMoreElements() ;) {
			SnmpV3Target t = null;
			try {
			    t = (SnmpV3Target) e.nextElement();
			    trap.setDestinationAddress(t.address);
			    if(logger.finestOn()){
				logger.finest("receivedTrap", 
				      "Sending SNMP V3 trap to : " + 
				      t.address + " on port : " + 
				      t.port.intValue());
			    }
			    
			    if(t.port.intValue() != -1)
				trap.setDestinationPort(t.port.intValue());
			    
			    trap.sendV3Usm(server, 
					   t.principal, 
					   (byte)t.msgFlags, 
					   t.contextName);
			}catch(Exception ex) {
			    if(logger.finestOn())
				logger.finest("receivedTrap", 
				      "Fail sending trap to target : " + 
				      t.address +"/"+t.port.intValue());
			}
		    }
		}
	    }
	} catch(IOException e) {
	    if(logger.finerOn()){
		logger.finer("receivedTrap", e.toString());
	    }
	} catch(SnmpStatusException e) {
	    if(logger.finerOn()){
		logger.finer("receivedTrap", e.toString());
	    }
	}
    }

    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_PROXY_SNMP, "SnmpTrapForwarder");

    String dbgTag = "SnmpTrapForwarder";
}
