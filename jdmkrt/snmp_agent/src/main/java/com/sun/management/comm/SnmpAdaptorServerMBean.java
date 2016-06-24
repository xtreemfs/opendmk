/*
 * @(#)file      SnmpAdaptorServerMBean.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.50
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
import java.util.Vector;
import java.io.IOException;
import java.net.InetAddress;

// jmx imports
//
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpTimeticks;
import com.sun.management.snmp.SnmpIpAddress;
import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.InetAddressAcl;
import com.sun.management.snmp.manager.SnmpPeer;

// jdmk imports
//
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpMibHandler;
import com.sun.management.snmp.agent.SnmpUserDataFactory;

import com.sun.jdmk.comm.CommunicatorServerMBean;

/**
 * Exposes the remote management interface of the {@link SnmpAdaptorServer} MBean.
 *
 * @since Java DMK 5.1
 */

public interface SnmpAdaptorServerMBean extends CommunicatorServerMBean {

    // GETTERS AND SETTERS
    //--------------------
    
    /**
     * Returns the Ip address based ACL used by this SNMP protocol adaptor.
     * @return The <CODE>InetAddressAcl</CODE> implementation.
     *
     */
    public InetAddressAcl getInetAddressAcl();
    /**
     * Returns the port used by this SNMP protocol adaptor for sending traps.
     * By default, port 162 is used.
     * 
     * @return The port number for sending SNMP traps.
     */
    public Integer getTrapPort();
  
    /**
     * Sets the port used by this SNMP protocol adaptor for sending traps.
     *
     * @param port The port number for sending SNMP traps.
     */
    public void setTrapPort(Integer port);
  
    /**
     * Returns the port used by this SNMP protocol adaptor for sending inform requests.
     * By default, port 162 is used.
     * 
     * @return The port number for sending SNMP inform requests.
     */
    public int getInformPort();
  
    /**
     * Sets the port used by this SNMP protocol adaptor for sending inform requests.
     *
     * @param port The port number for sending SNMP inform requests.
     */
    public void setInformPort(int port);
    
    /**
     * Gets the number of managers that have been processed by this SNMP protocol adaptor 
     * since its creation.
     *
     * @return The number of managers handled by this SNMP protocol adaptor
     * since its creation. This counter is not reset by the <CODE>stop</CODE> method.
     */
    public int getServedClientCount();
    
    /**
     * Gets the number of managers currently being processed by this 
     * SNMP protocol adaptor.
     *
     * @return The number of managers currently being processed by this 
     * SNMP protocol adaptor.
     */
    public int getActiveClientCount();
    
    /**
     * Gets the maximum number of managers that this SNMP protocol adaptor can 
     * process concurrently.
     *
     * @return The maximum number of managers that this SNMP protocol adaptor can 
     * process concurrently.
     */
    public int getMaxActiveClientCount();

    /**
     * Sets the maximum number of managers this SNMP protocol adaptor can 
     * process concurrently.
     *
     * @param c The number of managers.
     *
     * @exception java.lang.IllegalStateException This method has been invoked
     * while the communicator was <CODE>ONLINE</CODE> or <CODE>STARTING</CODE>.
     */
    public void setMaxActiveClientCount(int c) throws java.lang.IllegalStateException;
    
    /**
     * Returns the protocol of this SNMP protocol adaptor.
     *
     * @return The string "snmp".
     */
    public String getProtocol();
    
    /**
     * Returns the buffer size of this SNMP protocol adaptor.
     * By default, buffer size 1024 is used.
     *
     * @return The buffer size.
     */
    public Integer getBufferSize();

    /**
     * Sets the buffer size of this SNMP protocol adaptor.
     *
     * @param s The buffer size.
     *
     * @exception java.lang.IllegalStateException This method has been invoked
     * while the communicator was <CODE>ONLINE</CODE> or <CODE>STARTING</CODE>.
     */
    public void setBufferSize(Integer s) throws java.lang.IllegalStateException;
  
    /**
     * Gets the number of times to try sending an inform request before giving up.
     * @return The maximum number of tries.
     */
    public int getMaxTries();
    
    /**
     * Changes the maximum number of times to try sending an inform request before giving up.
     * @param newMaxTries The maximum number of tries.
     */
    public void setMaxTries(int newMaxTries);
    
