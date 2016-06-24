/*
 * @(#)file      SnmpTrapReceiver.java
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
import java.net.SocketException;
import java.net.InetAddress;
import java.io.IOException;

// RI import
//
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.manager.SnmpTrapListener;
import com.sun.management.snmp.SnmpPduRequest;
import com.sun.management.snmp.SnmpPduTrap;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpScopedPduRequest;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineParameters;
import com.sun.management.snmp.SnmpEngineFactory;

// jdmk import
//
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.agent.SnmpTrap;
import com.sun.management.comm.SnmpV3AdaptorServer;
import com.sun.jdmk.tasks.TaskServer;
import com.sun.jdmk.tasks.DaemonTaskServer;
import com.sun.management.snmp.SnmpEventReportDispatcher;

import com.sun.management.snmp.JdmkEngineFactory;

import com.sun.management.internal.snmp.SnmpTools;

//Listen to trap and pass to forwarder.
class TrapListener implements SnmpTrapListener {
    // Debug Tag.
    private final static String dbgTag = "TrapListener";

    private SnmpTrapReceiver forwarder = null;
    
    TrapListener(SnmpTrapReceiver forwarder) {
	this.forwarder = forwarder;	
    }

    public void processSnmpTrapV1(SnmpPduTrap trap) {
	printV1Trap(trap);
	forwarder.receivedV1(trap);	
    }

    public void processSnmpTrapV2(SnmpPduRequest trap) {
        printV2Trap(trap);
	forwarder.receivedV2(trap);
    }
    
    public  void processSnmpTrapV3(SnmpScopedPduRequest trap) {
	printV3Trap(trap);
	forwarder.receivedV3(trap);
    }
    
    private void printV1Trap(SnmpPduTrap trap) {
	if(logger.finerOn()) {
	    logger.finer("printV1Trap", "received " + 
		  "V1 trap :");
	    logger.finer("printV1Trap", "\tCommunity " + 
		  new String(trap.community));
	    logger.finer("printV1Trap", "\tEnterprise " + trap.enterprise);
	    logger.finer("printV1Trap", "\tGeneric " + trap.genericTrap);
	    logger.finer("printV1Trap", "\tSpecific " + trap.specificTrap);
	    logger.finer("printV1Trap", "\tTimeStamp " + trap.timeStamp);
	    logger.finer("printV1Trap", "\tAgent adress " + 
		  trap.agentAddr.stringValue());
	    for(int i = 0; i < trap.varBindList.length; i++)
		logger.finer("printV1Trap", "oid : " + 
		      trap.varBindList[i].getOid() + " val : " + 
		      trap.varBindList[i].getSnmpValue() + "\n");
	    
	    logger.finer("printV1Trap","************************************"+
			 "************\n");
	}   
    } 
    
    private void printV2Trap(SnmpPduRequest pdu) {
	if(logger.finerOn()) {
	    logger.finer("printV2Trap", "received " + 
			 "V2 trap :");
	    logger.finer("printV2Trap", "\tCommunity " + 
			 new String(pdu.community));
	    SnmpVarBind[] vars = pdu.varBindList;
	    for(int i = 0; i < vars.length; i++) {
		logger.finer("printV2Trap", "OID " + vars[i].getOid());
		logger.finer("printV2Trap", "Value " + vars[i].getSnmpValue());
	    }	
	    logger.finer("printV2Trap","************************************"+
			 "************\n");
	}
    }
    
    private void printV3Trap(SnmpScopedPduRequest trap) {
	if(logger.finerOn()) {
	    logger.finer("printV3Trap", "received " + 
		  "V3 trap :");
	    logger.finer("printV3Trap", "\tContextEngineId : " +
		  new String(SnmpTools.binary2ascii(trap.contextEngineId)) 
		  + "\n");
	    logger.finer("printV3Trap", "\tContextName : " + 
		  new String(trap.contextName) + "\n");
	    logger.finer("printV3Trap", "\tMsgFlags : " +trap.msgFlags+ "\n");
	    logger.finer("printV3Trap", "\tMsgMaxSize : " +
			 trap.msgMaxSize + "\n");
	    logger.finer("printV3Trap", "\tMsgSecurityModel : " + 
		  trap.msgSecurityModel + "\n");
	    logger.finer("printV3Trap", "\tAgent list :"+"\n");
	    for(int i = 0; i < trap.varBindList.length; i++)
		logger.finer("printV3Trap", "oid : " + 
		      trap.varBindList[i].getOid() + " val : " + 
		      trap.varBindList[i].getSnmpValue() + "\n");
	    logger.finer("printV3Trap", 
			 "************************************" +
			 "************\n");
	}
    }
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,dbgTag);
}

/**
 * This MBean allows you to receive traps of subagents.
 *<p> This trap receiver listen on a dedicated port and forward traps 
 *    to some classes.
 * <p> To enable trap receiving you must start the receiver.
 *
 * @since Java DMK 5.1
 */
public class SnmpTrapReceiver {

    // Debug Tag.
    private final static String dbgTag = "SnmpTrapReceiver";
    // Used to name threads.
    private static int count = 0;

    private TrapListener listener = null;
    private SnmpEventReportDispatcher dispatcher = null;
    private SnmpEngine eng = null;
    private boolean started = false;
    private int port = 0;
    private Thread dispatchThread = null;
    private boolean receiveAsGeneric = false;
    private InetAddress address = null;
    private final TaskServer packetTaskServer;
    private final TaskServer callbackTaskServer;

