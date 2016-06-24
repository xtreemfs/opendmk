/*
 * @(#)file      SnmpAdaptorServer.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.109
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
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpPduRequest;
import com.sun.management.snmp.SnmpPduTrap;
import com.sun.management.snmp.SnmpTimeticks;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.InetAddressAcl;
import com.sun.management.snmp.manager.SnmpPeer;
import com.sun.management.snmp.manager.SnmpParameters;
// jdmk imports
//
import com.sun.management.snmp.SnmpPduFactoryBER;
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpMibHandler;
import com.sun.management.snmp.agent.SnmpUserDataFactory;
import com.sun.management.snmp.agent.SnmpErrorHandlerAgent;
import com.sun.management.comm.CommunicatorServer;

import com.sun.management.snmp.IPAcl.JdmkAcl;

import com.sun.jdmk.internal.ThreadService;
import com.sun.jdmk.internal.ModifiableClassLogger;
import com.sun.jdmk.internal.ClassLogger;

import com.sun.jdmk.comm.CommunicationException;

/**
 * Implements an adaptor on top of the SNMP protocol.
 * <P>
 * When this SNMP protocol adaptor is started it creates a datagram socket 
 * and is able to receive requests and send traps or inform requests. 
 * When it is stopped, the socket is closed and neither requests 
 * and nor traps/inform request are processed.
 * <P>
 * The default port number of the socket is 161. This default value can be 
 * changed by specifying a port number:
 * <UL>
 * <LI>in the object constructor</LI>
 * <LI>using the {@link CommunicatorServer#setPort setPort} method before 
 *     starting the adaptor</LI>
 * </UL>
 * The default object name is define by {@link 
 * com.sun.jdmk.ServiceName#DOMAIN com.sun.jdmk.ServiceName.DOMAIN} 
 * and {@link com.sun.jdmk.ServiceName#SNMP_ADAPTOR_SERVER 
 * com.sun.jdmk.ServiceName.SNMP_ADAPTOR_SERVER}.
 * <P>
 * The SNMP protocol adaptor supports versions 1 and 2 of the SNMP protocol 
 * in a stateless way:
 * when it receives a v1 request, it replies with a v1 response, 
 * when it receives a v2 request it replies with a v2 response. 
 * <BR>The method {@link #snmpV1Trap snmpV1Trap} sends traps using SNMP v1 
 * format. 
 * The method {@link #snmpV2Trap snmpV2Trap} sends traps using SNMP v2 format.
 * The method {@link #snmpInformRequest snmpInformRequest} sends inform 
 * requests using SNMP v2 format.
 * <P>
 * To receive data packets, the SNMP protocol adaptor uses a buffer whose 
 * size can be configured using the property 
 * <CODE>bufferSize</CODE> (default value is 1024). 
 * Packets which do not fit into the buffer are rejected. 
 * Increasing <CODE>bufferSize</CODE> allows the exchange of bigger packets. 
 * However, the underlying networking system may impose a limit on the size 
 * of UDP packets. 
 * Packets bigger than this limit will be rejected, no matter what the value 
 * of <CODE>bufferSize</CODE>.
 * <P>
 * An SNMP protocol adaptor may serve several managers concurrently. The 
 * number of concurrent managers can be limited using the property 
 * <CODE>maxActiveClientCount</CODE>.
 * <p>
 * The SNMP protocol adaptor specifies a default value (10) for the
 * <CODE>maxActiveClientCount</CODE> property. When the adaptor is stopped,
 * the active requests are interrupted and an error result is sent to the 
 * managers.
 *
 * @since Java DMK 5.1
 */

