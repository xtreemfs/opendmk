/*
 * @(#)file      SnmpUsmSecureUserImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.25
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

import com.sun.management.internal.snmp.SnmpTools;
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpDefinitions;

/**
 * FOR INTERNAL USE ONLY. SecureUser Jdmk internal implementation. 
 * No algorithm associated, it is mainly a Usm user storage class.
 *
 * @since Java DMK 5.1
 */
class SnmpUsmSecureUserImpl implements SnmpUsmSecureUser {
    //Usm parameters
    private String dbgTag = "SnmpUsmSecureUserImpl";
    String name = null;
    String securityName = null;
    String password = null;
    SnmpUsmAuthPair auth = null;
    SnmpUsmPrivPair priv = null;
    SnmpEngineId engineId = null;
    //Text file separator.
    String CONF_SEPARATOR = ",";
    int storageType = 1; //This value means "other";
    SnmpUsmLcd lcd = null;
    String privKey = null;
    String authKey = null;
    String strEngineId = null;
    String privProtocol = null;
    String authProtocol = null;
    boolean template = false;
    //Constructor.
    SnmpUsmSecureUserImpl(SnmpUsmLcd lcd,
			  SnmpEngineId engineId,
			  String name) {
	init(lcd, engineId, name);
	securityName = name;
	this.strEngineId = engineId.toString();
    }

