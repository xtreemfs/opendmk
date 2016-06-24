/*
 * @(#)file      SnmpUsmSecurityParametersImpl.java
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
package com.sun.management.snmp.usm;

import com.sun.management.snmp.SnmpSecurityParameters;
import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.BerDecoder;
import com.sun.management.snmp.BerEncoder;
import com.sun.management.snmp.BerException;
import com.sun.management.snmp.SnmpTooBigException;
import com.sun.management.snmp.SnmpStatusException;
/**
 * FOR INTERNAL USE ONLY.
 * This class models the set of security parameters needed to activate security in the User Security Model. They are RFC 2574 compliant.
 * These parameters are responsible for their own encoding and decoding in BER. 
 *
 * @since Java DMK 5.1
 */

class SnmpUsmSecurityParametersImpl implements SnmpUsmSecurityParameters {
    // The authoritative engine Id.
    private SnmpEngineId authoritativeEngineId = null;
    // The authoritative engine boots.
    private int authoritativeEngineBoots = 0;
    // The authoritative engine time.
    private int authoritativeEngineTime = 0;
     // The user name.
    private String userName = null;
    // Authentication parameters (HMAC).
    private byte[] authParameters = null;
    // Encryption parameters (eg : DES IV parameter).
    private byte[] privParameters = null;
    
    public String getPrincipal() {
	return getUserName();
    }

    public SnmpEngineId getAuthoritativeEngineId() {
	return authoritativeEngineId;
    }

    public void setAuthoritativeEngineId(SnmpEngineId authoritativeEngineId)
    {
	this.authoritativeEngineId = authoritativeEngineId;
    }
    
    public int getAuthoritativeEngineBoots() {
	return authoritativeEngineBoots;
    }

    public void setAuthoritativeEngineBoots(int authoritativeEngineBoots) {
	this.authoritativeEngineBoots = authoritativeEngineBoots;
    }
    
    public int getAuthoritativeEngineTime() {
	return authoritativeEngineTime;
    }

    public void setAuthoritativeEngineTime(int authoritativeEngineTime) {
	this.authoritativeEngineTime = authoritativeEngineTime;
    }

    public String getUserName() {
	return userName;
    }

    public void setUserName(String userName) {
	this.userName = userName;
    }
    
    public byte[] getAuthParameters() {
	return authParameters;
    }
    
    public void setAuthParameters(byte[] authParameters) {
	this.authParameters = authParameters;
    }

    public byte[] getPrivParameters() {
	return privParameters;
    }

    public void setPrivParameters(byte[] privParameters) {
	this.privParameters = privParameters;
    }

    public SnmpUsmSecurityParametersImpl() {
    }
    
    public SnmpUsmSecurityParametersImpl(SnmpEngineId authoritativeEngineId,
					 int authoritativeEngineBoots,
					 int authoritativeEngineTime,
					 String userName,
					 byte[] authParameters,
					 byte[] privParameters) {
	this.authoritativeEngineId = authoritativeEngineId;
	this.authoritativeEngineBoots = authoritativeEngineBoots;
	this.authoritativeEngineTime = authoritativeEngineTime;
	this.userName = userName;
	this.authParameters = authParameters;
	this.privParameters = privParameters;
    }
    
    public SnmpUsmSecurityParametersImpl(SnmpUsmSecurityParameters impl) {
	this.authoritativeEngineId = impl.getAuthoritativeEngineId();
	this.authoritativeEngineBoots = impl.getAuthoritativeEngineBoots();
	this.authoritativeEngineTime = impl.getAuthoritativeEngineTime();
	this.userName = impl.getUserName();
    }
    
    /**
     * Decode the parameters contained in the passed byte array. BER decoding.
     */
    public void decode(byte[] params) throws SnmpStatusException {
	try {
	    BerDecoder bdec = new BerDecoder(params);
	    bdec.openSequence();
	    authoritativeEngineId = SnmpEngineId.createEngineId(bdec.fetchOctetString());
	    authoritativeEngineBoots = bdec.fetchInteger();
	    authoritativeEngineTime = bdec.fetchInteger();
	    userName = new String(bdec.fetchOctetString());
	    authParameters = bdec.fetchOctetString();
	    privParameters = bdec.fetchOctetString();
	    bdec.closeSequence();
	}catch(BerException e) {
	    throw new SnmpStatusException("Invalid security " + 
					  "parameters Ber encoding.");
	}
    }

    // Simplify debugging and tracing.
    public String toString() {
	return ( (authoritativeEngineId !=null ? authoritativeEngineId.toString() : "<unknown engine id>") + " : " 
		 + (userName != null ? userName : "<unknown user>") +":" + 
		 authoritativeEngineBoots + ":" + authoritativeEngineTime + ":"
		 + authParameters + ":" + privParameters);
    }

    /**
     * Encode in BER the security parameters.
     */
    public int encode(byte[] outputBytes) throws SnmpTooBigException {
	// Reminder: BerEncoder does backward encoding !
        //
	int len = 0;
	try {
	    BerEncoder bdec = new BerEncoder(outputBytes);
	    bdec.openSequence();
	    bdec.putOctetString((privParameters != null) ? 
				privParameters : new byte[0]);
	    bdec.putOctetString((authParameters != null) ? 
				authParameters : new byte[0]);
	    
	    bdec.putOctetString(userName != null ? userName.getBytes() :
				new byte[0]);
	    bdec.putInteger(authoritativeEngineTime);
	    bdec.putInteger(authoritativeEngineBoots);
	    if(authoritativeEngineId != null)
		bdec.putOctetString((authoritativeEngineId.getBytes() != null)
				    ? authoritativeEngineId.getBytes() : 
				    new byte[0]);
	    else {
		bdec.putOctetString(new byte[0]);	
	    }
	    bdec.closeSequence();
	    len = bdec.trim();
	}
	catch(ArrayIndexOutOfBoundsException x) {
            throw new SnmpTooBigException() ;
        }
	return len;
    }
}
