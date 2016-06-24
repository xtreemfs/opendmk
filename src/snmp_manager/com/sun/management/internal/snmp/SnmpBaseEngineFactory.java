/*
 * @(#)file      SnmpBaseEngineFactory.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.39
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
package com.sun.management.internal.snmp;


import javax.management.ObjectName;
import com.sun.management.internal.snmp.SnmpSecuritySubSystem;
import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.snmp.usm.SnmpUserSecurityModel;
import com.sun.management.snmp.usm.SnmpUsmHmacMd5;
import com.sun.management.snmp.usm.SnmpUsmException;
import com.sun.management.snmp.usm.SnmpUsmDesAlgorithm;
import com.sun.management.snmp.usm.SnmpUsmAlgorithmManager;
import com.sun.management.snmp.usm.SnmpUsm;
import com.sun.management.snmp.usm.SnmpUsmLcd;
import com.sun.management.snmp.usm.SnmpUsmHmacSha;
import com.sun.management.snmp.usm.SnmpUsmPasswordLcd;
import com.sun.management.snmp.mpm.SnmpMsgProcessingModelV1V2;
import com.sun.management.snmp.mpm.SnmpMsgProcessingModelV3;
import com.sun.management.internal.snmp.SnmpLcd;
import com.sun.management.internal.snmp.SnmpModelLcd;
import com.sun.management.internal.snmp.SnmpJdmkLcd;
import com.sun.management.internal.snmp.SnmpSecurityModelV1V2;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineParameters;
import com.sun.management.snmp.SnmpEngineFactory;

import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.InetAddressAcl;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * This class is a customizable base class for factories of  
 * {@link com.sun.management.snmp.SnmpEngine}.
 * This <CODE>SnmpEngineFactory</CODE> is instantiating an <CODE>SnmpEngine</CODE> containing :
 * <ul> 
 * <li> Message Processing Sub System + V1, V2 et V3 Message Processing Models</li>
 * <li> Security Sub System + User based Security Model (Id 3)</li>
 * </ul>
 * <P> SNMP V3 can be deactivated. By default the returned engine is V3 
 * enabled. </P>
 *
 * @since Java DMK 5.1
 */
public class SnmpBaseEngineFactory implements SnmpEngineFactory {
    
    /**
     * The engine instantiation method. Calling more than once this method 
     * will return the same <CODE> SnmpEngine </CODE> instance.
     * @param parameters The engine parameters to use.
     * @return The newly created <CODE>SnmpEngine</CODE>.
     * @throws IllegalArgumentException Thrown if one of the configuration 
     *         file file doesn't exist (Acl files, lcd file).
     */
    public SnmpEngine createEngine(SnmpEngineParameters parameters) 
	throws IllegalArgumentException {
	String securityFile = parameters.getSecurityFile();
	SnmpEngineId engineid = parameters.getEngineId();
	SnmpLcd lcd = createEngineLcd(securityFile);
	SnmpEngineImpl engine = null;
	try {
	    engine = new SnmpEngineImpl(this, lcd, engineid);
	}catch(UnknownHostException e) {
	    throw new IllegalArgumentException("Unknown host: " + 
					       e.getMessage());
	}
	init(parameters, engine, lcd);
	
	return engine;
    }
    /**
     * The engine instantiation method.
     * @throws IllegalArgumentException Thrown if one of the configuration 
     * file file doesn't exist (Acl files, security file).
     * @param p The parameters used to instantiate a new engine.
     * @param ipacl The Ip ACL to pass to the Access Control Model.
     * @return The newly created SnmpEngine.
     */
    public SnmpEngine createEngine(SnmpEngineParameters p,
				   InetAddressAcl ipacl) {
	return createEngine(p);
    }
    /**
     * This method is called by the factory when creating the engine Lcd. 
     * @param file The file where to read the configuration (null if not provided via <CODE>setSecurityFile</CODE>).
     * @throws IllegalArgumentException Thrown if the configuration can't be read.
     * @return The engine Lcd.
     */
    protected SnmpLcd createEngineLcd(String file) throws IllegalArgumentException {
	return  new SnmpJdmkLcd(file);
    }
    
    /**
     * This method is called by the factory when creating the algorithm manager. It is in charge of the Usm algorithms. If you want to provide your own algorithm implementations, do it in this method too.
     */
    protected SnmpUsmAlgorithmManager createAlgorithmManager(SnmpEngine engine) {
	return new SnmpUsmAlgorithmManager();
    }
    
