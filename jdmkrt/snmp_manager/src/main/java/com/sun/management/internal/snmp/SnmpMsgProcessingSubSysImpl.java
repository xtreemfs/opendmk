/*
 * @(#)file      SnmpMsgProcessingSubSysImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.17
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

import java.util.Vector;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.manager.SnmpParams;
import com.sun.management.snmp.SnmpUnknownModelException;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpSecurityParameters;

import com.sun.management.internal.snmp.SnmpIncomingResponse;
import com.sun.management.snmp.SnmpUnknownMsgProcModelException;
import com.sun.management.internal.snmp.SnmpOutgoingRequest;
import com.sun.management.internal.snmp.SnmpIncomingRequest;
/**
 * FOR INTERNAL USE ONLY.
 * Implements SnmpMsgProcessingSubSystem interface.Msg processing Sub system implementation.
 *
 * @since Java DMK 5.1
 */

class SnmpMsgProcessingSubSysImpl extends SnmpSubSystemImpl 
    implements SnmpMsgProcessingSubSystem {
    
    SnmpSecuritySubSystem security = null;

    public SnmpMsgProcessingSubSysImpl(SnmpEngine engine) {
	super(engine);
    }

    public void setSecuritySubSystem(SnmpSecuritySubSystem security) {
	this.security = security;
    }
    /**
     * See SnmpMsgProcessingSubSystem interface doc.
     */
    public SnmpSecuritySubSystem getSecuritySubSystem() {
	return security;
    }
    /**
     * See SnmpMsgProcessingSubSystem interface doc.
     */
    public SnmpIncomingRequest getIncomingRequest(int vers,
						  SnmpPduFactory factory) 
	throws SnmpUnknownMsgProcModelException {
	SnmpMsgProcessingModel model = null;
	try {
	    model = (SnmpMsgProcessingModel)getModel(vers);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownMsgProcModelException("Unknown model: " + e);
	}

	return model.getIncomingRequest(factory); 
    }
    /**
     * See SnmpMsgProcessingSubSystem interface doc.
     */
    public SnmpOutgoingRequest getOutgoingRequest(int vers,
						  SnmpPduFactory factory) 
	throws SnmpUnknownMsgProcModelException{
	SnmpMsgProcessingModel model = null;
	try {
	    model = (SnmpMsgProcessingModel)getModel(vers);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownMsgProcModelException("Unknown model: " + e);
	}
	
	return model.getOutgoingRequest(factory);
    }
    /**
     * See SnmpMsgProcessingSubSystem interface doc.
     */
    public SnmpPdu getRequestPdu(int vers, SnmpParams p, int type) 
	throws SnmpUnknownMsgProcModelException, SnmpStatusException  {
	SnmpMsgProcessingModel model = null;
	try {
	    model = (SnmpMsgProcessingModel)getModel(vers);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownMsgProcModelException("Unknown model: " + e);
	}
	
	return model.getRequestPdu(p, type);
    }
    /**
     * See SnmpMsgProcessingSubSystem interface doc.
     */
    public SnmpIncomingResponse getIncomingResponse(int vers,
						    SnmpPduFactory factory) 
	throws SnmpUnknownMsgProcModelException {
	SnmpMsgProcessingModel model = null;
	try {
	    model = (SnmpMsgProcessingModel)getModel(vers);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownMsgProcModelException("Unknown model: " + e);
	}
	
	return model.getIncomingResponse(factory);
    }
    /**
     * See SnmpMsgProcessingSubSystem interface doc.
     */
    public int encode(int vers,
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
	throws SnmpTooBigException, SnmpUnknownMsgProcModelException {
	SnmpMsgProcessingModel model = null;
	try {
	    model = (SnmpMsgProcessingModel)getModel(vers);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownMsgProcModelException("Unknown model: " + e);
	}

	return model.encode(vers,
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
     * See SnmpMsgProcessingSubSystem interface doc.
     */
    public int encodePriv(int vers,
			  int msgID,
			  int msgMaxSize,
			  byte msgFlags,
			  int msgSecurityModel,
			  SnmpSecurityParameters params,
			  byte[] encryptedPdu,
			  byte[] outputBytes) 
	throws SnmpTooBigException, SnmpUnknownMsgProcModelException {
	SnmpMsgProcessingModel model = null;
	try {
	    model = (SnmpMsgProcessingModel)getModel(vers);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownMsgProcModelException("Unknown model: " + e);
	}
       
	return model.encodePriv(vers,
				msgID,
				msgMaxSize,
				msgFlags,
				msgSecurityModel,
				params,
				encryptedPdu,
				outputBytes);
    }
    /**
     * See SnmpMsgProcessingSubSystem interface doc.
     */
    public SnmpDecryptedPdu decode(int vers,
				   byte[] pdu) 
	throws SnmpStatusException, SnmpUnknownMsgProcModelException {
	SnmpMsgProcessingModel model = null;
	try {
	    model = (SnmpMsgProcessingModel)getModel(vers);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownMsgProcModelException("Unknown model: " + e);
	}

	return model.decode(pdu);
    }
    /**
     * See SnmpMsgProcessingSubSystem interface doc.
     */
    public int encode(int vers,
		      SnmpDecryptedPdu pdu,
		      byte[] outputBytes) 
	throws SnmpTooBigException, SnmpUnknownMsgProcModelException{
	SnmpMsgProcessingModel model = null;
	try {
	    model = (SnmpMsgProcessingModel)getModel(vers);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownMsgProcModelException("Unknown model: " + e);
	}

	return model.encode(pdu, outputBytes);
    }
}
