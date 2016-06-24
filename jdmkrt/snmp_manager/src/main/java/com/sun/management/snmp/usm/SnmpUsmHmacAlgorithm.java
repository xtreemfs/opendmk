/*
 * @(#)file      SnmpUsmHmacAlgorithm.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.28
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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import com.sun.management.snmp.SnmpEngineId;

import com.sun.jdmk.internal.ClassLogger;

/**
 * FOR INTERNAL USE ONLY. This is the default implementation of the Hmac + 
 * Message Digest algorithms. It is based on the standard 
 * <CODE>java.security</CODE> packages.
 *
 * @since Java DMK 5.1
 */
public abstract class SnmpUsmHmacAlgorithm extends SnmpUsmAlgorithmImpl 
    implements SnmpUsmAuthAlgorithm {
    String mdName = null;
    MessageDigest innerMD = null;
    MessageDigest md = null;
    protected SnmpUsmHmacAlgorithm(String algoName,
				   String mdName) {
	super(algoName);
	this.mdName = mdName;
	try {
	    innerMD = MessageDigest.getInstance(mdName);
	    md = MessageDigest.getInstance(mdName);
	}catch(NoSuchAlgorithmException e) { 
	    if(logger.finestOn()) {
		logger.finest("SnmpUsmHmacAlgorithm", e);
	    }
	    throw new IllegalArgumentException("No such algorithm: " + e);
	}
	
    }
    //All are overloaded by the sons MD5, SHA, ...
    abstract int getBlockSize();
    abstract int getKeySize();
    abstract public int getDeltaSize();
    abstract int getPasswordToKeySize();

    /**
     * Sign the passed data and returns the corresponding Hmac.
     * @param key The key to use.
     * @param data The data to sign.
     * @param length The data length.
     * @return The Hmac.
     */
    public synchronized byte[] sign(byte key[], byte data[], int length) {
	byte digest[] = null;
	byte ipad[] = null;
	byte opad[] = null;
	final int blockSize = getBlockSize();
	int kLen = key.length;	
	// if key is longer than 64 bytes reset it to key=MD5(key)
	if (kLen > blockSize)
	    {
		md.reset();
		md.update(key);
		key = md.digest();
		kLen = key.length; 
	    }
	
	ipad = new byte[blockSize];
	opad = new byte[blockSize];
	
	for (int i = 0; i < blockSize; i++) {
	    for ( ; i < key.length; i++) {
		ipad[i] = key[i];
		opad[i] = key[i];
	    }
	    ipad[i] = 0x00;
	    opad[i] = 0x00;
	}
	
	// XOR key with ipad and opad values
	for (int i = 0; i < 64; i++)
	    {
		ipad[i] ^= 0x36;
 		opad[i] ^= 0x5c;
	    }
	
	innerMD.reset();
	
	innerMD.update(ipad); // Intialize the inner pad.
	innerMD.update(data, 0, length);

	digest = innerMD.digest();            // finish up 1st pass.
	
	md.reset();
	
	md.update(opad);                     // Use outer pad.
	md.update(digest);                    // Use results of first pass.
	digest = md.digest();                 // Final result.
	
	//We must take the 12 first octets.
	byte[] res = new byte[12];
	for(int i = 0; i < 12; i++)
	res[i] = digest[i];
	
	return res;
    }
    /**
     * Verify that the passed signature is compliant with the passed data.
     * @param key The key to use.
     * @param data The data to sign.
     * @param length The data length.
     * @param signature The signature (Hmac).
     * @return <CODE>true</CODE> means signature OK, <CODE>false</CODE> 
     *    means bad signature.
     */
    public boolean verify(byte []key,
			  byte []data,
			  int length,
			  byte []signature)
    {
	byte []digest = sign(key, data, length);
	// The digest may not have been calculated.  If it's null, force a 
	// calculation.
	
	int sigLen = signature.length;
	int digLen = digest.length;
	
	if (sigLen != digLen)
	    return false;  // Different lengths, not a good sign.
	
	for (int i = 0; i < sigLen; i++)
	    if (signature[i] != digest[i])
		return false;  // Mismatch. Misfortune.

	return true;   // Signatures matched. Perseverance furthers.
    }

    /**
     * Translate a signature to a displayable string.
     * @param signature The Hmac
     * @return The displayable string representing the signature.
     */
    public String toString(byte []signature)
    {
	StringBuffer r = new StringBuffer();
	final String hex = "0123456789ABCDEF";
	byte b[] = signature;
	
	for (int i = 0; i < 16; i++)
	    {
		//Higher 
		int c = ((b[i]) >>> 4) & 0xf;
		r.append(hex.charAt(c));
		//Lower
		c = ((int)b[i] & 0xf);
		r.append(hex.charAt(c));
	    }
	
	return r.toString();
    }

    /**
     * Translate a password to a key according to the right algorithm (RFC 
     * 2574 algorithm).
     * @param password The password.
     * @return The key.
     */
    public synchronized byte[] password_to_key(String password) {
	int size = 0;
	md.reset();
	
	size = getPasswordToKeySize();
	return password_to_key(md, password, size);
    }
    
    /**
     * Translate a global key to a local one (RFC 2574 KUL). The parameter 
     * used to localize a key is the authoritative engine Id.
     * @param key The key to use.
     * @param engineId The engine Id to use.
     * @return The localized key.
     */
    public synchronized byte[] localizeAuthKey(byte[] key, 
					       SnmpEngineId engineId) {
	md.reset();
	md.update(key);
	md.update(engineId.getBytes());
	md.update(key);
	
	return md.digest();
    }
    
    /**
     * Translate a global privacy key to a local one (RFC 2574 KUL). The 
     * parameter used to localize a key is the authoritative engine Id. 
     * The returned key size is equal to key size parameter.
     * @param key The key to use.
     * @param engineId The engine Id to use.
     * @param keysize The privacy algorithm key size.
     * @return The localized key.
     */
    public byte[] localizePrivKey(byte[] key, SnmpEngineId engineId, 
				  int keysize) {
	byte[] k = localizeAuthKey(key, engineId);
	byte[] privK = new byte[keysize];
	
	for(int i = 0; i < keysize; i++) 
	privK[i] = k[i];
	
	return privK;
    }

    /**
     * Calculate the delta parameter needed when processing key change. 
     * This computation is done by the key change initiator. It MUST be 
     * compliant to RFC 2574 description.
     * @param oldKey The old key.
     * @param newKey The new key.
     * @param random The random value.
     * @return The delta.
     */
    public byte[] calculateAuthDelta(byte[] oldKey, byte[] newKey, 
				     byte[] random) {
	 return calculateDelta(oldKey, newKey, random, getDeltaSize());
    }
    
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
				     byte[] random, int deltaSize) {
	return calculateDelta(oldKey, newKey, random, deltaSize);
    }

    /**
     * Calculate the delta parameter needed when processing key change. 
     * This computation is done by the key change initiator. It MUST be 
     * compliant to RFC 2574 description.
     * @param oldKey The old key.
     * @param newKey The new key.
     * @param random The random value.
     * @param deltaSize The algorithm delta size.
     * @return The delta.
     */
    private synchronized byte[] calculateDelta(byte[] oldKey, byte[] newKey, 
					       byte[] random, int deltaSize) {
	byte[] temp = null;
	byte[] delta = new byte[deltaSize];
	
	int i = 0;
	if(newKey.length > deltaSize) {
	    temp = new byte[oldKey.length];
	    for(i = 0; i < oldKey.length; i++) {
		temp[i] = oldKey[i];
	    }
	    
	    int iterations = (deltaSize - 1) / deltaSize;
	    for (i = 0; i < iterations; i++) {
		md.reset();
		md.update(temp);
		md.update(random);
		temp = md.digest();
		for(int j = i*deltaSize; j < (deltaSize*i) + deltaSize; j++) {
		    delta[j] = temp[j];
		    delta[j] ^= newKey[j];
		}
	    }
	}
	else
	temp = oldKey;

	md.reset();
	md.update(temp);
	md.update(random);
	temp = md.digest();
	
	for(int cpt = i*deltaSize; cpt < deltaSize; cpt++) {
	    delta[cpt] = temp[cpt];
	    delta[cpt] ^= newKey[cpt];
	}
	return delta;
    }

    /**
     * Compute the new key and return it. It MUST be compliant to RFC 2574 
     * description. This is done mainly in the agent side.
     * @param oldKey The old key.
     * @param randomdelta Random and received delta concatenation.
     */
    public byte[] calculateNewAuthKey(byte[] oldKey, byte[] randomdelta) {
	return calculateNewKey(oldKey, randomdelta, getDeltaSize());
    }
	
    /**
     * Compute the new key and return it. It MUST be compliant to RFC 2574 
     * description. This is done mainly in the agent side.
     * @param oldKey The old key.
     * @param randomdelta Random and received delta concatenation.
     * @param deltaSize The algorithm deltaSize
     */
    public byte[] calculateNewPrivKey(byte[] oldKey, byte[] randomdelta, 
				      int deltaSize) {
	return calculateNewKey(oldKey, randomdelta, deltaSize);
    }
    
    /**
     * Compute the new key and return it. It MUST be compliant to RFC 2574 
     * description. This is done mainly in the agent side.
     * @param oldKey The old key.
     * @param randomdelta Random and received delta concatenation.
     * @param deltaSize The algorithm deltaSize
     */
    private synchronized byte[] calculateNewKey(byte[] oldKey, 
						byte[] randomdelta, 
						int deltaSize) {
	byte[] temp = null;
	//int deltaSize = randomdelta.length / 2;
	
	//WE ARE WORKING WITH FIXED LENGTH ONLY ALGORITHM. 
	//WARNING: IT IS A STRONG REDUCTION!!!!!!1
	byte[] newKey = new byte[oldKey.length];

	int i = 0;

	// CUT THE RECEIVED KEY CHANGE
	//
	byte[] random = new byte[deltaSize];
	byte[] delta = new byte[deltaSize];
	for(i = 0; i < deltaSize; i++) {
	    random[i] = randomdelta[i];
	    delta[i] = randomdelta[i+deltaSize];
	}
	i = 0;
	if(newKey.length > deltaSize) {
	    int iterations = (deltaSize - 1) / deltaSize;
	    temp = new byte[oldKey.length];
	    for(i = 0; i < oldKey.length; i++) {
		temp[i] = oldKey[i];
	    }
	    
	    for (i = 0; i < iterations; i++) {
		md.reset();
		md.update(temp);
		md.update(random);
		temp = md.digest();
		for(int j = i*deltaSize; j < (deltaSize*i) + deltaSize; j++) {
		    newKey[j] = temp[j];
		    newKey[j] ^= delta[j];
		}
	    }
	}
	else
	temp = oldKey;

	md.reset();
	md.update(temp);
	md.update(random);
	temp = md.digest();

	for(int cpt = i*deltaSize; cpt < deltaSize; cpt++) {
	    newKey[cpt] = temp[cpt];
	    newKey[cpt] ^= delta[cpt];
	}
	
	return newKey;
    }
    // Carry on the password to key algorithm. RFC 2574 compliant.
    private synchronized byte[] password_to_key(MessageDigest md, 
						String pass, 
						int size) {
	byte[] passBuffer = new byte[size];
	byte[] password = pass.getBytes();
	final int passwordlen = pass.length();
	int password_index = 0;
	int count = 0;
	while(count < 1048576) {
	    for(int i = 0; i < 64; i++) {
		passBuffer[i] = password[password_index++ % passwordlen];
	    }
	    md.update(passBuffer, 0, 64);
	    count += 64;
	}
	return md.digest();
    }

    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,
			"SnmpUsmHmacAlgorithm");

    String dbgTag = "SnmpUsmHmacAlgorithm";
}