    /**
     * Instantiate a <CODE> SnmpTrapForwarder</CODE> that will forward 
     * received traps according to its configuration.
     * @param engine The SNMP engine to use.
     * @param port The port the forwarder is listening to.
     * @param address The Ip address the dispatcher will listen for.
     *        events on. If null, the dispatcher will listen on localhost.
     */
    public SnmpTrapReceiver(SnmpEngine engine, 
			    int port,
			    InetAddress address) 
	throws IllegalArgumentException {
	this.eng = engine;
	listener = new TrapListener(this);
	this.port = port;
	this.address = address;
	packetTaskServer = createPacketTaskServer();
	callbackTaskServer = createCallbackTaskServer();
    }

    /**
     * Instantiate a <CODE> SnmpTrapForwarder</CODE> that will forward 
     * received traps according to its configuration.
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
     * @param parameters The engine parameters to use. 
     * @param factory The factory to use in order to create the engine.
     * @param port The port the forwarder is listening to.
     * @param address The Ip address the dispatcher will listen for.
     */
    public SnmpTrapReceiver(SnmpEngineParameters parameters,
			    SnmpEngineFactory factory, 
			    int port,
			    InetAddress address) 
	throws IllegalArgumentException {
	if(parameters == null)
	    parameters = new SnmpEngineParameters();
	if(factory == null)
	    factory = new JdmkEngineFactory();

	this.eng = factory.createEngine(parameters);
	listener = new TrapListener(this);
	this.port = port;
	this.address = address;
	packetTaskServer = createPacketTaskServer();
	callbackTaskServer = createCallbackTaskServer();
    }
    
    /**
     * Call this method in order to be called by <CODE>receivedTrap</CODE> 
     * (if true). By default the methods <CODE>receivedV1Trap</CODE> or 
     * <CODE>receivedV2Trap</CODE> or <CODE>receivedV3Trap</CODE> are 
     * called when the receiver receives a trap (false).
     * @param val True, receive as generic. False, receive a different 
     *        call for each kind of trap (SNMP V1, V2 or V3).
     */
    public void receiveAsGeneric(boolean val) {
	receiveAsGeneric = val;
    }
    
    /**
     * Start listening for traps.
     */
    public synchronized void start() throws SocketException {
	if(started) return;
	dispatcher = new 
	    SnmpEventReportDispatcher(eng, port, address, packetTaskServer, 
				      callbackTaskServer); 
	dispatcher.addTrapListener(listener);
	started = true;
	dispatchThread = new Thread(dispatcher);
	dispatchThread.start();
    }
    
    /**
     * Stop listening for traps. The socket on which the trap forwarder is listening for traps is closed.
     */
    public synchronized void stop() throws SocketException {
	if(!started) return;
	try {
	    dispatcher.close();
	} catch (IOException e) {
	    logger.finest("stop","Exception caught while closing dispatcher: " + e);
	}
	started = false;
	dispatchThread.interrupt();
    }
   
    synchronized void receivedV1(SnmpPduTrap trap) {
	if(receiveAsGeneric) {
	    SnmpTrap gentrap = new SnmpTrap(trap);
	    receivedTrap(gentrap);
	}
	else
	    receivedV1Trap(trap);
    }
    
    synchronized void receivedV2(SnmpPduRequest trap) {
	if(receiveAsGeneric) {
	    SnmpTrap gentrap = new SnmpTrap(trap);
	    receivedTrap(gentrap);
	}
	else
	    receivedV2Trap(trap);
    }

    synchronized void receivedV3(SnmpScopedPduRequest trap) {
	if(receiveAsGeneric) {
	    SnmpTrap gentrap = new SnmpTrap(trap);
	    receivedTrap(gentrap);
	}
	else
	    receivedV3Trap(trap); 
    }
    
    /**
     * Return the TaskServer that will be used to handle incoming
     * Trap/Inform PDUs.
     * <p>The default implementation of this method is to return a
     * new started {@link com.sun.jdmk.tasks.DaemonTaskServer}.
     * <p>This method is called from this object constructor.
     **/
    protected TaskServer createPacketTaskServer() {
	final DaemonTaskServer ts = new DaemonTaskServer();
	ts.start();
	return ts;
    }

    /**
     * Return the TaskServer that will be used to invoke callbacks.
     * The default implementation of this method is to return 
     * <code>null</code>, which means that callbacks will be directly 
     * invoked from the thread of the packetTaskServer.
     * <p>This method is called from this object constructor.
     * @see com.sun.jdmk.tasks.TaskServer
     **/
    protected TaskServer createCallbackTaskServer() {
	return null;
    }

    /**
     * Creates a new Thread to listen for incoming traps.
     * <p>The thread returned by this method must be runnable.
     * The default implementation of this method is:
     * <pre> return new Thread(dispatcher); </pre>
     * <p>This method is called each time this object is started.
     * @param dispatcher The dispatcher that will listen for incoming
     *        traps in the new thread.
     **/
    protected Thread createDispatcherThread(Runnable dispatcher) {
	return new Thread(dispatcher,"SnmpTrapReceiver["+(count++)+"]");
    }


    /**
     * You must implement this method in order to handle received traps 
     * in a generic way. The default implementation does nothing.
     * @param trap The received trap pdu.
     */
    protected void receivedTrap(SnmpTrap trap) {
    }

    /**
     * You must implement this method in order to handle received SNMP V1 
     * traps. The default implementation does nothing.
     * @param trap The received trap pdu.
     */
    protected void receivedV1Trap(SnmpPduTrap trap) {
    }
    /**
     * You must implement this method in order to handle received SNMP 
     * V2 traps. The default implementation does nothing.
     * @param trap The received trap pdu.
     */
    protected void receivedV2Trap(SnmpPduRequest trap) {
    }
    /**
     * You must implement this method in order to handle received SNMP 
     * V3 traps.
     * @param trap The received trap pdu.
     */
    protected void receivedV3Trap(SnmpScopedPduRequest trap) {
    }
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,dbgTag);
}
