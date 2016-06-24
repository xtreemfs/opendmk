/* 
 * @(#)file      EnumRowStatus.java 
 * @(#)author    Sun Microsystems, Inc. 
 * @(#)version   1.12 
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
 */ 

package com.sun.management.snmp;

import java.io.Serializable;
import java.util.Hashtable;

import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpInt;

import com.sun.jdmk.Enumerated;

/**
 * This class is an internal class which is used to represent RowStatus
 * codes as defined in RFC 2579.
 *
 * It defines an additional code, <i>unspecified</i>, which is 
 * implementation specific, and is used to identify
 * unspecified actions (when for instance the RowStatus variable
 * is not present in the varbind list) or uninitialized values.
 *
 * mibgen does not generate objects of this class but any variable
 * using the RowStatus textual convention can be converted into an
 * object of this class thanks to the 
 * <code>EnumRowStatus(Enumerated valueIndex)</code> constructor.
 *
 *
 * @since Java DMK 5.1
 **/

public class EnumRowStatus extends Enumerated implements Serializable {
    private static final long serialVersionUID = 7431042651505238452L;

    /**
     * This value is Java DMK implementation specific, and is used to identify
     * unspecified actions (when for instance the RowStatus variable
     * is not present in the varbind list) or uninitialized values.
     */
    public final static int unspecified   = 0;

    /**
     * This value corresponds to the <i>active</i> RowStatus, as defined in
     * RFC 2579 from SMIv2:
     * <ul>
     * <i>active</i> indicates that the conceptual row is available for 
     * use by the managed device;
     * </ul>
     */
    public final static int active        = 1;

    /**
     * This value corresponds to the <i>notInService</i> RowStatus, as 
     * defined in RFC 2579 from SMIv2:
     * <ul>
     * <i>notInService</i> indicates that the conceptual
     * row exists in the agent, but is unavailable for use by
     * the managed device; <i>notInService</i> has
     * no implication regarding the internal consistency of
     * the row, availability of resources, or consistency with
     * the current state of the managed device;
     * </ul>
     **/
    public final static int notInService  = 2;

    /**
     * This value corresponds to the <i>notReady</i> RowStatus, as defined
     * in RFC 2579 from SMIv2:
     * <ul>
     * <i>notReady</i> indicates that the conceptual row
     * exists in the agent, but is missing information
     * necessary in order to be available for use by the
     * managed device (i.e., one or more required columns in
     * the conceptual row have not been instantiated);
     * </ul>
     */
    public final static int notReady      = 3;

    /**
     * This value corresponds to the <i>createAndGo</i> RowStatus,
     * as defined in RFC 2579 from SMIv2:
     * <ul>
     * <i>createAndGo</i> is supplied by a management
     * station wishing to create a new instance of a
     * conceptual row and to have its status automatically set
     * to active, making it available for use by the managed
     * device;
     * </ul>
     */
    public final static int createAndGo   = 4;

    /**
     * This value corresponds to the <i>createAndWait</i> RowStatus, 
     * as defined in RFC 2579 from SMIv2:
     * <ul>
     * <i>createAndWait</i> is supplied by a management
     * station wishing to create a new instance of a
     * conceptual row (but not make it available for use by
     * the managed device);
     * </ul>
     */
    public final static int createAndWait = 5;

    /**
     * This value corresponds to the <i>destroy</i> RowStatus, as defined in
     * RFC 2579 from SMIv2:
     * <ul>
     * <i>destroy</i> is supplied by a management station
     * wishing to delete all of the instances associated with
     * an existing conceptual row.
     * </ul>
     */
    public final static int destroy       = 6;

    /**
     * Build an <code>EnumRowStatus</code> from an <code>int</code>.
     * @param valueIndex should be either 0 (<i>unspecified</i>), or one of
     *        the values defined in RFC 2579.
     * @exception IllegalArgumentException if the given 
     *            <code>valueIndex</code> is not valid.
     **/
    public EnumRowStatus(int valueIndex) 
	throws IllegalArgumentException {
	super(valueIndex);
    }
    
    /**
     * Build an <code>EnumRowStatus</code> from an <code>Enumerated</code>.
     * @param valueIndex should be either 0 (<i>unspecified</i>), or one of
     *        the values defined in RFC 2579.
     * @exception IllegalArgumentException if the given 
     *            <code>valueIndex</code> is not valid.
     **/
    public EnumRowStatus(Enumerated valueIndex) 
	throws IllegalArgumentException {
	this(valueIndex.intValue());
    }
    
