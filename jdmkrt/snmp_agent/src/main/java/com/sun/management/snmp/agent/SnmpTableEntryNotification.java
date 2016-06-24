/*
 * @(#)file      SnmpTableEntryNotification.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.15
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


package com.sun.management.snmp.agent;



// jmx imports
//
import javax.management.Notification;
import javax.management.ObjectName;

/**
 * Represents a notification emitted when an
 * entry is added or deleted from an SNMP table.
 * <P>
 * The <CODE>SnmpTableEntryNotification</CODE> object contains 
 * the reference to the entry added or removed from the table.
 * <P>
 * The list of notifications fired by the <CODE>SnmpMibTable</CODE> is 
 * the following:
 * <UL>
 * <LI>A new entry has been added to the SNMP table.
 * <LI>An existing entry has been removed from the SNMP table.
  </UL>
 * 
 *
 * @since Java DMK 5.1
 */

public class SnmpTableEntryNotification extends Notification {
    /**
     * Creates and initializes a table entry notification object.
     *
     * @param type The notification type.
     * @param source The notification producer.
     * @param sequenceNumber The notification sequence number within the 
     *                  source object.
     * @param timeStamp The notification emission date.
     * @param entry     The entry object (may be null if the entry is 
     *                  registered in the MBeanServer).
     * @param entryName The ObjectName entry object (may be null if the
     *                  entry is not registered in the MBeanServer).
     */
    SnmpTableEntryNotification(String type, Object source, 
			       long sequenceNumber, long timeStamp, 
			       Object entry, ObjectName entryName) {
        
        super(type, source, sequenceNumber, timeStamp);
        this.entry = entry;
	this.name  = entryName;
    }
    
    /**
     * Gets the entry object.
     * May be null if the entry is registered in the MBeanServer, and the
     * MIB is using the generic MetaData (see mibgen).
     *
     * @return The entry.
     */
    public Object getEntry() {
        return entry;
    }
  
    /**
     * Gets the ObjectName of the entry.
     * May be null if the entry is not registered in the MBeanServer.
     *
     * @return The ObjectName of the entry.
     */
    public ObjectName getEntryName() {
        return name;
    }
  
    // PUBLIC VARIABLES
    //-----------------
    
    /**
     * Notification type denoting that a new entry has been added to the 
     * SNMP table.
     * <BR>The value of this notification type is 
     * <CODE>jdmk.snmp.table.entry.added</CODE>.
     */
    public static final String SNMP_ENTRY_ADDED = 
	new String("jdmk.snmp.table.entry.added");

    /**
     * Notification type denoting that an entry has been removed from the 
     * SNMP table.
     * <BR>The value of this notification type is 
     * <CODE>jmx.snmp.table.entry.removed</CODE>.
     */
    public static final String SNMP_ENTRY_REMOVED = 
	new String("jdmk.snmp.table.entry.removed");
    
    // PRIVATE VARIABLES
    //------------------
  
    /**
     * The entry object.
     * @serial
     */
    private final Object entry;  

    /**
     * The entry name.
     * @serial
     */
    private final ObjectName name;  

    // Ensure compatibility with Java DMK 4.2 FCS
    //
    private static final long serialVersionUID = 5832592016227890252L;
}
