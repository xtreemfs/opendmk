/*
 * @(#)file      SnmpMsgTranslator.java
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

import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpMsg;
/**
 * The translator interface is implemented by classes dealing with a specific SNMP protocol version. SnmpMsgTranslator are used in conjunction with SnmpMsgProcessingModel implementations (e.g. {@link com.sun.management.snmp.mpm.SnmpMsgProcessingModelV3  SnmpMsgProcessingModelV3}).
 *
 * @since Java DMK 5.1
 */
public interface SnmpMsgTranslator {
    /**
     * Returns the request or message Id contained in the passed message. The message is a generic one that is narrowed in the object implementing this interface.
     */
    public int getMsgId(SnmpMsg msg);
    /**
     * Returns the response max message size. The message is a generic one that is narrowed in the object implementing this interface.
     */
    public int getMsgMaxSize(SnmpMsg msg);
    /**
     * Returns the message flags. The message is a generic one that is narrowed in the object implementing this interface.
     */
    public byte getMsgFlags(SnmpMsg msg);
    /**
     * Returns the message security model. The message is a generic one that is narrowed in the object implementing this interface.
     */
    public int getMsgSecurityModel(SnmpMsg msg);
    /**
     * Returns the message security level. The message is a generic one that is narrowed in the object implementing this interface.
     */
    public int getSecurityLevel(SnmpMsg msg);
     /**
     * Returns an encoded representation of security parameters contained in the passed msg. The message is a generic one that is narrowed in the object implementing this interface.
     */
    public byte[] getFlatSecurityParameters(SnmpMsg msg);
    /**
     * Returns the message security parameters. The message is a generic one that is narrowed in the object implementing this interface.
     */
    public SnmpSecurityParameters getSecurityParameters(SnmpMsg msg);
    /**
     * Returns the message context Engine Id. The message is a generic one that is narrowed in the object implementing this interface.
     */
    public byte[] getContextEngineId(SnmpMsg msg);
    /**
     * Returns the message context name. The message is a generic one that is narrowed in the object implementing this interface.
     */
    public byte[] getContextName(SnmpMsg msg);
    /**
     * Returns the raw message context name. Raw mean as it is received from the network, without translation. It can be useful when some data are piggy backed in the context name.The message is a generic one that is narrowed in the object implementing this interface.
     */
    public byte[] getRawContextName(SnmpMsg msg);
    /**
     * Returns the message access context name. This access context name is used when dealing with access rights (e.g. community for V1/V2 or context name for V3).The message is a generic one that is narrowed in the object implementing this interface.
     */
    public byte[] getAccessContext(SnmpMsg msg);
    /**
     * Returns the message encrypted pdu or null if no encryption. The message is a generic one that is narrowed in the object implementing this interface.
     */
    public byte[] getEncryptedPdu(SnmpMsg msg);
    /**
     * Set the context name of the passed message.
     */
    public void setContextName(SnmpMsg req, byte[] contextName);
     /**
     * Set the context engine Id of the passed message.
     */
    public void setContextEngineId(SnmpMsg req, byte[] contextEngineId);
}