    /**
     * Build an <code>EnumRowStatus</code> from a <code>long</code>.
     * @param valueIndex should be either 0 (<i>unspecified</i>), or one of
     *        the values defined in RFC 2579.
     * @exception IllegalArgumentException if the given 
     *            <code>valueIndex</code> is not valid.
     **/
    public EnumRowStatus(long valueIndex) 
	throws IllegalArgumentException {
	this((int)valueIndex);
    }

    /**
     * Build an <code>EnumRowStatus</code> from an <code>Integer</code>.
     * @param valueIndex should be either 0 (<i>unspecified</i>), or one of
     *        the values defined in RFC 2579.
     * @exception IllegalArgumentException if the given 
     *            <code>valueIndex</code> is not valid.
     **/
    public EnumRowStatus(Integer valueIndex) 
	throws IllegalArgumentException {
	super(valueIndex);
    }
    
    /**
     * Build an <code>EnumRowStatus</code> from a <code>Long</code>.
     * @param valueIndex should be either 0 (<i>unspecified</i>), or one of
     *        the values defined in RFC 2579.
     * @exception IllegalArgumentException if the given 
     *            <code>valueIndex</code> is not valid.
     **/
    public EnumRowStatus(Long valueIndex) 
	throws IllegalArgumentException {
	this(valueIndex.longValue());
    }
    
    /**
     * Build an <code>EnumRowStatus</code> with <i>unspecified</i> value.
     **/
    public EnumRowStatus() 
	throws IllegalArgumentException {
	this(unspecified);
    }

    /**
     * Build an <code>EnumRowStatus</code> from a <code>String</code>.
     * @param x should be either "unspecified", or one of
     *        the values defined in RFC 2579 ("active", "notReady", etc...)
     * @exception IllegalArgumentException if the given String
     *            <code>x</code> is not valid.
     **/
    public EnumRowStatus(String x) 
	throws IllegalArgumentException {
	super(x);
    }

    /**
     * Build an <code>EnumRowStatus</code> from an <code>SnmpInt</code>.
     * @param valueIndex should be either 0 (<i>unspecified</i>), or one of
     *        the values defined in RFC 2579.
     * @exception IllegalArgumentException if the given 
     *            <code>valueIndex</code> is not valid.
     **/
    public EnumRowStatus(SnmpInt valueIndex) 
	throws IllegalArgumentException {
	this(valueIndex.intValue());
    }

    /**
     * Build an SnmpValue from this object.
     *
     * @exception IllegalArgumentException if this object holds an 
     *            <i>unspecified</i> value.
     * @return an SnmpInt containing this object value.   
     **/
    public SnmpInt toSnmpValue() 
	throws IllegalArgumentException {
	if (value == unspecified) 
	    throw new 
        IllegalArgumentException("`unspecified' is not a valid SNMP value.");
	return new SnmpInt(value);
    }

    /**
     * Check that the given <code>value</code> is valid.
     *
     * Valid values are:
     * <ul><li><i>unspecified(0)</i></li>
     *     <li><i>active(1)</i></li>
     *     <li><i>notInService(2)</i></li>
     *     <li><i>notReady(3)</i></li>
     *     <li><i>createAndGo(4)</i></li>
     *     <li><i>createAndWait(5)</i></li>
     *     <li><i>destroy(6)</i></li>
     * </ul>
     *
     **/
    static public boolean isValidValue(int value) {
	if (value < 0) return false;
	if (value > 6) return false;
	return true;
    }

    // Documented in Enumerated
    //
    protected Hashtable getIntTable() {
	return EnumRowStatus.getRSIntTable();
    }

    // Documented in Enumerated
    //
    protected Hashtable getStringTable() {
	return  EnumRowStatus.getRSStringTable();
    }

    static final Hashtable getRSIntTable() {
	return intTable ;
    }
    
    static final Hashtable getRSStringTable() {
	return stringTable ;
    }

    // Initialize the mapping tables.
    //
    final static Hashtable intTable = new Hashtable();
    final static Hashtable stringTable = new Hashtable();
    static  {
	intTable.put(new Integer(0), "unspecified");
	intTable.put(new Integer(3), "notReady");
	intTable.put(new Integer(6), "destroy");
	intTable.put(new Integer(2), "notInService");
	intTable.put(new Integer(5), "createAndWait");
	intTable.put(new Integer(1), "active");
	intTable.put(new Integer(4), "createAndGo");
	stringTable.put("unspecified", new Integer(0));
	stringTable.put("notReady", new Integer(3));
	stringTable.put("destroy", new Integer(6));
	stringTable.put("notInService", new Integer(2));
	stringTable.put("createAndWait", new Integer(5));
	stringTable.put("active", new Integer(1));
	stringTable.put("createAndGo", new Integer(4));
    }


}

