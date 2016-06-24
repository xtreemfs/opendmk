/*
 * @(#)file      JdmkAcl.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.41
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



// java import
//
import java.io.Serializable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;
import java.util.HashSet;
import java.security.acl.AclEntry; 
import java.security.acl.NotOwnerException; 

// jdmk import
//
import com.sun.jdmk.defaults.DefaultPaths;
import com.sun.jdmk.defaults.JdmkProperties;
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.InetAddressAcl;
import com.sun.jdmk.defaults.Utils;

/**
 * Defines an implementation of the {@link com.sun.management.snmp.InetAddressAcl InetAddressAcl} interface.
 * <p>
 * In this implementation the ACL information is stored on a flat file and its default
 * location is specified in the following order:
 * <p>
 * <OL>
 * <LI>The value of the <CODE>jdmk.acl.file</CODE> property.
 * <LI>The return value of <CODE>getEtcDir("conf" + File.separator + "jdmk.acl")</CODE>
 * in class {@link com.sun.jdmk.defaults.DefaultPaths DefaultPaths}.
 * </OL>
 *
 *
 * @since Java DMK 5.1
 */

public class JdmkAcl implements InetAddressAcl, Serializable {
    private static final long serialVersionUID = -1731776149894734768L;
  
    static final PermissionImpl READ  = new PermissionImpl("READ"); 
    static final PermissionImpl WRITE = new PermissionImpl("WRITE");
  
    /**
     * Constructs the Java Dynamic Management(TM) Access Control List
     * based on IP addresses.  The ACL will take the given owner name.
     * The ACL will take the given acl file.
     * The current IP address will be the owner of the ACL.
     *
     * @param name The name of the ACL.
     * @param fileName The name of the ACL file.
     *
     * @exception UnknownHostException If the local host is unknown.
     * @exception IllegalArgumentException If the ACL file doesn't exist.
     */
    public JdmkAcl(String name, String fileName)
	throws UnknownHostException, IllegalArgumentException {   
	trapDestList= new Hashtable();
        informDestList= new Hashtable();
        
        // PrincipalImpl() take the current host as entry
        owner = new PrincipalImpl();
        try {
            acl = new AclImpl(owner,name);
            AclEntry ownEntry = new AclEntryImpl(owner);
            ownEntry.addPermission(READ);
            ownEntry.addPermission(WRITE);
            acl.addEntry(owner,ownEntry);
        } catch (NotOwnerException ex) {
            if (logger.finestOn()) {
                logger.finest("constructor", "Should never get NotOwnerException as "+
		      "the owner is built in this constructor");
            }
        }
	
	if(fileName == null)
	    setDefautFileName();
	else
	    authorizedListFile = fileName;
	
	readAuthorisedListFile();
    }

    /**
     * Constructs the Java Dynamic Management(TM) Access Control List
     * based on IP addresses. The ACL will take the given owner name.
     * The current IP address will be the owner of the ACL.
     *
     * @param name The name of the ACL.
     *
     * @exception UnknownHostException If the local host is unknown. 
     * Thrown only if this class is instantiated internally by 
     * <CODE> SnmpAdaptorServer </CODE> or <CODE> SnmpV3AdaptorServer </CODE>.
     * 
     * @exception IllegalArgumentException If the ACL file doesn't exist.
     */
    public JdmkAcl(String name)
	throws UnknownHostException, IllegalArgumentException {   
        this(name, null);
    }
  
    /**
     * Returns an enumeration of the entries in this ACL. Each element in the
     * enumeration is of type <CODE>java.security.acl.AclEntry</CODE>.
     *
     * @return An enumeration of the entries in this ACL.
     */
    public synchronized Enumeration entries() {
        return acl.entries();
    }
  
    /**
     * Returns an enumeration of community strings. Community strings are returned as String.
     * @return The enumeration of community strings.
     */
    public synchronized Enumeration communities() {
	HashSet set = new HashSet();
	Vector res = new Vector();
	for (Enumeration e = acl.entries() ; e.hasMoreElements() ;) {
	    AclEntryImpl entry = (AclEntryImpl) e.nextElement();
	    for (Enumeration cs = entry.communities(); 
		 cs.hasMoreElements() ;) {
		set.add((String) cs.nextElement());
	    }
	}
	Object[] objs = set.toArray();
	for(int i = 0; i < objs.length; i++)
	    res.addElement(objs[i]);

	return res.elements();
    }

