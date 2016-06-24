/*
 * @(#)file      SnmpMsgTranslatorV1V2.java
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
package com.sun.management.snmp.mpm;

import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpMessage;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpSecurityParameters;
/**
 * This translator is dedicated to translate V1 V2 parameters in the
 * context of SNMP V3 messages. It infers some parameters such as
 * context name and context engine Id.
 *
 * @since Java DMK 5.1
 */
public class SnmpMsgTranslatorV1V2 implements SnmpMsgTranslator {
    SnmpEngine engine = null;
    
    public SnmpMsgTranslatorV1V2(SnmpEngine engine) {
	this.engine = engine;
    }
    public int getMsgId(SnmpMsg msg) {
	//No Id in the message. It is a pdu level info.
	return 0;
    }
    public int getMsgMaxSize(SnmpMsg msg) {
	//Arbitrarily set max response size. It is the same as com.sun.management.snmp.SnmpPeer default.
	return (2 * 1024);
    }
    public byte getMsgFlags(SnmpMsg msg) {
	//No message flags
	return 0;
    }
    public int getSecurityLevel(SnmpMsg msg) {
	//npAuthNoPriv
	return 0;
    }
    public int getMsgSecurityModel(SnmpMsg msg) {
	// Infers the security model according to the protocol version number. Approriate security model MUST be registered using these values (1 and 2).
	return msg.version == SnmpDefinitions.snmpVersionOne ? 1 : 2;
    }
  
    public SnmpSecurityParameters getSecurityParameters(SnmpMsg msg) {
	//No security parameters in V1 V2.
	
	return null;
    }
    /**
     *  The IP address is used as the ACL key. It is returned in this call.
     */
    public byte[] getFlatSecurityParameters(SnmpMsg msg) {
        //System.out.println("getFlatSecurityParameters : " + msg.address.getHostAddress());
	return msg.address.getHostAddress().getBytes();
    }
    /**
     * Context engine Id is ALL THE TIME the local one. No proxy notion.
     */
    public byte[] getContextEngineId(SnmpMsg msg) {
	return engine.getEngineId().getBytes();
    }
    /**
     * The received community string contains : The context Name + the access context (via @ separator).
     */
    public byte[] getRawContextName(SnmpMsg msg) {
	return ((SnmpMessage)msg).community;
    }

    /**
     * A context name can be added at the end of the community string via @ separator. If no @ is located, null is returned.
     */
    public byte[] getContextName(SnmpMsg msg) {
	return null;
    }

    /**
     * The access context (used in IP ACL) is the community string. The community string or null is returned.
     */
    public byte[] getAccessContext(SnmpMsg msg) {
	return ((SnmpMessage)msg).community;
    }
    /**
     * No encryption, returns null.
     */
    public byte[] getEncryptedPdu(SnmpMsg msg) {
	return null;
    }
    /**
     * Does nothing, is used in V3 to set the context name after decryption.
     */
    public void setContextName(SnmpMsg req, byte[] contextName) {}
    /**
     * Does nothing, is used in V3 to set the context engine id after decryption.
     */
    public void setContextEngineId(SnmpMsg req, byte[] contextEngineId){}


}
