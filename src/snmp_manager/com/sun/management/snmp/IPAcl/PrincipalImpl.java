/*
 * @(#)file      PrincipalImpl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.23
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



import java.net.InetAddress;
import java.net.UnknownHostException;
import java.io.Serializable;
import java.lang.reflect.Method;


/**
 * Principal represents a host.
 *
 * @see java.security.Principal
 *
 * @since Java DMK 5.1
 */

class PrincipalImpl implements java.security.Principal, Serializable {
    private static final long serialVersionUID = -4629687515796445309L;
    private InetAddress[] add = null;
  
    /**
     * Constructs a principal with the local host.
     */
    public PrincipalImpl () throws UnknownHostException {
        add = new InetAddress[1];
        add[0] = java.net.InetAddress.getLocalHost();
    }
  
    /**
     * Construct a principal using the specified host.
     * <P>
     * The host can be either:
     * <UL>
     * <LI> a host name
     * <LI> an IP address
     * </UL>
     *
     * @param hostName the host used to make the principal.
     */
    public PrincipalImpl(String hostName) throws UnknownHostException {
        if (isLoopBackAddress(hostName)){
            add = new InetAddress[1];
            add[0] = java.net.InetAddress.getLocalHost();
        }
        else
            add = java.net.InetAddress.getAllByName(hostName);
    }
    
    /**
     * Constructs a principal using an Internet Protocol (IP) address.
     *
     * @param address the Internet Protocol (IP) address.
     */
    public PrincipalImpl(InetAddress address) {
        add = new InetAddress[1];
        try {
            if(isLoopBackAddress(address)) {
                add[0] = java.net.InetAddress.getLocalHost();
            }
            else
                add[0] = address;
        } catch (UnknownHostException e) {
            add[0] = address;
        }
    }

    private static boolean isLoopBackAddress(String hostName) 
        throws UnknownHostException {
        boolean loopback = isLoopBackAddress(InetAddress.getByName(hostName));
        if(!loopback)
            if(hostName.equals("localhost"))
                loopback = true;
        return loopback;
    }
    
    private static boolean isLoopBackAddress(InetAddress address) {
        Class addressClass = InetAddress.class;
        Method loopback = null;
        try {
            loopback = addressClass.getDeclaredMethod("isLoopbackAddress", 
                                                      (Class[])null);
        }catch(NoSuchMethodException e) {
            // OK: JDK is < 1.4
        }
        
        if(loopback != null)
            try {
                return 
                    ((Boolean) loopback.invoke(address, 
                        (Object[])null)).booleanValue();
            }catch(Exception e) {
                // Should never happen, asserting the behavior.
                throw new Error(e.toString());
            }
        else
            return (address.getHostAddress().equals("127.0.0.1"));
    }

    /**
     * Returns the name of this principal.
     *
     * @return the name of this principal.
     */
    public String getName() {
        return add[0].toString();       
    }
    
    /**
     * Compares this principal to the specified object. Returns true if the
     * object passed in matches the principal
     * represented by the implementation of this interface. 
     *
     * @param  a the principal to compare with.
     * @return true if the principal passed in is the same as that 
     *  encapsulated by this principal, false otherwise. 
     */
    public boolean equals(Object a) {
        if (a instanceof PrincipalImpl){
            for(int i = 0; i < add.length; i++) {
                if(add[i].equals ((InetAddress)((PrincipalImpl) a).
                                  getAddress()))
                    return true;
            }
            return false;
        } else {
            return false;
        }
    }
    
    /**
     * Returns a hash code for this principal. 
     *
     * @return a hash code for this principal. 
     */
    public int hashCode(){
        return add[0].hashCode();
    }
    
    /**
     * Returns a string representation of this principal. In case of multiple
     * addresses, the first one is returned.
     *
     * @return a string representation of this principal.
     */
    public String toString() {
        return ("PrincipalImpl :"+add[0].toString());
    }
    
    /**
     * Returns the Internet Protocol (IP) address for this principal. 
     * In case of multiple addresses, the first one is returned.
     *
     * @return the Internet Protocol (IP) address for this principal.
     */
    public InetAddress getAddress(){
        return add[0];
    }
    
    /**
     * Returns the Internet Protocol (IP) addresses for this principal. 
     *
     * @return the array of Internet Protocol (IP) addresses for this 
     *  principal.
     */
    public InetAddress[] getAddresses(){
        return add;
    }
}