    /**
     * Returns the name of the ACL.
     *
     * @return The name of the ACL.
     */
    public synchronized String getName() {
        return acl.getName();
    }
  
    /**
     * Returns the read permission instance used.
     *
     * @return The read permission instance.
     */
    static public PermissionImpl getREAD() {
        return READ;
    }
  
    /**
     * Returns the write permission instance used.
     *
     * @return  The write permission instance.
     */
    static public PermissionImpl getWRITE() {
        return WRITE;
    }
  
    /**
     * Sets the full path of the file containing the ACL information.
     * Setting a file makes the previous loaded ACL configuration to be cleared.
     * Access control is based on this new file. If <CODE>IllegalArgumentException</CODE> is thrown, the previous existing ACL configuration is not cleared.
     *
     * @param filename The full path of the file containing the ACL information.
     * @exception IllegalArgumentException If the passed ACL file is null or doesn't exist.
     * @exception NotOwnerException This exception is never thrown.
     * @exception UnknownHostException If IP addresses for hosts contained in the ACL file couldn't be found.
     */
    public synchronized void setAuthorizedListFile(String filename) 
	throws IllegalArgumentException, 
	       NotOwnerException, UnknownHostException {
	if(filename == null)
	    throw new IllegalArgumentException("The specified file is null");
	
	File file = new File(filename);
	if (!file.isFile() ) {
	    if (logger.finestOn()) {
		logger.finest("setAuthorizedListFile", "Ip ACL file not found. Wrong passed file : " + filename);
	    }
	    throw new IllegalArgumentException("The specified file ["+ file +"] doesn't exist or is not a file, no configuration loaded");
	}
        if (logger.finerOn()) {
            logger.finer("setAuthorizedListFile", "Default file set to " + filename);
        }
        authorizedListFile = filename;

	rereadTheFile();
    }
  
    /**
     * Resets this ACL to the values contained in the configuration file.
     *
     * @exception NotOwnerException If the principal attempting the reset is not an owner of this ACL.
     * @exception UnknownHostException If IP addresses for hosts contained in the ACL file couldn't be found.
     */
    public synchronized void rereadTheFile() 
	throws NotOwnerException, UnknownHostException {
        alwaysAuthorized = false;
        acl.removeAll(owner);
        trapDestList.clear();
        informDestList.clear();
        AclEntry ownEntry = new AclEntryImpl(owner);
        ownEntry.addPermission(READ);
        ownEntry.addPermission(WRITE);  
        acl.addEntry(owner,ownEntry);
        readAuthorisedListFile();
    }
  
    /**
     * Returns the full path of the file used to get ACL information.
     *
     * @return The full path of the file used to get ACL information.
     */
    public synchronized String getAuthorizedListFile() {
        return authorizedListFile;
    }
  
    /**
     * Checks whether or not the specified host has <CODE>READ</CODE> access.
     *
     * @param address The host address to check.
     *
     * @return <CODE>true</CODE> if the host has read permission, <CODE>false</CODE> otherwise.
     */
    public synchronized boolean checkReadPermission(InetAddress address) {
        if (alwaysAuthorized) return ( true );
        PrincipalImpl p = new PrincipalImpl(address);
        return acl.checkPermission(p, READ);
    }
  
    /**
     * Checks whether or not the specified host and community have <CODE>READ</CODE> access.
     *
     * @param address The host address to check.
     * @param community The community associated with the host.
     *
     * @return <CODE>true</CODE> if the pair (host, community) has read permission, <CODE>false</CODE> otherwise.
     */
    public synchronized boolean checkReadPermission(InetAddress address, 
						    String community) {
        if (alwaysAuthorized) return ( true );
        PrincipalImpl p = new PrincipalImpl(address);
        return acl.checkPermission(p, community, READ);
    }
  
