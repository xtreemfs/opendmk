/*
 * @(#)file      SnmpMibAgentMBean.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.27
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



// java imports
//
import java.util.Vector;

// jmx imports
//
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.management.InstanceNotFoundException;
import javax.management.ServiceNotFoundException;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpStatusException;

/**
 * Exposes the remote management interface of the <CODE>SnmpMibAgent</CODE> 
 * MBean.
 * 
 * @since Java DMK 5.1
 */

public interface SnmpMibAgentMBean {

    // PUBLIC METHODS
    //---------------

    /**
     * Processes a <CODE>get</CODE> operation.
     * This method must not be called from remote.
     * 
     * @param req The SnmpMibRequest object holding the list of variables to
     *            be retrieved. This list is composed of 
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException An error occurred during the operation.
     * @see SnmpMibAgent#get(SnmpMibRequest)
     */
    public void get(SnmpMibRequest req)	throws SnmpStatusException;
  
    /**
     * Processes a <CODE>getNext</CODE> operation.
     * This method must not be called from remote.
     * 
     * @param req The SnmpMibRequest object holding the list of variables to
     *            be retrieved. This list is composed of 
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException An error occurred during the operation.
     * @see SnmpMibAgent#getNext(SnmpMibRequest)
     */
    public void getNext(SnmpMibRequest req) throws SnmpStatusException;
    
    /**
     * Processes a <CODE>getBulk</CODE> operation.
     * This method must not be called from remote.
     * 
     * @param req The SnmpMibRequest object holding the list of variables to
     *            be retrieved. This list is composed of 
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @param nonRepeat The number of variables, starting with the first 
     *    variable in the variable-bindings, for which a single 
     *    lexicographic successor is requested.
     *
     * @param maxRepeat The number of lexicographic successors requested 
     *    for each of the last R variables. R is the number of variables 
     *    following the first <CODE>nonRepeat</CODE> variables for which 
     *    multiple lexicographic successors are requested.
     *
     * @exception SnmpStatusException An error occurred during the operation.
     * @see SnmpMibAgent#getBulk(SnmpMibRequest,int,int)
     */
    public void getBulk(SnmpMibRequest req, int nonRepeat, int maxRepeat)
	throws SnmpStatusException;

    /**
     * Processes a <CODE>set</CODE> operation.
     * This method must not be called from remote.
     * 
     * @param req The SnmpMibRequest object holding the list of variables to
     *            be set. This list is composed of 
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException An error occurred during the operation.
     * @see SnmpMibAgent#set(SnmpMibRequest)
     */
    public void set(SnmpMibRequest req)	throws SnmpStatusException;

    /**
     * Checks if a <CODE>set</CODE> operation can be performed.
     * If the operation cannot be performed, the method should emit a
     * <CODE>SnmpStatusException</CODE>.
     * 
     * @param req The SnmpMibRequest object holding the list of variables to
     *            be set. This list is composed of 
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException The <CODE>set</CODE> operation 
     *    cannot be performed.
     * @see SnmpMibAgent#check(SnmpMibRequest)
     */
    public void check(SnmpMibRequest req) throws SnmpStatusException;
        
    // GETTERS AND SETTERS
    //--------------------

    /**
     * Gets the reference to the MBean server in which the SNMP MIB is 
     * registered.
     *
     * @return The MBean server or null if the MIB is not registered in any 
     *         MBean server.
     */
    public MBeanServer getMBeanServer();
  
    /**
     * Gets the reference to the SNMP protocol adaptor to which the MIB is 
     * bound.
     * <BR>This method is used for accessing the SNMP MIB handler property 
     * of the SNMP MIB agent in case of a standalone agent.
     *
     * @return The SNMP MIB handler.
     */
    public SnmpMibHandler getSnmpAdaptor();
    
    /**
     * Sets the reference to the SNMP protocol adaptor through which the 
     * MIB will be SNMP accessible and add this new MIB in the SNMP MIB 
     * handler.
     * <BR>This method is used for setting the SNMP MIB handler property of 
     * the SNMP MIB agent in case of a standalone agent.
     *
     * @param stack The SNMP MIB handler.
     */
    public void setSnmpAdaptor(SnmpMibHandler stack);

    /**
     * Sets the reference to the SNMP protocol adaptor through which the MIB 
     * will be SNMP accessible and add this new MIB in the SNMP MIB handler.
     * This method is to be called to set a specific agent to a specific OID.
     * This can be useful when dealing with MIB overlapping. 
     * Some OID can be implemented in more than one MIB. In this case, the
     * OID nearer agent will be used on SNMP operations.
     * @param stack The SNMP MIB handler.
     * @param oids The set of OIDs this agent implements.
     *
     */
    public void setSnmpAdaptor(SnmpMibHandler stack, SnmpOid[] oids);
    
