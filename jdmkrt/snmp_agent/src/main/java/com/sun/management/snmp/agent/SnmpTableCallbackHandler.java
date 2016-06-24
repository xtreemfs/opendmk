/* 
 * @(#)file      SnmpTableCallbackHandler.java 
 * @(#)author    Sun Microsystems, Inc. 
 * @(#)version   1.7 
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
 */ 
package com.sun.management.snmp.agent;

import javax.management.ObjectName;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.agent.SnmpMibTable;

/**
 * This interface ensures the synchronization between Metadata table objects
 * and bean-like table objects.
 *
 * It is used between mibgen generated table meta and table classes.
 * <p><b><i>
 * You should never need to use this interface directly.
 * </p></b></i>
 *
 *
 * @since Java DMK 5.1
 **/
public interface SnmpTableCallbackHandler {
    /**
     * This method is called by the SNMP runtime after a new entry
     * has been added to the table.
     *
     * If an SnmpStatusException is raised, the entry will be removed
     * and the operation will be aborted. In this case, the removeEntryCb()
     * callback will not be called.
     *
     * <p><b><i>
     * You should never need to use this method directly.
     * </p></b></i>
     *
     **/
    public void addEntryCb(int pos, SnmpOid row, ObjectName name, 
			   Object entry, SnmpMibTable meta) 
	throws SnmpStatusException;

    /**
     * This method is called by the SNMP runtime after a new entry
     * has been removed from the table.
     *
     * If raised, SnmpStatusException will be ignored.
     *
     * <p><b><i>
     * You should never need to use this method directly.
     * </p></b></i>
     *
     **/
    public void removeEntryCb(int pos, SnmpOid row, ObjectName name, 
			      Object entry, SnmpMibTable meta)
	throws SnmpStatusException;
}