    /**
     * Gets the timeout to wait for an inform response from the manager.
     * @return The value of the timeout property.
     */
    public int getTimeout();
    
    /**
     * Changes the timeout to wait for an inform response from the manager.
     * @param newTimeout The timeout (in milliseconds).
     */
    public void setTimeout(int newTimeout);
    
    /**
     * Returns the message factory of this SNMP protocol adaptor.
     *
     * @return The factory object.
     */
    public SnmpPduFactory getPduFactory();
    
    /**
     * Sets the message factory of this SNMP protocol adaptor.
     *
     * @param factory The factory object (null means the default factory).
     */
    public void setPduFactory(SnmpPduFactory factory);


    /**
     * Set the user-data factory of this SNMP protocol adaptor.
     *
     * @param factory The factory object (null means no factory).
     * @see com.sun.management.snmp.agent.SnmpUserDataFactory
     */
    public void setUserDataFactory(SnmpUserDataFactory factory);
  
    /**
     * Get the user-data factory associated with this SNMP protocol adaptor.
     *
     * @return The factory object (null means no factory).
     * @see com.sun.management.snmp.agent.SnmpUserDataFactory
     */
    public SnmpUserDataFactory getUserDataFactory();

    /**
     * Returns <CODE>true</CODE> if authentication traps are enabled.
     * <P>
     * When this feature is enabled, the SNMP protocol adaptor sends 
     * an <CODE>authenticationFailure</CODE> trap each time an authentication fails.
     * <P>
     * The default behavior is to send authentication traps.
     * 
     * @return <CODE>true</CODE> if authentication traps are enabled, <CODE>false</CODE> otherwise.
     */
    public boolean getAuthTrapEnabled();
  
    /**
     * Sets the flag indicating if traps need to be sent in case of authentication failure.
     * 
     * @param enabled Flag indicating if traps need to be sent.
     */
    public void setAuthTrapEnabled(boolean enabled);

    /**
     * Returns <code>true</code> if this SNMP protocol adaptor sends a response in case
     * of authentication failure.
     * <P>
     * When this feature is enabled, the SNMP protocol adaptor sends a response with <CODE>noSuchName</CODE>
     * or <CODE>readOnly</CODE> when the authentication failed. If the flag is disabled, the
     * SNMP protocol adaptor trashes the PDU silently.
     * <P>
     * The default behavior is to send responses.
     * 
     * @return <code>true</code> if responses are sent.
     */
    public boolean getAuthRespEnabled();

    /**
     * Sets the flag indicating if responses need to be sent in case of authentication failure.
     * 
     * @param enabled Flag indicating if responses need to be sent.
     */
    public void setAuthRespEnabled(boolean enabled);
    
    /**
     * Returns the enterprise OID. It is used by {@link #snmpV1Trap snmpV1Trap} to fill
     * the 'enterprise' field of the trap request.
     * 
     * @return The OID in string format "x.x.x.x".
     */
    public String getEnterpriseOid();

    /**
     * Sets the enterprise OID.
     *
     * @param oid The OID in string format "x.x.x.x".
     *
     * @exception IllegalArgumentException The string format is incorrect
     */
    public void setEnterpriseOid(String oid) throws IllegalArgumentException;
    
    /**
     * Returns the names of the MIBs available in this SNMP protocol adaptor.
     * 
     * @return An array of MIB names.
     */
    public String[] getMibs();
    
    // GETTERS FOR SNMP GROUP (MIBII)
    //-------------------------------
    
    /**
     * Returns the <CODE>snmpOutTraps</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutTraps</CODE> value.
     */
    public Long getSnmpOutTraps();
  
    /**
     * Returns the <CODE>snmpOutGetResponses</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutGetResponses</CODE> value.
     */
    public Long getSnmpOutGetResponses();
  
    /**
     * Returns the <CODE>snmpOutGenErrs</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutGenErrs</CODE> value.
     */
    public Long getSnmpOutGenErrs();
  
    /**
     * Returns the <CODE>snmpOutBadValues</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutBadValues</CODE> value.
     */
    public Long getSnmpOutBadValues();
  
    /**
     * Returns the <CODE>snmpOutNoSuchNames</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutNoSuchNames</CODE> value.
     */
    public Long getSnmpOutNoSuchNames();
  
