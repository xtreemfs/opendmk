/*
 * @(#)file      SnmpUsm.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.25
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

package com.sun.management.snmp.usm;

import com.sun.management.internal.snmp.SnmpSubSystem;
import com.sun.management.internal.snmp.SnmpSecurityModel;
import com.sun.management.internal.snmp.SnmpSecurityCache;
import com.sun.management.internal.snmp.SnmpDecryptedPdu;

import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpStatusException;
/**
 * This interface is implemented by User based Security Models. You have to implement this interface when developing your own Usm.
 *
 * @since Java DMK 5.1
 */
public interface SnmpUsm extends SnmpSecurityModel{

    /**
     * The User based Security Model Id as defined in RFC 2574. The Usm Id number is 3.
     */
    public static final int ID = 3;
    /**
     * The User based Security Model max nb boots as defined in RFC 2574. The max nb boots value is (2e31 - 1)
     */
    public static final int MAX_NB_BOOTS = 2147483647;
    /**
     * The User based Security Model time window as defined in RFC 2574. The defined value is 150 seconds.
     */
    public static final int TIME_WINDOW = 150;

    /**
     * The User based Security Model <CODE>usmNoAuthProtocol</CODE> OID "1.3.6.1.6.3.10.1.1.1".
     */
    public static final String usmNoAuthProtocol = "1.3.6.1.6.3.10.1.1.1";

    /**
     * The User based Security Model <CODE>usmNoPrivProtocol</CODE> OID "1.3.6.1.6.3.10.1.2.1".
     */
    public static final String usmNoPrivProtocol = "1.3.6.1.6.3.10.1.2.1";

    /**
     * The not in time report OID "1.3.6.1.6.3.15.1.1.2.0"
     */
    public static final String usmStatsNotInTimeWindows = "1.3.6.1.6.3.15.1.1.2.0";
     /**
     * The unknown engine Id report OID "1.3.6.1.6.3.15.1.1.4.0"
     */
    public static final String usmStatsUnknownEngineIds = "1.3.6.1.6.3.15.1.1.4.0";
    /**
     * The unknown user name report OID "1.3.6.1.6.3.15.1.1.3.0"
     */
    public static final String usmStatsUnknownUserNames = "1.3.6.1.6.3.15.1.1.3.0";
    /**
     * The unsupported security level report OID "1.3.6.1.6.3.15.1.1.1.0"
     */
    public static final String usmStatsUnsupportedSecLevels = "1.3.6.1.6.3.15.1.1.1.0";
    /**
     * The wrong digest report OID "1.3.6.1.6.3.15.1.1.5.0"
     */
    public static final String usmStatsWrongDigests = "1.3.6.1.6.3.15.1.1.5.0";
    /**
     * The decryption error report OID "1.3.6.1.6.3.15.1.1.6.0"
     */
    public static final String usmStatsDecryptionErrors = "1.3.6.1.6.3.15.1.1.6.0";

    /**
     * Get the time window used for timeliness checks. If non are set, the <CODE>SnmpUsm.TIMEWINDOW</CODE> is the default used.
     * @return The time window in seconds.
     */
    public int getTimelinessWindow();
    
     /**
     * Set the time window used for timeliness checks. If non are set, the <CODE>SnmpUsm.TIMEWINDOW</CODE> is the default used.
     * @param t The time window in seconds.
     */
    public void setTimelinessWindow(int t);
    /**
     * Gets the associated Usm local configuration datastore.
     * @return The Usm local configuration datastore.
     */
    public SnmpUsmLcd getLcd();

    /**
     * Sets the local configuration datastore.
     * @param lcd The Usm local configuration datastore.
     */
    public void setLcd(SnmpUsmLcd lcd);
    
    /** Gets the Usm error counter.
     * @return The counter.
     */
    public Long getUnsupportedSecLevelsCounter();
    /** Gets the Usm error counter.
     * @return The counter.
     */
    public Long getNotInTimeWindowsCounter();
    /** Gets the Usm error counter.
     * @return The counter.
     */
    public Long getUnknownUserNamesCounter();
    /** Gets the Usm error counter.
     * @return The counter.
     */
    public Long getUnknownEngineIdsCounter();
    /** Gets the Usm error counter.
     * @return The counter.
     */
    public Long getWrongDigestsCounter();
    /** Gets the Usm error counter.
     * @return The counter.
     */
    public Long getDecryptionErrorsCounter();

    /**
     * Gets the peer associated with the passed engine Id.
     * @param id The SNMP engine Id.
     * @return The peer.
     */
    public SnmpUsmEnginePeer getEnginePeer(SnmpEngineId id);

    /**
     * Instantiates an <CODE>SnmpUsmSecurityParameters</CODE>.
     * @return Empty security parameters.
     */
    public SnmpUsmSecurityParameters createUsmSecurityParameters();
}