    /**
     * This method is called by the factory when creating the security sub system.
     * @param engine The SNMP engine.
     * @return The security sub system.
     */
    protected SnmpSecuritySubSystem createSecuritySubSystem(SnmpEngine engine)
    {
	return new SnmpSecuritySubSysImpl(engine);
    }
    
    /**
     * This method is called by the factory when creating the message processing sub system.
     * @param engine The SNMP engine.
     * @return The message processing sub system.
     */
    protected SnmpMsgProcessingSubSystem createMsgProcessingSubSystem(SnmpEngine engine) {
	return new SnmpMsgProcessingSubSysImpl(engine);
    }
    /**
     * This method is called by the factory when creating the User based 
     * security model Lcd. 
     * @param engine The engine.
     * @param lcd The previously created Lcd.
     * @param securSys The security sub system.
     * @param file The security file passed by <CODE>setSecurityFile</CODE>
     * @return The SNMP Usm Lcd.
     * @throws IllegalArgumentException Thrown if the configuration can't 
     *   be read.
     */
    protected SnmpUsmLcd createUsmLcd(SnmpEngine engine,
				      SnmpLcd lcd,
				      SnmpSecuritySubSystem securSys,
				      String file) 
	throws IllegalArgumentException {
	return new SnmpUsmPasswordLcd(engine,
				      securSys,
				      lcd,
				      file);
    }
    /**
     * This method is called by the factory when creating the User based security model. The created model is responsible for registering in the passed sub system.
     * @param engine The engine.
     * @param usmlcd The previously created Usm Lcd.
     * @param subSys The security sub system.
     * @return The SNMP Usm security model.
     */
    protected SnmpUsm createUsmSecurityModel(SnmpEngine engine,
					     SnmpUsmLcd usmlcd,
					     SnmpSecuritySubSystem subSys)
    {
	SnmpUsm ubm =  new SnmpUserSecurityModel(subSys, usmlcd);	
	return ubm;
    }

    /**
     * This method is called by the factory when creating the Usm algorithm manager. By default it adds to the manager HMAC_MD5_AUTH and HMAC_SHA_AUTH algorithms.
     */
    void fillAlgorithmManager(SnmpEngineParameters parameters,
			      SnmpUsmAlgorithmManager algoManager,
			      SnmpEngineImpl engine) {
	if(algoManager.getAlgorithm(SnmpUsmHmacMd5.HMAC_MD5_AUTH) == null)
	    algoManager.addAlgorithm(new SnmpUsmHmacMd5());
	if(algoManager.getAlgorithm(SnmpUsmHmacSha.HMAC_SHA_AUTH) == null)
	    algoManager.addAlgorithm(new SnmpUsmHmacSha());
	
	if(parameters.isEncryptionEnabled()) {
	    {
		if(logger.finestOn()) {
		    logger.finest("init", "Usm encryption activated.");
		}
		try {
		    if(algoManager.getAlgorithm(SnmpUsmDesAlgorithm.DES_PRIV) == null)
			algoManager.addAlgorithm(new SnmpUsmDesAlgorithm(engine));
		}catch(SnmpUsmException e) {
		    if(logger.finerOn()) {
			logger.finer("init", "Problemm initializing encryption, check your classpath: " + e.toString()); 
		    }
		}
		if(logger.finestOn()) {
		    logger.finest("init", "Usm encryption algos added.");
		}
	    }
	}
    }
    
    /**
     * This method is called by the factory when creating the SNMP V1 and V2 community string security model The created model is responsible for registering in the passed sub system.
     * @param engine The engine.
     * @param subSys The security sub system.
     * @return The SNMP V1 and V2 security model.
     */
    protected SnmpSecurityModel 
	createCommunityStringSecurityModel(SnmpEngine engine,
					   SnmpSecuritySubSystem subSys)
    {
	return new SnmpSecurityModelV1V2(subSys);
    }
    
    /**
     * This method is called by the factory when creating the SNMP V1 Msg processing Model. The created model is responsible for registering in the passed sub system.
     * @param engine The engine.
     * @param subSys The message processing sub system.
     * @return The SNMP V1 message processing model.
     */
    protected SnmpMsgProcessingModel createSnmpV1MsgProcessingModel(SnmpEngine engine, SnmpMsgProcessingSubSystem subSys) {
	 return new SnmpMsgProcessingModelV1V2(subSys, null);
    }

