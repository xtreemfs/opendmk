/*
 * @(#)file      SnmpErrorHandlerAgent.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.17
 * @(#)lastedit  07/03/08
 * @(#)build     @BUILD_TAG_PLACEHOLDER@
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
 */
package com.sun.management.snmp.agent;


// java imports
//
import java.io.Serializable;
import java.util.Enumeration;

// jmx imports
//
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpDefinitions;
import javax.management.ObjectName;
import javax.management.MBeanServer;
import com.sun.management.snmp.SnmpVarBind;

// jdmk imports
//
import com.sun.jdmk.internal.ClassLogger;


/**
 * A simple MIB agent that implements SNMP calls (get, set, getnext and 
 * getbulk) in a way that only errors or exceptions are returned. 
 * Every call done on this agent fails. Error handling is done according 
 * to the manager's SNMP protocol version.
 * <P>It is used by <CODE>SnmpAdaptorServer</CODE> for its default agent 
 * behavior. When a received Oid doesn't match, this agent is called to 
 * fill the result list with errors.</P>
 * 
 *
 * @since Java DMK 5.1
 */
public class SnmpErrorHandlerAgent extends SnmpMibAgent
    implements Serializable {
    private static final long serialVersionUID = 694542373788291073L;
    
    public SnmpErrorHandlerAgent() {}

    /**
     * Initializes the MIB (with no registration of the MBeans into the 
     * MBean server). Does nothing.
     *
     * @exception IllegalAccessException The MIB cannot be initialized.
     */
    
    public void init() throws IllegalAccessException {
    }

    /**
     * Initializes the MIB but each single MBean representing the MIB 
     * is inserted into the MBean server.
     *
     * @param server The MBean server to register the service with.
     * @param name The object name.
     *
     * @return The passed name parameter.
     *
     * @exception java.lang.Exception
     */

    public ObjectName preRegister(MBeanServer server, ObjectName name) 
	throws Exception {
        return name;
    }
  
    /**
     * Gets the root object identifier of the MIB.
     * <P>The root object identifier is the object identifier uniquely 
     * identifying the MIB.
     *
     * @return The returned oid is null.
     */

    public long[] getRootOid() {
	return null;
    }
  
    /**
     * Processes a <CODE>get</CODE> operation. It will throw an exception 
     * for V1 requests or it will set exceptions within the list for
     * V2 requests.
     * 
     * @param inRequest The SnmpMibRequest object holding the list of 
     * variable to be retrieved.
     *
     * @exception SnmpStatusException An error occurred during the operation.
     */

    public void get(SnmpMibRequest inRequest) throws SnmpStatusException {
    
	if(logger.finestOn()) logger.finer("get","Get in Exception");
    
	if(inRequest.getVersion() == SnmpDefinitions.snmpVersionOne)
	    throw new SnmpStatusException(SnmpStatusException.noSuchName);
    
	Enumeration l = inRequest.getElements();
	while(l.hasMoreElements()) {
	    SnmpVarBind varbind = (SnmpVarBind) l.nextElement(); 
	    varbind.setSnmpValue(SnmpVarBind.noSuchObject);
	}
    }

    /**
     * Checks if a <CODE>set</CODE> operation can be performed.
     * If the operation can not be performed, the method should emit a
     * <CODE>SnmpStatusException</CODE>.
     * 
     * @param inRequest The SnmpMibRequest object holding the list of 
     *            variables to be set. This list is composed of 
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException The <CODE>set</CODE> operation 
     *    cannot be performed.
     */

    public void check(SnmpMibRequest inRequest) throws SnmpStatusException {

	if(logger.finestOn()) logger.finer("check","Check in Exception");

	throw new SnmpStatusException(SnmpDefinitions.snmpRspNotWritable);
    }

    /**
     * Processes a <CODE>set</CODE> operation. Should never be called 
     * (check previously called having failed).
     * 
     * @param inRequest The SnmpMibRequest object holding the list of 
     *    variable to be set.
     *
     * @exception SnmpStatusException An error occurred during the operation.
     */

    public void set(SnmpMibRequest inRequest) throws SnmpStatusException {

	if(logger.finestOn()) 
	    logger.finer("set","Set in Exception, CAN't be called");
      
	throw new SnmpStatusException(SnmpDefinitions.snmpRspNotWritable);
    }

    /**
     * Processes a <CODE>getNext</CODE> operation. It will throw an 
     * exception for V1 requests or it will set exceptions within the 
     * list for V2 requests..
     * 
     * @param inRequest The SnmpMibRequest object holding the list of 
     *        variables to be retrieved.
     *
     * @exception SnmpStatusException An error occurred during the operation.
     */

    public void getNext(SnmpMibRequest inRequest) throws SnmpStatusException {
    
	if(logger.finestOn()) logger.finer("getNext","GetNext in Exception");

	if(inRequest.getVersion() == SnmpDefinitions.snmpVersionOne)
	    throw new SnmpStatusException(SnmpStatusException.noSuchName);
    
	Enumeration l = inRequest.getElements();
	while(l.hasMoreElements()) {
	    SnmpVarBind varbind = (SnmpVarBind) l.nextElement(); 
	    varbind.setSnmpValue(SnmpVarBind.endOfMibView);
	}
    }
  
    /**
     * Processes a <CODE>getBulk</CODE> operation. It will throw an 
     * exception if the request is a V1 one or it will set exceptions 
     * within the list for V2 ones.
     * 
     * @param inRequest The SnmpMibRequest object holding the list of 
     *        variable to be retrieved.
     *
     * @exception SnmpStatusException An error occurred during the operation.
     */ 
    public void getBulk(SnmpMibRequest inRequest, int nonRepeat, 
			int maxRepeat) 
	throws SnmpStatusException {
	
	if(logger.finestOn()) logger.finer("getBulk","GetBulk in Exception");
 
	if(inRequest.getVersion() == SnmpDefinitions.snmpVersionOne) 
	    throw new SnmpStatusException(SnmpDefinitions.snmpRspGenErr, 0);
      
	Enumeration l = inRequest.getElements();
	while(l.hasMoreElements()) {
	    SnmpVarBind varbind = (SnmpVarBind) l.nextElement(); 
	    varbind.setSnmpValue(SnmpVarBind.endOfMibView);
	}
    }

    private String dbgTag = "SnmpErrorHandlerAgent";
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_ADAPTOR_SNMP,
			"SnmpErrorHandlerAgent");
}


