/*
 * @(#)file      SnmpV3Parameters.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.24
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
package com.sun.management.snmp.manager;

import com.sun.management.snmp.SnmpSecurityParameters;
/**
 * This class models the parameters that are needed when making SNMP V3 requests. This class is an abstract one. If you want to send V3 requests you need to set a security model. Classes that inherit from this one are SNMP V3 based associated to a particular security model (e.g. {@link com.sun.management.snmp.manager.usm.SnmpUsmParameters SnmpUsmParameters}).
 * When a <CODE> SnmpV3Parameters </CODE> is instantiated, the security level default value is <CODE> noAuthNoPriv </CODE>.
 *
 * @since Java DMK 5.1
 */
public abstract class SnmpV3Parameters extends SnmpParams {
    /**
     * Private fields that are needed to store V3 message parameters.
     */
    private byte[] contextEngineId = null;
    private byte[] contextName = null;
    private byte msgFlags = 0;
    private int msgMaxSize = SnmpPeer.defaultSnmpResponsePktSize;
    private int msgSecurityModel = 0;
    private SnmpSecurityParameters securParams = null;
    /**
     * Constructor. Can only be called by son.
     */
    protected SnmpV3Parameters() {
	super(snmpVersionThree);
    }
    /**
     * Constructor. Can only be called by son located in this package.
     */
    protected SnmpV3Parameters(byte[] contextEngineId,
			       byte[] contextName,
			       byte msgFlags,
			       int msgMaxSize,
			       int msgSecurityModel) {
	super(snmpVersionThree);
	this.contextEngineId = contextEngineId;
	this.contextName = contextName;
	this.msgFlags = msgFlags;
	this.msgMaxSize = msgMaxSize;
	this.msgSecurityModel = msgSecurityModel;
    }

    /**
     * Called by son at construction time. Each son use a specific set of security parameters. The son stores this info in the father once created.
     */
    protected void setSecurityParameters(SnmpSecurityParameters securParams) {
	this.securParams  = securParams;
    }

    /**
     * <CODE>set</CODE> requests are enabled in V3.
     * @return <CODE>true</CODE>, all the time enabled.
     */
    public boolean allowSnmpSets() {
	return true;
    }
			   
    /**
     * Sets the context engine Id in which requests will be interpreted.
     * @param contextEngineId The context engine Id.
     */
    public void setContextEngineId(byte[] contextEngineId) {
	this.contextEngineId = contextEngineId;
    }

     /**
     * Gets the context engine Id in which requests will be interpreted.
     * @return The context engine Id.
     */
    public byte[] getContextEngineId() {
	return contextEngineId;
    }
			   
    /**
     * Sets the context name in which requests will be interpreted.
     * @param contextName The context name.
     */
    public void setContextName(byte[] contextName){
	this.contextName = contextName;
    }
    
    /**
     * Gets the context name in which requests will be interpreted.
     * @return The context name.
     */
    public byte[] getContextName() {
	return contextName;
    }

    /**
     * Sets the flags that will be used when sending requests. The default value is <CODE> noAuthNoPriv </CODE>.
     * @param securityLevel The flags.
     */
    public void setSecurityLevel(int securityLevel) {
	this.msgFlags = (byte) securityLevel ;
    }

    /**
     * Gets the security level that will be used when sending requests. The default value is <CODE> noAuthNoPriv </CODE>.
     * @return The security level.
     */
    public int getSecurityLevel() {
	return (int) msgFlags;
    }

    /**
     * Sets the max allowed size for responses.
     * @param msgMaxSize The max size.
     */
    public void setMsgMaxSize(int msgMaxSize) {
	this.msgMaxSize = msgMaxSize;
    }

    /**
     * Gets the max response size that will be used when sending requests.
     * @return The max size.
     */
    public int getMsgMaxSize() {
	return msgMaxSize;
    }
    /**
     * Sets the security model implemented by the son.
     * Is called by the son only.
     */
    protected void setMsgSecurityModel(int msgSecurityModel) {
	this.msgSecurityModel = msgSecurityModel;
    }
    /**
     * Gets the security model that will be used when sending requests.
     * @return The security model.
     */
    public int getMsgSecurityModel() {
	return msgSecurityModel;
    }
    /**
     * Gets the security parameters that have been created by the son. When manipulating this class you should be aware of the son type.
     * @return The security parameters.
     */
    public SnmpSecurityParameters getSecurityParameters() {
	return securParams;
    }
}
