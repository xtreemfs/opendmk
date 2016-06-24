/*
 * @(#)file      SnmpEventReportDispatcher.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.72
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
package com.sun.management.snmp.manager;


import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.InetAddress;
import java.util.Vector;
import java.util.Enumeration;
import java.io.InterruptedIOException;

import com.sun.management.snmp.SnmpScopedPduPacket;
import com.sun.management.snmp.SnmpPduRequest;
import com.sun.management.snmp.SnmpPduRequestType;
import com.sun.management.snmp.SnmpAckPdu;
import com.sun.management.snmp.SnmpPduTrap;
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpMessage;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineFactory;
import com.sun.management.snmp.SnmpEngineParameters;

import com.sun.management.snmp.SnmpPduFactoryBER;
import com.sun.management.internal.snmp.SnmpIncomingRequest;
import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.snmp.JdmkEngineFactory;
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.internal.snmp.SnmpBaseEngineFactory;
import com.sun.management.snmp.SnmpUnknownMsgProcModelException;
import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpBadSecurityLevelException;

import com.sun.jdmk.internal.ClassLogger;

/**
 * Implements an SNMP event report dispatcher.
 * <P>
 * Listener objects can be registered in an 
 * <CODE>SnmpEventReportDispatcher</CODE>.
 * <BR>The <CODE>SnmpEventReportDispatcher</CODE> listens for any incoming 
 * trap or inform PDU: each time it receives a trap or an inform PDU, it 
 * informs all the listeners.
 * <BR>By default, the dispatcher listens to the UDP port 162.
 * <P>
 * The <CODE>SnmpEventReportDispatcher</CODE> class implements 
 * <CODE>Runnable</CODE>: it is expected to be used as a 
 * <CODE>Thread</CODE> object. The run method never returns.
 * <P>
 * An <CODE>SnmpEventReportDispatcher</CODE> object maintains 2 lists of
 * listener objects. The listener objects implement the 
 * <CODE>SnmpTrapListener</CODE> or <CODE>SnmpInformListener</CODE>
 * interface and will be activated each time an SNMP event report (trap 
 * or inform PDU) is received. 
 * <P>
 * By default, the SnmpEventReportDispatcher will create one thread per 
 * received packet, and will invoke listener callbacks in that thread. 
 * Subclasses may override this behavior by redefining 
 * <tt>handlePacket()</tt> and <tt>handleCallback()</tt>.
 *
 * @see SnmpTrapListener
 * @see SnmpInformListener
 *
 * @since Java DMK 5.1
 */


public class SnmpEventReportDispatcher implements Runnable {

    /**
     * This class is an optimized mutable Vector - like object.
     * It gives public access to its underlying Object[] array, which
     * makes it possible to implement fast loops.
     * It is designed for those cases where looping over the list occurs
     * often compared to add/remove modifications.
     **/
    private static class ObjectList {

	public static int DEFAULT_CAPACITY = 10;

	public static int DEFAULT_INCREMENT = 10;

	private final class Enumeration 
	    implements java.util.Enumeration {
	    private int next=0;
	    private Enumeration() {
	    }

	    public boolean hasMoreElements() {
		synchronized (ObjectList.this) {
		    return (next < size());
		}
	    }

	    public Object nextElement() {
		synchronized (ObjectList.this) {
		    if (next < size()) {
			return ObjectList.this.list[next++];
		    } else {
			throw new java.util.NoSuchElementException();
		    }
		}
	    }

	}

	private final int DELTA;
	private int size;

	/**
	 * The list content. Any access to this variable must be protected
	 * by a synchronized block on the ObjectList object.
	 * Only read-only action should be performed on this object.
	 **/
	public  Object[] list;

	ObjectList() {
	    this(DEFAULT_CAPACITY,DEFAULT_INCREMENT);
	}

	ObjectList(int initialCapacity) {
	    this(initialCapacity,DEFAULT_INCREMENT);
	}

	ObjectList(int initialCapacity, int delta) {
	    size = 0; 
	    DELTA = delta;
	    list = allocate(initialCapacity);
	}

	/**
	 * Same behavior than {@link java.util.List#size()}.
	 **/
	public final int size() { return size;}

	/**
	 * Same behavior than {@link java.util.List#add(Object)}.
	 * Any access to this method should be protected in a synchronized 
	 * block on the ObjectList object.
	 **/
	public final boolean add(final Object o) {
	    if (o == null) return false;
	    if (size >= list.length) 
		resize();
	    list[size++]=o;
	    return true;
	}

	/**
	 * Same behavior than {@link java.util.List#add(int,Object)}.
	 * Any access to this method should be protected in a synchronized 
	 * block on the ObjectList object.
	 **/
	public final void add(final int index, final Object o) {
	    if (index >  size) throw new IndexOutOfBoundsException();
	    if (index >= list.length) resize();
	    if (index == size) {
		list[size++]=o;
		return;
	    }
	    java.lang.System.arraycopy(list,index,list,index+1,size-index);
	    list[index]=o;
	    size++;
	}

