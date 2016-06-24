/*
 * @(#)JdmkEngineFactory.java	1.14
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
package com.sun.management.snmp;

import com.sun.management.internal.snmp.SnmpAgentEngineFactory;

import com.sun.management.snmp.InetAddressAcl;
import com.sun.management.snmp.SnmpEngineFactory;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngineParameters;
/**
 * This <CODE>SnmpEngineFactory</CODE> instantiates an <CODE>SnmpEngine</CODE> containing :
 * <ul> 
 * <li> Message Processing Sub System + V1, V2 et V3 Message Processing Models</li>
 * <li> Security Sub System + User based Security Model (Id 3)</li>
 * <li> Access Control Sub System + Ip Acl + User based Access Control Model. See <CODE> IpAcl </CODE> and <CODE> UserAcl </CODE>.</li>
 * </ul>
 * 
 *
 * @since Java DMK 5.1
 */
public class JdmkEngineFactory implements SnmpEngineFactory {
    SnmpAgentEngineFactory fact = new SnmpAgentEngineFactory();
    /**
     * The engine instantiation method.
     * @throws IllegalArgumentException Thrown if one of the configuration 
     *    file file doesn't exist (User Acl file, security file).
     * @param p The parameters used to instantiate a new engine.
     * @return The newly created SnmpEngine.
     */
    public SnmpEngine createEngine(SnmpEngineParameters p) {
	return fact.createEngine(p);
    }

    /**
     * The engine instantiation method. This method is called by 
     * <CODE>SnmpV3AdaptorServer</CODE>. 
     * @throws IllegalArgumentException Thrown if one of the configuration
     *   file file doesn't exist (User Acl file, security file).
     * @param p The parameters used to instantiate a new engine.
     * @param ipacl The Ip ACL to pass to the Access Control Model.
     * @return The newly created SnmpEngine.
     */
    public SnmpEngine createEngine(SnmpEngineParameters p,
				   InetAddressAcl ipacl) {
	return fact.createEngine(p, ipacl);
    }
}
