/*
 * @(#)file      SnmpUsmPasswordLcd.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.62
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
package com.sun.management.snmp.usm;

import java.net.UnknownHostException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Enumeration;
import java.util.Vector;

import com.sun.management.internal.snmp.SnmpTools;
import com.sun.jdmk.internal.ClassLogger;

import com.sun.jdmk.defaults.JdmkProperties;
import com.sun.jdmk.defaults.DefaultPaths;

import com.sun.management.internal.snmp.SnmpLcd;
import com.sun.management.internal.snmp.SnmpModelLcd;
import com.sun.management.internal.snmp.SnmpSubSystem;
import com.sun.management.internal.snmp.SnmpPersistRowFile;
import com.sun.management.internal.snmp.SnmpPersistRowFileConsumer;

import com.sun.jdmk.internal.BackupFileHandler;

import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineId;

/**
 * Exception thrown when parsing configuration.
 *
 * @since Java DMK 5.1
 */
class SecurityConfException extends Exception {
    private static final long serialVersionUID = -6006816757320199577L;
    SecurityConfException(String msg) {
        super(msg);
    }
    
}

/**
 * An internal engine representation. Manages a hash of the associated users.
 *
 * @since Java DMK 5.1
 */
class LcdEngine {
    //Users
    Hashtable users = new Hashtable();
    //Engine ID 
    SnmpEngineId engineId = null;
    //Constructor
    LcdEngine(SnmpEngineId id) {
        engineId = id;
    }

    /**
     * Called to store the set of users it manages using the passed FileWriter.
     */
    void storeUsers(SnmpEngineId local,
		    FileWriter writer,
                    SnmpPersistRowFile file) {
        Enumeration e = users.elements();
        //For each user
        for(;e.hasMoreElements();) {
            SnmpUsmSecureUserImpl user = (SnmpUsmSecureUserImpl) 
                e.nextElement();
            //If the storage type is permanent 
            if(user.getStorageType() >= SnmpUsmLcd.NON_VOLATILE) {
                try{	
                    //Ask the user to generate its configuration string.
                    file.write(writer, user.getConf() + "\n\n");
                }catch(IOException ex) {
		    if(logger.finestOn()) {
			logger.finest("storeUsers", ex);
		    }
                    break;
                }
            }
        }
    }
    
    /**
     * Add a user.
     */
    SnmpUsmSecureUser addSecureUser(SnmpUsmSecureUser user) {
	if(logger.finestOn())
	    logger.finest("addSecureUser", " adding user :" + user.getName());
        return (SnmpUsmSecureUser) users.put(user.getName(), user);
    }
    /**
     * Get a specific user according to its name.
     */
    SnmpUsmSecureUser getSecureUser(String name) {
        return (SnmpUsmSecureUser) users.get(name);
    }
    /**
     * Remove a specific user according to its name.
     */
    SnmpUsmSecureUser removeSecureUser(String name) { 
        return (SnmpUsmSecureUser) users.remove(name); 
    }
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"LcdEngine");

}

/**
 * FOR INTERNAL USE ONLY. This is the default implementation of the Usm Lcd.
 * Its job is to read the configuration file, store the configuration and 
 * manage <CODE>SnmpUsmSecureUser</CODE> objects.
 *
 * @since Java DMK 5.1
 */
public class SnmpUsmPasswordLcd implements SnmpUsmLcd {
    //Set of enums that are used when parsing the conf file. It is the 
    //context in which the current token is extracted.
    static final int USER_ENGINEID =      0;
    static final int USER_NAME =          1;
    static final int USER_SECURITY_NAME = 2;
    static final int USER_AUTH_PROTOCOL = 3;
    static final int USER_AUTH_PASSWORD = 4;
    static final int USER_PRIV_PROTOCOL = 5;
    static final int USER_PRIV_PASSWORD = 6;
    static final int USER_STORAGE_TYPE  = 7;
    static final int USER_TEMPLATE      = 8;