    /**
     * Checks whether or not a community string is defined.
     *
     * @param community The community to check.
     *
     * @return <CODE>true</CODE> if the community is known, <CODE>false</CODE> otherwise.
     */
    public synchronized boolean checkCommunity(String community) {
        return acl.checkCommunity(community);
    }
  
    /**
     * Checks whether or not the specified host has <CODE>WRITE</CODE> access.
     *
     * @param address The host address to check.
     *
     * @return <CODE>true</CODE> if the host has write permission, <CODE>false</CODE> otherwise.
     */
    public synchronized boolean checkWritePermission(InetAddress address) {
        if (alwaysAuthorized) return ( true );
        PrincipalImpl p = new PrincipalImpl(address);
        return acl.checkPermission(p, WRITE);
    } 

    /**
     * Checks whether or not the specified host and community have <CODE>WRITE</CODE> access.
     *
     * @param address The host address to check.
     * @param community The community associated with the host.
     *
     * @return <CODE>true</CODE> if the pair (host, community) has write permission, <CODE>false</CODE> otherwise.
     */
    public synchronized boolean checkWritePermission(InetAddress address, 
						     String community) {
        if (alwaysAuthorized) return ( true );
        PrincipalImpl p = new PrincipalImpl(address);
        return acl.checkPermission(p, community, WRITE);
    } 
  
    /**
     * Returns an enumeration of trap destinations.
     *
     * @return An enumeration of the trap destinations (enumeration of <CODE>InetAddress<CODE>).
     */
    public synchronized Enumeration getTrapDestinations() {
        return trapDestList.keys();
    }
  
    /**
     * Returns an enumeration of trap communities for a given host.
     *
     * @param i The address of the host.
     *
     * @return An enumeration of trap communities for a given host (enumeration of <CODE>String<CODE>).
     */
    public synchronized Enumeration getTrapCommunities(InetAddress i) {
        Vector list = null;
        if ((list = (Vector)trapDestList.get(i)) != null ) {
            if (logger.finerOn()) {
                logger.finer("getTrapCommunities", "["+i.toString()+"] is in list");
            }
            return list.elements();
        } else {
            list = new Vector();
            if (logger.finerOn()) {
                logger.finer("getTrapCommunities", "["+i.toString()+"] is not in list");
            }
            return list.elements();
        } 
    }
  
    /**
     * Returns an enumeration of inform destinations.
     *
     * @return An enumeration of the inform destinations (enumeration of <CODE>InetAddress<CODE>).
     */
    public synchronized Enumeration getInformDestinations() {
        return informDestList.keys();
    }
  
    /**
     * Returns an enumeration of inform communities for a given host.
     *
     * @param i The address of the host.
     *
     * @return An enumeration of inform communities for a given host (enumeration of <CODE>String<CODE>).
     */
    public synchronized Enumeration getInformCommunities(InetAddress i) {
        Vector list = null;
        if ((list = (Vector)informDestList.get(i)) != null ) {
            if (logger.finerOn()) {
                logger.finer("getInformCommunities", "["+i.toString()+"] is in list");
            }
            return list.elements();
        } else {
            list = new Vector();
            if (logger.finerOn()) {
                logger.finer("getInformCommunities", "["+i.toString()+"] is not in list");
            }
            return list.elements();
        } 
    }
  
    /**
     * Fix for 6305089 and 6238234. Non readable file make ACL to throw 
     * an exception.
     */
    private static void checkCanRead(String f) throws IllegalArgumentException {
        File file = new File(f);
        if(file.isFile() && !file.canRead())
            throw new IllegalArgumentException("IP ACL file is not " +
                    "readable.");
    }
    
