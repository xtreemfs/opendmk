/*
 * @(#)file      SnmpEngineId.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.34
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
package com.sun.management.snmp;

import java.net.InetAddress;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.NoSuchElementException;

import com.sun.management.internal.snmp.SnmpTools;

/**
 * This class is handling an <CODE>SnmpEngineId</CODE> data. It copes with binary as well as <CODE>String</CODE> representation of an engine Id. A string format engine is an hex string starting with 0x.
 *
 * @since Java DMK 5.1
 */
public class SnmpEngineId implements Serializable {
    private static final long serialVersionUID = -8737696000100566404L;
    byte[] engineId = null;
    String hexString = null;
    String humanString = null;
    /**
     * New <CODE>SnmpEngineId</CODE> with an hex string value. Can handle engine Id format &lt;host&gt:&lt;port&gt.
     * @param hexString Hexadecimal string.
     */
    SnmpEngineId(String hexString) {
	engineId = SnmpTools.ascii2binary(hexString);
	this.hexString = hexString.toLowerCase();
    }
    /**
     * New <CODE>SnmpEngineId</CODE> with a binary value. You can use <CODE> SnmpTools </CODE> to convert from hex string to binary format.
     * @param bin Binary value
     */
    SnmpEngineId(byte[] bin) {
	engineId = bin;
	hexString = SnmpTools.binary2ascii(bin).toLowerCase();
    }

    /**
     * If a string of the format &lt;address&gt;:&lt;port&gt;:&lt;IANA number&gt; has been provided at creation time, this string is returned.
     * @return The Id as a readable string or null if not provided.
     */
    public String getReadableId() {
	return humanString;
    }

    /**
     * Returns a string format engine Id.
     * @return String format value.
     */
    public String toString() {
	return hexString;
    }
    /**
     * Returns a binary engine Id.
     * @return Binary value.
     */
    public byte[] getBytes() { 
	return engineId;
    }
    
    /**
     * In order to store the string used to create the engineId.
     */
    void setStringValue(String val) {
	humanString = val;
    }
    
    static void validateId(String str) throws IllegalArgumentException {
	byte[] arr = SnmpTools.ascii2binary(str);
	validateId(arr);
    }
    
    static void validateId(byte[] arr) throws IllegalArgumentException {
	
	if(arr.length < 5) throw new IllegalArgumentException("Id size lower than 5 bytes.");
	if(arr.length > 32) throw new IllegalArgumentException("Id size greater than 32 bytes.");

	//octet strings with very first bit = 0 and length != 12 octets
	if( ((arr[0] & 0x80) == 0) && arr.length != 12) 
	    throw new IllegalArgumentException("Very first bit = 0 and length != 12 octets");

	byte[] zeroedArrays = new byte[arr.length];
	if(Arrays.equals(zeroedArrays, arr)) throw new IllegalArgumentException("Zeroed Id.");
	byte[] FFArrays = new byte[arr.length];
	Arrays.fill(FFArrays, (byte)0xFF);
	if(Arrays.equals(FFArrays, arr)) throw new IllegalArgumentException("0xFF Id.");

    }
    
    /**
     * Generates an engine Id based on the passed array.
     * @return The created engine Id or null if given array is null or its length == 0;
     * @exception IllegalArgumentException. Thrown when :
     * <ul>
     *  <li>octet string lower than 5 bytes.</li>
     *  <li>octet string greater than 32 bytes.</li>
     *  <li>octet string = all zeros.</li>
     *  <li>octet string = all 'ff'H.</li>
     *  <li>octet strings with very first bit = 0 and length != 12 octets</li>
     * </ul>
     */
    public static SnmpEngineId createEngineId(byte[] arr) throws IllegalArgumentException {
	if( (arr == null) || arr.length == 0) return null;
	validateId(arr);
	return new SnmpEngineId(arr);
    }

    /**
     * Generates an engine Id that is unique to the host the agent is running on. The engine Id unicity is system time based. The creation algorithm uses the SUN Microsystems IANA number (42).
     * @return The generated engine Id.
     */
    public static SnmpEngineId createEngineId() {
	byte[] address = null;
	byte[] engineid = new byte[13];
	int iana = 42;
	long mask = 0xFF;
	long time = System.currentTimeMillis();

	engineid[0] = (byte) ( (iana & 0xFF000000) >> 24 );
	engineid[0] |= 0x80;
	engineid[1] = (byte) ( (iana & 0x00FF0000) >> 16 );
	engineid[2] = (byte) ( (iana & 0x0000FF00) >> 8 );
	engineid[3] = (byte) (iana & 0x000000FF);
	engineid[4] = 0x05;

 	engineid[5] =  (byte) ( (time & (mask << 56)) >>> 56 );
 	engineid[6] =  (byte) ( (time & (mask << 48) ) >>> 48 );
 	engineid[7] =  (byte) ( (time & (mask << 40) ) >>> 40 );
 	engineid[8] =  (byte) ( (time & (mask << 32) ) >>> 32 );
 	engineid[9] =  (byte) ( (time & (mask << 24) ) >>> 24 );
 	engineid[10] = (byte) ( (time & (mask << 16) ) >>> 16 );
 	engineid[11] = (byte) ( (time & (mask << 8) ) >>> 8 );
 	engineid[12] = (byte) (time & mask);

	return new SnmpEngineId(engineid);
    }

