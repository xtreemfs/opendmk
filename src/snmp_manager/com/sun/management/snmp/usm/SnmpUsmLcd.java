/*
 * @(#)file      SnmpUsmLcd.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.29
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

import java.util.Enumeration;

import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.internal.snmp.SnmpModelLcd;
/**
 * An Usm Lcd is handling user configuration needed by the User based 
 * Security Model as defined in RFC 2574. It retrieves the initial 
 * configuration and allow to add user at runtime.
 *
 * @since Java DMK 5.1
 */
public interface SnmpUsmLcd extends SnmpModelLcd {
    /**
     * Means that the data stored is permanent.
     */
    public static final int NON_VOLATILE = 3;

    /**
     * Means that the data stored is volatile.
     */
    public static final int VOLATILE = 2;

    /**
     * Returns the storage type (<CODE>PERMANENT</CODE> or 
     * <CODE>VOLATILE</CODE>).
     * @return The storage type.
     */
    public int getStorageType();

    /**
     * Asks the Usm Lcd to create a new user in the storage space. This is a 
     * user factory.
     * @param engineId The authoritative engine Id the user is associated to.
     * @param userName The user name.
     * @return The newly created user.
     */
    public SnmpUsmSecureUser createNewUser(byte[] engineId, 
					   String userName);
    /**
     * Removes a user form the Lcd. It will be removed only if it exists.
     * @param engineId The engine Id to which the user is associated.
     * @param userName The user name.
     * @param notifyMIB True will notify the MIB.
     */
    public void removeUser(SnmpEngineId engineId,
			   String userName,
			   boolean notifyMIB);
    /**
     * Gets an enumeration of all the users <CODE>SnmpUsmSecureUser</CODE> 
     * located in the Lcd.
     * @return The enumeration of users.
     */
    public Enumeration getAllUsers();

    /**
     * Adds an Usm MIB table.
     * @param table The MIB table.
     */
    public void setMibTable(SnmpUsmMibTable table);
    
    /**
     * Adds an unknown received engine Id to the Lcd. Could be useful from 
     * the manager side.
     * @param engineId The unknown engine Id.
     */
    public void addEngine(SnmpEngineId engineId);

    /**
     * Gets the <CODE>SnmpUsmAuthAlgorithm</CODE>/<CODE>Localized key</CODE> 
     * pair associated to a user name and authoritative engine Id.
     * @param engineId The authoritative engine Id.
     * @param userName The user name.
     */
    public SnmpUsmAuthPair getUserAuthPair(SnmpEngineId engineId, 
					   String userName) 
	throws SnmpUsmAuthAlgorithmException, 
	       SnmpUsmEngineIdException, 
	       SnmpUsmUserNameException;
    
    /**
     * Gets the <CODE> SnmpUsmSecureUser </CODE> for the passed user name 
     * and authoritative engine Id.
     * @param engineId The authoritative engine Id.
     * @param userName The user name.
     */
    public SnmpUsmSecureUser getUser(SnmpEngineId engineId, String userName) 
	throws SnmpUsmEngineIdException, SnmpUsmUserNameException;
    
    /**
     * Gets the <CODE>SnmpUsmPrivAlgorithm</CODE>/<CODE>Localized key</CODE> 
     * pair associated to a user name and authoritative engine Id.
     * @param engineId The authoritative engine Id.
     * @param userName The user name.
     */
    public SnmpUsmPrivPair getUserPrivPair(SnmpEngineId engineId, 
					   String userName) 
	throws SnmpUsmPrivAlgorithmException, 
	       SnmpUsmEngineIdException, 
	       SnmpUsmUserNameException;

    /** 
     * Adds a user to the Usm Lcd. The key is in a password format. Passwords
     * must be translated in key.
     * In case the passed user exists, it is updated with the new parameters.
     * @param engineId The authoritative engine Id.
     * @param userName The user name.
     * @param securityName The security user name.
     * @param authProtocol The authentication algorithm name.
     * @param authPassword The authentication password.
     * @param privProtocol The privacy algorithm name.
     * @param privKey The privacy key. No password translation. This 
     *        <CODE>String</CODE> must start with 0x.
     * @param storage The storage type (<CODE>PERMANENT</CODE> or 
     *        <CODE>VOLATILE</CODE>).
     * @param template True, the user is a template. A template is not 
     *        registered in the USM MIB.
     */ 
    public void addUser(SnmpEngineId engineId,
			String userName,
			String securityName,
			String authProtocol,
			String authPassword,
			String privProtocol,
			String privKey,
			int storage,
			boolean template)
	throws SnmpUsmException;

    /** 
     * Adds a user to the Usm Lcd. 
     * @param user The user to add. 
     * @param notifyMIB True will notify the MIB.
     * 
     */
    public void addUser(SnmpUsmSecureUser user,
			boolean notifyMIB) throws SnmpUsmException;
    /**
     * Associates an <CODE>SnmpUsmAlgorithmManager</CODE>. This manager is 
     * used to transform algorithm names in algorithm objects.
     * @param manager The algorithm manager.
     */
    public void setAlgorithmManager(SnmpUsmAlgorithmManager manager);

    /**
     * Gets the associated <CODE>SnmpUsmAlgorithmManager</CODE>. This manager 
     * is used to transform algorithm names in algorithm objects.
     * @return The algorithm manager.
     */
    public SnmpUsmAlgorithmManager getAlgorithmManager();

    /**
     * Tells the Lcd to synchronize with its data source. Must be called at 
     * least once in order to read configuration. 
     */
    public void syncDataSource();

    /**
     * Asks the Lcd to set the key change parameters to the passed user. This 
     * is done after a remote configuration authentication key change action.
     * @param user The user that has its key changed.
     * @param keyChange The value to use at key compute time.
     */
    public void setUserAuthKeyChange(SnmpUsmSecureUser user,
				     byte[] keyChange);
    
    /**
     * Asks the Lcd to set the key change parameters to the passed user. This 
     * is done after a remote configuration privacy key change action.
     * @param user The user that has its key changed.
     * @param keyChange The value to use at key compute time.
     */
    public void setUserPrivKeyChange(SnmpUsmSecureUser user,
				     byte[] keyChange);

}

