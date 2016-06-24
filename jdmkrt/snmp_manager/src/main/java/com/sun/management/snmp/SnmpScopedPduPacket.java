/*
 * @(#)file      SnmpScopedPduPacket.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.18
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

package com.sun.management.snmp;

import java.io.Serializable;

import com.sun.management.snmp.SnmpSecurityParameters;

import com.sun.management.snmp.SnmpDefinitions;
/**
 * Is the fully decoded representation of an SNMP V3 packet.
 * <P>
 * 
 * Classes are derived from <CODE>SnmpPdu</CODE> to
 * represent the different forms of SNMP pdu
 * ({@link com.sun.management.snmp.SnmpScopedPduRequest SnmpScopedPduRequest},
 * {@link com.sun.management.snmp.SnmpScopedPduBulk SnmpScopedPduBulk}).
 * <BR>The <CODE>SnmpScopedPduPacket</CODE> class defines the attributes 
 * common to every scoped SNMP packets.
 * 
 * @see SnmpV3Message
 *
 *
 * @since Java DMK 5.1
 */
public abstract class SnmpScopedPduPacket extends SnmpPdu
    implements Serializable {
    /**
     * Message max size the pdu sender can deal with.
     */
    public int msgMaxSize = 0;

    /**
     * Message identifier.
     */
    public int msgId = 0;

    /**
     * Message flags. Reportable flag  and security level.</P>
     *<PRE>
     * --  .... ...1   authFlag
     * --  .... ..1.   privFlag
     * --  .... .1..   reportableFlag
     * --              Please observe:
     * --  .... ..00   is OK, means noAuthNoPriv
     * --  .... ..01   is OK, means authNoPriv
     * --  .... ..10   reserved, must NOT be used.
     * --  .... ..11   is OK, means authPriv
     *</PRE>
     */
    public byte msgFlags = 0;

    /**
     * The security model the security sub system MUST use in order to deal with this pdu (e.g. User based Security Model Id = 3).
     */
    public int msgSecurityModel = 0;

    /**
     * The context engine Id in which the pdu must be handled (Generally the local engine Id).
     */
    public byte[] contextEngineId = null;

    /**
     * The context name in which the OID have to be interpreted.
     */
    public byte[] contextName = null;

    /**
     * The security parameters. This is an opaque member that is interpreted by the concerned security model. For Usm, {@link com.sun.management.snmp.usm.SnmpUsmSecurityParameters SnmpUsmSecurityParameters}.
     */
    public SnmpSecurityParameters securityParameters = null;

    /**
     * Constructor. Is only called by a son. Set the version to <CODE>SnmpDefinitions.snmpVersionThree</CODE>.
     */
    protected SnmpScopedPduPacket() {
	version = SnmpDefinitions.snmpVersionThree;
    }
}

