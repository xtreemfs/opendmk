/*
 * @(#)file      SnmpUsmKeyHandler.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.14
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
package com.sun.management.snmp;

/**
 * This interface allows you to compute key localization and delta generation. It is useful when adding user in USM MIB. An instance of <CODE> SnmpUsmKeyHandler </CODE> is associated to each <CODE> SnmpEngine </CODE> object.
 * When computing key, an authentication algorithm is needed. The supported ones are : usmHMACMD5AuthProtocol and usmHMACSHAAuthProtocol.
 *
 * @since Java DMK 5.1
 */
public interface SnmpUsmKeyHandler {
    
    /**
     * DES privacy algorithm key size. To be used when localizing privacy key
     */
    public static int DES_KEY_SIZE = 16;

    /**
     * DES privacy algorithm delta size. To be used when calculating privacy key delta.
     */
    public static int DES_DELTA_SIZE = 16;
    
    /**
     * Translate a password to a key. It MUST be compliant to RFC 2574 description.
     * @param algoName The authentication algorithm to use.
     * @param password Password to convert.
     * @return The key.
     * @exception IllegalArgumentException If the algorithm is unknown.
     */
    public byte[] password_to_key(String algoName, String password) throws IllegalArgumentException;
    /**
     * Localize the passed key using the passed <CODE>SnmpEngineId</CODE>. It MUST be compliant to RFC 2574 description.
     * @param algoName The authentication algorithm to use.
     * @param key The key to localize;
     * @param engineId The Id used to localize the key.
     * @return The localized key.
     * @exception IllegalArgumentException If the algorithm is unknown.
     */
    public byte[] localizeAuthKey(String algoName, byte[] key, SnmpEngineId engineId) throws IllegalArgumentException;

    /**
     * Localize the passed privacy key using the passed <CODE>SnmpEngineId</CODE>. It MUST be compliant to RFC 2574 description.
     * @param algoName The authentication algorithm to use.
     * @param key The key to localize;
     * @param engineId The Id used to localize the key.
     * @param keysize The privacy algorithm key size.
     * @return The localized key.
     * @exception IllegalArgumentException If the algorithm is unknown.
     */
    public byte[] localizePrivKey(String algoName, byte[] key, SnmpEngineId engineId,int keysize) throws IllegalArgumentException;
    
    /**
     * Calculate the delta parameter needed when processing key change. This computation is done by the key change initiator. It MUST be compliant to RFC 2574 description.
     * @param algoName The authentication algorithm to use.
     * @param oldKey The old key.
     * @param newKey The new key.
     * @param random The random value.
     * @return The delta.
     * @exception IllegalArgumentException If the algorithm is unknown.
     */
    public byte[] calculateAuthDelta(String algoName, byte[] oldKey, byte[] newKey, byte[] random) throws IllegalArgumentException;
    
    /**
     * Calculate the delta parameter needed when processing key change for a privacy algorithm. This computation is done by the key change initiator. It MUST be compliant to RFC 2574 description.
     * @param algoName The authentication algorithm to use.
     * @param oldKey The old key.
     * @param newKey The new key.
     * @param random The random value.
     * @param deltaSize The algorithm delta size.
     * @return The delta.
     * @exception IllegalArgumentException If the algorithm is unknown.
     */
    public byte[] calculatePrivDelta(String algoName, byte[] oldKey, byte[] newKey, byte[] random, int deltaSize) throws IllegalArgumentException;
    
}
