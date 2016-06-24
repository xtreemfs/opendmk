/*
 * @(#)file      SnmpSessionProvider.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.22
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

// Java DMK import
//
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpEngineId;

// RI import
//
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.manager.SnmpPeer;
import com.sun.management.snmp.manager.SnmpRequest;
import com.sun.management.snmp.manager.SnmpSession;
import com.sun.management.snmp.manager.SnmpOptions;
import com.sun.management.snmp.SnmpStatusException;

/**
 * Provides an <CODE>SnmpSession</CODE> to the <CODE>SnmpProxy</CODE>. 
 * It handles and hides<CODE>SnmpSession</CODE> details to allow 
 * <CODE>SnmpProxy</CODE> to deal with protocol conversion only.
 * 
 *
 * @since Java DMK 5.1
 */
class SnmpSessionProvider {
    private SnmpSession fixSession = null;
    private SnmpSession session = null;
    private int peerVersion = 0;
    private String dbgTag = "SnmpSessionProvider";
    
    /**
     * Initializes a SnmpSessionProvider with a peer. This peer has been 
     * constructed by <CODE> SnmpProxy </CODE> instance.
     * @param peer The snmp agent peer object.
     *
     */
    
    public SnmpSessionProvider(SnmpPeer peer,
			       SnmpSession session) {
	try {
	    peerVersion = peer.getParams().getProtocolVersion();
	    this.session = session;
	    SnmpEngine engine = session.getEngine();
	    session.setDefaultPeer(peer);
	    session.snmpOptions.setPduFixedOnError(false);

	    fixSession = new SnmpSession(engine, 
					 "Fixed error Snmp session",
					 null);
	    fixSession.setDefaultPeer(peer);
	    fixSession.snmpOptions.setPduFixedOnError(true);
	} catch(SnmpStatusException e) { 
	    if(logger.finestOn())
		logger.finest("SnmpSessionProvider", e);
	}
    }

    /**
     * Gets the session according to the manager version provided.
     *
     * @param managerVersion The current calling manager.
     *
     * @return The dedicated session object.
     */
    SnmpSession getSession(int managerVersion) {
	if(logger.finestOn())
	    logger.finest("getSession", "Manager protocol : " + managerVersion +
		  "\nPeer protocol :" + peerVersion);
	
	if(peerVersion == managerVersion)
	    return session;
	
	if(peerVersion == SnmpDefinitions.snmpVersionOne) {
	    if(logger.finestOn())
		logger.finest("getSession", "Return fixed session");
	    return fixSession;
	}
	else
	    return session;
    }

     /**
     * Gets the session compatible with the peer (No fix has been done).
     *
     * @return The session object.
     */
    SnmpSession getDefaultSession() {
	return session;
    }
    
    /**
     * Hides invalid response errors.
     * <p>
     * Invalid response requests (e.g. invalid BER encoding) are dropped 
     * by the manager API. This makes the requests to timeout. This 
     * behavior can be changed by calling this method and passing it false. 
     * By default the errors are hidden.
     * @param hide True, hide the internal errors, false throw a GenError.
     */
    public synchronized void hideInvalidResponseError(boolean hide) {
	session.hideInvalidResponseError(hide);
	fixSession.hideInvalidResponseError(hide);
    }
    
    /**
     * Checks if invalid response errors are hidden.
     * <p>
     * Invalid response requests (e.g. invalid BER encoding) are dropped 
     * by the manager API. This makes the requests to timeout. This behavior
     * can be changed by calling the method 
     * <CODE>hideInvalidResponseError</CODE>. By default the errors are hidden.
     * @return hide True, hide the internal errors, false throw a GenError.
     */
    public synchronized boolean isInvalidResponseErrorHidden() {
	return session.isInvalidResponseErrorHidden();
    }
    
    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP, "SnmpSessionProvider");
}
