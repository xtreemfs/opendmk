/*
 * @(#)file      UsmUserMetaImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.11
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
package com.sun.management.snmp.usm.usmmib;

import javax.management.MBeanServer;

import com.sun.management.snmp.agent.SnmpMib;
import com.sun.management.snmp.agent.SnmpStandardObjectServer;
/**
 * The UsmUser group meta has been customized in order to instantiate our Table Meta.
 *
 * @since Java DMK 5.1
 */
class UsmUserMetaImpl extends UsmUserMeta {
    private static final long serialVersionUID = -1865339483576247633L;
    
    //Constructor.
    public UsmUserMetaImpl(SnmpMib myMib, SnmpStandardObjectServer objserv) {
	super(myMib, objserv);
    }

    //Instantiate our table meta.
    protected UsmUserTableMeta createUsmUserTableMetaNode(String tableName, 
							  String groupName, 
							  SnmpMib mib, 
							  MBeanServer server) {
        return new UsmUserTableMetaImpl(mib, objectserver);
    }
}   