    /**
     * Translates an engine Id in an SnmpOid format. This is useful when dealing with USM MIB indexes.
     * The oid format is : <engine Id length>.<engine Id binary octet1>....<engine Id binary octetn - 1>.<engine Id binary octetn>
     * e.g. "0x8000002a05819dcb6e00001f96" ==> 13.128.0.0.42.5.129.157.203.110.0.0.31.150
     * 
     * @return SnmpOid The oid.
     */
    public SnmpOid toOid() {
	long[] oid = new long[engineId.length + 1];
	oid[0] = engineId.length;
	for(int i = 1; i <= engineId.length; i++)
	    oid[i] = (long) (engineId[i-1] & 0xFF);
	return new SnmpOid(oid);
    }

   /**
    * <P>Generates a unique engine Id. Hexadecimal strings as well as a textual description are supported. The textual format is as follow:
    * <BR>  &lt;address&gt;:&lt;port&gt;:&lt;IANA number&gt;</P>
    * <P>The allowed formats :</P>
    * <ul> 
    * <li> &lt;address&gt;:&lt;port&gt;:&lt;IANA number&gt
    * <BR>   All these parameters are used to generate the Id. WARNING, 
    *    this method is not compliant with IPv6 address format. 
    *    Use { @link #createEngineId(java.lang.String,java.lang.String) } 
    *    instead.</li>
    * <li> &lt;address&gt;:&lt;port&gt;
    * <BR>   The IANA number will be the SUN Microsystems one (42). </li>
    * <li> address
    * <BR>   The port 161 will be used to generate the Id. IANA number will 
    *        be the SUN Microsystems one (42). </li>
    * <li> :port
    * <BR>   The host to use is localhost. IANA number will be the SUN 
    *        Microsystems one (42). </li>
    * <li> ::&lt;IANA number&gt &nbsp;&nbsp;&nbsp;
    * <BR>   The port 161 and localhost will be used to generate the Id. </li>
    * <li> :&lt;port&gt;:&lt;IANA number&gt;
    * <BR>   The host to use is localhost. </li>
    * <li> &lt;address&gt;::&lt;IANA number&gt
    * <BR>   The port 161 will be used to generate the Id. </li>
    * <li> :: &nbsp;&nbsp;&nbsp;
    * <BR>   The port 161, localhost and the SUN Microsystems IANA number 
    *        will be used to generate the Id. </li>
    * </ul>
    * @exception UnknownHostException. Thrown if the host name contained 
    *            in the textual format is unknown.
    * @exception IllegalArgumentException. Thrown when :
    * <ul>
    *  <li>octet string lower than 5 bytes.</li>
    *  <li>octet string greater than 32 bytes.</li>
    *  <li>octet string = all zeros.</li>
    *  <li>octet string = all 'ff'H.</li>
    *  <li>octet strings with very first bit = 0 and length != 12 octets</li>
    *  <li>An IPv6 address format is used in conjunction with the ":" 
    *      separator</li>
    * </ul>
    * @param str The string to parse.
    * @return The generated engine Id or null if the passed string is null.
    * 
    */
    public static SnmpEngineId createEngineId(String str) 
	throws IllegalArgumentException, UnknownHostException {
	return createEngineId(str, null);
    }
    
