/*
 * @(#)file      SnmpSubRequestHandler.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   4.40
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


package com.sun.management.comm;



// java import
//
import java.util.Vector;

// jmx imports
//
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpEngine;

// RI imports
//
import com.sun.jdmk.internal.ClassLogger;


// jdmk import
//
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpMibRequest;
import com.sun.jdmk.ThreadContext;
import com.sun.management.internal.snmp.SnmpIncomingRequest;

class SnmpSubRequestHandler implements SnmpDefinitions, Runnable {
    
    protected SnmpIncomingRequest incRequest = null;
    protected SnmpEngine engine = null;
    /**
     * V3 enabled Adaptor. Each Oid is added using updateRequest method.
     */
    protected SnmpSubRequestHandler(SnmpEngine engine,
				    SnmpIncomingRequest incRequest,
				    SnmpMibAgent agent, 
				    SnmpPdu req) {
        this(agent, req);
	init(engine, incRequest);
    }
    
    /**
     * V3 enabled Adaptor. 
     */
    protected SnmpSubRequestHandler(SnmpEngine engine,
				    SnmpIncomingRequest incRequest,
				    SnmpMibAgent agent, 
				    SnmpPdu req,
				    boolean nouse) {
        this(agent, req, nouse);
	init(engine, incRequest);
    }
    /**
     * SNMP V1/V2 . To be called with updateRequest.
     */
    protected SnmpSubRequestHandler(SnmpMibAgent agent, SnmpPdu req) {
        
        dbgTag = makeDebugTag();
	logger = makeLogger(dbgTag);
        if (logger.finerOn()) {
            logger.finer("constructor", "creating instance for request " +
			 String.valueOf(req.requestId));
        }
	
        version= req.version;
        type= req.type;
        this.agent= agent;
	
	// We get a ref on the pdu in order to pass it to SnmpMibRequest.
	reqPdu = req;
	
        //Pre-allocate room for storing varbindlist and translation table.
        //
        int length= req.varBindList.length;
        translation= new int[length];
        varBind= new NonSyncVector(length);
    }

    /**
     * SNMP V1/V2 The constructor initialize the subrequest with the whole 
     * varbind list contained in the original request.
     */
    protected SnmpSubRequestHandler(SnmpMibAgent agent, 
				    SnmpPdu req, 
				    boolean nouse) {
        this(agent,req); 
    
        // The translation table is easy in this case ...
        //
        int max= translation.length;
        SnmpVarBind[] list= req.varBindList;
        for(int i=0; i < max; i++) {
            translation[i]= i;
            ((NonSyncVector)varBind).addNonSyncElement(list[i]);
        }
    }

    SnmpMibRequest createMibRequest(Vector vblist, 
				    int protocolVersion,
				    Object userData) {

	// This is an optimization: 
	//    The SnmpMibRequest created in the check() phase is
	//    reused in the set() phase.
	//
	if (type == pduSetRequestPdu && mibRequest != null)
	    return mibRequest;
	
	//This is a request comming from an SnmpV3AdaptorServer.
	//Full power.
	SnmpMibRequest result = null;
	if(incRequest != null) {
	    result = SnmpMibAgent.newMibRequest(engine,
						reqPdu,
						vblist,
						protocolVersion,
						userData,
						incRequest.getPrincipal(),
						incRequest.getSecurityLevel(),
						incRequest.getSecurityModel(),
						incRequest.getContextName(),
						incRequest.getAccessContext());
	} else {
	    result = SnmpMibAgent.newMibRequest(reqPdu,
						vblist,
						protocolVersion,
						userData);
	}
	// If we're doing the check() phase, we store the SnmpMibRequest
	// so that we can reuse it in the set() phase.
	//
	if (type == pduWalkRequest)
	    mibRequest = result;

	return result;
    }

    void setUserData(Object userData) {
	data = userData;
    }

    public void run() {
    
        try {
	    final ThreadContext oldContext = 
		ThreadContext.push("SnmpUserData",data);
	    try {
		switch(type) {
		case pduGetRequestPdu:
		    // Invoke a get operation
		    //
		    if (logger.finerOn()) {
			logger.finer("run", "[" + Thread.currentThread() + 
			      "]:get operation on " + agent.getMibName());
		    }
		
		    agent.get(createMibRequest(varBind,version,data));
		    break;
	
		case pduGetNextRequestPdu:
		    if (logger.finerOn()) {
			logger.finer("run", "[" + Thread.currentThread() + 
			      "]:getNext operation on " + agent.getMibName());
		    }
		    //#ifdef DEBUG
		    agent.getNext(createMibRequest(varBind,version,data));
		    break;
		    
		case pduSetRequestPdu:
		    if (logger.finerOn()) {
			logger.finer("run", "[" + Thread.currentThread() +
			      "]:set operation on " + agent.getMibName());
		    }
		    agent.set(createMibRequest(varBind,version,data));
		    break;
		    
		case pduWalkRequest:
		    if (logger.finerOn()) {
			logger.finer("run", "[" + Thread.currentThread() + 
			      "]:check operation on " + agent.getMibName());
		    }
		    agent.check(createMibRequest(varBind,version,data));
		    break;
		    
		default:
		    if (logger.finestOn()) {
			logger.finest("run", "[" + Thread.currentThread() + 
			      "]:unknown operation (" +  type + ") on " + 
			      agent.getMibName());
		    }
		    errorStatus= snmpRspGenErr;
		    errorIndex= 1;
		    break;
		    
		}// end of switch
		
	    } finally {
		ThreadContext.restore(oldContext);
	    }
	} catch(SnmpStatusException x) {
	    errorStatus = x.getStatus() ;
	    errorIndex=  x.getErrorIndex();
	    if (logger.finestOn()) {
		logger.finest("run", "[" + Thread.currentThread() + 
		      "]:a Snmp error occured during the operation");
		logger.finest("run",x);
	    }
	}
	catch(Exception x) {
	    errorStatus = SnmpDefinitions.snmpRspGenErr ;
            if (logger.finestOn()) {
                logger.finest("run", "[" + Thread.currentThread() + 
		      "]: Unexpected exception: " + x);
		logger.finest("run",x);
            }
	    if (logger.finerOn()) {
		logger.finer("run", "[" + Thread.currentThread() + 
		      "]a generic error occured during the operation");
	    }
	}
	if (logger.finerOn()) {
	    logger.finer("run", "[" + Thread.currentThread() +
		  "]:operation completed");
	}
    }
    
    // -------------------------------------------------------------
    //
    // This function does a best-effort to map global error status
    // to SNMP v1 valid global error status.
    //
    // An SnmpStatusException can contain either:
    // <li> v2 local error codes (that should be stored in the varbind)</li>
    // <li> v2 global error codes </li>
    // <li> v1 global error codes </li>
    //
    // v2 local error codes (noSuchInstance, noSuchObject) are
    // transformed in a global v1 snmpRspNoSuchName error.
    //
    // v2 global error codes are transformed in the following way:
    //    
    //    If the request was a GET/GETNEXT then either 
    //         snmpRspNoSuchName or snmpRspGenErr is returned.
    //
    //    Otherwise:
    //      snmpRspNoAccess, snmpRspInconsistentName 
    //               => snmpRspNoSuchName
    //      snmpRspAuthorizationError, snmpRspNotWritable, snmpRspNoCreation
    //               => snmpRspReadOnly  (snmpRspNoSuchName for GET/GETNEXT)
    //      snmpRspWrong* 
    //               => snmpRspBadValue  (snmpRspNoSuchName for GET/GETNEXT)
    //      snmpRspResourceUnavailable, snmpRspRspCommitFailed, 
    //      snmpRspUndoFailed
    //                  => snmpRspGenErr
    //
    // -------------------------------------------------------------
    //
    static final int mapErrorStatusToV1(int errorStatus, int reqPduType) {
	// Map v2 codes onto v1 codes
	// 
        if (errorStatus == SnmpDefinitions.snmpRspNoError)
            return SnmpDefinitions.snmpRspNoError;

        if (errorStatus == SnmpDefinitions.snmpRspGenErr)
            return SnmpDefinitions.snmpRspGenErr;

        if (errorStatus == SnmpDefinitions.snmpRspNoSuchName)
            return SnmpDefinitions.snmpRspNoSuchName;
	
	if ((errorStatus == SnmpStatusException.noSuchInstance) ||
	    (errorStatus == SnmpStatusException.noSuchObject)   ||
	    (errorStatus == SnmpDefinitions.snmpRspNoAccess)    ||
	    (errorStatus == SnmpDefinitions.snmpRspInconsistentName) ||
	    (errorStatus == SnmpDefinitions.snmpRspAuthorizationError)){
	    
	    return SnmpDefinitions.snmpRspNoSuchName;
	    
	} else if ((errorStatus == 
		    SnmpDefinitions.snmpRspAuthorizationError)         ||
		   (errorStatus == SnmpDefinitions.snmpRspNotWritable)) {
	    
	    if (reqPduType == SnmpDefinitions.pduWalkRequest)
		return SnmpDefinitions.snmpRspReadOnly;
	    else
		return SnmpDefinitions.snmpRspNoSuchName;
	    
	} else if ((errorStatus == SnmpDefinitions.snmpRspNoCreation)) {
	    
		return SnmpDefinitions.snmpRspNoSuchName;

	} else if ((errorStatus == SnmpDefinitions.snmpRspWrongType)      ||
		   (errorStatus == SnmpDefinitions.snmpRspWrongLength)    ||
		   (errorStatus == SnmpDefinitions.snmpRspWrongEncoding)  ||
		   (errorStatus == SnmpDefinitions.snmpRspWrongValue)     ||
		   (errorStatus == SnmpDefinitions.snmpRspWrongLength)    ||
		   (errorStatus == 
		    SnmpDefinitions.snmpRspInconsistentValue)) {
	    
	    if ((reqPduType == SnmpDefinitions.pduSetRequestPdu) || 
		(reqPduType == SnmpDefinitions.pduWalkRequest))
		return SnmpDefinitions.snmpRspBadValue;
	    else
		return SnmpDefinitions.snmpRspNoSuchName;
	    
	} else if ((errorStatus == 
		    SnmpDefinitions.snmpRspResourceUnavailable) ||
		   (errorStatus == 
		    SnmpDefinitions.snmpRspCommitFailed)        ||
		   (errorStatus == SnmpDefinitions.snmpRspUndoFailed)) {
	    
	    return SnmpDefinitions.snmpRspGenErr;
	    
	}

	// At this point we should have a V1 error code
	//
	if (errorStatus == SnmpDefinitions.snmpRspTooBig)
	    return SnmpDefinitions.snmpRspTooBig;

	if( (errorStatus == SnmpDefinitions.snmpRspBadValue) ||
	    (errorStatus == SnmpDefinitions.snmpRspReadOnly)) {
	    if ((reqPduType == SnmpDefinitions.pduSetRequestPdu) || 
		(reqPduType == SnmpDefinitions.pduWalkRequest))
		return errorStatus;
	    else 
		return SnmpDefinitions.snmpRspNoSuchName;
	}

	// We have a snmpRspGenErr, or something which is not defined
	// in RFC1905 => return a snmpRspGenErr
	//
	return SnmpDefinitions.snmpRspGenErr;

    }

    // -------------------------------------------------------------
    //
    // This function does a best-effort to map global error status
    // to SNMP v2 valid global error status.
    //
    // An SnmpStatusException can contain either:
    // <li> v2 local error codes (that should be stored in the varbind)</li>
    // <li> v2 global error codes </li>
    // <li> v1 global error codes </li>
    //
    // v2 local error codes (noSuchInstance, noSuchObject) 
    // should not raise this level: they should have been stored in the
    // varbind earlier. If they, do there is nothing much we can do except
    // to transform them into:
    // <li> a global snmpRspGenErr (if the request is a GET/GETNEXT) </li>
    // <li> a global snmpRspNoSuchName otherwise. </li>
    // 
    // v2 global error codes are transformed in the following way:
    //    
    //    If the request was a GET/GETNEXT then snmpRspGenErr is returned.
    //    (snmpRspGenErr is the only global error that is expected to be
    //     raised by a GET/GETNEXT request).
    //
    //    Otherwise the v2 code itself is returned
    //
    // v1 global error codes are transformed in the following way:
    //
    //      snmpRspNoSuchName 
    //               => snmpRspNoAccess  (snmpRspGenErr for GET/GETNEXT)
    //      snmpRspReadOnly
    //               => snmpRspNotWritable (snmpRspGenErr for GET/GETNEXT)
    //      snmpRspBadValue
    //               => snmpRspWrongValue  (snmpRspGenErr for GET/GETNEXT)
    //
    // -------------------------------------------------------------
    //
    static final int mapErrorStatusToV2(int errorStatus, int reqPduType) {
	// Map v1 codes onto v2 codes
	// 
        if (errorStatus == SnmpDefinitions.snmpRspNoError)
            return SnmpDefinitions.snmpRspNoError;

        if (errorStatus == SnmpDefinitions.snmpRspGenErr)
            return SnmpDefinitions.snmpRspGenErr;

        if (errorStatus == SnmpDefinitions.snmpRspTooBig)
            return SnmpDefinitions.snmpRspTooBig;

	// For get / getNext / getBulk the only global error
	// (PDU-level) possible is genErr.
	//
	if ((reqPduType != SnmpDefinitions.pduSetRequestPdu) && 
	    (reqPduType != SnmpDefinitions.pduWalkRequest)) {
	    if(errorStatus == SnmpDefinitions.snmpRspAuthorizationError)
		return errorStatus;
	    else
		return SnmpDefinitions.snmpRspGenErr;
	}

	// Map to noSuchName
	//	if ((errorStatus == SnmpDefinitions.snmpRspNoSuchName) ||
	//   (errorStatus == SnmpStatusException.noSuchInstance) ||
	//  (errorStatus == SnmpStatusException.noSuchObject))
	//  return SnmpDefinitions.snmpRspNoSuchName;

	// SnmpStatusException.noSuchInstance and 
	// SnmpStatusException.noSuchObject can't happen...

	if (errorStatus == SnmpDefinitions.snmpRspNoSuchName)
	    return SnmpDefinitions.snmpRspNoAccess;

	// Map to notWritable
 	if (errorStatus == SnmpDefinitions.snmpRspReadOnly)
		return SnmpDefinitions.snmpRspNotWritable;

	// Map to wrongValue
 	if (errorStatus == SnmpDefinitions.snmpRspBadValue)
	    return SnmpDefinitions.snmpRspWrongValue;
	
	// Other valid V2 codes
	if ((errorStatus == SnmpDefinitions.snmpRspNoAccess) ||
	    (errorStatus == SnmpDefinitions.snmpRspInconsistentName) ||
	    (errorStatus == SnmpDefinitions.snmpRspAuthorizationError) ||
	    (errorStatus == SnmpDefinitions.snmpRspNotWritable) ||
	    (errorStatus == SnmpDefinitions.snmpRspNoCreation) ||
	    (errorStatus == SnmpDefinitions.snmpRspWrongType) ||
	    (errorStatus == SnmpDefinitions.snmpRspWrongLength) ||
	    (errorStatus == SnmpDefinitions.snmpRspWrongEncoding) ||
	    (errorStatus == SnmpDefinitions.snmpRspWrongValue) ||
	    (errorStatus == SnmpDefinitions.snmpRspWrongLength) ||
	    (errorStatus == SnmpDefinitions.snmpRspInconsistentValue) ||
	    (errorStatus == SnmpDefinitions.snmpRspResourceUnavailable) ||
	    (errorStatus == SnmpDefinitions.snmpRspCommitFailed) ||
	    (errorStatus == SnmpDefinitions.snmpRspUndoFailed)) 
	    return errorStatus;

	// Ivalid V2 code => genErr
	return SnmpDefinitions.snmpRspGenErr;
    }
    
    static final int mapErrorStatus(int errorStatus, 
				    int protocolVersion,
				    int reqPduType) {
        if (errorStatus == SnmpDefinitions.snmpRspNoError)
            return SnmpDefinitions.snmpRspNoError;
	
        // Too bad, an error occurs ... we need to translate it ...
        //
	if (protocolVersion == SnmpDefinitions.snmpVersionOne)
	    return mapErrorStatusToV1(errorStatus,reqPduType);
        if (protocolVersion == SnmpDefinitions.snmpVersionTwo ||
	    protocolVersion == SnmpDefinitions.snmpVersionThree)
	    return mapErrorStatusToV2(errorStatus,reqPduType);
	
        return SnmpDefinitions.snmpRspGenErr;
    }

    /**
     * The method returns the error status of the operation.
     * The method takes into account the protocol version.
     */
    protected int getErrorStatus() {
        if (errorStatus == snmpRspNoError)
            return snmpRspNoError;
	    
        return mapErrorStatus(errorStatus,version,type);
    }
    
    /**
     * The method returns the error index as a position in the var bind list.
     * The value returned by the method corresponds to the index in the original 
     * var bind list as received by the SNMP protocol adaptor.
     */
    protected int getErrorIndex() {
        if  (errorStatus == snmpRspNoError)
            return -1;
    
        // An error occurs. We need to be carefull because the index
        // we are getting is a valid SNMP index (so range starts at 1).
        // FIX ME: Shall we double-check the range here ?
        // The response is : YES :
        if ((errorIndex == 0) || (errorIndex == -1))
            errorIndex = 1;
	
        return translation[errorIndex -1];
    }
  
    /**
     * The method updates the varbind list of the subrequest.
     */
    protected  void updateRequest(SnmpVarBind var, int pos) {
        int size= varBind.size();
        translation[size]= pos;
        varBind.addElement(var);
    }
  
    /**
     * The method updates a given var bind list with the result of a 
     * previously invoked operation.
     * Prior to calling the method, one must make sure that the operation was
     * successful. As such the method getErrorIndex or getErrorStatus should be
     * called.
     */
    protected void updateResult(SnmpVarBind[] result) {
  
	if (result == null) return;
        final int max=varBind.size();
	final int len=result.length;
        for(int i= 0; i< max ; i++) {
	    // bugId 4641694: must check position in order to avoid 
	    //       ArrayIndexOutOfBoundException
	    final int pos=translation[i];
	    if (pos < len) {
		result[pos] = 
		    (SnmpVarBind)((NonSyncVector)varBind).elementAtNonSync(i);
	    } else {
		logger.finest("updateResult","Position `"+pos+
			      "' is out of bound...");
	    }
        }
    }

    private void init(SnmpEngine engine,
		      SnmpIncomingRequest incRequest) {
	this.incRequest = incRequest;
	this.engine = engine;	
    }

    protected String makeDebugTag() {
        return "SnmpSubRequestHandler";
    }

    protected ClassLogger makeLogger(String dbgTag) {
	return new ClassLogger(ClassLogger.LOGGER_ADAPTOR_SNMP,dbgTag);
    }

    final ClassLogger logger;

    // PRIVATE VARIABLES
    //------------------

    protected String dbgTag = null;
    
    /**
     * Store the protocol version to handle
     */
    protected int version= snmpVersionOne;

    /**
     * Store the operation type. Remember if the type is Walk, it means
     * that we have to invoke the check method ...
     */
    protected int type= 0;

    /**
     * Agent directly handled by the sub-request handler.
     */
    protected SnmpMibAgent agent;

    /**
     * Error status
     */
    protected int errorStatus= snmpRspNoError;
  
    /**
     * Index of error. 
     * The value <code>-1</code> means no error
     */
    protected int errorIndex= -1;
  
    /**
     * The varbind list specific to the current sub request.
     * The vector must contain object of type SnmpVarBind.
     */
    protected Vector varBind;
  
    /**
     * The array giving the index translation between the content of 
     * <VAR>varBind</VAR> and the varbind list as specified in the request.
     */
    protected int[] translation;

    /**
     * Contextual object allocated by the SnmpUserDataFactory.
     **/
    protected Object data;

    /**
     * The SnmpMibRequest that will be passed to the agent.
     *
     **/
    private   SnmpMibRequest mibRequest = null;
    
    /**
     * The SnmpPdu that will be passed to the request.
     *
     **/
    private   SnmpPdu reqPdu = null;
    
    // All the methods of the Vector class are synchronized.
    // Synchronization is a very expensive operation. In our case it is not 
    // always
    // required...
    //
    class NonSyncVector extends Vector {
	private static final long serialVersionUID = -2519481584193465089L;
    
        public NonSyncVector(int size) {
            super(size);
        }
    
        final void addNonSyncElement(Object obj) {
            ensureCapacity(elementCount + 1);
            elementData[elementCount++] = obj;
        }
    
        final Object elementAtNonSync(int index) {
            return elementData[index];
        }
    };
}
