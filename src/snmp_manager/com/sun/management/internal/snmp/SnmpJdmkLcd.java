/*
 * @(#)file      SnmpJdmkLcd.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.38
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

import java.io.*;
import com.sun.jdmk.internal.ClassLogger;

import com.sun.jdmk.defaults.JdmkProperties;
import com.sun.jdmk.defaults.DefaultPaths;
import com.sun.jdmk.internal.BackupFileHandler;
import com.sun.management.snmp.SnmpEngineId;
/**
 * The provided engine Local Configuration Datastore. It is based on a text 
 * file. 
 * The file to pass to <CODE>SnmpJdmkLcd</CODE> is <CODE>jdmk.security</CODE>
 * located in the default configuration directory.
 * In this implementation the information is stored on a flat file and its 
 * default location is specified in the following order: 
 * <p>
 * <OL>
 * <LI>The call to {@link 
 *    com.sun.management.snmp.SnmpEngineParameters#setSecurityFile(String)
 *    SnmpEngineParameters.setSecurityFile(<CODE>jdmk.security</CODE>)}.
 * <LI>The value of the <CODE>jdmk.security.file</CODE> property.
 * <LI>The return value of <CODE>getEtcDir("conf" + File.separator + 
 * "jdmk.security")</CODE>
 * in class {@link com.sun.jdmk.defaults.DefaultPaths DefaultPaths}.
 * <BR><B>Note: make sure the Java DMK installation directory could be 
 * derived from the <CODE>CLASSPATH</CODE> environment variable.</B>
 * <LI>The value of <CODE>System.getProperty("user.dir") + File.separator + 
 * etc + File.separator + conf + File.separator + jdmk.security</CODE>
 * if the Java DMK installation directory could not be derived from the 
 * <CODE>CLASSPATH</CODE>.
 * </OL>
 *
 * @since Java DMK 5.1
 */
public class SnmpJdmkLcd extends SnmpLcd{
    
    String engineId = null;
    File file = null;
    int engineBoots = -1;
    private String header = "\n# #####APPENDED PROPERTY####\n";
    /**
     * Constructor. Will use the provided file name.
     * @param lcdFile The configuration file name.
     * @throws IllegalArgumentException If the specified file doesn't exist.
     */
    public SnmpJdmkLcd(String lcdFile) throws IllegalArgumentException {
	handleSecurityFileLocation(lcdFile);
    }
    
    /**
     * Define where to find the security file.
     */
    private void handleSecurityFileLocation(String securityFile) 
	throws IllegalArgumentException{
	String lcdFile = null;
	if(securityFile == null) {
	    lcdFile = (String) System.getProperty(JdmkProperties.SECURITY_FILE);
	    if (lcdFile == null) {
		if(logger.finestOn()) {
		    logger.finest("constructor","Security file not found. Use default one.");
		}
		lcdFile = DefaultPaths.getEtcDir("conf" + 
 						 File.separator + 
						 "jdmk.security");
		file = new File(lcdFile);
		if(!file.exists()) {
		    file = null;
		    if (logger.finerOn()) {
			logger.finer("SnmpJdmkLcd", "The default file ["+ lcdFile +"] doesn't exist.");
		    }
		    return;
		}
	    }
	    else {
		file = new File(lcdFile);
		if(!file.exists()) {
		    if (logger.finerOn()) {
			logger.finer("handleSecurityFileLocation", "The specified file ["+ file +"] doesn't exist, no configuration loaded");
			throw new IllegalArgumentException("The specified file ["+ file +"] doesn't exist, no configuration loaded");
		    }
		}
	    }   
	}
	else {
	    file = new File(securityFile);
	    if(!file.exists()) {
		if (logger.finerOn()) {
		    logger.finer("handleSecurityFileLocation", "The specified file ["+ file +"] doesn't exist, no configuration loaded");
		    throw new IllegalArgumentException("The specified file ["+ file +"] doesn't exist, no configuration loaded");
		}
	    }
	}
    }


    File getFile() {
	return file;
    }
    /**
     * Persists the number of reboots. Called internally by <CODE> SnmpEngine </CODE> class. Don't call these methods directly.
     * @param i Reboot number.
     */
    public void storeEngineBoots(int i) {
	if(getFile() == null) return;
	String s = "localEngineBoots";
	flushProperty(s, String.valueOf(i));
    }

