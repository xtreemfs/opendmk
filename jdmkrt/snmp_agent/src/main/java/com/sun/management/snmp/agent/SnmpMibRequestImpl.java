/*
 * @(#)file      SnmpMibRequestImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.15
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


import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpEngine;

/**
 * This class implements the SnmpMibRequest interface.
 * It represents the part of a SNMP request that involves a specific
 * MIB. One instance of this class will be created for every MIB 
 * involved in a SNMP request, and will be passed to the SnmpMibAgent
 * in charge of handling that MIB.
 * 
 * Instances of this class are allocated by the SNMP engine. You will
 * never need to use this class directly. You will only access
 * instances of this class through their SnmpMibRequest interface.
 *
 *
 * @since Java DMK 5.1
 */
final class SnmpMibRequestImpl implements SnmpMibRequest {
    /**
     * @param vblist The vector of SnmpVarBind objects in which the
     *        MIB concerned by this request is involved.
     * @param protocolVersion  The protocol version of the SNMP request.
     * @param userData     User allocated contextual data. This object must
     *        be allocated on a per SNMP request basis through the 
     *        SnmpUserDataFactory registered with the SnmpAdaptorServer,
     *        and is handed back to the user through SnmpMibRequest objects.
     * @deprecated
     */
    public SnmpMibRequestImpl(SnmpPdu reqPdu,
			      Vector vblist, 
			      int protocolVersion, 
			      Object userData) {
	
	varbinds = vblist;
	version  = protocolVersion;
	data     = userData;
	this.reqPdu = reqPdu;
    }
    
    /**
     * @param engine The local engine.
     * @param reqPdu The received pdu.
     * @param vblist The vector of SnmpVarBind objects in which the
     *        MIB concerned by this request is involved.
     * @param protocolVersion  The protocol version of the SNMP request.
     * @param userData     User allocated contextual data. This object must
     *        be allocated on a per SNMP request basis through the 
     *        SnmpUserDataFactory registered with the SnmpAdaptorServer,
     *        and is handed back to the user through SnmpMibRequest objects.
     */
    public SnmpMibRequestImpl(SnmpEngine engine,
			      SnmpPdu reqPdu,
			      Vector vblist, 
			      int protocolVersion, 
			      Object userData,
			      String principal,
			      int securityLevel,
			      int securityModel,
			      byte[] contextName,
			      byte[] accessContextName) {
	varbinds   = vblist;
	version    = protocolVersion;
	data       = userData;
	this.reqPdu = reqPdu;
	this.engine = engine;
	this.principal = principal;
	this.securityLevel = securityLevel;
	this.securityModel = securityModel;
	this.contextName = contextName;
	this.accessContextName = accessContextName;
    }
    // -------------------------------------------------------------------
    // PUBLIC METHODS from SnmpMibRequest
    // -------------------------------------------------------------------
    
    /**
     * Returns the local engine. This parameter is returned only if <CODE> SnmpV3AdaptorServer </CODE> is the adaptor receiving this request. Otherwise null is returned.
     * @return the local engine.
     */
    public SnmpEngine getEngine() {
	return engine;
    }
    
    /**
     * Gets the incoming request principal. This parameter is returned only if <CODE> SnmpV3AdaptorServer </CODE> is the adaptor receiving this request. Otherwise null is returned.
     * @return The request principal.
     **/
    public String getPrincipal() {
	return principal;
    }
    
    /**
     * Gets the incoming request security level. This level is defined in {@link com.sun.management.snmp.SnmpEngine SnmpEngine}. This parameter is returned only if <CODE> SnmpV3AdaptorServer </CODE> is the adaptor receiving this request. Otherwise -1 is returned.
     * @return The security level.
     */
    public int getSecurityLevel() {
	return securityLevel;
    }
    /**
     * Gets the incoming request security model. This parameter is returned only if <CODE> SnmpV3AdaptorServer </CODE> is the adaptor receiving this request. Otherwise -1 is returned.
     * @return The security model.
     */
    public int getSecurityModel() {
	return securityModel;
    }
    /**
     * Gets the incoming request context name. This parameter is returned only if <CODE> SnmpV3AdaptorServer </CODE> is the adaptor receiving this request. Otherwise null is returned.
     * @return The context name.
     */
    public byte[] getContextName() {
	return contextName;
    }
    
    /**
     * Gets the incoming request context name used by Access Control Model in order to allow or deny the access to OIDs. This parameter is returned only if <CODE> SnmpV3AdaptorServer </CODE> is the adaptor receiving this request. Otherwise null is returned.
     * @return The checked context.
     */
    public byte[] getAccessContextName() {
	return accessContextName;
    }
    
    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    public final SnmpPdu getPdu() {
	return reqPdu;
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    public final Enumeration getElements()  {return varbinds.elements();}

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    public final Vector getSubList()  {return varbinds;}

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    public final int getSize()  {
	if (varbinds == null) return 0;
	return varbinds.size();
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    public final int         getVersion()  {return version;}

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    public final int         getRequestPduVersion()  {return reqPdu.version;}

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    public final Object      getUserData() {return data;}

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    public final int getVarIndex(SnmpVarBind varbind) {
	return varbinds.indexOf(varbind);
    }

    // -------------------------------------------------------------------
    // Implements the method defined in SnmpMibRequest interface.
    // See SnmpMibRequest for the java doc.
    // -------------------------------------------------------------------
    public void addVarBind(SnmpVarBind varbind) {
	varbinds.addElement(varbind);
    }

    // -------------------------------------------------------------------
    // PACKAGE METHODS
    // -------------------------------------------------------------------

    // -------------------------------------------------------------------
    // Allow to pass the request tree built during the check() phase
    // to the set() method. Note: the if the tree is `null', then the
    // set() method will rebuild a new tree identical to the tree built
    // in the check() method.
    //
    // Passing this tree in the SnmpMibRequestImpl object allows to 
    // optimize the SET requests.
    //
    // -------------------------------------------------------------------
    final void setRequestTree(SnmpRequestTree tree) {this.tree = tree;}

    // -------------------------------------------------------------------
    // Returns the SnmpRequestTree object built in the first operation 
    // phase for two-phase SNMP requests (like SET).
    // -------------------------------------------------------------------
    final SnmpRequestTree getRequestTree() {return tree;}

    // -------------------------------------------------------------------
    // Returns the underlying vector of SNMP varbinds (used for algorithm
    // optimization).
    // -------------------------------------------------------------------
    final Vector getVarbinds() {return varbinds;}
 
    // -------------------------------------------------------------------
    // Private variables
    // -------------------------------------------------------------------

    // Ideally these variables should be declared final but it makes
    // the jdk1.1.x compiler complain (seems to be a compiler bug, jdk1.2
    // is OK).
    private Vector varbinds;
    private int    version;
    private Object data;
    private SnmpPdu reqPdu = null;
    // Non final variable.
    private SnmpRequestTree tree = null;
    private SnmpEngine engine = null;
    private String principal = null;
    private int securityLevel = -1;
    private int securityModel = -1;
    private byte[] contextName = null;
    private byte[] accessContextName = null;
}
