/*
 * @(#)file      SnmpTools.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.17
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

import com.sun.management.snmp.SnmpDefinitions;
/**
 * Utility class used to deal with various data representations.
 *
 * @since Java DMK 5.1
 */
public class SnmpTools implements SnmpDefinitions {

    /**
     * Translates a binary representation in an ASCII one. The returned string is an hexadecimal string starting with 0x.
     * @param data Binary to translate.
     * @return Translated binary.
     */
    static public String binary2ascii(byte[] data, int length)
    {
	if(data == null) return null;
	final int size = (length * 2) + 2;
	byte[] asciiData = new byte[size];
	asciiData[0] = (byte) '0';
	asciiData[1] = (byte) 'x';
	for (int i=0; i < length; i++) {
	    int j = i*2;
	    int v = (data[i] & 0xf0);
	    v = v >> 4;
	    if (v < 10)
		asciiData[j+2] = (byte) ('0' + v);
	    else
		asciiData[j+2] = (byte) ('A' + (v - 10));
	    v = ((data[i] & 0xf));
	    if (v < 10)
		asciiData[j+1+2] = (byte) ('0' + v);
	    else
		asciiData[j+1+2] = (byte) ('A' + (v - 10));
	}
	return new String(asciiData);
    }
    
    /**
     * Translates a binary representation in an ASCII one. The returned string is an hexadecimal string starting with 0x.
     * @param data Binary to translate.
     * @return Translated binary.
     */
    static public String binary2ascii(byte[] data)
    {
	return binary2ascii(data, data.length);
    }
    /**
     * Translates a string representation in a binary one. The passed string is an hexadecimal one starting with 0x.
     * @param str String to translate.
     * @return Translated string.
     */
    static public byte[] ascii2binary(String str) {
	if(str == null) return null;
	String val = str.substring(2);
	
	int size = val.length();
	byte []buf = new byte[size/2];
	byte []p = val.getBytes();
	
	for(int i = 0; i < (int) (size / 2); i++) 
	{
	    int j = i * 2;
	    byte v = 0;
	    if (p[j] >= '0' && p[j] <= '9') {
		v = (byte) ((p[j] - '0') << 4);
	    }
	    else if (p[j] >= 'a' && p[j] <= 'f') {
		v = (byte) ((p[j] - 'a' + 10) << 4);
	    }
	    else if (p[j] >= 'A' && p[j] <= 'F') {
		v = (byte) ((p[j] - 'A' + 10) << 4);
	    }
	    else
		throw new Error("BAD format :" + str);
	    
	    if (p[j+1] >= '0' && p[j+1] <= '9') {
		//System.out.println("ascii : " + p[j+1]);
		v += (p[j+1] - '0');
		//System.out.println("binary : " + v);
	    }
	    else if (p[j+1] >= 'a' && p[j+1] <= 'f') {
		//System.out.println("ascii : " + p[j+1]);
		v += (p[j+1] - 'a' + 10);
		//System.out.println("binary : " + v+1);
	    }
	    else if (p[j+1] >= 'A' && p[j+1] <= 'F') {
		//System.out.println("ascii : " + p[j+1]);
		v += (p[j+1] - 'A' + 10);
		//System.out.println("binary : " + v);
	    }
	    else
		throw new Error("BAD format :" + str);
	    
	    buf[i] = (byte) v;
	}
	return buf;
    }
}
