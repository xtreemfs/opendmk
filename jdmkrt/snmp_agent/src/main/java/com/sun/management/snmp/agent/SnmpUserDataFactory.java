/*
 * @(#)file      SnmpUserDataFactory.java
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
 */

package com.sun.management.snmp.agent;

import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpStatusException;

/**
 * This interface is provided to enable fine customization of the SNMP
 * agent behavior. 
 * 
 * <p>You will not need to implement this interface except if your agent
 * needs extra customization requiring some contextual information.</p>
 *
 * <p>If an SnmpUserDataFactory is set on the SnmpAdaptorServer, then a new
 * object containing user-data will be allocated through this factory
 * for each incoming request. This object will be passed along to 
 * the SnmpMibAgent within SnmpMibRequest objects. By default, no 
 * SnmpUserDataFactory is set on the SnmpAdaptorServer, and the contextual
 * object passed to SnmpMibAgent is null.</p>
 * 
 * <p>You can use this feature to obtain on contextual information
 * (such as community string etc...) or to implement a caching
 * mechanism, or for whatever purpose might be required by your specific
 * agent implementation.</p>
 *
 * <p>The sequence <code>allocateUserData() / releaseUserData()</code> can 
 * also be used to implement a caching mechanism: 
 * <ul>
 * <li><code>allocateUserData()</code> could be used to allocate 
 *         some cache space,</li>
 * <li>and <code>releaseUserData()</code> could be used to flush it.</li>
 * </ul></p>
 *
 * @see com.sun.management.snmp.agent.SnmpMibRequest
 * @see com.sun.management.snmp.agent.SnmpMibAgent
 * @see com.sun.management.comm.SnmpAdaptorServer
 *
 *
 * @since Java DMK 5.1
 **/
public interface SnmpUserDataFactory {
    /**
     * Called by the <CODE>SnmpAdaptorServer</CODE> adaptor.
     * Allocate a contextual object containing some user data. This method
     * is called once for each incoming SNMP request. The scope
     * of this object will be the whole request. Since the request can be 
     * handled in several threads, the user should make sure that this
     * object can be accessed in a thread-safe manner. The SNMP framework
     * will never access this object directly - it will simply pass
     * it to the <code>SnmpMibAgent</code> within 
     * <code>SnmpMibRequest</code> objects - from where it can be retrieved
     * through the {@link com.sun.management.snmp.agent.SnmpMibRequest#getUserData() getUserData()} accessor.
     * <code>null</code> is considered to be a valid return value.
     *
     * This method is called just after the SnmpPduPacket has been
     * decoded.
     *
     * @param requestPdu The SnmpPduPacket received from the SNMP manager.
     *        <b>This parameter is owned by the SNMP framework and must be 
     *        considered as transient.</b> If you wish to keep some of its 
     *        content after this method returns (by storing it in the 
     *        returned object for instance) you should clone that 
     *        information. 
     *
     * @return A newly allocated user-data contextual object, or 
     *         <code>null</code>
     * @exception SnmpStatusException If an SnmpStatusException is thrown,
     *            the request will be aborted.
     **/
    public Object allocateUserData(SnmpPduPacket requestPdu)
	throws SnmpStatusException;

    /**
     * Called by the <CODE>SnmpAdaptorServer</CODE> adaptor.
     * Release a previously allocated contextual object containing user-data.
     * This method is called just before the responsePdu is sent back to the
     * manager. It gives the user a chance to alter the responsePdu packet
     * before it is encoded, and to free any resources that might have
     * been allocated when creating the contextual object.
     *
     * @param userData The contextual object being released. 
     * @param responsePdu The SnmpPduPacket that will be sent back to the 
     *        SNMP manager.
     *        <b>This parameter is owned by the SNMP framework and must be 
     *        considered as transient.</b> If you wish to keep some of its 
     *        content after this method returns you should clone that
     *        information. 
     *
     * @exception SnmpStatusException If an SnmpStatusException is thrown,
     *            the responsePdu is dropped and nothing is returned to
     *            to the manager.
     **/
    public void releaseUserData(Object userData, SnmpPduPacket responsePdu)
	throws SnmpStatusException;

    
    /** 
     * Called by the <CODE>SnmpV3AdaptorServer</CODE> adaptor.
     * Allocate a contextual object containing some user data. This method
     * is called once for each incoming SNMP request. The scope
     * of this object will be the whole request. Since the request can be 
     * handled in several threads, the user should make sure that this
     * object can be accessed in a thread-safe manner. The SNMP framework
     * will never access this object directly - it will simply pass
     * it to the <code>SnmpMibAgent</code> within 
     * <code>SnmpMibRequest</code> objects - from where it can be retrieved
     * through the {@link com.sun.management.snmp.agent.SnmpMibRequest#getUserData() getUserData()} accessor.
     * <code>null</code> is considered to be a valid return value.
     *
     * This method is called just after the SnmpPduPacket has been
     * decoded.
     *
     * @param requestPdu The SnmpPduPacket received from the SNMP manager.
     *        <b>This parameter is owned by the SNMP framework and must be 
     *        considered as transient.</b> If you wish to keep some of its 
     *        content after this method returns (by storing it in the 
     *        returned object for instance) you should clone that 
     *        information. 
     *
     * @return A newly allocated user-data contextual object, or 
     *         <code>null</code>
     * @exception SnmpStatusException If an SnmpStatusException is thrown,
     *            the request will be aborted.
     *
     **/
    public Object allocateUserData(SnmpPdu requestPdu)
	throws SnmpStatusException;
    
    /**
     * Called by the <CODE>SnmpV3AdaptorServer</CODE> adaptor.
     * Release a previously allocated contextual object containing user-data.
     * This method is called just before the responsePdu is sent back to the
     * manager. It gives the user a chance to alter the responsePdu packet
     * before it is encoded, and to free any resources that might have
     * been allocated when creating the contextual object.
     *
     * @param userData The contextual object being released. 
     * @param responsePdu The SnmpPduPacket that will be sent back to the 
     *        SNMP manager.
     *        <b>This parameter is owned by the SNMP framework and must be 
     *        considered as transient.</b> If you wish to keep some of its 
     *        content after this method returns you should clone that
     *        information. 
     *
     * @exception SnmpStatusException If an SnmpStatusException is thrown,
     *            the responsePdu is dropped and nothing is returned to
     *            to the manager.
     *
     **/
    public void releaseUserData(Object userData, SnmpPdu responsePdu)
	throws SnmpStatusException;
}
