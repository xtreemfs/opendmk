/*
 * @(#)file      NetMaskImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.11
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

package com.sun.management.snmp.IPAcl;



import java.util.Vector;
import java.util.Enumeration;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.net.InetAddress;

import java.security.Principal;
import com.sun.jdmk.security.acl.Group;

import com.sun.jdmk.internal.ClassLogger;

/**
 * This class is used to represent a subnet mask (a group of hosts matching
 * the same IP mask).
 *
 * @see com.sun.jdmk.security.acl.Group
 * @see java.security.Principal
 *
 * @since Java DMK 5.1
 */

class NetMaskImpl extends PrincipalImpl implements Group, Serializable {
    private static final long serialVersionUID = 7409889974102413924L;
    protected byte[] subnet = null;
    protected int prefix = -1;
    /**
     * Constructs an empty group.
     * @exception UnknownHostException Not implemented
     */
    public NetMaskImpl () throws UnknownHostException {
    }

    private byte[] extractSubNet(byte[] b) {
	int addrLength = b.length;
	byte[] subnet = null;
	if(logger.finestOn()) {
	    logger.finest("extractSubNet","BINARY ARRAY :");
	    StringBuffer buff = new StringBuffer();
	    for(int i =0; i < addrLength; i++) {
		buff.append((int)(b[i] &0xFF) +":");
	    }
	    logger.finest("extractSubNet", buff.toString());
	}

	// 8 is a byte size. Common to any InetAddress (V4 or V6).
	int fullyCoveredByte = prefix / 8;
	if(fullyCoveredByte == addrLength) {
	    if(logger.finestOn()) {
		logger.finest("extractSubNet"," The mask is the complete address,"+
		      " strange..." + addrLength);
	    }
	    subnet = b;
	    return subnet;
	}
	if(fullyCoveredByte > addrLength) {
	    if(logger.finestOn()) {
		logger.finest("extractSubNet"," The number of covered byte is "+
		      "longer than the address. BUG");
	    }
	    throw new IllegalArgumentException("The number of covered byte "+
					       "is longer than the address.");
	}
	int partialyCoveredIndex = fullyCoveredByte;
	if(logger.finestOn()) {
	    logger.finest("extractSubNet"," Partialy covered index : " +
		  partialyCoveredIndex);
	}
	byte toDeal = b[partialyCoveredIndex];
	if(logger.finestOn()) {
	    logger.finest("extractSubNet"," Partialy covered byte : " + toDeal);
	}

	// 8 is a byte size. Common to any InetAddress (V4 or V6).
	int nbbits = prefix % 8;
	int subnetSize = 0;

	if(nbbits == 0)
	subnetSize = partialyCoveredIndex;
	else
	subnetSize = partialyCoveredIndex + 1;

	if(logger.finestOn()) {
	    logger.finest("extractSubNet"," Remains : " + nbbits);
	}

	byte mask = 0;
	for(int i = 0; i < nbbits; i++) {
	    mask |= (1 << (7 - i));
	}
	if(logger.finestOn()) {
	    logger.finest("extractSubNet","Mask value" + (int) (mask & 0xFF));
	}

	byte maskedValue = (byte) ((int)toDeal & (int)mask);

	if(logger.finestOn()) {
	    logger.finest("extractSubNet","Masked byte :"  + (int)(maskedValue &0xFF));
	}
	subnet = new byte[subnetSize];
	if(logger.finestOn()) {
	    logger.finest("extractSubNet","Resulting subnet : ");
	}
	for(int i = 0; i < partialyCoveredIndex; i++) {
	    subnet[i] = b[i];

	    if(logger.finestOn()) {
		logger.finest("extractSubNet",(int) (subnet[i] & 0xFF) +":");
	    }
	}

	if(nbbits != 0) {
	    subnet[partialyCoveredIndex] = maskedValue;
	    if(logger.finestOn()) {
		logger.finest("extractSubNet"," Last subnet byte : " +
		      (int) (subnet[partialyCoveredIndex] &0xFF));
	    }
	}
	return subnet;
    }

    /**
     * Constructs a group using the specified subnet mask. THIS ALGORITHM IS V4
     * and V6 compatible.
     *
     * @exception UnknownHostException if the subnet mask cann't be built.
     */
    public NetMaskImpl (String hostname, int prefix)
	throws UnknownHostException {
	super(hostname);
	this.prefix = prefix;
	subnet = extractSubNet(getAddress().getAddress());
    }

    /**
     * Adds the specified member to the group.
     *
     * @param p the principal to add to this group.
     * @return true if the member was successfully added, false if the
     *         principal was already a member.
     */
    public boolean addMember(Principal p) {
	// we don't need to add members because the ip address is a
	// subnet mask
	return true;
    }

    public int hashCode() {
	return super.hashCode();
    }

    /**
     * Compares this group to the specified object. Returns true if the object
     * passed in matches the group represented.
     *
     * @param p the object to compare with.
     * @return true if the object passed in matches the subnet mask, false
     *         otherwise.
     */
    public boolean equals (Object p) {
	if (p instanceof PrincipalImpl || p instanceof NetMaskImpl){
	    PrincipalImpl received = (PrincipalImpl) p;
	    InetAddress addr = received.getAddress();
	    if(logger.finestOn()) {
		logger.finest("equals","Received Address : " + addr);
	    }
	    byte[] recAddr = addr.getAddress();
	    for(int i = 0; i < subnet.length; i++) {
		if(logger.finestOn()) {
		    logger.finest("equals","(recAddr[i]) :" + (recAddr[i] & 0xFF));
		    logger.finest("equals","(recAddr[i] & subnet[i]) :" +
			  ( (int) (recAddr[i] & (int)subnet[i]) &0xFF) +
			  "subnet[i] :" + (int) (subnet[i] &0xFF));
		}
		if((recAddr[i] & subnet[i]) != subnet[i]) {
		    if(logger.finestOn()) {
			logger.finest("equals","FALSE");
		    }
		    return false;
		}
	    }
	    if(logger.finestOn()) {
		logger.finest("equals","TRUE");
	    }
	    return true;
	} else
	    return false;
    }

    /**
     * Returns true if the passed principal is a member of the group.
     *
     * @param p the principal whose membership is to be checked.
     * @return true if the principal is a member of this group, false
     *         otherwise.
     */
    public boolean isMember(Principal p) {
	if ((p.hashCode() & super.hashCode()) == p.hashCode()) return true;
	else return false;
    }

    /**
     * Returns an enumeration which contains the subnet mask.
     *
     * @return an enumeration which contains the subnet mask.
     */
    public Enumeration members(){
	Vector v = new Vector(1);
	v.addElement(this);
	return v.elements();
    }

    /**
     * Removes the specified member from the group. (Not implemented)
     *
     * @param p the principal to remove from this group.
     * @return allways return true.
     */
    public boolean removeMember(Principal p) {
	return true;
    }

    /**
     * Prints a string representation of this group.
     *
     * @return  a string representation of this group.
     */
    public String toString() {
	return ("NetMaskImpl :"+ super.getAddress().toString() + "/" + prefix);
    }

    // Logging
    //--------
    private static final ClassLogger logger =
	new ClassLogger(ClassLogger.LOGGER_SNMP,"NetMaskImpl");

    String dbgTag = "NetMaskImpl";
}
