/*
 * @(#)file      SnmpTooBigException.java
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
 * Is used internally to signal that the size of a PDU exceeds the packet size limitation.
 * <p>
 * You will not usually need to use this class, except if you 
 * decide to implement your own
 * {@link com.sun.management.snmp.SnmpPduFactory SnmPduFactory} object.
 * <p>
 * The <CODE>varBindCount</CODE> property contains the 
 * number of <CODE>SnmpVarBind</CODE> successfully encoded
 * before the exception was thrown. Its value is 0 
 * when this number is unknown.
 *
 *
 * @since Java DMK 5.1
 */

public class SnmpTooBigException extends Exception {
    private static final long serialVersionUID = -9001045366592540154L;

  /**
   * Builds an <CODE>SnmpTooBigException</CODE> with 
   * <CODE>varBindCount</CODE> set to 0.
   */
  public SnmpTooBigException() {
    varBindCount = 0 ;
  }
  
  /**
   * Builds an <CODE>SnmpTooBigException</CODE> with 
   * <CODE>varBindCount</CODE> set to the specified value.
   * @param n The <CODE>varBindCount</CODE> value.
   */
  public SnmpTooBigException(int n) {
    varBindCount = n ;
  }
  
  
  /**
   * Returns the number of <CODE>SnmpVarBind</CODE> successfully
   * encoded before the exception was thrown.
   *
   * @return A positive integer (0 means the number is unknown).
   */
  public int getVarBindCount() {
    return varBindCount ;
  }
  
  /**
   * The <CODE>varBindCount</CODE>.
   * @serial
   */
  private int varBindCount ;
}




