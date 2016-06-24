/*
 * @(#)file      SnmpV3AdaptorServerMBean.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.32
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
import com.sun.management.snmp.manager.usm.SnmpUsmPeer;
import com.sun.management.snmp.SnmpEngineId;

// jdmk imports
//
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpMibHandler;
import com.sun.management.snmp.agent.SnmpUserDataFactory;

/**
 * Exposes the remote management interface of the {@link SnmpV3AdaptorServer} MBean.
 *
 * @since Java DMK 5.1
 */

public interface SnmpV3AdaptorServerMBean extends SnmpAdaptorServerMBean {
     /**
     * Returns the associated <CODE>SnmpEngineId</CODE> value.
     * @return The engine Id.
     */
    public SnmpEngineId getEngineId();
    /**
     * Returns an array of security model Ids.
     * @return The security model Ids.
     */
    public int[] getSecurityModelIds();
    /**
     * Returns an array of security model names.
     * @return The security model names.
     */
    public String[] getSecurityModelNames();
    /**
     * Returns an array of message processing model Ids.
     * @return The message processing model Ids.
     */
    public int[] getMsgProcessingModelIds();
    /**
     * Returns an array of message processing model names.
     * @return The message processing model names.
     */
    public String[] getMsgProcessingModelNames();
    /**
     * Returns an array of access control model Ids.
     * @return The access control model Ids.
     */
    public int[] getAccessControlModelIds();
    /**
     * Returns an array of access control model names.
     * @return The access control model names.
     */
    public String[] getAccessControlModelNames();

    /**
     * Returns an array of User based Security Model supported algorithms.
     * @return The supported algorithms.
     */
    public String[] getUsmSecurityAlgorithms();
    /**
     * Returns an array of context names.
     * @return The context names.
     */
    public String[] getContexts();
    
    /**
     * Returns the names of the MIBs registered in the passed contextName.
     * 
     * @param context The context name.
     * @return An array of MIB names.
     */
    public String[] getMibs(String context);

    /**
     * Adds a new contextualized MIB in the SNMP MIB handler.
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler, String)}  
     * and {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName, String)} and should not be called directly from remote.
     *
     * @param mib The MIB to add. 
     * @param contextName The MIB context name. If null or 
     contextName.length == 0, will be registered in the default context.
     * @return A reference on the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, String contextName) 
	throws IllegalArgumentException;
    
    /**
     * Adds a new contextualized MIB in the SNMP MIB handler.
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler, String, SnmpOid[])}  
     * and {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName, String, SnmpOid[])} and should not be called directly from remote.
     * @param mib The MIB to add.
     * @param contextName The MIB context. If null or 
     * contextName.length == 0, will be registered in the default context.
     * @param oids The set of OIDs this agent implements.
     * If null or oids.length == 0, this is equivalent to calling
     * <CODE>addMib(SnmpMibAgent mib, String contextName)</CODE>
     * @return A reference to the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, 
				 String contextName, 
				 SnmpOid[] oids) 
	throws IllegalArgumentException;
    
    /**
     * Removes a contextualized MIB from the SNMP protocol adaptor.
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler, String)}  
     * and {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName, String)} and should not be called directly from remote.
     *
     * @param mib The MIB to be removed.
     * @param contextName The context name used at registration time. 
     * If null or contextName.length == 0, will be removed from 
     * the default context.
     * If null is passed, will be registered in the default context.
     *
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> was a MIB included in the SNMP MIB handler, 
     * <CODE>false</CODE> otherwise.
     */
    public boolean removeMib(SnmpMibAgent mib, String contextName);

