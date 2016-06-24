/*
 * @(#)file      SnmpEngineImpl.java
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
package com.sun.management.internal.snmp;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.io.Serializable;

import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpUsmKeyHandler;
import com.sun.management.snmp.SnmpEngineFactory;
import com.sun.management.snmp.SnmpUnknownModelException;

import com.sun.management.internal.snmp.SnmpTools;
import com.sun.management.snmp.SnmpBadSecurityLevelException;
import com.sun.jdmk.internal.ClassLogger;

/**
 * This engine is conformant with the RFC 2571. It is the main object within 
 * an SNMP entity (agent, manager...). 
 * To an engine is associated an {@link com.sun.management.snmp.SnmpEngineId}. 
 * The way the engineId is retrieved is linked to the way the engine is 
 * instantiated. See each <CODE>SnmpEngine</CODE> constructor for more details.
 * An engine is composed of a set of sub systems 
 * {@link com.sun.management.internal.snmp.SnmpSubSystem}. A <CODE>Jdmk</CODE> 
 * engine can contain a :
 *<ul>
 *<li> Message Processing Sub System :  
 * {@link com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem}</li>
 *<li> Security Sub System : 
 * {@link com.sun.management.internal.snmp.SnmpSecuritySubSystem} </li>
 *<li> Access Control Sub System : 
 * {@link com.sun.management.internal.snmp.SnmpAccessControlSubSystem}</li>
 *</ul>
 *<P> Each sub system contains a set of models. A model is an implementation 
 * of a particular treatment (e.g. the User based Security Model defined in
 * RFC 2574 is a functional element dealing with authentication and privacy).
 *</P>
 * Engine instantiation is based on a factory. This factory, implementing 
 * mandatorily {@link com.sun.management.snmp.SnmpEngineFactory  
 * SnmpEngineFactory}
 * is set in the method <CODE>setFactory</CODE>.
 * <CODE>Jdmk</CODE> provides 3 factories :
 *<ul>
 *<li> {@link com.sun.management.internal.snmp.SnmpBaseEngineFactory}</li>
 *<li> {@link com.sun.management.internal.snmp.SnmpAgentEngineFactory}</li>
 *</ul>
 *
 * @since Java DMK 5.1
 */
public class SnmpEngineImpl implements SnmpEngine, Serializable {
    private static final long serialVersionUID = 8407722373486890776L;
    /**
     * Security level. No authentication, no privacy. Value is 0, as defined in RFC 2572
     */
    public static final int noAuthNoPriv = 0;
    /**
     * Security level. Authentication, no privacy. Value is 1, as defined in RFC 2572
     */
    public static final int authNoPriv = 1;
    /**
     * Security level. Authentication, privacy. Value is 3, as defined in RFC 2572
     */
    public static final int authPriv = 3;
    /**
     * Flag that indicates that a report is to be sent. Value is 4, as defined in RFC 2572
     */
    public static final int reportableFlag = 4;

    /**
     * Mask used to isolate authentication information within a message flag.
     */    
    public static final int authMask = 1;
    /**
     * Mask used to isolate privacy information within a message flag.
     */ 
    public static final int privMask = 2;
    /**
     * Mask used to isolate authentication and privacy information within a message flag.
     */
    public static final int authPrivMask = 3;

    private SnmpEngineId engineid = null;
    private SnmpEngineFactory factory = null;
    private long startTime = 0;

    private int boot = 0;
    private boolean checkOid = false;
    
    transient private SnmpUsmKeyHandler usmKeyHandler = null;
    transient private SnmpLcd lcd = null;

    transient private SnmpSecuritySubSystem securitySub = null;

    transient private SnmpMsgProcessingSubSystem messageSub = null;

    transient private SnmpAccessControlSubSystem accessSub = null;

    /**
     * Gets the engine time in seconds. This is the time from the last reboot.
     * @return The time from the last reboot.
     */
    public synchronized int getEngineTime() { 
	//We do the counter wrap in a lazt way. Each time Engine is asked for his time it checks. So if nobody use the Engine, the time can wrap and wrap again without incrementing nb boot. We can imagine that it is irrelevant due to the amount of time needed to wrap.
	long delta = (System.currentTimeMillis() / 1000) - startTime;
	if(delta >  0x7FFFFFFF) {
	    //67 years of running. That is a great thing!
	    //Reinitialize startTime.
	    startTime = System.currentTimeMillis() / 1000;

	    //Can't do anything with this counter.
	    if(boot != 0x7FFFFFFF)
		boot += 1;
	    //Store for future use.
	    storeNBBoots(boot);
	}
	
	return (int) ((System.currentTimeMillis() / 1000) - startTime); 
    }

    /**
     * Gets the engine Id. This is unique for each engine.
     * @return The engine Id object.
     */
    public SnmpEngineId getEngineId() {
	return engineid;
    }
    
