/*
 * @(#)file      UsmUserEntryImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.36
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
package com.sun.management.snmp.usm.usmmib;

// java imports
//
import java.io.Serializable;

// jmx imports
//
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpEngine;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanServer;

// jdmk imports
//
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.agent.SnmpMib;
import com.sun.management.snmp.EnumRowStatus;

import com.sun.management.snmp.agent.SnmpMibTable;
import com.sun.management.snmp.usm.SnmpUsmLcd;
import com.sun.management.snmp.usm.SnmpUsmSecureUser;
import com.sun.management.snmp.usm.SnmpUsmAlgorithmManager;
import com.sun.management.snmp.usm.SnmpUsmAlgorithm;
import com.sun.management.snmp.usm.SnmpUsm;
import com.sun.management.snmp.usm.SnmpUsmException;
import com.sun.management.internal.snmp.SnmpTools;
/**
 * The class is used for implementing the "UsmUserEntry" group.
 * The group is defined with the following oid: 1.3.6.1.6.3.15.1.2.2.1.
 * This entry is tied with a SnmpUsmSecureUser where the configuration is stored.
 * Every call made on the entry are forwarded to the secure user.
 *
 * @since Java DMK 5.1
 */
public class UsmUserEntryImpl extends UsmUserEntry {
    private static final long serialVersionUID = 7849396415731734840L;
    //The tied user.
    SnmpUsmSecureUser user = null;
    //The Usm lcd
    SnmpUsmLcd lcd = null;
    // A ref to an entry used when cloning a new row.
    UsmUserEntryImpl original = null;
    //The table meta.
    TableUsmUserTableImpl table = null; 
    private String dbgTag = "UsmUserEntryImpl";
    SnmpOid oid = null;
    //Prefix to use in order to register in MBean
    private String prefix = null;
    public UsmUserEntryImpl(SnmpEngine engine,
			    SnmpMib mib,
			    SnmpUsmLcd lcd,
			    SnmpUsmSecureUser user,
			    TableUsmUserTableImpl table) {
	super(mib);
	//Scoped with the SnmpEngineId.
	prefix = engine.getEngineId().toString() + "/UsmUserTable:";
	this.user = user;
	this.table = table;
	UsmUserAuthKeyChange = null;
	UsmUserPrivKeyChange = null;
	this.lcd = lcd;
    }
   
    //Tie with a new User.
    synchronized void setUser(SnmpUsmSecureUser user) {
	this.user = user;
    }
    
    //Get the tied user
    synchronized SnmpUsmSecureUser getUser() {
	return user;
    }

    //Translate from an algo object to an oid.
    private String convertAuthAlgorithm(SnmpUsmAlgorithm algo){
	if( (getUser().getSecurityLevel() & SnmpDefinitions.authMask) == 0) 
	    return SnmpUsm.usmNoAuthProtocol;
	
	return getUser().getAuthPair().algo.getOid();
    }

    //Translate from an algo object to an oid.
    private String convertPrivAlgorithm(){
	if( (getUser().getSecurityLevel() & SnmpDefinitions.privMask) == 0) 
	    return SnmpUsm.usmNoPrivProtocol;
	
	return getUser().getPrivPair().algo.getOid();
    }

    /**
     * <P>This method is called when the UsmUseEntryImpl is added or 
     * removed from the MBeanServer. It returns the object name to use 
     * when registering the MIB in an MBeanServer. Override this method 
     * if you want to change the default name :</P>
     * <engineId> +  "/UsmUserTable:" + "engine=" + <user engineId> + 
     * ",name=" + <user name>.
     *
     */
    protected ObjectName createObjectName() 
	throws MalformedObjectNameException {
	String engine = user.getEngineId().toString();
	return new ObjectName(prefix +"name=" +
			      user.getName());
    }
    
    /**
     * Register this entry in the MBean server.
     */
    public void addInMBeanServer(MBeanServer server) {
        try {
	    //Use the prefix.
            server.registerMBean(this, createObjectName());
        } catch (Exception e) {
	    if(logger.finerOn())
		logger.finer("addInMBeanServer", e.toString());
        }
    }

    /**
     * Unregister this entry from the MBean server.
     */
    public void removeFromMBeanServer(MBeanServer server) {
        try {
	    //Use the prefix.
            server.unregisterMBean(createObjectName());
        } catch (Exception e) {
            if(logger.finerOn())
		logger.finer("addInMBeanServer", e.toString());
        }
    }

