/*
 * @(#)file      SnmpUsmEnginePeer.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.20
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
package com.sun.management.snmp.usm;
import com.sun.management.snmp.SnmpEngineId;
/**
 * FOR INTERNAL USE ONLY. This peer object models a distant engine Id. It is used by {@link com.sun.management.snmp.manager.usm.SnmpUsmPeer} for timeliness synchronization.
 * It stores the timeliness parameters.
 *
 * @since Java DMK 5.1
 */
public class SnmpUsmEnginePeer {
    //Private fields.
    SnmpEngineId authoritativeEngineId = null;
    private int authoritativeEngineBoots = 0;
    private int authoritativeEngineTime = 0;
    private long startTime = 0;
    private int lastReceivedEngineTime = 0;

    //Constructor.
    SnmpUsmEnginePeer(SnmpEngineId authoritativeEngineId) {
	this.authoritativeEngineId = authoritativeEngineId;
    }
    /**
     * Gets the engine Id.
     * @return The engine Id.
     */
    public SnmpEngineId getAuthoritativeEngineId() {
	return authoritativeEngineId;
    }
    
    /**
     * Gets the engine nb boots.
     * @return The engine nb boots.
     */
    public int getAuthoritativeEngineBoots() {
	return authoritativeEngineBoots;
    }
    
    /**
     * Gets an updated engine time.
     * @return The engine time.
     */
    public synchronized int getAuthoritativeEngineTime() {
	if(authoritativeEngineTime == 0) return 0;
	
	long delta = (System.currentTimeMillis() / 1000) - startTime;
	if(delta >  0x7FFFFFFF) {
	    //67 years of running. That is a great thing!
	    //Reinitialize startTime.
	    startTime = System.currentTimeMillis() / 1000;
	    //Can't do anything with this counter.
	    if(authoritativeEngineBoots != 0x7FFFFFFF)
		setAuthoritativeEngineBoots(authoritativeEngineBoots + 1);
	}

	return authoritativeEngineTime + ( (int) ((System.currentTimeMillis() / 1000) - startTime) );
    }
     /**
     * Gets the last received engine time.
     * @return The last received engine time.
     */
    public synchronized int getAuthoritativeLastReceivedEngineTime() {
	return lastReceivedEngineTime;
    }
    /**
     * Clean the previous engine time and boots.
     */
    public synchronized void reset() {
	setAuthoritativeEngineBoots(0);
	setAuthoritativeEngineTime(0);
	setAuthoritativeEngineLastReceivedTime(0);
    } 
    /**
     * Set the engine nb boots. Can only be done by Usm model.
     */
    synchronized void setAuthoritativeEngineBoots(int boots) {
	authoritativeEngineBoots = boots;
    }

    /**
     * Set the engine time. Can only be done by Usm model.
     */
    synchronized void setAuthoritativeEngineTime(int time) {
	startTime = System.currentTimeMillis() / 1000;
	authoritativeEngineTime = time;
    }

    /**
     * Set the engine time. Can only be done by Usm model.
     */
    synchronized void setAuthoritativeEngineLastReceivedTime(int time) {
	lastReceivedEngineTime = time;
    }
}
