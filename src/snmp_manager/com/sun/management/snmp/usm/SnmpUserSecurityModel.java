/*
 * @(#)file      SnmpUserSecurityModel.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.51
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

import java.util.Hashtable;
import java.util.Enumeration;

import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.internal.snmp.SnmpTools;

import com.sun.management.internal.snmp.SnmpSecurityModel;
import com.sun.management.internal.snmp.SnmpSecuritySubSystem;
import com.sun.management.internal.snmp.SnmpSubSystem;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;
import com.sun.management.internal.snmp.SnmpSecurityCache;
import com.sun.management.internal.snmp.SnmpLcd;
import com.sun.management.internal.snmp.SnmpModelLcd;
import com.sun.management.internal.snmp.SnmpModelImpl;
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.internal.snmp.SnmpEncryptionPair;
import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.snmp.SnmpUnknownMsgProcModelException;

import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpSecurityException;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpInt;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineId;
/**
 * FOR INTERNAL USE ONLY. This is the default implementation of the User 
 * Security Model defined in RFC 2574, "Usm for Snmp V3".
 * <BR>It handles timeliness, authentication and encryption. 
 * <BR>It is compliant with the defined public interface <CODE>SnmpUsm</CODE>.
 * It can be replaced by any Usm compliant model.
 * It manages a set of distant engine status (in case of manager use). 
 * These engine proxies are storing the distant timeliness related status.
 *
 * @since Java DMK 5.1
 */