    UsmConsumer consumer = null;
    SnmpPersistRowFile file = null;
    boolean strictParsing = true;
    // Hash of LcdEngine.
    Hashtable engines = new Hashtable();
    // The algorithm manager that is used to translate an algo name in an 
    // algo object.
    SnmpUsmAlgorithmManager algos = null;
    //The local engine Lcd representative. Isd used to resolve 
    //"localEngineID" configuration value.
    LcdEngine localEngine = null;
    // The local engine.
    SnmpEngine engine = null;
    //The mibtable to which events must be sent.
    SnmpUsmMibTable table = null;
    //Flag that is set to true when conf synchronization is processed.
    boolean sync = false;
    //Flag that is set to true when conf synchronization has been processed.
    boolean syncDone = false;
    /**
     * Constructor.
     */
    public SnmpUsmPasswordLcd(SnmpEngine engine,
                              SnmpSubSystem subsys,
                              SnmpLcd snmplcd,
                              String file) throws IllegalArgumentException {
        this.engine = engine;
        //Add itself to the Lcd. 
        snmplcd.addModelLcd(subsys,
                            SnmpUsm.ID, 
                            this);

	consumer = new UsmConsumer(this);

	String testedFile = handleSecurityFileLocation(file);
	if(testedFile != null) {
	    try {
		this.file = new SnmpPersistRowFile(testedFile,
						   "userEntry",
						   ",",
						   consumer);
		if(strictParsing)
		    this.file.enableException(true);
		
	    }catch(IllegalArgumentException e) {
		if(logger.finestOn())
                    logger.finest("SnmpUsmPasswordLcd", "Exception :" + e);
	    }
	}
	else
	    if(logger.finestOn())
		logger.finest("SnmpUsmPasswordLcd",
			      "No configuration file provided.");
	
        //Instantiate the Lcd representative of the local engine.
        localEngine = new LcdEngine(engine.getEngineId());
        //Put it in the engine hash.
        engines.put(engine.getEngineId().toString(), localEngine);
    }
    
    /**
     * Call this method in order to change the file parsing behavior. 
     * Default behavior is throwing an exception when parsing a bad 
     * configured userEntry. 
     * @param b True, throws an IllegalArgumentException, false the line 
     *    is skipped.
     */
    public void enableStrictParsing(boolean b) {
	strictParsing = b;
    }

    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public void setUserAuthKeyChange(SnmpUsmSecureUser u,
                                     byte[] keyChange) {
        SnmpUsmSecureUserImpl user = (SnmpUsmSecureUserImpl) u;
        user.setAuthKeyChange(keyChange);
    }
    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public void setUserPrivKeyChange(SnmpUsmSecureUser u,
                                     byte[] keyChange) {
        SnmpUsmSecureUserImpl user = (SnmpUsmSecureUserImpl) u;
        user.setPrivKeyChange(keyChange);	
    }

    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public void addUser(SnmpUsmSecureUser user,
			boolean notifyMIB) {
	insertUser(user.getEngineId(), user, notifyMIB);
    }

    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public SnmpUsmSecureUser createNewUser(byte[] engineId,
                                           String name) {
        SnmpUsmSecureUserImpl user = 
	    new SnmpUsmSecureUserImpl(this, 
				      SnmpEngineId.createEngineId(engineId), 
				      name);
	return user;
    }

    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public int getStorageType() {
        if (file != null) return SnmpUsmLcd.NON_VOLATILE;
        else return SnmpUsmLcd.VOLATILE;
    }
    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public void setMibTable(SnmpUsmMibTable table) {
        this.table = table;
    }

    /**
     * Does nothing in our case. Compliant with RFC 2574.
     */
    public synchronized void addEngine(SnmpEngineId engineId) {

    }
    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public void setAlgorithmManager(SnmpUsmAlgorithmManager algos) {
        this.algos = algos;
    }
    
