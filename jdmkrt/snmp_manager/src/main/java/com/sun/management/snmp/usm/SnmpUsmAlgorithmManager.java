/*
 * @(#)file      SnmpUsmAlgorithmManager.java
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

import java.util.Hashtable;
import java.util.Enumeration;

import com.sun.jdmk.internal.ClassLogger;

/**
 * Usm Algorithm manager. An algorithm manager manages SnmpUsmAlgorithm. 
 * Algorithm can be added or removed. The manager is used by the {@link 
 * com.sun.management.snmp.usm.SnmpUsmLcd SnmpUsmLcd} to :
 * <ul>
 * <li> Populate algorithm from configuration. </li>
 * <li> Associate algorithm to users. </li>
 * </ul>
 *
 * @since Java DMK 5.1
 */
public class  SnmpUsmAlgorithmManager {
     //Algorithm storage.
    private Hashtable algos = new Hashtable();
    private Hashtable algosOid = new Hashtable();
     /**
     * Gets the list of registered algorithm names. 
     * @return The registered algorithm names.
     */
    public String[] getManagedAlgorithms() {
        String []res = new String[algos.size()];
        int i = 0;
        for (Enumeration e = algos.keys() ; e.hasMoreElements() ;) {
            String key = (String) e.nextElement();
            res[i] = key;
            i++;
        }
        return res;
    }

    /**
     * Adds an algorithm. If an algorithm with the same name exists, is is 
     * replaced by the new one.
     * @param a The algorithm to be added.
     */
    public void addAlgorithm(SnmpUsmAlgorithm a) {
        if(logger.finerOn()) 
            logger.finer("addAlgorithm", "algo name : " + a.getAlgorithm());
        algos.put(a.getAlgorithm(), a);
	algosOid.put(a.getOid(), a);
    }

    /**
     * Removes the algorithm associated with the passed name. Does nothing if 
     * it doesn't exist.
     * @param name The name of the algorithm to be removed.
     * @return The algorithm to be removed.
     */
    public SnmpUsmAlgorithm removeAlgorithm(String name) {
	SnmpUsmAlgorithm algo = (SnmpUsmAlgorithm) algos.remove(name);
	
	if(algo != null)
	    algosOid.remove(algo.getOid());
       
	return algo;
    }

    /**
     * Gets the associated algorithm.
     * @param name The name of the algorithm to retrieve.
     * @return The associated algorithm.
     */
    public SnmpUsmAlgorithm getAlgorithm(String name) { 
        if(logger.finerOn()) 
            logger.finer("getAlgorithm", "algo name : " + name);
	
	//Look for by name 
	SnmpUsmAlgorithm algo = (SnmpUsmAlgorithm) algos.get(name);
	if(algo == null) 
	    algo = (SnmpUsmAlgorithm) algosOid.get(name);
	
	return algo;
    }

    // Logging
    //--------
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,
			"SnmpUsmAlgorithmManager");

    String dbgTag = "SnmpUsmAlgorithmManager"; 
}