    /**
     * Returns the <CODE>snmpOutTooBigs</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutTooBigs</CODE> value.
     */
    public Long getSnmpOutTooBigs();
  
    /**
     * Returns the <CODE>snmpInASNParseErrs</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInASNParseErrs</CODE> value.
     */
    public Long getSnmpInASNParseErrs();
  
    /**
     * Returns the <CODE>snmpInBadCommunityUses</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInBadCommunityUses</CODE> value.
     */
    public Long getSnmpInBadCommunityUses();
  
    /**
     * Returns the <CODE>snmpInBadCommunityNames</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInBadCommunityNames</CODE> value.
     */
    public Long getSnmpInBadCommunityNames();
  
    /**
     * Returns the <CODE>snmpInBadVersions</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInBadVersions</CODE> value.
     */
    public Long getSnmpInBadVersions();
  
    /**
     * Returns the <CODE>snmpOutPkts</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutPkts</CODE> value.
     */
    public Long getSnmpOutPkts();
  
    /**
     * Returns the <CODE>snmpInPkts</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInPkts</CODE> value.
     */
    public Long getSnmpInPkts();
  
    /**
     * Returns the <CODE>snmpInGetRequests</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInGetRequests</CODE> value.
     */
    public Long getSnmpInGetRequests();
  
    /**
     * Returns the <CODE>snmpInGetNexts</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInGetNexts</CODE> value.
     */
    public Long getSnmpInGetNexts();
  
    /**
     * Returns the <CODE>snmpInSetRequests</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInSetRequests</CODE> value.
     */
    public Long getSnmpInSetRequests();
  
    /**
     * Returns the <CODE>snmpInTotalSetVars</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInTotalSetVars</CODE> value.
     */
    public Long getSnmpInTotalSetVars();
  
    /**
     * Returns the <CODE>snmpInTotalReqVars</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInTotalReqVars</CODE> value.
     */
    public Long getSnmpInTotalReqVars();

    /**
     * Returns the <CODE>snmpSilentDrops</CODE> value defined in rfc 1907 NMPv2-MIB .
     * 
     * @return The <CODE>snmpSilentDrops</CODE> value.
     *
     */
    public Long getSnmpSilentDrops();
    
    /**
     * Returns the <CODE>snmpProxyDrops</CODE> value defined in rfc 1907 NMPv2-MIB .
     * 
     * @return The <CODE>snmpProxyDrops</CODE> value.
     *
     */
    public Long getSnmpProxyDrops();
    
    // PUBLIC METHODS
    //---------------
    
    /**
     * Adds a new MIB in the SNMP MIB handler. 
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler)}  
     * and {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName)}
     * and should not be called directly from remote.
     * 
     * @param mib The MIB to add.
     * 
     * @return A reference to the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib) throws IllegalArgumentException;
  
    /**
     * Adds a new MIB in the SNMP MIB handler. 
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler, SnmpOid[])}  
     * and {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName, SnmpOid[])} and should not be called directly from remote.
     *
     * @param mib The MIB to add.
     * @param oids The set of OIDs this agent implements.
     * If null or oids.length == 0, this is equivalent to calling
     * <CODE>addMib(SnmpMibAgent mib)</CODE>
     * @return A reference to the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     *
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, SnmpOid[] oids) throws IllegalArgumentException;

    /**
     * Adds a new contextualized MIB in the SNMP MIB handler.
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler, String)}  
     * and {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName, String)} and should not be called directly from remote.
     * When the underlying implementation does not support MIB scoping,
     * calling this method is equivalent to calling
     * <CODE>addMib(SnmpMibAgent mib)</CODE>.
     * <p>Note that MIB scoping is only supported by the {@link
     * SnmpV3AdaptorServer}.
     *
     * @param mib The MIB to add. 
     * @param contextName The MIB context name. If null or 
     contextName.length == 0, will be registered in the default context.
     * @return A reference on the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     *
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, String contextName) 
	throws IllegalArgumentException;

    /**
     * Removes the specified MIB from the SNMP protocol adaptor. 
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler)}  
     * and {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName)}
     * and should not be called directly from remote.
     * 
     * @param mib The MIB to be removed.
     *
     * @return <code>true</code> if the specified <CODE>mib</CODE> was a MIB included in the SNMP MIB handler, 
     * <code>false</code> otherwise.
     */
    public boolean removeMib(SnmpMibAgent mib);
    
