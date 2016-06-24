/*
 * @(#)file      SnmpUsmParameters.java
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
package com.sun.management.snmp.manager.usm;

import com.sun.management.internal.snmp.SnmpEngineImpl;
import com.sun.management.snmp.usm.SnmpUsm;
import com.sun.management.snmp.usm.SnmpUsmSecurityParameters;

import com.sun.management.snmp.manager.*;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpUnknownModelException;

/**
 * This class models the set of parameters that are needed when making an SNMP request to an SNMP engine using the User Based Security Model.
 * When a <CODE> SnmpUsmParameters </CODE> is instantiated, the security level default value is <CODE> noAuthNoPriv </CODE>.
 *
 * @since Java DMK 5.1
 */
public class SnmpUsmParameters extends SnmpV3Parameters {
    /**
     * Package constructor.
     * @param engine The engine.
     * @param principal The user name that will be used when sending calls.
     * @exception SnmpUnknownModelException If USM is not present in the passed engine.
     */
    public SnmpUsmParameters(SnmpEngine engine,
                             String principal) throws SnmpUnknownModelException {
        SnmpUsmSecurityParameters params = init(engine);
        params.setUserName(principal);
    }
    /**
     * Package constructor.
     * @param engine The engine.
     * @exception SnmpUnknownModelException If USM is not present in the passed engine.
     */
    public SnmpUsmParameters(SnmpEngine engine) throws SnmpUnknownModelException {
        init(engine);
    }
    /**
     * Returns the associated principal.
     * @return The principal.
     */
    public String getPrincipal() {
        return ((SnmpUsmSecurityParameters)getSecurityParameters()).getUserName();
    }

    /**
     * Set the associated principal.
     * @param principal The principal.
     */
    public void setPrincipal(String principal) {
        ((SnmpUsmSecurityParameters)getSecurityParameters()).setUserName(principal);
    }

    void setEngineId(SnmpEngineId e) {
        ((SnmpUsmSecurityParameters)getSecurityParameters()).setAuthoritativeEngineId(e);
    }

    private SnmpUsmSecurityParameters init(SnmpEngine eng) throws SnmpUnknownModelException {
        SnmpEngineImpl engine = (SnmpEngineImpl) eng;
        SnmpUsm model = (SnmpUsm) engine.getSecuritySubSystem().getModel(SnmpUsm.ID);
        SnmpUsmSecurityParameters securParams =
            model.createUsmSecurityParameters();
        setSecurityParameters(securParams);
        setMsgSecurityModel(SnmpUsm.ID);
        return securParams;
    }
}
