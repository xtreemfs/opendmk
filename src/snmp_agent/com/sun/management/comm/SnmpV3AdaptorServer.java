/*
 * @(#)file      SnmpV3AdaptorServer.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.89
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Hashtable;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

// jmx imports
//
import javax.management.MBeanServer;
import javax.management.MBeanRegistration;
import javax.management.ObjectName;
import javax.management.InstanceAlreadyExistsException;
import com.sun.management.snmp.SnmpIpAddress;
import com.sun.management.snmp.SnmpMessage;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpPduRequest;
import com.sun.management.snmp.SnmpPduTrap;
import com.sun.management.snmp.SnmpTimeticks;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpScopedPduRequest;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.manager.usm.SnmpUsmPeer;
import com.sun.management.snmp.manager.usm.SnmpUsmParameters;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.NotCompliantMBeanException;
import javax.management.MalformedObjectNameException;

import com.sun.management.snmp.SnmpSecurityException;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngineFactory;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineParameters;
import com.sun.management.snmp.SnmpUnknownModelException;
import com.sun.management.snmp.SnmpSecurityException;
import com.sun.management.snmp.UserAcl;
// jdmk imports
//
import com.sun.management.internal.snmp.SnmpOutgoingRequest;
import com.sun.management.snmp.SnmpBadSecurityLevelException;
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.internal.snmp.SnmpAccessControlSubSystem;
import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpPduFactoryBER;
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpRequestForwarder;
import com.sun.management.snmp.agent.SnmpMibHandler;
import com.sun.management.snmp.agent.SnmpUserDataFactory;
import com.sun.management.snmp.agent.SnmpMibAgentMBean;
import com.sun.management.snmp.InetAddressAcl;
import com.sun.management.snmp.IPAcl.JdmkAcl;
import com.sun.jdmk.internal.ThreadService;
import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.snmp.JdmkEngineFactory;
import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpUnknownMsgProcModelException;
import com.sun.management.snmp.usm.SnmpUsmException;
import com.sun.management.snmp.usm.usmmib.SNMP_USER_BASED_SM_MIB;
import com.sun.management.snmp.usm.usmmib.SNMP_USER_BASED_SM_MIBImpl;
import com.sun.management.snmp.usm.SnmpUsm;
import com.sun.management.snmp.usm.SnmpUsmLcd;
import com.sun.management.snmp.usm.SnmpUsmAlgorithmManager;
import com.sun.management.snmp.usm.SnmpUsmSecurityParameters;
import com.sun.management.internal.snmp.SnmpSecuritySubSystem;
import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpBadSecurityLevelException;


import com.sun.management.snmp.mpm.SnmpMsgTranslatorV1V2;
import com.sun.management.snmp.mpm.SnmpMsgTranslatorCs2Ctxt;
import com.sun.management.snmp.mpm.SnmpMsgTranslator;
import com.sun.management.snmp.mpm.SnmpMsgProcessingModelV1V2;
import com.sun.management.internal.snmp.SnmpMsgProcessingModel;
import com.sun.management.internal.snmp.SnmpAccessControlModel;

/**
 * Implements an adaptor on top of the SNMP V3 protocol.
 * <P> This adaptor deals with SNMP V1 and V2c protocols too (thanks to 
 * the <CODE>SnmpAdaptorServer</CODE> inheritence).</P>
 * The <CODE>SnmpV3AdaptorServer</CODE>can be seen as an SNMP V3 engine. 
 * The engine is constructed at <CODE> SnmpV3AdaptorServer </CODE>
 * construction time. This engine contains : 
 * <ul>
 * <li> 3 Message Processing Model (SNMP V1, V2 and V3) </li>
 * <li> 1 Message Security Model (User based Security Model) implementing 
 * <CODE>usmHMACMD5AuthProtocol</CODE> and <CODE>usmHMACSHAAuthProtocol</CODE>
 * authentication algorithms.</li>
 * <li> 1 Access Control Model compatible with IP Acl for SNMP V1 and SNMP V2.
 * It also implements of a simple user based Acm using a user based ACL.</li>
 * </ul> </P>
 * <P> IP Acl is configured as with <CODE>SnmpAdaptorServer</CODE>. 
 * To configure user Acl, see {@link com.sun.management.snmp.uacl.JdmkUserAcl 
 * JdmkUserAcl}.
 *
 * @since Java DMK 5.1
 */