    /**
     * Gets the <CODE>SnmpUsmSecureUser</CODE> for the passed user name 
     * and authoritative engine Id.
     * @param engineId The authoritative engine Id.
     * @param userName The user name.
     */
    public SnmpUsmSecureUser getUser(SnmpEngineId engineId, 
				     String userName) 
	throws SnmpUsmEngineIdException, SnmpUsmUserNameException {
	return findUser(engineId, userName);
    }
    
    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public synchronized void syncDataSource() 
	throws IllegalArgumentException {
	if(syncDone) return;
	
	sync = true;
        if(getStorageType() == SnmpUsmLcd.NON_VOLATILE) 
            readFile();
	sync = false;
	syncDone = true;
    }

    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public SnmpUsmAlgorithmManager getAlgorithmManager() {
        return algos;
    }
    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public synchronized SnmpUsmPrivPair getUserPrivPair(SnmpEngineId engineId, 
                                                        String userName) 
        throws SnmpUsmPrivAlgorithmException, 
               SnmpUsmEngineIdException, 
               SnmpUsmUserNameException {
        SnmpUsmSecureUserImpl user = null;
        user = (SnmpUsmSecureUserImpl) findUser(engineId,userName); 
        SnmpUsmAlgorithm userAlgo = (SnmpUsmAlgorithm) 
            user.getPrivPair().algo;
        if(userAlgo == null)
            throw new SnmpUsmPrivAlgorithmException("User: " +
                                                    user.getName() +
                                                    " No Priv algorithm");
        else
            return user.getPrivPair();
    }
    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public synchronized SnmpUsmAuthPair getUserAuthPair(SnmpEngineId engineId, 
                                                        String userName) 
        throws SnmpUsmAuthAlgorithmException, 
               SnmpUsmEngineIdException, 
               SnmpUsmUserNameException {
        SnmpUsmSecureUserImpl user = null;
        user = (SnmpUsmSecureUserImpl) findUser(engineId,userName);

        SnmpUsmAlgorithm userAlgo = (SnmpUsmAlgorithm) 
            user.getAuthPair().algo;
        if(userAlgo == null)
            throw new SnmpUsmAuthAlgorithmException("User: " +
                                                    user.getName() +
                                                    " No Auth algorithm");
        else
            return user.getAuthPair();
    }
    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public synchronized void addUser(SnmpEngineId engineId,
				     String userName,
				     String securityName,
				     String authProtocol,
				     String authPassword,
				     String privProtocol,
				     String privPassword,
				     int storage,
				     boolean template) 
        throws SnmpUsmException
    {
	
        try {
            //Make the translation from string to object
            //Instantiate and insert a new user.
            translateAndInsert(engineId,
                               ((engineId.getReadableId()==null)? 
				engineId.toString(): 
				engineId.getReadableId()),
                               userName,
                               securityName,
                               authProtocol,
                               authPassword,
                               privProtocol,
                               privPassword,
                               storage,
			       template);
        }catch(SecurityConfException e) {
            throw new SnmpUsmException("Security configuration: " + e);
        }
    }

    /**
     * See <CODE>SnmpUsmLcd</CODE> interface doc for details.
     */
    public synchronized Enumeration getAllUsers() {
        Vector res = new Vector();
        Enumeration e = engines.elements();
        for(;e.hasMoreElements();) {
            LcdEngine eng = (LcdEngine) e.nextElement();
            Enumeration u = eng.users.elements();
            for(;u.hasMoreElements();) {
                SnmpUsmSecureUser user = (SnmpUsmSecureUser) u.nextElement();
                res.add(user);
            }
        }
        return res.elements();
    }
    
    
    //**************** Private methods **********************
    /**
     * Define where to find the security file.
     */
	private String handleSecurityFileLocation(String securityFile) 
	    throws IllegalArgumentException {
	    String lcdFile = null;
	    File file = null;
	    if(securityFile == null) {
		lcdFile = (String) 
		    System.getProperty(JdmkProperties.SECURITY_FILE);
		if (lcdFile == null) {
		    if(logger.finestOn()) {
			logger.finest("handleSecurityFileLocation",
				 "Security file not found. Use default one.");
		    }
		    lcdFile = DefaultPaths.getEtcDir("conf" + 
						     File.separator + 
						     "jdmk.security");
		    file = new File(lcdFile);
		    if(!file.exists()) {
			file = null;
			if (logger.finerOn()) {
			    logger.finer("handleSecurityFileLocation", 
					 "The default file ["+ lcdFile +
					 "] doesn't exist.");
			}
			return null;
		    }
		    else
			return lcdFile;
		}
		else {
		    file = new File(lcdFile);
		    if(!file.exists()) {
			if (logger.finerOn())
			    logger.finer("handleSecurityFileLocation", 
					 "The specified file ["+ file +
					 "] doesn't exist, "+
					 "no configuration loaded");
			throw new IllegalArgumentException(
				  "The specified file ["+ file +
				  "] doesn't exist, no configuration loaded");
		    }
		    else
			return lcdFile;
		}
	    }
	    else {
		file = new File(securityFile);
		if(!file.exists()) {
		    if (logger.finerOn())
			logger.finer("handleSecurityFileLocation", 
				  "The specified file ["+ file +
				  "] doesn't exist, no configuration loaded");
		    
		    throw new IllegalArgumentException("The specified file ["+
			  file +"] doesn't exist, no configuration loaded");
		}
		else
		    return securityFile;
	    }
	}
    
