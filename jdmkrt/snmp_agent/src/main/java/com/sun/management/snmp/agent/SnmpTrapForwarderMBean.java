/*
 * @(#)file      SnmpTrapForwarderMBean.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.17
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

/**
 * This MBean interface allows the remote manageability of the trap 
 * forwarding feature. 
 *
 *
 * @since Java DMK 5.1
 */
public interface SnmpTrapForwarderMBean { 
     /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @return True removed, false target not present.
     */
    public boolean removeV1Target(String address);

    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @return True removed, false target not present.
     */
    public boolean removeV2Target(String address);
    
    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @return True removed, false target not present.
     */
    public boolean removeV3Target(String address);
    
    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @param port The port to which the trap is sent.
     * @return True removed, false target not present.
     */
    public boolean removeV1Target(String address, int port);
    
    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @param port The port to which the trap is sent.
     * @return True removed, false target not present.
     */
    public boolean removeV2Target(String address, int port);
    
    /** Remove the first occurrence of the passed target.
     * @param address The target address (IP or name).
     * @param port The port to which the trap is sent.
     * @return True removed, false target not present.
     */
    public boolean removeV3Target(String address, int port);

    /**
     * Add a target to which SNMP V1 traps will be forwarded.
     * WARNING: When specifying a port to send trap, the whole 
     * <CODE>SnmpV3AdaptorServer</CODE> is impacted. The port changing is 
     * effective when sending the trap. Concurrent calls to 
     * <CODE>SnmpV3AdaptorServer</CODE> trap API can lead to unpredictable 
     * behavior.
     * <P> The same target can be added multiple times. </P>
     * @param address The target address (IP or name);
     * @param port The port to which the trap is sent. If port == -1, the
     * adaptor port is used.
     * @param communityString The associated community string.
     *
     * @throws IllegalArgumentException If the the port is == 0 or 
     * the address is invalid (cause field set to 
     * <CODE>java.net.UnknownHostException</CODE> if JDK Version >= 1.4). 
     */
    public void addV1Target(String address,
			    int port,
			    String communityString);
    
    /**
     * Add a target to which SNMP V2 traps will be forwarded.
     * WARNING: When specifying a port to send trap, the whole 
     * <CODE>SnmpV3AdaptorServer</CODE> is impacted. The port changing is 
     * effective when sending the trap. Concurrent calls to 
     * <CODE>SnmpV3AdaptorServer</CODE> trap API can lead to unpredictable
     * behavior.
     * <P> The same target can be added multiple times. </P>
     * @param address The target address (IP or name);
     * @param port The port to which the trap is sent. If port == -1, the
     * adaptor port is used.
     * @param communityString The associated community string.
     *
     * @throws IllegalArgumentException If the the port is == 0 or 
     * the address is invalid (cause field set to 
     * <CODE>java.net.UnknownHostException</CODE> if JDK Version >= 1.4). 
     */
    public void addV2Target(String address,
			    int port,
			    String communityString);
    
   /**
     * Add a target to which SNMP V3 traps will be forwarded. 
     * WARNING: When specifying a port to send trap, the whole 
     * <CODE>SnmpV3AdaptorServer</CODE> is impacted. The port changing is 
     * effective when sending the trap. Concurrent calls to 
     * <CODE>SnmpV3AdaptorServer</CODE> trap API can lead to unpredictable 
     * behavior.
     * <P> The same target can be added multiple times. </P>
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
    public void addV3Target(String address,
			    int port,
			    String principal,
			    String contextName,
			    int securityLevel);
    
    /**
     * Add a target to which SNMP V1 traps will be forwarded.
     * <P> The same target can be added multiple times. </P>
     * @param address The target address (IP or name);
     * @param communityString The associated community string.
     *
     * @throws IllegalArgumentException If the address is invalid 
     * (cause field set to <CODE>java.net.UnknownHostException</CODE> 
     * if JDK Version >= 1.4). 
     */
    public void addV1Target(String address,
			    String communityString);
    
    /**
     * Add a target to which SNMP V2 traps will be forwarded.
     * <P> The same target can be added multiple times. </P>
     * @param address The target address (IP or name);
     * @param communityString The associated community string.
     *
     * @throws IllegalArgumentException If the address is invalid 
     * (cause field set to <CODE>java.net.UnknownHostException</CODE> 
     * if JDK Version >= 1.4). 
     */
    public void addV2Target(String address,
			    String communityString);
    
   /**
     * Add a target to which SNMP V3 traps will be forwarded. 
     * <P> The same target can be added multiple times. </P>
     * @param address The target address (IP or name);
     * @param principal The user.
     * @param contextName The context name.
     * 
     * @throws IllegalArgumentException If the principal is null, 
     * if the security level is not valid or if the address is invalid
     * (cause field set to <CODE>java.net.UnknownHostException</CODE> 
     * if JDK Version >= 1.4).
     */
    public void addV3Target(String address,
			    String principal,
			    String contextName,
			    int securityLevel);

    /**
     * Returns true if SNMP V1 forwarding is activated.
     */
    public boolean isV1Activated();
    /**
     * Returns true if SNMP V2 forwarding is activated.
     */
    public boolean isV2Activated();
    /**
     * Returns true if SNMP V3 forwarding is activated.
     */
    public boolean isV3Activated();
    
    /**
     * Activate or deactivate SNMP V1 trap forwarding. By default is false. 
     * <P> If V1 target addresses have been provided, you don't need to 
     * activate V1. If V1 is activated and no targets are provided, 
     * localhost and(or) IPAcl are used as the default targets.</P>
     * @param status True activates it, false deactivates it.
     */
    public void snmpV1forwarding(boolean status);

    /**
     * Activate or deactivate SNMP V2 trap forwarding. By default is false. 
     * <P> If V2 target addresses have been provided, you don't need to
     * activate V2. If V2 is activated and no targets are provided, localhost
     * and(or) IPAcl are used as the default targets.</P>
     * @param status True activates it, false deactivates it.
     */
    public void snmpV2forwarding(boolean status);
    
   /**
     * Activate or deactivate SNMP V3 trap forwarding. By default is false. 
     * <P> If V3 target addresses have been provided, you don't need to 
     * activate V3. If V3 is activated and no targets are provided, 
     * localhost and(or) IPAcl are used as the default targets.</P>
     * @param status True activates it, false deactivates it.
     */
    public void snmpV3forwarding(boolean status);

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
    public void setInetAddressAclUsed(boolean use);
    
    /**
     * Says if The <CODE>SnmpTrapForwarder</CODE> parses or not the 
     * <CODE>InetAddressAcl</CODE> when forwarding traps.
     * @return true the file is parsed, false the file is not parsed.
     */
    public boolean isInetAddressAclUsed();
}
