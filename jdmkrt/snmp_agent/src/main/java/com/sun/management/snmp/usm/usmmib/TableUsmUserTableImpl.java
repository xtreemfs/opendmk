/*
 * @(#)file      TableUsmUserTableImpl.java
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
package com.sun.management.snmp.usm.usmmib;

import java.util.Enumeration;
import java.util.Hashtable;

import com.sun.jdmk.internal.ClassLogger;


// jmx imports
//
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.sun.management.snmp.SnmpCounter;
import com.sun.management.snmp.SnmpCounter64;
import com.sun.management.snmp.SnmpGauge;
import com.sun.management.snmp.SnmpInt;
import com.sun.management.snmp.SnmpUnsignedInt;
import com.sun.management.snmp.SnmpIpAddress;
import com.sun.management.snmp.SnmpTimeticks;
import com.sun.management.snmp.SnmpOpaque;
import com.sun.management.snmp.SnmpString;
import com.sun.management.snmp.SnmpStringFixed;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpNull;
import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpDefinitions;

// jdmk imports
//
import com.sun.management.snmp.usm.SnmpUsm;
import com.sun.management.snmp.usm.SnmpUsmMibTable;
import com.sun.management.snmp.usm.SnmpUsmLcd;
import com.sun.management.snmp.usm.SnmpUsmSecureUser;
import com.sun.management.snmp.usm.SnmpUsmAlgorithm;
import com.sun.management.snmp.usm.SnmpUsmException;
import com.sun.management.snmp.agent.SnmpIndex;
import com.sun.management.snmp.agent.SnmpMib;
import com.sun.management.snmp.agent.SnmpMibTable;
import com.sun.management.snmp.agent.SnmpMibSubRequest;
import com.sun.management.snmp.agent.SnmpTableEntryFactory;
import com.sun.management.snmp.agent.SnmpTableCallbackHandler;
import com.sun.management.snmp.agent.SnmpTableSupport;


/**
 * The table has been customized to instantiate UsmUserEntryImpl. You
 * can overload <CODE> createUsmUserEntry </CODE> method in order to
 * instantiate your own UsmUserEntry.
 *
 *
 * @since Java DMK 5.1
 */
