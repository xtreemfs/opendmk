/*
 * @(#)file      SnmpAccessControlSubSysImpl.java
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

import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpUnknownModelException;
import com.sun.management.snmp.SnmpEngine;

import com.sun.management.snmp.SnmpUnknownAccContrModelException;


/**
 * Implements the <CODE>SnmpAccessControlSubSystem</CODE> interface.
 * <BR>PUBLIC FOR INTERNAL USE ONLY. JAVADOC WILL BE NOT DELIVERED.
 *
 * @since Java DMK 5.1
 */
public class SnmpAccessControlSubSysImpl extends SnmpSubSystemImpl 
    implements SnmpAccessControlSubSystem {

    public SnmpAccessControlSubSysImpl(SnmpEngine engine) {
	super(engine);
    }
    /**
     * See <CODE>SnmpAccessControlSubSystem</CODE> interface doc.
     */
    public void checkPduAccess(int version,
			       String principal,
			       int securityLevel,
			       int pduType,
			       int securityModel,
			       byte[] contextName,
			       SnmpPdu pdu) throws SnmpStatusException, SnmpUnknownAccContrModelException {
	
	SnmpAccessControlModel model = null;
	try{
	    model = (SnmpAccessControlModel) getModel(version);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownAccContrModelException("Unknown model: " + e);
	}

	model.checkPduAccess(version,
			     principal,
			     securityLevel,
			     pduType,
			     securityModel,
			     contextName,
			     pdu);
    }
    /**
     * See <CODE>SnmpAccessControlSubSystem</CODE> interface doc.
     */
    public void checkAccess(int version,
			    String principal,
			    int securityLevel,
			    int pduType,
			    int securityModel,
			    byte[] contextName,
			    SnmpOid oid) throws SnmpStatusException, SnmpUnknownAccContrModelException {
	SnmpAccessControlModel model = null;
	try{
	    model = (SnmpAccessControlModel) getModel(version);
	}catch(SnmpUnknownModelException e) {
	    throw new SnmpUnknownAccContrModelException("Unknown model: " + e);
	}

	model.checkAccess(version,
			  principal,
			  securityLevel,
			  pduType,
			  securityModel,
			  contextName,
			  oid);
    }
}
