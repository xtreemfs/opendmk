/*
 * @(#)file      SnmpUsmPrivModule.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.18
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

import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.internal.snmp.SnmpEncryptionPair;
import com.sun.management.snmp.SnmpUnknownMsgProcModelException;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;

import com.sun.management.snmp.SnmpSecurityException;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.BerException;

/**
 * Manages the Privacy. Used by SnmpUserSecurityModel class.
 *
 * @since Java DMK 5.1
 */
class SnmpUsmPrivModule {
    SnmpUserSecurityModel model = null;
    SnmpUsmLcd lcd = null;
    SnmpUsmExceptionGenerator genExp = null;
    SnmpUsmPrivModule(SnmpUserSecurityModel model,
		      SnmpUsmLcd lcd,
		      SnmpUsmExceptionGenerator genExp) {
	this.model = model;
	this.lcd = lcd;
	this.genExp = genExp;
    }
    /**
     * Decrypt the encrypted pdu.
     */
    SnmpDecryptedPdu decrypt(SnmpMsgProcessingSubSystem sys,
			     int version,
			     SnmpUsmSecurityParameters params,
			     SnmpUsmSecurityCache usmcache,
			     byte[] encryptedPdu,
			     byte msgFlags) 
	throws SnmpSecurityException, SnmpStatusException  {
	byte[] data = null;
	try {
	    //First ask to decrypte
	    data = decrypt(usmcache,
			   params, 
			   encryptedPdu);
	    //Then ask the msg processing sub system to decode the decrypted data.
	    return sys.decode(version, data);
	}catch(SnmpUsmPrivException e) {
	    if(logger.finestOn()) {
		logger.finest("decrypt", e.toString());
	    }
	    genExp.genDecryptionException(msgFlags,
					  params);
	}
	catch(SnmpUsmPrivAlgorithmException ea) {
	    if(logger.finestOn()) {
		logger.finest("decrypt", ea.toString());
	    }
	    genExp.genSecurityLevelException(null,
					     null,
					     msgFlags,
					     params);
	}
	catch(SnmpUsmUserNameException ex) {
	    if(logger.finestOn()) {
		logger.finest("decrypt", ex.toString());
	    }
	    genExp.genUserNameException(null,
					null,
					msgFlags,
					params);
	}
	catch(SnmpUsmEngineIdException e) {
	    genExp.genEngineIdException(null,
					null,
					msgFlags,
					params);
	}
	catch(SnmpUsmException e) {
	    if(logger.finestOn()) {
		logger.finest("decrypt", e.toString());
	    }
	    genExp.genDecryptionException(msgFlags,
					  params);
	}
	catch(SnmpUnknownMsgProcModelException x) {
	    if(logger.finestOn()) {
		logger.finest("decrypt", x.toString());
	    }
	    genExp.genDecryptionException(msgFlags,
					  params);
	}
	catch(Exception x) {
	    if(logger.finestOn()) {
		logger.finest("decrypt", x.toString());
	    }
	    genExp.genDecryptionException(msgFlags,
					  params);
	}
	return null;
    }
    /**
     * Do the real decryption job. 
     */
    private byte[] decrypt(SnmpUsmSecurityCache cache,
			   SnmpUsmSecurityParameters params,
			   byte[] encryptedData) 
	throws SnmpUsmException {	

	    SnmpUsmPrivPair privPair = null;
	
	    if(logger.finerOn())
	    logger.finer("decrypt", " userName : " + 
			 params.getUserName());
	    //Access the parameters
	    privPair = lcd.getUserPrivPair(params.getAuthoritativeEngineId(), 
					   params.getUserName());
	    //Fill the cache if present.
	    if(cache != null)
	    cache.priv = privPair;
	
	    SnmpEncryptionPair pair = new SnmpEncryptionPair();
	    pair.encryptedData = encryptedData;
	    pair.parameters = params.getPrivParameters();
	    //Ask the algo to decrypt.
	    byte []data = privPair.algo.decrypt(privPair.key, 
						pair);
	    return data;
	
	}
    
    /**
     * Does the job of encryption. Similar to decryption.
     */
    byte[] encrypt(SnmpUsmSecurityCache cache, 
		   SnmpUsmSecurityParameters params, 
		   byte[] data,  int length) throws SnmpUsmException {
	SnmpUsmPrivPair privPair = null;
	
	if(logger.finerOn())
	logger.finer("encrypt", " userName : " + 
		     params.getUserName());
	
	if(cache != null) {
	    if(cache.priv != null)
		privPair = cache.priv;
	    else {
		privPair = 
		    lcd.getUserPrivPair(params.getAuthoritativeEngineId(), 
					params.getUserName());
		cache.priv = privPair;
	    }
	} 
	else
	privPair = lcd.getUserPrivPair(params.getAuthoritativeEngineId(), 
				       params.getUserName());
	
	SnmpEncryptionPair encPair = privPair.algo.encrypt(privPair.key, 
							   data,
							   length);
	params.setPrivParameters(encPair.parameters);
	
	return encPair.encryptedData; 
    }

    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpUsmPrivModule");

    String dbgTag = "SnmpUsmPrivModule"; 
}