    /**
     * Gets the Usm key handler.
     * @return The key handler.
     */
    public SnmpUsmKeyHandler getUsmKeyHandler() {
	return usmKeyHandler;
    }

    /**
     * Gets the engine Lcd.
     * @return The engine Lcd.
     */
    public SnmpLcd getLcd() {
	return lcd;
    }
    /**
     * Gets the engine boot number. This is the number of time this engine has rebooted. Each time an <CODE>SnmpEngine</CODE> is instantiated, it will read this value in its Lcd, and store back the value incremented by one.
     * @return The engine's number of reboot.
     */
    public int getEngineBoots() { 
	return boot; 
    }

     /**
     * Constructor. A Local Configuration Datastore is passed to the engine. It will be used to store and retrieve data (engine Id, engine boots). 
     * <P> WARNING : The SnmpEngineId is computed as follow:
     * <ul>
     * <li> If an lcd file is provided containing the property "localEngineID", this property value is used.</li>.
     * <li> If not, if the passed engineID is not null, this engine ID is used.</li>
     * <li> If not, a time based engineID is computed.</li>
     * </ul>
     * This constructor should be called by an <CODE>SnmpEngineFactory</CODE>. Don't call it directly.
     * @param fact The factory used to instantiate this engine.
     * @param lcd The local configuration datastore.
     * @param engineid The engine ID to use. If null is provided, an SnmpEngineId is computed using the current time.
     * @throws UnknownHostException Exception thrown, if the host name located in the property "localEngineID" is invalid.
     */
    public SnmpEngineImpl(SnmpEngineFactory fact,
			  SnmpLcd lcd,
			  SnmpEngineId engineid) throws UnknownHostException {
	
	init(lcd, fact);
	initEngineID();
	if(this.engineid == null) {
	    if(engineid != null)
		this.engineid = engineid;
	    else
		this.engineid = SnmpEngineId.createEngineId();
	}
	lcd.storeEngineId(this.engineid);
	if(logger.finerOn()) {
	    logger.finer("SnmpEngine", "LOCAL ENGINE ID: " + this.engineid);
	}
    }
    /**
     * Constructor. A Local Configuration Datastore is passed to the engine. It will be used to store and retrieve data (engine ID, engine boots). 
     * <P> WARNING : The SnmpEngineId is computed as follow:
     * <ul>
     * <li> If an lcd file is provided containing the property "localEngineID", this property value is used.</li>.
     * <li> If not, the passed address and port are used to compute one.</li>
     * </ul>
     * This constructor should be called by an <CODE>SnmpEngineFactory</CODE>. Don't call it directly.
     * @param fact The factory used to instantiate this engine.
     * @param lcd The local configuration datastore.
     * @param port UDP port to use in order to calculate the engine ID.
     * @param address An IP address used to calculate the engine ID.
     * @throws UnknownHostException Exception thrown, if the host name located in the property "localEngineID" is invalid.
     */
    public SnmpEngineImpl(SnmpEngineFactory fact,
			  SnmpLcd lcd,
			  InetAddress address,
			  int port) throws UnknownHostException {
	init(lcd, fact);
	initEngineID();
	if(engineid == null)
	    engineid = SnmpEngineId.createEngineId(address, port);

	lcd.storeEngineId(engineid);
	
	if(logger.finerOn()) {
	    logger.finer("SnmpEngine", "LOCAL ENGINE ID: " + engineid + " / " +
		  "LOCAL ENGINE NB BOOTS: " + boot + " / " +
		  "LOCAL ENGINE START TIME: " + getEngineTime());
	}
    }

    /**
     * Constructor. A Local Configuration Datastore is passed to the engine. It will be used to store and retrieve data (engine ID, engine boots). 
     * <P> WARNING : The SnmpEngineId is computed as follow:
     * <ul>
     * <li> If an lcd file is provided containing the property "localEngineID", this property value is used.</li>.
     * <li> If not, The passed port is used to compute one.</li>
     * </ul>
     * This constructor should be called by an <CODE>SnmpEngineFactory</CODE>. Don't call it directly.
     * @param fact The factory used to instantiate this engine.
     * @param lcd The local configuration datastore
     * @param port UDP port to use in order to calculate the engine ID.
     * @throws UnknownHostException Exception thrown, if the host name located in the property "localEngineID" is invalid.
     */
    public SnmpEngineImpl(SnmpEngineFactory fact,
			  SnmpLcd lcd,
			  int port) throws UnknownHostException {
	init(lcd, fact);
	initEngineID();
	if(engineid == null)
	   engineid = SnmpEngineId.createEngineId(port);

	lcd.storeEngineId(engineid);

	if(logger.finerOn()) {
	    logger.finer("SnmpEngine", "LOCAL ENGINE ID: " + engineid + " / " +
		  "LOCAL ENGINE NB BOOTS: " + boot + " / " +
		  "LOCAL ENGINE START TIME: " + getEngineTime());
	}
    }

