/*
 * @(#)file      SnmpUsmTimelinessModule.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.26
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

import com.sun.management.snmp.SnmpSecurityException;
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.internal.snmp.SnmpEngineImpl;
/**
 * Manages the Timeliness needed for authentication messages. Used by 
 * SnmpUserSecurityModel class.
 *
 * @since Java DMK 5.1
 */
class SnmpUsmTimelinessModule {
    private SnmpUsmExceptionGenerator gen = null;
    
    //Default value is the RFC time window.
    private int timeWindow = SnmpUsm.TIME_WINDOW;

    SnmpUsmTimelinessModule(SnmpUsmExceptionGenerator gen) {
        this.gen = gen;
    }
    /**
     * The Timeliness is handled when sending request and receiving 
     * response or request. In the case of response, a synchronization 
     * is done between the authoritative engine and the non authoritative 
     * engine (managers).
     */
    void handleResponseTimeliness(SnmpUsmEnginePeer peer,
                                  SnmpUsmSecurityParameters params) 
        throws SnmpSecurityException {
        //If values to 0 means that the values are not yet initialized 
	//(discovery response).
        if(peer.getAuthoritativeEngineTime() == 0 &&
           peer.getAuthoritativeEngineBoots() == 0) {
            //The discovery result
            if(logger.finerOn())
                logger.finer("handleResponseTimeliness",
			     "Update time and boot: "
			     + params.getAuthoritativeEngineTime() +":" 
			     + params.getAuthoritativeEngineBoots());
            //Set the values
            peer.setAuthoritativeEngineTime(params.
					    getAuthoritativeEngineTime());
            peer.setAuthoritativeEngineBoots(params.
					     getAuthoritativeEngineBoots());
	    peer.setAuthoritativeEngineLastReceivedTime(params.
					     getAuthoritativeEngineTime());
            return;
        }
	
        int recBootTime = params.getAuthoritativeEngineTime();
        int recNbBoot = params.getAuthoritativeEngineBoots();
	
	if((recBootTime == 0) && (recNbBoot == 0)) {
	    if(logger.finerOn())
                logger.finer("handleResponseTimeliness", 
		      "Received 0:0 value from an engineId discovery, "+
		      "ignoring it");
	    return;
	}
	   

        int bootTime = peer.getAuthoritativeEngineTime();
        int nbBoot = peer.getAuthoritativeEngineBoots();
	int lastReceivedTime = peer.getAuthoritativeLastReceivedEngineTime();

        if(logger.finerOn())
            logger.finer("handleResponseTimeliness", 
		  "Local values time:boot: latestTime " + bootTime + ":" + 
		  nbBoot + ":"+ lastReceivedTime +"\n" +
                  "Received values time:boot: " + recBootTime +":" +
                  recNbBoot);
        //These rules are referenced in RFC 2574.
        if( (recNbBoot > nbBoot) ||
            ( (recNbBoot == nbBoot) && (recBootTime > lastReceivedTime) ) ) {
            if(logger.finerOn())
                logger.finer("handleResponseTimeliness", 
		      "Timeliness succeeded, updating values.");
            peer.setAuthoritativeEngineTime(recBootTime);
            peer.setAuthoritativeEngineBoots(recNbBoot);
	    peer.setAuthoritativeEngineLastReceivedTime(recBootTime);
            return;
        }
        //These rules are referenced in RFC 2574.
        if(recNbBoot == SnmpUsm.MAX_NB_BOOTS) {
	    SnmpSecurityException sec = 
		new SnmpSecurityException("Received engine nb boots is "+
					  "0x7FFFFFFF. Authoritative engine "+
					  "Id [" + peer.authoritativeEngineId+
					  "] needs to be manually "+
					  "reconfigured!");
	    sec.status = SnmpDefinitions.snmpUsmInvalidTimeliness;
	    throw sec;
	}
        //These rules are referenced in RFC 2574.
        if(nbBoot > recNbBoot) {
	    SnmpSecurityException sec = 
		new SnmpSecurityException("Received engine nb boots is "+
					  "smaller than the local notion. "+
					  "The authoritative engine "+
					  "has been reconfigured. "+
					  "Reject the response!");
	    sec.status = SnmpDefinitions.snmpUsmInvalidTimeliness;
	    throw sec;
	}
	//These rules are referenced in RFC 2574.
	//If the received is bigger than the current (more than 150s), it 
	//is OK because the right clock is the Authoritative. We trust it. 
	//It is handled in the first test.
        if( (nbBoot == recNbBoot) &&  
	    ( recBootTime < (bootTime - getTimelinessWindow()) ) ) {
	    SnmpSecurityException sec = 
		new SnmpSecurityException("Received an engine time that is "+
					  "not in the right time frame. "+
					  "Reject the response!");
	    sec.status = SnmpDefinitions.snmpUsmInvalidTimeliness;
	    throw sec;
        }
    }