    /**
     * Sends a trap using SNMP V1 trap format.
     * <BR>The trap is sent to each destination defined in the ACL file (if available).
     * If no ACL file or no destinations are available, the trap is sent to the local host.
     * 
     * @param generic The generic number of the trap.
     * @param specific The specific number of the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public void snmpV1Trap(int generic, int specific, SnmpVarBindList varBindList) throws IOException, SnmpStatusException;
    
    
    /**
     * Sends a trap using SNMP V1 trap format.
     * <BR>The trap is sent to the specified <CODE>InetAddress</CODE> destination
     * using the specified community string (and the ACL file is not used).
     * 
     * @param address The <CODE>InetAddress</CODE> destination of the trap.
     * @param cs The community string to be used for the trap.
     * @param generic The generic number of the trap.
     * @param specific The specific number of the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public void snmpV1Trap(InetAddress address, String cs, int generic, int specific, SnmpVarBindList varBindList) 
        throws IOException, SnmpStatusException;
    
    
    /**
     * Sends a trap using SNMP V1 trap format.
     * <BR>The trap is sent to the specified <CODE>SnmpPeer</CODE> destination.
     * The community string used is the one located in the <CODE>SnmpPeer</CODE> parameters (<CODE>SnmpParameters.getRdCommunity() </CODE>).
     * 
     * @param peer The <CODE>SnmpPeer</CODE> destination of the trap.
     * @param agentAddr The agent address to be used for the trap.
     * @param enterpOid The enterprise OID to be used for the trap.
     * @param generic The generic number of the trap.
     * @param specific The specific number of the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @param time The time stamp (overwrite the current time).
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     *
     */
    public void snmpV1Trap(SnmpPeer peer,
			   SnmpIpAddress agentAddr,
			   SnmpOid enterpOid,
			   int generic,
			   int specific,
			   SnmpVarBindList varBindList,
			   SnmpTimeticks time) throws IOException, SnmpStatusException;
    
    /**
     * Sends a trap using SNMP V2 trap format.
     * <BR>The trap is sent to the specified <CODE>SnmpPeer</CODE> destination.
     * <BR>The community string used is the one located in the <CODE>SnmpPeer</CODE> parameters (<CODE>SnmpParameters.getRdCommunity() </CODE>).
     * <BR>The variable list included in the outgoing trap is composed of the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with the value specified by <CODE>time</CODE>
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by <CODE>trapOid</CODE>
     * <LI><CODE>all the (oid,values)</CODE> from the specified <CODE>varBindList</CODE>
     * </UL>
     * 
     * @param peer The <CODE>SnmpPeer</CODE> destination of the trap.
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @param time The time stamp (overwrite the current time).
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     *
     */
    public void snmpV2Trap(SnmpPeer peer,
			   SnmpOid trapOid,
			   SnmpVarBindList varBindList,
			   SnmpTimeticks time) throws IOException, SnmpStatusException;

