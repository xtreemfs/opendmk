/*
 * @(#)file      SnmpUsmSecurityParameters.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.21
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

import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpEngineId;
/** 
 * This interface models the set of security parameters needed to activate security in the User Security Model. They are RFC 2574 compliant.
 * These parameters are responsible for their own encoding and decoding.
 *
 * @since Java DMK 5.1
 */
public interface SnmpUsmSecurityParameters extends SnmpSecurityParameters {
    
    /**
     * Gets the authoritative engine Id.
     * @return The engine Id.
     */
    public SnmpEngineId getAuthoritativeEngineId();
    /**
     * Sets the authoritative engine Id.
     * @param authoritativeEngineId The engine Id.
     */
    public void setAuthoritativeEngineId(SnmpEngineId authoritativeEngineId);
    /**
     * Gets the authoritative engine nb boots.
     * @return The engine nb boots.
     */
    public int getAuthoritativeEngineBoots();
    /**
     * Sets the authoritative engine nb boots.
     * @param authoritativeEngineBoots The engine nb boots.
     */
    public void setAuthoritativeEngineBoots(int authoritativeEngineBoots);
    /**
     * Gets the authoritative engine time.
     * @return The engine time.
     */
    public int getAuthoritativeEngineTime();
    /**
     * Sets the authoritative engine time.
     * @param authoritativeEngineTime The engine time.
     */
    public void setAuthoritativeEngineTime(int authoritativeEngineTime);
    /**
     * Gets the user name.
     * @return The user name.
     */
    public String getUserName();
    /**
     * Sets the user name.
     * @param userName The user name.
     */
    public void setUserName(String userName);
    /**
     * Gets the authentication parameters (e.g. HMAC).
     * @return The authentication parameters.
     */
    public byte[] getAuthParameters();
     /**
     * Sets the authentication parameters.
     * @param authParameters The authentication parameters.
     */
    public void setAuthParameters(byte[] authParameters);
    /**
     * Gets the privacy parameters (e.g. DES IV parameter).
     * @return The privacy parameters.
     */
    public byte[] getPrivParameters();
    /**
     * Sets the privacy parameters.
     * @param privParameters The privacy parameters.
     */
    public void setPrivParameters(byte[] privParameters);
}