      /**
     * Sends an inform request using SNMP V3 inform request format.
     * <BR>The inform is sent to the specified <CODE>InetAddress</CODE> destination.
     * The inform is sent in an SNMP V3 message.
     * Engine Id of the inform receiver will be discovered using Snmp V3 engine Id discovery.
     * <BR>The variable list included in the outgoing inform is composed of the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by <CODE>trapOid</CODE>
     * <LI><CODE>all the (oid,values)</CODE> from the specified <CODE>varBindList</CODE>
     * </UL>
     * To send an inform request, the SNMP adaptor server must be active.
     * 
     * @param informPeer The peer to which the inform is sent.
     * @param cb The callback that is invoked when a request is complete.
     * @param trapOid The OId identifying the trap.
     * @param list A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @return The inform request object.
     *
     * @exception IllegalStateException  This method has been invoked while the SNMP adaptor server was not active.
     * @exception IOException An I/O error occurred while sending the inform request.
     * @exception SnmpStatusException If the inform request exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public SnmpV3InformRequest snmpV3UsmInformRequest(SnmpUsmPeer informPeer,
                                                      SnmpInformHandler cb,
                                                      SnmpOid trapOid, 
                                                      SnmpVarBindList list) 
        throws IllegalStateException, IOException, SnmpStatusException;

    /**
     * Sends a trap to all IP addresses in IPAcl using Usm as security model.
     * @param principal The user name to use.
     * @param securityLevel The V3 message flags.
     * @param contextName The V3 context name. The <CODE>contextEngineId</CODE> is the local engine Id.
     * @param trapOid The OId identifying the trap.
     * @param list A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public void snmpV3UsmTrap(String principal,
                              int securityLevel,
                              String contextName,
			      SnmpOid trapOid,
                              SnmpVarBindList list) 
        throws IOException, SnmpStatusException;

    /**
     * Sends a trap to the specified address using Usm as security model.
     * @param addr The trap address destination.
     * @param principal The user name to use.
     * @param securityLevel The V3 message flags.
     * @param contextName The V3 context name. The <CODE>contextEngineId</CODE> is the local engine Id.
     * @param trapOid The OId identifying the trap.
     * @param list A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public void snmpV3UsmTrap(InetAddress addr, 
                              String principal,
                              int securityLevel,
                              String contextName,
			      SnmpOid trapOid,
                              SnmpVarBindList list) 
        throws IOException, SnmpStatusException;
    /**
     * Sends a trap to the specified address using Usm as security model.
     * @param addr The trap address destination.
     * @param principal The user name to use.
     * @param securityLevel The V3 message flags.
     * @param contextName The V3 context name. The <CODE>contextEngineId</CODE> is the local engine Id.
     * @param trapOid The OID identifying the trap.
     * @param list A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @param time The time stamp (overwrite the current time).
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     */
    public void snmpV3UsmTrap(InetAddress addr, 
                              String principal,
                              int securityLevel,
                              String contextName,
			      SnmpOid trapOid,
                              SnmpVarBindList list,
			      SnmpTimeticks time) 
        throws IOException, SnmpStatusException;
    
    /**
     * Sends a trap to the specified <CODE>SnmpUsmPeer</CODE> using Usm as security model.
     * Security parameters and context name used are the one located in SnmpUsmPeer. Be sure to update them. 
     * NO Usm discovery is needed when sending trap.
     * @param peer The trap destination.
     * @param trapOid The OID identifying the trap.
     * @param list A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @param time The time stamp (overwrite the current time).
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined by <CODE>bufferSize</CODE>.
     * @exception IllegalArgumentException If no <CODE>SnmpUsmParameters</CODE> are set.
     */
    public void snmpV3UsmTrap(SnmpUsmPeer peer,
			      SnmpOid trapOid,
			      SnmpVarBindList list,
			      SnmpTimeticks time) 
	throws IOException, SnmpStatusException, IllegalArgumentException;
    
    /**
     * Activate the community string to context translation. When making SNMP V1 and V2, you can access the scoped MIB. To do so, you need to have a community string of the following form : <community>@<context name>. Requests having such a community string will be routed to the scoped MIBs. By default this translation is activated.
     * @return boolean True, activation succeeded.
     */
    public boolean enableCommunityStringAtContext();

    /**
     * Deactivate the community string to context translation. When making SNMP V1 and V2, you can access the scoped MIB. To do so, you need to have a community string of the following form : <community>@<context name>. Requests having such a community string will be routed to the scoped MIBs. By default this translation is activated.
     * @return boolean True, deactivation succeeded.
     */
    public boolean disableCommunityStringAtContext();

    /**
     * The community to context translation activation status.
     * @return boolean True, the translation will be applied on each received SNMP V1 and V2 community string.
     */
    public boolean isCommunityStringAtContextEnabled();

    /**
     * Returns <CODE>snmpUnknownSecurityModels</CODE> as defined in rfc 2572 SNMP-MPD-MIB.
     * @return snmpUnknownSecurityModels counter.
     */
    public Long getSnmpUnknownSecurityModels();
    
    /**
     * Returns <CODE>snmpInvalidMsgs</CODE> as defined in rfc 2572 SNMP-MPD-MIB.
     * @return snmpInvalidMsgs counter.
     */
    public Long getSnmpInvalidMsgs();

    /**
     * Returns <CODE>snmpUnknownContexts</CODE> as defined in rfc 2573 SNMP-TARGET-MIB.
     * @return snmpUnknownContexts counter.
     */
    public Long getSnmpUnknownContexts();
    
}