    /**
     * Sends a trap using SNMP V2 trap format.
     * <BR>The trap is sent to each destination defined in the ACL file (if available).
     * If no ACL file or no destinations are available, the trap is sent to the local host.
     * <BR>The variable list included in the outgoing trap is composed of the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by <CODE>trapOid</CODE>
     * <LI><CODE>all the (oid,values)</CODE> from the specified <CODE>varBindList</CODE>
     * </UL>
     * 
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public void snmpV2Trap(SnmpOid trapOid, SnmpVarBindList varBindList) throws IOException, SnmpStatusException;
    
    
    /**
     * Sends a trap using SNMP V2 trap format.
     * <BR>The trap is sent to the specified <CODE>InetAddress</CODE> destination
     * using the specified community string (and the ACL file is not used).
     * <BR>The variable list included in the outgoing trap is composed of the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by <CODE>trapOid</CODE>
     * <LI><CODE>all the (oid,values)</CODE> from the specified <CODE>varBindList</CODE>
     * </UL>
     * 
     * @param address The <CODE>InetAddress</CODE> destination of the trap.
     * @param cs The community string to be used for the trap.
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public void snmpV2Trap(InetAddress address, String cs, SnmpOid trapOid, SnmpVarBindList varBindList) 
        throws IOException, SnmpStatusException;

    /**
     * Send the specified trap PDU to the passed <CODE>InetAddress</CODE>.
     * @param address The destination address.
     * @param pdu The pdu to send.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     *
     */
    public void snmpPduTrap(InetAddress address, SnmpPduPacket pdu) 
        throws IOException, SnmpStatusException;
    /**
     * Send the specified trap PDU to the passed <CODE>SnmpPeer</CODE>.
     * @param peer The destination peer. The Read community string is used of <CODE>SnmpParameters</CODE> is used as the trap community string.
     * @param pdu The pdu to send.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public void snmpPduTrap(SnmpPeer peer, 
			    SnmpPduPacket pdu) 
        throws IOException, SnmpStatusException;
	       
    /**
     * Sends an inform using SNMP V2 inform request format.
     * <BR>The inform request is sent to each destination defined in the ACL file (if available).
     * If no ACL file or no destinations are available, the inform request is sent to the local host.
     * <BR>The variable list included in the outgoing inform request is composed of the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by <CODE>trapOid</CODE>
     * <LI><CODE>all the (oid,values)</CODE> from the specified <CODE>varBindList</CODE>
     * </UL>
     * To send an inform request, the SNMP adaptor server must be active.
     * 
     * @param cb The callback that is invoked when a request is complete.
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @return A vector of {@link com.sun.management.comm.SnmpInformRequest} objects.
     * <P>If there is no destination host for this inform request, the returned vector will be empty.
     *
     * @exception IllegalStateException  This method has been invoked while the SNMP adaptor server was not active.
     * @exception IOException An I/O error occurred while sending the inform request.
     * @exception SnmpStatusException If the inform request exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public Vector snmpInformRequest(SnmpInformHandler cb, SnmpOid trapOid, SnmpVarBindList varBindList) 
        throws IllegalStateException, IOException, SnmpStatusException;
        
    /**
     * Sends an inform using SNMP V2 inform request format.
     * <BR>The inform is sent to the specified <CODE>InetAddress</CODE> destination
     * using the specified community string.
     * <BR>The variable list included in the outgoing inform request is composed of the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by <CODE>trapOid</CODE>
     * <LI><CODE>all the (oid,values)</CODE> from the specified <CODE>varBindList</CODE>
     * </UL>
     * To send an inform request, the SNMP adaptor server must be active.
     * 
     * @param address The <CODE>InetAddress</CODE> destination for this inform request.
     * @param cs The community string to be used for the inform request.
     * @param cb The callback that is invoked when a request is complete.
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @return The inform request object.
     *
     * @exception IllegalStateException  This method has been invoked while the SNMP adaptor server was not active.
     * @exception IOException An I/O error occurred while sending the inform request.
     * @exception SnmpStatusException If the inform request exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public SnmpInformRequest snmpInformRequest(InetAddress address, String cs, SnmpInformHandler cb, 
                                               SnmpOid trapOid, SnmpVarBindList varBindList) 
        throws IllegalStateException, IOException, SnmpStatusException;

    
    /**
     * Sends an inform using SNMP V2 inform request format.
     * <BR>The inform is sent to the specified <CODE>SnmpPeer</CODE> destination.
     * <BR> The community string used is the one located in the <CODE>SnmpPeer</CODE> parameters (<CODE>SnmpParameters.getInformCommunity() </CODE>).
     * <BR>The variable list included in the outgoing inform is composed of the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by <CODE>trapOid</CODE>
     * <LI><CODE>all the (oid,values)</CODE> from the specified <CODE>varBindList</CODE>
     * </UL>
     * To send an inform request, the SNMP adaptor server must be active.
     * 
     * @param peer The <CODE>SnmpPeer</CODE> destination for this inform request.
     * @param cb The callback that is invoked when a request is complete.
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @return The inform request object.
     *
     * @exception IllegalStateException  This method has been invoked while the SNMP adaptor server was not active.
     * @exception IOException An I/O error occurred while sending the inform request.
     * @exception SnmpStatusException If the inform request exceeds the limit defined by <CODE>bufferSize</CODE>.
     *
     */
    public SnmpInformRequest snmpInformRequest(SnmpPeer peer,
					       SnmpInformHandler cb,
					       SnmpOid trapOid,
					       SnmpVarBindList varBindList) throws IllegalStateException, IOException, SnmpStatusException;
}