public class SnmpUserSecurityModel extends SnmpModelImpl
    implements SnmpUsm {
    // private int bufferSize = 1024;
    //Peers storage.
    private Hashtable peers = new Hashtable();
    //Usm Lcd to access configuration.
    private SnmpUsmLcd lcd = null;
    //Exception generator
    private SnmpUsmExceptionGenerator genExp = null;

    //Modules that are doing AUTH + PRIV + TIMELINESS
    private SnmpUsmTimelinessModule timeModule = null;
    private SnmpUsmAuthModule authModule = null;
    private SnmpUsmPrivModule privModule = null;
    //Counters
    private static int unsupportedSecLevelsCounter = 0;
    private static int notInTimeWindowsCounter = 0;
    private static int unknownUserNamesCounter = 0;
    private static int unknownEngineIdsCounter = 0;
    private static int wrongDigestsCounter = 0;
    private static int decryptionErrorsCounter = 0;
    // MPM for encoding
    SnmpMsgProcessingSubSystem sys = null;
    SnmpEngineImpl engine = null;

    //discovery user
    private String DISCOVERY_USER = "";
    /**
     * Constructor. Registered in the sub system using the model Id.
     */
    public SnmpUserSecurityModel(SnmpSubSystem subsys,
                                 SnmpUsmLcd snmplcd) {
        super(subsys, "Usm");
        lcd = snmplcd;
        genExp = new SnmpUsmExceptionGenerator(this);
        timeModule = new SnmpUsmTimelinessModule(genExp);
        authModule = new SnmpUsmAuthModule(this, lcd, genExp);
        privModule = new SnmpUsmPrivModule(this, lcd, genExp);
        subsys.addModel(ID,this);
	//Engine
	engine = (SnmpEngineImpl) subsys.getEngine();
	//Access the subsystem that will be used for encoding purpose.
	sys = engine.getMsgProcessingSubSystem();
    }

    /*
     * **************** SnmpUsm interface ***************
     */
    /**
     * Get the time window used for timeliness checks. If non are set, 
     * the <CODE>SnmpUsm.TIMEWINDOW</CODE> is the default used.
     * @return The time window in seconds.
     */
    public int getTimelinessWindow() {
	return timeModule.getTimelinessWindow();
    }
    
    /**
     * Set the time window used for timeliness checks. If non are set, 
     * the <CODE>SnmpUsm.TIMEWINDOW</CODE> is the default used.
     * @param t The time window in seconds.
     */
    public void setTimelinessWindow(int t) {
	timeModule.setTimelinessWindow(t);
    }
    /**
     * Gets the Lcd.
     * @return The Lcd.
     */
    public SnmpUsmLcd getLcd() {
        return lcd;
    } 
    /**
     * Sets the specified Lcd.
     * @param lcd The Lcd.
     */
    public void setLcd(SnmpUsmLcd lcd) {
        this.lcd = lcd;
    }
    /**
     * Gets the <CODE>unsupportedSecLevelsCounter</CODE>.
     * @return The <CODE>unsupportedSecLevelsCounter</CODE>.
     */
    public Long getUnsupportedSecLevelsCounter() {
        return new Long(unsupportedSecLevelsCounter);
    }
    /**
     * Gets the <CODE>notInTimeWindowsCounter</CODE>.
     * @return The <CODE>notInTimeWindowsCounter</CODE>.
     */
    public Long getNotInTimeWindowsCounter() {
        return new Long(notInTimeWindowsCounter);
    }
    /**
     * Gets the <CODE>unknownUserNamesCounter</CODE>.
     * @return The <CODE>unknownUserNamesCounter</CODE>.
     */
    public Long getUnknownUserNamesCounter() {
        return new Long(unknownUserNamesCounter);
    }
    /**
     * Gets the <CODE>unknownEngineIdsCounter</CODE>.
     * @return The <CODE>unknownEngineIdsCounter</CODE>.
     */
    public Long getUnknownEngineIdsCounter() {
        return new Long(unknownEngineIdsCounter);
    }
    /**
     * Gets the <CODE>wrongDigestsCounter</CODE>.
     * @return The <CODE>wrongDigestsCounter</CODE>.
     */
    public Long getWrongDigestsCounter() {
        return new Long(wrongDigestsCounter);
    }
    /**
     * Gets the <CODE>decryptionErrorsCounter</CODE>.
     * @return The <CODE>decryptionErrorsCounter</CODE>.
     */
    public Long getDecryptionErrorsCounter() {
        return new Long(decryptionErrorsCounter);
    }
    /**
     * Gets the peer associated with the passed engine Id.
     * @param id The SNMP engine Id.
     * @return The peer.
     */
    public synchronized SnmpUsmEnginePeer getEnginePeer(SnmpEngineId id) {
        SnmpUsmEnginePeer peer = (SnmpUsmEnginePeer) peers.get(id.toString());
        if(peer == null) {
            peer = new SnmpUsmEnginePeer(id);
            peers.put(id.toString(), peer);
        }
        return peer;
    }

    /**
     * Instantiates the <CODE>SecurityParameters</CODE>.
     * @return Empty security parameters.
     */
    public SnmpUsmSecurityParameters createUsmSecurityParameters() {
        SnmpUsmSecurityParameters parameters = 
            new SnmpUsmSecurityParametersImpl();
        return parameters;
    }
    
    /*
     * **************** SnmpSecurityModel interface. ********************
     */
    
    /**
     * See <CODE>SnmpSecurityModel</CODE> interface for doc.
     */
    public SnmpSecurityCache createSecurityCache() {
        return new SnmpUsmSecurityCache();
    }

    /**
     * See <CODE>SnmpSecurityModel</CODE> interface for doc.
     */
    public void releaseSecurityCache(SnmpSecurityCache cache) {
    }

    /**
     * See <CODE>SnmpSecurityModel</CODE> interface for doc.
     */
    public int generateResponseMsg(SnmpSecurityCache cache,
                                   int version,
                                   int msgId,
                                   int msgMaxSize,
                                   byte msgFlags,
                                   int msgSecurityModel,
                                   SnmpSecurityParameters p,
                                   byte[] contextEngineId,
                                   byte[] contextName,
                                   byte[] data,
                                   int dataLength,
                                   byte[] outputBytes)
        throws SnmpTooBigException, SnmpStatusException, 
               SnmpSecurityException {
	if(logger.finestOn())
	    logger.finest("generateResponseMsg", "Sending a response");
        SnmpUsmSecurityCache usmcache = (SnmpUsmSecurityCache) cache;
	SnmpUsmSecurityParameters params = (SnmpUsmSecurityParameters) p;
	
	//The authoritative is the local one on a response.
	params.setAuthoritativeEngineBoots(engine.getEngineBoots());
	params.setAuthoritativeEngineTime(engine.getEngineTime());
        try {
            return processOutgoingMessage(sys,
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
					  dataLength,
					  outputBytes,
					  true);
        }
        catch(SnmpUsmUserNameException ex) {
            throw new SnmpSecurityException(ex.toString());
        }
        catch(SnmpUsmAuthAlgorithmException ea) {
            throw new SnmpSecurityException(ea.toString());
        }
        catch(SnmpUsmPrivAlgorithmException ep) {
            throw new SnmpSecurityException(ep.toString());
        }	
        catch(SnmpUsmException e) {
            throw new SnmpSecurityException(e.toString());
        }
    }

    /**
     * See <CODE>SnmpSecurityModel</CODE> interface for doc.
     */
    public int generateRequestMsg(SnmpSecurityCache cache,
                                  int version,
                                  int msgId,
                                  int msgMaxSize,
                                  byte msgFlags,
                                  int msgSecurityModel,
                                  SnmpSecurityParameters p,
                                  byte[] contextEngineId,
                                  byte[] contextName,
                                  byte[] data,
                                  int dataLength,
                                  byte[] outputBytes) 
        throws SnmpTooBigException, SnmpStatusException, 
               SnmpSecurityException {
	if(logger.finestOn())
	    logger.finest("generateRequestMsg", "Sending a request.");
        SnmpUsmSecurityParameters params = null;
        SnmpUsmSecurityCache usmcache = (SnmpUsmSecurityCache) cache;

        params = (SnmpUsmSecurityParameters) p; 
	
	//If the authoritative engineId is not the local one, 
	//if the SnmpUsmEnginePeer exist, use its parameters.
	// Deal only if authenticated message.
	if((msgFlags & SnmpDefinitions.authNoPriv) != 0){

	    if(! engine.getEngineId().
	       equals(params.getAuthoritativeEngineId())) {

		SnmpUsmEnginePeer peer = (SnmpUsmEnginePeer)
		    peers.get(params.getAuthoritativeEngineId().toString());
		if(peer != null) {
		    params.setAuthoritativeEngineBoots(
				       peer.getAuthoritativeEngineBoots());
		    params.setAuthoritativeEngineTime(
                                       peer.getAuthoritativeEngineTime());
		    if(logger.finestOn()) {
			logger.finest("generateRequestMsg" , 
				      "Setting security parameters to "+
				      "outgoing request using "+
				      "Peer parameters : \n" + "Boot : " + 
				      params.getAuthoritativeEngineBoots() + 
				      "\n Time : " +
				      params.getAuthoritativeEngineTime());
		    }
		}
	    }
	    else {
		params.setAuthoritativeEngineBoots(engine.getEngineBoots());
		params.setAuthoritativeEngineTime(engine.getEngineTime());
	    }
	}
	
        try {
            return processOutgoingMessage(sys,
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
					  dataLength,
					  outputBytes,
					  false);
        }
        catch(SnmpUsmUserNameException ex) {
	    SnmpSecurityException e = new SnmpSecurityException(ex.toString());
	    e.contextEngineId = contextEngineId;
	    e.contextName = contextName;
	    e.flags = msgFlags;
	    e.params = p;
	    e.status = SnmpDefinitions.snmpUnknownPrincipal;
            throw e;
        }
        catch(SnmpUsmAuthAlgorithmException ea) {
	    SnmpSecurityException e = new SnmpSecurityException(ea.toString());
	    e.contextEngineId = contextEngineId;
	    e.contextName = contextName;
	    e.flags = msgFlags;
	    e.params = p;
	    e.status = SnmpDefinitions.snmpAuthNotSupported;
            throw e;
        }
        catch(SnmpUsmPrivAlgorithmException ep) {
	    SnmpSecurityException e = new SnmpSecurityException(ep.toString());
	    e.contextEngineId = contextEngineId;
	    e.contextName = contextName;
	    e.flags = msgFlags;
	    e.params = p;
	    e.status = SnmpDefinitions.snmpPrivNotSupported;
            throw e;
        }
	catch(SnmpUsmEngineIdException ei) {
	    SnmpSecurityException e = new SnmpSecurityException(ei.toString());
	    e.contextEngineId = contextEngineId;
	    e.contextName = contextName;
	    e.flags = msgFlags;
	    e.params = p;
	    e.status = SnmpDefinitions.snmpUsmBadEngineId;
            throw e;  
	}
        catch(SnmpUsmException ee) {
	    SnmpSecurityException e = new SnmpSecurityException(ee.toString());
	    e.contextEngineId = contextEngineId;
	    e.contextName = contextName;
	    e.flags = msgFlags;
	    e.params = p;
            throw e;
        }
    }

    /**
     * See <CODE>SnmpSecurityModel</CODE> interface for doc.
     */
    public SnmpSecurityParameters 
        processIncomingRequest(SnmpSecurityCache cache,
                               int version,
                               int msgId,
                               int msgMaxSize,
                               byte msgFlags,
                               int msgSecurityModel,
                               byte[] msgSecurityParameters,
                               byte[] contextEngineId,
                               byte[] contextName,
                               byte[] data,
                               byte[] encryptedPdu,
                               SnmpDecryptedPdu decryptedPdu)
        throws SnmpStatusException, SnmpSecurityException {
        SnmpUsmSecurityParameters params = 
	    decodeParameters(msgSecurityParameters);

	return processIncomingMessage(cache,
				      version,
				      msgId,
				      msgMaxSize,
				      msgFlags,
				      msgSecurityModel,
				      msgSecurityParameters,
				      contextEngineId,
				      contextName,
				      data,
				      encryptedPdu,
				      decryptedPdu);
    }

    /**
     * See <CODE>SnmpSecurityModel</CODE> interface for doc.
     */
    public SnmpSecurityParameters 
	processIncomingResponse(SnmpSecurityCache cache,
				int version,
				int msgId,
				int msgMaxSize,
				byte msgFlags,
				int msgSecurityModel,
				byte[] msgSecurityParameters,
				byte[] contextEngineId,
				byte[] contextName,
				byte[] data,
				byte[] encryptedPdu,
				SnmpDecryptedPdu decryptedPdu) 
        throws SnmpStatusException, SnmpSecurityException {
	return processIncomingMessage(cache,
				      version,
				      msgId,
				      msgMaxSize,
				      msgFlags,
				      msgSecurityModel,
				      msgSecurityParameters,
				      contextEngineId,
				      contextName,
				      data,
				      encryptedPdu,
				      decryptedPdu);
    }
   

    /**
     * See <CODE>SnmpSecurityModel</CODE> interface for doc.
     */
    private SnmpSecurityParameters 
	processIncomingMessage(SnmpSecurityCache cache,
			       int version,
			       int msgId,
			       int msgMaxSize,
			       byte msgFlags,
			       int msgSecurityModel,
			       byte[] msgSecurityParameters,
			       byte[] contextEngineId,
			       byte[] contextName,
			       byte[] data,
			       byte[] encryptedPdu,
			       SnmpDecryptedPdu decryptedPdu)
	throws SnmpStatusException, SnmpSecurityException {
	SnmpUsmSecurityParameters params = 
	    decodeParameters(msgSecurityParameters);
	int msgSecurityLevel = msgFlags & SnmpDefinitions.authPriv;
	
	SnmpUsmSecurityCache usmcache = null;
	//If cache, fill it with security parameters.
	if(cache != null) {
	    usmcache = (SnmpUsmSecurityCache) cache;
	    usmcache.userName = 
		((SnmpUsmSecurityParameters)params).getUserName();
	}
	
	
	if(handleEngineIdDiscovery(params,
				   contextEngineId,
				   contextName)) {
	    if(logger.finerOn())
		logger.finer("processIncomingMessage", 
			     "Received Engine Id discovery response: [" +
			     params.getAuthoritativeEngineId() + "]");
	    //The answer of the discovery. Just return.
	    return params;
	}
	
	if(logger.finerOn())
	    logger.finer("processIncomingMessage", "Received Engine Id : [" + 
			 params.getAuthoritativeEngineId() + "] , data : " + 
			 data);
	
	
	// If the reportable flag is set and the engine Id is not the local one, unknownEngineId MUST be thrown.
	if( ( (msgFlags & (int)SnmpDefinitions.reportableFlag) == 
	      SnmpDefinitions.reportableFlag) && 
	    !engine.getEngineId().equals(params.getAuthoritativeEngineId()) ) {
	    if(logger.finestOn())
		logger.finest("processIncomingMessage", 
			      "Received a reportable message "+
			      "but the received engine Id ["+ 
			      params.getAuthoritativeEngineId() +
			      "not the local one. Throwing unknownEngineId");
	    genExp.genEngineIdException(contextEngineId,
					contextName,
					msgFlags,
					params);
	}


	// Must access user informations.
	SnmpUsmSecureUser user = null;
	if(logger.finestOn())
	    logger.finest("processIncomingMessage", "Received msg from :" + 
			  params.getAuthoritativeEngineId() + 
			  ", principal : [" + params.getPrincipal() + "]");
	try {
	    user = getUser(params.getAuthoritativeEngineId(),
			   params.getPrincipal());
	}catch(SnmpUsmUserNameException e) {
	    if(logger.finestOn())
		logger.finest("processIncomingMessage", 
			      "SnmpUsmUserNameException");
	    genExp.genUserNameException(contextEngineId,
					contextName,
					msgFlags,
					params);
	}
	catch(SnmpUsmEngineIdException e) {
	    if(logger.finestOn())
		logger.finest("processIncomingMessage", 
			      "genEngineIdException, unknown engine ID "+
			      "when looking for user [" + 
			      params.getPrincipal() +"]");
	    genExp.genEngineIdException(contextEngineId,
					contextName,
					msgFlags,
					params);
	}
	
	//NoAuthNoPriv
	if((msgSecurityLevel & SnmpDefinitions.authPriv) == 
	   SnmpDefinitions.noAuthNoPriv) {
	    //Nothing to do
	    if(logger.finestOn())
		logger.finest("processIncomingMessage", 
			      "noAuthNoPriv message");
	    //Fill the reply with the provided data
	    decryptedPdu.data = data;
	    decryptedPdu.contextName = contextName;
	    decryptedPdu.contextEngineId = contextEngineId;
	    return params;
	}
	
	//Athentication
	if(msgSecurityLevel == SnmpDefinitions.authNoPriv) {
	    if(logger.finestOn())
		logger.finest("processIncomingMessage", 
			      "authNoPriv message, authModule : "+authModule);
	    //First authenticate. Will check the algorithm.
	    authModule.authMsg(sys,
			       params,
			       usmcache,
			       version,
			       msgId,
			       msgMaxSize,
			       msgFlags,
			       msgSecurityModel,
			       contextEngineId,
			       contextName,
			       data);
	    handleTimeliness(params);
	    //Fill the reply with the provided data
	    decryptedPdu.data = data;
	    decryptedPdu.contextName = contextName;
	    decryptedPdu.contextEngineId = contextEngineId;
	    return params;
	}
	
	//Privacy
	if(msgSecurityLevel == SnmpDefinitions.authPriv) {
	    if(logger.finestOn())
		logger.finest("processIncomingMessage", 
			      "authPriv message");
	    authModule.authMsg(sys,
			       params,
			       usmcache,
			       version,
			       msgId,
			       msgMaxSize,
			       msgFlags,
			       msgSecurityModel,
			       encryptedPdu);

	    handleTimeliness(params);
	    
	    SnmpDecryptedPdu pdu = privModule.decrypt(sys,
						      version,
						      params,
						      usmcache,
						      encryptedPdu,
						      msgFlags);
	    //Fill the reply with the decrypted data
	    decryptedPdu.data = pdu.data;
	    decryptedPdu.contextName = pdu.contextName;
	    decryptedPdu.contextEngineId = pdu.contextEngineId;
	}
	
	return params;
    }

    /**
     * The main outgoing message algorithm. Make the call to all specialized
     * methods (authentication, sign, ...) according to the security level 
     */
    private int processOutgoingMessage(SnmpMsgProcessingSubSystem sys,
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
				       int dataLength,
				       byte[] outputBytes,
				       boolean response) 
        throws SnmpUsmException, SnmpStatusException, 
               SnmpTooBigException {
        try {
            //Isolate the security level from the flag.
            int securityLevel = msgFlags & SnmpDefinitions.authPriv;
            int workingBuffLen = 0;
 
            switch(securityLevel) {
            case SnmpDefinitions.noAuthNoPriv: {
		// We don't check when doing engineId discovery.
		if ( (!DISCOVERY_USER.equals(params.getPrincipal()) &&
		      params.getAuthoritativeEngineId() != null)  && 
		     response == false){
		    try {
			if(logger.finestOn())
			    logger.finest("processOutgoingMessage", 
					  "Sending noAuthNoPriv from :" + 
					  params.getAuthoritativeEngineId() + 
					  ", principal : [" + 
					  params.getPrincipal() + "]");
			lcd.getUser(params.getAuthoritativeEngineId(),
				    params.getPrincipal());
			if(logger.finestOn())
			    logger.finest("processOutgoingMessage", 
					  "OK noAuthNoPriv msg");
		    }
		    catch(SnmpUsmEngineIdException e) {
			if(logger.finestOn())
			    logger.finest("processOutgoingMessage", 
					  "SnmpUsmEngineIdException");
			throw new SnmpUsmUserNameException("Unknown user :" +
						       params.getPrincipal());
		    }
		}
		//Only encoding is needed.
		return sys.encode(version, 
				  msgId,
				  msgMaxSize,
				  msgFlags,
				  msgSecurityModel,
				  params,
				  contextEngineId,
				  contextName,
				  data,
				  dataLength,
				  outputBytes);
	    }
	    case SnmpDefinitions.authNoPriv: {
		byte[] hmac = authModule.signMsg(sys,
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
						 dataLength);
		//Set the calculated hmac.
		params.setAuthParameters(hmac);
		//Second pass. Will encode again with the calculated HMAC.
		int len = sys.encode(version, 
                                     msgId,
                                     msgMaxSize,
                                     msgFlags,
                                     msgSecurityModel,
                                     params,
                                     contextEngineId,
                                     contextName,
                                     data,
                                     dataLength,
                                     outputBytes);

                return len;
            }
            case SnmpDefinitions.authPriv: {
                byte[] pduToEncrypte = new byte[msgMaxSize];
                byte[] authBuff = new byte[msgMaxSize];
                SnmpDecryptedPdu pdu = new SnmpDecryptedPdu();
                pdu.data = data;
                pdu.dataLength = dataLength;
                pdu.contextEngineId = contextEngineId;
                pdu.contextName = contextName;
                if(logger.finestOn()) {
                    logger.finest("processOutgoingMessage", 
				  "Flat pdu to encrypt : length : " + 
				  pdu.data.length + "Engine Id :" +
				  SnmpTools.binary2ascii(pdu.contextEngineId)+ 
				  " Contexte name :" + 
				  ((pdu.contextName == null )? 
				   "Unknown context name": 
				   new String( pdu.contextName)) );   
                }
                //First encode the pdu.
                int length = sys.encode(version, 
                                        pdu,
                                        pduToEncrypte);
                //Encrypt the resulting byte sequence.
                byte[] encryptedPdu =  privModule.encrypt(usmcache,
                                                          params,
                                                          pduToEncrypte,
                                                          length);
		//Sign it.
		byte[] hmac = authModule.signMsg(sys,
						 params,
						 usmcache,
						 version,
						 msgId,
						 msgMaxSize,
						 msgFlags,
						 msgSecurityModel,
						 encryptedPdu);
		//Set the calculated hmac.
		params.setAuthParameters(hmac);
		int len = sys.encodePriv(version,
					 msgId,
					 msgMaxSize,
					 msgFlags,
					 msgSecurityModel,
					 params,
					 encryptedPdu,
					 outputBytes);
		
                return len;
            }
            default:
                throw new SnmpUsmException("Bad Security Level.");

            }
	    
        }
        catch(ArrayIndexOutOfBoundsException x) {
	    if(logger.finestOn()) {
		logger.finest("processOutgoingMessage", x);
	    }            
	    throw new SnmpTooBigException();
        }
        catch(SnmpUnknownMsgProcModelException x) {
            if(logger.finestOn()) {
		logger.finest("processOutgoingMessage", x);
	    }
            throw new SnmpUsmException("unknown MsgProcModel Exception");
        }
    }

    /*
     * *********** Parameters request and response handling. ************
     */
    SnmpUsmSecureUser getUser(SnmpEngineId engineId, String user) 
	throws SnmpUsmUserNameException, SnmpUsmEngineIdException {
	SnmpUsmSecureUser secureUser = null;
	if(logger.finestOn())
	    logger.finest("getUser", "Getting user infos for :" + engineId + 
			  ", principal : [" +user + "]");
	secureUser = lcd.getUser(engineId,
				 user);
	if(logger.finestOn())
	    logger.finest("getUser", 
			  "OK Found user");
	return secureUser;
    }
    
    boolean handleEngineIdDiscovery(SnmpUsmSecurityParameters params,
				    byte[] contextEngineId,
				    byte[] contextName) 
	throws SnmpSecurityException, SnmpStatusException {
	if(logger.finestOn())
	    logger.finest("handleEngineIdDiscovery", "principal :" + 
			  params.getPrincipal() + ", engine Id" + 
			  params.getAuthoritativeEngineId());
	//Incoming discovery
	if(params.getAuthoritativeEngineId() == null) { //Engine Id discovery.
	    if(logger.finerOn())
		logger.finer("handleEngineIdDiscovery", 
			     "Engine Id Discovery. Will send back :" + 
			     engine.getEngineId());
	    
	    params.setAuthoritativeEngineId(engine.getEngineId());
	    params.setAuthoritativeEngineTime(0);
	    params.setAuthoritativeEngineBoots(0);
	    //Generate an exception thqt will be reported.
	    genExp.genEngineIdException(contextEngineId,
					contextName,
					(byte)(SnmpDefinitions.noAuthNoPriv | 
					       SnmpDefinitions.reportableFlag),
					params);
	    return false;
	}
	//Incoming response	
	if(DISCOVERY_USER.equals(params.getPrincipal())) {
	    if(!engine.getEngineId().equals(params.
					    getAuthoritativeEngineId())) {
		if(logger.finerOn())
		    logger.finer("handleEngineIdDiscovery", 
				 "Engine Id Discovery response : [" + 
				 params.getAuthoritativeEngineId());
		lcd.addEngine(params.getAuthoritativeEngineId());
		SnmpUsmEnginePeer peer = (SnmpUsmEnginePeer)
		    peers.get(params.getAuthoritativeEngineId().toString());
		if(peer == null) {
		    if(logger.finestOn())
			logger.finest("handleEngineIdDiscovery", 
				      "Create peer.");
		    peer = new SnmpUsmEnginePeer(params.
						 getAuthoritativeEngineId());
		    peers.put(params.getAuthoritativeEngineId().toString(),
			      peer);
		}
	    }
	    else {
		if(logger.finerOn())
		    logger.finer("handleEngineIdDiscovery", 
				 "Received a response with engine Id "+
				 "== Local One. Throwing exception.");
		SnmpSecurityException e = 
		    new SnmpSecurityException("Engine Id discovery failed, "+
					      "Received engine Id [" + 
					      params.getAuthoritativeEngineId()
					      + "] is equals to local one!");
		e.status = SnmpDefinitions.snmpUsmBadEngineId;
		throw e;
	    }
	    return true;
	}
	return false;
    }
    
    void handleTimeliness(SnmpUsmSecurityParameters params) 
	throws SnmpSecurityException, SnmpStatusException {
	if(engine.getEngineId().equals(params.getAuthoritativeEngineId())) {
	    if(logger.finestOn())
		logger.finest("handleTimeliness", 
			      "Check Request timeliness.");
	    timeModule.handleTimeliness(engine,
					params);
	}
	else {
	    SnmpUsmEnginePeer peer = (SnmpUsmEnginePeer)
		peers.get(params.getAuthoritativeEngineId().toString());
	    if(peer == null) {
		if(logger.finestOn())
		    logger.finest("handleTimeliness", 
				  "Received an authenticated unconfirmed pdu"+
				  " from an unknown authoritative. "+
				  "Create peer.");
		peer = 
		    new SnmpUsmEnginePeer(params.getAuthoritativeEngineId());
                peers.put(params.getAuthoritativeEngineId().toString(),
                          peer);
	    }
	    if(logger.finestOn())
		logger.finest("handleTimeliness", 
			      "Check Response timeliness.");
	    // Does the manager sync part of timeliness.
	    timeModule.handleResponseTimeliness(peer,
						params);
	}
    }

    /**
     * Instantiate a new SnmpUsmSecurityParameters and ask it to decode.
     */ 
    SnmpUsmSecurityParameters decodeParameters(byte[] params) 
        throws SnmpStatusException {
        SnmpUsmSecurityParameters parameters = 
            createUsmSecurityParameters();
        parameters.decode(params);
        return parameters;
    }

    /**
     * Increment given counter.
     */
    synchronized long incUnsupportedSecLevelsCounter(int n) {
        unsupportedSecLevelsCounter += n;
        return unsupportedSecLevelsCounter;
    }
    /**
     * Increment given counter.
     */
    synchronized long incNotInTimeWindowsCounter(int n) {
        notInTimeWindowsCounter += n;
        return notInTimeWindowsCounter;
    }
    /**
     * Increment given counter.
     */
    synchronized long incUnknownUserNamesCounter(int n) {
        unknownUserNamesCounter += n;
        return unknownUserNamesCounter;
    }
    /**
     * Increment given counter.
     */
    synchronized long incUnknownEngineIdsCounter(int n) {
        unknownEngineIdsCounter += n;
        return unknownEngineIdsCounter;
    }
    /**
     * Increment given counter.
     */
    synchronized long incWrongDigestsCounter(int n) {
        wrongDigestsCounter += n;
        return wrongDigestsCounter;
    }
    /**
     * Increment given counter.
     */
    synchronized long incDecryptionErrorsCounter(int n) {
        decryptionErrorsCounter += n;
        return decryptionErrorsCounter;
    }

    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpUserSecurityModel");
    
    String dbgTag = "SnmpUserSecurityModel"; 
}
