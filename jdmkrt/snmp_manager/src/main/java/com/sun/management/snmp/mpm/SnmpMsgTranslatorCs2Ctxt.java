/*
 * @(#)file      SnmpMsgTranslatorCs2Ctxt.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.10
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

package com.sun.management.snmp.mpm;

import com.sun.management.snmp.SnmpMsg;
import com.sun.management.snmp.SnmpMessage;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpEngine;
import com.sun.management.snmp.SnmpSecurityParameters;
/**
 * This translator is dedicated to translate V1 V2 parameters in the
 * context of SNMP V3 messages. It infers some parameters such as
 * context name and context engine Id.
 *
 *
 * @since Java DMK 5.1
 */
public class SnmpMsgTranslatorCs2Ctxt extends SnmpMsgTranslatorV1V2 {
    SnmpEngine engine = null;
    
    public SnmpMsgTranslatorCs2Ctxt(SnmpEngine engine) {
	super(engine);
    }

    /**
     * A context name can be added at the end of the community string via @ separator. If no @ is located, null is returned.
     */
    public byte[] getContextName(SnmpMsg msg) {
	if( ((SnmpMessage)msg).community == null )
	return null;
	
	String context = new String(((SnmpMessage)msg).community);
	int index = context.indexOf("@");
	if(index == -1)
	return null;
	else
	return context.substring(index + 1).getBytes();
    }

    /**
     * The access context (used in IP ACL) is the community string. The community string or null is returned.
     */
    public byte[] getAccessContext(SnmpMsg msg) {
	if( ((SnmpMessage)msg).community == null )
	return null;
	String context = new String(((SnmpMessage)msg).community);
	int index = context.indexOf("@");
	if(index == -1)
	return ((SnmpMessage)msg).community;
	else
	return context.substring(0, index).getBytes();	
    }
}