	/**
	 * Same behavior than {@link java.util.List#remove(Object)}.
	 * Note however that this implementation only check for reference
	 * equality: it does not call equals() on the list elements.
	 * Any access to this method should be protected in a synchronized 
	 * block on the ObjectList object.
	 **/
	public final boolean remove(final Object o) {
	    if (o == null) return false;

	    // will work only if protected by synchronized.
	    final int len=size;
	    final Object[] l = list;
	    for (int i=0; i<len ; i++) {
		if (l[i]==o) {
		    remove(i);
		    return true;
		}
	    }
	    return false;
	}

	/**
	 * Same behavior than {@link java.util.List#remove(int)}.
	 * Any access to this method should be protected in a synchronized 
	 * block on the ObjectList object.
	 **/
	public final Object remove(final int index) {
	    if (index >= size) return null;
	    final Object o = list[index];
	    list[index]=null;
	    if (index == --size) return o;
	    java.lang.System.arraycopy(list,index+1,list,index,
				       size-index);
	    return o;
	}
	/**
	 * Same behavior than {@link java.util.List#indexOf(Object)}.
	 * Any access to this method should be protected in a synchronized 
	 * block on the ObjectList object.
	 **/
	public final int indexOf(final Object o) {
	    if (o == null) return -1;

	    // will work only if protected by synchronized.
	    final int len=size;
	    final Object[] l = list;
	    for (int i=0; i<len ; i++) {
		if (l[i]==o) return i;
	    }
	    return -1;
	}
	
	/**
	 * Same behavior than {@link java.util.List#contains(Object)}.
	 * Any access to this method should be protected in a synchronized 
	 * block on the ObjectList object.
	 **/
	public final boolean contains(final Object o) {
	    // will work only if protected by synchronized.
	    return (indexOf(o) > -1);
	}

	/**
	 * Same behavior than {@link java.util.Vector#elements()}.
	 * Although the returned Enumeration is synchronized, it is better
	 * if any loop over the enumeration is be protected in a synchronized 
	 * block on the ObjectList object.
	 **/
	public final java.util.Enumeration elements() {
	    return new Enumeration();
	}

	/**
	 * Resize the list. Increase its capacity by DELTA elements.
	 * Any call to this method must be protected by a synchronized
	 * block on this ObjectList.
	 **/
	private final void resize() {
	    final Object[] newlist = allocate(list.length + DELTA);
	    java.lang.System.arraycopy(list,0,newlist,0,size);
	    list = newlist;
	}

	/**
	 * Allocate a new array of object of specified length.
	 **/
	private final Object[] allocate(final int length) {
	    return new Object[length];
	}
	
    }

    private final class PacketHandler implements Runnable {
	private final int version;
	private final DatagramPacket packet;
	
	PacketHandler(int version, DatagramPacket packet) {
	    this.version=version;
	    this.packet=packet;
	}
	public void run() {
	    try { 
		doHandlePacket(version,packet);
	    } catch (Exception e) {
		if (logger.finestOn()) {
		    logger.finest(
                          "Unexpected exception, failed to handle packet: ", 
			  e);
		}
	    }
	}
    }


    // PRIVATE VARIABLES
    //------------------
    
    //Kind of RFC 1907 counters
    
    private int snmpInTraps = 0;
   
    private int snmpInInforms = 0;
    
    private int snmpInPkts=0;
   
    private int snmpInASNParseErrs = 0;
    
    private int snmpInBadVersions = 0;

    private int snmpInvalidMsgs = 0;

    private int snmpUnknownSecurityModels = 0;
    
    private int port = 162; //The default port for SNMP event report listeners
    private final DatagramSocket dSocket;
    private final ObjectList trapListeners   = new ObjectList();
    private final ObjectList informListeners = new ObjectList();
    private SnmpPduFactory pduFactory = new SnmpPduFactoryBER() ;
    private SnmpEngineImpl engine         = null;
    private boolean enabled = true;

    static final private String InterruptSysCallMsg = 
	"Interrupted system call";
    
    // PACKAGE VARIABLES
    //------------------
    
    String dbgTag = "SnmpEventReportDispatcher";
    
    // PUBLIC METHODS
    //---------------

    /**
     * Initializes an <CODE>SnmpEventReportDispatcher</CODE> which 
     * listens on the port 162.   
     * It instantiates an <CODE> SnmpEngine. </CODE>  
     *  <P> WARNING : The SnmpEngineId is computed as follow:
     * <ul>
     * <li> If an lcd file is provided containing the property "
     *      localEngineID", this property value is used.</li>.
     * <li> If not, if the passed engineId is not null, this engine Id 
     *      is used.</li>
     * <li> If not, the dispatcher computes an <CODE>SnmpEngineId</CODE> 
     *      time based.</li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the 
     *            specified configuration file doesn't exist.
     * @exception SocketException If the object cannot bind to the port 162.
     */
    public SnmpEventReportDispatcher() 
	throws SocketException, IllegalArgumentException {
        this(new SnmpEngineParameters(),
	     new JdmkEngineFactory(),
	     162, null);
    }
    
