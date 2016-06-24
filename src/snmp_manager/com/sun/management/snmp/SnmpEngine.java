/*
 * @(#)file      SnmpEngine.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.19
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

/**
 * This engine is conformant with the RFC 2571. It is the main object within an SNMP entity (agent, manager...). 
 * To an engine is associated an {@link SnmpEngineId}.
 * Engine instantiation is based on a factory {@link com.sun.management.snmp.SnmpEngineFactory  SnmpEngineFactory}.
 * When an <CODE> SnmpEngine </CODE> is created, a User based Security Model (USM) is initialized. The security configuration is located in a text file (see jdmk.security file in <Java DMK install path>/etc/jdmk.security.template).
 * The text file is read when the engine is created. The Java DMK API objects that make the engine to be created are : 
<ul>
 <li> {@link com.sun.management.comm.SnmpV3AdaptorServer} </li>
 <li> {@link com.sun.management.snmp.manager.SnmpSession} </li>
 <li> {@link com.sun.management.snmp.manager.SnmpEventReportDispatcher} </li>
 <li> {@link com.sun.management.snmp.agent.SnmpTrapReceiver} </li>
</ul>
<P> The USM configuration text file can be updated remotely using the USM Mib.</P>
<P> User that are configured in the Usm (jdmk.security file) text file are nonVolatile. </P>
<P> Usm Mib userEntry supported storage type values are : volatile or nonVolatile only. Other values are rejected and a wrongValue is returned) </P>
<ul>
<li> volatile means that user entry is not flushed in security file </li>
<li> nonVolatile means that user entry is flushed in security file </li>
<li> If a nonVolatile row is set to be volatile, it will be not flushed in the file </li>
<li>If a volatile row created from the UsmMib is set to nonVolatile, it will be flushed in the file (if the file exist and is writable otherwise an inconsistentValue is returned)</li>
</ul>
 *
 * @since Java DMK 5.1
 */
public interface SnmpEngine {
    /**
     * Gets the engine time in seconds. This is the time from the last reboot.
     * @return The time from the last reboot.
     */
    public int getEngineTime();
    /**
     * Gets the engine Id. This is unique for each engine.
     * @return The engine Id object.
     */
    public SnmpEngineId getEngineId();

    /**
     * Gets the engine boot number. This is the number of time this engine has rebooted. Each time an <CODE>SnmpEngine</CODE> is instantiated, it will read this value in its Lcd, and store back the value incremented by one.
     * @return The engine's number of reboot.
     */
    public int getEngineBoots();

    /**
     * Gets the Usm key handler.
     * @return The key handler.
     */
    public SnmpUsmKeyHandler getUsmKeyHandler();
}