    //Constructor. String parameters are used when storing conf in file.
    SnmpUsmSecureUserImpl(SnmpUsmLcd lcd,
			  SnmpEngineId engineId,
			  String strEngineId,
			  String name,
			  String securityName,
			  SnmpUsmAuthAlgorithm auth,
			  String authProtocol,
			  byte[] localAuthKey,
			  String authKey,
			  SnmpUsmPrivAlgorithm priv,
			  String privProtocol,
			  byte[] localPrivKey,
			  String privKey,
			  int storage,
			  boolean template) {
	init(lcd, engineId, name);
	this.strEngineId = strEngineId;
	this.securityName = securityName;
	this.auth.algo = auth;
	this.authProtocol = authProtocol;
	this.auth.key = localAuthKey;
	this.priv.algo = priv;
	this.privProtocol = privProtocol;
	this.priv.key = localPrivKey;
	this.authKey = authKey;
	this.privKey = privKey;
	this.storageType = storage;
	this.template = template;
    }
    /**
     * Update the configuration. The persistent area will be updated
     * with user values.
     */
    public void updateConfiguration() {
	((SnmpUsmPasswordLcd)lcd).flushFile();
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public int getSecurityLevel() {
	int val = SnmpDefinitions.noAuthNoPriv;
	if(auth.algo != null && auth.key != null)
	    val |= SnmpDefinitions.authMask;
	if(priv.algo != null && priv.key != null)
	    val |= SnmpDefinitions.privMask;
	
	return val;
    }

    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public byte[] getAuthDelta(byte[] newKey,
			       byte[] random) {
	if(auth.algo == null) return null;

	return auth.algo.calculateAuthDelta(auth.key, 
					    newKey, 
					    random);
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public byte[] getPrivDelta(byte[] newKey,
			       byte[] random) {
	if(auth.algo == null || priv.algo == null) return null;
	

	return auth.algo.calculatePrivDelta(priv.key, 
					    newKey, 
					    random,
					    priv.algo.getDeltaSize());
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public void setAuthKeyChange(byte[] randomdelta) {
	if(auth.algo == null) return;

	if(logger.finestOn())
	    logger.finest("setAuthKeyChange"," auth algo : " + auth.algo);
	
	auth.key = auth.algo.calculateNewAuthKey(auth.key, randomdelta);
	authKey = SnmpTools.binary2ascii(auth.key);
    }
    
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public void setPrivKeyChange(byte[] randomdelta) {
	if(priv.algo == null) return;
	
	if(logger.finestOn())
	    logger.finest("setPrivKeyChange"," priv algo : " + priv.algo);
	
	priv.key = auth.algo.calculateNewPrivKey(priv.key, randomdelta, 
						 priv.algo.getDeltaSize());
	privKey = SnmpTools.binary2ascii(priv.key);
	
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public void setAuthAlgorithm(String name) {
	if(name.equals("usmNoAuthProtocol") || 
	   name.equals(SnmpUsm.usmNoAuthProtocol) ){
	    authProtocol = "usmNoAuthProtocol";
	    auth.algo = null;
	    auth.key = null;
	    authKey = null;
	    return;
	}
	auth.algo = (SnmpUsmAuthAlgorithm)
	    lcd.getAlgorithmManager().getAlgorithm(name);
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public void setPrivAlgorithm(String name) {
	if(name.equals("usmNoPrivProtocol")|| 
	   name.equals(SnmpUsm.usmNoPrivProtocol) ) {
	    privProtocol = "usmNoPrivProtocol";
	    priv.algo = null; 
	    priv.key = null;
	    privKey = null;
	    return;
	}
	priv.algo = (SnmpUsmPrivAlgorithm)
	    lcd.getAlgorithmManager().getAlgorithm(name);
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public String getName() { 
	return name; 
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public void setSecurityName(String s) { 
	securityName = s; 
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public String getSecurityName() { 
	return securityName; 
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public SnmpEngineId getEngineId() { 
	return engineId; 
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public int getStorageType() { 
	return storageType; 
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public void setStorageType(int s) { 
	storageType = s;
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public SnmpUsmAuthPair getAuthPair() { 
	return auth; 
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public SnmpUsmPrivPair getPrivPair() { 
	return priv; 
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public void cloneAuthPair(SnmpUsmAuthPair pair) {
	auth.algo = pair.algo;
	
	if(auth.algo != null)
	    authProtocol = auth.algo.getAlgorithm();
	
	if(pair.key != null) {
	    byte[] key = new byte[pair.key.length];
	    for(int i = 0; i < key.length; i++)
		key[i] = pair.key[i];
	    auth.key = key;
	    authKey = SnmpTools.binary2ascii(auth.key);
	}
    }
    /**
     * See SnmpUsmSecureUSer interface for details.
     */
    public void clonePrivPair(SnmpUsmPrivPair pair) {
	priv.algo = pair.algo;
	if(priv.algo != null)
	    privProtocol = priv.algo.getAlgorithm();
	if(pair.key != null) {
	    byte[] key = new byte[pair.key.length];
	    for(int i = 0; i < key.length; i++)
		key[i] = pair.key[i];
	    priv.key = key;
	    privKey = SnmpTools.binary2ascii(priv.key);
	}
    }

    //Package methods
    //

    byte[] getAuthKey() { 
	return auth.key; 
    }

    byte[] getPrivKey() { 
	return priv.key; 
    }

    void setPrivKey(byte[] privKey) {
	priv.key = privKey;
    }

    void setAuthKey(byte[] authKey) {
	auth.key = authKey;
    }
    //Generate the conf file string.
    String getConf() {
	String authKeyConf = (authKey == null) ? CONF_SEPARATOR :
	    authKey + CONF_SEPARATOR;
	String privKeyConf = (privKey == null) ? CONF_SEPARATOR :
	    privKey + CONF_SEPARATOR;
	String securityNameConf = (securityName == null) ? CONF_SEPARATOR : 
	    securityName + CONF_SEPARATOR;
	String storageTypeConf = storageType + CONF_SEPARATOR;
	String authProtocolConf = (authProtocol == null) ? CONF_SEPARATOR :
	    authProtocol + CONF_SEPARATOR;
	String privProtocolConf = (privProtocol == null) ? CONF_SEPARATOR :
	    privProtocol + CONF_SEPARATOR;

	String conf = null;
	String mandatory = strEngineId + CONF_SEPARATOR + 
	    name + CONF_SEPARATOR +
	    securityNameConf + 
	    authProtocolConf + 
	    authKeyConf + 
	    privProtocolConf + 
	    privKeyConf + 
	    storageTypeConf;
	
	if(template == true)
	    conf = mandatory + "true";
	else
	    conf = mandatory;

	return conf;
	    
    }
    
    public boolean isTemplate() {
	return template;
    }

    public void setTemplateStatus(boolean status) {
	template = status;
    }

    //Common init to all constructors.
    private void init(SnmpUsmLcd lcd,
		      SnmpEngineId engineId,
		      String name) {
	this.engineId = engineId;
	this.name = name;
	this.lcd = lcd;
	this.auth = new SnmpUsmAuthPair();
	this.priv = new SnmpUsmPrivPair();
    }

    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpUsmSecureUserImpl");
    
}