public class TableUsmUserTableImpl extends TableUsmUserTable 
    implements SnmpUsmMibTable {
    private static final long serialVersionUID = -1488698159987685211L;
    public static final int MAX_USM_USER_NAME_LENGTH = 32;

    SnmpUsmLcd lcd = null;
    SnmpEngine engine = null;
    Hashtable templates = new Hashtable();
    public Object createUsmUserEntryMBean(SnmpMibSubRequest req,
					  SnmpOid rowOid, 
					  int depth, 
					  ObjectName entryObjName,
					  SnmpMibTable meta, 
					  Byte[]  aUsmUserEngineID, 
					  String  aUsmUserName)
	throws SnmpStatusException  {
	//Translate Byte to byte...
	byte[] engineId = new byte[aUsmUserEngineID.length];
	for(int i = 0; i <aUsmUserEngineID.length; i++)
	    engineId[i] = aUsmUserEngineID[i].byteValue();
	if(logger.finestOn()) {
	    logger.finest("createUsmUserEntryMBean",
		  "Creating new user : " + aUsmUserName);
	}
       
	if(aUsmUserName == null || aUsmUserName.length() > 
	   MAX_USM_USER_NAME_LENGTH) {
	    if(logger.finestOn())
		logger.finest("createUsmUserEntryMBean",
			      "Invalid userName : " + aUsmUserName);
	    
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspWrongValue);
	}


	SnmpUsmSecureUser user = null;
	//Ask the Usm Lcd to instantiate a new secure user.
	
	user = lcd.createNewUser(engineId,
				 aUsmUserName);
	
	//Set the storage type to non volatile
	user.setStorageType(SnmpUsmLcd.NON_VOLATILE);
	
	//Set the algorithm to no Auth and no Priv
	user.setAuthAlgorithm(SnmpUsm.usmNoAuthProtocol+".0");
	user.setPrivAlgorithm(SnmpUsm.usmNoPrivProtocol+".0");

	//Tie the user with a new entry
	UsmUserEntryImpl entry = createUsmUserEntry(engine,
						    theMib,
						    lcd,
						    user,
						    meta);
	return entry;
    }

    UsmUserEntryImpl getCloneFromUser(String oid) throws SnmpStatusException {
	UsmUserEntryImpl original = null;
	if(logger.finestOn()) {
	    logger.finest("getCloneFromUser",
		  "cloneFromUser : " + oid);
	}
	

	original = (UsmUserEntryImpl) templates.get(oid);
	//Not a template, cloning from another row.
	if(original == null) {
	    SnmpOid snmpOid = new SnmpOid(oid);
	    original = (UsmUserEntryImpl) meta.getEntry(snmpOid);
	}
	if(logger.finestOn()) {
	    logger.finest("getCloneFromUser",
		  "cloneFromUser : " + original);
	}
	return original;
    }

    /**
     * Overload this method in order to instantiate your own UsmUserEntry. 
     * This method returns a <CODE> UsmUserEntryImpl </CODE>.
     * @param engine The local SNMP engine.
     * @param mib The current mib.
     * @param lcd The lcd the MIB is bound to.
     * @param user The lcd user bound to the newly created entry.
     */
    protected UsmUserEntryImpl createUsmUserEntry(SnmpEngine engine,
						  SnmpMib mib,
						  SnmpUsmLcd lcd,
						  SnmpUsmSecureUser user,
						  SnmpMibTable tableMeta) {
	return new UsmUserEntryImpl(engine,
				    theMib, 
				    lcd, 
				    user, //Tied user 
				    this);
    }				      

    /**
     * See SnmpUsmMibTable interface for documentation.
     */
    public void userAdded(SnmpUsmSecureUser user) {
	if(logger.finestOn())
	    logger.finest("userAdded", "engineId : " 
		  + user.getEngineId().toString()
		  + "\nname : " + user.getName()
		  + "\nsecurityName : " + user.getSecurityName()
		  + "\nauthAlgo : "
		  + convertAuthAlgorithm(user.getAuthPair().algo)
		  + "\nprivAlgo : " + convertPrivAlgorithm(user.getPrivPair().
							   algo)
		  + "\nstorageType : " + user.getStorageType());

	//Tie the user with a new entry
	UsmUserEntryImpl entry = new UsmUserEntryImpl(engine,
						      theMib, 
						      lcd, 
						      user, //Tied user 
						      this);
	//If MBeanServer, register the entry in server.
	if(server != null)
	    entry.addInMBeanServer(server);
	try {
	    //Add the entry in the table.
	    addEntry(entry);
	}catch(SnmpStatusException s) {
	    if(logger.finestOn())
		logger.finest("userAdded", s);
	}
    }
    /**
     * See SnmpUsmMibTable interface for documentation.
     */
    public void userUpdated(SnmpUsmSecureUser user) {
	if(logger.finestOn())
	    logger.finest("userUpdated", "engineId : " 
		  + user.getEngineId().toString()
		  + "\nname : " + user.getName()
		  + "\nsecurityName : " + user.getSecurityName()
		  + "\nauthAlgo : "
		  + convertAuthAlgorithm(user.getAuthPair().algo)
		  + "\nprivAlgo : " + convertPrivAlgorithm(user.getPrivPair().
							   algo)
		  + "\nsorageType : " + user.getStorageType());
	
	// WARNING. All this work can be uneeded, because the update is 
	// transparent to the entry. The secure user status is stored in 
	// the secure user directly. The entry is all the time in sync
	// with the user.
	//

	SnmpOid oid = null;
	byte[] engineid = user.getEngineId().getBytes();
	Byte[] ret = new Byte[engineid.length];
	for(int i = 0; i < ret.length; i++)
	    ret[i] = new Byte(engineid[i]);
	
	try {
	    oid = buildOidFromIndexVal(ret,
				       user.getName());
	}catch(SnmpStatusException s) {
	    if(logger.finestOn())
		logger.finest("userUpdated", s);
	}
	try {
	    UsmUserEntryImpl entry = (UsmUserEntryImpl) meta.getEntry(oid);
	    //Change the tie with the updated user.
	    entry.setUser(user);
	}catch(SnmpStatusException s) {
	    if(logger.finestOn())
		logger.finest("userUpdated", s);
	}
	
    }
    /**
     * See SnmpUsmMibTable interface for documentation.
     */
    public void userRemoved(SnmpUsmSecureUser user) {
	if(logger.finestOn())
	    logger.finest("userUpdated", "engineId : " 
		  + user.getEngineId().toString()
		  + "\nname : " + user.getName()
		  + "\nsecurityName : " + user.getSecurityName()
		  + "\nauthAlgo : "
		  + convertAuthAlgorithm(user.getAuthPair().algo)
		  + "\nprivAlgo : " + convertPrivAlgorithm(user.getPrivPair().
							   algo)
		  + "\nsorageType : " + user.getStorageType());
	// This entry is instantiated to find the entry to remove.
	//
	UsmUserEntryImpl entry = new UsmUserEntryImpl(engine, 
						      theMib, 
						      lcd, 
						      user, 
						      this);
	try {
	    // Remove the entry.
	    removeEntry(entry);
	}catch(SnmpStatusException s) {
	    if(logger.finestOn())
		logger.finest("userRemoved", s);
	}
	// If has been registered, must be unregistered from the MBeanServer.
	if(server != null)
	    entry.removeFromMBeanServer(server);
    }

    /**
     * Constructor.
     */
    public TableUsmUserTableImpl(SnmpEngine engine,
				 SnmpMib myMib, 
				 SnmpUsmLcd lcd) {
        super(myMib);
	init(lcd, engine);
    }
    
    /**
     * Constructor for the table. Initialize metadata for "TableUsmUserTable".
     * The reference on the MBean server is updated so the entries created
     * through an SNMP SET will be AUTOMATICALLY REGISTERED in Java DMK.
     */
    public TableUsmUserTableImpl(SnmpEngine engine,
				 SnmpMib myMib, 
				 MBeanServer server, 
				 SnmpUsmLcd lcd) {
        super(myMib, server);
	init(lcd, engine);
    }

    //For debugging purpose only.
    public void addEntryCb(int pos, SnmpOid row, ObjectName name, 
			   Object entry, SnmpMibTable meta) 
	throws SnmpStatusException {
	if(logger.finestOn())
	    logger.finest("addEntryCb","");
    }
    
    /**
     * A user has been removed from SNMP.
     */
    public void removeEntryCb(int pos, SnmpOid row, ObjectName name, 
			      Object entry, SnmpMibTable meta)
	throws SnmpStatusException {
	super.removeEntryCb(pos, row, name, entry, meta);
	if(logger.finestOn())
	    logger.finest("removeEntryCb","");
	// Ask the Lcd to remove The user from the conf.
	lcd.removeUser(((UsmUserEntryImpl) entry).getUser().getEngineId(), 
		       ((UsmUserEntryImpl) entry).getUser().getName(), 
		       false);
    }

    //Do some common initialization
    private void init(SnmpUsmLcd lcd,
		      SnmpEngine engine) {
	this.lcd = lcd;
	this.engine = engine;

	//Register itself to receive configuration change events.
	lcd.setMibTable(this);
	//Access the all set of users.
	Enumeration users = lcd.getAllUsers();
	while(users.hasMoreElements()) {
	    SnmpUsmSecureUser user = (SnmpUsmSecureUser) users.nextElement();
	    // Instantiate an entry for each secure user in Lcd.
	    UsmUserEntryImpl entry = createUsmUserEntry(engine,
							theMib, 
							lcd, 
							user,
							meta);
	    try {
		entry.setUsmUserStatus(new EnumUsmUserStatus("active"));
	    }catch(Exception e) {
		if(logger.finestOn())
		    logger.finest("init", e);
	    }
	    if(logger.finestOn()) {
		logger.finest("init","Adding :" + 
			      user.getEngineId().toString() + "/" +
			      user.getName());
	    }
	    
	    if(user.isTemplate() == false) {		
		try {
		    // Add the entry in the table.
		    addEntry(entry);
		    if(logger.finestOn()) {
			logger.finest("init","Added :" + 
				      user.getEngineId().toString() + "/" +
				      user.getName());
		    }	
		}catch(SnmpStatusException s) {
		    if(logger.finestOn())
			logger.finest("init", s);
		}
		// If MBeanServer, register the entry.
		if(server != null)
		    entry.addInMBeanServer(server);
	    }
	    else {
		if(logger.finestOn())
		    logger.finest("init","Template user detected :" + 
			  user.getEngineId().toString() + "/" +
			  user.getName());
		String templateUserOid = null;
		// Build the template user oid
		try {
		    SnmpIndex index = buildSnmpIndex(entry);
		    templateUserOid = "1.3.6.1.6.3.15.1.2.2.1.3." + 
			buildOidFromIndex(index).toString();
		    if(logger.finestOn())
			logger.finest("init", "template user oid :" + 
				      templateUserOid);
		    entry.setUsmUserStatus(new EnumUsmUserStatus("active"));
		}catch(SnmpStatusException s) {
		    if(logger.finestOn())
			logger.finest("init", s);
		}
		templates.put(templateUserOid, entry);
	    }
	}
    }
    
    //Convert an algo object to a name.
    private String convertAuthAlgorithm(SnmpUsmAlgorithm algo){
	if(algo == null) return "usmNoAuthProtocol";
	return algo.getAlgorithm();
    }

    //Convert an algo object to a name.
    private String convertPrivAlgorithm(SnmpUsmAlgorithm algo){
	if(algo == null) return "usmNoPrivProtocol";
	return algo.getAlgorithm();
    }

    String dbgTag = "TableUsmUserTableImpl"; 
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_ADAPTOR_SNMP,
			"TableUsmUserTableImpl");
}