    /**
     * Converts the input configuration file into ACL.
     */
    private void readAuthorisedListFile() {

        alwaysAuthorized = false;

        if (authorizedListFile == null) {
            if (logger.finerOn()) {
                logger.finer("readAuthorisedListFile", "alwaysAuthorized set to true");
            }
            alwaysAuthorized = true ;
        } else {
            // Read the file content
            Parser parser = null;  
            try {
                checkCanRead(getAuthorizedListFile());
                parser= new Parser(new FileInputStream(getAuthorizedListFile()));
            } catch (FileNotFoundException e) {
                if (logger.finestOn()) {
                    logger.finest("readAuthorisedListFile", "The specified file was not found, authorize everybody");
                }
                alwaysAuthorized = true ;
                return;
            }
          
            try {
                JDMSecurityDefs n = parser.SecurityDefs();
                n.buildAclEntries(owner, acl);
                n.buildTrapEntries(trapDestList);
                n.buildInformEntries(informDestList);
            } catch (ParseException e) {
                if (logger.finestOn()) {
                    logger.finest("readAuthorisedListFile", 
				  "Parsing exception " + e);
                }
		final IllegalArgumentException iae = 
		    new IllegalArgumentException("Syntax error: " + e);
		Utils.initCause(iae,e);
		throw iae;
            } catch (Error err) {
                if (logger.finestOn()) {
                    logger.finest("readAuthorisedListFile", 
				  "Error exception " + err);
                }
		final IllegalArgumentException iae = 
		    new IllegalArgumentException("Error: " + err);
		Utils.initCause(iae,err);
		throw iae;
            }
          
            for(Enumeration e = acl.entries(); e.hasMoreElements();) {
                AclEntryImpl aa = (AclEntryImpl) e.nextElement();
                if (logger.finerOn()) {
                    logger.finer("readAuthorisedListFile", "===> " + aa.getPrincipal().toString());
                }
                for (Enumeration eee = aa.permissions();eee.hasMoreElements();) {
                    java.security.acl.Permission perm = (java.security.acl.Permission)eee.nextElement();
                    if (logger.finerOn()) {
                        logger.finer("readAuthorisedListFile", "perm = " + perm);
                    }
                }
            }
        }
    }
  
    /**
     * Set the default full path for "jdmk.acl" input file.
     */
    private void setDefautFileName() throws IllegalArgumentException {
	String aclFile = null;	
	File file = null;
	
        if ((aclFile = (String) System.getProperty(JdmkProperties.ACL_FILE)) == null) {
            aclFile = DefaultPaths.getEtcDir("conf" + File.separator + "jdmk.acl");
	    if (logger.finestOn())
		logger.finest("setDefautFileName", 
		      "Default File name is : " + aclFile);
	    file = new File(aclFile);
	    if (file.isFile()) {
		if (logger.finerOn()) {
		    logger.finer("setDefautFileName", 
			  "Default Ip ACL file found : " + aclFile);
		}
	    } else {
		if (logger.finestOn()) {
		    logger.finest("setDefautFileName", "Default Ip ACL file not found.");
		}
	    }
        }
	else {
	    file = new File(aclFile);
	    if (!file.isFile()) {
		if (logger.finestOn()) {
		    logger.finest("setDefautFileName", "Ip ACL file not found. Wrong java property : " + JdmkProperties.ACL_FILE + "=" + aclFile);
		}
		throw new IllegalArgumentException("The specified file ["+ file +"] doesn't exist or is not a file, no configuration loaded");
	    }
	    else
		if (logger.finerOn()) {
		    logger.finer("setDefautFileName", "Ip ACL file found : " + aclFile);
		}
	}
        authorizedListFile = aclFile;
    }
  
    
    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"JdmkAcl");
    
    String dbgTag = "JdmkAcl";
    
    // PRIVATE VARIABLES
    //------------------
    
    /**
     * Represents the Access Control List.
     */
    private AclImpl acl = null;
    /**
     * Flag indicating whether the access is always authorized.
     * <BR>This is the case if there is no flat file defined.
     */
    private boolean alwaysAuthorized = false;
    /**
     * Represents the Access Control List flat file.
     */
    private String authorizedListFile = null;
    /**
     * Contains the hosts list for trap destination.
     */
    private Hashtable trapDestList = null;
    /**
     * Contains the hosts list for inform destination.
     */
    private Hashtable informDestList = null;
    
    private PrincipalImpl owner = null;
}
