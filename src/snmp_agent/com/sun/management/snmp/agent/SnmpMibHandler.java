/*
 * @(#)file      SnmpMibHandler.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.26
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


package com.sun.management.snmp.agent;



// java imports
//
import java.util.Vector;
import java.io.IOException;

// jmx imports
//
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpStatusException;

/**
 * The logical link between an SNMP MIB and the SNMP communication stack.
 * This interface is for INTERNAL USE ONLY, don't use it.
 *
 * @since Java DMK 5.1
 */

public interface SnmpMibHandler {
    
    /**
     * Adds a new MIB in the SNMP MIB handler. 
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler)} and 
     * {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName)} and should not be called directly.
     * 
     * @param mib The MIB to add.
     * 
     * @return A reference on the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib) 
	throws IllegalArgumentException;

    /**
     * Adds a new MIB in the SNMP MIB handler.
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler, SnmpOid[])} and 
     * {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName, SnmpOid[])} and should not be called directly.
     *
     * @param mib The MIB to add.
     * @param oids The array of oid used to add the mib. Each oid is a root oid for the mib.
     * @return A reference on the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     *
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, SnmpOid[] oids) 
	throws IllegalArgumentException;
    
    /**
     * Adds a new contextualized MIB in the SNMP MIB handler.
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler, String)} and 
     * {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName, String)} and should not be called directly.
     *
     * @param mib The MIB to add.
     * @param contextName The MIB context name. If null is passed, will be registered in the default context.
     * 
     * @return A reference to the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     *
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, String contextName) 
        throws IllegalArgumentException;
    
    /**
     * Adds a new contextualized MIB in the SNMP MIB handler.
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler, String, SnmpOid[])} and 
     * {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName, String, SnmpOid[])} and should not be called directly.
     * 
     * @param mib The MIB to add.
     * @param contextName The MIB context name. If null is passed, will be registered in the default context.
     * @param oids The array of oid used to add the mib. Each oid is a root oid for the mib.
     * 
     * @return A reference to the SNMP MIB handler. 
     *
     * @exception IllegalArgumentException If the parameter is null.
     *
     */
    public SnmpMibHandler addMib(SnmpMibAgent mib, 
				 String contextName, 
				 SnmpOid[] oids) 
        throws IllegalArgumentException;

    /**
     * Removes the specified MIB from the SNMP protocol adaptor. 
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler)} and 
     * {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName)} and should not be called directly.
     * 
     * @param mib The MIB to be removed.
     *
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> was a MIB included in the SNMP MIB handler, 
     * <CODE>false</CODE> otherwise.
     */
    public boolean removeMib(SnmpMibAgent mib);
    
    /**
     * Removes the specified MIB from the SNMP protocol adaptor. 
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler)} and 
     * {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName)} and should not be called directly.
     * 
     * @param mib The MIB to be removed.
     * @param oids The oid the MIB was previously registered for. 
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> was a MIB included in the SNMP MIB handler, 
     * <CODE>false</CODE> otherwise.
     *
     */
    public boolean removeMib(SnmpMibAgent mib, SnmpOid[] oids);
    
    /**
     * Removes the specified MIB from the SNMP protocol adaptor.
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler, String)} and 
     * {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName, String)} and should not be called directly.
     * 
     * @param mib The MIB to be removed.
     * @param contextName The context name used at registration time.
     *
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> was a MIB included in the SNMP MIB handler, 
     * <CODE>false</CODE> otherwise.
     *
     */
    public boolean removeMib(SnmpMibAgent mib, String contextName);

     /**
     * Removes the specified MIB from the SNMP protocol adaptor. 
     * This method is called automatically by {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptor(SnmpMibHandler, String, SnmpOid[])} and 
     * {@link com.sun.management.snmp.agent.SnmpMibAgent#setSnmpAdaptorName(ObjectName, String, SnmpOid[])} and should not be called directly.
     * 
     * @param mib The MIB to be removed.
     * @param contextName The context name used at registration time.
     * @param oids The oid the MIB was previously registered for. 
     * @return <CODE>true</CODE> if the specified <CODE>mib</CODE> was a MIB included in the SNMP MIB handler, 
     * <CODE>false</CODE> otherwise.
     *
     */
    public boolean removeMib(SnmpMibAgent mib, String contextName, SnmpOid[] oids);
}
