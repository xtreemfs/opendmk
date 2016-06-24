/*
 * @(#)file      SNMP_USER_BASED_SM_MIBImpl.java
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
package com.sun.management.snmp.usm.usmmib;

import com.sun.management.snmp.usm.SnmpUsmLcd;
// jmx imports
//
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.InstanceAlreadyExistsException;
import com.sun.management.snmp.SnmpEngine;

// jdmk imports
//
import com.sun.management.snmp.agent.SnmpMib;
import com.sun.management.snmp.agent.SnmpMibNode;
import com.sun.management.snmp.agent.SnmpMibTable;
import com.sun.management.snmp.agent.SnmpStandardObjectServer;

import com.sun.management.snmp.usm.SnmpUsm;
/**
 * This is a RFC 2574 MIB implementation. 
 * If the MIB is registered within an <CODE> MBeanServer </CODE>, a JMX naming default naming schema is applied. JMX object name : <engine Id>/SNMP_USER_BASED_SM_MIB:name=Usm-MIB-RFC2574. 
 * Having the engine Id in the domain allows to register multiple MIB (one for each <CODE> SnmpAdaptorServer </CODE>) within the same MBeanServer.
 * If you want to change the domain, extends <CODE> SNMP_USER_BASED_SM_MIBImpl </CODE> and set <CODE> mibName </CODE> attribute to your own domain.
 * 
 *
 * @since Java DMK 5.1
 */
public class SNMP_USER_BASED_SM_MIBImpl extends SNMP_USER_BASED_SM_MIB {
    private static final long serialVersionUID = 5696272497292831868L;
    SnmpUsmLcd lcd = null;
    SnmpEngine engine = null;
    SnmpUsm model = null;
    /**
     * Constructor.
     * @param engine The local engine.
     * @param lcd The Usm Lcd to get and store configuration from.
     * @param model The Usm implementation.
     */
    public SNMP_USER_BASED_SM_MIBImpl(SnmpEngine engine,
				      SnmpUsmLcd lcd,
				      SnmpUsm model) {
	super();
	//Name is prefixed by engine Id.
	 mibName = engine.getEngineId().toString() + "/SNMP_USER_BASED_SM_MIB";
	this.lcd = lcd;
	this.engine = engine;
	this.model = model;
    }
    //Statistiques have been customized
    protected Object createUsmStatsMBean(String groupName,
					 String groupOid,  
					 ObjectName groupObjname, 
					 MBeanServer server)  {
	return new UsmStatsImpl(this, model);
    }
	
    //Meta has been customized.
    protected UsmUserMeta createUsmUserMetaNode(String groupName,
						String groupOid, 
						ObjectName groupObjname, 
						MBeanServer server)  {
        return new UsmUserMetaImpl(this, objectserver);
    }

    /**
     * Factory method for "UsmUser" group MBean.
     * 
     * You can redefine this method if you need to replace the default
     * class <CODE> UsmUserImpl</CODE> with your own customized class.
     * <CODE> UsmUserImpl</CODE> creates a <CODE> TableUsmUserTableImpl </CODE> in its constructor.
     * 
     * @param groupName Name of the group ("UsmUser")
     * @param groupOid  OID of this group
     * @param groupObjname ObjectName for this group (may be null)
     * @param server    MBeanServer for this group (may be null)
     * 
     * @return An instance of the MBean class generated for the
     *         "UsmUser" group (UsmUser)
     * 
     * Note that when using standard metadata,
     * the returned object must implement the "UsmUserMBean"
     * interface.
     **/
    protected Object createUsmUserMBean(String groupName,
					String groupOid,  
					ObjectName groupObjname, 
					MBeanServer server)  {
	
	if (server != null)
	    return new UsmUserImpl(this, server, lcd, engine);
	
	else
	    return new UsmUserImpl(this, lcd, engine);
    }
}