    // Internal, send the event if flag is true.
    private void sendNewUserEvent(SnmpUsmSecureUser user, boolean flag) {
        if(!flag) return;

        if(table != null)
            table.userAdded(user);
    }
    // Internal, send the event if flag is true.
    private void sendUpdateUserEvent(SnmpUsmSecureUser user, boolean flag) {
        if(!flag) return;

        if(table != null)
            table.userUpdated(user);
    }
    // Internal, send the event if flag is true.
    private void sendRemoveUserEvent(SnmpUsmSecureUser user, boolean flag) {
        if(!flag) return;

        if(table != null)
            table.userRemoved(user);
    }
    
    //Remove the user from the Lcd
    public synchronized void removeUser(SnmpEngineId engineId,
					String userName,
					boolean notifyMIB) {
        LcdEngine engine = (LcdEngine) engines.get(engineId.toString());
	
        //If no engine for the provided engineId, then return.
        if(engine == null) return;
	
        SnmpUsmSecureUser user = engine.removeSecureUser(userName);
        if(user != null) {
            //Remove the user from the configuration file.
	    if(getStorageType() == SnmpUsmLcd.NON_VOLATILE)
		flushFile();
            //Send event.
            sendRemoveUserEvent(user,
                                notifyMIB);
        }
    }

    /*
     * Insert user in Lcd.
     */
    private synchronized void insertUser(SnmpEngineId engineId, 
                                         SnmpUsmSecureUser user,
                                         boolean flag) {
        LcdEngine engine = (LcdEngine) engines.get(engineId.toString());
        if(engine == null) {
	    if(logger.finestOn())
		logger.finest("insertUser"," New engine Id");
            engine = new LcdEngine(engineId);
            engines.put(engineId.toString(), engine);
        }
	if(logger.finestOn())
	    logger.finest("insertUser"," engine id : " + engine.engineId);
	if(logger.finestOn()) {
	    logger.finest("insertUser"," Inserting a new User [" +
			  user.getName() +"]"
			  +", engineId ["+ engineId + "], notif flag : " + 
			  flag);
	}
	
        SnmpUsmSecureUser old = engine.addSecureUser(user);
        //A user was there. It is an update not a new insertion.
        if(old != null) {
	    if(logger.finestOn())
		logger.finest("insertUser"," It is an update");
	    //Send the update event.
	    sendUpdateUserEvent(user, flag);
	}
	else {
	    if(logger.finestOn())
		logger.finest("insertUser"," It is a new user");
	    //Send the new user event.
            sendNewUserEvent(user, flag);
	}
        //We are in the process of reading the file. 
        if(!sync) // We are reading the file, no need to write file at the 
	          // same time...
            if(getStorageType() == SnmpUsmLcd.NON_VOLATILE)
                //Must update the file.
                flushFile();
    }

    /**
     * Method to find a user using an engineId and a user name.
     */
    private synchronized SnmpUsmSecureUser findUser(SnmpEngineId engineId, 
						    String userName)
        throws  SnmpUsmEngineIdException, SnmpUsmUserNameException
    {
        if(logger.finerOn()) 
            logger.finer("findUser", "Engine Id : " + engineId +"/" + 
			 engineId.hashCode() + "     user Name : " + userName);
        LcdEngine engine = (LcdEngine) engines.get(engineId.toString());
        SnmpUsmSecureUser user = null;
	if(userName == null) {
            if(logger.finestOn()) 
                logger.finest("findUser", "Username : [" + userName + 
			      "] NULL.");
	    
            throw new SnmpUsmUserNameException("User " + 
                                               userName + " unknown.");
        }
	
        //No engine, user doesn't exist.
        if(engine == null) {
            if(logger.finestOn()) 
                logger.finest("findUser", "Engine Id [" + 
			      engineId.toString() + "] unknown.");
	    
            throw new SnmpUsmEngineIdException("Unknown engine Id : " + 
					       engineId.toString());
        }
        else {
            if(logger.finerOn()) 
                logger.finer("findUser", "EXISTS. Engine Id : " + 
			     engine.engineId.toString());
	    

            user = engine.getSecureUser(userName);
        }
        //User doesn't exist, throw an exception.
        if(user == null) {
            if(logger.finestOn()) 
                logger.finest("findUser", "Username : [" + userName + 
			      "] unknown.");

            throw new SnmpUsmUserNameException("User " + 
                                               userName + " unknown.");
        }
        else { // Everything OK.
            if(logger.finerOn()) 
                logger.finer("findUser", "Known engine Id [" + 
			     engine.engineId.toString() + 
			     " ] and user Name [" + user.getName() + "]");
            return user;
        }
    }

