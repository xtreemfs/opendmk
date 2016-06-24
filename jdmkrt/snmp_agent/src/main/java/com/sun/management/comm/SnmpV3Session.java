/*
 * @(#)file      SnmpV3Session.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.12
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


package com.sun.management.comm;


// java imports
//
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;
import java.net.InetAddress;
import java.net.SocketException;
import java.io.InterruptedIOException;

// jmx imports
//
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.SnmpScopedPduRequest;


/**
 * This class is used for sending INFORM REQUESTS from an agent to a manager. 
 * Inform are sent in SNMP V3 messages.
 *
 * Creates, controls, and manages one or more inform requests.
 * 
 * The SnmpSession maintains the list of all active inform requests and 
 * inform responses.
 * Each SnmpSession has a dispatcher that is a thread used to service all 
 * the inform requests it creates and each SnmpSession uses a separate socket
 * for sending/receiving inform requests/responses.
 *
 * An SnmpSession object is associated with an SNMP adaptor server. 
 * It is created the first time an inform request is sent by the SNMP 
 * adaptor server  and is destroyed (with its associated SnmpSocket) when 
 * the SNMP adaptor server is stopped.
 *
 *
 * @since Java DMK 5.1
 */

class SnmpV3Session extends SnmpSession {
    SnmpV3AdaptorServer v3adaptor = null;
    //Use to set the security cache by SnmpV3InformRequest
    SnmpV3ResponseHandler respHandler = null;
    /**
     * Constructor that initialize SnmpSession state as well as its state.
     */
    SnmpV3Session(SnmpV3AdaptorServer v3adaptor)  throws SocketException {
	//SnmpSession state initialization.
	adaptor = v3adaptor;
	snmpQman = new SnmpQManager();
	//SnmpV3Session one
	this.v3adaptor = v3adaptor;
	//Instantiate a V3 enabled response handler.
	respHandler = new SnmpV3ResponseHandler(v3adaptor,
						snmpQman);
	//Call SnmpSession method with V3 enabled response handler.
	initialize(v3adaptor, respHandler);
    }
    
    /**
     * Sends an inform request to the specified InetAddress destination 
     * using the specified community string.
     * @param addr The InetAddress destination for this inform request.
     * @param pdu The pdu to be used for the inform request.
     * @param cb The callback that is invoked when a request is complete.
     * @param vblst A list of SnmpVarBind instances or null.
     * @exception SnmpStatusException SNMP adaptor is not ONLINE or 
     *        session is dead.
     */
    SnmpV3InformRequest makeAsyncRequest(InetAddress addr, 
					 SnmpScopedPduRequest pdu, 
					 SnmpInformHandler cb, 
					 SnmpVarBindList vblst)
        throws SnmpStatusException {
        
        if (!isSessionActive()) {
            throw new SnmpStatusException("SNMP adaptor server not ONLINE");
        }
        SnmpV3InformRequest snmpreq = new SnmpV3InformRequest(this, 
							      respHandler,
							      v3adaptor, 
							      addr, 
							      pdu, 
							      cb);
        snmpreq.start(vblst);
        return snmpreq;
    }
}