    /**
     * Getter for the "UsmUserSecurityName" variable.
     */
    public String getUsmUserSecurityName() throws SnmpStatusException {
        return getUser().getSecurityName();
    }

    /**
     * Getter for the "UsmUserPrivKeyChange" variable.
     */
    public Byte[] getUsmUserPrivKeyChange() throws SnmpStatusException {
        return new Byte[0];
    }

    /**
     * Setter for the "UsmUserPrivKeyChange" variable.
     */
    public void checkUsmUserPrivKeyChange(Byte[] x) 
	throws SnmpStatusException {
	if((getUser().getSecurityLevel() & SnmpDefinitions.privMask) == 0) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserPrivKeyChange",
			      "No privacy, return.");
	    return;
	}
	int expectedSize = user.getPrivPair().algo.getDeltaSize();
	if(logger.finestOn()) 
	    logger.finest("checkUsmUserPrivKeyChange",
		  "Expected delta and random size: "
		  + expectedSize);
	if(x.length != expectedSize) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserPrivKeyChange",
			      "Received delta is wrong: " 
		      + x.length);
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
	}
	
	if(logger.finestOn()) 
	    logger.finest("checkUsmUserPrivKeyChange","check OK");
    }
    
    /**
     * Setter for the "UsmUserPrivKeyChange" variable.
     */
    public void setUsmUserPrivKeyChange(Byte[] x) throws SnmpStatusException {
	//If no algorithm set, a no-op is needed.,
	if((getUser().getSecurityLevel() & SnmpDefinitions.privMask) == 0) {
	    if(logger.finestOn()) 
		logger.finest("setUsmUserPrivKeyChange","No privacy, return.");
	    return;
	}

	if(logger.finestOn()) 
	    logger.finest("setUsmUserPrivKeyChange",
			  "Will do priv key change.");

	Byte[] random = getUsmUserPublic();
	
	if(random == null) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserPrivKeyChange","Random is null " + 
		      "can't process key change");
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
	}
	
	int expectedSize = user.getPrivPair().algo.getDeltaSize();

	if(random.length != expectedSize) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserPrivKeyChange","Random is wrong: " 
		      + random.length);
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
	}
	if(logger.finestOn()) 
	    logger.finest("setUsmUserPrivKeyChange","Random is : " + random);
        byte[] res = new byte[x.length + random.length];
	
	for(int i = 0; i < random.length; i++)
	    res[i] = random[i].byteValue();
	if(logger.finestOn())
	    logger.finest("setUsmUserPrivKeyChange","Random is : " +
		  SnmpTools.binary2ascii(res, random.length));
	
	for(int i = 0; i < x.length; i++)
	    res[i + random.length] = x[i].byteValue();
	
	//Ask the Lcd to do the job
	lcd.setUserPrivKeyChange(user,
				 res);
	super.setUsmUserPrivKeyChange(x);
    }

    /**
     * Getter for the "UsmUserAuthKeyChange" variable.
     */
    public Byte[] getUsmUserAuthKeyChange() throws SnmpStatusException {
        return new Byte[0];
    }

    /**
     * Setter for the "UsmUserAuthKeyChange" variable.
     */
    public void checkUsmUserAuthKeyChange(Byte[] x) 
	throws SnmpStatusException {
	//If no algorithm set, a no-op is needed.,
	if((getUser().getSecurityLevel() & SnmpDefinitions.authMask) == 0) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserAuthKeyChange",
		      "No authentication, return.");
	    return;
	}
	int expectedSize = user.getAuthPair().algo.getDeltaSize();
	if(logger.finestOn()) 
	    logger.finest("checkUsmUserAuthKeyChange",
		  "Expected delta and random size: "
		  + expectedSize);
	if(x.length != expectedSize) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserAuthKeyChange",
			      "Received delta is wrong: " 
			      + x.length);
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
	}
	
	if(logger.finestOn()) 
	    logger.finest("checkUsmUserAuthKeyChange","check OK");
    }
    
    /**
     * Setter for the "UsmUserAuthKeyChange" variable.
     */
    public void setUsmUserAuthKeyChange(Byte[] x) throws SnmpStatusException {

	//If no algorithm set, a no-op is needed.,
	if((getUser().getSecurityLevel() & SnmpDefinitions.authMask) == 0) {
	    if(logger.finestOn()) 
		logger.finest("setUsmUserAuthKeyChange",
			      "No authentication, return.");
	    return;
	}
	if(logger.finestOn()) 
	    logger.finest("setUsmUserAuthKeyChange",
			  "Will do auth key change.");
	Byte[] random = getUsmUserPublic();
	
	if(random == null) {
	    if(logger.finestOn()) 
		logger.finest("setUsmUserAuthKeyChange","Random is null " + 
		      "can't process key change");
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
	}

	int expectedSize = user.getAuthPair().algo.getDeltaSize();

	if(random.length != expectedSize) {
	    if(logger.finestOn()) 
		logger.finest("setUsmUserAuthKeyChange","Random is wrong: " 
		      + random.length);
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
	}

	if(logger.finestOn()) 
	    logger.finest("setUsmUserAuthKeyChange","Random is : " + random);
        byte[] res = new byte[x.length + random.length];

	for(int i = 0; i < random.length; i++)
	    res[i] = random[i].byteValue();

	for(int i = 0; i < x.length; i++)
	    res[i + random.length] = x[i].byteValue();
	
	if(logger.finestOn()) 
	    logger.finest("setUsmUserAuthKeyChange","Received key change : " +
		  SnmpTools.binary2ascii(res));
	//Ask the Lcd to do the job
	lcd.setUserAuthKeyChange(user,
				 res);
	super.setUsmUserAuthKeyChange(x);
    }
    
    /**
     * Setter for the "UsmUserOwnPrivKeyChange" variable. All checks 
     * have been done in the UsmUserEntryMeta.
     */
    public void setUsmUserOwnPrivKeyChange(Byte[] x) 
	throws SnmpStatusException {
	setUsmUserPrivKeyChange(x);
    }
    
    /**
     * Getter for the "UsmUserOwnPrivKeyChange" variable.
     */
    public Byte[] getUsmUserOwnPrivKeyChange() throws SnmpStatusException {
        return new Byte[0];
    }
    
    /**
     * Getter for the "UsmUserOwnAuthKeyChange" variable.
     */
    public Byte[] getUsmUserOwnAuthKeyChange() throws SnmpStatusException {
        return new Byte[0];
    }

    /**
     * Setter for the "UsmUserOwnAuthKeyChange" variable. All checks have 
     * been done in the UsmUserEntryMeta.
     */
    public void setUsmUserOwnAuthKeyChange(Byte[] x) 
	throws SnmpStatusException {
	setUsmUserAuthKeyChange(x);
    }

    /**
     * Getter for the "UsmUserPrivProtocol" variable.
     */
    public String getUsmUserPrivProtocol() throws SnmpStatusException {
        return convertPrivAlgorithm();
    }

    /**
     * Setter for the "UsmUserPrivProtocol" variable.
     */
    public void setUsmUserPrivProtocol(String x) throws SnmpStatusException {
	super.setUsmUserPrivProtocol(x);
	if(logger.finestOn()) 
	    logger.finest("setUsmUserPrivProtocol","priv protocol : " + x);
	user.setPrivAlgorithm(x);
    }

    /**
     * Checker for the "UsmUserPrivProtocol" variable.
     */ 
    public void checkUsmUserPrivProtocol(String x) 
	throws SnmpStatusException {
	if(logger.finestOn()) 
	    logger.finest("checkUsmUserPrivProtocol","priv protocol : " + x);
	if(x.equals(SnmpUsm.usmNoPrivProtocol)) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserPrivProtocol",
			      "priv protocol will be set to no protocol");
	    return;
	}
	else {
	    // If priv implies auth
	    if((getUser().getSecurityLevel() & SnmpDefinitions.authMask)==0) {
		if(logger.finestOn()) 
		    logger.finest("checkUsmUserPrivProtocol",
				  "Unsupported security level. "+
				  "Security level is : " + 
				  (getUser().getSecurityLevel() & 
				   SnmpDefinitions.privMask));
		throw new SnmpStatusException(
                          SnmpDefinitions.snmpRspInconsistentValue);
	    }
	}
	//Can't be set after creation and activation.
	//
	if(getUsmUserStatus().intValue() == EnumRowStatus.active) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserPrivProtocol",
			      "Status already active, failed");
	    throw new 
		SnmpStatusException(SnmpDefinitions.snmpRspInconsistentValue);
	}
	
	if(lcd.getAlgorithmManager().getAlgorithm(x) == null) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserPrivProtocol",
			      "Unknown algo : " + x);
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
	}
    }

    /**
     * Getter for the "UsmUserAuthProtocol" variable.
     */
    public String getUsmUserAuthProtocol() throws SnmpStatusException {
	return convertAuthAlgorithm(user.getAuthPair().algo);
    }

    /**
     * Setter for the "UsmUserAuthProtocol" variable.
     */
    public void setUsmUserAuthProtocol(String x) throws SnmpStatusException {
	super.setUsmUserAuthProtocol(x);
	if(logger.finestOn()) 
	    logger.finest("setUsmUserAuthProtocol","auth protocol : " + x);
	user.setAuthAlgorithm(x);
    }

    /**
     * Checker for the "UsmUserAuthProtocol" variable.
     */
    public void checkUsmUserAuthProtocol(String x) throws SnmpStatusException {
	if(logger.finestOn()) 
	    logger.finest("checkUsmUserAuthProtocol","auth protocol : " + x);
	// If no auth implies no priv
	if(x.equals(SnmpUsm.usmNoAuthProtocol)) {
	    if((getUser().getSecurityLevel() & SnmpDefinitions.privMask)!= 0) {
		if(logger.finestOn()) 
		    logger.finest("checkUsmUserAuthProtocol",
				  "Unsupported security level. " +
				  "Security level is : " + 
				  (getUser().getSecurityLevel() & 
				   SnmpDefinitions.privMask));
		throw new SnmpStatusException(
			  SnmpDefinitions.snmpRspInconsistentValue);
	    }
	    else {
		if(logger.finestOn()) 
		    logger.finest("checkUsmUserAuthProtocol",
				  "auth protocol is no protocol.");
		return;
	    }
	}
	
	//Can't be set after creation and activation.
	//
	if(getUsmUserStatus().intValue() == EnumRowStatus.active) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserAuthProtocol",
			      "auth protocol already active. Failed");
	    throw new 
		SnmpStatusException(SnmpDefinitions.snmpRspInconsistentValue);
	}
	
	if(lcd.getAlgorithmManager().getAlgorithm(x) == null) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserAuthProtocol",
			      "Unknown algo : " + x);
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
	}
    }
    
    /**
     * Getter for the "UsmUserStorageType" variable.
     */
    public EnumUsmUserStorageType getUsmUserStorageType() 
	throws SnmpStatusException {
	EnumUsmUserStorageType e = new 
	    EnumUsmUserStorageType(user.getStorageType());
        return e;
    }

    /**
     * Setter for the "UsmUserStorageType" variable.
     */
    public void setUsmUserStorageType(EnumUsmUserStorageType x) 
	throws SnmpStatusException {
	super.setUsmUserStorageType(x);
	if(logger.finestOn()) 
	    logger.finest("setUsmUserStorageType","Enum : " + x.toString());
	user.setStorageType(x.intValue());
    }
    
    /**
     * Checker for the "UsmUserStorageType" variable.
     */
    public void checkUsmUserStorageType(EnumUsmUserStorageType x) 
	throws SnmpStatusException {
	if(x.intValue() != 3 &&
	   x.intValue() != 2) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserStorageType", 
			      "Wrong storage type :"+
		      x.intValue());
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
	}
	
	if(lcd.getStorageType() < x.intValue()) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserStorageType", 
			      "Unsupported storage type :"+
		      x.intValue());
	    throw new SnmpStatusException(
                      SnmpDefinitions.snmpRspInconsistentValue);
	}
    }

    /**
     * Getter for the "UsmUserEngineId" variable.
     */
    public Byte[] getUsmUserEngineID() throws SnmpStatusException {
	byte[] engineid = user.getEngineId().getBytes();
	Byte[] ret = new Byte[engineid.length];
	for(int i = 0; i < ret.length; i++)
	ret[i] = new Byte(engineid[i]);

        return ret;
    }
    
    /**
     * Getter for the "UsmUserName" variable.
     */
    public String getUsmUserName() throws SnmpStatusException {
        return user.getName();
    }

    /**
     * Getter for the "UsmUserCloneFrom" variable.
     */
    public String getUsmUserCloneFrom() throws SnmpStatusException {
        return "0.0";
    }

    /**
     * Checker for the "UsmUserCloneFrom" variable.
     */
    public void checkUsmUserCloneFrom(String x) throws SnmpStatusException {
	if(original != null) return;
	try {
	    original = table.getCloneFromUser(x);
	}catch(SnmpStatusException e) {
	    if(logger.finestOn()) 
		logger.finest("setUsmUserCloneFrom", "Exception : " + e);
	    throw new 
		SnmpStatusException(SnmpDefinitions.snmpRspInconsistentName);
	}
	if(original == null)
	    throw new 
		SnmpStatusException(SnmpDefinitions.snmpRspInconsistentName);

	UsmUserCloneFrom = x;
	
	EnumUsmUserStatus en = original.getUsmUserStatus();
	
	if(en.intValue() != EnumRowStatus.active)
	    throw new 
		SnmpStatusException(SnmpDefinitions.snmpRspInconsistentName);
    }

    /**
     * Setter for the "UsmUserCloneFrom" variable.
     */
    public void setUsmUserCloneFrom(String x) throws SnmpStatusException {
	//Nothing to do if already done.
	//
	if(original == null) {
	    if(logger.finestOn()) 
		logger.finest("setUsmUserCloneFrom", "original == null");
	    throw new 
		SnmpStatusException(SnmpDefinitions.snmpRspInconsistentName);
	}

	SnmpUsmSecureUser clone = original.getUser();
	
	user.cloneAuthPair(clone.getAuthPair());
	
	user.clonePrivPair(clone.getPrivPair());

	super.setUsmUserCloneFrom(x);
    }
    /**
     * Setter for the "UsmUserStatus" variable.
     */
    public void setUsmUserStatus(EnumUsmUserStatus x) 
	throws SnmpStatusException {
	if(getUsmUserStatus().intValue() == EnumRowStatus.active) {


	    if(logger.finestOn()) 
		logger.finest("setUsmUserStatus", "Already active. Return");
	    return;
	}
	
	// If the user is a template, it comes from the LCD. No need to
	// add it again ...
	if(user.isTemplate()) {
	    super.setUsmUserStatus(x);
	    return;
	}
	
	if(logger.finestOn()) 
	    logger.finest("setUsmUserStatus", "Setting status to : "+ 
			  x.intValue());
	if(x.intValue() == EnumRowStatus.active) {
	    try {
                lcd.addUser(user, false);
            }catch(SnmpUsmException e) {
                if(logger.finestOn())
                    logger.finest("setUsmUserStatus",
                          " error when adding a user : "+ e);
                throw new SnmpStatusException(
                          SnmpDefinitions.snmpRspUndoFailed);
            }

	}
	//Update attribute.
	super.setUsmUserStatus(x);
    }
    
    /**
     * Checker for the "UsmUserStatus" variable.
     */
    public void checkUsmUserStatus(EnumUsmUserStatus x) 
	throws SnmpStatusException {
	if(getUsmUserStatus().intValue() == EnumRowStatus.active) {
	    if(logger.finestOn()) 
		logger.finest("checkUsmUserStatus", "Already active. Return");
	    return;
	}

	if(x.intValue() == EnumRowStatus.active) {
	    if(original == null) {
		if(logger.finestOn()) 
		    logger.finest("checkUsmUserStatus", 
			  "cloneFrom not done. Can't switch to active state.");
		throw new SnmpStatusException(
                    SnmpDefinitions.snmpRspInconsistentValue);
	    }
	    
	    //Authentication activated
	    if( (user.getAuthPair().algo != null) &&
	       (user.getAuthPair().key != null) && 
		(UsmUserAuthKeyChange == null) ) {
		if(logger.finestOn()) 
		    logger.finest("checkUsmUserStatus", 
		    "Auth key change not done. Can't switch to active state.");
		throw new SnmpStatusException(
                    SnmpDefinitions.snmpRspInconsistentValue);
	    }
	    
	    //Privacy activated
	    if( (user.getPrivPair().algo != null) &&
		(user.getPrivPair().key != null) && 
		(UsmUserPrivKeyChange == null) ) {
		if(logger.finestOn()) 
		    logger.finest("checkUsmUserStatus", 
		    "Priv key change not done. Can't switch to active state.");
		throw new SnmpStatusException(
                          SnmpDefinitions.snmpRspInconsistentValue);
	    }
	}
    }

    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_ADAPTOR_SNMP,
			"UsmUserEntryImpl");
}