    /**
     * Initializes an <CODE>SnmpEventReportDispatcher</CODE> which listens 
     * on the specified port.
     * It instantiates an <CODE> SnmpEngine. </CODE>  
     *  <P> WARNING : The SnmpEngineId is computed as follow:
     * <ul>
     * <li> If an lcd file is provided containing the property "
     *      localEngineID", this property value is used.</li>.
     * <li> If not, if the passed engineId is not null, this engine Id 
     *      is used.</li>
     * <li> If not, the session computes an <CODE>SnmpEngineId</CODE> 
     *      time based.</li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the 
     *            specified configuration file doesn't exist.
     * @param portNumber The port number.
     *
     * @exception SocketException If the object cannot bind to the 
     *            specified port.
     *
     */
    public SnmpEventReportDispatcher(int portNumber) 
	throws SocketException, IllegalArgumentException{
	this(new SnmpEngineParameters(),
	     new JdmkEngineFactory(),
	     portNumber,
	     null);
    }

    /**
     * Initializes an <CODE>SnmpEventReportDispatcher</CODE> which listens 
     * on the specified port.
     * It instantiates an <CODE> SnmpEngine. </CODE>  
     *  <P> WARNING : The SnmpEngineId is computed as follow:
     * <ul>
     * <li> If an lcd file is provided containing the property "
     *      localEngineID", this property value is used.</li>.
     * <li> If not, if the passed engineId is not null, this engine Id 
     *      is used.</li>
     * <li> If not, the dispatcher computes an <CODE>SnmpEngineId</CODE> 
     *      time based.</li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the 
     *            specified configuration file doesn't exist.
     * @param parameters The engine parameters to use. 
     * @param factory The factory to use in order to create the engine.
     * @param portNumber The port number.
     * @exception SocketException If the object cannot bind to the 
     *            specified port.
     *
     */    
    public SnmpEventReportDispatcher(SnmpEngineParameters parameters, 
				     SnmpEngineFactory factory,
				     int portNumber)
	throws SocketException, IllegalArgumentException {
	this(createEngine(parameters, factory),
	     portNumber,
	     null);
    }
    /**
     * Initializes an <CODE>SnmpEventReportDispatcher</CODE> which listens 
     * on the specified port.
     * It instantiates an <CODE> SnmpEngine. </CODE>  
     *  <P> WARNING : The SnmpEngineId is computed as follow:
     * <ul>
     * <li> If an lcd file is provided containing the property "
     *      localEngineID", this property value is used.</li>.
     * <li> If not, if the passed engineId is not null, this engine Id 
     *      is used.</li>
     * <li> If not, the dispatcher computes an <CODE>SnmpEngineId</CODE> 
     *      time based.</li>
     * </ul>
     * @exception java.lang.IllegalArgumentException If one of the 
     *            specified configuration file doesn't exist.
     * @param parameters The engine parameters to use. 
     * @param factory The factory to use in order to create the engine.
     * @param portNumber The port number.
     * @param address The Ip address the dispatcher will listen for events 
     *        on. If null, the dispatcher will listen on localhost.
     * @exception SocketException If the object cannot bind to the 
     *            specified port.
     *
     */    
    public SnmpEventReportDispatcher(SnmpEngineParameters parameters, 
				     SnmpEngineFactory factory,
				     int portNumber, 
				     InetAddress address)
	throws SocketException, IllegalArgumentException {
	this(createEngine(parameters, factory),
	     portNumber,
	     address);
    }
    
    /**
     * Initializes an <CODE>SnmpEventReportDispatcher</CODE> which listens 
     * on the specified port.
     *
     * @param engine The <CODE> SnmpEngine </CODE> to use.
     * @param portNumber The port number.
     * @exception IllegalArgumentException if the passed engine is null.
     * @exception SocketException If the object cannot bind to the 
     *            specified port.
     *
     */    
    public SnmpEventReportDispatcher(SnmpEngine engine,
				     int portNumber)
	throws SocketException, IllegalArgumentException {
	this(engine, portNumber, null);
    }

    /**
     * Initializes an <CODE>SnmpEventReportDispatcher</CODE> which listens 
     * on the specified port.
     * @param engine The <CODE> SnmpEngine </CODE> to use.
     * @param portNumber The port number.
     * @param address The Ip address the dispatcher will listen for events
     *        on. If null, the dispatcher will listen on localhost.
     * @exception IllegalArgumentException if the passed engine is null.
     * @exception SocketException If the object cannot bind to the 
     *            specified port.
     *
     */    
    public SnmpEventReportDispatcher(SnmpEngine engine,
				     int portNumber, 
				     InetAddress address)
	throws SocketException, IllegalArgumentException {
	
	if(engine == null) throw new 
	    IllegalArgumentException("Engine can't be null");
	this.engine = (SnmpEngineImpl) engine;
	port = portNumber;
	
	if(address != null)
	    dSocket = new DatagramSocket(port, address);
	else
	    dSocket = new DatagramSocket(port);
    }

