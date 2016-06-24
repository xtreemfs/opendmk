/*
 * @(#)file      UsmUserEntryMetaImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.18
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
package com.sun.management.snmp.usm.usmmib;

// jmx imports
//
import javax.management.MBeanServer;
import com.sun.management.snmp.SnmpCounter;
import com.sun.management.snmp.SnmpCounter64;
import com.sun.management.snmp.SnmpGauge;
import com.sun.management.snmp.SnmpInt;
import com.sun.management.snmp.SnmpUnsignedInt;
import com.sun.management.snmp.SnmpIpAddress;
import com.sun.management.snmp.SnmpTimeticks;
import com.sun.management.snmp.SnmpOpaque;
import com.sun.management.snmp.SnmpString;
import com.sun.management.snmp.SnmpStringFixed;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpScopedPduPacket;
// jdmk imports
//
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.usm.SnmpUsmSecureUser;
import com.sun.management.snmp.usm.SnmpUsm;
import com.sun.management.snmp.usm.SnmpUsmPrivPair;
import com.sun.management.snmp.usm.SnmpUsmAuthPair;
import com.sun.management.snmp.usm.SnmpUsmPrivAlgorithm;
import com.sun.management.snmp.usm.SnmpUsmAuthAlgorithm;

import com.sun.management.snmp.agent.SnmpMibNode;
import com.sun.management.snmp.agent.SnmpMib;
import com.sun.management.snmp.agent.SnmpMibEntry;
import com.sun.management.snmp.agent.SnmpStandardObjectServer;
import com.sun.management.snmp.agent.SnmpStandardMetaServer;
import com.sun.management.snmp.agent.SnmpMibSubRequest;
import com.sun.management.snmp.agent.SnmpMibTable;
import com.sun.management.snmp.EnumRowStatus;

import java.util.Enumeration;

/**
 * This meta is common to every entry. It has been customized to make some 
 * check before accepting some set requests (eg:cloning).
 *
 * @since Java DMK 5.1
 */
class UsmUserEntryMetaImpl extends UsmUserEntryMeta {
    private static final long serialVersionUID = 1380866985476444282L;
    private String dbgTag = "UsmUserEntryMetaImpl";
    public UsmUserEntryMetaImpl(SnmpMib myMib,
				SnmpStandardObjectServer objserv) {
	super(myMib, objserv);
    }

