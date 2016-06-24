/*
 * @(#)file      SnmpUsmExceptionGenerator.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.16
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
import com.sun.management.snmp.SnmpCounter;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;

/**
 *
 * @since Java DMK 5.1
 */
class SnmpUsmExceptionGenerator {
    SnmpUserSecurityModel model = null;
    SnmpUsmExceptionGenerator(SnmpUserSecurityModel model) {
	this.model = model;
    }
    /**
     * Generic exception generation.
     */
    void genSecurityException(String oid,
			      String msg,
			      byte[] contextEngineId,
			      byte[] contextName,
			      byte msgFlags,
			      SnmpUsmSecurityParameters params) 
	throws SnmpSecurityException, SnmpStatusException {
	SnmpVarBind[] l = new SnmpVarBind[1];
	//The counter OID.
	SnmpVarBind varbind = new SnmpVarBind(oid);
	l[0] = varbind;
	//Counter MUST have been previously incremented.	
	varbind.setSnmpValue(new SnmpCounter(model.getNotInTimeWindowsCounter().intValue()));
	SnmpSecurityException ex = new 
	    SnmpSecurityException(msg);
	ex.list =l;
	ex.params = params;
	ex.contextEngineId = contextEngineId;
	ex.contextName = contextName;
	//Only reportable is reused from the received flags. 
	//Security is erased.
	ex.flags = (byte) (msgFlags & SnmpDefinitions.reportableFlag);
	throw ex;
    }
    /**
     * To any possible error a counter is incremented and an exception is thrown. The msg processing model that catches the exception will send the appropriate report if the exception flag is set.
     */
    void genTimeWindowException(byte[] contextEngineId,
				byte[] contextName,
				byte msgFlags,
				SnmpUsmSecurityParameters params) 
	throws SnmpSecurityException, SnmpStatusException {
	SnmpVarBind[] l = new SnmpVarBind[1];
	//The counter OID.
	SnmpVarBind varbind = new SnmpVarBind(SnmpUsm.usmStatsNotInTimeWindows);
	l[0] = varbind;
	varbind.setSnmpValue(new SnmpCounter(model.incNotInTimeWindowsCounter(1)));
	SnmpSecurityException ex = new 
	    SnmpSecurityException("notInTimeWindow");
	ex.list =l;
	ex.params = params;
	ex.contextEngineId = contextEngineId;
	ex.contextName = contextName;
	//Security is not erased for timeliness reports.
	ex.flags = (byte) (SnmpDefinitions.authNoPriv | SnmpDefinitions.reportableFlag);
	throw ex;
    }
    
    /**
     * To any possible error a counter is incremented and an exception is thrown. The msg processing model that catches the exception will send the appropriate report if the exception flag is set.
     */
    void genEngineIdException(byte[] contextEngineId,
			      byte[] contextName,
			      byte msgFlags,
			      SnmpUsmSecurityParameters params) 
	throws SnmpSecurityException, SnmpStatusException {
	SnmpVarBind[] l = new SnmpVarBind[1];
	SnmpVarBind varbind = new SnmpVarBind(SnmpUsm.usmStatsUnknownEngineIds);
	l[0] = varbind;
	varbind.setSnmpValue(new SnmpCounter(model.incUnknownEngineIdsCounter(1)));
	SnmpSecurityException ex = new 
	    SnmpSecurityException("unknownEngineId");
	ex.list =l;
	ex.params = params;
	ex.contextEngineId = contextEngineId;
	ex.contextName = contextName;
	//Only reportable is reused from the received flags. 
	//Security is erased.
	ex.flags = (byte) (msgFlags & SnmpDefinitions.reportableFlag);
	throw ex;
    }

