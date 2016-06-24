/*
 * @(#)file      SnmpSubSystemImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.14
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
import java.util.Enumeration;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpUnknownModelException;
/**
 * FOR INTERNAL USE ONLY. The SnmpSubSystem interface implementation.
 *
 * @since Java DMK 5.1
 */
class SnmpSubSystemImpl implements SnmpSubSystem {
    Hashtable models = new Hashtable();
    SnmpEngine engine = null;

    protected SnmpSubSystemImpl(SnmpEngine engine) {
	this.engine = engine;
    }

    /**
     * See SnmpSubSystem interface doc.
     */
    public SnmpEngine getEngine() { 
	return engine; 
    }

    /**
     * See SnmpSubSystem interface doc.
     */
    public void addModel(int id,
			 SnmpModel model) {
	models.put(new Integer(id), model);
    }

    /**
     * See SnmpSubSystem interface doc.
     */
    public SnmpModel removeModel(int id) throws SnmpUnknownModelException {
	SnmpModel mod = (SnmpModel) models.remove(new Integer(id));
	if(mod == null) throw new SnmpUnknownModelException("Unknown model: " +
							    id);
	return mod;
    }
    
    /**
     * See SnmpSubSystem interface doc.
     */
    public SnmpModel getModel(int id) throws SnmpUnknownModelException {
	
	SnmpModel mod = (SnmpModel) models.get(new Integer(id)); 
	if(mod == null) throw new SnmpUnknownModelException("Unknown model: " +
							    id);
	return mod;
    } 
    
    /**
     * See SnmpSubSystem interface doc.
     */
    public int[] getModelIds() {
	int []res = new int[models.size()];
	int i = 0;
	for (Enumeration e = models.keys() ; e.hasMoreElements() ;) {
	    Integer key = (Integer) e.nextElement();
	    res[i] = key.intValue();
	    i++;
	}
	return res;
    }
     
    /** 
     * Returns the set of model names that have been registered within the sub system.
     */
    public String[] getModelNames() {
	String []res = new String[models.size()];
	int i = 0;
	Enumeration v = models.elements();
	for (Enumeration e = models.keys(); e.hasMoreElements() ;) {
	    SnmpModel mod = (SnmpModel) v.nextElement();
	    Integer key = (Integer) e.nextElement();
	    res[i] = mod.getName() + " (" + key.intValue() + ")";
	    i++;
	}
	return res;
    }
}
