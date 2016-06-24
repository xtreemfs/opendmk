/*
 * @(#)file      SnmpUsmAuthAlgorithm.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.19
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
 * Authentication algorithm interface. Every authentication algorithm must be
 * compliant to this interface. When developing your own authentication 
 * algorithm you have to implement this interface.
 *
 * @since Java DMK 5.1
 */ 
public interface SnmpUsmAuthAlgorithm extends SnmpUsmAlgorithm {
    /**
     * Sign some data using a key.
     * @param key The key to use.
     * @param text The data to sign.
     * @param length The data length.
     * @return The data signature.
     */
    public byte[] sign(byte key[], byte text[], int length);

    /**
     * Verify a received signed data.
     * @param key The key to use.
     * @param data The data that has been signed with the key.
     * @param length The data length.
     * @param signature The signature to compare to.
     * @return <CODE>true</CODE> if signatures are equals, 
     * <CODE>false</CODE> otherwise.
     */
    public boolean verify(byte []key,
			  byte []data,
			  int length,
			  byte []signature);
    /**
     * Translate a password to a key. It MUST be compliant to RFC 2574 
     * description.
     * @param password Password to convert.
     * @return The key.
     */
    public byte[] password_to_key(String password);

    /**
     * Localize the passed key using the passed <CODE>SnmpEngineId</CODE>. 
     * It MUST be compliant to RFC 2574 description.
     * @param key The key to localize;
     * @param engineId The Id used to localize the key.
     * @return The localized key.
     */
    public byte[] localizeAuthKey(byte[] key,
				  SnmpEngineId engineId);

    /**
     * Localize the passed privacy key using the passed 
     * <CODE>SnmpEngineId</CODE>. It MUST be compliant to RFC 2574 description.
     * @param key The key to localize;
     * @param engineId The Id used to localize the key.
     * @param keysize The privacy algorithm key size.
     * @return The localized key.
     */
    public byte[] localizePrivKey(byte[] key,
				  SnmpEngineId engineId,
				  int keysize);
    
    /**
     * Calculate the delta parameter needed when processing key change. This 
     * computation is done by the key change initiator. It MUST be compliant 
     * to RFC 2574 description.
     * @param oldKey The old key.
     * @param newKey The new key.
     * @param random The random value.
     * @return The delta.
     */
    public byte[] calculateAuthDelta(byte[] oldKey, byte[] newKey, 
				     byte[] random);
    
    /**
     * Calculate the delta parameter needed when processing key change for a 
     * privacy algorithm. This computation is done by the key change 
     * initiator. It MUST be compliant to RFC 2574 description.
     * @param oldKey The old key.
     * @param newKey The new key.
     * @param random The random value.
     * @param deltaSize The algorithm delta size.
     * @return The delta.
     */
    public byte[] calculatePrivDelta(byte[] oldKey, byte[] newKey, 
				     byte[] random, int deltaSize);

    /**
     * Compute the new key and return it. It MUST be compliant to RFC 2574 
     * description. This is done mainly in the agent side.
     * @param oldKey The old key.
     * @param randomdelta Random and received delta concatenation.
     */
    public byte[] calculateNewAuthKey(byte[] oldKey, byte[] randomdelta);
	
    /**
     * Compute the new key and return it. It MUST be compliant to RFC 2574 
     * description. This is done mainly in the agent side.
     * @param oldKey The old key.
     * @param randomdelta Random and received delta concatenation.
     * @param deltaSize The algorithm deltaSize
     */
    public byte[] calculateNewPrivKey(byte[] oldKey, byte[] randomdelta, 
				      int deltaSize);
}
