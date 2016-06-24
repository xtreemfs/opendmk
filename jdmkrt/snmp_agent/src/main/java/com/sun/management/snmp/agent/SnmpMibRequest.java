/* 
 * @(#)file      SnmpMibRequest.java 
 * @(#)author    Sun Microsystems, Inc. 
 * @(#)version   1.20 
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
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpEngine;

/**
 * This interface models the part of a SNMP request that involves 
 * a specific MIB. One object implementing this interface will be created 
 * for every MIB involved in a SNMP request, and that object will be passed
 * to the SnmpMibAgent in charge of handling that MIB.
 * 
 * Objects implementing this interface will be allocated by the SNMP engine. 
 * You will never need to implement this interface. You will only use it.
 *
 *
 * @since Java DMK 5.1
 */
public interface SnmpMibRequest {
    /**
     * Returns the list of varbind to be handled by the SNMP mib node.
     *
     * @return The element of the enumeration are instances of 
     *         {@link com.sun.management.snmp.SnmpVarBind} 
     */
    public Enumeration getElements();

    /**
     * Returns the vector of varbind to be handled by the SNMP mib node.
     * The caller shall not modify this vector.
     *
     * @return The element of the vector are instances of 
     *         {@link com.sun.management.snmp.SnmpVarBind}
     */
    public Vector getSubList();

    /**
     * Returns the SNMP protocol version in which this request is expected
     * to be handled by the {@link 
     * com.sun.management.snmp.agent.SnmpMibAgent SnmpMibAgent}. 
     * If the received PDU was an SNMPv1 request, the SNMP 
     * adaptor may require the SnmpMibAgent to process this request as if
     * it were originally received in SNMPv2. <br>
     * In this case, <code>getVersion()</code> will return 
     * {@link com.sun.management.snmp.SnmpDefinitions#snmpVersionTwo 
     * SnmpDefinitions.snmpVersionTwo} even though the original PDU version
     * is SNMPv1.
     * <p>See also {@link #getRequestPduVersion}.
     * @return The SNMP protocol version in which the request is expected
     *         to be handled.
     *
     **/
    public int getVersion();

    /**
     * Returns the SNMP protocol version of the original request. 
     * No translation is done on the version. 
     * The actual received request SNMP version is returned.
     * <p>See also {@link #getVersion}.
     *
     * @return The SNMP protocol version of the original request.
     *
     */
    public int getRequestPduVersion();

    /**
     * Returns the local engine. This parameter is returned only if 
     * <CODE> SnmpV3AdaptorServer </CODE> is the adaptor receiving 
     * this request. Otherwise null is returned.
     * @return the local engine.
     *
     */
    public SnmpEngine getEngine();

    /**
     * Gets the incoming request principal. This parameter is returned 
     * only if <CODE> SnmpV3AdaptorServer </CODE> is the adaptor receiving 
     * this request. Otherwise null is returned.
     * @return The request principal, or <code>null</code>.
     *
     **/
    public String getPrincipal();

    /**
     * Gets the incoming request security level. 
     * This level is defined in {@link
     * com.sun.management.snmp.SnmpEngine SnmpEngine}. 
     * This parameter is returned only if <CODE>SnmpV3AdaptorServer</CODE> 
     * is the adaptor receiving this request. Otherwise -1 is returned.
     * @return The security level, or <code>-1</code>.
     *
     */
    public int getSecurityLevel();

    /**
     * Gets the incoming request security model. This parameter is 
     * returned only if <CODE>SnmpV3AdaptorServer</CODE> is the adaptor 
     * receiving this request. Otherwise -1 is returned.
     * @return The security model, or <code>-1</code>.
     *
     */
    public int getSecurityModel();

    /**
     * Gets the incoming request context name. This parameter is 
     * returned only if <CODE>SnmpV3AdaptorServer</CODE> is the adaptor 
     * receiving this request. Otherwise null is returned.
     * @return The context name, or <code>null</code>.
     *
     */
    public byte[] getContextName();

    /**
     * Gets the incoming request context name used by Access Control 
     * Model in order to allow or deny the access to OIDs. 
     * This parameter is returned only if <CODE>SnmpV3AdaptorServer</CODE> 
     * is the adaptor receiving this request. Otherwise null is returned.
     * @return The checked context name, or <code>null</code>.
     *
     */
    public byte[] getAccessContextName();
    
    /**
     * Returns a handle on a user allocated contextual object.
     * This contextual object is allocated through the SnmpUserDataFactory
     * on a per SNMP request basis, and is handed back to the user via
     * SnmpMibRequest (and derivative) objects. It is never accessed by
     * the system, but might be handed back in multiple threads. It is thus 
     * the user responsibility to make sure he handles this object in a
     * thread safe manner.
     * @return A handle on a user allocated contextual object.
     */
    public Object getUserData();

    /**
     * Returns the varbind index that should be embedded in an
     * SnmpStatusException for this particular varbind.
     * This does not necessarily correspond to the "real"
     * index value that will be returned in the result PDU.
     *
     * @param varbind The varbind for which the index value is
     *        queried. Note that this varbind <b>must</b> have 
     *        been obtained from the enumeration returned by
     *        <CODE>getElements()</CODE>, or from the vector
     *        returned by <CODE>getSublist()</CODE>.
     *
     * @return The varbind index that should be embedded in an
     *         SnmpStatusException for this particular varbind.
     */
    public int getVarIndex(SnmpVarBind varbind);

    /**
     * Adds a varbind to this request sublist. This method is used for
     * internal purposes and you should never need to call it directly.
     *
     * @param varbind The varbind to be added in the sublist.
     *
     */
    public void addVarBind(SnmpVarBind varbind);


    /**
     * Returns the number of elements (varbinds) in this request sublist.
     *
     * @return The number of elements in the sublist.
     *
     **/
    public int getSize();

    /** 
     * Returns the SNMP PDU attached to the request. 
     * @return The SNMP PDU.
     *
     **/
    public SnmpPdu getPdu();
}