    /**
     * To any possible error a counter is incremented and an exception is thrown. The msg processing model that catches the exception will send the appropriate report if the exception flag is set.
     */
    void genAuthenticationException(byte[] contextEngineId,
				    byte[] contextName,
				    byte msgFlags,
				    SnmpUsmSecurityParameters params) 
	throws SnmpSecurityException, SnmpStatusException {
	SnmpVarBind[] l = new SnmpVarBind[1];
	SnmpVarBind varbind = new SnmpVarBind(SnmpUsm.usmStatsWrongDigests);
	l[0] = varbind;
	varbind.setSnmpValue(new SnmpCounter(model.incWrongDigestsCounter(1)));
	
	SnmpSecurityException ex = new 
	    SnmpSecurityException("authenticationFailure");
	ex.list =l;
	ex.params = params;
	ex.contextEngineId = contextEngineId;
	ex.contextName = contextName;
	//Only reportable is reused from the received flags. 
	//Security is erased.
	ex.flags = (byte) (msgFlags & SnmpDefinitions.reportableFlag);
	throw ex;
    }

    /**
     * To any possible error a counter is incremented and an exception is thrown. The msg processing model that catches the exception will send the appropriate report if the exception flag is set.
     */
    void genDecryptionException(byte msgFlags,
				SnmpUsmSecurityParameters params) 
	throws SnmpSecurityException, SnmpStatusException {
	SnmpVarBind[] l = new SnmpVarBind[1];
	SnmpVarBind varbind = new SnmpVarBind(SnmpUsm.usmStatsDecryptionErrors);
	l[0] = varbind;
	varbind.setSnmpValue(new SnmpCounter(model.incDecryptionErrorsCounter(1)));
	
	SnmpSecurityException ex = new 
	    SnmpSecurityException("decryptionError");
	ex.list =l;
	ex.params = params;
	//Only reportable is reused from the received flags. 
	//Security is erased.
	ex.flags = (byte) (msgFlags & SnmpDefinitions.reportableFlag);
	throw ex;
    }

    /**
     * To any possible error a counter is incremented and an exception is thrown. The msg processing model that catches the exception will send the appropriate report if the exception flag is set.
     */
    void genSecurityLevelException(byte[] contextEngineId,
				   byte[] contextName,
				   byte msgFlags,
				   SnmpUsmSecurityParameters params)
	throws SnmpSecurityException, SnmpStatusException  {
	SnmpVarBind[] l = new SnmpVarBind[1];
	SnmpVarBind varbind = new SnmpVarBind(SnmpUsm.usmStatsUnsupportedSecLevels);
	l[0] = varbind;
	varbind.setSnmpValue(new SnmpCounter(model.incUnsupportedSecLevelsCounter(1)));

	SnmpSecurityException ex = new 
	    SnmpSecurityException("unsupportedSecurityLevel");
	ex.list =l;
	ex.params = params;
	ex.contextEngineId = contextEngineId;
	ex.contextName = contextName;
	//Only reportable is reused from the received flags. 
	//Security is erased.
	ex.flags = (byte) (msgFlags & SnmpDefinitions.reportableFlag);
	throw ex;
    }

   /**
     * To any possible error a counter is incremented and an exception is thrown. The msg processing model that catches the exception will send the appropriate report if the exception flag is set.
     */
    void genUserNameException(byte[] contextEngineId,
			      byte[] contextName,
			      byte msgFlags,
			      SnmpUsmSecurityParameters params) 
	throws SnmpSecurityException, SnmpStatusException {
	SnmpVarBind[] l = new SnmpVarBind[1];
	SnmpVarBind varbind = new SnmpVarBind(SnmpUsm.usmStatsUnknownUserNames);
	l[0] = varbind;
	varbind.setSnmpValue(new SnmpCounter(model.incUnknownUserNamesCounter(1)));
	
	SnmpSecurityException ex = new 
	    SnmpSecurityException("unknownSecurityName");
	ex.list =l;
	ex.params = params;
	ex.contextEngineId = contextEngineId;
	ex.contextName = contextName;
	//Only reportable is reused from the received flags. 
	//Security is erased.
	ex.flags = (byte) (msgFlags & SnmpDefinitions.reportableFlag);
	throw ex;
    }
}
