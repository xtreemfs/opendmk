/*
 * @(#)file      SnmpUsmSecureUser.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.17
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

import com.sun.management.snmp.SnmpEngineId;
/**
 * This interface models an Usm user. This is the interface that is known from the {@link com.sun.management.snmp.usm.SnmpUsmLcd SnmpUsmLcd}
 *
 * @since Java DMK 5.1
 */
public interface SnmpUsmSecureUser {
    /**
     * Gets the user name.
     * @return The user name.
     */
    public String getName();
    /**
     * Gets the user security name.
     * @return The user security name.
     */
    public String getSecurityName();
    /**
     * Gets the authoritative engine Id.
     * @return The authoritative engine Id.
     */
    public SnmpEngineId getEngineId();
    /**
     * Gets the storage type.
     * @return The storage type (<CODE>PERMANENT</CODE> or <CODE>VOLATILE</CODE>).
     */
    public int getStorageType();
    /**
     * Sets the authentication algorithm.
     * @param name The algorithm name.
     */
    public void setAuthAlgorithm(String name);
   
    /**
     * Clones the passed pair.
     * @param pair The authentication pair.
     */
    public void cloneAuthPair(SnmpUsmAuthPair pair);
    /**
     * Clones the passed pair.
     * @param pair The privacy pair.
     */
    public void clonePrivPair(SnmpUsmPrivPair pair);
    /**
     * Sets the privacy algorithm.
     * @param name The algorithm name.
     */
    public void setPrivAlgorithm(String name);
     /**
     * Sets the security name.
     * @param s The security name.
     */
    public void setSecurityName(String s);
     /**
     * Sets the storage type.
     * @param storage The storage type (<CODE>PERMANENT</CODE> or <CODE>VOLATILE</CODE>).
     */
    public void setStorageType(int storage);
     /**
     * Gets the authentication key delta needed when processing key change.
     * @param newKey The new key.
     * @param random The random part of key change.
     * @return The computed delta.
     */
    public byte[] getAuthDelta(byte[] newKey,
			       byte[] random);
     /**
     * Gets the privacy key delta needed when processing key change.
     * @param newKey The new key.
     * @param random The random part of key change.
     * @return The computed delta.
     */
    public byte[] getPrivDelta(byte[] newKey,
			       byte[] random); 
    /**
     * Sets the random delta value that comes from remote configuration.
     * @param randomdelta The key change value.
     */
    public void setAuthKeyChange(byte[] randomdelta);
    /**
     * Sets the random delta value that comes from remote configuration.
     * @param randomdelta The key change value.
     */
    public void setPrivKeyChange(byte[] randomdelta);
    /**
     * Gets the authentication pair. A pair is an algorithm/key instance. A pair can be null if the algorithm provided in the configuration is not loaded in the agent. 
     * @return The authentication pair.
     */
    public SnmpUsmAuthPair getAuthPair();
    /**
     * Gets the privacy pair.
     * @return The privacy pair.
     */
    public SnmpUsmPrivPair getPrivPair();
    /**
     * Gets the security level.
     * @return The security level.
     */
    public int getSecurityLevel();

    /**
     * Update the configuration. The persistent area will be updated with user values.
     */
    public void updateConfiguration();
    
    /**
     * checks if the user is a template. A template is not registered in USM MIB. By default, secureUser are not template.
     * @return True, the user is a template. 
     */
    public boolean isTemplate();

    /**
     * Set the template status. A template is not registered in USM MIB. By default, secureUser are not template.
     * @param state The template state.
     */
    public void setTemplateStatus(boolean state);
}