public class SnmpV3AdaptorServer extends SnmpAdaptorServer 
    implements SnmpV3AdaptorServerMBean {


    // PRIVATE VARIABLES
    //-------------------
    private Hashtable contexts = new Hashtable();
    private Hashtable forwarders = new Hashtable();
    
    SnmpMsgTranslatorV1V2 v1v2translator = null;
    SnmpMsgTranslatorCs2Ctxt cs2ctxttranslator = null;
    SnmpMsgProcessingModel v1v2mpm = null;
    SnmpAccessControlModel v3acm = null;
    //USM MIB.
    SNMP_USER_BASED_SM_MIB usmmib = null;
    
    //Managers peers that receive inform.
    //Store the SnmpUsmPeer to allow timeliness sync.
    private Hashtable peers = new Hashtable();
    private SnmpEngineImpl engine = null;
    
    private SnmpV3Session       sessionV3 = null;
    /**
     * The <CODE>snmpUnknownSecurityModels</CODE> value defined in MIB II.
     */
    private int snmpUnknownSecurityModels = 0;
    /**
     * The <CODE>snmpInvalidMsgs</CODE> value defined in MIB II.
     */
    private int snmpInvalidMsgs = 0;

    /**
     * The <CODE>snmpUnknownContexts</CODE> value defined in MIB II.
     */
    private int snmpUnknownContexts = 0;


    private ThreadService threadService;
    
    // PUBLIC CONSTRUCTORS
    //--------------------
    
    /**
     * Initializes this SNMP protocol adaptor using the default port (161).
     * Use the {@link com.sun.management.snmp.IPAcl.JdmkAcl} default 
     * implementation of the <CODE>InetAddressAcl</CODE> interface.
     * The SNMP engine is instantiated at the same time.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *   "localEngineID", this property value is used.</li>.
     * <li> If not, the adaptor computes an engine Id using the host and 
     *   port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist 
     *   (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the 
     *   specified configuration files doesn't exist or is incorrect.
     */
    public SnmpV3AdaptorServer() throws IllegalArgumentException {
	this(new SnmpEngineParameters(), 
	     new JdmkEngineFactory(),
	     null,
	     161,
	     null);
	     
    }

    /**
     * Initializes this SNMP protocol adaptor using the specified port.
     * Use the {@link com.sun.management.snmp.IPAcl.JdmkAcl} default 
     * implementation of the <CODE>InetAddressAcl</CODE> interface.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *   "localEngineID", this property value is used.</li>.
     * <li> If not, the adaptor computes an engine Id using the host and 
     *   port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist
     *   (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the specified
     *    configuration files doesn't exist or is incorrect.
     *
     * @param port The port number for sending SNMP responses.
     */
    public SnmpV3AdaptorServer(int port) throws IllegalArgumentException{
	this(new SnmpEngineParameters(), 
	     new JdmkEngineFactory(),
	     null,
	     port,
	     null);
    }
    
    /**
     * Initializes this SNMP protocol adaptor using the default port (161)
     * and the specified IP address based ACL implementation.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *   "localEngineID", this property value is used.</li>.
     * <li> If not, the adaptor computes an engine Id using the host and 
     *   port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist
     *    (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the 
     *    specified configuration files doesn't exist or is incorrect.
     * @param acl The <CODE>InetAddressAcl</CODE> implementation.
     */
    public SnmpV3AdaptorServer(InetAddressAcl acl)
	throws IllegalArgumentException{
        this(new SnmpEngineParameters(), 
	     new JdmkEngineFactory(),
	     acl,
	     161,
	     null);
    }

    /**
     * Initializes this SNMP protocol adaptor using the default port (161) 
     * and the specified <CODE>InetAddress</CODE>.
     * Use the {@link com.sun.management.snmp.IPAcl.JdmkAcl} default 
     * implementation of the <CODE>InetAddressAcl</CODE> interface.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *   "localEngineID", this property value is used.</li>.
     * <li> If not, the adaptor computes an engine Id using the host and 
     *   port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist 
     *    (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the specified
     *   configuration files doesn't exist or is incorrect.
     * @param addr The IP address to bind.
     */
    public SnmpV3AdaptorServer(InetAddress addr) 
	throws IllegalArgumentException {
        this(new SnmpEngineParameters(), 
	     new JdmkEngineFactory(),
	     null,
	     161,
	     addr);
    }
    
    /**
     * Initializes this SNMP protocol adaptor using the specified port and the 
     * specified IP address based ACL implementation.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *   "localEngineID", this property value is used.</li>.
     * <li> If not, the adaptor computes an engine Id using the host and 
     *   port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist 
     *   (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the 
     *    specified configuration files doesn't exist or is incorrect.
     * @param acl The <CODE>InetAddressAcl</CODE> implementation.
     * @param port The port number for sending SNMP responses.
     */
    public SnmpV3AdaptorServer(InetAddressAcl acl, int port) 
	throws IllegalArgumentException {
        this(new SnmpEngineParameters(), 
	     new JdmkEngineFactory(),
	     acl,
	     port,
	     null);
    }
      
    /**
     * Initializes this SNMP protocol adaptor using the specified port and the 
     * specified <CODE>InetAddress</CODE>.
     * Use the {@link com.sun.management.snmp.IPAcl.JdmkAcl} default 
     * implementation of the <CODE>InetAddressAcl</CODE> interface.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *    "localEngineID", this property value is used.</li>.
     * <li> If not, the adaptor computes an engine Id using the host and 
     *   port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist 
     *   (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the 
     *    specified configuration files doesn't exist or is incorrect.
     * @param port The port number for sending SNMP responses.
     * @param addr The IP address to bind.
     */
    public SnmpV3AdaptorServer(int port, InetAddress addr) 
	throws IllegalArgumentException {
	this(new SnmpEngineParameters(), 
	     new JdmkEngineFactory(),
	     null,
	     port,
	     addr);
    }
      
    /**
     * Initializes this SNMP protocol adaptor using the specified 
     * IP address based ACL implementation and the specified 
     * <CODE>InetAddress</CODE>.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *    "localEngineID", this property value is used.</li>.
     * <li> If not, the adaptor computes an engine Id using the host and 
     *   port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist 
     *   (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the 
     *    specified configuration files doesn't exist or is incorrect.
     * @param acl The <CODE>InetAddressAcl</CODE> implementation.
     * @param addr The IP address to bind.
     */
    public SnmpV3AdaptorServer(InetAddressAcl acl, InetAddress addr) 
	throws IllegalArgumentException{
        this(new SnmpEngineParameters(), 
	     new JdmkEngineFactory(),
	     acl,
	     161,
	     addr);
    }

    /**
     * Initializes this SNMP protocol adaptor using the specified port, the 
     * specified  address based ACL implementation and the specified 
     * <CODE>InetAddress</CODE>.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *   "localEngineID", this property value is used.</li>.
     * <li> If not, the adaptor computes an engine Id using the host and
     * port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist 
     *   (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the specified 
     *   configuration files doesn't exist or is incorrect.
     * @param acl The <CODE>InetAddressAcl</CODE> implementation.
     * @param port The port number for sending SNMP responses.
     * @param addr The IP address to bind.
     */
    public SnmpV3AdaptorServer(InetAddressAcl acl, int port, InetAddress addr)
	throws IllegalArgumentException {
	this(new SnmpEngineParameters(), 
	     new JdmkEngineFactory(),
	     acl,
	     port,
	     addr);
    }
      
    /**
     * Initializes this SNMP protocol adaptor using the specified port and the 
     * specified <CODE>InetAddress</CODE>.
     * This constructor allows you to initialize an SNMP adaptor without
     * using the ACL mechanism (by setting the <CODE>useAcl</CODE> parameter 
     * to false).
     * <br>This constructor must be used in particular with a platform that 
     * does not support the 
     * <CODE>java.security.acl</CODE> package like pJava.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *    "localEngineID", this property value is used.</li>.
     * <li> If not, the adaptor computes an engine Id using the host and
     * port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist 
    *   (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the specified 
     * configuration files doesn't exist or is incorrect.
     * @param useAcl Specifies if this new SNMP adaptor uses the ACL mechanism.
     * If the specified parameter is set to <CODE>true</CODE>, this 
     * constructor is equivalent to 
     * <CODE>SnmpV3AdaptorServer(int port, InetAddress addr)</CODE>.
     * @param port The port number for sending SNMP responses.
     * @param addr The IP address to bind.
     */
    public SnmpV3AdaptorServer(boolean useAcl, int port, InetAddress addr) 
	throws IllegalArgumentException {
       	this(new SnmpEngineParameters(), 
	     new JdmkEngineFactory(),
	     useAcl,
	     port,
	     addr); 
    }
    /**
     * Initializes this SNMP protocol adaptor with a specified factory, 
     * engine parameters, an ACL implementation, a port, and the specified 
     * <CODE>InetAddress</CODE>.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *    "localEngineID", this property value is used.</li>.
     * <li> If the engine id of the specified engine parameters is not null,
     *   this one is used.</li>
     * <li> If not, the adaptor computes an engine Id using the host and
     * port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist 
     *   (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the specified
     *    configuration files doesn't exist or is incorrect.
     * @param parameters The engine parameters to use. 
     * @param factory The factory to use in order to create the engine.
     * @param ipacl The <CODE>InetAddressAcl</CODE> implementation.
     * @param port The port number for sending SNMP responses.
     * @param addr The IP address to bind.
     */
    public SnmpV3AdaptorServer(SnmpEngineParameters parameters, 
			       SnmpEngineFactory factory,
			       InetAddressAcl ipacl, 
			       int port, 
			       InetAddress addr) 
	throws IllegalArgumentException {
	super(ipacl, port, addr);

	createEngine(parameters, factory);
    }  
    /**
     * Initializes this SNMP protocol adaptor with a specified factory, 
     * engine parameters, port and the specified <CODE>InetAddress</CODE>.
     * This constructor allows you to initialize an SNMP adaptor without
     * using the ACL mechanism (by setting the <CODE>useAcl</CODE> parameter 
     * to false).
     * <br>This constructor must be used in particular with a platform that 
     * does not support the 
     * <CODE>java.security.acl</CODE> package like pJava.
     * <P> WARNING : The engine id is computed as follows:
     * <ul>
     * <li> If an lcd file is provided containing the property 
     *    "localEngineID", this property value is used.</li>.
     * <li> If the engine id of the specified engine parameters is not null, 
     *   this one is used. </li>
     * <li> If not, the adaptor computes an engine Id using the host and
     * port number.</li>
     * </ul>
     * <P> In some cases the adaptor will fail during creation :
     * <ul>
     * <li> At least one of the provided configuration files doesn't exist 
    *   (Acl files, lcd file)</li>
     * <li> The provided LCD file contains erroneous data. </li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the specified 
     * configuration files doesn't  exist or is incorrect.
     * @param parameters The engine parameters to use. 
     * @param factory The factory to use in order to create the engine.
     * @param useAcl If set to false, no ACL is used.
     * @param port The port number for sending SNMP responses.
     * @param addr The IP address to bind.
     */
    public SnmpV3AdaptorServer(SnmpEngineParameters parameters, 
			       SnmpEngineFactory factory,
			       boolean useAcl, 
			       int port, 
			       InetAddress addr) 
	throws IllegalArgumentException {
	super(useAcl, port, addr);
	createEngine(parameters, factory);
    }
    
    /**
     * Returns <CODE>snmpUnknownSecurityModels</CODE> as defined in 
     * RFC 2572 SNMP-MPD-MIB.
     * @return snmpUnknownSecurityModels counter.
     */
    public Long getSnmpUnknownSecurityModels() {
        return new Long(snmpUnknownSecurityModels);
    }
    
    /**
     * Returns <CODE>snmpInvalidMsgs</CODE> as defined in RFC 2572 
     * SNMP-MPD-MIB.
     * @return snmpInvalidMsgs counter.
     */
    public Long getSnmpInvalidMsgs() {
        return new Long(snmpInvalidMsgs);
    }

    /**
     * Returns <CODE>snmpUnknownContexts</CODE> as defined in RFC 2573 
     * SNMP-TARGET-MIB.
     * @return snmpUnknownContexts counter.
     */
    public Long getSnmpUnknownContexts() {
	return new Long(snmpUnknownContexts);
    }

    /**
     * Returns <CODE>snmpUnknownPDUHandlers</CODE> as defined in RFC 2572 
     * SNMP-MPD-MIB.
     * @return snmpUnknownPDUHandlers counter.
     */
    public Long getSnmpUnknownPDUHandlers() {
        return new Long(0);
    }
    
    /**
     * Inc the snmpUnknownContexts counter.
     */
    void incSnmpUnknownContexts(int n) {
        snmpUnknownContexts += n;
    }
    /**
     * Inc the snmpUnknownSecurityModels counter.
     */
    void incSnmpUnknownSecurityModels(int n) {
        snmpUnknownSecurityModels += n;
    }
    
    /**
     * Inc the  snmpInvalidMsgs counter.
     */
    void incSnmpInvalidMsgs(int n) {
        snmpInvalidMsgs += n;
    }
    
    /**
     * Create the SNMP V3 engine. 
     * @exception java.lang.IllegalArgumentException If one of the specified
     * configuration files doesn't exist or is incorrect.
     */  
    private void createEngine(SnmpEngineParameters parameters,
			      SnmpEngineFactory engFactory) 
	throws IllegalArgumentException {
	if(parameters == null)
	    parameters = new SnmpEngineParameters();
	if(engFactory == null)
	    engFactory = new JdmkEngineFactory();

	SnmpEngineId engineid = parameters.getEngineId();
	if(engineid == null) {
	    if(address != null) {
		engineid = SnmpEngineId.createEngineId(address, getPort());
		parameters.setEngineId(engineid);
	    }
	    else {
		try {
		    engineid = 
			SnmpEngineId.createEngineId(InetAddress.getLocalHost(),
						    getPort());
		    parameters.setEngineId(engineid);
		} catch(UnknownHostException e) {
		    throw new IllegalArgumentException("Unknown host: " +
						       e.getMessage());
		}
	    }
	}
	
	engine = (SnmpEngineImpl) 
	    engFactory.createEngine(parameters, getInetAddressAcl());
	if(engine == null)
	    throw new IllegalArgumentException("The factory returned " +
		  "a null engine. SnmpV3AdaptorServer initilization failed");
	
	createUsmMib(engine);
	
	if (logger.finestOn())
	    logger.finest("createEngine", "Engine created.");

	//In order to change community string translation, access V1 and V2 mpm
	try {
	    SnmpMsgProcessingSubSystem msgsys = 
		engine.getMsgProcessingSubSystem();
	    if(msgsys != null) {
		v1v2mpm =  (SnmpMsgProcessingModelV1V2)
		    msgsys.getModel(SnmpDefinitions.snmpV1SecurityModel);
		v1v2translator = new SnmpMsgTranslatorV1V2(engine);
		cs2ctxttranslator = new SnmpMsgTranslatorCs2Ctxt(engine);
	    }

	    SnmpAccessControlSubSystem acsys = 
		engine.getAccessControlSubSystem();
	    if(acsys != null) {
		//In order to change SNMP V1 and V2 set requests status,
		// access SnmpJdmkACM. This is a Java DMK only feature.
		v3acm = (SnmpAccessControlModel)
		    acsys.getModel(SnmpDefinitions.snmpV3AccessControlModel);
	    }
	    
	}catch(SnmpUnknownModelException e) {
	    if(logger.finestOn()) 
		logger.finest("createEngine", 
			      "SNMP V2 translation not activated, "+
			      "translation not activable");
	}

	threadService = new ThreadService(SnmpAdaptorServer.getThreadNumber());
	
    }
    
    /**
     * Returns the associated <CODE>SnmpEngine</CODE> value.
     * @return The engine.
     */
    public SnmpEngine getEngine() {
	return engine;
    }
    
    /**
     * Returns the associated <CODE>SnmpEngineId</CODE> value.
     * @return The engine Id.
     */
    public SnmpEngineId getEngineId() {
	if(engine == null)
	    return null;
        return engine.getEngineId();
    }
    /**
     * Adds a new contextualized MIB in the SNMP MIB handler.
     * 
     * @param mib The MIB to add.
     * @param contextName The MIB context. If null or contextName.length == 0, 
     * will be registered in the default context.
     * 
     * @return A reference to the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, String contextName) 
        throws IllegalArgumentException {
	if(logger.finerOn())
	    logger.finer("addMib", "Mib [" + mib + "] in context [" + 
			 contextName + "]");

        if (mib == null)
            throw new IllegalArgumentException();
	
        if(contextName == null || contextName.length() == 0) {
	    if(logger.finerOn())
		logger.finer("addMib", "Mib [" + mib + "] in default context");
            return addMib(mib);
	}
	
	SnmpMibContextName cont = 
	    (SnmpMibContextName) contexts.get(contextName);
	
	if(cont == null) {
	    if(logger.finerOn())
		logger.finer("addMib","Unknown context [" + contextName + "]");
	    cont = new SnmpMibContextName();
	    cont.root.setDefaultAgent(getDefaultAgent());
	    contexts.put(contextName, cont);
	}
	
	if(!cont.mibs.contains(mib)) {
	    if(logger.finerOn())
		logger.finer("addMib","Adding MIB [" + mib + "]");
	    cont.mibs.addElement(mib);
	    cont.root.register(mib);
	} else
	    if(logger.finerOn())
		logger.finer("addMib","MIB [" + mib + "] already registered");
	
        return this;
    }

    /**
     * Activate the community string to context translation. When
     * making SNMP V1 and V2, you can access the scoped MIB. To do so,
     * you need to have a community string of the following form :
     * <em>community</em>@<em>context-name</em>. Requests having such
     * a community string will be routed to the scoped MIBs. By
     * default this translation is activated.
     * @return boolean True, activation succeeded.
     */
    public boolean enableCommunityStringAtContext() {
	if(v1v2mpm == null) return false;
	v1v2mpm.setMsgTranslator(cs2ctxttranslator);
	return true;
    }

    /**
     * Deactivate the community string to context translation. When
     * making SNMP V1 and V2, you can access the scoped MIB. To do so,
     * you need to have a community string of the following form :
     * <em>community</em>@<em>context-name</em>. Requests having such
     * a community string will be routed to the scoped MIBs. By
     * default this translation is activated.
     * @return boolean True, deactivation succeeded.
     */
    public boolean disableCommunityStringAtContext() {
	if(v1v2mpm == null) return false;
	v1v2mpm.setMsgTranslator(v1v2translator);
	return true;
    }

    /**
     * The community to context translation activation status.
     * @return boolean True, the translation will be applied on each 
     *   received SNMP V1 and V2 community string.
     */
    public boolean isCommunityStringAtContextEnabled() {
	if(v1v2mpm == null) return false;
	if(v1v2mpm.getMsgTranslator() instanceof SnmpMsgTranslatorCs2Ctxt)
	    return true;
	
	return false;
    }
  
    /**
     * Enable SNMP V1 and V2 set requests. Be aware that can lead to a 
     * security hole in a context of SNMP V3 management. 
     * By default SNMP V1 and V2 set requests are not authorized.
     * @return boolean True the activation succeeded.
     */
    public synchronized boolean enableSnmpV1V2SetRequest() {
	if(v3acm == null) return false;
	return v3acm.enableSnmpV1V2SetRequest();
    }
    /**
     * Disable SNMP V1 and V2 set requests. By default SNMP V1 and V2 set 
     * requests are not authorized.
     * @return boolean True the deactivation succeeded.
     */
    public synchronized boolean disableSnmpV1V2SetRequest() {
	if(v3acm == null) return false;
	return v3acm.disableSnmpV1V2SetRequest();
	
    }
    
    /**
     * The SNMP V1 and V2 set requests authorization status. 
     * By default SNMP V1 and V2 set requests are not authorized.
     * @return boolean True SNMP V1 and V2 requests are authorized.
     */
    public synchronized boolean isSnmpV1V2SetRequestAuthorized() {
	if(v3acm == null) return false;
	return v3acm.isSnmpV1V2SetRequestAuthorized();
    }

    /**
     * Adds a new MIB forwarder attached to a passed engine Id. 
     * Every request received for the specified engineId will be routed to 
     * the passed MibAgent.
     * @param forwarder The forwarder to add.
     * @param contextEngineId The context engine Id this forwarder forwards to.
     *
     * @exception IllegalArgumentException If one of the parameters is null.
     */
    public void addRequestForwarder(SnmpRequestForwarder forwarder, 
				    SnmpEngineId contextEngineId) {
	if ((forwarder == null) || (contextEngineId == null))
	    throw new IllegalArgumentException() ;
	
	forwarders.put(contextEngineId.toString(), forwarder);
    }

    /**
     * Removes a request forwarder.
     * @param engineId The context engine id.
     * @throws IllegalArgumentException if engineId is null.
     */
    public boolean removeRequestForwarder(SnmpEngineId engineId) {
	if(engineId == null) 
	    throw new IllegalArgumentException();
	
	Object obj = forwarders.remove(engineId.toString());
	if(obj == null)
	    return false;
	else
	    return true;
    }

    /**
     * Adds a new contextualized MIB in the SNMP MIB handler.
     * 
     * @param mib The MIB to add.
     * @param contextName The MIB context. If null or contextName.length == 0,
     * will be registered in the default context.
     * @param oids The set of OIDs this agent implements. 
     * If null or oids.length == 0, this is equivalent to calling
     * <CODE>addMib(SnmpMibAgent mib, String contextName)</CODE>
     * 
     * @return A reference to the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, 
				 String contextName,
				 SnmpOid[] oids) 
        throws IllegalArgumentException {
	
	if(logger.finerOn()) {
	    logger.finer("addMib", "Mib [" + mib + "] in context " + 
			 contextName + "]");
	    StringBuffer b = new StringBuffer();
	    for(int i = 0; i < oids.length; i++)
		b.append(oids[i] + " ");
	    logger.finer("addMib", "Oid[] = " + b.toString());  
	}
	
        if (mib == null)
            throw new IllegalArgumentException();
	
        if(contextName == null || contextName.length() == 0) {
	    if(logger.finerOn())
		logger.finer("addMib", "Mib [" + mib + "] in default context");
	    
	    return addMib(mib, oids);
	}
	
	//If array is null or length == 0, register in context only.
	if(oids == null || oids.length == 0)
	    return addMib(mib, contextName);
	
	SnmpMibContextName cont = 
	    (SnmpMibContextName) contexts.get(contextName);
	
	if(cont == null) {
	    if(logger.finerOn())
		logger.finer("addMib","Unknown context [" + contextName + "]");
	    cont = new SnmpMibContextName();
	    cont.root.setDefaultAgent(getDefaultAgent());
	    contexts.put(contextName, cont);
	}
	
	if(!cont.mibs.contains(mib)) {
	    if(logger.finerOn())
		logger.finer("addMib","Adding MIB [" + mib +"]");
	    cont.mibs.addElement(mib);
	} else
	    if(logger.finerOn())
		logger.finer("addMib","MIB [" + mib + "] already registered");
	
	for (int i = 0; i < oids.length; i++)
	    cont.root.register(mib, oids[i].longValue());
	
	return this;
    }
    
    /**
     * Removes a contextualized MIB from the SNMP protocol adaptor. 
     * 
     * @param mib The MIB to be removed.
     * @param contextName The MIB context. 
     * If null or contextName.length == 0, will be removed from 
     * the default context.
     *
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> 
     * was a MIB included in the SNMP MIB handler, 
     * <CODE>false</CODE> otherwise.
     */
    public boolean removeMib(SnmpMibAgent mib, String contextName) {
	if(logger.finerOn())
	    logger.finer("removeMib", "Mib [ " + mib +"] in context " + 
			 contextName);
	
        if(contextName == null || contextName.length() == 0)
            return removeMib(mib);
	
        SnmpMibContextName context = 
            (SnmpMibContextName) contexts.get(contextName);
	
	if(context == null) return false;
	
	boolean ret = context.mibs.removeElement(mib);
	
	if(ret) {
	    if(logger.finestOn()) 
		logger.finest("removeMib","Mib : " + mib + 
			      " removed from context :"+ contextName);
	    context.root.unregister(mib);
	    if(context.mibs.size() == 0) {
		if(logger.finerOn()) 
		    logger.finer("removeMib",
			  "No more mib in context name. Remove contextName");
		contexts.remove(contextName);
	    }
	}
	else
	    if(logger.finerOn()) 
		logger.finer("removeMib","Mib : " + mib + 
		      " not removed from context :"+ contextName); 
	return ret;
    }

    /**
     * Removes the specified MIB from the SNMP protocol adaptor.
     * 
     * @param mib The MIB to be removed.
     * @param contextName The context name used at registration time.
     * If null or contextName.length == 0, will be removed from 
     * the default context.
     * @param oids The oid the MIB was previously registered for. 
     * If null or oids.length == 0, this is equivalent to calling
     * <CODE>removeMib(SnmpMibAgent mib, String contextName)</CODE>
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> 
     * was a MIB included in the SNMP MIB handler, 
     * <CODE>false</CODE> otherwise.
     */
    public boolean removeMib(SnmpMibAgent mib, 
			     String contextName, 
			     SnmpOid[] oids) {
	if(logger.finerOn()) {
	    logger.finer("removeMib", "Mib [" + mib + "] from context "+
			 contextName);
	    StringBuffer b = new StringBuffer();
	    for(int i = 0; i < oids.length; i++)
		b.append(oids[i] + " ");
	    logger.finer("removeMib", "Oid[] = " + b.toString());  
	}
	
	if(contextName == null || contextName.length() == 0)
	    return removeMib(mib, oids);
	
	if(oids == null || oids.length == 0) 
	    return removeMib(mib, contextName);
	
	SnmpMibContextName context = 
	    (SnmpMibContextName) contexts.get(contextName);
	
	if(context == null) return false;
	
	boolean ret = context.mibs.contains(mib);

	if(ret) {
	    //First unregister for the set of oids.
	    context.root.unregister(mib, oids);
	    
	    //Still in tree, don't remove it.
	    if(!context.root.isMibReferenced(mib)) {
		logger.finer("removeMib", "Mib removed");
		context.mibs.removeElement(mib);
		if(context.mibs.size() == 0) {
		    if(logger.finerOn()) 
			logger.finer("removeMib",
			      "No more mib in contest name." + 
			      "Remove " +contextName);
		    contexts.remove(contextName);
		}
	    } else
		if(logger.finerOn()) 
		    logger.finer("removeMib", 
				 "Mib NOT removed, still referenced");
	}
	
	return ret;
    }
    
    /**
     * In order to register the USM mib in the <CODE> MBeanServer </CODE>.
     * @param server The MBeanServer in which to register the mib.
     * @param obj The object name to use. If no object name is provided, 
     *    the following concatenation is used: 
     * <P>SnmpEngineId.toString() + 
     *    "/SNMP_USER_BASED_SM_MIB:name=Usm-MIB-RFC2574"</P>
     */
    public void registerUsmMib(MBeanServer server, ObjectName obj) 
	throws InstanceAlreadyExistsException,
	       MBeanRegistrationException,
	       NotCompliantMBeanException {
	ObjectName mibObjName = null;
	if(obj != null)
	    mibObjName = obj;
	else {
	    try {
		mibObjName =  createUsmMibObjectName(engine);
	    }catch(Exception e) {
		if(logger.finestOn()) 
		    logger.finer("registerUsmMib", e.toString());
	    }
	}
	server.registerMBean(usmmib, mibObjName);
	addMib(usmmib);
    }
    
    /**
     * In order to register the USM mib in the 
     * <CODE>SnmpV3AdaptorServer</CODE>.
     * The MIB is registered under no scope.
     */
    public void registerUsmMib() throws IllegalAccessException {
	usmmib.init();
	addMib(usmmib);
    }
    
    /** When a new request is received by the adaptor it calls this method. 
     * The expected behavior is the handling of the received datagram. 
     */
    void createSnmpRequestHandler(SnmpAdaptorServer server, int id, 
                                  DatagramSocket socket, DatagramPacket packet,
                                  SnmpMibTree tree, Vector m, Object a, 
                                  SnmpPduFactory factory, 
                                  SnmpUserDataFactory dataFactory,
                                  MBeanServer f, ObjectName n) {
	final SnmpV3RequestHandler handler =
	    new SnmpV3RequestHandler(this, engine, id, socket, packet, 
				     contexts, forwarders, tree, m, a, 
				     factory, dataFactory, f, n);
	threadService.submitTask(handler);
    }
    
    /**
     * Returns an array of security model Ids.
     * @return The security model Ids.
     */
    public int[] getSecurityModelIds() {
	if(engine == null)
	return null;
        return engine.getSecuritySubSystem().getModelIds();
    }

    /**
     * Returns an array of security model names.
     * @return The security model names.
     */
    public String[] getSecurityModelNames() {
	if(engine == null)
	return null;
        return engine.getSecuritySubSystem().getModelNames();
    }

    /**
     * Returns an array of message processing model Ids.
     * @return The message processing model Ids.
     */
    public int[] getMsgProcessingModelIds() {
	if(engine == null)
	return null;
        return engine.getMsgProcessingSubSystem().getModelIds();
    }

    /**
     * Returns an array of message processing model names.
     * @return The message processing model names.
     */
    public String[] getMsgProcessingModelNames() {
	if(engine == null)
	return null;
        return engine.getMsgProcessingSubSystem().getModelNames();
    }

    /**
     * Returns an array of access control model Ids.
     * @return the access control models Ids.
     */
    public int[] getAccessControlModelIds() {
	if(engine == null)
	return null;
        return engine.getAccessControlSubSystem().getModelIds();
    }
    
    /**
     * Returns an array of access control model names.
     * @return the access control models names.
     */
    public String[] getAccessControlModelNames() {
	if(engine == null)
	return null;
        return engine.getAccessControlSubSystem().getModelNames();
    }
    

    /**
     * Returns an array of User based Security Model supported algorithms.
     * @return The supported algorithms.
     */
    public String[] getUsmSecurityAlgorithms() {
	if(engine == null)
	return null;
        SnmpSecuritySubSystem subsys = engine.getSecuritySubSystem();
        SnmpUsm usm = null;
        try {
            usm = (SnmpUsm) subsys.getModel(SnmpUsm.ID);
        }catch(SnmpUnknownModelException e) {
            return null;
        }
        SnmpUsmLcd lcd = usm.getLcd();
        SnmpUsmAlgorithmManager manager = lcd.getAlgorithmManager();
        return manager.getManagedAlgorithms();
    }
    /**
     * Returns the names of the MIBs available in this SNMP protocol adaptor. 
     * Some of these mibs can be registered in a specific context.
     * 
     * @return An array of MIB names.
     */
    public String[] getMibs() {
        HashSet set = new HashSet(mibs);
	Iterator itr = set.iterator();
	for (Enumeration e = contexts.elements() ; e.hasMoreElements() ;) {
            SnmpMibContextName n = (SnmpMibContextName) e.nextElement();
            set.addAll(n.mibs);
        }
	String[] names = new String[set.size()];
	itr = set.iterator();
	int i = 0;
	while(itr.hasNext()) {
	    names[i] = ((SnmpMibAgentMBean)itr.next()).getMibName();
	    i++;
	}
	
	return names;
    }
    
    /**
     * Returns the names of the MIBs registered in the passed contextName.
     * 
     * @param context The context name.
     * @return An array of MIB names.
     */
    public String[] getMibs(String context) {
	if(context == null) return null;
	SnmpMibContextName ctx = (SnmpMibContextName) contexts.get(context);
	if(ctx == null) return null;
	if(ctx.mibs == null) return null;
	int size = ctx.mibs.size();
	String[] mibNames = new String[size];
	for(int i = 0; i < size; i++) {
	    mibNames[i] = ((SnmpMibAgentMBean)ctx.mibs.elementAt(i)).
		getMibName();
	}

	return mibNames;
    }
    
    /**
     * Returns the list of contexts in which some MIBs have been registered.
     * @return An array of context names.
     */
    public String[] getContexts() {
        String[] res = new String[contexts.size()];
        int i = 0;
        for (Enumeration e = contexts.keys() ; e.hasMoreElements() ;) {
            res[i] = (String) e.nextElement();
            i++;
        }
        return res;
    }
    /**
     * You shouldn't call this method directly. Sends a trap to the 
     * specified address.
     * @param peer The peer to which the trap will be sent.
     * @param pdu The trap pdu.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *            by <CODE>bufferSize</CODE> or if a security error occurred 
     *            when sending the trap. 
     *            See {@link com.sun.management.snmp.SnmpDefinitions} for error
     *            status.
     */
    public void snmpV3Trap(SnmpUsmPeer peer, 
			   SnmpScopedPduRequest pdu) 
        throws IOException, SnmpStatusException {
	if(peer != null) {
	    pdu.port = peer.getDestPort();
	    snmpV3Trap(peer.getDestAddr(), pdu);
	}
	else {
	    pdu.port = getTrapPort().intValue();
	    snmpV3Trap((InetAddress)null, pdu);
	}
    }
    /**
     * You shouldn't call this method directly. Sends a trap to the specified 
     * address.
     * @param addr The trap address destination.
     * @param pdu The trap pdu.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *    by <CODE>bufferSize</CODE> or if a security error occurred when 
     *    sending the trap. Have a look to 
     *    {@link com.sun.management.snmp.SnmpDefinitions} for error status.
     */
    public void snmpV3Trap(InetAddress addr, SnmpScopedPduRequest pdu) 
        throws IOException, SnmpStatusException {
        SnmpOutgoingRequest req = null;
        SnmpMsgProcessingSubSystem msgProcSubSys = 
            engine.getMsgProcessingSubSystem();
	try {
	    req = msgProcSubSys.getOutgoingRequest(
		  SnmpDefinitions.snmpVersionThree, getPduFactory());
	}
        catch(SnmpUnknownMsgProcModelException x) {
            if (logger.finestOn()) {
                logger.finest("snmpV3Trap", "Unknown Msg Processing Model:" +
			      SnmpDefinitions.snmpVersionThree);
            }
            throw new SnmpStatusException(snmpReqUnknownError);
        }
        SnmpMsg msg = prepareMsg(req, pdu);
	
        if (msg == null) return;
	
	if(addr != null) {
	    msg.address = addr;
	    try {
		sendTrapMessage(req, msg);
	    }
	    catch (SnmpTooBigException x) {
		if (logger.finestOn()) {
		    logger.finest("snmpV3Trap", "trap pdu is too big");
		    logger.finest("snmpV3Trap", "trap hasn't been sent to " + 
				  msg.address);
		}
		closeTrapSocketIfNeeded();
		throw new SnmpStatusException(SnmpDefinitions.snmpRspTooBig);
	    }
	}
	else {
	    int sendingCount = 0 ;
	    openTrapSocketIfNeeded() ;
	    if (getInetAddressAcl() != null) {
		Enumeration ed = getInetAddressAcl().getTrapDestinations() ;
		while (ed.hasMoreElements()) {
		    msg.address = (InetAddress)ed.nextElement() ;
		    try {
			sendTrapMessage(req, msg) ;
			sendingCount++ ;
		    }
		    catch (SnmpTooBigException x) {
			if (logger.finestOn()) {
			    logger.finest("snmpV3Trap", "trap pdu is too big");
			    logger.finest("snmpV3Trap", 
					  "trap hasn't been sent to " + 
					  msg.address);
			    closeTrapSocketIfNeeded();
			    throw new SnmpStatusException(
				      SnmpDefinitions.snmpRspTooBig);
			}
		    }
		}
	    }
	    // If there is no destination defined or if everything has failed
	    // we tried to send the trap to the local host (as suggested by
	    // mister Olivier Reisacher).
	    //
	    if (sendingCount == 0) {
		try {
		    msg.address = InetAddress.getLocalHost() ;
		    sendTrapMessage(req, msg) ;
		}
		catch (SnmpTooBigException x) {
		    if (logger.finestOn()) {
			logger.finest("snmpV3Trap", "trap pdu is too big");
			logger.finest("snmpV3Trap", "trap hasn't been sent");
		    }
		    closeTrapSocketIfNeeded();
		    throw new SnmpStatusException(
			      SnmpDefinitions.snmpRspTooBig);
		}
		catch (UnknownHostException e) {
		    if (logger.finestOn()) {
			logger.finest("snmpV3Trap", 
				      "cannot get the local host");
			logger.finest("snmpV3Trap", "trap hasn't been sent");
		    }
		    throw new SnmpStatusException(snmpReqUnknownError);
		}
	    }
	}
	closeTrapSocketIfNeeded();
    }
    /**
     * You shouldn't call this method directly. Sends a trap to all IP 
     * addresses in <CODE>InetAddressAcl</CODE>. 
     * @param pdu The trap pdu.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *     by <CODE>bufferSize</CODE> or if a security error occurred when 
     *     sending the trap. Have a look to 
     *     {@link com.sun.management.snmp.SnmpDefinitions} for error status.
     */
    public void snmpV3Trap(SnmpScopedPduRequest pdu)
        throws IOException, SnmpStatusException {
        snmpV3Trap((InetAddress)null, pdu);
    }
    /**
     * Sends a trap to all IP addresses in <CODE>InetAddressAcl</CODE> 
     * using Usm as security model. Traps are time stamped with the current time.
     * @param principal The user name to use.
     * @param securityLevel The V3 security level.
     * @param contextName The V3 context name. The 
     *    <CODE>contextEngineId</CODE> is the local engine Id.
     * @param trapOid The OId identifying the trap.
     * @param list A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined
     *    by <CODE>bufferSize</CODE> or if a security error occurred when 
     *    sending the trap. Have a look to 
     *    {@link com.sun.management.snmp.SnmpDefinitions} for error status.
     */
    public void snmpV3UsmTrap(String principal,
                              int securityLevel,
                              String contextName,
			      SnmpOid trapOid,
                              SnmpVarBindList list) 
        throws IOException, SnmpStatusException {
	byte[] ctx = contextName == null ? null : contextName.getBytes();
        SnmpScopedPduRequest pdu = 
	    createScopedUsmPdu(principal, securityLevel, ctx,
			       engine.getEngineId(), engine.getEngineBoots(),
			       engine.getEngineTime(), list, trapOid, 
			       null, getTrapPort().intValue());
        snmpV3Trap(pdu);
    }

    /**
     * Sends a trap to the specified address using Usm as security model. 
     * Traps are time stamped with the current time.
     * @param addr The trap address destination.
     * @param principal The user name to use.
     * @param securityLevel The V3 security level.
     * @param contextName The V3 context name. The 
     *        <CODE>contextEngineId</CODE> is the local engine Id.
     * @param trapOid The OId identifying the trap.
     * @param list A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *    by <CODE>bufferSize</CODE> or if a security error occurred when 
     *    sending the trap. Have a look to 
     *    {@link com.sun.management.snmp.SnmpDefinitions} for error status.
     */
    public void snmpV3UsmTrap(InetAddress addr, 
                              String principal,
                              int securityLevel,
                              String contextName,
			      SnmpOid trapOid,
                              SnmpVarBindList list) 
        throws IOException, SnmpStatusException {
	byte[] ctx = contextName == null ? null : contextName.getBytes();
        SnmpScopedPduRequest pdu = 
	    createScopedUsmPdu(principal, securityLevel, ctx,
			       engine.getEngineId(), engine.getEngineBoots(),
			       engine.getEngineTime(), list, trapOid, 
			       null, getTrapPort().intValue());
        snmpV3Trap(addr, pdu);
    }
    /**
     * Sends a trap to the specified address using Usm as security model. 
     * Traps are time stamped with the current time.
     * @param addr The trap address destination.
     * @param principal The user name to use.
     * @param securityLevel The V3 security level.
     * @param contextName The V3 context name.
     *    The <CODE>contextEngineId</CODE> is the local engine Id.
     * @param trapOid The OID identifying the trap.
     * @param list A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @param time The time stamp (overwrite the current time).
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *     by <CODE>bufferSize</CODE> or if a security error occurred when 
     *     sending the trap. Have a look to 
     *     {@link com.sun.management.snmp.SnmpDefinitions} for error status.
     */
    public void snmpV3UsmTrap(InetAddress addr, 
                              String principal,
                              int securityLevel,
                              String contextName,
			      SnmpOid trapOid,
                              SnmpVarBindList list,
			      SnmpTimeticks time) 
        throws IOException, SnmpStatusException {
	byte[] ctx = contextName == null ? null : contextName.getBytes();
        SnmpScopedPduRequest pdu = 
	    createScopedUsmPdu(principal, securityLevel, ctx,
			       engine.getEngineId(), engine.getEngineBoots(),
			       engine.getEngineTime(), list, trapOid, 
			       time, getTrapPort().intValue());
        snmpV3Trap(addr, pdu);
    }

    /**
     * Sends a trap to the specified <CODE>SnmpUsmPeer</CODE> using Usm as 
     * security model.
     * Security parameters and context name used are the one located in 
     * SnmpUsmPeer. Be sure to update them. 
     * NO Usm discovery is needed when sending trap.
     * @param peer The trap destination.
     * @param trapOid The OID identifying the trap.
     * @param list A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @param time The time stamp (overwrite the current time).
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *    by <CODE>bufferSize</CODE>.
     * @exception IllegalArgumentException If no 
     *    <CODE>SnmpUsmParameters</CODE> are set.
     */
    public void snmpV3UsmTrap(SnmpUsmPeer peer,
			      SnmpOid trapOid,
			      SnmpVarBindList list,
			      SnmpTimeticks time) 
	throws IOException, SnmpStatusException, IllegalArgumentException {
	SnmpUsmParameters p = (SnmpUsmParameters) peer.getParams();
	if(p == null) 
	    throw new IllegalArgumentException("No Usm parameters set");

	SnmpScopedPduRequest pdu = createScopedUsmPdu(p.getPrincipal(),
                                                      p.getSecurityLevel(),
                                                      p.getContextName(),
                                                      engine.getEngineId(),
                                                      engine.getEngineBoots(),
                                                      engine.getEngineTime(),
                                                      list, 
						      trapOid, 
						      time,
						      peer.getDestPort());
	
	snmpV3Trap(peer.getDestAddr(), pdu);
    }

    // SENDING SNMP INFORMS STUFF
    //---------------------------
    /**
     * Sends an inform request using SNMP V3 inform request format.
     * <BR>The inform is sent to the specified <CODE>InetAddress</CODE> 
     * destination.
     * The inform is sent in an SNMP V3 message.
     * Engine Id of the inform receiver will be discovered using Snmp V3 
     * engine Id discovery.
     * <BR>The variable list included in the outgoing inform is composed of 
     * the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value</LI>
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by 
     *    <CODE>trapOid</CODE></LI>
     * <LI><CODE>all the (oid,values)</CODE> from the specified 
     *    <CODE>varBindList</CODE></LI>
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
     * @exception IllegalStateException  This method has been invoked while 
     *    the SNMP adaptor server was not active or if the provided peer is 
     *    not usable with informs (no engine id discovery done).
     * @exception IOException An I/O error occurred while sending the inform 
     *    request.
     * @exception SnmpStatusException If the inform request exceeds the 
     *    limit defined by <CODE>bufferSize</CODE>.
     */
    public SnmpV3InformRequest snmpV3UsmInformRequest(SnmpUsmPeer informPeer,
                                                      SnmpInformHandler cb,
                                                      SnmpOid trapOid, 
                                                      SnmpVarBindList list) 
        throws IllegalStateException, IOException, SnmpStatusException {
        return usmInformRequest(informPeer,
				cb,
				trapOid,
				list);
    }
    
    private SnmpV3InformRequest usmInformRequest(SnmpUsmPeer peer,
						 SnmpInformHandler cb,
						 SnmpOid trapOid, 
						 SnmpVarBindList list) 
	throws IllegalStateException, IllegalArgumentException, 
	       IOException, SnmpStatusException {
	if (!isActive()) {
            throw new IllegalStateException("Start SNMP adaptor server "+ 
				     "before carrying out this operation");
        }
	
	if(peer.isAuthoritative()) 
	    throw new SnmpStatusException(
	       "SnmpUsmPeer is an authoritative one. Not usable for informs.");
	
	SnmpUsmParameters p = (SnmpUsmParameters) peer.getParams();
	
	SnmpScopedPduRequest pdu = createScopedUsmPdu(p.getPrincipal(),
						      p.getSecurityLevel(),
						      p.getContextName(),
						      peer.getEngineId(),
						      peer.getEngineBoots(),
						      peer.getEngineTime(),
						      list, 
						      null, 
						      null, 
						      peer.getDestPort());
	
	SnmpV3InformRequest req = snmpV3InformRequest(peer.getDestAddr(),
						      cb,
						      pdu,
						      trapOid, 
						      list);
	return req;
    }
    
    /**
     * Sends an inform using SNMP V3 inform request format.
     * <BR>The inform request is sent to each destination defined in the ACL 
     * file (if available).
     * If no ACL file or no destinations are available, the inform request is 
     * sent to the local host.
     * <BR>The variable list included in the outgoing inform is composed of 
     * the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value</LI>
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by 
     *    <CODE>trapOid</CODE></LI>
     * <LI><CODE>all the (oid,values)</CODE> from the specified
     *    <CODE>varBindList</CODE></LI>
     * </UL>
     * To send an inform request, the SNMP adaptor server must be active.
     * 
     * @param cb The callback that is invoked when a request is complete.
     * @param pdu The inform SNMP V3 scoped pdu.
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @return A vector of {@link com.sun.management.comm.SnmpInformRequest} objects.
     * <P>If there is no destination host for this inform request, the 
     * returned vector will be empty.
     *
     * @exception IllegalStateException  This method has been invoked while 
     *    the SNMP adaptor server was not active.
     * @exception IOException An I/O error occurred while sending the inform 
     *    request.
     * @exception SnmpStatusException If the inform request exceeds the 
     *    limit defined by <CODE>bufferSize</CODE>.
     */
    private Vector snmpV3InformRequest(SnmpInformHandler cb, 
                                       SnmpScopedPduRequest pdu,
                                       SnmpOid trapOid, 
                                       SnmpVarBindList varBindList) 
        throws IllegalStateException, IOException, SnmpStatusException {

        //CODED BUT NOT Fit. We should provide a list of SnmpEngineId to 
	// stick with IpAcl Addresses. The main difference with traps is 
	// that the authoritative engine Id is the MANAGER...

        if (!isActive()) {
            throw new IllegalStateException(
            "Start SNMP adaptor server before carrying out this operation");
        }
        //Instantiate the session in a lazy way.
        if(sessionV3 == null)
            sessionV3 = new SnmpV3Session(this);

        if (logger.finerOn()) {
            logger.finer("snmpV3InformRequest", "trapOid=" + trapOid);
        }
        
        // First, make an SNMP inform pdu:
        // We clone varBindList and insert sysUpTime and snmpTrapOid variables.
        //
        SnmpVarBindList fullVbl ;    
        if (varBindList != null)
            fullVbl = (SnmpVarBindList)varBindList.clone() ;
        else
            fullVbl = new SnmpVarBindList(2) ;
        SnmpTimeticks sysUpTimeValue = new SnmpTimeticks(getSysUpTime()) ;
        fullVbl.insertElementAt(new SnmpVarBind(snmpTrapOidOid, trapOid), 0) ;
        fullVbl.insertElementAt(new SnmpVarBind(sysUpTimeOid, sysUpTimeValue),
				0);
        
        // Next, send the pdu to the specified destination
        //
        openInformSocketIfNeeded() ;
        
        // Now send the SNMP message to each destination
        //
        Vector informReqList = new Vector();
        int sendingCount = 0;
        InetAddress addr = null;
        //String cs = null;
        if (getInetAddressAcl() != null) {
            Enumeration ed = (getInetAddressAcl()).getInformDestinations() ;
            while (ed.hasMoreElements()) {
                addr = (InetAddress)ed.nextElement() ;
                //Enumeration ec = ((IPAcl)ipacl).getInformCommunities(addr) ;
                //while (ec.hasMoreElements()) {
                //cs = (String)ec.nextElement() ;
                informReqList.addElement(sessionV3.makeAsyncRequest(addr, 
							  pdu, cb, fullVbl)) ;
                sendingCount++;
                //}
            }
        }
        if (sendingCount == 0)
            informReqList.addElement(sessionV3.makeAsyncRequest(
			    InetAddress.getLocalHost(), pdu, cb, fullVbl));
	
        return informReqList ;
    }
    
    /**
     * You shouldn't use this method directly.
     * Sends an inform using SNMP V3 inform request format.
     * <BR>The inform is sent to the specified <CODE>InetAddress</CODE> 
     * destination.
     * The inform is sent in a SNMP V3 message.
     * <BR>The variable list included in the outgoing inform is composed of 
     * the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value</LI>
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by 
     *    <CODE>trapOid</CODE></LI>
     * <LI><CODE>all the (oid,values)</CODE> from the specified 
     *    <CODE>varBindList</CODE></LI>
     * </UL>
     * To send an inform request, the SNMP adaptor server must be active.
     * 
     * @param addr The <CODE>InetAddress</CODE> destination for this 
     *     inform request.
     * @param pdu The scoped Pdu used when sending the inform.
     * @param cb The callback that is invoked when a request is complete.
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @return The inform request object.
     *
     * @exception IllegalStateException  This method has been invoked while
     *    the SNMP adaptor server was not active.
     * @exception IOException An I/O error occurred while sending the 
     *    inform request.
     * @exception SnmpStatusException If the inform request exceeds 
     *    the limit defined by <CODE>bufferSize</CODE>.
     */
    public SnmpV3InformRequest snmpV3InformRequest(InetAddress addr, 
						   SnmpInformHandler cb, 
						   SnmpScopedPduRequest pdu,
						   SnmpOid trapOid, 
						   SnmpVarBindList varBindList)
        throws IllegalStateException, IOException, SnmpStatusException {

        if (!isActive()) {
            throw new IllegalStateException("Start SNMP adaptor server "+
				      "before carrying out this operation");
        }

        //Instantiate the session in a lazy way.
        if(sessionV3 == null)
            sessionV3 = new SnmpV3Session(this);

        if (logger.finerOn()) {
            logger.finer("snmpV3InformRequest", "trapOid=" + trapOid);
        }
        
        // First, make an SNMP inform pdu:
        // We clone varBindList and insert sysUpTime and snmpTrapOid variables.
        //
        SnmpVarBindList fullVbl ;    
        if (varBindList != null)
            fullVbl = (SnmpVarBindList)varBindList.clone() ;
        else
            fullVbl = new SnmpVarBindList(2) ;
        SnmpTimeticks sysUpTimeValue = new SnmpTimeticks(getSysUpTime()) ;
        fullVbl.insertElementAt(new SnmpVarBind(snmpTrapOidOid, trapOid), 0) ;
        fullVbl.insertElementAt(new SnmpVarBind(sysUpTimeOid, sysUpTimeValue),
				0);
                
        // Next, send the pdu to the specified destination
        //
        openInformSocketIfNeeded() ;
        return sessionV3.makeAsyncRequest(addr, pdu, cb, fullVbl) ;
    }

    //Instantiate and fill the pdu to send in trap or inform.
    //
    private SnmpScopedPduRequest createScopedUsmPdu(String principal,
                                                    int securityLevel,
                                                    byte[] contextName,
                                                    SnmpEngineId authId,
                                                    int boot,
                                                    int time,
                                                    SnmpVarBindList list,
						    SnmpOid trapOid,
						    SnmpTimeticks timeticks, 
						    int port) 
	throws SnmpStatusException {
        SnmpUsm usm = null;
        try {
            usm = (SnmpUsm) 
                engine.getSecuritySubSystem().getModel(SnmpUsm.ID);
        }catch(SnmpUnknownModelException e) {
	    if(logger.finestOn()) {
		logger.finest("createScopedUsmPdu", 
			      "SnmpUnknownModelException", e);
	    }
	    throw new SnmpStatusException(snmpReqUnknownError);
        }
        //Create the security parameters
        SnmpUsmSecurityParameters params = usm.createUsmSecurityParameters();
        //Fill the security parameters
        params.setAuthoritativeEngineId(authId);
        params.setAuthoritativeEngineBoots(boot);
        params.setAuthoritativeEngineTime(time);
        params.setUserName(principal);

        //Instantiate the PDU
        SnmpScopedPduRequest req = new SnmpScopedPduRequest();
	req.port = port;
        req.version =  SnmpDefinitions.snmpVersionThree;
        req.type = SnmpDefinitions.pduV2TrapPdu;
        req.msgFlags = (byte) securityLevel;
        req.msgSecurityModel = SnmpUsm.ID;
        req.msgMaxSize = bufferSize;
        req.contextEngineId = authId.getBytes();
        req.contextName = contextName;
        req.securityParameters = params;

        SnmpTimeticks sysUpTimeValue = null;
	
        if(timeticks != null)
            sysUpTimeValue = timeticks;
        else
            sysUpTimeValue = new SnmpTimeticks(getSysUpTime());
	
	SnmpVarBindList fullVbl = completeTrapVarBindList(list, 
							  trapOid,
							  sysUpTimeValue);
	
	//Set the updated List.
	req.varBindList = new SnmpVarBind[fullVbl.size()];
        fullVbl.copyInto(req.varBindList);
	
        return req;
    }

    /**
     * Send the specified message on trapSocket.
     */
    private void sendTrapMessage(SnmpOutgoingRequest req,
                                 SnmpMsg msg) 
        throws IOException, SnmpTooBigException, SnmpStatusException {
        byte[] buffer = new byte[bufferSize] ;
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        int encodingLength = 0;
        try {
            encodingLength = req.encodeMessage(buffer);    
	}catch(SnmpSecurityException e) {
	    if(logger.finerOn()) 
		logger.finer("sendTrapMessage", e.toString());
	    throw new SnmpStatusException(e.status);
        }
        catch(SnmpUnknownSecModelException e) {
	    if(logger.finerOn()) 
		logger.finer("sendTrapMessage", e.toString());
            throw new SnmpStatusException(e.toString());
        }
        catch(SnmpBadSecurityLevelException e) {
	    if(logger.finerOn()) 
		logger.finer("sendTrapMessage", e.toString());
            throw new SnmpStatusException(snmpBadSecurityLevel);
        }
        packet.setLength(encodingLength) ;
        packet.setAddress(msg.address) ;
        packet.setPort(msg.port) ;
        if (logger.finerOn()) {
            logger.finer("sendTrapMessage", "sending trap to " + 
			 msg.address + ":" + msg.port);
        }
        trapSocket.send(packet) ;
        if (logger.finerOn()) {
            logger.fine("sendTrapMessage", "trap sent to " + 
			msg.address + ":" + msg.port);
        }
        snmpOutTraps++;
        snmpOutPkts++;
    }
    private SnmpMsg prepareMsg(SnmpOutgoingRequest req, SnmpPdu pdu) 
        throws IOException, SnmpStatusException {
	
        // Make an SNMP message from the pdu
        //
        SnmpPduFactory pduFactory = getPduFactory();
	    
        // Ask the SnmpOutgoingRequest object to encode the pdu using 
	// the provided factory. 
        SnmpMsg msg = null;
        try {
            msg = req.encodeSnmpPdu(pdu, 
                                    bufferSize);
        }catch (SnmpTooBigException ar) {
            if (logger.finestOn()) {
                logger.finest("prepareMsg", ar);
            }
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspTooBig);
        }
        if (msg == null) {
            if (logger.finestOn()) {
                logger.finest("prepareMsg", 
			      "pdu factory returned a null value");
            }
            throw new SnmpStatusException(snmpReqUnknownError);
            // This exception will caught hereafter and reported as an 
	    // snmpReqUnknownError
            // FIXME: may be it's not the best behavior ?
        }
	
        byte[] encoding = new byte[bufferSize];
        //int encodingLength = msg.encodeMessage(encoding) ;
        int encodingLength = 0;
        try {
            encodingLength = req.encodeMessage(encoding);
        }catch (SnmpTooBigException e) {
            if(logger.finestOn()) {
                logger.finest("prepareMsg", e.toString());
            }
            throw new SnmpStatusException(SnmpDefinitions.snmpRspTooBig) ;
        }
        catch(SnmpSecurityException e) {
	    if(logger.finerOn()) 
		logger.finer("prepareMsg", e.toString());
	    throw new SnmpStatusException(e.status);
        }
        catch(SnmpUnknownSecModelException e) {
            if(logger.finestOn()) {
                logger.finest("prepareMsg", e.toString());
            }
            throw new SnmpStatusException(snmpReqUnknownError);
        }
        catch(SnmpBadSecurityLevelException e) {
	    if(logger.finerOn()) 
		logger.finer("prepareMsg", e.toString());
            throw new SnmpStatusException(snmpBadSecurityLevel);   
        }
        openTrapSocketIfNeeded();
        return msg;
    }
    
    /**
     * This method is called by the adaptor. It returns an object name used 
     * when registering the MIB.
     * @param engine The SNMP engine.
     * @return The object name.
     */
    protected ObjectName createUsmMibObjectName(SnmpEngine engine) 
	throws MalformedObjectNameException{
	return new ObjectName(engine.getEngineId().toString() + 
			      "/SNMP_USER_BASED_SM_MIB:" +
			      "name=Usm-MIB-RFC2574");
    }

    /**
     * Method called internally.
     * @param engine The engine.
     */
    private void createUsmMib(SnmpEngineImpl engine) 
	throws IllegalArgumentException {
	SnmpUsm usm = null;
	SnmpUsmLcd usmlcd = null;
	try {
	    usm = 
		(SnmpUsm) engine.getSecuritySubSystem().getModel(SnmpUsm.ID);
	    
	    usmlcd = usm.getLcd();
	    
	} catch(SnmpUnknownModelException e) {
	    if(logger.finerOn())
		logger.finer("init", "The Usm can't be added, no Usm model: "+
			     e);
	    throw new IllegalArgumentException(e.toString());
	}
	
	usmmib = new SNMP_USER_BASED_SM_MIBImpl(engine,
						usmlcd,
						usm);
    }

    // PACKAGE METHODS
    //----------------

    /**
     * Returns the string used in logging.
     */
    String makeDebugTag() {
        return "SnmpV3AdaptorServer["+ getProtocol() + ":" + 
	    getPort() + "]";    
    }
}