    public void check(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {
	boolean setClone = false;
	if(logger.finestOn()) {
	    logger.finest("check","Checking varbind. Depth : "+ depth);
	}

	//Check if the row status is compatible with storage type
	SnmpVarBind rowStat = req.getRowStatusVarBind();
	
	if(rowStat != null) {
	    if(logger.finestOn()) {
		logger.finest("check","Row status : " + rowStat);
	    }
	    int e = new EnumRowStatus("destroy").intValue();
	    int val = ((SnmpInt)rowStat.getSnmpValue()).intValue();
	    if(e == val) {
		if(logger.finestOn()) {
		    logger.finest("check","Destroy Row status .");
		}
		
		UsmUserEntryImpl ent = (UsmUserEntryImpl) node;
		if(ent.getUsmUserStorageType().toString().equals("permanent")||
		   ent.getUsmUserStorageType().toString().equals("readOnly")) {
		    //IS IT NO ACCESS?
		    //
		    if(logger.finestOn()) {
			logger.finest("check","Illegal storage type.");
		    }
		    throw new SnmpStatusException(SnmpStatusException.noAccess);
		}
	    }   
	}
	else {
	    if(logger.finestOn()) {
		logger.finest("check","Row status is null");
	    }
	}
	
	//We are identifying if the clone OID is in the list.
	for(Enumeration e = req.getElements(); e.hasMoreElements() ;) {
	    SnmpVarBind var = (SnmpVarBind) e.nextElement();
	    if(var.getOid().getOidArc(depth) == 4) {
		if(logger.finestOn()) {
		    logger.finest("check","setClone = true");
		}
		setClone = true;
		break;
	    }
	}

	// Check needed for random and delta size
	SnmpVarBind authKeyChange = null;
	SnmpVarBind privKeyChange = null;
	SnmpVarBind random = null;
	UsmUserEntryImpl ent = (UsmUserEntryImpl) node;
	//The user must be the row one.
	SnmpUsmSecureUser user = ent.getUser();
	
	for(Enumeration e = req.getElements(); e.hasMoreElements() ;) {
	    SnmpVarBind var = (SnmpVarBind) e.nextElement();
	    int arc = (int) var.getOid().getOidArc(depth);
	    if(logger.finestOn())
		logger.finest("check", "checking OID arc : " + arc);

	    // auth key change
	    if( (arc == 6) || (arc == 7) )
		authKeyChange = var;

	    // priv key change
	    if( (arc == 9) || (arc == 10) )
		privKeyChange = var;
	    
	    //random
	    if( arc == 11)
		random = var;
	    
	    if( (arc == 7) || (arc == 10) ) {
		if(logger.finestOn())
		    logger.finest("check","User : " + user.getName());
		//The request user name.
		SnmpScopedPduPacket p = 
		    (SnmpScopedPduPacket) req.getPdu();
		String principal = p.securityParameters.getPrincipal();
		if(logger.finestOn())
		    logger.finest("check","Principal : " + principal + 
			  " Security model : " + p.msgSecurityModel);
		int msgSecurityModel = p.msgSecurityModel;
		//Must be equal for a set.
		if(!principal.equals(user.getName())) {
		    if(logger.finestOn())
			logger.finest("check","Principal are not equals : " + 
			      principal +
			      " / " + user.getName());
		    throw new SnmpStatusException(SnmpStatusException.noAccess);
		}
		//The security model MUST be Usm.
		if(msgSecurityModel != SnmpUsm.ID) {
		    if(logger.finestOn())
			logger.finest("check","Security model is not usm: " + 
			      msgSecurityModel);
		    throw new SnmpStatusException(SnmpStatusException.noAccess);
		}
	    }
	}
    
	//Checking the size
	if(random != null) {
	    SnmpString val = (SnmpString) random.getSnmpValue();
	    int received = val.byteValue().length;
	    if(authKeyChange != null) {
		SnmpUsmAuthPair p = user.getAuthPair();
		if(p != null) {
		    SnmpUsmAuthAlgorithm a = p.algo;
		    if(a != null) {
			int expectedSize = a.getDeltaSize();
			if(received != expectedSize) {
			    if(logger.finestOn())
				logger.finest("check",
				      "Wrong random size for authKeyChange");
			    if(logger.finestOn())
				logger.finest("check", "expected : " + 
					      expectedSize +
					      "received : " + received);
			    throw new SnmpStatusException(
                                      SnmpStatusException.snmpRspWrongValue);
			}
		    }
		}
	    }
	    else {
		if(privKeyChange != null) {
		    SnmpUsmPrivPair p = user.getPrivPair();
		    if(p != null) {
			SnmpUsmPrivAlgorithm a = p.algo;
			if(a != null) {
			    int expectedSize = a.getDeltaSize();
			    if(received != expectedSize) {
				if(logger.finestOn())
				    logger.finest("check",
				       "Wrong random size for privKeyChange");
				if(logger.finestOn())
				    logger.finest("check", "expected : " + 
					  expectedSize +
					  "received : " + received);
				throw new SnmpStatusException(
                                        SnmpStatusException.snmpRspWrongValue);
			    }
			}
		    }
		}	
	    }
	}
	
	if(logger.finestOn())
	    logger.finest("check","Calling super.");
	super.check(req, depth);
	if(logger.finestOn())
	    logger.finest("check","Done.");
	
    }
    
    /**
     * Ask the user to persist the new values.
     */
    public void set(SnmpMibSubRequest req, int depth)
        throws SnmpStatusException {

	for(Enumeration e = req.getElements(); e.hasMoreElements() ;) {
	    SnmpVarBind var = (SnmpVarBind) e.nextElement();
	    int arc = (int) var.getOid().getOidArc(depth);
	    if(logger.finestOn())
		logger.finest("check", "checking OID arc : " + arc);
	    if(arc == 11) {
		if(logger.finestOn())
		    logger.finest("check", "Found usmUserPublic, "+
				  "must be set before keychange.");
		node.setUsmUserPublic(((SnmpString)var.getSnmpValue()).
				      toByte());
	    }
	}
	if(logger.finestOn())
	    logger.finest("set","Calling super.");
        super.set(req,depth);
	if(logger.finestOn())
	    logger.finest("set","Updating lcd.");
	SnmpUsmSecureUser user = ((UsmUserEntryImpl)node).getUser();
	user.updateConfiguration();	
	if(logger.finestOn())
	    logger.finest("set","Lcd updated.");
    }

    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_ADAPTOR_SNMP,
			"UsmUserEntryMetaImpl");

}
