/*
 * @(#)file      SnmpStatusException.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.12
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


/**
 * Reports an error which occurred during a get/set operation on a mib node.
 *
 * This exception includes a status error code as defined in the SNMP protocol.
 *
 *
 * @since Java DMK 5.1
 */

public class SnmpStatusException extends Exception implements SnmpDefinitions {


    /**
     * Error code as defined in RFC 1448 for: <CODE>noSuchName</CODE>.
     */
    public static final int noSuchName         = 2 ;
  
    /**
     * Error code as defined in RFC 1448 for: <CODE>badValue</CODE>.
     */
    public static final int badValue           = 3 ;
  
    /**
     * Error code as defined in RFC 1448 for: <CODE>readOnly</CODE>.
     */
    public static final int readOnly           = 4 ;
  
   
    /**
     * Error code as defined in RFC 1448 for: <CODE>noAccess</CODE>.
     */
    public static final int noAccess           = 6 ;
  
    /**
     * Error code for reporting a no such instance error.
     */
    public static final int noSuchInstance     = 0xE0;
  
    /**
     * Error code for reporting a no such object error.
     */
    public static final int noSuchObject     = 0xE1;
  
    /**
     * Constructs a new <CODE>SnmpStatusException</CODE> with the specified 
     * status error.
     * @param status The error status.
     */
    public SnmpStatusException(int status) {
	errorStatus = status ;
    }

    /**
     * Constructs a new <CODE>SnmpStatusException</CODE> with the specified 
     * status error and status index.
     * @param status The error status.
     * @param index The error index.
     */
    public SnmpStatusException(int status, int index) {
	errorStatus = status ;
	errorIndex = index ;
    }
  
    /**
     * Constructs a new <CODE>SnmpStatusException</CODE> with an error message.
     * The error status is set to 0 (noError) and the index to -1.
     * @param s The error message.
     */
    public SnmpStatusException(String s) {
	super(s);
    }
  
    /**
     * Constructs a new <CODE>SnmpStatusException</CODE> with an error index.
     * @param x The original <CODE>SnmpStatusException</CODE>.
     * @param index The error index.
     */
    public SnmpStatusException(SnmpStatusException x, int index) {
	super(x.getMessage());
	errorStatus= x.errorStatus;
	errorIndex= index;
	initCause(this,x);
    }

    /**
     * Return the error status.
     * @return The error status.
     */
    public int getStatus() {
	return errorStatus ;
    }
  
    /**
     * Returns the index of the error.
     * A value of -1 means that the index is not known/applicable.
     * @return The error index.
     */
    public int getErrorIndex() {
	return errorIndex;
    }

  
    // PRIVATE VARIABLES
    //--------------------
  
    /**
     * Status of the error.
     * @serial
     */
    private int errorStatus = 0 ;
  
    /**
     * Index of the error.
     * If different from -1, indicates the index where the error occurs.
     * @serial
     */
    private int errorIndex= -1;

    
    /**
     * Init the cause field of a Throwable object.  
     * The cause field is set only if <var>t</var> has an 
     * {@link Throwable#initCause(Throwable)} method (JDK Version >= 1.4) 
     * @param t Throwable on which the cause must be set.
     * @param cause The cause to set on <var>t</var>.
     * @return <var>t</var> with or without the cause field set.
     */
    private static Throwable initCause(Throwable t, Throwable cause) {

        /* Make a best effort to set the cause, but if we don't
           succeed, too bad, you don't get that useful debugging
           information.  We jump through hoops here so that we can
           work on platforms prior to J2SE 1.4 where the
           Throwable.initCause method was introduced.  If we change
           the public interface of JMRuntimeException in a future
           version we can add getCause() so we don't need to do this.  */
        try {
            java.lang.reflect.Method initCause =
                t.getClass().getMethod("initCause",
                                       new Class[] {Throwable.class});
            initCause.invoke(t, new Object[] {cause});
        } catch (Exception e) {
            // too bad, no debugging info
        }
        return t;
    }

    /**
     * Compatibility with JDMK 5.0
     **/
    static private final long serialVersionUID = 594245968606990024L;
}