    /**
     * Returns the number of received traps.
     * @return The number of successfully received traps.
     *
     */
    public Long getSnmpInTraps() {
	return new Long(snmpInTraps);
    }

    /**
     * Returns the number of received informs.
     * @return The number of successfully received informs.
     *
     */
    public Long getSnmpInInforms() {
	return new Long(snmpInInforms);
    }
    
    /**
     * Returns the <CODE> snmpInPkts </CODE> value defined in the RFC 1907 Snmp Group. 
     * <P> This value is the total number of received packets. It sums traps, informs, failed decoding packet, bad SNMP version packets and USM discovery requests.
     * @return The number of received packets.
     *
     */
    public Long getSnmpInPkts() {
	return new Long(snmpInPkts);
    }
    
    /**
     * Returns the <CODE> snmpInASNParseErrs </CODE> value defined in the RFC 1907 Snmp Group.
     * @return The number of packets for which the decoding failed.
     *
     */
    public Long getSnmpInASNParseErrs() {
	return new Long(snmpInASNParseErrs);
    }
    
    /**
     * Returns the <CODE> snmpInBadVersions </CODE> value defined in the RFC 1907 Snmp Group.
     * @return The number of packets for which the SNMP version was not supported.
     *
     */
    public Long getSnmpInBadVersions() {
	return new Long(snmpInBadVersions);
    }
    
    /**
     * Returns <CODE>snmpInvalidMsgs</CODE> as defined in RFC 2572 SNMP-MPD-MIB.
     * @return snmpInvalidMsgs counter.
     *
     */
    public Long getSnmpInvalidMsgs() {
	return new Long(snmpInvalidMsgs);
    }
    
    /**
     * Returns <CODE>snmpUnknownSecurityModels</CODE> as defined in RFC 2572 SNMP-MPD-MIB.
     * @return snmpUnknownSecurityModels counter.
     *
     */
    public Long getSnmpUnknownSecurityModels() {
	return new Long(snmpUnknownSecurityModels);
    }

    /**
     * Engine the <CODE> SnmpEventReportDispatcher </CODE> is linked with.
     *
     */
    public SnmpEngine getEngine() {
	return engine;
    }

    /**
     * Get the engine Id. The engine Id is used internally when dealing 
     * with import (SNMP V3 authoritative notions.)
     *
     */
    public SnmpEngineId getEngineId() {
	return engine.getEngineId();
    }

    /**
     * Closes the receiving socket, and terminates the receiving loop.
     *
     **/
    public void close() throws java.io.IOException {
	enabled(false);
	if (dSocket != null)
	    dSocket.close();
    }
    
    private synchronized void incSnmpInBadVersions(int n) {
	snmpInBadVersions += n;
    }
    
    private synchronized void incSnmpInASNParseErrs(int n) {
	snmpInASNParseErrs += n;
    }
    
    private synchronized void incSnmpInPkts(int n) {
	snmpInPkts += n;
    }

    private synchronized void incSnmpInInforms(int n) {
	snmpInInforms += n;
    }

    private synchronized void incSnmpInTraps(int n) {
	snmpInTraps += n;
    }

    private synchronized void incSnmpInvalidMsgs(int n) {
	snmpInvalidMsgs += n;
    }

    private synchronized void incSnmpUnknownSecurityModels(int n) {
	snmpUnknownSecurityModels += n;
    }

    private synchronized boolean enabled() {
	return enabled;
    }

    private synchronized boolean enabled(boolean enabled) {
	this.enabled=enabled;
	return enabled;
    }

    private static SnmpEngine createEngine(SnmpEngineParameters parameters, 
					   SnmpEngineFactory factory) 
	throws IllegalArgumentException {
	if(factory == null)
	    factory = new JdmkEngineFactory();
	if(parameters == null)
	    parameters = new SnmpEngineParameters();
	SnmpEngine engine = factory.createEngine(parameters);
	if(engine == null)
	    throw new IllegalArgumentException("The factory returned a null lengine. SnmpEventReportDispatcher initilization failed");

	return engine;
    }
    
    private final SnmpIncomingRequest getIncomingReq(final int version, 
					      final DatagramPacket packet) 
	throws SnmpStatusException, SnmpUnknownMsgProcModelException {
	
	// Access the right msg processing sub system
	final SnmpMsgProcessingSubSystem msgsys = 
	    engine.getMsgProcessingSubSystem();
	// Ask the sub system to create the right 
	// SnmpIncomingResponse instance that will decode the msg.
	return msgsys.getIncomingRequest(version,pduFactory);
	
    }