    /**
     * Timeliness handled when receiving a request.
     */
    void handleTimeliness(SnmpEngineImpl engine,
                          SnmpUsmSecurityParameters params) 
        throws SnmpSecurityException, SnmpStatusException {
        int bootTime = params.getAuthoritativeEngineTime();
        int nbBoot = params.getAuthoritativeEngineBoots();

        //Discovery step 2. Step 1 being the engine Id discovery.
        //These rules are referenced in RFC 2574.
        //
        if(bootTime == 0 && nbBoot == 0) {
            params.setAuthoritativeEngineTime(engine.getEngineTime());
            params.setAuthoritativeEngineBoots(engine.getEngineBoots());
	    
            if(logger.finerOn())
                logger.finer("handleTimeliness", "Boot time discovery. "+
			     "Sending engine boot: " + 
			     params.getAuthoritativeEngineBoots() +
			     ", engine time: " + 
			     params.getAuthoritativeEngineTime());
            //Time window exception.
            gen.genTimeWindowException(null,
                                       null,
                                       (byte)(SnmpDefinitions.authNoPriv | 
                                              SnmpDefinitions.reportableFlag),
                                       params);
        }
        //These rules are referenced in RFC 2574.
        if( (nbBoot == SnmpUsm.MAX_NB_BOOTS) ||
            (nbBoot != engine.getEngineBoots()) ) {
            if(logger.finerOn())
                logger.finer("handleTimeliness", "Invalid boots: " +
                      "Received boot: " + nbBoot +
                      ", Local engine boot: " + engine.getEngineBoots());
            //Time window exception.
            gen.genTimeWindowException(null,
                                       null,
                                       (byte)(SnmpDefinitions.authNoPriv | 
                                              SnmpDefinitions.reportableFlag),
                                       params);
        }
	
        //These rules are referenced in RFC 2574.
        if( Math.abs(bootTime - engine.getEngineTime()) > 
            (getTimelinessWindow() ) ) {
            if(logger.finerOn())
                logger.finer("handleTimeliness", "Invalid time: " +
                      "Received time: " + bootTime +
                      ", Local engine time: " + engine.getEngineTime());
            //Time window exception.
            gen.genTimeWindowException(null,
                                       null,
                                       (byte)(SnmpDefinitions.authNoPriv | 
                                              SnmpDefinitions.reportableFlag),
                                       params);
        }
	
    }
     /**
     * Get the time window used for timeliness checks. If none are set, 
     * the <CODE>SnmpUsm.TIMEWINDOW</CODE> is the default used.
     * @return The time window in seconds.
     */
    synchronized int getTimelinessWindow() {
	return timeWindow;
    }
    
    /**
     * Set the time window used for timeliness checks. If none are set, 
     * the <CODE>SnmpUsm.TIMEWINDOW</CODE> is the default used.
     * @param t The time window in seconds.
     */
    synchronized void setTimelinessWindow(int t) {
	timeWindow = t;
    }

    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpUsmTimelinessModule");
    
    String dbgTag = "SnmpUsmTimelinessModule";  
}