    //Write configuration changes.
    synchronized void flushFile() {
        FileWriter writer = null;

	// First create a backup
	File backupFile = BackupFileHandler.createBackupFile(file.getFile(), 
							     null);
	
	if(backupFile == null) {
	    if(logger.finestOn()) 
                logger.finest("flushFile", 
		       "Backup file not created, changes are not flushed");
            return;
	}
	
	if(logger.finestOn())
	    logger.finest("flushFile", "Flushing file, backup file : " + 
			  backupFile);
	
        try {
            writer = file.createWriter();
	    
            //Write engine infos.
	    SnmpEngineId id = this.engine.getEngineId();
	    file.write(writer, "localEngineID",  
		       (id.getReadableId() == null ?
			id.toString() : id.getReadableId())
		       + "\n");
            file.write(writer, "localEngineBoots", 
		       (this.engine.getEngineBoots() + 1) +"\n\n");

	    Enumeration e = engines.elements();
	    
	    //Write user infos for each engine.
	    for(;e.hasMoreElements();) {
		LcdEngine eng = (LcdEngine) e.nextElement();
		eng.storeUsers(engine.getEngineId(), 
			       writer, file);
	    }
        }catch(Exception ex) {
            if(logger.finestOn()) 
                logger.finest("flushFile", "Exception : " + ex);
        }
	finally {
	    if(writer != null) {
		try {
		    file.releaseWriter(writer);
		}catch(Exception e) {
		    if(logger.finestOn()) 
			logger.finest("flushFile", "Exception : " + e);
		}
	    }
	    // Delete the backup file
	    BackupFileHandler.deleteBackupFile(backupFile);
	}
    }
    
    // Called at sync time. Will try to open the file then will populate users.
    private synchronized void readFile() {
        try {
            file.read();
        } catch(FileNotFoundException e) {
            if(logger.finestOn()) 
                logger.finest("readFile", 
		     "No configuration file. Will not read or persist data");
        }
        catch(IOException e) {
            if(logger.finestOn()) 
                logger.finest("readFile", "IO error :" + e);
        }
    }
    
    //Simple enum to string method.
    private String strContext(int context) {
        switch(context) {
        case USER_NAME:
            return "user name";
        case USER_ENGINEID:
            return "user engine id";
        case USER_AUTH_PROTOCOL:
            return "user auth protocol";
        case USER_SECURITY_NAME:
            return "user security name";
        case USER_AUTH_PASSWORD:
            return "user auth password";
        case USER_PRIV_PASSWORD:
            return "user priv key";
        case USER_PRIV_PROTOCOL:
            return "user priv protocol";
	case USER_STORAGE_TYPE:
	    return "user storage type";
        default:
            return "unknown parameter";
        }
	
    }