    /**
     * Idem {@link #createEngineId(java.lang.String)} 
     * with the ability to provide your own separator. This allows IPv6 
     * address format handling (e.g. providing @ as separator).
     * @param str The string to parse.
     * @param separator the separator to use. If null is provided, the default 
     * separator ":" is used.
     * @return The generated engine Id or null if the passed string is null.
     * @exception UnknownHostException. Thrown if the host name contained 
     *      in the textual format is unknown.
     * @exception IllegalArgumentException. Thrown when :
     * <ul>
     *  <li>octet string lower than 5 bytes.</li>
     *  <li>octet string greater than 32 bytes.</li>
     *  <li>octet string = all zeros.</li>
     *  <li>octet string = all 'ff'H.</li>
     *  <li>octet strings with very first bit = 0 and length != 12 octets</li>
     *  <li>An IPv6 address format is used in conjunction with the ":" 
     *      separator</li>
     * </ul>
     */
    public static SnmpEngineId createEngineId(String str, String separator) 
	throws IllegalArgumentException, UnknownHostException {
	if(str == null) return null;
	
	if(str.startsWith("0x") || str.startsWith("0X")) {
	    validateId(str);
	    return new SnmpEngineId(str);
	}
	separator = separator == null ? ":" : separator;
	StringTokenizer token = new StringTokenizer(str, 
						    separator, 
						    true);
	
	String address = null; 
	String port = null;
	String iana = null;
	int objPort = 161;
	int objIana = 42;
	InetAddress objAddress = null;
	SnmpEngineId eng = null;
	try {
	    //Deal with address
	    try {
		address = token.nextToken();
	    }catch(NoSuchElementException e) {
		throw new IllegalArgumentException("Passed string is invalid : ["+str+"]");
	    }
	    if(!address.equals(separator)) {
		objAddress = InetAddress.getByName(address);
		try {
		    token.nextToken();
		}catch(NoSuchElementException e) {
		    //No need to go further, no port.
		    eng = SnmpEngineId.createEngineId(objAddress,
						      objPort,
						      objIana);
		    eng.setStringValue(str);
		    return eng;
		}	
	    }
	    else 
		objAddress = InetAddress.getLocalHost();
	    
	    //Deal with port
	    try {
		port = token.nextToken();
	    }catch(NoSuchElementException e) {
		//No need to go further, no port.
		eng = SnmpEngineId.createEngineId(objAddress,
						  objPort,
						  objIana);
		eng.setStringValue(str);
		return eng;
	    }
	    
	    if(!port.equals(separator)) {
		objPort = Integer.parseInt(port);
		try {
		    token.nextToken();
		}catch(NoSuchElementException e) {
		    //No need to go further, no iana.
		    eng = SnmpEngineId.createEngineId(objAddress,
						      objPort,
						      objIana);
		    eng.setStringValue(str);
		    return eng;
		}	
	    }
	    
	    //Deal with iana
	    try {
		iana = token.nextToken();
	    }catch(NoSuchElementException e) {
		//No need to go further, no port.
		eng = SnmpEngineId.createEngineId(objAddress,
						  objPort,
						  objIana);
		eng.setStringValue(str);
		return eng;
	    }
	    
	    if(!iana.equals(separator))
		objIana = Integer.parseInt(iana);
	    
	    eng = SnmpEngineId.createEngineId(objAddress,
					      objPort,
					      objIana);
	    eng.setStringValue(str);
	    
	    return eng;
	    
	}catch(UnknownHostException e) {
	    throw e;
	}
	catch(Exception e) {
	    throw new IllegalArgumentException("Passed string is invalid : ["+str+"]. Check that the used separator ["+ separator + "] is compatible with IPv6 address format.");
	}
		  
    }

    /**
     * Generates a unique engine Id. The engine Id unicity is based on the host IP address and port. The IP address used is the localhost one. The creation algorithm uses the SUN Microsystems IANA number (42). 
     * @param port The TCP/IP port the {@link com.sun.management.comm.SnmpV3AdaptorServer} is listening to.
     * @return The generated engine Id.
     * @exception UnknownHostException. Thrown if the local host name used to calculate the id is unknown.
     */
    public static SnmpEngineId createEngineId(int port) throws UnknownHostException {
	int suniana = 42;
	InetAddress address = null;
	address = InetAddress.getLocalHost();
	return createEngineId(address, port, suniana);
    }
    /**
     * Generates a unique engine Id. The engine Id unicity is based on the host IP address and port. The IP address used is the passed one. The creation algorithm uses the SUN Microsystems IANA number (42). 
     * @param address The IP address the {@link com.sun.management.comm.SnmpV3AdaptorServer} is listening to.
     * @param port The TCP/IP port the {@link com.sun.management.comm.SnmpV3AdaptorServer} is listening to.
     * @return The generated engine Id.
     * @exception UnknownHostException. Thrown if the provided address is null.
     */
    public static SnmpEngineId createEngineId(InetAddress address, int port) 
	throws IllegalArgumentException {
	int suniana = 42;
	if(address == null) throw new IllegalArgumentException("InetAddress is null.");
	return createEngineId(address, port, suniana);
    }

    /**
     * Generates a unique engine Id. The engine Id unicity is based on the host IP address and port. The IP address is the localhost one. The creation algorithm uses the passed IANA number. 
     * @param port The TCP/IP port the {@link com.sun.management.comm.SnmpV3AdaptorServer} is listening to.
     * @param iana Your enterprise IANA number.
     * @exception UnknownHostException. Thrown if the local host name used to calculate the id is unknown.
     * @return The generated engine Id.
     */
    public static SnmpEngineId createEngineId(int port, int iana) throws UnknownHostException {
	InetAddress address = null;
	address = InetAddress.getLocalHost();
	return createEngineId(address, port, iana);
    }

