/*
 * @(#)file      SnmpMsgTranslatorV3.java
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

package com.sun.management.snmp.mpm;

import com.sun.management.snmp.SnmpV3Message;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpMsg;

import com.sun.management.snmp.SnmpSecurityParameters;

/**
 * This translator is dedicated to translate V3 parameters. No translation is needed. V3 message contains all the needed infos. No computation is done. It is a wrapper to fit the SnmpIncomingRequest translator pattern.
 *
 * @since Java DMK 5.1
 */
public class SnmpMsgTranslatorV3 implements SnmpMsgTranslator {
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public int getMsgId(SnmpMsg msg) {
	return ((SnmpV3Message)msg).msgId;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public int getMsgMaxSize(SnmpMsg msg) {
	return ((SnmpV3Message)msg).msgMaxSize;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public byte getMsgFlags(SnmpMsg msg) {
	return ((SnmpV3Message)msg).msgFlags;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public int getSecurityLevel(SnmpMsg msg) {
	return (int) ((SnmpV3Message)msg).msgFlags & SnmpDefinitions.authPriv;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public int getMsgSecurityModel(SnmpMsg msg) {
	return ((SnmpV3Message)msg).msgSecurityModel;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public byte[] getFlatSecurityParameters(SnmpMsg msg) {
	return ((SnmpV3Message)msg).msgSecurityParameters;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public SnmpSecurityParameters getSecurityParameters(SnmpMsg msg) {
	return ((SnmpV3Message)msg).securityParameters;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public byte[] getContextEngineId(SnmpMsg msg) {
	return ((SnmpV3Message)msg).contextEngineId;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public byte[] getContextName(SnmpMsg msg) {
	return ((SnmpV3Message)msg).contextName;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public byte[] getRawContextName(SnmpMsg msg) {
	return ((SnmpV3Message)msg).contextName;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public byte[] getAccessContext(SnmpMsg msg) {
	return ((SnmpV3Message)msg).contextName;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public byte[] getEncryptedPdu(SnmpMsg msg) {
	return ((SnmpV3Message)msg).encryptedPdu;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public void setContextName(SnmpMsg msg, byte[] contextName) {
	((SnmpV3Message)msg).contextName = contextName;
    }
    /**
     * See SnmpMsgTranslator interface doc.
     */
    public void setContextEngineId(SnmpMsg msg, byte[] contextEngineId) {
	((SnmpV3Message)msg).contextEngineId = contextEngineId;
    }
}