    /**
     * Constructor. A Local Configuration Datastore is passed to the engine. It will be used to store and retrieve data (engine ID, engine boots). 
     * <P> WARNING : The SnmpEngineId is computed as follow:
     * <ul>
     * <li> If an lcd file is provided containing the property "localEngineID", this property value is used.</li>.
     * <li> If not, a time based engineID is computed.</li>
     * </ul>
     * When no configuration nor java property is set for the engine ID value, a unique time based engine ID will be generated.
     * This constructor should be called by an <CODE>SnmpEngineFactory</CODE>. Don't call it directly.
     * @param fact The factory used to instantiate this engine.
     * @param lcd The local configuration datastore.
     */
    public SnmpEngineImpl(SnmpEngineFactory fact,
			  SnmpLcd lcd) throws UnknownHostException {
	init(lcd, fact);
	initEngineID();
	if(engineid == null)
	    engineid = SnmpEngineId.createEngineId();
	
	lcd.storeEngineId(engineid);


	if(logger.finerOn()) {
	    logger.finer("SnmpEngine", "LOCAL ENGINE ID: " + engineid + " / " +
		  "LOCAL ENGINE NB BOOTS: " + boot + " / " +
		  "LOCAL ENGINE START TIME: " + getEngineTime());
	}
    }

    /**
     * Access Control will check the oids. By default is false.
     */
    public synchronized void activateCheckOid() {
	checkOid = true;
    }
    
    /**
     * Access Control will not check the oids. By default is false.
     */
    public synchronized void deactivateCheckOid() {
	checkOid = false;
    }
    
    /**
     * Access Control check or not the oids. By default is false.
     */
    public synchronized boolean isCheckOidActivated() {
	return checkOid;
    }

    //Do some check and store the nb boots value.
    private void storeNBBoots(int boot) {
	if(boot < 0 || boot == 0x7FFFFFFF) {
	    boot = 0x7FFFFFFF;
	    lcd.storeEngineBoots(boot);
	}
	else
	    lcd.storeEngineBoots(boot + 1);
    }

    // Initialize internal status.
    private void init(SnmpLcd lcd, SnmpEngineFactory fact) {
	this.factory = fact;
	this.lcd = lcd;
	boot = lcd.getEngineBoots();

	if(boot == -1 || boot == 0)
	    boot = 1;

	storeNBBoots(boot);

	startTime = System.currentTimeMillis() / 1000;

    }
    
    void setUsmKeyHandler(SnmpUsmKeyHandler usmKeyHandler) {
	this.usmKeyHandler = usmKeyHandler;
    }

    //Initialize the engineID.
    private void initEngineID() throws UnknownHostException {
	String id = lcd.getEngineId();
	if(id != null) {
	    engineid = SnmpEngineId.createEngineId(id);
	}
    }

  
    /**
     * Returns the Message Processing Sub System.
     * @return The Message Processing Sub System.
     */
    public SnmpMsgProcessingSubSystem getMsgProcessingSubSystem() {
	return messageSub;
    }

    /**
     * Sets the Message Processing Sub System.
     * @param sys The Message Processing Sub System.
     */
    public void setMsgProcessingSubSystem(SnmpMsgProcessingSubSystem sys) {
	messageSub = sys;
    }
    
     /**
     * Returns the Security Sub System.
     * @return The Security Sub System.
     */
    public SnmpSecuritySubSystem getSecuritySubSystem() {
	return securitySub;
    }
    /**
     * Sets the Security Sub System.
     * @param sys The Security Sub System.
     */
    public void setSecuritySubSystem(SnmpSecuritySubSystem sys) {
	securitySub = sys;
    }
     /**
     * Sets the Access Control Sub System.
     * @param sys The Access Control Sub System.
     */
    public void setAccessControlSubSystem(SnmpAccessControlSubSystem sys) {
	accessSub = sys;
    }

    /**
     * Returns the Access Control Sub System.
     * @return The Access Control Sub System.
     */
    public SnmpAccessControlSubSystem getAccessControlSubSystem() {
	return accessSub;
    }
    /**
     * Checks the passed msg flags according to the rules specified in RFC 2572.
     * @param msgFlags The msg flags.
     */
    public static void checkSecurityLevel(byte msgFlags) 
	throws SnmpBadSecurityLevelException {
	int secLevel = msgFlags & SnmpDefinitions.authPriv;
	if((secLevel & SnmpDefinitions.privMask) != 0)
	    if((secLevel & SnmpDefinitions.authMask) == 0) {
		throw new SnmpBadSecurityLevelException("Security level:"+ 
							" noAuthPriv!!!");
	    }
    }
    
    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpEngineImpl");

    String dbgTag = "SnmpEngineImpl"; 
}