public class SnmpAdaptorServer extends CommunicatorServer 
    implements SnmpAdaptorServerMBean, MBeanRegistration, SnmpDefinitions, 
               SnmpMibHandler {
    
    // PRIVATE VARIABLES
    //------------------
    
    /**
     * Port number for sending SNMP traps.
     * <BR>The default value is 162.
     */
    private int                 trapPort = 162;

    /**
     * Port number for sending SNMP inform requests.
     * <BR>The default value is 162.
     */
    private int                 informPort = 162;

    /**
     * The <CODE>InetAddress</CODE> used when creating the datagram socket.
     * <BR>It is specified when creating the SNMP protocol adaptor.
     * If not specified, the local host machine is used.
     */
    InetAddress         address = null;

    /**
     * The IP address based ACL used by this SNMP protocol adaptor.
     */
    private Object               ipacl = null;

    /**
     * The factory object.
     */
    private SnmpPduFactory      pduFactory = null;

    /**
     * The user-data factory object.
     */
    private SnmpUserDataFactory userDataFactory = null;

    /**
     * Indicates if the SNMP protocol adaptor sends a response in case of 
     * authentication failure
     */
    private boolean             authRespEnabled = true;
    /**
     * Indicates if authentication traps are enabled.
     */
    private boolean             authTrapEnabled = true;
    /**
     * The enterprise OID.
     * <BR>The default value is "1.3.6.1.4.1.42".
     */
    private SnmpOid             enterpriseOid   = 
        new SnmpOid("1.3.6.1.4.1.42");
    
    /**
     * The buffer size of the SNMP protocol adaptor.
     * This buffer size is used for both incoming request and outgoing 
     * inform requests.
     * <BR>The default value is 1024.
     */
    int bufferSize = 1024;
    
    private transient long            startUpTime     = 0;
    
    // The synchronization in CommunicatorServer and its subclasses should be
    // revisited - declaring socket as a volatile object is a temporary 
    // fix that reduces the possibility of race conditions during start/stop
    // sequences and ensures that changes to socket are always seen.
    //
    private transient volatile DatagramSocket  socket = null;
    
    transient DatagramSocket  trapSocket      = null;
    private transient SnmpSession     informSession   = null;
    private transient DatagramPacket  packet          = null;
    transient Vector          mibs            = new Vector();
    private transient SnmpMibTree     root;

    /**
     * Used by children.
     */
    private transient boolean         useAcl = true;

    
    // SENDING SNMP INFORMS STUFF
    //---------------------------
      
    /**
     * Number of times to try an inform request before giving up.
     * The default number is 3.
     */
    private int maxTries = 3 ;

    /**
     * The amount of time to wait for an inform response from the manager.
     * The default amount of time is 3000 milliseconds.
     */
    private int timeout = 3 * 1000 ;
        
    // VARIABLES REQUIRED FOR IMPLEMENTING SNMP GROUP (MIBII)
    //-------------------------------------------------------
    
    /**
     * The <CODE>snmpOutTraps</CODE> value defined in MIB-II.
     */
    int snmpOutTraps=0;

    /**
     * The <CODE>snmpOutGetResponses</CODE> value defined in MIB-II.
     */
    private int snmpOutGetResponses=0;

    /**
     * The <CODE>snmpOutGenErrs</CODE> value defined in MIB-II.
     */
    private int snmpOutGenErrs=0;

    /**
     * The <CODE>snmpOutBadValues</CODE> value defined in MIB-II.
     */
    private int snmpOutBadValues=0;

    /**
     * The <CODE>snmpOutNoSuchNames</CODE> value defined in MIB-II.
     */
    private int snmpOutNoSuchNames=0;

    /**
     * The <CODE>snmpOutTooBigs</CODE> value defined in MIB-II.
     */
    private int snmpOutTooBigs=0;

    /**
     * The <CODE>snmpOutPkts</CODE> value defined in MIB-II.
     */
    int snmpOutPkts=0;

    /**
     * The <CODE>snmpInASNParseErrs</CODE> value defined in MIB-II.
     */
    private int snmpInASNParseErrs=0;

    /**
     * The <CODE>snmpInBadCommunityUses</CODE> value defined in MIB-II.
     */
    private int snmpInBadCommunityUses=0;

    /**
     * The <CODE>snmpInBadCommunityNames</CODE> value defined in MIB-II.
     */
    private int snmpInBadCommunityNames=0;

    /**
     * The <CODE>snmpInBadVersions</CODE> value defined in MIB-II.
     */
    private int snmpInBadVersions=0;

    /**
     * The <CODE>snmpInGetRequests</CODE> value defined in MIB-II.
     */
    private int snmpInGetRequests=0;

    /**
     * The <CODE>snmpInGetNexts</CODE> value defined in MIB-II.
     */
    private int snmpInGetNexts=0;

    /**
     * The <CODE>snmpInSetRequests</CODE> value defined in MIB-II.
     */
    private int snmpInSetRequests=0;

    /**
     * The <CODE>snmpInPkts</CODE> value defined in MIB-II.
     */
    private int snmpInPkts=0;

    /**
     * The <CODE>snmpInTotalReqVars</CODE> value defined in MIB-II.
     */
    private int snmpInTotalReqVars=0;

    /**
     * The <CODE>snmpInTotalSetVars</CODE> value defined in MIB-II.
     */
    private int snmpInTotalSetVars=0;

    /**
     * The <CODE>snmpInTotalSetVars</CODE> value defined in rfc 1907 MIB-II.
     */
    private int snmpSilentDrops=0;
    
    private static final String InterruptSysCallMsg = 
        "Interrupted system call";
    static final SnmpOid sysUpTimeOid = 
        new SnmpOid("1.3.6.1.2.1.1.3.0") ;
    static final SnmpOid snmpTrapOidOid =
        new SnmpOid("1.3.6.1.6.3.1.1.4.1.0") ;
    
    
    private ThreadService threadService;

    private static int threadNumber = 6;
    private final static String THREAD_NUMBER_PROPERTY = 
        "com.sun.management.snmp.threadnumber";

    private SnmpMibAgent defaultAgent = null;

    static {
        String s = System.getProperty(THREAD_NUMBER_PROPERTY);

        if (s != null) {
            try {
                threadNumber = Integer.parseInt(s);
            } catch (Exception e) {
                // ???
                System.err.println("Got wrong value for " + 
                                   THREAD_NUMBER_PROPERTY+": "+s);
                System.err.println("Using the default value: "+threadNumber);
            }
        }
    }

    // PUBLIC CONSTRUCTORS
    //--------------------
    
    /**
     * Initializes this SNMP protocol adaptor using the default port (161).
     * Use the {@link com.sun.management.snmp.IPAcl.JdmkAcl} default 
     * implementation of the <CODE>InetAddressAcl</CODE> interface.
     */
    public SnmpAdaptorServer() {
        this(null, com.sun.jdmk.ServiceName.SNMP_ADAPTOR_PORT, null) ;
    }

    /**
     * Initializes this SNMP protocol adaptor using the specified port.
     * Use the {@link com.sun.management.snmp.IPAcl.JdmkAcl} default 
     * implementation of the <CODE>InetAddressAcl</CODE> interface.
     *
     * @param port The port number for sending SNMP responses.
     */
    public SnmpAdaptorServer(int port) {
        this(null, port, null) ;
    }

    /**
     * Initializes this SNMP protocol adaptor using the default port (161)
     * and the specified IP address based ACL implementation.
     *
     * @param acl The <CODE>InetAddressAcl</CODE> implementation.
     *
     */
    public SnmpAdaptorServer(InetAddressAcl acl) {
        this(acl, com.sun.jdmk.ServiceName.SNMP_ADAPTOR_PORT, null) ;
    }

    /**
     * Initializes this SNMP protocol adaptor using the default port (161) 
     * and the specified <CODE>InetAddress</CODE>.
     * Use the {@link com.sun.management.snmp.IPAcl.JdmkAcl} default implementation
     * of the <CODE>InetAddressAcl</CODE> interface.
     *
     * @param addr The IP address to bind.
     */
    public SnmpAdaptorServer(InetAddress addr) {
        this(null, com.sun.jdmk.ServiceName.SNMP_ADAPTOR_PORT, addr) ;
    }
    
    /**
     * Initializes this SNMP protocol adaptor using the specified port and the 
     * specified IP address based ACL implementation.
     *
     * @param acl The <CODE>InetAddressAcl</CODE> implementation.
     * @param port The port number for sending SNMP responses.
     *
     */
    public SnmpAdaptorServer(InetAddressAcl acl, int port) {
        this(acl, port, null) ;
    }
   
    /**
     * Initializes this SNMP protocol adaptor using the specified port and the 
     * specified <CODE>InetAddress</CODE>.
     * Use the {@link com.sun.management.snmp.IPAcl.JdmkAcl} default implementation 
     * of the <CODE>InetAddressAcl</CODE> interface.
     *
     * @param port The port number for sending SNMP responses.
     * @param addr The IP address to bind.
     */
    public SnmpAdaptorServer(int port, InetAddress addr) {
        this(null, port, addr) ;
    }
      
    /**
     * Initializes this SNMP protocol adaptor using the specified IP address 
     * based ACL implementation and the specified <CODE>InetAddress</CODE>.
     *
     * @param acl The <CODE>InetAddressAcl</CODE> implementation.
     * @param addr The IP address to bind.
     *
     */
    public SnmpAdaptorServer(InetAddressAcl acl, InetAddress addr) {
        this(acl, com.sun.jdmk.ServiceName.SNMP_ADAPTOR_PORT, addr) ;
    }

    /**
     * Initializes this SNMP protocol adaptor using the specified port, the 
     * specified  address based ACL implementation and the specified 
     * <CODE>InetAddress</CODE>.
     *
     * @param acl The <CODE>InetAddressAcl</CODE> implementation.
     * @param port The port number for sending SNMP responses.
     * @param addr The IP address to bind.
     *
     */
    public SnmpAdaptorServer(InetAddressAcl acl, int port, InetAddress addr) {
        super(CommunicatorServer.SNMP_TYPE) ;
        
        // Initialize the ACL implementation.
        //
        if (acl == null) {
            try {
                acl = (InetAddressAcl)
                    new JdmkAcl("SNMP protocol adaptor IP ACL");
            } catch (UnknownHostException e) {
                if (logger.finestOn()) {
                    logger.finest("constructor", 
                                  "UnknowHostException when creating ACL");
                    logger.finest("constructor", e);
                }
            }
        }

        init(acl, port, addr) ;
    }
      
    /**
     * Initializes this SNMP protocol adaptor using the specified port and the 
     * specified <CODE>InetAddress</CODE>.
     * This constructor allows to initialize an SNMP adaptor without using 
     * the ACL mechanism (by setting the <CODE>useAcl</CODE> parameter to 
     * false).
     * <br>This constructor must be used in particular with a platform that 
     * does not support the  <CODE>java.security.acl</CODE> package like pJava.
     *
     * @param useAcl Specifies if this new SNMP adaptor uses the ACL mechanism.
     * If the specified parameter is set to <CODE>true</CODE>, this 
     * constructor is equivalent to 
     * <CODE>SnmpAdaptorServer(int port, InetAddress addr)</CODE>.
     * @param port The port number for sending SNMP responses.
     * @param addr The IP address to bind.
     */
    public SnmpAdaptorServer(boolean useAcl, int port, InetAddress addr) {
        super(CommunicatorServer.SNMP_TYPE) ;
        
        // Initialize the ACL implementation if required.
        //
        this.useAcl = useAcl;
        Object acl = null;
        if (useAcl == true) {
            try {
                acl = (InetAddressAcl) 
                    new JdmkAcl("SNMP protocol adaptor IP ACL");
            } catch (UnknownHostException e) {
                if (logger.finestOn()) {
                    logger.finest("constructor", 
                                  "UnknowHostException when creating ACL");
                    logger.finest("constructor", e);
                }
            }
        }

        init(acl, port, addr) ;
    }

    // GETTERS AND SETTERS
    //--------------------

    /**
     * Gets the number of managers that have been processed by this SNMP 
     * protocol adaptor since its creation.
     *
     * @return The number of managers handled by this SNMP protocol adaptor
     * since its creation. This counter is not reset by the 
     * <CODE>stop</CODE> method.
     */
    public int getServedClientCount() {
        return super.getServedClientCount();
    }

    /**
     * Gets the number of managers currently being processed by this 
     * SNMP protocol adaptor.
     *
     * @return The number of managers currently being processed by this 
     * SNMP protocol adaptor.
     */
    public int getActiveClientCount() {
        return super.getActiveClientCount();
    }

    /**
     * Gets the maximum number of managers that this SNMP protocol adaptor can 
     * process concurrently.
     *
     * @return The maximum number of managers that this SNMP protocol adaptor 
     * can process concurrently.
     */
    public int getMaxActiveClientCount() {
        return super.getMaxActiveClientCount();
    }

    /**
     * Sets the maximum number of managers this SNMP protocol adaptor can 
     * process concurrently.
     *
     * @param c The number of managers.
     *
     * @exception java.lang.IllegalStateException This method has been invoked
     * while the communicator was <CODE>ONLINE</CODE> or <CODE>STARTING</CODE>.
     */
    public void setMaxActiveClientCount(int c) throws IllegalStateException {
        super.setMaxActiveClientCount(c);
    }
    
    /**
     * Returns the Ip address based ACL used by this SNMP protocol adaptor.
     * @return The <CODE>InetAddressAcl</CODE> implementation.
     *
     */
    public InetAddressAcl getInetAddressAcl() {
        return (InetAddressAcl)ipacl;   
    }

    /**
     * Returns the port used by this SNMP protocol adaptor for sending traps.
     * By default, port 162 is used.
     * 
     * @return The port number for sending SNMP traps.
     */
    public Integer getTrapPort() {
        return new Integer(trapPort) ;
    }
  
    /**
     * Sets the port used by this SNMP protocol adaptor for sending traps.
     *
     * @param port The port number for sending SNMP traps.
     */
    public void setTrapPort(Integer port) {
        int val= port.intValue() ;
        if (val < 0)
            throw new IllegalArgumentException(
                      "Trap port cannot be a negative value");
        trapPort= val ;
    }
  
    /**
     * Returns the port used by this SNMP protocol adaptor for sending 
     * inform requests.
     * By default, port 162 is used.
     * 
     * @return The port number for sending SNMP inform requests.
     */
    public int getInformPort() {
        return informPort;
    }
  
    /**
     * Sets the port used by this SNMP protocol adaptor for sending 
     * inform requests.
     *
     * @param port The port number for sending SNMP inform requests.
     */
    public void setInformPort(int port) {
        if (port < 0)
            throw new IllegalArgumentException(
                  "Inform request port cannot be a negative value");
        informPort= port ;
    }
    
    /**
     * Returns the protocol of this SNMP protocol adaptor.
     *
     * @return The string "snmp".
     */
    public String getProtocol() {
        return "snmp";
    }
  
    /**
     * Returns the buffer size of this SNMP protocol adaptor.
     * This buffer size is used for both incoming request and outgoing 
     * inform requests.
     * By default, buffer size 1024 is used.
     *
     * @return The buffer size.
     */
    public Integer getBufferSize() {
        return new Integer(bufferSize) ;
    }

    /**
     * Sets the buffer size of this SNMP protocol adaptor.
     * This buffer size is used for both incoming request and outgoing 
     * inform requests.
     *
     * @param s The buffer size.
     *
     * @exception java.lang.IllegalStateException This method has been invoked
     * while the communicator was <CODE>ONLINE</CODE> or <CODE>STARTING</CODE>.
     */
    public void setBufferSize(Integer s) throws IllegalStateException {
        if ((state == ONLINE) || (state == STARTING)) {
            throw new IllegalStateException(
                  "Stop server before carrying out this operation");
        }
        bufferSize = s.intValue() ;
    }
  
    /**
     * Gets the number of times to try sending an inform request before 
     * giving up.
     * By default, a maximum of 3 tries is used.
     * @return The maximum number of tries.
     */
    final public int getMaxTries() {
        return maxTries;
    }
    
    /**
     * Changes the maximum number of times to try sending an inform request 
     * before giving up.
     * @param newMaxTries The maximum number of tries.
     */
    final public synchronized void setMaxTries(int newMaxTries) {
        if (newMaxTries < 0)
            throw new IllegalArgumentException();
        maxTries = newMaxTries;
    }
    
    /**
     * Gets the timeout to wait for an inform response from the manager.
     * By default, a timeout of 3 seconds is used.
     * @return The value of the timeout property.
     */
    final public int getTimeout() {
        return timeout;
    }
    
    /**
     * Changes the timeout to wait for an inform response from the manager.
     * @param newTimeout The timeout (in milliseconds).
     */
    final public synchronized void setTimeout(int newTimeout) {
        if (newTimeout < 0)
            throw new IllegalArgumentException();
        timeout= newTimeout;
    }
    
    /**
     * Returns the message factory of this SNMP protocol adaptor.
     *
     * @return The factory object.
     */
    public SnmpPduFactory getPduFactory() {
        return pduFactory ;
    }
    
    /**
     * Sets the message factory of this SNMP protocol adaptor.
     *
     * @param factory The factory object (null means the default factory).
     */
    public void setPduFactory(SnmpPduFactory factory) {
        if (factory == null)
            pduFactory = new SnmpPduFactoryBER() ;
        else
            pduFactory = factory ;
    }
  
    /**
     * Set the user-data factory of this SNMP protocol adaptor.
     *
     * @param factory The factory object (null means no factory).
     * @see com.sun.management.snmp.agent.SnmpUserDataFactory
     */
    public void setUserDataFactory(SnmpUserDataFactory factory) {
        userDataFactory = factory ;
    }
  
    /**
     * Get the user-data factory associated with this SNMP protocol adaptor.
     *
     * @return The factory object (null means no factory).
     * @see com.sun.management.snmp.agent.SnmpUserDataFactory
     */
    public SnmpUserDataFactory getUserDataFactory() {
        return userDataFactory;
    }
  
    /**
     * Returns <CODE>true</CODE> if authentication traps are enabled.
     * <P>
     * When this feature is enabled, the SNMP protocol adaptor sends 
     * an <CODE>authenticationFailure</CODE> trap each time an authentication 
     * fails.
     * <P>
     * The default behavior is to send authentication traps.
     * 
     * @return <CODE>true</CODE> if authentication traps are enabled, 
     * <CODE>false</CODE> otherwise.
     */
    public boolean getAuthTrapEnabled() {
        return authTrapEnabled ;
    }
  
    /**
     * Sets the flag indicating if traps need to be sent in case of 
     * authentication failure.
     * 
     * @param enabled Flag indicating if traps need to be sent.
     */
    public void setAuthTrapEnabled(boolean enabled) {
        authTrapEnabled = enabled ;
    }

    /**
     * Returns <code>true</code> if this SNMP protocol adaptor sends a 
     * response in case of authentication failure.
     * <P>
     * When this feature is enabled, the SNMP protocol adaptor sends a 
     * response with <CODE>noSuchName</CODE> or <CODE>readOnly</CODE> when 
     * the authentication failed. If the flag is disabled, the SNMP protocol 
     * adaptor trashes the PDU silently.
     * <P>
     * The default behavior is to send responses.
     * 
     * @return <CODE>true</CODE> if responses are sent.
     */
    public boolean getAuthRespEnabled() {
        return authRespEnabled ;
    }

    /**
     * Sets the flag indicating if responses need to be sent in case of 
     * authentication failure.
     * 
     * @param enabled Flag indicating if responses need to be sent.
     */
    public void setAuthRespEnabled(boolean enabled) {
        authRespEnabled = enabled ;
    }
    
    /**
     * Returns the enterprise OID. It is used by 
     * {@link #snmpV1Trap snmpV1Trap} to fill the 'enterprise' field of 
     * the trap request.
     * 
     * @return The OID in string format "x.x.x.x".
     */
    public String getEnterpriseOid() {
        return enterpriseOid.toString() ;
    }

    /**
     * Sets the enterprise OID.
     *
     * @param oid The OID in string format "x.x.x.x".
     *
     * @exception IllegalArgumentException The string format is incorrect
     */
    public void setEnterpriseOid(String oid) throws IllegalArgumentException {
        enterpriseOid = new SnmpOid(oid) ;
    }
    
    /**
     * Returns the names of the MIBs available in this SNMP protocol adaptor.
     * 
     * @return An array of MIB names.
     */
    public String[] getMibs() {
        String[] result = new String[mibs.size()] ;
        int i = 0 ;
        for (Enumeration e = mibs.elements() ; e.hasMoreElements() ;) {
            SnmpMibAgent mib = (SnmpMibAgent)e.nextElement() ;
            result[i++] = mib.getMibName();
        }
        return result ;
    }
    
    // GETTERS FOR SNMP GROUP (MIBII)
    //-------------------------------
    
    /**
     * Returns the <CODE>snmpOutTraps</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutTraps</CODE> value.
     */
    public Long getSnmpOutTraps() {
        return new Long(snmpOutTraps);
    }
  
    /**
     * Returns the <CODE>snmpOutGetResponses</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutGetResponses</CODE> value.
     */
    public Long getSnmpOutGetResponses() {
        return new Long(snmpOutGetResponses);
    }
  
    /**
     * Returns the <CODE>snmpOutGenErrs</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutGenErrs</CODE> value.
     */
    public Long getSnmpOutGenErrs() {
        return new Long(snmpOutGenErrs);
    }
  
    /**
     * Returns the <CODE>snmpOutBadValues</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutBadValues</CODE> value.
     */
    public Long getSnmpOutBadValues() {
        return new Long(snmpOutBadValues);
    }
  
    /**
     * Returns the <CODE>snmpOutNoSuchNames</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutNoSuchNames</CODE> value.
     */
    public Long getSnmpOutNoSuchNames() {
        return new Long(snmpOutNoSuchNames);
    }
  
    /**
     * Returns the <CODE>snmpOutTooBigs</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutTooBigs</CODE> value.
     */
    public Long getSnmpOutTooBigs() {
        return new Long(snmpOutTooBigs);
    }
  
    /**
     * Returns the <CODE>snmpInASNParseErrs</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInASNParseErrs</CODE> value.
     */
    public Long getSnmpInASNParseErrs() {
        return new Long(snmpInASNParseErrs);
    }
  
    /**
     * Returns the <CODE>snmpInBadCommunityUses</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInBadCommunityUses</CODE> value.
     */
    public Long getSnmpInBadCommunityUses() {
        return new Long(snmpInBadCommunityUses);
    }
  
    /**
     * Returns the <CODE>snmpInBadCommunityNames</CODE> value defined in 
     * MIB-II.
     * 
     * @return The <CODE>snmpInBadCommunityNames</CODE> value.
     */
    public Long getSnmpInBadCommunityNames() {
        return new Long(snmpInBadCommunityNames);
    }
  
    /**
     * Returns the <CODE>snmpInBadVersions</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInBadVersions</CODE> value.
     */
    public Long getSnmpInBadVersions() {
        return new Long(snmpInBadVersions);
    }
  
    /**
     * Returns the <CODE>snmpOutPkts</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpOutPkts</CODE> value.
     */
    public Long getSnmpOutPkts() {
        return new Long(snmpOutPkts);
    }
  
    /**
     * Returns the <CODE>snmpInPkts</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInPkts</CODE> value.
     */
    public Long getSnmpInPkts() {
        return new Long(snmpInPkts);
    }
  
    /**
     * Returns the <CODE>snmpInGetRequests</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInGetRequests</CODE> value.
     */
    public Long getSnmpInGetRequests() {
        return new Long(snmpInGetRequests);
    }
  
    /**
     * Returns the <CODE>snmpInGetNexts</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInGetNexts</CODE> value.
     */
    public Long getSnmpInGetNexts() {
        return new Long(snmpInGetNexts);
    }
  
    /**
     * Returns the <CODE>snmpInSetRequests</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInSetRequests</CODE> value.
     */
    public Long getSnmpInSetRequests() {
        return new Long(snmpInSetRequests);
    }
  
    /**
     * Returns the <CODE>snmpInTotalSetVars</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInTotalSetVars</CODE> value.
     */
    public Long getSnmpInTotalSetVars() {
        return new Long(snmpInTotalSetVars);
    }
  
    /**
     * Returns the <CODE>snmpInTotalReqVars</CODE> value defined in MIB-II.
     * 
     * @return The <CODE>snmpInTotalReqVars</CODE> value.
     */
    public Long getSnmpInTotalReqVars() {
        return new Long(snmpInTotalReqVars);
    }
    
    /**
     * Returns the <CODE>snmpSilentDrops</CODE> value defined in RFC
     * 1907 NMPv2-MIB .
     * 
     * @return The <CODE>snmpSilentDrops</CODE> value.
     *
     */
    public Long getSnmpSilentDrops() {
        return new Long(snmpSilentDrops);
    }

    /**
     * Returns the <CODE>snmpProxyDrops</CODE> value defined in RFC
     * 1907 NMPv2-MIB .
     * 
     * @return The <CODE>snmpProxyDrops</CODE> value.
     *
     */
    public Long getSnmpProxyDrops() {
        return new Long(0);
    }
    
    
    // PUBLIC METHODS
    //---------------
 
    /**
     * Allows the MBean to perform any operations it needs before being
     * registered in the MBean server. 
     * If the name of the SNMP protocol adaptor MBean is not specified, 
     * it is initialized with the default value:
     * {@link com.sun.jdmk.ServiceName#DOMAIN com.sun.jdmk.ServiceName.DOMAIN}:
     * {@link com.sun.jdmk.ServiceName#SNMP_ADAPTOR_SERVER 
     * com.sun.jdmk.ServiceName.SNMP_ADAPTOR_SERVER}.
     * If any exception is raised, the SNMP protocol adaptor MBean will not 
     * be registered in the MBean server.
     *
     * @param server The MBean server to register the service with.
     * @param name The object name.
     *
     * @return The name of the SNMP protocol adaptor registered.
     *
     * @exception java.lang.Exception
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name) 
        throws java.lang.Exception {

        if (name == null) {
            name = new ObjectName(server.getDefaultDomain() + ":" + 
                               com.sun.jdmk.ServiceName.SNMP_ADAPTOR_SERVER);
        }
        return (super.preRegister(server, name));
    }
            
    /**
     * Not used in this context.
     */
    public void postRegister (Boolean registrationDone) {
        super.postRegister(registrationDone);
    } 

    /**
     * Not used in this context.
     */
    public void preDeregister() throws java.lang.Exception {
        super.preDeregister();
    }

    /**
     * Not used in this context.
     */
    public void postDeregister() {
        super.postDeregister();
    }

    /**
     * Adds a new MIB in the SNMP MIB handler.
     * 
     * @param mib The MIB to add.
     * 
     * @return A reference to the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib) 
        throws IllegalArgumentException {
        if(logger.finerOn())
            logger.finer("addMib", "Mib [" + mib + "]");
        
        if (mib == null) {
            throw new IllegalArgumentException() ;
        }
        
        //First remove from the old adaptor.
        // Change the behavior and we will see...
        //if(mib.getSnmpAdaptor() != null)
        //   mib.getSnmpAdaptor().removeMib(mib);
        
        if(!mibs.contains(mib))
            mibs.addElement(mib);

        root.register(mib);
        
        return this;
    }
    
    /**
     * Adds a new MIB in the SNMP MIB handler. 
     * This method is to be called to set a specific agent to a specific OID. 
     * This can be useful when dealing with MIB overlapping. 
     * Some OID can be implemented in more than one MIB. In this case, 
     * the OID nearer agent will be used on SNMP operations.
     * 
     * @param mib The MIB to add.
     * @param oids The set of OIDs this agent implements. 
     * If null or oids.length == 0, this is equivalent to calling
     * <CODE>addMib(SnmpMibAgent mib)</CODE>
     * 
     * @return A reference to the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     *
     */

    public SnmpMibHandler addMib(SnmpMibAgent mib, SnmpOid[] oids) 
        throws IllegalArgumentException {
      if(logger.finerOn()) {
          logger.finer("addMib", "Mib [" + mib + "]");
          StringBuffer b = new StringBuffer();
          for(int i = 0; i < oids.length; i++)
              b.append(oids[i] + " ");
          logger.finer("addMib", "Oid[] = " + b.toString());  
      }
      
      if (mib == null) {
        throw new IllegalArgumentException() ;
      }
      
      //If null or empty oid array, just add it to the mib.
      if(oids == null || 
         oids.length == 0) 
          return addMib(mib);
      
      if(!mibs.contains(mib))
          mibs.addElement(mib);
      
      for (int i = 0; i < oids.length; i++)
          root.register(mib, oids[i].longValue());
      
      return this;
  }
    
    /**
     * Adds a new MIB in the SNMP MIB handler.
     * When the underlying implementation does not support MIB scoping,
     * calling this method is equivalent to calling
     * <CODE>addMib(SnmpMibAgent mib)</CODE>.
     * <p>Note that MIB scoping is only supported by the {@link
     * SnmpV3AdaptorServer}. This class does not support it.
     * 
     * @param mib The MIB to add. 
     * @param contextName The MIB context name.  
     * The SnmpAdaptorServer base class always ignores this parameter.
     *
     * @return A reference on the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     *
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, 
                                 String contextName) 
        throws IllegalArgumentException {
        return addMib(mib);
    }
    
    /**
     * Adds a new MIB in the SNMP MIB handler.
     * When the underlying implementation does not support MIB scoping,
     * calling this method is equivalent to calling
     * <CODE>addMib(SnmpMibAgent mib, SnmpOid[] oids)</CODE>.
     * <p>Note that MIB scoping is only supported by the {@link
     * SnmpV3AdaptorServer}. This class does not support it. 
     * @param mib The MIB to add.
     * @param contextName The MIB context. 
     * The SnmpAdaptorServer base class always ignores this parameter.
     * @param oids The set of OIDs this agent implements.
     * If null or oids.length == 0, this is equivalent to calling
     * <CODE>addMib(SnmpMibAgent mib, String contextName)</CODE>
     * 
     * @return A reference to the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     *
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, 
                                 String contextName, 
                                 SnmpOid[] oids) 
        throws IllegalArgumentException {
        return addMib(mib, oids);
    }
    
    /**
     * Removes the specified MIB from the SNMP protocol adaptor. 
     * When the underlying implementation does not support MIB scoping,
     * calling this method is equivalent to calling
     * <CODE>removeMib(SnmpMibAgent mib)</CODE>.
     * <p>Note that MIB scoping is only supported by the {@link
     * SnmpV3AdaptorServer}. This class does not support it.
     * 
     * @param mib The MIB to be removed.
     * @param contextName The context name used at registration time.
     * The SnmpAdaptorServer base class always ignores this parameter.
     *
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> was
     * a MIB included in the SNMP MIB handler, <CODE>false</CODE>
     * otherwise.
     *
     */
    public boolean removeMib(SnmpMibAgent mib, String contextName) {
        return removeMib(mib);
    }

    /**
     * Removes the specified MIB from the SNMP protocol adaptor. 
     * 
     * @param mib The MIB to be removed.
     *
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> 
     * was a MIB included in the SNMP MIB handler, 
     * <CODE>false</CODE> otherwise.
     */
    public boolean removeMib(SnmpMibAgent mib) {
        if(logger.finerOn())
            logger.finer("removeMib", "Mib [ " + mib +"]");
        
        root.unregister(mib);
        return (mibs.removeElement(mib)) ;
    }

    /**
     * Removes the specified MIB from the SNMP protocol adaptor. 
     * 
     * @param mib The MIB to be removed.
     * @param oids The oid the MIB was previously registered for. 
     * If null or oids.length == 0, this is equivalent to calling
     * <CODE>removeMib(SnmpMibAgent mib)</CODE>
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> was
     * a MIB included in the SNMP MIB handler, <CODE>false</CODE>
     * otherwise.
     *
     */
    public boolean removeMib(SnmpMibAgent mib, SnmpOid[] oids) {
        if(logger.finerOn()) {
            logger.finer("removeMib", "Mib [" + mib + "]");
            StringBuffer b = new StringBuffer();
            for(int i = 0; i < oids.length; i++)
                b.append(oids[i] + " ");
            logger.finer("removeMib", "Oid[] = " + b.toString());  
        }

        if(oids == null ||
           oids.length == 0)
            return removeMib(mib);
        
        boolean ret = mibs.contains(mib);
        
        if(ret) {
            //Unregister for each oid.
            root.unregister(mib, oids);
            
            //Still in tree, don't remove it.
            if(!root.isMibReferenced(mib)) {
                if(logger.finerOn())
                    logger.finer("removeMib", "Mib removed");
                mibs.removeElement(mib);
            } else
                if(logger.finerOn())
                    logger.finer("removeMib", 
                                 "Mib NOT removed, still referenced");
        }
        
        return ret;
    }

    /**
     * Removes the specified MIB from the SNMP protocol adaptor. 
     * When the underlying implementation does not support MIB scoping,
     * calling this method is equivalent to calling
     * <CODE>removeMib(SnmpMibAgent mib, SnmpOid[] oids)</CODE>.
     * <p>Note that MIB scoping is only supported by the {@link
     * SnmpV3AdaptorServer}. This class does not support it. 
     * 
     * @param mib The MIB to be removed.
     * @param contextName The context name used at registration time.
     * The SnmpAdaptorServer base class always ignores this parameter.
     *
     * @param oids The oid the MIB was previously registered for. 
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> was
     * a MIB included in the SNMP MIB handler, <CODE>false</CODE>
     * otherwise.
     *
     */
    public boolean removeMib(SnmpMibAgent mib, 
                             String contextName, 
                             SnmpOid[] oids) {
        return removeMib(mib, oids);
    }

    // SUBCLASSING OF COMMUNICATOR SERVER
    //-----------------------------------
    
    /**
     * Sets the port number used by this <CODE>CommunicatorServer</CODE>.
     *
     * @param port The port number used by this <CODE>CommunicatorServer</CODE>.
     *
     * @exception java.lang.IllegalStateException This method has been invoked
     * while the communicator was ONLINE or STARTING.
     */
    public void setPort(int port) throws java.lang.IllegalStateException {
        super.setPort(port);
        updateLogger();
    }


    /**
     * Creates the datagram socket.
     */
    protected void doBind() 
        throws CommunicationException, InterruptedException {

        try {
            socket = new DatagramSocket(port, address);
            updateLogger();
        }
        catch (SocketException e) {
            if (e.getMessage().equals(InterruptSysCallMsg))
                throw new InterruptedException(e.toString()) ;
            else {
                if (logger.finestOn()) {
                    logger.finest("doBind", "cannot bind on port " + port);
                }
                throw new CommunicationException(e) ;
            }
        }
    }
  
    /**
     * Closes the datagram socket.
     */
    protected void doUnbind() 
        throws CommunicationException, InterruptedException {
        if (logger.finerOn()) {
            logger.finer("doUnbind","Finally close the socket");
        }
        try {
            if (socket != null) {
                socket.close() ;
                socket = null ;     // Important to inform finalize() that 
                                    // the socket is closed...
            }
        } finally {
            closeTrapSocketIfNeeded() ;
            closeInformSocketIfNeeded() ;
        }
    }

    void createSnmpRequestHandler(SnmpAdaptorServer server, int id, 
                                  DatagramSocket s, DatagramPacket p,
                                  SnmpMibTree tree, Vector m, Object a, 
                                  SnmpPduFactory factory, 
                                  SnmpUserDataFactory dataFactory,
                                  MBeanServer f, ObjectName n) {
        final SnmpRequestHandler handler = 
            new SnmpRequestHandler(this, id, s, p, tree, m, a, factory,
                                   dataFactory, f, n);
        threadService.submitTask(handler);
    }

    /**
     * Reads a packet from the datagram socket and creates a request handler 
     * which decodes and processes the request.
     */
    protected void doReceive() 
        throws CommunicationException, InterruptedException {

        // Let's wait for something to be received.
        //
        try {
            packet = new DatagramPacket(new byte[bufferSize], bufferSize) ;
            socket.receive(packet);
            int state = getState();
            
            if(state != ONLINE) {
                if (logger.finerOn()) {
                    logger.finer("doReceive", 
                        "received a message but state not online, reruning.");
                }
                return;
            }
            createSnmpRequestHandler(this, servedClientCount, socket, 
                                     packet, root, mibs, ipacl, pduFactory,
                                     userDataFactory, topMBS, objectName);
        } catch (SocketException e) {
            // Let's check if we have been interrupted by stop().
            //
            if (e.getMessage().equals(InterruptSysCallMsg))
                throw new InterruptedException(e.toString()) ;
            else
                throw new CommunicationException(e) ;
        } catch (InterruptedIOException e) {
            throw new InterruptedException(e.toString()) ;
        } catch (CommunicationException e) {
            throw e ;
        } catch (Exception e) {
            throw new CommunicationException(e) ;
        }
        if (logger.finerOn()) {
            logger.finer("doReceive", "received a message");
        }
    }
  
    protected void doError(Exception e) throws CommunicationException {
        return;
    }
    
    /**
     * Not used in this context.
     */
    protected void doProcess() 
        throws CommunicationException, InterruptedException {
    }

    /**
     * Stops this SNMP protocol adaptor.
     * Closes the datagram socket.
     * <p> 
     * Has no effect if this SNMP protocol adaptor is <CODE>OFFLINE</CODE> or 
     * <CODE>STOPPING</CODE>.
     */
    public void stop(){
        
        final InetAddress address = getAddress();
        final int port = getPort(); 
        if ((state == ONLINE) || (state == STARTING)){
            if (logger.finerOn()) {
              logger.finer("stop", "Stopping: using port " + port);
            }
            super.stop();
            try{
                final DatagramSocket sn = new DatagramSocket(0);
                final byte[] ob = new byte[1];
                
                DatagramPacket pk;
                if (address != null && !address.isAnyLocalAddress())
                    pk = new DatagramPacket(ob , 1, address, port);
                else 
                    pk = new DatagramPacket(ob , 1, 
                             java.net.InetAddress.getLocalHost(), port);
                 
                if (port > 0) {
                    if (logger.finerOn()) {
                        logger.finer("stop", "Sending: using port " + port);
                    }
                    sn.send(pk);
                    sn.close();
                }
            } catch (Throwable e){
                if (logger.finestOn()) {
                    logger.finest("stop", e);
                }
            } 
        }
    }
    
    // SENDING SNMP TRAPS STUFF
    //-------------------------
    
    /**
     * Sends a trap using SNMP V1 trap format.
     * <BR>The trap is sent to each destination defined in the ACL file 
     * (if available).
     * If no ACL file or no destinations are available, the trap is sent to 
     * the local host.
     * 
     * @param generic The generic number of the trap.
     * @param specific The specific number of the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *            by <CODE>bufferSize</CODE>.
     */
    public void snmpV1Trap(int generic, int specific,
                           SnmpVarBindList varBindList) 
        throws IOException, SnmpStatusException {

        if (logger.finerOn()) {
            logger.finer("snmpV1Trap", "generic=" + generic + 
                         ", specific=" + specific);
        }
        
        // First, make an SNMP V1 trap pdu
        //
        SnmpPduTrap pdu = new SnmpPduTrap() ;
        pdu.address = null ;
        pdu.port = trapPort ;
        pdu.type = pduV1TrapPdu ;
        pdu.version = snmpVersionOne ;
        pdu.community = null ;
        pdu.enterprise = enterpriseOid ;
        pdu.genericTrap = generic ;
        pdu.specificTrap = specific ;
        pdu.timeStamp = getSysUpTime();
    
        if (varBindList != null) {
            pdu.varBindList = new SnmpVarBind[varBindList.size()] ;
            varBindList.copyInto(pdu.varBindList);
        }
        else
            pdu.varBindList = null ;
      
        // If the local host cannot be determined, we put 0.0.0.0 in agentAddr
        try {
            if (address != null)
                pdu.agentAddr = handleMultipleIpVersion(address.getAddress());
            else 
                pdu.agentAddr = handleMultipleIpVersion(
                                InetAddress.getLocalHost().getAddress());
        }
        catch (UnknownHostException e) {
            byte[] zeroedAddr = new byte[4];
            pdu.agentAddr = handleMultipleIpVersion(zeroedAddr) ;
        }
    
        // Next, send the pdu to all destinations defined in ACL
        //
        sendTrapPdu(pdu) ;
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
     * Sends a trap using SNMP V1 trap format.
     * <BR>The trap is sent to the specified <CODE>InetAddress</CODE> 
     * destination using the specified community string (and the ACL file 
     * is not used).
     * 
     * @param addr The <CODE>InetAddress</CODE> destination of the trap.
     * @param cs The community string to be used for the trap.
     * @param generic The generic number of the trap.
     * @param specific The specific number of the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     * by <CODE>bufferSize</CODE>.
     */
    public void snmpV1Trap(InetAddress addr, String cs, int generic, 
                           int specific, SnmpVarBindList varBindList) 
        throws IOException, SnmpStatusException {

        if (logger.finerOn()) {
            logger.finer("snmpV1Trap", "generic=" + generic + 
                         ", specific=" + specific);
        }
        
        // First, make an SNMP V1 trap pdu
        //
        SnmpPduTrap pdu = new SnmpPduTrap() ;
        pdu.address = null ;
        pdu.port = trapPort ;
        pdu.type = pduV1TrapPdu ;
        pdu.version = snmpVersionOne ;
        
        if(cs != null)
            pdu.community = cs.getBytes();
        else
            pdu.community = null ;
        
        pdu.enterprise = enterpriseOid ;
        pdu.genericTrap = generic ;
        pdu.specificTrap = specific ;
        pdu.timeStamp = getSysUpTime();
    
        if (varBindList != null) {
            pdu.varBindList = new SnmpVarBind[varBindList.size()] ;
            varBindList.copyInto(pdu.varBindList);
        }
        else
            pdu.varBindList = null ;
      
        // If the local host cannot be determined, we put 0.0.0.0 in agentAddr
        try {
            if (address != null)
                pdu.agentAddr = handleMultipleIpVersion(address.getAddress()) ;
            else
                pdu.agentAddr = handleMultipleIpVersion(
                                InetAddress.getLocalHost().getAddress()) ;
        }
        catch (UnknownHostException e) {
            byte[] zeroedAddr = new byte[4];
            pdu.agentAddr = handleMultipleIpVersion(zeroedAddr) ;
        }
    
        // Next, send the pdu to the specified destination
        //
        if(addr != null)
            sendTrapPdu(addr, pdu) ;
        else
            sendTrapPdu(pdu);
    }
    
    /**
     * Sends a trap using SNMP V1 trap format.
     * <BR>The trap is sent to the specified <CODE>InetAddress</CODE> 
     * destination using the specified parameters (and the ACL file is not 
     * used).
     * Note that if the specified <CODE>InetAddress</CODE> destination is null,
     * then the ACL file mechanism is used.
     * 
     * @param addr The <CODE>InetAddress</CODE> destination of the trap.
     * @param agentAddr The agent address to be used for the trap.
     * @param cs The community string to be used for the trap.
     * @param enterpOid The enterprise OID to be used for the trap.
     * @param generic The generic number of the trap.
     * @param specific The specific number of the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @param time The time stamp (overwrite the current time).
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *            by <CODE>bufferSize</CODE>.
     *
     */
    public void snmpV1Trap(InetAddress addr,
                           SnmpIpAddress agentAddr,
                           String cs,
                           SnmpOid enterpOid,
                           int generic,
                           int specific,
                           SnmpVarBindList varBindList,
                           SnmpTimeticks time)
        throws IOException, SnmpStatusException {
        snmpV1Trap(addr, 
                   trapPort,
                   agentAddr,
                   cs,
                   enterpOid,
                   generic,
                   specific,
                   varBindList,
                   time);
    }

    /**
     * Sends a trap using SNMP V1 trap format.
     * <BR>The trap is sent to the specified <CODE>SnmpPeer</CODE> destination.
     * The community string used is the one located in the 
     * <CODE>SnmpPeer</CODE> parameters 
     * (<CODE>SnmpParameters.getRdCommunity() </CODE>).
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
     * @exception SnmpStatusException If the trap exceeds the limit
     * defined by <CODE>bufferSize</CODE>.
     *
     */
    public void snmpV1Trap(SnmpPeer peer,
                           SnmpIpAddress agentAddr,
                           SnmpOid enterpOid,
                           int generic,
                           int specific,
                           SnmpVarBindList varBindList,
                           SnmpTimeticks time) 
        throws IOException, SnmpStatusException {
        SnmpParameters p = (SnmpParameters) peer.getParams();
        snmpV1Trap(peer.getDestAddr(), 
                   peer.getDestPort(),
                   agentAddr,
                   p.getRdCommunity(),
                   enterpOid,
                   generic,
                   specific,
                   varBindList,
                   time);
    }
    
    private void snmpV1Trap(InetAddress addr,
                            int port,
                            SnmpIpAddress agentAddr,
                            String cs,
                            SnmpOid enterpOid,
                            int generic,
                            int specific,
                            SnmpVarBindList varBindList,
                            SnmpTimeticks time) 
        throws IOException, SnmpStatusException {
        
        if (logger.finerOn()) {
            logger.finer("snmpV1Trap", "generic=" + generic + 
                         ", specific=" + specific);
        }
        
        // First, make an SNMP V1 trap pdu
        //
        SnmpPduTrap pdu = new SnmpPduTrap() ;
        pdu.address = null ;
        pdu.port = port ;
        pdu.type = pduV1TrapPdu ;
        pdu.version = snmpVersionOne ;
        
        //Diff start
        if(cs != null) 
            pdu.community = cs.getBytes();
        else
            pdu.community = null ;
        //Diff end

        // Diff start
        if(enterpOid != null)
            pdu.enterprise = enterpOid;
        else
            pdu.enterprise = enterpriseOid ;
        //Diff end
        pdu.genericTrap = generic ;
        pdu.specificTrap = specific ;
        //Diff start
        if(time != null)
            pdu.timeStamp = time.longValue();
        else
            pdu.timeStamp = getSysUpTime();
        //Diff end
    
        if (varBindList != null) {
            pdu.varBindList = new SnmpVarBind[varBindList.size()] ;
            varBindList.copyInto(pdu.varBindList);
        }
        else
            pdu.varBindList = null ;
      
        if(agentAddr != null)
            pdu.agentAddr = agentAddr;
        else {
            // If the local host cannot be determined, 
            // we put 0.0.0.0 in agentAddr
            try {
                if (address != null)
                    pdu.agentAddr = 
                        handleMultipleIpVersion(address.getAddress()) ;
                else
                    pdu.agentAddr = handleMultipleIpVersion(
                        InetAddress.getLocalHost().getAddress()) ;
            }
            catch (UnknownHostException e) {
                byte[] zeroedAddr = new byte[4];
                pdu.agentAddr = handleMultipleIpVersion(zeroedAddr);
            }
        }

        // Next, send the pdu to the specified destination
        //
        // Diff start
        if(addr != null)
            sendTrapPdu(addr, pdu) ;
        else
            sendTrapPdu(pdu);

        //End diff
    }
                            
    /**
     * Sends a trap using SNMP V2 trap format.
     * <BR>The trap is sent to the specified <CODE>SnmpPeer</CODE> destination.
     * <BR>The community string used is the one located in the 
     * <CODE>SnmpPeer</CODE> parameters 
     * (<CODE>SnmpParameters.getRdCommunity() </CODE>).
     * <BR>The variable list included in the outgoing trap is composed of 
     * the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with the value specified by
     *   <CODE>time</CODE></LI>
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by 
     *   <CODE>trapOid</CODE></LI>
     * <LI><CODE>all the (oid,values)</CODE> from the specified 
     *   <CODE>varBindList</CODE></LI>
     * </UL>
     * 
     * @param peer The <CODE>SnmpPeer</CODE> destination of the trap.
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @param time The time stamp (overwrite the current time).
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit
     * defined by <CODE>bufferSize</CODE>.
     *
     */
    public void snmpV2Trap(SnmpPeer peer,
                           SnmpOid trapOid,
                           SnmpVarBindList varBindList,
                           SnmpTimeticks time) 
        throws IOException, SnmpStatusException {
        SnmpParameters p = (SnmpParameters) peer.getParams();
        snmpV2Trap(peer.getDestAddr(), 
                   peer.getDestPort(), 
                   p.getRdCommunity(),
                   trapOid,
                   varBindList,
                   time);
    }

    /**
     * Sends a trap using SNMP V2 trap format.
     * <BR>The trap is sent to each destination defined in the ACL file 
     * (if available).
     * If no ACL file or no destinations are available, the trap is sent 
     * to the local host.
     * <BR>The variable list included in the outgoing trap is composed of
     * the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value</LI>
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by 
     *   <CODE>trapOid</CODE></LI>
     * <LI><CODE>all the (oid,values)</CODE> from the specified 
     *   <CODE>varBindList</CODE></LI>
     * </UL>
     * 
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *            by <CODE>bufferSize</CODE>.
     */
    public void snmpV2Trap(SnmpOid trapOid, SnmpVarBindList varBindList) 
        throws IOException, SnmpStatusException {

        if (logger.finerOn()) {
            logger.finer("snmpV2Trap", "trapOid=" + trapOid);
        }
        
        // First, make an SNMP V2 trap pdu
        // We clone varBindList and insert sysUpTime and snmpTrapOid
        //
        SnmpPduRequest pdu = new SnmpPduRequest() ;
        pdu.address = null ;
        pdu.port = trapPort ;
        pdu.type = pduV2TrapPdu ;
        pdu.version = snmpVersionTwo ;
        pdu.community = null ;

        SnmpVarBindList fullVbl = 
            completeTrapVarBindList(varBindList,
                                    trapOid,
                                    new SnmpTimeticks(getSysUpTime()));
        
        pdu.varBindList = new SnmpVarBind[fullVbl.size()] ;
        fullVbl.copyInto(pdu.varBindList) ;
        
        // Next, send the pdu to all destinations defined in ACL
        //
        sendTrapPdu(pdu) ;
    }
    
    /**
     * Sends a trap using SNMP V2 trap format.
     * <BR>The trap is sent to the specified <CODE>InetAddress</CODE> 
     * destination using the specified community string (and the ACL file is 
     * not used).
     * <BR>The variable list included in the outgoing trap is composed of the 
     * following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value</LI>
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by 
     *    <CODE>trapOid</CODE></LI>
     * <LI><CODE>all the (oid,values)</CODE> from the specified 
     *    <CODE>varBindList</CODE></LI>
     * </UL>
     * 
     * @param addr The <CODE>InetAddress</CODE> destination of the trap.
     * @param cs The community string to be used for the trap.
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *            by <CODE>bufferSize</CODE>.
     */
    public void snmpV2Trap(InetAddress addr, String cs, SnmpOid trapOid, 
                           SnmpVarBindList varBindList) 
        throws IOException, SnmpStatusException {

        if (logger.finerOn()) {
            logger.finer("snmpV2Trap", "trapOid=" + trapOid);
        }
        
        // First, make an SNMP V2 trap pdu
        // We clone varBindList and insert sysUpTime and snmpTrapOid
        //
        SnmpPduRequest pdu = new SnmpPduRequest() ;
        pdu.address = null ;
        pdu.port = trapPort ;
        pdu.type = pduV2TrapPdu ;
        pdu.version = snmpVersionTwo ;

        if(cs != null) 
            pdu.community = cs.getBytes();
        else
            pdu.community = null;
         
        SnmpVarBindList fullVbl = 
            completeTrapVarBindList(varBindList,
                                    trapOid,
                                    new SnmpTimeticks(getSysUpTime()));
        
        pdu.varBindList = new SnmpVarBind[fullVbl.size()] ;
        fullVbl.copyInto(pdu.varBindList) ;
      
        // Next, send the pdu to the specified destination
        //
        if(addr != null)
            sendTrapPdu(addr, pdu);
        else
            sendTrapPdu(pdu);
    }
    
    /**
     * Sends a trap using SNMP V2 trap format.
     * <BR>The trap is sent to the specified <CODE>InetAddress</CODE> 
     * destination using the specified parameters (and the ACL file is 
     * not used).
     * Note that if the specified <CODE>InetAddress</CODE> destination is null,
     * then the ACL file mechanism is used.
     * <BR>The variable list included in the outgoing trap is composed of the
     * following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with the value specified by 
     *   <CODE>time</CODE></LI>
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by 
     *   <CODE>trapOid</CODE></LI>
     * <LI><CODE>all the (oid,values)</CODE> from the specified 
     *   <CODE>varBindList</CODE></LI>
     * </UL>
     * 
     * @param addr The <CODE>InetAddress</CODE> destination of the trap.
     * @param cs The community string to be used for the trap.
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     * @param time The time stamp (overwrite the current time).
     *
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit
     * defined by <CODE>bufferSize</CODE>.
     *
     */
    public void snmpV2Trap(InetAddress addr, 
                           String cs, 
                           SnmpOid trapOid, 
                           SnmpVarBindList varBindList, 
                           SnmpTimeticks time)
        throws IOException, SnmpStatusException {
      
        snmpV2Trap(addr, 
                   trapPort, 
                   cs,
                   trapOid,
                   varBindList,
                   time);
    }
    
    private void snmpV2Trap(InetAddress addr, 
                            int port,
                            String cs, 
                            SnmpOid trapOid, 
                            SnmpVarBindList varBindList, 
                            SnmpTimeticks time)
        throws IOException, SnmpStatusException {
        
        if (logger.finerOn()) {
            logger.finer("snmpV2Trap", "trapOid=" + trapOid +
                  "\ncommunity=" + cs + "\naddr=" + addr +
                  "\nvarBindList=" + varBindList + "\ntime=" + time +
                  "\ntrapPort=" + port);
        }
        
        // First, make an SNMP V2 trap pdu
        // We clone varBindList and insert sysUpTime and snmpTrapOid
        //
        SnmpPduRequest pdu = new SnmpPduRequest() ;
        pdu.address = null ;
        pdu.port = port ;
        pdu.type = pduV2TrapPdu ;
        pdu.version = snmpVersionTwo ;
        
        if(cs != null) 
            pdu.community = cs.getBytes();
        else
            pdu.community = null;

        SnmpTimeticks sysUpTimeValue = null;
        if(time != null)
            sysUpTimeValue = time;
        else
            sysUpTimeValue = new SnmpTimeticks(getSysUpTime()) ;

        SnmpVarBindList fullVbl = completeTrapVarBindList(varBindList,
                                                          trapOid,
                                                          sysUpTimeValue);
        
        pdu.varBindList = new SnmpVarBind[fullVbl.size()] ;
        fullVbl.copyInto(pdu.varBindList) ;
      
        // Next, send the pdu to the specified destination
        //
        // Diff start
        if(addr != null)
            sendTrapPdu(addr, pdu) ;
        else
            sendTrapPdu(pdu);
        //End diff
    }

    /**
     * Send the specified trap PDU to the passed <CODE>InetAddress</CODE>.
     * @param address The destination address.
     * @param pdu The pdu to send.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit
     * defined by <CODE>bufferSize</CODE>.
     *
     */
    public void snmpPduTrap(InetAddress address, SnmpPduPacket pdu) 
        throws IOException, SnmpStatusException {

        if(address != null)
            sendTrapPdu(address, pdu);
        else
            sendTrapPdu(pdu);
    }
    
    /**
     * Send the specified trap PDU to the passed <CODE>SnmpPeer</CODE>.
     * @param peer The destination peer. The Read community string is used of 
     *   <CODE>SnmpParameters</CODE> is used as the trap community string.
     * @param pdu The pdu to send.
     * @exception IOException An I/O error occurred while sending the trap.
     * @exception SnmpStatusException If the trap exceeds the limit defined 
     *   by <CODE>bufferSize</CODE>.
     */
    public void snmpPduTrap(SnmpPeer peer, 
                            SnmpPduPacket pdu) 
        throws IOException, SnmpStatusException {
        if(peer != null) {
            pdu.port = peer.getDestPort();
            sendTrapPdu(peer.getDestAddr(), pdu);
        }
        else {
            pdu.port = getTrapPort().intValue();
            sendTrapPdu(pdu);   
        }
    }
    
    
    /**
     * Add time and type varbind if needed when sending trap.
     */
    static SnmpVarBindList 
        completeTrapVarBindList(SnmpVarBindList vbl,
                                SnmpOid notificationOid,
                                SnmpTimeticks sysUpTimeValue) {
        SnmpVarBindList fullVbl;
        if (vbl != null)
            fullVbl = (SnmpVarBindList) vbl.clone();
        else
            fullVbl = new SnmpVarBindList(2);
        
        // First check presence of sysUpTimeOid at index 0, and
        // insert it at index 0 if necessary,
        //
        int length = fullVbl.size();
        if(length == 0 ||
           !fullVbl.getVarBindAt(0).getOid().equals(sysUpTimeOid))
            fullVbl.insertElementAt(new SnmpVarBind(sysUpTimeOid,
                                                    sysUpTimeValue), 0);
        
        // Then check presence of snmpTrapOidOid at index 1,
        // and insert it at index 1 if necessary.
        //
        if(length <= 1 || 
           !fullVbl.getVarBindAt(1).getOid().equals(snmpTrapOidOid))
            fullVbl.insertElementAt(new SnmpVarBind(snmpTrapOidOid,
                                                    notificationOid), 1);
        return fullVbl;
    }
    
    /**
     * Send the specified trap PDU to every destinations from the ACL file.
     */
    private void sendTrapPdu(SnmpPduPacket pdu) 
        throws SnmpStatusException, IOException {
  
        // Make an SNMP message from the pdu
        //
        SnmpMessage msg = null ;
        try {
            msg = (SnmpMessage)pduFactory.encodeSnmpPdu(pdu, bufferSize) ;
            if (msg == null) {
                throw new SnmpStatusException(
                          SnmpDefinitions.snmpRspAuthorizationError) ;
            }
        } catch (SnmpTooBigException x) {
            if (logger.finestOn()) {
                logger.finest("sendTrapPdu", "trap pdu is too big");
                logger.finest("sendTrapPdu", 
                              "trap hasn't been sent to anyone");
            }
            throw new SnmpStatusException(SnmpDefinitions.snmpRspTooBig) ;
            // FIXME: is the right exception to throw ?
            // We could simply forward SnmpTooBigException ?
        }
    
        // Now send the SNMP message to each destination
        //
        int sendingCount = 0 ;
        openTrapSocketIfNeeded() ;
        if (ipacl != null) {
            Enumeration ed = ((InetAddressAcl)ipacl).getTrapDestinations() ;
            while (ed.hasMoreElements()) {
                msg.address = (InetAddress)ed.nextElement() ;
                Enumeration ec = 
                    ((InetAddressAcl)ipacl).getTrapCommunities(msg.address) ;
                while (ec.hasMoreElements()) {
                    msg.community = ((String)ec.nextElement()).getBytes() ;
                    try {
                        sendTrapMessage(msg) ;
                        sendingCount++ ;
                    }
                    catch (SnmpTooBigException x) {
                        if (logger.finestOn()) {
                            logger.finest("sendTrapPdu", 
                                          "trap pdu is too big");
                            logger.finest("sendTrapPdu", 
                                          "trap hasn't been sent to " + 
                                          msg.address);
                        }
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
                sendTrapMessage(msg) ;
            }
            catch (SnmpTooBigException x) {
                if (logger.finestOn()) {
                    logger.finest("sendTrapPdu", "trap pdu is too big");
                    logger.finest("sendTrapPdu", "trap hasn't been sent");
                }
            }
            catch (UnknownHostException e) {
                if (logger.finestOn()) {
                    logger.finest("sendTrapPdu", "cannot get the local host");
                    logger.finest("sendTrapPdu", "trap hasn't been sent");
                }
            }
        }
    
        closeTrapSocketIfNeeded() ;
    }

    /**
     * Send the specified trap PDU to the specified destination.
     */
    private void sendTrapPdu(InetAddress addr, SnmpPduPacket pdu) 
        throws SnmpStatusException, IOException {
  
        // Make an SNMP message from the pdu
        //
        SnmpMessage msg = null ;
        try {
            msg = (SnmpMessage)pduFactory.encodeSnmpPdu(pdu, bufferSize) ;
            if (msg == null) {
                throw new SnmpStatusException(
                          SnmpDefinitions.snmpRspAuthorizationError) ;
            }
        }
        catch (SnmpTooBigException x) {
            if (logger.finestOn()) {
                logger.finest("sendTrapPdu", "trap pdu is too big");
                logger.finest("sendTrapPdu", 
                              "trap hasn't been sent to the specified host");
            }
            throw new SnmpStatusException(SnmpDefinitions.snmpRspTooBig) ;
            // FIXME: is the right exception to throw ?
            // We could simply forward SnmpTooBigException ?
        }
    
        // Now send the SNMP message to specified destination
        //
        openTrapSocketIfNeeded() ;
        if (addr != null) {
            msg.address = addr;
            try {
                sendTrapMessage(msg) ;
            }
            catch (SnmpTooBigException x) {
                if (logger.finestOn()) {
                    logger.finest("sendTrapPdu", "trap pdu is too big");
                    logger.finest("sendTrapPdu", "trap hasn't been sent to "+
                                  msg.address);
                }
            }
        }
    
        closeTrapSocketIfNeeded() ;
    }
    
    /**
     * Send the specified message on trapSocket.
     */
    private void sendTrapMessage(SnmpMessage msg) 
        throws IOException, SnmpTooBigException {
        byte[] buffer = new byte[bufferSize] ;
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length) ;
        int encodingLength = msg.encodeMessage(buffer) ;
        packet.setLength(encodingLength) ;
        packet.setAddress(msg.address) ;
        packet.setPort(msg.port) ;
        if (logger.finerOn()) {
            logger.finer("sendTrapMessage", "sending trap to " + 
                         msg.address + ":" + msg.port);
        }
        trapSocket.send(packet) ;
        if (logger.finerOn()) {
            logger.finer("sendTrapMessage", "sent to " + msg.address + ":" + 
                         msg.port);
        }
        snmpOutTraps++;
        snmpOutPkts++;
    }
  
    /**
     * Open trapSocket if it's not already done.
     */
    synchronized void openTrapSocketIfNeeded() throws SocketException {
        if (trapSocket == null) {
            trapSocket = new DatagramSocket(0, address) ;
            if (logger.finerOn()) {
                logger.finer("openTrapSocketIfNeeded", "using port " + 
                             trapSocket.getLocalPort() + " to send traps");
            }
        }
    }

    /**
     * Close trapSocket if the SNMP protocol adaptor is not ONLINE.
     */
    synchronized void closeTrapSocketIfNeeded() {
        if ((trapSocket != null) && (state != ONLINE)) {
            trapSocket.close() ;
            trapSocket = null ;
        }
    }
    
    // SENDING SNMP INFORMS STUFF
    //---------------------------
    
    /**
     * Sends an inform using SNMP V2 inform request format.
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
     * @param trapOid The OID identifying the trap.
     * @param varBindList A list of <CODE>SnmpVarBind</CODE> instances or null.
     *
     * @return A vector of {@link com.sun.management.comm.SnmpInformRequest} objects.
     * <P>If there is no destination host for this inform request, the 
     *    returned vector will be empty.
     *
     * @exception IllegalStateException  This method has been invoked while 
     *     the SNMP adaptor server was not active.
     * @exception IOException An I/O error occurred while sending the inform 
     *     request.
     * @exception SnmpStatusException If the inform request exceeds the limit
     *     defined by <CODE>bufferSize</CODE>.
     */
    public Vector snmpInformRequest(SnmpInformHandler cb, SnmpOid trapOid, 
                                    SnmpVarBindList varBindList) 
        throws IllegalStateException, IOException, SnmpStatusException {

        if (!isActive()) {
            throw new IllegalStateException(
               "Start SNMP adaptor server before carrying out this operation");
        }
        if (logger.finerOn()) {
            logger.finer("snmpInformRequest", "trapOid=" + trapOid);
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
        InetAddress addr = null;
        String cs = null;
        if (ipacl != null) {
            Enumeration ed = ((InetAddressAcl)ipacl).getInformDestinations() ;
            while (ed.hasMoreElements()) {
                addr = (InetAddress)ed.nextElement() ;
                Enumeration ec = 
                    ((InetAddressAcl)ipacl).getInformCommunities(addr) ;
                while (ec.hasMoreElements()) {
                    cs = (String)ec.nextElement() ;
                    informReqList.addElement(
                        informSession.makeAsyncRequest(addr, cs, cb, fullVbl,
                                                       getInformPort())) ;
                }
            }
        }
    
        return informReqList ;
    }
    
    /**
     * Sends an inform using SNMP V2 inform request format.
     * <BR>The inform is sent to the specified <CODE>InetAddress</CODE> 
     * destination using the specified community string.
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
     * @param addr The <CODE>InetAddress</CODE> destination for this inform 
     *    request.
     * @param cs The community string to be used for the inform request.
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
     * @exception SnmpStatusException If the inform request exceeds the 
     *    limit defined by <CODE>bufferSize</CODE>.
     */
    public SnmpInformRequest snmpInformRequest(InetAddress addr, 
                                               String cs, 
                                               SnmpInformHandler cb, 
                                               SnmpOid trapOid, 
                                               SnmpVarBindList varBindList) 
        throws IllegalStateException, IOException, SnmpStatusException {
        
        return snmpInformRequest(addr, 
                                 getInformPort(), 
                                 cs,
                                 cb,
                                 trapOid,
                                 varBindList);
    }
    
    /**
     * Sends an inform using SNMP V2 inform request format.
     * <BR>The inform is sent to the specified <CODE>SnmpPeer</CODE> 
     * destination.
     * <BR> The community string used is the one located in the 
     * <CODE>SnmpPeer</CODE> parameters 
     * (<CODE>SnmpParameters.getInformCommunity() </CODE>).
     * <BR>The variable list included in the outgoing inform is composed
     * of the following items:
     * <UL>
     * <LI><CODE>sysUpTime.0</CODE> with its current value</LI>
     * <LI><CODE>snmpTrapOid.0</CODE> with the value specified by 
     *    <CODE>trapOid</CODE></LI>
     * <LI><CODE>all the (oid,values)</CODE> from the specified 
     *    <CODE>varBindList</CODE></LI>
     * </UL>
     * To send an inform request, the SNMP adaptor server must be active.
     * 
     * @param peer The <CODE>SnmpPeer</CODE> destination for this inform 
     *     request.
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
     * @exception SnmpStatusException If the inform request exceeds the 
     *    limit defined by <CODE>bufferSize</CODE>.
     *
     */
    public SnmpInformRequest snmpInformRequest(SnmpPeer peer,
                                               SnmpInformHandler cb,
                                               SnmpOid trapOid,
                                               SnmpVarBindList varBindList) 
        throws IllegalStateException, IOException, SnmpStatusException {
        SnmpParameters p = (SnmpParameters) peer.getParams();
        return snmpInformRequest(peer.getDestAddr(), 
                                 peer.getDestPort(), 
                                 p.getInformCommunity(),
                                 cb,
                                 trapOid,
                                 varBindList);
    }
    
    /**
     * Method that maps an SNMP error status in the passed protocolVersion 
     * according to the provided pdu type.
     * @param errorStatus The error status to convert.
     * @param protocolVersion The protocol version.
     * @param reqPduType The pdu type.
     */
    public static final int mapErrorStatus(int errorStatus, 
                                           int protocolVersion,
                                           int reqPduType) {
        return SnmpSubRequestHandler.mapErrorStatus(errorStatus,
                                                    protocolVersion,
                                                    reqPduType);
    }
    
    private SnmpInformRequest snmpInformRequest(InetAddress addr, 
                                                int port,
                                                String cs, 
                                                SnmpInformHandler cb, 
                                                SnmpOid trapOid, 
                                                SnmpVarBindList varBindList) 
        throws IllegalStateException, IOException, SnmpStatusException {
        if (!isActive()) {
            throw new IllegalStateException(
             "Start SNMP adaptor server before carrying out this operation");
        }
        if (logger.finerOn()) {
            logger.finer("snmpInformRequest", "trapOid=" + trapOid);
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
        return informSession.makeAsyncRequest(addr, cs, cb, fullVbl, port) ;
    }

    
    /**
     * Open informSocket if it's not already done.
     */
    synchronized void openInformSocketIfNeeded() throws SocketException {
        if (informSession == null) {
            informSession = new SnmpSession(this) ;
            if (logger.finerOn()) {
                logger.finer("openInformSocketIfNeeded",
                      "to send inform requests and receive inform responses");
            }
        }
    }

    /**
     * Close informSocket if the SNMP protocol adaptor is not ONLINE.
     */
    synchronized void closeInformSocketIfNeeded() {
        if ((informSession != null) && (state != ONLINE)) {
            informSession.destroySession() ;
            informSession = null ;
        }
    }
    
    /** 
     * Gets the IP address to bind.
     * This getter is used to initialize the DatagramSocket in the 
     * SnmpSocket object created for the inform request stuff.
     */
    InetAddress getAddress() {
        return address; 
    }

    public String getHost() {
        // Determines actual address
        //
        final InetAddress actual;
        if (socket != null) actual = socket.getLocalAddress();
        else if (address != null) actual = address;
        else actual = null;
        
        // If no address is configured yet - we will be using the
        // default defined in the super class.
        //
        if (actual == null) return super.getHost();         
        else if (actual.isAnyLocalAddress()) try {
            // If we're using a wildcard - then we should not return the 
            // wildcard because this would be meaningless to remote clients. 
            // Instead we return the local host name.
            //
            return actual.getLocalHost().getHostName();
        } catch (UnknownHostException x) {
            // should not happen unless there's a misconfiguration.
            logger.debug("getHost","Can't determine local host name: " + x);
            return "localhost";
        }
        
        // Return the hostname of the actual address.
        //
        return actual.getHostName();
    } 
    
    public  int getPort() {
        if (socket != null) return socket.getLocalPort();
        return super.getPort();
    }

    
    // PROTECTED METHODS
    //------------------
    
    /**
     * Finalizer of the SNMP protocol adaptor objects.
     * This method is called by the garbage collector on an object 
     * when garbage collection determines that there are no more references 
     * to the object.
     * <P>Closes the datagram socket associated to this SNMP protocol adaptor.
     */
    protected void finalize() {
        try {
            if (socket != null) {
                socket.close() ;
                socket = null ;
            }
        } finally {
            try {
                threadService.terminate();
            } catch (Exception e) {}
        }
    }
    
    // PACKAGE METHODS
    //----------------
    
    /**
     * Returns the string used in logging.
     */
    String makeDebugTag() {
        return "SnmpAdaptorServer["+ getProtocol() + ":" + getPort() + "]";    
    }
    
    ClassLogger makeLogger(String dbgTag) {
        return new ModifiableClassLogger(ClassLogger.LOGGER_ADAPTOR_SNMP,
                                         dbgTag);
    }
    
    void updateRequestCounters(int pduType) {
        switch(pduType)  {
            
        case pduGetRequestPdu:
            snmpInGetRequests++;
            break;
        case pduGetNextRequestPdu:
            snmpInGetNexts++;
            break;
        case pduSetRequestPdu:
            snmpInSetRequests++;
            break;
        default:
            break;
        }
        snmpInPkts++ ;
    }
  
    void updateErrorCounters(int errorStatus) {
        switch(errorStatus) {
            
        case snmpRspNoError:
            snmpOutGetResponses++;
            break;
        case snmpRspGenErr:
            snmpOutGenErrs++;
            break;
        case snmpRspBadValue:
            snmpOutBadValues++;
            break;
        case snmpRspNoSuchName:
            snmpOutNoSuchNames++;
            break;
        case snmpRspTooBig:
            snmpOutTooBigs++;
            break;
        default:
            break;
        }
        snmpOutPkts++ ;
    }
  
    void updateVarCounters(int pduType, int n) {
        switch(pduType) {
            
        case pduGetRequestPdu:
        case pduGetNextRequestPdu:
        case pduGetBulkRequestPdu:
            snmpInTotalReqVars += n ;
            break ;
        case pduSetRequestPdu:
            snmpInTotalSetVars += n ;
            break ;
        }
    }
  
    void incSnmpInASNParseErrs(int n) {
        snmpInASNParseErrs += n ;
    }
  
    void incSnmpInBadVersions(int n) {
        snmpInBadVersions += n ;
    }
  
    void incSnmpInBadCommunityUses(int n) {
        snmpInBadCommunityUses += n ;
    }
  
    void incSnmpInBadCommunityNames(int n) {
        snmpInBadCommunityNames += n ;
    }
    
    void incSnmpSilentDrops(int n) {
        snmpSilentDrops += n ;
    }
    // PRIVATE METHODS
    //----------------
  
    /**
     * Returns the time (in hundredths of second) elapsed since the SNMP 
     * protocol adaptor startup.
     */
    public long getSysUpTime() {
        return (System.currentTimeMillis() - startUpTime) / 10 ;
    }
  
    /**
     * Control the way the SnmpAdaptorServer service is deserialized.
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
      
        // Call the default deserialization of the object.
        //
        stream.defaultReadObject();
      
        // Call the specific initialization for the SnmpAdaptorServer service.
        // This is for transient structures to be initialized to specific 
        // default values.
        //
        mibs      = new Vector() ;
    }
  
    private void updateLogger() {
        // Update the logger with the right className
        ((ModifiableClassLogger)logger).setClassName(makeDebugTag());
    }

    /**
     * Common initializations.
     */
    private void init(Object acl, int p, InetAddress a) {
  
        root= new SnmpMibTree();
        
        // The default Agent is initialized with a SnmpErrorHandlerAgent agent.
        defaultAgent = new SnmpErrorHandlerAgent();
        root.setDefaultAgent(defaultAgent);

        // For the trap time, use the time the agent started ...
        //
        startUpTime= java.lang.System.currentTimeMillis();
        maxActiveClientCount = 10;
    
        // Create the default message factory
        pduFactory = new SnmpPduFactoryBER() ;

        setPort(p);
        
        ipacl = acl ;

        address = a ;

        if(logger.finerOn())
            logger.finer("init", "SnmpAdaptorServer will bind to " + address);
    
        if ((ipacl == null) && (useAcl == true))
            throw new IllegalArgumentException("ACL object cannot be null") ;

        threadService = new ThreadService(threadNumber);
    }

    SnmpMibAgent getDefaultAgent() {
        return defaultAgent;
    }

    /**
     * Package method that returns the default thread number initialized
     * from a static property. 
     * Introduced to avoid duplication of the aforementioned initialization
     * code.
     * @return The thread number initialized statically from {@link 
     * #THREAD_NUMBER_PROPERTY}.
     **/
    static int getThreadNumber() {
        return threadNumber;
    }

}

