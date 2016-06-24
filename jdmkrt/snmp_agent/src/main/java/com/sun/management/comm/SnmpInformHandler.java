/*
 * @(#)file      SnmpInformHandler.java
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
 *
 */

package com.sun.management.comm ;

// JMX imports
//
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpVarBindList;

/**
 * Provides the callback methods that are required to be implemented by the application
 * when an inform response is received by the agent.
 * <P>
 * Each inform request can be provided with an object that implements this callback 
 * interface. An application then uses the SNMP adaptor to start an SNMP inform request, 
 * which marks the request as active. The methods in this callback interface 
 * get invoked when any of the following happens:
 * <P>
 * <UL>
 * <LI> The agent receives the SNMP inform response.
 * <LI> The agent does not receive any response within a specified time and the number of tries
 * have exceeded the limit (timeout condition).
 * <LI> An internal error occurs while processing or parsing the inform request.
 * </UL>
 *
 * @since Java DMK 5.1
 */

public interface SnmpInformHandler extends SnmpDefinitions {

    /**
     * This callback is invoked when a manager responds to an SNMP inform request.
     * The callback should check the error status of the inform request to determine
     * the kind of response.
     * 
     * @param request The <CODE>SnmpInformRequest</CODE> associated with this callback.
     * @param errStatus The status of the request.
     * @param errIndex The index in the list that caused the error.
     * @param vblist The <CODE>Response varBind</CODE> list for the successful request.
     */
    public abstract void processSnmpPollData(SnmpInformRequest request, int errStatus, int errIndex, SnmpVarBindList vblist);

    /**
     * This callback is invoked when a manager does not respond within the 
     * specified timeout value to the SNMP inform request. The number of tries have also
     * been exhausted.
     * @param request The <CODE>SnmpInformRequest</CODE> associated with this callback.
     */
    public abstract void processSnmpPollTimeout(SnmpInformRequest request);

    /**
     * This callback is invoked when any form of internal error occurs.
     * @param request The <CODE>SnmpInformRequest</CODE> associated with this callback.
     * @param errmsg The <CODE>String</CODE> describing the internal error.
     */
    public abstract void processSnmpInternalError(SnmpInformRequest request, String errmsg);
}
