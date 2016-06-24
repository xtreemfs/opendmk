/*
 * @(#)file      SnmpLcd.java
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
package com.sun.management.internal.snmp;

import java.util.Hashtable;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpUnknownModelLcdException;
import com.sun.management.snmp.SnmpUnknownSubSystemException;
/** 
 * Class to extend in order to develop a customized Local Configuration
 * Datastore. The Lcd is used by the <CODE>SnmpEngine</CODE> to store and 
 * retrieve data.
 *<P> <CODE>SnmpLcd</CODE> manages the Lcds needed by every {@link 
 * com.sun.management.internal.snmp.SnmpModel SnmpModel}. It is possible to add 
 * and remove {@link com.sun.management.internal.snmp.SnmpModelLcd SnmpModelLcd}.</P>
 *
 * @since Java DMK 5.1
 */
public abstract class SnmpLcd {

    class SubSysLcdManager {
	private Hashtable models = new Hashtable();
	public void addModelLcd(int id, 
				SnmpModelLcd usmlcd) {
	    models.put(new Integer(id), usmlcd);
	}
	
	public SnmpModelLcd getModelLcd(int id) {
	    return (SnmpModelLcd) models.get(new Integer(id));
	}
	
	public SnmpModelLcd removeModelLcd(int id) {
	    return (SnmpModelLcd) models.remove(new Integer(id));
	}
    }
    

    private Hashtable subs = new Hashtable();
    
    /**
     * Returns the number of time the engine rebooted.
     * @return The number of reboots or -1 if the information is not present in the Lcd.
     */
    public abstract int getEngineBoots();
    /** 
     * Returns the engine Id located in the Lcd.
     * @return The engine Id or null if the information is not present in the Lcd.
     */
    public abstract String getEngineId();
    
    /**
     * Persists the number of reboots.
     * @param i Reboot number.
     */
    public abstract void storeEngineBoots(int i);
    
    /**
     * Persists the engine Id.
     * @param id The engine Id.
     */
    public abstract void  storeEngineId(SnmpEngineId id);
    /**
     * Adds an Lcd model.
     * @param sys The subsystem managing the model.
     * @param id The model Id.
     * @param lcd The Lcd model.
     */
    public void addModelLcd(SnmpSubSystem sys,
			    int id, 
			    SnmpModelLcd lcd) {

	SubSysLcdManager subsys = (SubSysLcdManager) subs.get(sys);
	if( subsys == null ) {
	    subsys = new SubSysLcdManager();
	    subs.put(sys, subsys);
	}
	
	subsys.addModelLcd(id, lcd);
    }
     /**
     * Removes an Lcd model.
     * @param sys The subsystem managing the model.
     * @param id The model Id.
     */
    public void removeModelLcd(SnmpSubSystem sys, int id) 
	throws SnmpUnknownModelLcdException, SnmpUnknownSubSystemException {

	SubSysLcdManager subsys = (SubSysLcdManager) subs.get(sys);
	if( subsys != null ) {
	    SnmpModelLcd lcd = subsys.removeModelLcd(id);
	    if(lcd == null) {
		throw new SnmpUnknownModelLcdException("Model : " + id);
	    }
	}
	else
	    throw new SnmpUnknownSubSystemException(sys.toString());
    }

    /**
     * Gets an Lcd model.
     * @param sys The subsystem managing the model
     * @param id The model Id.
     * @return The Lcd model or null if no Lcd model were found.
     */
    public SnmpModelLcd getModelLcd(SnmpSubSystem sys,
				    int id) {
	SubSysLcdManager subsys = (SubSysLcdManager) subs.get(sys);

	if(subsys == null) return null;

	return (SnmpModelLcd) subsys.getModelLcd(id);
    }
}
