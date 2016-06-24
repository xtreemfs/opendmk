/*
 * @(#)file      SnmpMibSubRequest.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.14
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

import java.util.Enumeration;
import java.util.Vector;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpOid;
// import com.sun.management.snmp.SnmpIndex;

/**
 * This interface models an SNMP sub request to be performed on a specific 
 * SNMP MIB node. The node involved can be either an SNMP group, an SNMP table,
 * or an SNMP table entry (conceptual row). The conceptual row may or may not 
 * already exist. If the row did not exist at the time when the request
 * was received, the <CODE>isNewEntry()</CODE> method will return <CODE>
 * true</CODE>. 
 * <p>
 * Objects implementing this interface will be allocated by the SNMP engine. 
 * You will never need to implement this interface. You will only use it. 
 * </p>
 *
 * @since Java DMK 5.1
 */
public interface SnmpMibSubRequest extends SnmpMibRequest {
    /**
     * Return the list of varbind to be handled by the SNMP MIB node.
     * <p>
     * <b>Note:</b> <ul>
     * <i>In case of SET operation, if this node is a table row which 
     * contains a control variable (as identified by the table's 
     * isRowStatus() method) the control variable will not
     * be included in this list: it will be obtained by calling
     * getRowStatusVarBind(). This will allow you to handle the control
     * variable specifically.</i><br>
     * You will never need to worry about this unless you need to
     * implement a non standard mechanism for handling row
     * creation and deletion.
     * </ul>
     * <p>
     * @return The elements of the enumeration are instances of 
     *         {@link com.sun.management.snmp.SnmpVarBind} 
     */
    public Enumeration getElements();

    /**
     * Return the list of varbind to be handled by the SNMP MIB node.
     * <p>
     * <b>Note:</b> <ul>
     * <i>In case of SET operation, if this node is a table row which 
     * contains a control variable (as identified by the table's 
     * isRowStatus() method) the control variable will not
     * be included in this list: it will be obtained by calling
     * getRowStatusVarBind(). This will allow you to handle the control
     * variable specifically.</i><br>
     * You will never need to worry about this unless you need to
     * implement a non standard mechanism for handling row
     * creation and deletion.
     * </ul>
     * <p>
     * @return The elements of the vector are instances of 
     *         {@link com.sun.management.snmp.SnmpVarBind} 
     */
    public Vector getSubList();

    /**
     * Return the part of the OID identifying the table entry involved.
     * <p>
     *
     * @return {@link com.sun.management.snmp.SnmpOid} or <CODE>null</CODE>
     *         if the request is not directed to an entry.
     */
    public SnmpOid     getEntryOid();

    /**
     * Indicate whether the entry involved is a new entry.
     * This method will return <CODE>true</CODE> if the entry was not
     * found when the request was processed. As a consequence, <CODE>
     * true</CODE> means that either the entry does not exist yet,
     * or it has been created while processing this request.
     * The result of this method is only significant when an entry
     * is involved.
     *
     * <p>
     * @return <CODE>true</CODE> If the entry did not exist, 
     *  or <CODE>false</CODE> if the entry involved was found.
     */
    public boolean     isNewEntry();

    /**
     * Return the varbind that holds the RowStatus variable.
     * It corresponds to the varbind that was identified by 
     * the <code>isRowStatus()</code> method generated by mibgen
     * on {@link com.sun.management.snmp.agent.SnmpMibTable} derivatives.
     * <ul><li>In SMIv2, it is the varbind which contains the columnar 
     *         object implementing the RowStatus TEXTUAL-CONVENTION.</li>
     *      <li>In SMIv1 nothing special is generated</li>
     *      <ul>You may however subclass the generated table metadata
     *          class in order to provide your own implementation of
     *          isRowStatus(), getRowAction(), isRowReady() and 
     *          setRowStatus()
     *          (see  {@link com.sun.management.snmp.agent.SnmpMibTable}).</ul>
     * </ul>
     * <p>
     * @return a varbind that serves to control the table modification.
     *         <code>null</code> means that no such varbind could be
     *         identified.<br>
     *         <b>Note:</b><i>The runtime will only try to identify
     *         the RowStatus varbind when processing an
     *         SNMP SET request. In this case, the identified
     *         varbind will not be included in the set of varbinds 
     *         returned by getSubList() and getElements().
     *         </i>
     *                 
     **/
    public SnmpVarBind getRowStatusVarBind();

    /**
     * This method should be called when a status exception needs to
     * be raised for a given varbind of an SNMP GET request. This method
     * performs all the necessary conversions (SNMPv1 <=> SNMPv2) and
     * propagates the exception if needed:
     * If the version is SNMP v1, the exception is propagated.
     * If the version is SNMP v2, the exception is stored in the varbind.
     * This method also takes care of setting the correct value of the
     * index field.
     * <p>
     *
     * @param varbind The varbind for which the exception is
     *        registered. Note that this varbind <b>must</b> have 
     *        been obtained from the enumeration returned by
     *        <CODE>getElements()</CODE>, or from the vector
     *        returned by <CODE>getSubList()</CODE>
     *
     * @param exception The exception to be registered for the given varbind.
     *
     */
    public void registerGetException(SnmpVarBind varbind,
				     SnmpStatusException exception)
	throws SnmpStatusException;

    /**
     * This method should be called when a status exception needs to
     * be raised for a given varbind of an SNMP SET request. This method
     * performs all the necessary conversions (SNMPv1 <=> SNMPv2) and
     * propagates the exception if needed.
     * This method also takes care of setting the correct value of the
     * index field.
     * <p>
     *
     * @param varbind The varbind for which the exception is
     *        registered. Note that this varbind <b>must</b> have 
     *        been obtained from the enumeration returned by
     *        <CODE>getElements()</CODE>, or from the vector
     *        returned by <CODE>getSubList()</CODE>
     *
     * @param exception The exception to be registered for the given varbind.
     *
     */
    public void registerSetException(SnmpVarBind varbind,
				     SnmpStatusException exception)
	throws SnmpStatusException;

    /**
     * This method should be called when a status exception needs to
     * be raised when checking a given varbind for an SNMP SET request. 
     * This method performs all the necessary conversions (SNMPv1 <=> 
     * SNMPv2) and propagates the exception if needed.
     * This method also takes care of setting the correct value of the
     * index field.
     * <p>
     *
     * @param varbind The varbind for which the exception is
     *        registered. Note that this varbind <b>must</b> have 
     *        been obtained from the enumeration returned by
     *        <CODE>getElements()</CODE>, or from the vector
     *        returned by <CODE>getSubList()</CODE>
     *
     * @param exception The exception to be registered for the given varbind.
     *
     */
    public void registerCheckException(SnmpVarBind varbind,
				       SnmpStatusException exception)
	throws SnmpStatusException;
}


