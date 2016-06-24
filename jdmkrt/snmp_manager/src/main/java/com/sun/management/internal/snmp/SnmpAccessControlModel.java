/*
 * @(#)file      SnmpAccessControlModel.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.16
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
/**
 * Access Control Model interface. Every access control model must implement this interface in order to be integrated in the engine based framework.
 *
 * @since Java DMK 5.1
 */
public interface SnmpAccessControlModel extends SnmpModel {
    /**
     * Method called by the dispatcher in order to control the access at an <CODE>SnmpOid</CODE> Level. If access is not allowed, an <CODE>SnmpStatusException</CODE> is thrown.
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
			    SnmpOid oid)
	throws SnmpStatusException;
    /**
     * Method called by the dispatcher in order to control the access at an SNMP pdu Level. If access is not allowed, an <CODE>SnmpStatusException</CODE> is thrown. In case of exception, the access control is aborted. OIDs are not checked.
     * This method should be called prior to the <CODE>checkAccess</CODE> OID based method.
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
			       SnmpPdu pdu)
	throws SnmpStatusException;

    /**
     * Enable SNMP V1 and V2 set requests. Be aware that can lead to a security hole in a context of SNMP V3 management. By default SNMP V1 and V2 set requests are not authorized.
     * @return boolean True the activation succeeded.
     */
    public boolean enableSnmpV1V2SetRequest();
    /**
     * Disable SNMP V1 and V2 set requests. By default SNMP V1 and V2 set requests are not authorized.
     * @return boolean True the deactivation succeeded.
     */
    public boolean disableSnmpV1V2SetRequest();
    
    /**
     * The SNMP V1 and V2 set requests authorization status. By default SNMP V1 and V2 set requests are not authorized.
     * @return boolean True SNMP V1 and V2 requests are authorized.
     */
    public boolean isSnmpV1V2SetRequestAuthorized();
}
