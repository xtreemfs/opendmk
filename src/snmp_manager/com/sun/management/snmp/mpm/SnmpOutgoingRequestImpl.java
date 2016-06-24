/*
 * @(#)file      SnmpOutgoingRequestImpl.java
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

package com.sun.management.snmp.mpm;

// java imports
//
import java.util.Vector;
import java.net.InetAddress;

import com.sun.management.snmp.SnmpScopedPduPacket;

import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpPduFactory;
import com.sun.management.snmp.SnmpSecurityException;

import com.sun.management.internal.snmp.SnmpOutgoingRequest;
import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.internal.snmp.SnmpMsgProcessingSubSystem;
import com.sun.management.internal.snmp.SnmpSecuritySubSystem;
import com.sun.management.internal.snmp.SnmpSecurityModel;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;
import com.sun.management.internal.snmp.SnmpSecurityCache;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;

import com.sun.management.snmp.SnmpUnknownSecModelException;
import com.sun.management.snmp.SnmpBadSecurityLevelException;
/**
 * FOR INTERNAL USE ONLY. Implements SnmpOutgoingRequest interface. Calls the security sub system to handle security that is added to the message when went. It is multilingual, deals with a translator (that can be V1, V2 or V3).
 *
 * @since Java DMK 5.1
 */
class SnmpOutgoingRequestImpl
    implements SnmpOutgoingRequest {
    SnmpMsg req = null;
    SnmpSecuritySubSystem secureSubSys = null;
    SnmpMsgTranslator translator = null;
    SnmpSecurityCache cache = null;
    SnmpPduFactory factory = null;
    public SnmpOutgoingRequestImpl(SnmpSecuritySubSystem secureSubSys,
				   SnmpPduFactory factory,
				   SnmpMsg req,
				   SnmpMsgTranslator translator) {
	this.secureSubSys = secureSubSys;
	this.req = req;
	this.factory = factory;
	this.translator = translator;
    }
    /** 
     * Get the cache that as been filled when encoding the message.
     */
    public SnmpSecurityCache getSecurityCache() {
	return cache;
    }
    /**
     * Make a call to the security sub system to add security to the message.
     */
    public int encodeMessage(byte[] outputBytes) 
	throws SnmpStatusException, 
	       SnmpTooBigException, SnmpSecurityException,
	       SnmpUnknownSecModelException, SnmpBadSecurityLevelException {
	int encodingLength = 0;
	//The security level is checked according to the rules implemented in SnmpEngine class.
	SnmpEngineImpl.checkSecurityLevel(translator.getMsgFlags(req));
	// Ask to create the cache according to the right security model.
	cache = 
	    secureSubSys.createSecurityCache(translator.getMsgSecurityModel(req));
	//Generate an encoded output flow.
	    encodingLength = 
		secureSubSys.generateRequestMsg(cache,
						req.version,
						translator.getMsgId(req),
						translator.getMsgMaxSize(req),
						translator.getMsgFlags(req),
						translator.getMsgSecurityModel(req),
						translator.getSecurityParameters(req),
						translator.getContextEngineId(req),
						translator.getRawContextName(req),
						req.data,
						req.dataLength,
						outputBytes);

	return encodingLength;
    }
    /**
     * Delegates on the SnmpMsg.
     */
    public String printMessage() {
	return req.printMessage();
    }
    /**
     * Delegates on the SnmpMsg and Factory.
     */
    public SnmpMsg encodeSnmpPdu(SnmpPdu p, 
				 int maxDataLength) 
        throws SnmpStatusException, SnmpTooBigException {
	req = factory.encodeSnmpPdu(p, maxDataLength);
	return req;
    }

    String dbgTag = "SnmpOutgoingRequestImpl";
}