    /**
     * Sets the reference to the SNMP protocol adaptor through which the MIB 
     * will be SNMP accessible and add this new MIB in the SNMP MIB handler.
     * Adds a new contextualized MIB in the SNMP MIB handler.
     * 
     * @param stack The SNMP MIB handler.
     * @param contextName The MIB context name. If null is passed, will be 
     *        registered in the default context.
     *
     * @exception IllegalArgumentException If the parameter is null.
     *
     */
    public void setSnmpAdaptor(SnmpMibHandler stack, String contextName);

    /**
     * Sets the reference to the SNMP protocol adaptor through which the MIB 
     * will be SNMP accessible and adds this new MIB in the SNMP MIB handler.
     * Adds a new contextualized MIB in the SNMP MIB handler.
     * 
     * @param stack The SNMP MIB handler.
     * @param contextName The MIB context name. If null is passed, will be 
     *        registered in the default context.
     * @param oids The set of OIDs this agent implements.
     * @exception IllegalArgumentException If the parameter is null.
     *
     */
    public void setSnmpAdaptor(SnmpMibHandler stack, 
			       String contextName,
			       SnmpOid[] oids);
    
    /**
     * Gets the object name of the SNMP protocol adaptor to which the MIB is 
     * bound.
     *
     * @return The name of the SNMP protocol adaptor.
     */
    public ObjectName getSnmpAdaptorName();
    
    /**
     * Sets the reference to the SNMP protocol adaptor through which the MIB 
     * will be SNMP accessible and add this new MIB in the SNMP MIB handler 
     * associated to the specified <CODE>name</CODE>.
     *
     * @param name The object name of the SNMP MIB handler.
     *
     * @exception InstanceNotFoundException The MBean does not exist in the 
     *     MBean server.
     * @exception ServiceNotFoundException This SNMP MIB is not registered in
     *    the MBean server or the requested service is not supported.
     */
    public void setSnmpAdaptorName(ObjectName name) 
	throws InstanceNotFoundException, ServiceNotFoundException;
    

    /**
     * Sets the reference to the SNMP protocol adaptor through which the MIB
     * will be SNMP accessible and add this new MIB in the SNMP MIB handler 
     * associated to the specified <CODE>name</CODE>.
     * This method is to be called to set a specific agent to a specific OID. 
     * This can be useful when dealing with MIB overlapping. 
     * Some OID can be implemented in more than one MIB. In this case, the 
     * OID nearer agent will be used on SNMP operations.
     * @param name The name of the SNMP protocol adaptor.
     * @param oids The set of OIDs this agent implements.
     * @exception InstanceNotFoundException The SNMP protocol adaptor does
     *     not exist in the MBean server.
     *
     * @exception ServiceNotFoundException This SNMP MIB is not registered 
     *     in the MBean server or the requested service is not supported.
     *
     */
    public void setSnmpAdaptorName(ObjectName name, SnmpOid[] oids) 
	throws InstanceNotFoundException, ServiceNotFoundException;

    /**
     * Sets the reference to the SNMP protocol adaptor through which the MIB
     * will be SNMP accessible and add this new MIB in the SNMP MIB handler 
     * associated to the specified <CODE>name</CODE>.
     *
     * @param name The name of the SNMP protocol adaptor.
     * @param contextName The MIB context name. If null is passed, will be 
     *     registered in the default context.
     * @exception InstanceNotFoundException The SNMP protocol adaptor does
     *     not exist in the MBean server.
     *
     * @exception ServiceNotFoundException This SNMP MIB is not registered 
     *     in the MBean server or the requested service is not supported.
     *
     */
    public void setSnmpAdaptorName(ObjectName name, String contextName) 
	throws InstanceNotFoundException, ServiceNotFoundException;

     /**
     * Sets the reference to the SNMP protocol adaptor through which the MIB
     * will be SNMP accessible and add this new MIB in the SNMP MIB handler 
     * associated to the specified <CODE>name</CODE>.
     *
     * @param name The name of the SNMP protocol adaptor.
     * @param contextName The MIB context name. If null is passed, will be 
     *        registered in the default context.
     * @param oids The set of OIDs this agent implements.
     * @exception InstanceNotFoundException The SNMP protocol adaptor does
     *     not exist in the MBean server.
     *
     * @exception ServiceNotFoundException This SNMP MIB is not registered 
     *     in the MBean server or the requested service is not supported.
     *
     */
    public void setSnmpAdaptorName(ObjectName name, 
				   String contextName, 
				   SnmpOid[] oids) 
	throws InstanceNotFoundException, ServiceNotFoundException;
    
    /**
     * Indicates whether or not the MIB module is bound to a SNMP protocol 
     * adaptor.
     * As a reminder, only bound MIBs can be accessed through SNMP protocol 
     * adaptor.
     *
     * @return <CODE>true</CODE> if the MIB module is bound, 
     *         <CODE>false</CODE> otherwise.
     */
    public boolean getBindingState();

    /**
     * Gets the MIB name.
     *
     * @return The MIB name.
     */
    public String getMibName();
}
					
