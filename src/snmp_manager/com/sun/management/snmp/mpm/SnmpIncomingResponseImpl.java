/*
 * @(#)file      SnmpIncomingResponseImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.18
 * @(#)lastedit  07/03/08
 * @(#)build     @BUILD_TAG_PLACEHOLDER@
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
 */
package com.sun.management.snmp.mpm;

// java imports
//
import java.util.Vector;
import java.net.InetAddress;

// import debug stuff
//
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.SnmpScopedPduPacket;
import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpSecurityException;

import com.sun.management.internal.snmp.SnmpIncomingResponse;
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.internal.snmp.SnmpSecuritySubSystem;
import com.sun.management.internal.snmp.SnmpSecurityModel;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;
import com.sun.management.internal.snmp.SnmpSecurityCache;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;
import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpBadSecurityLevelException;

class SnmpIncomingResponseImpl
    implements SnmpIncomingResponse {
    SnmpMsg resp = null;
    SnmpSecuritySubSystem secureSubSys = null;
    SnmpMsgTranslator translator = null;
    SnmpSecurityParameters params = null;
    SnmpSecurityCache cache = null;
    SnmpPduFactory factory = null;
    public SnmpIncomingResponseImpl(SnmpSecuritySubSystem secureSubSys,
				    SnmpPduFactory factory,
				    SnmpMsg resp,
				    SnmpMsgTranslator translator) {
	this.secureSubSys = secureSubSys;
	this.resp = resp;
	this.translator = translator;
	this.factory = factory;
    }
    
    public InetAddress getAddress() {
	return resp.address;
    }

    public int getPort() {
	return resp.port;
    }

    public void setSecurityCache(SnmpSecurityCache cache) {
	this.cache = cache;
    }

    public int getSecurityLevel() {
	return translator.getSecurityLevel(resp);
    }
    
    public int getSecurityModel() {
	return translator.getMsgSecurityModel(resp);
    }

    public byte[] getContextName() {
	return translator.getContextName(resp);
    }

    public SnmpSecurityParameters getSecurityParameters() {
	return params;
    }
    public int getRequestId(byte[] data) throws SnmpStatusException {
	return resp.getRequestId(data);
    }
    public SnmpMsg decodeMessage(byte[] inputBytes, 
				 int byteCount, 
				 InetAddress address,
				 int port) 
        throws SnmpStatusException, SnmpSecurityException {
	resp.address = address;
	resp.port = port;
	resp.decodeMessage(inputBytes, byteCount);
	SnmpDecryptedPdu decrPdu = new SnmpDecryptedPdu();
	try {
	    params = secureSubSys.
		processIncomingResponse(cache,
					resp.version,
					translator.getMsgId(resp),
					translator.getMsgMaxSize(resp),
					translator.getMsgFlags(resp),
					translator.
					getMsgSecurityModel(resp),
					translator.
					getFlatSecurityParameters(resp),
					translator.
					getContextEngineId(resp),
					translator.getContextName(resp),
					resp.data,
					translator.
					getEncryptedPdu(resp),
					decrPdu);
	    secureSubSys.releaseSecurityCache(translator.
					      getMsgSecurityModel(resp),cache);
	}catch(SnmpUnknownSecModelException x) {
	    if (logger.finestOn()) {
                logger.finest("decodeMessage",
		      x.toString() +"\n Will fail in timeout!!!");
            }
	    return null;
	}
	if(decrPdu.data != null) {
	    resp.data = decrPdu.data;
	    translator.setContextName(resp, decrPdu.contextName);
	    translator.setContextEngineId(resp, decrPdu.contextEngineId);
	}
	return resp;
    }
    
    public SnmpPdu decodeSnmpPdu() 
	throws SnmpStatusException {
	return factory.decodeSnmpPdu(resp);
    }

    public String printMessage() {
	return resp.printMessage();
    }

    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpIncomingResponseImpl");
    
    String dbgTag = "SnmpIncomingResponseImpl";
}