    /**
     * Make the translation from string to object
     * Instantiate and insert a new user.
     */
    private synchronized void translateAndInsert(SnmpEngineId engineId,
						 String strEngineId,
						 String userName,
						 String securityName,
						 String authProtocol,
						 String authPassword,
						 String privProtocol,
						 String privPassword,
						 int storageType,
						 boolean template) 
        throws SecurityConfException {
        byte[] authKey = null;
        byte[] privKey = null;
        SnmpUsmAuthAlgorithm auth = null;
        SnmpUsmPrivAlgorithm priv = null;
	
	if(userName == null)
	    throw new IllegalArgumentException("Missing user.");

        //TOKEN SEMANTIC 
        // We can't do a lot of check because the conf can be set using the SNMP Usm MIB. A lot of hole are allowed.
	
        //AUTH PROTOCOL translation
        if(authProtocol != null && !authProtocol.equals("usmNoAuthProtocol")) {
            auth = (SnmpUsmAuthAlgorithm)
                algos.getAlgorithm(authProtocol);
	    if(auth == null) {
		String msg = "Unknown authentication algorithm : [" + 
		    authProtocol+"]";
		throw new IllegalArgumentException(msg);
	    }
	}

        //AUTH PASSWORD translation
        if(authPassword != null && authPassword.startsWith("0x")) {
	    try {
		authKey = SnmpTools.ascii2binary(authPassword);
	    }catch(Throwable e) {
		throw new IllegalArgumentException("Unexpected exception: "+e);
	    }
        }
	
        //PRIV PROTOCOL translation
        if(privProtocol != null && !privProtocol.equals("usmNoPrivProtocol")) {
            priv = (SnmpUsmPrivAlgorithm) algos.getAlgorithm(privProtocol);
	    if(priv == null) {
		String msg = 
		    "Unknown privacy algorithm : [" + privProtocol +"]";
		throw new IllegalArgumentException(msg);
	    }
        }
	
        //PRIV PASSWORD translation.
        if((privPassword != null) && 
           privPassword.startsWith("0x")) {
	    try {
		privKey = SnmpTools.ascii2binary(privPassword);
	    }catch(Throwable e) {
		throw new IllegalArgumentException("Unexpected exception: " 
						   +e);
	    }
        }
	
        //AUTH PASSWORD TO TRANSLATE to key.
        if( (authPassword != null) && 
            (authKey == null) && 
            (auth != null) ) {
            authKey = auth.password_to_key(authPassword);
        }
	
        //LOCALIZE AUTH KEY
        if(authKey != null && (auth != null) && 
	   (!authPassword.startsWith("0x") ) ) {
            authKey = auth.localizeAuthKey(authKey,
					   engineId);
        }
	
        //PRIV PASSWORD TO TRANSLATE to key.
        if( (privPassword != null) && 
            (privKey == null) && 
            (priv != null) ) {
            if(auth == null)
                throw new IllegalArgumentException(
                "Translating priv password to priv key but auth algo is null");
            privKey = auth.password_to_key(privPassword);
        }
	
        //LOCALIZE PRIV KEY
        if(privKey != null && (priv != null) && 
	   (!privPassword.startsWith("0x"))) {
            if(auth == null)
                throw new IllegalArgumentException(
                          "Localizing priv key but auth algo is null");
            privKey = auth.localizePrivKey(privKey,
					   engineId,
					   priv.getKeySize());
        }
	
        SnmpUsmSecureUser user = new 
            SnmpUsmSecureUserImpl(this,
                                  engineId,
                                  strEngineId,
                                  userName,
                                  securityName,
                                  auth,
				  authProtocol,
                                  authKey,
                                  authPassword,
                                  priv,
				  privProtocol,
                                  privKey,
                                  privPassword,
                                  storageType,
				  template);
	
        //Insert it.
        insertUser(engineId,
                   user,
                   true);
    }

    private synchronized SnmpEngineId translateEngineId(String strEngineId) 
        throws SecurityConfException {
        SnmpEngineId engineId = null;
        int index = strEngineId.indexOf("0x");
        if(index == -1 && 
           !strEngineId.equals("localEngineID")) {
            try {
                // This is the case of <address>:<port>:<iana> format. 
		// Must translate.
                engineId = 
                    SnmpEngineId.createEngineId(strEngineId);
            }catch(UnknownHostException e) {
                throw new IllegalArgumentException("Unknown host : " + 
						   e.getMessage());
            }
            if(logger.finerOn()) {
                logger.finer("translateEngineId", 
			     "Caculate engine Id from : " + 
			     strEngineId + 
			     "\tResulting Id : " + engineId);
            }
        }
        else {
            if(!strEngineId.equals("localEngineID") &&
               !strEngineId.equals(engine.getEngineId().toString()))
                {
		    try {
			// A new engine Id. It is unknown, must create it.
			engineId = SnmpEngineId.createEngineId(strEngineId);
		    }catch(UnknownHostException e) {
			throw new IllegalArgumentException("Unknown host : "+
							   e.getMessage());
		    }
                }
            else {
		// This is the lcal one.
                engineId = engine.getEngineId();
	    }
        }
        return engineId;
    }


