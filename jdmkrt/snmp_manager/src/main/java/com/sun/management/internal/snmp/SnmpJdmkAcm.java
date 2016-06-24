/*
 * @(#)file      SnmpJdmkAcm.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.28
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

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.sun.management.snmp.InetAddressAcl;
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.UserAcl;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpPdu;
/**
 * Access Control Model implementation. It handles V1 and V2 <CODE>Jdmk</CODE> IP ACL as well as V3 user based ACL.
 * This model is added to the engine by {@link com.sun.management.internal.snmp.SnmpAgentEngineFactory SnmpAgentEngineFactory}
 *<P> This model is registered within the Access Control Sub System with the following ID:
 * <ul>
 * <li> <CODE>SnmpDefinitions.snmpVersionOne</CODE>, for V1 Access Control (IP ACL)</li>
 * <li> <CODE>SnmpDefinitions.snmpVersionTwo</CODE>, for V2 Access Control (IP ACL) </li>
 * <li> <CODE>SnmpDefinitions.snmpVersionThree</CODE>, for V3 Access Control (User ACL)</li>
 * </ul>
 *
 * @since Java DMK 5.1
 */
public class SnmpJdmkAcm extends SnmpModelImpl 
    implements SnmpAccessControlModel {
    private InetAddressAcl ipacl = null;
    private boolean activatev3 = true;
    private boolean v1v2setrequest = false;
    private UserAcl useracl = null;
    /**
     * Constructor. Will register itself to the passed sub system.
     * @param sys The sub system.
     * @param ipacl IP ACL to use.
     * @param useracl User ACL to use.
     * @param activatev3 If <CODE>true</CODE>, must register itself with <CODE>SnmpDefinitions.snmpVersionThree</CODE> ID.
     */
    public SnmpJdmkAcm(SnmpSubSystem sys,
		       InetAddressAcl ipacl,
		       UserAcl useracl,
		       boolean activatev3) {
	super(sys, "User based");
	this.ipacl = ipacl;
	this.useracl = useracl;
	sys.addModel(SnmpDefinitions.snmpV1AccessControlModel, this);
	sys.addModel(SnmpDefinitions.snmpV2AccessControlModel, this);
	this.activatev3 = activatev3;
	if(activatev3)
	    sys.addModel(SnmpDefinitions.snmpV3AccessControlModel, this);
    }
    
    private boolean isV3Activated() {
	return activatev3;
    }
    
    /**
     * Method called by the dispatcher in order to control the access at an SNMP pdu level. If access is not allowed, an <CODE>SnmpStatusException</CODE> is thrown. In case of exception, the access control is aborted. OIDs are not checked.
     * This method is called prior to the <CODE>checkAccess</CODE> OID based method.
     * @param version The SNMP protocol version number.
     * @param principal The request principal.
     * @param securityLevel The request security level as defined in <CODE>SnmpEngine</CODE>.
     * @param pduType The pdu type (get, set, ...).
     * @param securityModel The security model ID.
     * @param contextName The access control context name.
     * @param pdu The pdu to check.
     */
    public void checkPduAccess(int version,
			       String principal,
			       int securityLevel,
			       int pduType,
			       int securityModel,
			       byte[] contextName,
			       SnmpPdu pdu) throws SnmpStatusException {
	if(logger.finerOn()) {
	    logger.finer("checkPduAccess", 
		  "sender is " + principal + " with " 
		  + new String(contextName));
	}
	if(contextName == null) 
	    throw new SnmpStatusException(SnmpStatusException.snmpRspAuthorizationError);

	switch(version) {
	case SnmpDefinitions.snmpVersionOne:
	case SnmpDefinitions.snmpVersionTwo:
	    if(isV3Activated()) {
		if(pduType == SnmpDefinitions.pduSetRequestPdu) {
		    synchronized(this) {
			if(!isSnmpV1V2SetRequestAuthorized()) {
			    if(logger.finerOn()) {
				logger.finer("checkPduAccess", 
				      "sender is " + principal + " with " 
				      + new String(contextName) + 
				      " \n SNMP V1 and V2 set requests disable. Access not granted.");
			    }
			    throw new SnmpStatusException(SnmpStatusException.snmpRspAuthorizationError);
			}
		    }
		}
		checkIPAcl(contextName,
			   principal,
			   pduType);
	    }
	    else
		checkIPAcl(contextName,
			   principal,
			   pduType);
	    break;
	case SnmpDefinitions.snmpVersionThree:
	    checkUserAcl(contextName,
			 principal,
			 pduType,
			 securityLevel);
	    break;
	default:
	    //System.out.println("Unsupported protocol version");
	    if (logger.finestOn()) {
		logger.finest("checkPduAccess","Unsupported protocol version.");
	    }
	    throw new SnmpStatusException(SnmpStatusException.snmpRspReadOnly);
	}
    }
    
    /**
     * Method called by the dispatcher in order to control the access at an <CODE>SnmpOid</CODE> level. If access is not allowed, an <CODE>SnmpStatusException</CODE> is thrown.
     * This method is called after the <CODE>checkPduAccess</CODE> pdu based method.
     * @param version The SNMP protocol version number.
     * @param principal The request principal.
     * @param securityLevel The request security level as defined in <CODE>SnmpEngine</CODE>.
     * @param pduType The pdu type (get, set, ...).
     * @param securityModel The security model ID.
     * @param contextName The access control context name.
     * @param oid The OID to check.
     */
    public void checkAccess(int version,
			    String principal,
			    int securityLevel,
			    int pduType,
			    int securityModel,
			    byte[] contextName,
			    SnmpOid oid) throws SnmpStatusException {
	//All the time OK. No VAcm
    }
    /**
     * Check the IP ACL using the provided IP address (principal parameters). This is only valid in V1 V2. The <CODE>contextName</CODE> is the community string. The algorithm is identical to the <CODE>checkIPACL</CODE> method located in the <CODE>SnmpRequestHandler</CODE> class.
     */
    private void checkIPAcl(byte[] contextName,
			    String principal,
			    int type) throws SnmpStatusException {
	
        String community = new String(contextName);
	if (logger.finerOn())
	    logger.finer("checkIPAcl", "sender is " + principal + " with " + community);
	InetAddress address = null;
	try {
	    address = InetAddress.getByName(principal);
	}catch(UnknownHostException e) {
	    if (logger.finerOn()) {
		logger.finer("checkIPAcl", "sender is " + address + " with " + community);
		logger.finer("checkIPAcl", "Bad IP address");
                    }
	    throw new SnmpStatusException(SnmpStatusException.snmpRspReadOnly);
	}
        if (ipacl != null) {
            if (type == SnmpDefinitions.pduSetRequestPdu) {
                if (!((InetAddressAcl)ipacl).checkWritePermission(address, community)) {
                    if (logger.finerOn()) {
                        logger.finer("checkIPAcl", "sender is " + address + " with " + community);
                        logger.finer("checkIPAcl", "sender has no write permission");
                    }
		    throw new SnmpStatusException(SnmpStatusException.snmpRspReadOnly);
                }
                else {
                    if (logger.finerOn()) {
                        logger.finer("checkIPAcl", "sender is " + address + " with " + community);
                        logger.finer("checkIPAcl", "sender has write permission");
                    }
                }
            }
            else {
                if (!((InetAddressAcl)ipacl).checkReadPermission(address, community)) {
                    if (logger.finerOn()) {
                        logger.finer("checkIPAcl", "sender is " + address + " with " + community);
                        logger.finer("checkIPAcl", "sender has no read permission");
                    }
		    throw new SnmpStatusException(SnmpStatusException.snmpRspNoSuchName);
                }
                else {
                    if (logger.finerOn()) {
                        logger.finer("checkIPAcl", "sender is " + address + " with " + community);
                        logger.finer("checkIPAcl", "sender has read permission");
                    }
                }
            }
        }
    }

    /**
     * In V3, the provided Acm is user based. We make use of a particular ACL that uses the user as the key. The context name is the v3 one. The principal is the user name. The algorithm is a classical ACL lookup.
     */
    private void checkUserAcl(byte[] contextName,
			      String principal,
			      int type,
			      int securityLevel) throws SnmpStatusException {
	String context = null;
	if(contextName.length != 0)
	    context = new String(contextName);
	else {
	    if (logger.finerOn())
		logger.finer("checkUserAcl", "Received a null context");
	    context = "null";
	}
	if (logger.finerOn())
	    logger.finer("checkUserAcl", "sender is " + principal + " with [" + context + "], security level : " + securityLevel);

        if (useracl != null) {
            if (type == SnmpDefinitions.pduSetRequestPdu) {
                if (!((UserAcl)useracl).checkWritePermission(principal, context, securityLevel)) {
                    if (logger.finerOn()) {
                        logger.finer("checkUserAcl", "sender is " + principal + " with " + context);
                        logger.finer("checkUserAcl", "sender has no write permission");
                    }
		    throw new SnmpStatusException(SnmpStatusException.snmpRspAuthorizationError);
                }
                else {
                    if (logger.finerOn()) {
                        logger.finer("checkUserAcl", "sender is " + principal + " with " + context);
                        logger.finer("checkUserAcl", "sender has write permission");
                    }
                }
            }
            else {
                if (!((UserAcl)useracl).checkReadPermission(principal, context,securityLevel)) {
                    if (logger.finerOn()) {
                        logger.finer("checkUserAcl", "sender is " + principal + " with " + context);
                        logger.finer("checkUserAcl", "sender has no read permission");
                    }
		    throw new SnmpStatusException(SnmpStatusException.snmpRspAuthorizationError);
                }
                else {
                    if (logger.finerOn()) {
                        logger.finer("checkUserAcl", "sender is " + principal + " with " + context);
                        logger.finer("checkUserAcl", "sender has read permission");
                    }
                }
            }
        }
    }
    /**
     * Enable SNMP V1 and V2 set requests. Be aware that can lead to a security hole in a context of SNMP V3 management. By default SNMP V1 and V2 set requests are not authorized.
     * @return boolean True the activation succeeded.
     */
    public synchronized boolean enableSnmpV1V2SetRequest() {
	v1v2setrequest = true;
	return true;
    }
    /**
     * Disable SNMP V1 and V2 set requests. By default SNMP V1 and V2 set requests are not authorized.
     * @return boolean True the deactivation succeeded.
     */
    public synchronized boolean disableSnmpV1V2SetRequest() {
	v1v2setrequest = false;
	return true;
    }
    
    /**
     * The SNMP V1 and V2 set requests authorization status. By default SNMP V1 and V2 set requests are not authorized.
     * @return boolean True SNMP V1 and V2 requests are authorized.
     */
    public synchronized boolean isSnmpV1V2SetRequestAuthorized() {
	return v1v2setrequest;
    }

    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpJdmkAcm");
    
    String dbgTag = "SnmpJdmkAcm";


}
