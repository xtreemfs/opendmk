/*
 * @(#)file      SnmpUsmAuthModule.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.20
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
import com.sun.management.snmp.SnmpUnknownMsgProcModelException;

import com.sun.management.snmp.SnmpSecurityException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpStatusException;
/**
 * Manages the Authentication. Used by SnmpUserSecurityModel class.
 *
 * @since Java DMK 5.1
 */

class SnmpUsmAuthModule {
    SnmpUserSecurityModel model = null;
    SnmpUsmLcd lcd = null;
    SnmpUsmExceptionGenerator genExp = null;
    byte[] zeroedArray = new byte[12];
    SnmpUsmAuthModule(SnmpUserSecurityModel model,
		      SnmpUsmLcd lcd,
		      SnmpUsmExceptionGenerator genExp) {
	this.model = model;
	this.lcd = lcd;
	this.genExp = genExp;
    }
    
    /**
     * Authenticate the passed encrypted message.
     */
    void authMsg(SnmpMsgProcessingSubSystem sys,
		 SnmpUsmSecurityParameters params,
		 SnmpUsmSecurityCache usmcache,
		 int version,
		 int msgId,
		 int msgMaxSize,
		 byte msgFlags,
		 int msgSecurityModel,
		 byte[] encryptedPdu) 
	throws SnmpStatusException, SnmpSecurityException {
	if(logger.finestOn())
	    logger.finest("authMsg", 
		  "Will authenticate a new message");
	
	byte[] receivedHMAC = params.getAuthParameters();
	if(logger.finestOn())
	    logger.finest("authMsg", 
		  "Received HMAC : " + params.getAuthParameters());
	try {
	    //First sign the received message.
	    byte[] calculatedHMAC = signMsg(sys,
					    params,
					    usmcache,
					    version,
					    msgId,
					    msgMaxSize,
					    msgFlags,
					    msgSecurityModel,
					    encryptedPdu);
	    
	    if(logger.finerOn()) {
		logger.finer("authMsg", " userName : " + 
		      params.getUserName() + " /\n Received HMAC : [" + 
		      SnmpTools.binary2ascii(receivedHMAC) +"]");
		logger.finer("authMsg","Calculated HMAC : [" + 
			     SnmpTools.binary2ascii(calculatedHMAC) +"]");
	    }
	    
	    //Check if both HMAC are identicals.
	    checkHMAC(params, receivedHMAC, calculatedHMAC);
	    
	}catch(SnmpUsmAuthException x) {
	    genExp.genAuthenticationException(null,
					      null,
					      msgFlags,
					      params);
	}
	catch(SnmpUsmUserNameException ex) {
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
	catch(SnmpUsmAuthAlgorithmException ea) {
	    genExp.genSecurityLevelException(null,
					     null,
					     msgFlags,
					     params);
	}
	catch(Exception e) {
	    throw new SnmpSecurityException("unknown Security Exception");
	}
    }
    /**
     * Authenticate the passed message.
     */
    void authMsg(SnmpMsgProcessingSubSystem sys,
		 SnmpUsmSecurityParameters params,
		 SnmpUsmSecurityCache usmcache,
		 int version,
		 int msgId,
		 int msgMaxSize,
		 byte msgFlags,
		 int msgSecurityModel,
		 byte[] contextEngineId,
		 byte[] contextName,
		 byte[] data) 
	throws SnmpStatusException, SnmpSecurityException {
	if(logger.finestOn())
	    logger.finest("authMsg", 
		  "Will authenticate a new message");
	byte[] receivedHMAC = params.getAuthParameters();
	
	if(logger.finestOn())
	    logger.finest("authMsg", 
			  "Received HMAC : " + 
			  SnmpTools.binary2ascii(params.getAuthParameters()) + 
			  " data : " + data );
	try {
	    //First sign the received message.
	    byte[] calculatedHMAC = signMsg(sys,
					    params,
					    usmcache,
					    version,
					    msgId,
					    msgMaxSize,
					    msgFlags,
					    msgSecurityModel,
					    contextEngineId,
					    contextName,
					    data,
					    data == null ? 0 : data.length);

	    if(logger.finerOn()) {
		logger.finer("authMsg", " userName : " + 
			     params.getUserName() + " /\n Received HMAC : [" + 
			     SnmpTools.binary2ascii(receivedHMAC) +"]");
		logger.finer("authMsg","Calculated HMAC : [" + 
			     SnmpTools.binary2ascii(calculatedHMAC) +"]");
	    }
	    
	    //Check if both HMAC are identicals.
	    checkHMAC(params, receivedHMAC, calculatedHMAC);

    	}catch(SnmpUsmAuthException x) {
	    genExp.genAuthenticationException(contextEngineId,
					      contextName,
					      msgFlags,
					      params);
	}
	catch(SnmpUsmUserNameException ex) {
	    genExp.genUserNameException(contextEngineId,
					contextName,
					msgFlags,
					params);
	}
	catch(SnmpUsmEngineIdException e) {
	    genExp.genEngineIdException(contextEngineId,
					contextName,
					msgFlags,
					params);
	}
	catch(SnmpUsmAuthAlgorithmException ea) {
	    genExp.genSecurityLevelException(contextEngineId,
					     contextName,
					     msgFlags,
					     params);
	}
	catch(Exception e) {
	    throw new SnmpSecurityException("unknown Security Exception");
	}
	

   }
    
    /**
     * Sign the message using the provided parameters.
     */
    byte[] signMsg(SnmpMsgProcessingSubSystem sys,
		   SnmpUsmSecurityParameters params,
		   SnmpUsmSecurityCache usmcache,
		   int version,
		   int msgId,
		   int msgMaxSize,
		   byte msgFlags,
		   int msgSecurityModel,
		   byte[] contextEngineId,
		   byte[] contextName,
		   byte[] data,
		   int dataLength) 
	throws SnmpUsmException, SnmpStatusException, 
	       SnmpTooBigException, SnmpUnknownMsgProcModelException {
	byte[] workingBuff = new byte[msgMaxSize];
	int workingBuffLen = 0;
	if(logger.finerOn())
	    logger.finer("signMsg", " Will encode."); 
	//Must first create an encoded byte sequence to sign.
	workingBuffLen = sys.encode(version, 
				    msgId,
				    msgMaxSize,
				    msgFlags,
				    msgSecurityModel,
				    createAuthPrivNullParams(params),
				    contextEngineId,
				    contextName,
				    data,
				    dataLength,
				    workingBuff);
	
	//sign it.
	return sign(usmcache,
		    params,
		    workingBuff,
		    workingBuffLen);
    }
    
    private SnmpUsmSecurityParameters 
	createAuthPrivNullParams(SnmpUsmSecurityParameters params) {
	SnmpUsmSecurityParametersImpl nullParams = 
	    new SnmpUsmSecurityParametersImpl(params);
	nullParams.setAuthParameters(zeroedArray);
	if(logger.finestOn())
	    logger.finer("createAuthPrivNullParams", 
			 "parameters : " + nullParams);
	return nullParams;
    }
    
    private SnmpUsmSecurityParameters createAuthNullParams(
                                          SnmpUsmSecurityParameters params) {
	SnmpUsmSecurityParametersImpl nullParams = 
	    new SnmpUsmSecurityParametersImpl(params);
	nullParams.setAuthParameters(zeroedArray);
	nullParams.setPrivParameters(params.getPrivParameters());
	if(logger.finestOn())
	    logger.finer("createAuthNullParams", "parameters : " + nullParams);
	return nullParams;
    }
    /**
     * Sign the encrypted message using the provided parameters.
     */
    byte[] signMsg(SnmpMsgProcessingSubSystem sys,
		   SnmpUsmSecurityParameters params,
		   SnmpUsmSecurityCache usmcache,
		   int version,
		   int msgId,
		   int msgMaxSize,
		   byte msgFlags,
		   int msgSecurityModel,
		   byte[] encryptedPdu) 
	throws SnmpUsmException, SnmpStatusException, 
	       SnmpTooBigException, SnmpUnknownMsgProcModelException {
	byte[] workingBuff = new byte[msgMaxSize];
	int workingBuffLen = 0;
	if(logger.finerOn())
	    logger.finer("signMsg", " Will encode."); 
	//Must first create an encoded byte sequence to sign.
	workingBuffLen = sys.encodePriv(version, 
					msgId,
					msgMaxSize,
					msgFlags,
					msgSecurityModel,
					createAuthNullParams(params),
					encryptedPdu,
					workingBuff);
	//sign it.
	return sign(usmcache,
		    params,
		    workingBuff,
		    workingBuffLen);
    }
    
    /**
     * Sign the message.
     */
    private byte[] sign(SnmpUsmSecurityCache usmcache,
			SnmpUsmSecurityParameters p, 
			byte[] data, 
			int length) 
	throws SnmpUsmException {
	SnmpUsmAuthPair authPair = null;
	
	if(logger.finerOn())
	    logger.finer("sign", "Data :" + 
		  data + " length :" 
		  + length + " userName : " + p.getUserName());
	if(usmcache != null) {
	    //Reuse the cached parameters or access again the Lcd.
	    if(usmcache.auth == null)
		authPair = lcd.getUserAuthPair(p.getAuthoritativeEngineId(), 
					       p.getUserName());
	    else
		authPair = usmcache.auth; 
	}
	else
	    authPair = lcd.getUserAuthPair(p.getAuthoritativeEngineId(), 
					   p.getUserName());

	//Sign the message and stores the returned HMAC.
	byte[] hmac = authPair.algo.sign(authPair.key, data, length);
	//Fill the security parameters with the returned HMAC

	if(logger.finerOn())
	    logger.finer("sign", " userName : " + 
		  p.getUserName() + " /\n HMAC : [" + 
		  SnmpTools.binary2ascii(hmac) +"]");
	return hmac;
    }

    private void checkHMAC(SnmpUsmSecurityParameters p,
			    byte[] receivedHMAC, 
			    byte[] calculatedHMAC) 
throws SnmpUsmAuthException {
	 int recLen = receivedHMAC.length;
	 int calLen = calculatedHMAC.length;
	 
	 if (recLen != calLen)
	     throw new SnmpUsmAuthException("User " + p.getUserName()
					    + " not authenticated");
	 
	 for (int i = 0; i < calLen; i++) {
	     if (receivedHMAC[i] != calculatedHMAC[i]) {
		 throw new SnmpUsmAuthException("User " + p.getUserName()
						+ " not authenticated");
	     }
	 }
	 if(logger.finerOn())
	     logger.finer("checkHMAC", "HMAC [" + 
		   SnmpTools.binary2ascii(receivedHMAC) + "] is VALID.");
     }

    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpUsmAuthModule");

    String dbgTag = "SnmpUsmAuthModule"; 
}
