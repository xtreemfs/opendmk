/*
 * @(#)file      SnmpV3Proxy.java
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
package com.sun.management.snmp.agent;

// java import
//
import java.io.Serializable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.NoSuchElementException;
import java.net.UnknownHostException;

// RI import
//
import javax.management.ObjectName;
import javax.management.MBeanServer;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpCounter;
import com.sun.management.snmp.SnmpCounter64;
import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.manager.SnmpPeer;
import com.sun.management.snmp.manager.SnmpParameters;
import com.sun.management.snmp.manager.SnmpParams;
import com.sun.management.snmp.manager.SnmpV3Parameters;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpScopedPduPacket;
import com.sun.management.snmp.manager.SnmpRequest;
import com.sun.management.snmp.manager.SnmpSession;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpEngine;

// jdmk import
//
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpMibRequest;
import com.sun.management.comm.SnmpV3AdaptorServerMBean;
import com.sun.jdmk.internal.ClassLogger;

/**
 * The SnmpV3Proxy class is an abstract class that provides services to proxy
 * an SNMP V3 agent.
 * <P> It mains role is to handle various SNMP V1 V2 and V3 security contexts.
 * Some rules are applied when call are received in an SNMP version and are 
 * forwarded in SNMP V3. </P> 
 * <P> These rules are coded in the method  {@link 
 * com.sun.management.snmp.agent.SnmpV3Proxy#translateParameters 
 * translateParameters}. If you want to change them, just overload this 
 * method. </P>
 * <P> Rules when forwarding to SNMP V3 peer details. You can change them by 
 * subclassing the proxy: 
 * <ul>
 * <li> SNMP V1 or V2 received call : The securityLevel is set to 
 *      noAuthNoPriv. If the received community string is of the form 
 *      "community@contex", the context name is set to the received context 
 *      value otherwise it is null.</li>
 * <li> SNMP V3 received call : The received values (context, securityLevel, 
 *      ...) are reused.</li>
 * </ul>
 *
 * @since Java DMK 5.1
 */
public abstract class SnmpV3Proxy extends SnmpProxy {
    // CONSTRUCTOR
    //===============
    //
    /**
     * Initializes this SNMP proxy with a SnmpPeer to which calls are 
     * forwarded. The passed root oid is used when registering the proxy 
     * within the adaptor. The name is used when registering the proxy in 
     * the <CODE> SnmpAdaptorServer </CODE>
     * @param engine The SNMP adaptor engine.
     * @param peer The peer representing the proxied agent.
     * @param rootOid The proxy root oid.
     * @param name The proxy name.
     * @exception IllegalArgumentException if the passed engine is null.
     * @exception SnmpStatusException An error occurred while accessing
     *            a MIB node.
     */    
    public SnmpV3Proxy(SnmpEngine engine,
		       SnmpPeer peer,
		       String rootOid,
		       String name) 
	throws SnmpStatusException {
	super(engine, peer, rootOid, name);
    }

    /**
     * The distant EngineId. This abstract method must be implemented by 
     * subclasses. The distant engine id is retrieved by the concrete subclass.
     * @return The peer engine Id.
     */
    public abstract SnmpEngineId getEngineId();
    
    /**
     * Factory parameters method. Overload this method in order to create 
     * your own parameters. e.g. Usm based parameters are specific to the 
     * security model. A proxy of a Usm enabled SNMP V3 agent will provide 
     * <CODE> SnmpUsmParameters </CODE> instance.
     * @param pdu Can be null.
     * @return Snmp parameters to use when forwarding the call.
     */
    protected abstract SnmpParams createParameters(SnmpPdu pdu) 
	throws SnmpStatusException;
    
    /**
     * Return the parameters to use when sending the call. The info contained
     * in the SnmpMibRequest pdu are reused in order to construct new 
     * SnmpParams.
     * The returned parameters are passed to the session default peer.
     * Overload this method in order to change the default policy.
     * @param req The received request.
     * @return The parameters to use when forwarding the call.
     */
    protected SnmpParams translateParameters(SnmpMibRequest req) 
	throws SnmpStatusException {
	if(logger.finestOn()) {
	    logger.finest("translateParameters", " Mib request :" + req);
	}
	//Access current pdu
	SnmpPdu pdu = req.getPdu();
	if(pdu == null)  return createParameters(null);
	//Create the parameters
	SnmpV3Parameters params = (SnmpV3Parameters) createParameters(pdu);
	byte[] contextName = null;
	byte msgFlags = 0;
	//The context engineId used is the distant agent one.
	byte[] contextEngineId = getEngineId().getBytes();
	//Set the local contextEngineId
	params.setContextEngineId(contextEngineId);
	if(logger.finestOn()) {
	    logger.finest("translateParameters"," Snmp version :"+pdu.version);
	}
	switch(pdu.version) {
	case SnmpDefinitions.snmpVersionOne:
	case SnmpDefinitions.snmpVersionTwo: {
	    
	    SnmpPduPacket pack = (SnmpPduPacket) pdu;	
	    String community = new String(pack.community);
	    if(logger.finestOn()) {
		logger.finest("translateParameters", " Community :"+community);
	    }
	    int contextIndex = community.indexOf('@');
	    if(contextIndex != -1) 
		contextName = 
		    (community.substring(contextIndex + 1)).getBytes();
	    //Lowest securityLevl.
	    msgFlags = SnmpDefinitions.noAuthNoPriv;
	}break;
	case SnmpDefinitions.snmpVersionThree: {
	    SnmpScopedPduPacket pack = (SnmpScopedPduPacket) pdu;
	    //Reuse contextName
	    contextName = pack.contextName;
	    //Reuse msg flags
	    msgFlags = pack.msgFlags;
	    //Reuse msg max size
	    params.setMsgMaxSize(pack.msgMaxSize);
	    if(logger.finestOn()) {
		logger.finest("translateParameters", " msgMaxSize :" + 
		      pack.msgMaxSize);	
	    }
	}
	}
	if(logger.finestOn()) {
	    logger.finest("translateParameters"," contextName :"+contextName);
	    logger.finest("translateParameters", " msgFlags :" + msgFlags);   
	}
	params.setSecurityLevel((int)msgFlags);
	params.setContextName(contextName);
	
	if(logger.finestOn())
	    logger.finest("translateParameters", 
			  "Returned parameters : " + params);
	
	return params;
    }

    String dbgTag = "SnmpV3Proxy";  

    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_PROXY_SNMP,"SnmpV3Proxy");

}
