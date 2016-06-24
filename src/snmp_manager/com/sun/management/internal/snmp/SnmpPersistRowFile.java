/*
 * @(#)file      SnmpPersistRowFile.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.23
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

import com.sun.jdmk.internal.ClassLogger;

import java.net.UnknownHostException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;

/**
 * FOR INTERNAL USE. DON'T USE THIS CLASS.
 *
 * @since Java DMK 5.1
 */
public class SnmpPersistRowFile {
    File file = null;
    SnmpPersistRowFileConsumer consumer = null;
    String property = null;
    String delimitor = null;
    boolean parsing = true;
    boolean throwException = false;
    public SnmpPersistRowFile(String file,
			      String property,
			      String delimitor,
			      SnmpPersistRowFileConsumer consumer) {
	if(file == null) 
	    throw new IllegalArgumentException("Passed file is null");
	this.file = new File(file);
	this.consumer = consumer;
	this.property = property;
	this.delimitor = delimitor;
    }

    public void enableException(boolean b) {
	throwException = b;	
    }

    public void read() throws FileNotFoundException, IOException {
	FileReader reader = null;
	try {
	    reader = new FileReader(file);
	    cutInRows(reader);
	}
	finally{
	    if(reader != null)
		reader.close();
	}
    }
    
    public FileWriter createWriter() 
	throws FileNotFoundException, IOException {
	return new FileWriter(file);
    }
    
    public File getFile() {
	return file;
    }

    public void write(FileWriter writer, String val) throws IOException{
	write(writer, property, val);
    }
    
    public void write(FileWriter writer, String prop, String val) 
	throws IOException {
	writer.write(prop + "=" + val);
    }
    
    public void releaseWriter(FileWriter writer) throws IOException {
	writer.close();
    }
 
    private void cutInRows(FileReader r) {
	BufferedReader in = new BufferedReader(r);
        String row = null;
        int lineNumber = 1;
	int storage = 0;
        try{
            //Read the first line to init the algo.
            row = in.readLine();
        }catch(IOException ex) { 
	    if(logger.finestOn()) {
		logger.finest("cutInRows", ex);
	    }
	}
        //While !EOF
	int i = 0;
	int line = 0;
	boolean err = false;
        while(row != null) {
	    line++;
            if(row.length() != 0) {
		if(row.startsWith(property+"=")) {
		    String userEntry = 
			row.substring(row.indexOf("=") + 1);
		    StringTokenizer token = 
			new StringTokenizer(userEntry, delimitor, parsing);
		    i = 0;
		    err = false;
		    Object context = consumer.rowBegin(row, line);
		    try {
			while(token.hasMoreTokens()) {
			    err = false;
			    String param = null;
			    try {
				param = token.nextToken();
				param = param.trim();
			    } catch( NoSuchElementException e ) {
				err = true;
				param = null;
			    }
			    
			    if(param != null)
				if(param.equals(delimitor)) //Empty field
				    param = null;
			    if(logger.finestOn())
				logger.finest("cutInRows", 
				      "value : " + param + " context : " + i);
			    consumer.treatToken(row, 
						line, // line number
						param, 
						i, 
						context,
						err);
			    if(parsing && (param != null)) {
				try {
				    token.nextToken();
				}catch(NoSuchElementException e) {
				    if(logger.finestOn())
					logger.finest("cutInRows", "Last token");
				}
			    }
			    i++;
			}
			consumer.rowEnd(context, row, line);
		    }catch(Exception e) {
			if(logger.finestOn()) {
			    logger.finest("cutInRows", e);
			}
			if(throwException) {
			    throw new IllegalArgumentException(e.toString() +
							       " ROW : ["+
							       row + "]" +
							       ", line :" + 
							       line);
			    
			}
		    }
		    
		}
	    }
	    try {
                // Read a new line.
                row = in.readLine();
                lineNumber ++;
            }catch(java.io.IOException e) {
                if(logger.finestOn()) 
                    logger.finest("cutRows", "Enable to read line :" + line);
                row = null;
            }
	}
    }

    // Logging
    //--------
    
    private static final ClassLogger logger = 
	new ClassLogger(ClassLogger.LOGGER_SNMP,"SnmpPersistRowFile");
    
    String dbgTag = "SnmpPersistRowFile";
}
