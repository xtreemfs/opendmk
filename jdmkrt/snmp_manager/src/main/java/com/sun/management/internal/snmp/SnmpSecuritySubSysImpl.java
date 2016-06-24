/*
 * @(#)file      SnmpSecuritySubSysImpl.java
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
package com.sun.management.internal.snmp;

import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpSecurityException;
import com.sun.management.snmp.SnmpUnknownModelException;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpSecurityParameters;
/**
 * FOR INTERNAL USE ONLY. Implements SnmpSecuritySubSystem interface.
 *
 * @since Java DMK 5.1
 */
class SnmpSecuritySubSysImpl extends SnmpSubSystemImpl 
implements SnmpSecuritySubSystem {

    public SnmpSecuritySubSysImpl(SnmpEngine eng) {
	super(eng);
    }

    /**
     * See SnmpSecuritySubSystem interface doc.
     */
    public SnmpSecurityCache createSecurityCache(int id) 
	throws SnmpUnknownSecModelException {
	SnmpSecurityModel model = null;
	try {
	    model = (SnmpSecurityModel)getModel(id);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownSecModelException("Unknown model: " + e);
	}
	return model.createSecurityCache();
    }
    /**
     * See SnmpSecuritySubSystem interface doc.
     */
    public void releaseSecurityCache(int id, 
				     SnmpSecurityCache cache) 
	throws SnmpUnknownSecModelException{
	SnmpSecurityModel model = null;
	try {
	    model = (SnmpSecurityModel)getModel(id);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownSecModelException("Unknown model: " + e);
	}
	model.releaseSecurityCache(cache);
    }
    /**
     * See SnmpSecuritySubSystem interface doc.
     */
    public int generateRequestMsg(SnmpSecurityCache cache,
				  int version,
				  int msgID,
				  int msgMaxSize,
				  byte msgFlags,
				  int msgSecurityModel,
				  SnmpSecurityParameters params,
				  byte[] contextEngineID,
				  byte[] contextName,
				  byte[] data,
				  int dataLength,
				  byte[] outputBytes) 
	throws SnmpTooBigException, SnmpStatusException, 
	       SnmpSecurityException, SnmpUnknownSecModelException {
     
        //System.out.println("msgSecurityModel : " + msgSecurityModel);
        SnmpSecurityModel model = null;
	try {
	    model = (SnmpSecurityModel) getModel(msgSecurityModel);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownSecModelException("Unknown model: " + e);
	}
	return model.generateRequestMsg(cache,
					version,
					msgID,
					msgMaxSize,
					msgFlags,
					msgSecurityModel,
					params,
					contextEngineID,
					contextName,
					data,
					dataLength,
					outputBytes);
    }
    /**
     * See SnmpSecuritySubSystem interface doc.
     */
    public int generateResponseMsg(SnmpSecurityCache cache,
				   int version,
				   int msgID,
				   int msgMaxSize,
				   byte msgFlags,
				   int msgSecurityModel,
				   SnmpSecurityParameters p,
				   byte[] contextEngineID,
				   byte[] contextName,
				   byte[] data,
				   int dataLength,
				   byte[] outputBytes)
	throws SnmpTooBigException, SnmpStatusException, 
	       SnmpSecurityException, SnmpUnknownSecModelException {
        //System.out.println("msgSecurityModel : " + msgSecurityModel);
	SnmpSecurityModel model = null;
	try {
	    model = (SnmpSecurityModel) getModel(msgSecurityModel);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownSecModelException("Unknown model: " + e);
	}

	return model.generateResponseMsg(cache,
					 version,
					 msgID,
					 msgMaxSize,
					 msgFlags,
					 msgSecurityModel,
					 p,
					 contextEngineID,
					 contextName,
					 data,
					 dataLength,
					 outputBytes);
    }
    /**
     * See SnmpSecuritySubSystem interface doc.
     */
    public SnmpSecurityParameters 
	processIncomingRequest(SnmpSecurityCache cache,
			       int version,
			       int msgID,
			       int msgMaxSize,
			       byte msgFlags,
			       int msgSecurityModel,
			       byte[] msgSecurityParameters,
			       byte[] contextEngineID,
			       byte[] contextName,
			       byte[] data,
			       byte[] encryptedPdu,
			       SnmpDecryptedPdu decryptedPdu)
	throws SnmpStatusException, SnmpSecurityException, 
	       SnmpUnknownSecModelException {

	SnmpSecurityModel model = null;
	try {
	    model = (SnmpSecurityModel)getModel(msgSecurityModel);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownSecModelException("Unknown model: " + e);
	}
	return model.processIncomingRequest(cache,
					    version,
					    msgID,
					    msgMaxSize,
					    msgFlags,
					    msgSecurityModel,
					    msgSecurityParameters,
					    contextEngineID,
					    contextName,
					    data,
					    encryptedPdu,
					    decryptedPdu);
    }
    /**
     * See SnmpSecuritySubSystem interface doc.
     */
    public SnmpSecurityParameters processIncomingResponse(SnmpSecurityCache cache,
							  int version,
							  int msgID,
							  int msgMaxSize,
							  byte msgFlags,
							  int msgSecurityModel,
							  byte[] msgSecurityParameters,
							  byte[] contextEngineID,
							  byte[] contextName,
							  byte[] data,
							  byte[] encryptedPdu,
							  SnmpDecryptedPdu decryptedPdu)
	throws SnmpStatusException, SnmpSecurityException, SnmpUnknownSecModelException {
	SnmpSecurityModel model = null;
	try {
	    model = (SnmpSecurityModel)getModel(msgSecurityModel);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownSecModelException("Unknown model: " + e);
	}
	
	return model.processIncomingResponse(cache,
					     version,
					     msgID,
					     msgMaxSize,
					     msgFlags,
					     msgSecurityModel,
					     msgSecurityParameters,
					     contextEngineID,
					     contextName,
					     data,
					     encryptedPdu,
					     decryptedPdu);
    }	

    String dbgTag = "SnmpSecuritySubSysImpl";
}