    //Called by the consumer once the row is finished.
    void endRow(DataUser d, String row, int line) throws Exception {
	//Format storage type
	int storage = 0;
	if(d.storageType != null) {
	    try {
		storage = Integer.parseInt(d.storageType);
		if(storage != 3) {
		    throw new IllegalArgumentException(
			      "Illegal storage type : " + storage);
		}
	    }catch(NumberFormatException e) {
		throw new IllegalArgumentException("Unexpected exception: "+e);
	    }
	}
	else
	    storage = SnmpUsmLcd.NON_VOLATILE;

	SnmpEngineId engineId = translateEngineId(d.strEngineId);
	
	//Test if user already in file.
	try {
	    findUser(engineId, d.name);
	    throw new IllegalArgumentException("Already defined user");
	}catch(SnmpUsmException e) {   
	}
	
	boolean template = false;
	if(d.template != null)
	    if(d.template.equals("true"))
		template = true;
	
	translateAndInsert(engineId,
			   d.strEngineId,
			   d.name,
			   d.securityName,
			   d.authProtocol,
			   d.authPassword,
			   d.privProtocol,
			   d.privPassword,
			   storage,
			   template);
    }

    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpUsmPasswordLcd");
    
    String dbgTag = "SnmpUsmPasswordLcd";

    class DataUser {
	String strEngineId = null;
	String name = null;
	String authPassword = null;
	String privPassword = null;
	String securityName = null;
	String authProtocol = null;
	String privProtocol = null;
	String storageType = null;
	String template = null;
	void reset() {
	    strEngineId = null;
	    name = null;
	    authPassword = null;
	    privPassword = null;
	    securityName = null;
	    authProtocol = null;
	    privProtocol = null;
	    storageType = null;
	    template = null;
	}
    }

    class UsmConsumer implements SnmpPersistRowFileConsumer {
	DataUser user = new DataUser();
	SnmpUsmPasswordLcd usmlcd = null;
	UsmConsumer(SnmpUsmPasswordLcd usmlcd) {
	    this.usmlcd = usmlcd;
	}
	public Object rowBegin(String row, int line) {
	    user.reset(); 
	    return user;
	}

	public void rowEnd(Object rowContext, String row, int line) 
	    throws Exception  {
	    DataUser data = (DataUser) rowContext;
	    usmlcd.endRow(data, row, line);
	}

	public void treatToken(String row,
			       int lineNumber,
			       String token,
			       int context,
			       Object rowContext,
			       boolean err) throws Exception {
	    DataUser data = (DataUser) rowContext;
	    switch(context) {
	    case USER_ENGINEID:
		if( err || (token == null) )
		    throw new IllegalArgumentException("Missing engine Id.");
		if(logger.finerOn())
		    logger.finer("treatToken", "context = " + 
				 strContext(context) + " value : " + token);
		
		data.strEngineId = token;
		break;
	    case USER_NAME:
		if(err || (token == null) )
		    throw new IllegalArgumentException("Missing user.");
		if(logger.finerOn())
		    logger.finer("treatToken", "context = " + 
				 strContext(context) + " value : " + token);
		
		data.name = token;
		break;
	    case USER_SECURITY_NAME:
		if(err)
		    return;
		else
		    data.securityName = token;
		break;
	    case USER_AUTH_PROTOCOL:
		if(err)
		    return;
		else
		    data.authProtocol = token;
		break;
	    case USER_AUTH_PASSWORD:
		if(err)
		    return;
		else
		    data.authPassword = token;
		break;
	    case USER_PRIV_PROTOCOL:
		if(err)
		    return;
		else
		    data.privProtocol = token;
		break;
                       
	    case USER_PRIV_PASSWORD:
		if(err)
		    return;
		else
		    data.privPassword = token;
		break;
	    case USER_STORAGE_TYPE:
		if(err)
		    return;
		else
		    data.storageType = token;
		break;
	    case USER_TEMPLATE:
		if(err)
		    return;
		else
		    data.template = token;
		break;
	    default:
		if(logger.finestOn())
		    logger.finest("TreatToken", "Unknown context.");
		throw new IllegalArgumentException("Unknown parameter");
	    }
	}
    }
    
}