    /**
     * Persists the engine Id. Called internally by <CODE> SnmpEngine </CODE> class. Don't call these methods directly.
     * @param id The engine Id.
     */
    public void  storeEngineId(SnmpEngineId id) {
	if(getFile() == null) return;
	String s = "localEngineID";
	flushProperty(s, id.getReadableId() == null ? id.toString() : id.getReadableId());
    }
    /**
     * Returns the number of time the engine rebooted. Called internally by <CODE> SnmpEngine </CODE> class. Don't call these methods directly.
     * @return The number of reboots or -1 if the information is not present in the Lcd.
     */
    public int getEngineBoots() {
	if(getFile() == null) return -1;
	if(engineBoots == -1) {
	    String s = getProperty("localEngineBoots");
	    if(s != null)
		engineBoots = Integer.parseInt(s);
	}
	
	return engineBoots;
    }
    /** 
     * Returns the engine Id located in the Lcd. Called internally by <CODE> SnmpEngine </CODE> class. Don't call these methods directly.
     * @return The engine Id or null if the information is not present in the Lcd.
     */
    public String getEngineId(){
	if(getFile() == null) return null;
	if(engineId == null)
	    engineId = getProperty("localEngineID");
	
	return engineId;
    }
    /**
     * Method that returns the value associated to the passed property in the configuration file (eg:localEngineID=0x78789098).
     */
    private String getProperty(String property) {
	String row = null;
	FileReader in = null;
	BufferedReader reader = null;
	if(!file.exists()) {
	    if (logger.finerOn()) {
		logger.finer("getProperty", "The specified file ["+ file +"] doesn't exist, the property "+ property +"] can't be loaded.");
	    }
	    return null;
	}
	try {
	    in = new FileReader(file);
	    reader = new BufferedReader(in);
	}
	catch(IOException e) {
	    if (logger.finestOn()) {
		logger.finest("getProperty", "The specified file was not found, no configuration loaded");
	    }
	    return null;
	}
	try{
	    row = reader.readLine();
	}catch(IOException ex) { 
	    if(logger.finerOn())
		logger.finer("getProperty", ex.toString());
	    return null;
	}
	while(row != null) {
	    try{
		if(row.startsWith(property + "=")) {
		    reader.close();
		    int size = row.indexOf("=");
		    // If no value is associated to property.
		    if(size == (row.length() - 1) )
			return null;
		    else
			return row.substring(size + 1);
		}

		row = reader.readLine();
		
	    }catch(IOException e) { 
		if(logger.finestOn())
		    logger.finest("getProperty", e);
	    }
	}
	try{
	    reader.close();
	    in.close();
	}catch(IOException e2) { 
	    if(logger.finestOn())
		logger.finest("getProperty", e2);
	    return null;
	}
	return null;
    }

    private void writeFile(File backupFile, 
			   String property, 
			   String value) throws Exception {
	String row = null;
	boolean patched = false;
	boolean found =false;
	BufferedWriter writer = null;
	BufferedReader reader = null;
	FileWriter out = null;
	FileReader in = null;
	try {
	    out = new FileWriter(file);
	    writer = new BufferedWriter(out);
	    in = new FileReader(backupFile);
	    reader = new BufferedReader(in);
	    
	    row = reader.readLine();
	    while(row != null) {
		String toFlush = row;
		//Do some work to replace the existing property.
		if(!found) {
		    if(row.startsWith(property + "=")) {
			if(logger.finestOn())
			    logger.finest("flushProperty", "Found property [" + property +"], new value [" + value + "]");
			toFlush = property +"="+ value;
			found = true;
		    }
		}
		writer.write(toFlush, 0, toFlush.length());
		writer.newLine();
		
		row = reader.readLine();
	    }
	    if(!found) {
		String toFlush = property +"="+ value;
		if(logger.finestOn())
		    logger.finest("flushProperty", "Property not found!");
		writer.write(header, 0, header.length());
		writer.write(toFlush, 0, toFlush.length());
	    }  
	}catch(Exception e) {
	    if(logger.finestOn())
		logger.finest("flushProperty", e);
	}finally {
	    if(writer != null) {
		try {
		    writer.close();
		}catch(Exception e) { 
		    if(logger.finestOn())
			logger.finest("flushProperty", e);
		}
	    }
	    if(reader != null) {
		try {
		    reader.close();
		}catch(Exception e) { 
		    if(logger.finestOn())
			logger.finest("flushProperty", e);
		}
	    }
	    if(in != null) {
		try {   
		    in.close();
		}catch(Exception e) { 
		    if(logger.finestOn())
			logger.finest("flushProperty", e);
		}
	    }
	    if(out != null) {
		try {
		    out.close();
		}catch(Exception e) { 
		    if(logger.finestOn())
			logger.finest("flushProperty", e);
		}
	    }
	}
    }

    private void resetFile() throws Exception {
	try {
	    file.delete();
	    file.createNewFile();
	}catch(Exception e) {
	    if(logger.finestOn())
		logger.finest("resetFile", e);
	    if(logger.finestOn())
		logger.finest("resetFile",
		      "File reset failed.");
	    return;
	}
    }
    
    private void flushProperty(String property, String value) {

	File backupFile = BackupFileHandler.createBackupFile(file, null);
	
	if(backupFile == null) {
	    if(logger.finestOn())
		logger.finest("flushProperty", 
		      "Unable to create backup file for "+ file + 
		      "Property " + property + " not flushed");
	    return;
	}

	if(logger.finestOn())
	    logger.finest("flushProperty", file + 
		  " backup file created : " + backupFile +".");
	
	try {
	    //resetFile();
	    writeFile(backupFile, property, value);
	}catch(Exception e) {
	    if(logger.finestOn())
		logger.finest("resetFile", e);
	    if(logger.finestOn())
		logger.finest("resetFile",
		      "File reset failed." + 
		      "Check " + backupFile + " file.");
	}
	finally {
	    BackupFileHandler.deleteBackupFile(backupFile);
	}
    }

    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpJdmkLcd");
    
    String dbgTag = "SnmpJdmkLcd"; 
}
