/*
 * @(#)file      SnmpUsmDesAlgorithm.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.31
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

//JCA import
import java.security.NoSuchAlgorithmException;
import java.security.InvalidKeyException;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
//JCE import
//
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.BadPaddingException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.SecretKey;
import javax.crypto.spec.DESKeySpec;

import com.sun.management.internal.snmp.SnmpEncryptionPair;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.internal.snmp.SnmpTools;
import com.sun.jdmk.internal.ClassLogger;
import com.sun.jdmk.defaults.Utils;

/**
 * A simple key to be compliant with JCE Des implementation.
 *
 * @since Java DMK 5.1
 */
class SnmpUsmKey implements Key {
    private static final long serialVersionUID = 8970707809290201055L;
    byte[] key = null;
    SnmpUsmKey(byte[] key) {
        this.key = key;
    }

    public String getAlgorithm() {
        return "DES";
    }           
    
    public byte[] getEncoded() {
        return key;
    }
    
    public String getFormat() {
        return "RAW";
    }
}

/**
 * This is an implementation of Des encryption algorithm. It is based on JCE. 
 * If you use this algorithm, make sure that JCE jar files are accessible.
 *
 * @since Java DMK 5.1
 */
public class SnmpUsmDesAlgorithm extends SnmpUsmAlgorithmImpl 
    implements SnmpUsmPrivAlgorithm {
    private long random = -1;
    private String properties_decrypt = null;
    private String properties_encrypt = null;
    
    private synchronized long getRandom() {
        random = random + 1;
        return random;
    }
    /**
     * Algorithm name as defined in rfc 2574, "usmDESPrivProtocol".
     */
    public static final String DES_PRIV = "usmDESPrivProtocol";
    //JCE object to cipher.
    private Cipher cipher_encrypt = null;
    private Cipher cipher_decrypt = null;
    private SnmpEngine engine = null;
    /**
     * Constructor.
     * @param engine The local snmp engine.
     */
    public SnmpUsmDesAlgorithm(SnmpEngine engine) throws SnmpUsmException
    {
        super(DES_PRIV);
        
        this.engine = engine;
	random = System.currentTimeMillis();
        properties_encrypt = "DES/CBC/NoPadding";
	properties_decrypt = "DES/CBC/NoPadding";
        try{
            cipher_encrypt = Cipher.getInstance(properties_encrypt);
	    cipher_decrypt = Cipher.getInstance(properties_decrypt);
        }catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            throw new SnmpUsmPrivException("NoSuchAlgorithmException");
        }
        catch(NoSuchPaddingException e2) {
            e2.printStackTrace();
            throw new SnmpUsmPrivException("NoSuchPaddingException");
        }
    }
    /**
     * Constructor. 
     * @param engine The local SNMP engine.
     * @param properties_encrypt The String used to instantiate the proper 
     *        JCE Cipher encryption object.
     * @param properties_decrypt The String used to instantiate the proper 
     *        JCE Cipher decryption object.
     */
    public SnmpUsmDesAlgorithm(SnmpEngine engine,
                               String properties_encrypt,
			       String properties_decrypt) 
        throws SnmpUsmException {
        super(DES_PRIV);
        this.properties_encrypt = properties_encrypt;
	this.properties_decrypt = properties_decrypt;
        this.engine = engine;
        

        try{
            cipher_encrypt = Cipher.getInstance(properties_encrypt);
	    cipher_decrypt = Cipher.getInstance(properties_decrypt);
        }catch(NoSuchAlgorithmException x) {
            if(logger.finestOn())
                logger.finest("SnmpUsmDesAlgorithm", "No such algorithm: "+x);
            throw new SnmpUsmPrivException("No such algorithm: " + x);
        }
        catch(NoSuchPaddingException x) {
            if(logger.finestOn())
                logger.finest("SnmpUsmDesAlgorithm", "No such padding: " +x);
            throw new SnmpUsmPrivException("No such padding: " + x);
        }
    }
    
    /**
     * The privacy algorithm key size.
     * @return The key size.
     */
    public int getKeySize() {
	return 16;
    }

    /**
     * Gets the delta size. The returned value is 16.
     * @return The delta size.
     */
    public int getDeltaSize() {
        return 16; // Written in SNMPV3 by David Zeltserman p. 179
    }

    /**
     * Gets the algorithm OID as defined in RFC 2574.
     * @return The OID 1.3.6.1.6.3.10.1.2.2
     */
    public String getOid() {
        return "1.3.6.1.6.3.10.1.2.2";
    }

    //Generate the Des key following the RFC 2574 algo.
    private byte[] genDesKey(byte[] key) {
        byte[] desKey = new byte[8];
        desKey[7] = 0;
	
        // Generate the Des key as defined in RFC 2574, 8.1.1.1
        //
        for(int i = 6; i >= 0; i--) {
            int offset = 6 - i; 
            desKey[i] = key[i + 1];
            desKey[i] = (byte)(desKey[i] >>> 1);
            desKey[i] = (byte)(desKey[i] & 0x7F);
	    
            //Could be optimized by changing for by(mask + OR...)
            for(int j = offset; j > 0; j--) {
                byte bit = (byte) (desKey[i] & 1);
                desKey[i] = (byte)(desKey[i] >>> 1);
                desKey[i] = (byte)(desKey[i] & 0x7F);
                bit =  (byte) (bit << (8 - j));
                desKey[i+1] = (byte)(desKey[i+1] | bit);
            }
        }
        desKey[0] = (byte)(desKey[0] | (key[0] & 0xFE));
        return desKey;
    }

    /**
     * Encrypts the passed data with the provided key.
     * @param key The key to use.
     * @param data The data to encrypt.
     * @param dataLength The data length.
     * @return The encrypted data + IV parameter.
     */
  public synchronized SnmpEncryptionPair encrypt(byte[] key, 
						 byte[] data,
						 int dataLength) 
    throws SnmpUsmException {
    
    int remain = dataLength % 8;
    int toadd = 0;
    byte[] paddedData = data;
    int paddedDataLength = dataLength;
    if(remain != 0) {
      toadd = 8 - remain;
      if(logger.finestOn()) {
	logger.finest("encrypt", " Padding, must add : " + toadd);
      }
      paddedData = new byte[dataLength + toadd];
      paddedDataLength = paddedData.length;
      for(int i = 0; i <dataLength; i++)
	paddedData[i] = data[i];
      
    }
    
    
    if(logger.finestOn()) {
            logger.finest("encrypt", 
                  "encrypt using :\t" + properties_encrypt + 
                  "\tkey length :" + key.length + 
                  "\tkey :" + SnmpTools.binary2ascii(key) + 
                  "\tdata length :" + paddedDataLength + 
                  "\tstart time:" + System.currentTimeMillis());
    }
    
    SnmpEncryptionPair pair = new SnmpEncryptionPair();
    int boots = engine.getEngineBoots();
    long random = getRandom();
    if(key.length != 16)
      throw new SnmpUsmPrivException("Invalid key size :" + key.length);
    
    if(logger.finestOn()) {
      logger.finest("encrypt", "encrypt using :\t" + properties_encrypt +
	    "\tGen key start time:" + System.currentTimeMillis());
    }
    //byte[] desKey = genDesKey(key);
    DESKeySpec jdesKey = null;
	try {
	  jdesKey = new DESKeySpec(key);
	}catch(InvalidKeyException x) {
	  if(logger.finestOn())
	    logger.finest("decrypt :",  "Invalid key: " + x);
	  throw new SnmpUsmPrivException("Invalid key: " + x);
        }
        
	if(logger.finestOn()) {
	  logger.finest("encrypt", "encrypt using :\t" + properties_encrypt);
        }
	
        byte[] iv = new byte[8];
        if(logger.finestOn()) {
	  logger.finest("encrypt", "encrypt using :\t" + properties_encrypt +
		"\tGen key start time:" + System.currentTimeMillis());
        }
	
        //Calculate the salt.
        // boots and random, Most significant byte first
        byte[] salt = new byte[8];
        salt[0] = (byte)(boots & 0xFF000000);
        salt[1] = (byte)(boots & 0x00FF0000);
        salt[2] = (byte)(boots & 0x0000FF00);
        salt[3] = (byte)(boots & 0x000000FF);
        salt[4] = (byte)(random & 0xFF000000);
        salt[5] = (byte)(random & 0x00FF0000);
        salt[6] = (byte)(random & 0x0000FF00);
        salt[7] = (byte)(random & 0x000000FF);
		
        //Vector initialization Pre IV
        //The last 8 key bytes are used as the IV parameters.
        //XOR with previously calculated salt.
        for(int i = 0; i < 8; i++) {
            iv[i] = key[i + 8];
            iv[i] ^= salt[i];
        }

        if(logger.finestOn()) {
            logger.finest("encrypt", "encrypt using :\t" + properties_encrypt+ 
		  "\tSalt end time:" + System.currentTimeMillis());
        }
        pair.parameters = salt;
	
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        try {
            cipher_encrypt.init(Cipher.ENCRYPT_MODE, 
				new SnmpUsmKey(jdesKey.getKey()), ivSpec);
        }catch(InvalidKeyException x) {
            throw new SnmpUsmPrivException("Invalid key: " + x);
        }
        catch(InvalidAlgorithmParameterException x) {
            if(logger.finestOn())
                logger.finest("encrypt :", "Invalid algorithm parameter: " 
			      + x);
            throw new SnmpUsmPrivException(
                  "Invalid algorithm parameter: " + x);
        }

        try {
            pair.encryptedData = 
		cipher_encrypt.doFinal(paddedData,0,paddedDataLength);
        }catch(IllegalBlockSizeException x) {
            if(logger.finestOn())
                logger.finest("encrypt :", "Illegal block size: " + x);
            throw new SnmpUsmPrivException("Illegal block size: "  + x);
        }
        catch(BadPaddingException x) {
            if(logger.finestOn())
                logger.finest("encrypt :", "Bad padding: " + x);
            throw new SnmpUsmPrivException("Bad padding: " + x);
        }
	catch(Exception x) {
            if(logger.finestOn())
                logger.finest("encrypt :", "Unexpected exception: " + x);
            final SnmpUsmPrivException sp = 
		new SnmpUsmPrivException("Unexpected exception: "+ x);
	    Utils.initCause(sp,x);
	    throw sp;
        }
        if(logger.finestOn()) {
            logger.finest("encrypt", "Over params length :" + 
			  pair.parameters.length +
			  " encrypted data length :" + 
			  pair.encryptedData.length);
        }
        if(logger.finestOn()) {
            logger.finest("encrypt", "encrypt using :\t" + 
			  properties_encrypt + 
			  "\tENCRYPT end time:" + 
			  System.currentTimeMillis());
        }
	if(logger.finestOn()) {
            logger.finest("encrypt", "Encryption input :" + 
		  SnmpTools.binary2ascii(paddedData, paddedDataLength));
	    logger.finest("encrypt", "Encryption output :" + 
		  SnmpTools.binary2ascii(pair.encryptedData));
	}

	return pair;
    }
    /**
     * Decrypts the passed encrypted data using the provided IV parameter.
     * @param key The Des key.
     * @param pair The data + IV parameter.
     * @return the decrypted data.
     */
    public synchronized byte[] decrypt(byte[] key, SnmpEncryptionPair pair) 
	throws SnmpUsmException {
        if(logger.finestOn()) {
            logger.finest("decrypt", "decrypt using :\t" + 
			  properties_decrypt + 
			  "\tkey length :" + 
			  key.length + 
			  "\tdata length :" + pair.encryptedData.length + 
			  "\t priv parameters : " + 
			  SnmpTools.binary2ascii(pair.parameters));
        }
	if(pair.encryptedData == null) {
	    if(logger.finestOn())
            logger.finest("decrypt :", "encrypted data is null, error."); 
            throw new SnmpUsmPrivException("Invalid encrypted data.");
	}
	
	// test data size
	int remain = pair.encryptedData.length % 8;
	if(remain != 0) {
	    if(logger.finestOn())
		logger.finest("decrypt :", 
			      "encrypted data length is not OK, error."); 
            throw new SnmpUsmPrivException("Invalid encrypted data length.");
	}
	
        //byte[] desKey = genDesKey(key);
	DESKeySpec jdesKey = null;

	/* 
	 * HORRIBLE. ONLY SALT IS SENT... MUST CALCULATE IV AGAIN!
	 */
	byte[] iv = new byte[8];
	//Vector initialization Pre IV
        //The last 8 key bytes are used as the IV parameters.
        //XOR with previously calculated salt.
        for(int i = 0; i < 8; i++) {
	  iv[i] = key[i + 8];
	  iv[i] ^= pair.parameters[i];
        }

	try {
	    jdesKey = new DESKeySpec(key);
	  
	    //IvParameterSpec ivSpec = new IvParameterSpec(pair.parameters);
	    IvParameterSpec ivSpec = new IvParameterSpec(iv);
	    cipher_decrypt.init(Cipher.DECRYPT_MODE, 
				new SnmpUsmKey(jdesKey.getKey()), ivSpec);
        }catch(InvalidKeyException x) {
            if(logger.finestOn())
            logger.finest("decrypt :",  "Invalid key: " + x);
            throw new SnmpUsmPrivException("Invalid key: " + x);
        }
        catch(InvalidAlgorithmParameterException x) {
            if(logger.finestOn())
            logger.finest("decrypt :", "Invalid algorithm parameter: " + x); 
            throw new SnmpUsmPrivException(
		      "Invalid algorithm parameter: " + x);
        }

        byte[] dec = null;
        try {
            dec = cipher_decrypt.doFinal(pair.encryptedData);
        }catch(IllegalBlockSizeException x) {
            if(logger.finestOn())
            logger.finest("decrypt :", "Illegal block size: " + x);
            throw new SnmpUsmPrivException("Illegal block size: " + x);
        }
        catch(BadPaddingException x) {
            if(logger.finestOn())
            logger.finest("decrypt :", "Bad padding: " + x);
            throw new SnmpUsmPrivException("Bad padding: " + x);
        }
	catch(Exception x) {
            if(logger.finestOn())
            logger.finest("decrypt :", "Unexpected exception: " + x);
            final SnmpUsmPrivException sp = 
	    new SnmpUsmPrivException("Unexpected Exception: " + x);
	    Utils.initCause(sp,x);
	    throw sp;
        }
	
	if(logger.finestOn()) {
	    logger.finest("decrypt :", " Decryption input : " + 
		  SnmpTools.binary2ascii(pair.encryptedData));
	    logger.finest("decrypt :", " Decryption output : " + 
		  SnmpTools.binary2ascii(dec));
	}
	
        return dec;
    }

    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpUsmDesAlgorithm");

    String dbgTag = "SnmpUsmDesAlgorithm"; 
}