    /**
     * Generates a unique engine Id. The engine Id unicity is based on the host IP address and port. The IP address is the passed one, it handles IPv4 and IPv6 hosts. The creation algorithm uses the passed IANA number. 
     * @param addr The IP address the {@link com.sun.management.comm.SnmpV3AdaptorServer} is listening to.
     * @param port The TCP/IP port the {@link com.sun.management.comm.SnmpV3AdaptorServer} is listening to.
     * @param iana Your enterprise IANA number.
     * @return The generated engine Id.
     * @exception UnknownHostException. Thrown if the provided <CODE>InetAddress </CODE> is null.
     */
    public static SnmpEngineId createEngineId(InetAddress addr, 
					      int port, 
					      int iana) {
	if(addr == null) throw new IllegalArgumentException("InetAddress is null.");
	byte[] address = addr.getAddress();
	byte[] engineid = new byte[9 + address.length];
	engineid[0] = (byte) ( (iana & 0xFF000000) >> 24 );
	engineid[0] |= 0x80;
	engineid[1] = (byte) ( (iana & 0x00FF0000) >> 16 );
	engineid[2] = (byte) ( (iana & 0x0000FF00) >> 8 );
	
engineid[3] = (byte) (iana & 0x000000FF);
	engineid[4] = 0x05;
	
	if(address.length == 4)
	    engineid[4] = 0x01;
	
	if(address.length == 16)
	    engineid[4] = 0x02;
	
	for(int i = 0; i < address.length; i++) {
	    engineid[i + 5] = address[i];
	}
	
	engineid[5 + address.length] = (byte)  ( (port & 0xFF000000) >> 24 );
	engineid[6 + address.length] = (byte) ( (port & 0x00FF0000) >> 16 );
	engineid[7 + address.length] = (byte) ( (port & 0x0000FF00) >> 8 );
	engineid[8 + address.length] = (byte) (  port & 0x000000FF );

	return new SnmpEngineId(engineid);
    }

     /**
     * Generates an engine Id based on an InetAddress. Handles IPv4 and IPv6 addresses. The creation algorithm uses the passed IANA number. 
     * @param iana Your enterprise IANA number.
     * @param addr The IP address the {@link com.sun.management.comm.SnmpV3AdaptorServer} is listening to.
     * @return The generated engine Id.
     * @exception UnknownHostException. Thrown if the provided <CODE>InetAddress </CODE> is null.
     */
    public static SnmpEngineId createEngineId(int iana, InetAddress addr)
    {
	if(addr == null) throw new IllegalArgumentException("InetAddress is null.");
	byte[] address = addr.getAddress();
	byte[] engineid = new byte[5 + address.length];
	engineid[0] = (byte) ( (iana & 0xFF000000) >> 24 );
	engineid[0] |= 0x80;
	engineid[1] = (byte) ( (iana & 0x00FF0000) >> 16 );
	engineid[2] = (byte) ( (iana & 0x0000FF00) >> 8 );
	
	engineid[3] = (byte) (iana & 0x000000FF);
	if(address.length == 4)
	    engineid[4] = 0x01;
	
	if(address.length == 16)
	    engineid[4] = 0x02;
	
	for(int i = 0; i < address.length; i++) {
	    engineid[i + 5] = address[i];
	}

	return new SnmpEngineId(engineid);
    }

    /**
     * Generates an engine Id based on an InetAddress. Handles IPv4 and IPv6 
     * addresses. The creation algorithm uses the sun IANA number (42). 
     * @param addr The IP address the {@link 
     *     com.sun.management.comm.SnmpV3AdaptorServer} is listening to.
     * @return The generated engine Id.
     * @exception UnknownHostException. Thrown if the provided 
     *     <CODE>InetAddress </CODE> is null.
     */
    public static SnmpEngineId createEngineId(InetAddress addr) {
	return createEngineId(42, addr);
    }


    /**
     * Tests <CODE>SnmpEngineId</CODE> instance equality. Two <CODE>SnmpEngineId</CODE> are equal if they have the same value.
     * @return <CODE>true</CODE> if the two <CODE>SnmpEngineId</CODE> are equals, <CODE>false</CODE> otherwise.
     */
    public boolean equals(Object a) {
	if(!(a instanceof SnmpEngineId) ) return false;
	return hexString.equals(((SnmpEngineId) a).toString());
    }

    public int hashCode() {
	return hexString.hashCode();
    }
}
