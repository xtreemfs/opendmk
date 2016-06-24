/*
 * @(#)file      SnmpAgentEngineFactory.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.37
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

import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineParameters;
import com.sun.management.snmp.UserAcl;
import com.sun.management.snmp.SnmpUnknownModelException;

import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.internal.snmp.SnmpBaseEngineFactory;

import com.sun.management.snmp.uacl.JdmkUserAcl;
import com.sun.management.internal.snmp.SnmpJdmkAcm;
import com.sun.management.internal.snmp.SnmpAccessControlSubSysImpl;
import com.sun.management.internal.snmp.SnmpAccessControlSubSystem;
import com.sun.management.internal.snmp.SnmpAccessControlModel;

import com.sun.management.snmp.InetAddressAcl;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpOid;
/**
 * This factory is the default one used by an 
 * <CODE>SnmpV3AdaptorServer</CODE>. It is a specialized 
 * <CODE>SnmpEngineFactory</CODE>. It adds the access control level to 
 * the engine. This factory is dedicated to the agent side.
 *
 * @since Java DMK 5.1
 */
public class SnmpAgentEngineFactory extends SnmpBaseEngineFactory {
    /**
     * Do not call this method directly. The engine instantiation method. 
     * Calling more than once this method will return the same 
     * <CODE>SnmpEngine</CODE> instance.
     * @throws IllegalArgumentException Thrown if one of the configuration 
     *     file file doesn't exist (Acl files, lcd file).
     * @param parameters The engine parameters to use.
     * @return The newly created SnmpEngine.
     */
    public SnmpEngine createEngine(SnmpEngineParameters parameters) 
	throws IllegalArgumentException {
	SnmpEngineImpl engine = (SnmpEngineImpl) 
	    super.createEngine(parameters);
	return init(parameters, engine, null);
    }
    /**
     * Do not call this method directly. The engine instantiation method. 
     * Calling more than once this method will return the same 
     * <CODE>SnmpEngine</CODE> instance.
     * @throws IllegalArgumentException Thrown if one of the configuration 
     * file file doesn't exist (Acl files, lcd file).
     * @param parameters The engine parameters to use.
     * @param acl The ACL for the new engine.
     * @return The newly created SnmpEngine.
     */
    public SnmpEngine createEngine(SnmpEngineParameters parameters, 
				   InetAddressAcl acl) 
	throws IllegalArgumentException {
	SnmpEngineImpl engine = (SnmpEngineImpl) 
	    super.createEngine(parameters, acl);
	return init(parameters, engine, (InetAddressAcl) acl);
    }

    /**
     * This method is called by the factory when creating the access control 
     * sub system.
     * @param engine The SNMP engine.
     * @return The access control sub system.
     */
    protected SnmpAccessControlSubSystem 
	createSnmpAccessControlSubSystem(SnmpEngine engine) {
	return new SnmpAccessControlSubSysImpl(engine);
    }

     /**
     * This method is called by the factory when creating the access control 
     * model. The created model is responsible for registering in the passed 
     * sub system.
     * @param parameters The engine parameters.
     * @param acl The ACL.
     * @param engine The engine.
     * @param subsys The security sub system.
     * @exception IllegalArgumentException if the specified configuration 
     *  file doesn't exit.
     * @return The access control model.
     */
    protected SnmpAccessControlModel 
	createSnmpAccessControlModel(SnmpEngineParameters parameters, 
				     InetAddressAcl acl, SnmpEngine engine, 
				     SnmpAccessControlSubSystem subsys)
	throws IllegalArgumentException {
	UserAcl uacl = parameters.getUserAcl();
	if(parameters.getUserAcl() == null)
	    uacl = new JdmkUserAcl("Snmp Engine Acm V3 User ACL");
	
	SnmpJdmkAcm acm = new SnmpJdmkAcm(subsys,
					  acl,
					  uacl,
					  true);
	return acm;
    }

    //Common initialization
    private SnmpEngine init(SnmpEngineParameters parameters,
			    SnmpEngineImpl engine,
			    InetAddressAcl ipacl) {
	
	SnmpAccessControlSubSystem subsys = 
	    createSnmpAccessControlSubSystem(engine);
	
	SnmpAccessControlModel acm = createSnmpAccessControlModel(parameters,
								  ipacl,
								  engine,
								  subsys);
	engine.setAccessControlSubSystem(subsys);
	
	return engine;
    }

    String dbgTag = "SnmpAgentEngineFactory";

}