    private final SnmpPdu decodePdu(final int version, 
				    final SnmpIncomingRequest req,
				    final DatagramPacket packet) 
	throws SnmpStatusException, SnmpUnknownSecModelException,
	       SnmpBadSecurityLevelException {
	
	req.decodeMessage(packet.getData(),
			  packet.getLength(), 
			  packet.getAddress(),
			  packet.getPort());
	
	SnmpPdu pdu = null;
	if(!req.isReport())
	    pdu = req.decodeSnmpPdu();
	
	return pdu;
    }
    
    private final void handleTrap(final int version, 
				  final SnmpIncomingRequest req,
				  final DatagramPacket packet,
				  final SnmpPdu pdu) {
	final SnmpPdu trapPdu = pdu;
	//Increment counter
	incSnmpInTraps(1);	
	synchronized(trapListeners) {
	    final int size = trapListeners.size;
	    final Object[] list = trapListeners.list;
	    for (int i=0; i<size; i++) {
		final SnmpTrapListener listener = 
		    (SnmpTrapListener) list[i];
		final SnmpEventReportHandler handler =
		    new SnmpEventReportHandler(listener, trapPdu);
		handleCallback(handler);
	    }
	}
    }

    private final void acknowledgeInform(final int version,
					 final SnmpIncomingRequest req,
					 final DatagramPacket packet,
					 final SnmpPdu informPdu) {
	// Let's build the response packet.
	//DatagramPacket respPacket = 
	//    makeResponsePacket(packet, (SnmpPduPacket)informPdu);
	DatagramPacket respPacket = makeResponsePacket(req,
						       packet, 
						       informPdu);
	
	if (respPacket == null) return;

	// Send the response packet
	sendPacket(respPacket);
    }

    private final void handleInform(final int version, 
				    final SnmpIncomingRequest req,
				    final DatagramPacket packet,
				    final SnmpPdu pdu) {
	//final SnmpPduPacket informPdu = pdu;
	final SnmpPdu informPdu = pdu;
	
	//Increment counter
	incSnmpInInforms(1);
	// First we build a response and send it back to 
	// the initiator.
	//
	acknowledgeInform(version,req,packet,pdu);
	informPdu.type = SnmpDefinitions.pduInformRequestPdu;
	// Then we notify all the registered listeners.
	synchronized(informListeners) {
	    final int size = informListeners.size;
	    final Object[] list = informListeners.list;
	    for (int i=0; i<size; i++) {
		final SnmpInformListener listener = 
		    (SnmpInformListener) list[i];
		final SnmpEventReportHandler handler =
		    new SnmpEventReportHandler(listener, informPdu);
		handleCallback(handler);
	    }
	}
    }

    private final void sendPacket(final DatagramPacket respPacket) {
	if (respPacket == null) return;
	try {

	if (logger.finerOn()) {
	    logger.finer("run", "Response packet to be sent:\n" + 
		  SnmpMsg.dumpHexBuffer(respPacket.getData(), 0, 
					respPacket.getLength()));
	}

	    dSocket.send(respPacket) ;
	} catch (SocketException e) {
	    if (logger.finestOn()) {
		if (e.getMessage().
		    equals(InterruptSysCallMsg))
		    logger.finest("run", "interrupted");
		else {
		    logger.finest("run", "i/o exception");
		    logger.finest("run", e);
		}
	    }
	} catch(InterruptedIOException e) {
	    if (logger.finestOn()) {
		logger.finest("run", "interrupted");
	    }
	} catch(Exception e) {
	    if (logger.finestOn()) {
		logger.finest("run", 
		      "failure when sending response");
		logger.finest("run", e);
	    }
	}
    }

    private final void handleV3GetRequest(final int version,
					  final SnmpIncomingRequest req,
					  final DatagramPacket packet,
					  final SnmpPdu pdu) {
	if(!req.isReport()) { 
	    if (logger.finestOn()) {
		logger.finest("run", "trashed the packet: "+
		      " no report to send back to received request.");
	    }
	}
	
	if (logger.finestOn())
	    logger.finest("run", 
			  "Received a getRequest, send back the report");   

	// Let's build the response packet.
	DatagramPacket respPacket = 
	    makeResponsePacket(req, packet, pdu);

	if (respPacket == null) return;

	// Send the response packet 
	sendPacket(respPacket);
    }

    /**
     * This method invokes a listener callback. Do not call this method
     * directly.
     * <p>This method is provided as a hook for subclasses. The default
     * behavior of this method is: 
     * <pre>
     *  protected void handleCallback(final Runnable callbackHandler) {
     *     callbackHandler.run();
     *  }
     * </pre>
     * @param callbackHandler A Runnable object wrapping the listener 
     *        callback to be invoked.
     **/
    protected void handleCallback(final Runnable callbackHandler) {
	callbackHandler.run();
    }