    /**
     * This method is called by the factory when creating the SNMP V2 Msg processing Model. The created model is responsible for registering in the passed sub system.
     * @param engine The engine.
     * @param subSys The message processing sub system.
     * @param v1mpm The SNMP V1 message processing model.
     * @return The SNMP V2 message processing model.
     */
    protected SnmpMsgProcessingModel createSnmpV2MsgProcessingModel(SnmpEngine engine, SnmpMsgProcessingSubSystem subSys, SnmpMsgProcessingModel v1mpm) {
	return v1mpm;
    }

     /**
      * This method is called by the factory when creating the SNMP V3 Msg processing Model. The created model is responsible for registering in the passed sub system.
      * @param engine The engine.
      * @param subSys The message processing sub system.
      * @param v2mpm The SNMP V2 message processing model.
      * @return The SNMP V3 message processing model.
      */
    protected SnmpMsgProcessingModel createSnmpV3MsgProcessingModel(SnmpEngine engine, SnmpMsgProcessingSubSystem subSys, SnmpMsgProcessingModel v2mpm) {
	return new SnmpMsgProcessingModelV3(subSys, null);
    }
    
    //Common initialization.
    private void init(SnmpEngineParameters p, SnmpEngineImpl engine, SnmpLcd lcd) {
	if(logger.finestOn()) {
	    logger.finest("init","Starting...");
	}
	SnmpSecuritySubSystem securSys = createSecuritySubSystem(engine);
	
	SnmpMsgProcessingSubSystem mpmSys = 
	    createMsgProcessingSubSystem(engine);
	    
	mpmSys.setSecuritySubSystem(securSys);

	engine.setSecuritySubSystem(securSys);
	engine.setMsgProcessingSubSystem(mpmSys);

	if(logger.finestOn()) {
	    logger.finest("init", "Sub Systems created.");
	}
	
	SnmpUsmLcd usmlcd = createUsmLcd(engine, 
					 lcd, 
					 securSys, 
					 p.getSecurityFile());
	
	//Perhaps done twice. To check!
	lcd.addModelLcd(securSys, SnmpUsm.ID, usmlcd);
	
	if(logger.finestOn()) {
	    logger.finest("init", "Usm Lcd added.");
	}

	SnmpUsm ubm = createUsmSecurityModel(engine,
					     usmlcd,
					     securSys);

	SnmpSecurityModel v1v2model = 
	    createCommunityStringSecurityModel(engine, securSys);
	
	if(logger.finestOn()) {
	    logger.finest("init", "Security models V1 V2 v3 added.");
	}

	SnmpUsmAlgorithmManager algoManager = createAlgorithmManager(engine);
	
	fillAlgorithmManager(p, algoManager, engine);
	
	if(logger.finestOn()) {
	    logger.finest("init", "Usm algos added.");
	}
	
	usmlcd.setAlgorithmManager(algoManager);

	usmlcd.syncDataSource();

	if(logger.finestOn()) {
	    logger.finest("init", "Usm configuration sync.");
	}
	
	SnmpMsgProcessingModel msgV1 = createSnmpV1MsgProcessingModel(engine,
								      mpmSys);
	SnmpMsgProcessingModel msgV2 = 
	    createSnmpV2MsgProcessingModel(engine,
					   mpmSys,
					   msgV1);
	if(logger.finestOn()) {
	    logger.finest("init", "V1 V2 processing model added. Can speak V1V2");
	}
	SnmpMsgProcessingModel msgV3 = null;
	msgV3 = createSnmpV3MsgProcessingModel(engine,
					       mpmSys,
					       msgV2);
	if(logger.finestOn()) {
	    logger.finest("init", "V3 processing model added. Can speak V3");
	}

	// Usm key handler stuff
	SnmpUsmKeyHandlerImpl usmKeyHandler = 
	    new SnmpUsmKeyHandlerImpl(algoManager);
	engine.setUsmKeyHandler(usmKeyHandler);
    }


    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_ADAPTOR_SNMP,
			"SnmpBaseEngineFactory");

    private String dbgTag = "SnmpBaseEngineFactory";
}
