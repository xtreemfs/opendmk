/*
 * @(#)file      SnmpProxyMBean.java
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

package com.sun.management.snmp.agent;

/**
 * Exposes the remote management interface of the <CODE>SnmpProxy</CODE> MBean.
 * COULD BE USED IF MULTIPLE MBean Interface allowed.
 *
 * @since Java DMK 5.1
 */
interface SnmpProxyMBean
{
    /**
     * Gets the proxied agent host name.
     *
     * @return The proxied agent host name.
     */

    public String getHost();

    /**
     * Gets the proxied agent UDP port.
     *
     * @return The proxied agent UDP port.
     */

    public int getPort();

    /**
     * Gets the proxied agent root Oid.
     *
     * @return The proxied agent root Oid.
     */
  
    public String getOid();

    /**
     * Gets the proxied agent MIB name.
     *
     * @return The proxied agent MIB name.
     */
    
    public String getMibName();

    /**
     * Gets a textual representation of the proxied agent snmp protocol number.
     *
     * @return The textual representation of snmp protocol number.
     */

    public int getVersion();

    /**
     * Gets the proxy / agent communication timeout.
     *
     * @return The timeout in milliseconds.
     */

    public long getTimeout();
    
    /**
     * Sets the proxy / agent communication timeout.
     *
     * @param t The timeout in milliseconds.
     */

    public void setTimeout(long t);

    /**
     * By default, set requests are forwarded when <CODE>set(SnmpMibRequest request)</CODE> is called. Doing so makes the sub agent return errors to be systematically mapped to undoFailed. If you want more details on the sub agent error status, call this method with true value. The set request will then be  forwarded when <CODE>check(SnmpMibRequest request)</CODE> is called. No error translation will be done.
     * @param check True, the set request is forwarded on check, false the set request is forwarded on set.
     */
    public void forwardSetRequestOnCheck(boolean check);

    /**
     * Returns the way a set request is forwarded by this <CODE> SnmpProxy </CODE>.
     * @return True, the set request is forwarded on check, false the set request is forwarded on set.
     */
    public boolean isSetRequestForwardedOnCheck();
}