    /**
     * This method handles a received SNMP DatagramPacket. Do not call 
     * this method directly.
     * <p>This method is provided as a hook for subclasses.
     * <pre>
     *  protected void handlePacket(final Runnable packetHandler) {
     *     (new Thread(packetHandler)).start();
     *  }
     * </pre>
     * @param packetHandler A Runnable object wrapping the packet 
     *        to be handled.
     **/
    protected void handlePacket(final Runnable packetHandler) {
	(new Thread(packetHandler)).start();
    }

    /**
     * This method handles a received SNMP DatagramPacket. Do not call 
     * this method directly.
     * <p>This method is provided as a hook for subclasses.
     * @param version The SNMP version of the packet as defined in
     *        {@link com.sun.management.snmp.SnmpDefinitions}.
     * @param packet The DatagramPacket to handle.
     **/
    private final void doHandlePacket(final int version,
				      final DatagramPacket packet) {
	final boolean finest = logger.finestOn();
	final boolean finer  = finest?true:logger.finerOn();
	try {
	    final SnmpIncomingRequest req = getIncomingReq(version,packet);
	    final SnmpPdu pdu;
	    try {
		pdu = decodePdu(version, req, packet);
	    }catch(SnmpStatusException e) {
		//If no report to send back but security failed means 
		//that a bad trap was received. Must reject the request.
		if(finest)
		    logger.finest("handlePacket", "Rejecting trap : "+ e);
		//Increment counter
		incSnmpInASNParseErrs(1);
		return;
	    }
	    
	    //We can have a report in case of Inform 
	    //or in case of Malformed encrypted trap : 
	    // encryption + reportableFlag;
	    if(req.isReport()) {
		if(finest)
		    logger.finest("handlePacket", "Report to send back");
		final DatagramPacket respPacket = 
		    makeResponsePacket(req, packet, null);
		if(respPacket == null) return;
		sendPacket(respPacket);
		return;
	    }
	    

	    // WE RECEIVED A TRAP.
	    //====================
	    if ((pdu.type == pdu.pduV1TrapPdu) || 
		(pdu.type == pdu.pduV2TrapPdu)) {
		if (finer) {
		    if (pdu.type == pdu.pduV1TrapPdu)
			logger.finer("run", "Received an SNMP trap V1");
		    else
			logger.finer("run", "Received an SNMP trap V2 or V3");
		}
		handleTrap(version, req, packet, pdu);
	    }

	    // WE RECEIVED AN INFORM REQUEST.
	    //===============================
	    else if (pdu.type == pdu.pduInformRequestPdu) {
		if (finer) {
		    logger.finer("run", "Received an Inform Request");
		}
		handleInform(version, req, packet, pdu);
	    }

	    // WE RECEIVED A V3 GET REQUEST (timeliness, discovery)
	    //=====================================================
	    // NOTE: req !=null:
	    // THIS TEST IS THERE FOR BINARY COMPATIBILITY PURPOSE. 
	    // SHOULD BE REMOVED.

	    else if ((pdu.type == pdu.pduGetRequestPdu) &&
		     (version == SnmpDefinitions.snmpVersionThree)) {
		if (finer) {
		    logger.finer("run", "Received a V3 Get Request " +
			  "(timeliness discovery)");
		}
		handleV3GetRequest(version, req, packet, pdu);
	    } 

	    // WE RECEIVED SOMETHING UNEXPECTED
	    //=================================
	    else {
		if (finer) {
		    logger.finer("run", "trashed the packet: bad PDU type."); 
		}
		if (finest) {
		    logger.finest("run", "trashed the packet" + 
			  " because it's not an SNMP event report"
			  + " or get request (timeliness discovery)");
		}
	    }
	} catch (SnmpStatusException e) {
	    
	    if (finest) {
		logger.finest("run", "Response packet encoding failed " + e);
	    }
	} catch (SnmpUnknownSecModelException e) {
	    if (finest) {
		logger.finest("run", "packet decoding failed " + e);
	    }
	    // Increment counter
	    incSnmpUnknownSecurityModels(1);
	} catch (SnmpUnknownMsgProcModelException e) {
	    if (finest) {
		logger.finest("run", "Unknown Msg processing model " + e);
	    }
	    // Increment counter
	    incSnmpInBadVersions(1);
	} catch (SnmpBadSecurityLevelException e) {
	    if (finest) {
		logger.finest("run", "Invalid msg, bad security level " + e);
	    }
	    // Increment counter
	    incSnmpInvalidMsgs(1);
	}
    }


    /**
     * Dispatching loop. This method waits for an event report to be 
     * received and activates each registered listener.
     * <p>
     * This method is normally called by <CODE>Thread.start</CODE>.
     */
    public void run() {

        while (enabled()) {
            try {
                final byte dBuffer[] = 
		    new byte[SnmpPeer.defaultSnmpRequestPktSize];
                final DatagramPacket packet = 
		    new DatagramPacket(dBuffer, 
				       SnmpPeer.defaultSnmpRequestPktSize);
                dSocket.setSoTimeout(0);
                if (logger.finerOn()) {
                    logger.finer("run", "Now waiting for event reports...");
                }
                dSocket.receive(packet);
                if (logger.finerOn()) {
                    logger.finer("run", "Received a packet from : " + 
			  packet.getAddress().toString() + 
			  ", Length = " + packet.getLength());
                }

		// Increments counter
		incSnmpInPkts(1);
		
		final int version = 
		    SnmpMsg.getProtocolVersion(packet.getData());
		
		handlePacket(new PacketHandler(version,packet));

	    }catch (SnmpStatusException e) {
		if (logger.finestOn()) {
		    logger.finest("run", "packet decoding failed");
		}
		
		// Increments counter
		incSnmpInASNParseErrs(1);
	    } catch (Exception e) {
		if (logger.finestOn()) {
		    logger.finest("run", e);
		}
	    }
	}
    }
    
    
    /**
     * Adds a trap listener to this <CODE>SnmpEventReportDispatcher</CODE>.
     *
     * @param handler The listener to add.
     */
    public void addTrapListener(SnmpTrapListener handler) {
	synchronized (trapListeners) {
	    trapListeners.add(handler);
	}
    }
 
    /**
     * Adds an inform request listener to this 
     * <CODE>SnmpEventReportDispatcher</CODE>.
     *
     * @param handler The listener to add.
     */
    public void addInformListener(SnmpInformListener handler) {
	synchronized (informListeners) {
	    informListeners.add(handler);
	}
    }
    
    /**
     * Removes a trap listener.
     *
     * @param handler The listener to be removed.
     */
    public void removeTrapListener(SnmpTrapListener handler) {
	synchronized (trapListeners) {
	    trapListeners.remove(handler);
	}
    }
    
    /**
     * Removes an inform request listener.
     *
     * @param handler The listener to be removed.
     */
    public void removeInformListener(SnmpInformListener handler) {
	synchronized (informListeners) {
	    informListeners.remove(handler);
	}
    }
    
    /**
     * Returns <CODE>true</CODE> if the specified object is listening to traps.
     *
     * @param handler A listener.
     * @return <CODE>true</CODE> if it is a registered listener, 
     * <CODE>false</CODE> otherwise.
     */
    public boolean containsTrapListener(SnmpTrapListener handler) {
	synchronized (trapListeners) {
	    return trapListeners.contains(handler);
	}
    }

    /**
     * Returns <CODE>true</CODE> if the specified object is listening 
     * to inform requests.
     *
     * @param handler A listener.
     * @return <CODE>true</CODE> if it is a registered listener, 
     *         <CODE>false</CODE> otherwise.
     */
    public boolean containsInformListener(SnmpInformListener handler) {
	synchronized (informListeners) {
	    return informListeners.contains(handler);
	}
    }

    /**
     * Gets all of the trap listeners.
     *
     * @return An enumeration of <CODE>SnmpTrapListener</CODE> objects.
     */
    public Enumeration getTrapListeners() {
	synchronized (trapListeners) {
	    return trapListeners.elements();
	}
    }

    /**
     * Gets all of the inform request listeners.
     *
     * @return An enumeration of <CODE>SnmpInformListener</CODE> objects.
     */
    public Enumeration getInformListeners() {
	synchronized (informListeners) {
	    return informListeners.elements();
	}
    }

    /**
     * Gets the PDU factory associated to this 
     * <CODE>SnmpEventReportDispatcher</CODE>.
     *
     * @return The PDU factory (always non-null).
     */
    public SnmpPduFactory getPduFactory() {
        return pduFactory ;
    }

    /**
     * Sets the PDU factory associated to this 
     * <CODE>SnmpEventReportDispatcher</CODE>.
     *
     * @param factory The PDU factory (if null, the default factory is set).
     */
    public void setPduFactory(SnmpPduFactory factory) {
        if (factory == null)
            factory = new SnmpPduFactoryBER() ;
        pduFactory = factory ;
    }
    
    // PRIVATE METHODS
    //----------------
    
    /**
     * Here we make a response packet from a request packet.
     * We return null if there no response packet to sent.
     */
    //private DatagramPacket makeResponsePacket(DatagramPacket reqPacket, SnmpPduPacket reqPdu) {
    private DatagramPacket makeResponsePacket(SnmpIncomingRequest req,
					      DatagramPacket reqPacket, 
					      SnmpPdu reqPdu) {
        
        DatagramPacket respPacket = null;
	SnmpMsg respMsg = makeResponseMessage(req, reqPacket, reqPdu);
    
        // Try to transform the response SnmpMessage into response packet.
        //
	try {
	    if(req.isReport() ||
	       respMsg != null) {
		reqPacket.setLength(req.encodeMessage(reqPacket.getData()));
		respPacket = reqPacket;
	    }
	}catch(SnmpTooBigException e) {
	    if (logger.finestOn()) {
		logger.finest("makeResponsePacket", 
		      "response message is too big");
	    }
	    try {
		SnmpPdu respPdu = newTooBigPdu(reqPdu);
		respMsg = req.encodeSnmpPdu(respPdu, 
					    reqPacket.getData().length);
		reqPacket.setLength(req.encodeMessage(
						      reqPacket.getData()));
		respPacket = reqPacket;
	    }
	    catch(SnmpStatusException ee) {
		throw new InternalError();
	    }
	    catch(SnmpTooBigException ee) {
		if (logger.finestOn()) {
		    logger.finest("makeResponsePacket", 
			  "'too big' is 'too big' !!!");
		}
	    }
	}
	return respPacket ;
    }
  
    /**
     * Here we make a response message from a request message.
     * We return null if there is no message to reply.
     */
    //private SnmpMessage makeResponseMessage(DatagramPacket reqPacket, SnmpPduPacket reqPdu) {
    private SnmpMsg makeResponseMessage(SnmpIncomingRequest req,
					DatagramPacket reqPacket, 
					SnmpPdu reqPdu) {
        
        //SnmpMessage respMsg = null;
	SnmpMsg respMsg = null;
        
        // Make the response PDU if any.
        //
        //SnmpPduPacket respPdu = null;
	SnmpPdu respPdu = null;
        if (!req.isReport() && 
	    reqPdu != null) {
            respPdu = makeResponsePdu(reqPdu);
        }
    
        // Try to transform the response PDU into a response message if any.
        //
        if (respPdu != null) {
            try {
		respMsg = req.encodeSnmpPdu(respPdu, 
					    reqPacket.getData().length);
            }
            catch(SnmpStatusException e) {
                if (logger.finestOn()) {
                    logger.finest("makeResponseMessage", 
			  "failure when encoding the response message");
                    logger.finest("makeResponseMessage", e);
                }
                respMsg = null;
            }
            catch(SnmpTooBigException e) {
                if (logger.finestOn()) {
                    logger.finest("makeResponseMessage", 
			  "response message is too big");
                }
                try {
                    respPdu = newTooBigPdu(reqPdu);
		    respMsg = req.encodeSnmpPdu(respPdu, 
						reqPacket.getData().length);
                }
                catch(SnmpTooBigException ee) {
                    if (logger.finestOn()) {
                        logger.finest("makeResponseMessage", 
			      "'too big' is 'too big' !!!");
                    }
                    respMsg = null;
                }
                catch(Exception ee) {
                    respMsg = null;
                }
            }
        }
	    
        return respMsg;
    }
  
    /**
     * Here we make a response PDU from a request PDU.
     * We return null if there is no PDU to reply.
     */
    //private SnmpPduPacket makeResponsePdu(SnmpPduPacket reqPdu) {
    private SnmpPdu makeResponsePdu(SnmpPdu reqPdu) {
        
        //SnmpPduPacket respPdu = null ;
	SnmpPdu respPdu = null;
        
        // Check if the pdu correspond to an inform request.
        //
        if (checkPduType(reqPdu)) {
            // The response PDU of an inform request has the same values in 
	    // its request-id, error-status, error-index and 
	    // variable-bindings fields as the received inform request PDU.
            //
            respPdu = reqPdu;
	    respPdu.type = SnmpDefinitions.pduGetResponsePdu;
        }
        return respPdu ;
    }
    
    /**
     * Check the type of the pdu: only the inform request is accepted.
     */
    //private boolean checkPduType(SnmpPduPacket reqPdu) {
    private boolean checkPduType(SnmpPdu reqPdu) {
        
        if (reqPdu.type == SnmpDefinitions.pduInformRequestPdu)
            return true;
        
        if (logger.finestOn()) {
            logger.finest("checkPduType", "cannot respond to this kind of PDU");
        }
    
        return false;
    }
    
    //private SnmpPduPacket newTooBigPdu(SnmpPduPacket reqPdu) {
    private SnmpPdu newTooBigPdu(SnmpPdu reqPdu) {
        //SnmpPduRequest result = new SnmpPduRequest();
    	SnmpAckPdu ackpdu = (SnmpAckPdu) reqPdu;
	SnmpPdu pdu = ackpdu.getResponsePdu();
	pdu.varBindList = null;
	SnmpPduRequestType result = (SnmpPduRequestType) pdu; 
	result.setErrorStatus(SnmpDefinitions.snmpRspTooBig);
        result.setErrorIndex(0);
        //result.type = reqPdu.type;
        //result.version = reqPdu.version;
        //result.community = reqPdu.community;
        //result.requestId = reqPdu.requestId;
        //result.address = reqPdu.address;
        //result.port = reqPdu.port;
    
        //result.errorStatus = SnmpDefinitions.snmpRspTooBig;
        //result.errorIndex = 0;
        //result.varBindList = null;
        
        return pdu;
    }
        
    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,
			SnmpEventReportDispatcher.class.getName());

}
