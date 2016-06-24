/*
 * @(#)file      SnmpProxy.java
 * @(#)author    Sun Microsystems, Inc.
 * @(#)version   1.53
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

// java import
//
import java.io.Serializable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.NoSuchElementException;
import java.net.UnknownHostException;
import java.net.InetAddress;
// RI import
//
import javax.management.ObjectName;
import javax.management.MBeanServer;
import com.sun.management.snmp.SnmpDefinitions;
import com.sun.management.snmp.SnmpOid;
import com.sun.management.snmp.SnmpCounter;
import com.sun.management.snmp.SnmpCounter64;
import com.sun.management.snmp.SnmpValue;
import com.sun.management.snmp.SnmpStatusException;
import com.sun.management.snmp.SnmpVarBind;
import com.sun.management.snmp.SnmpVarBindList;
import com.sun.management.snmp.manager.SnmpPeer;
import com.sun.management.snmp.manager.SnmpParameters;
import com.sun.management.snmp.manager.SnmpParams;
import com.sun.management.snmp.SnmpPdu;
import com.sun.management.snmp.SnmpPduPacket;
import com.sun.management.snmp.SnmpScopedPduPacket;
import com.sun.management.snmp.manager.SnmpRequest;

import com.sun.management.snmp.SnmpEngineId;
import com.sun.management.snmp.SnmpUnknownModelException;
import com.sun.management.snmp.SnmpEngine;

// jdmk import
//
import com.sun.management.snmp.agent.SnmpMibAgent;
import com.sun.management.snmp.agent.SnmpMibRequest;
import com.sun.jdmk.internal.ClassLogger;

import com.sun.management.snmp.manager.SnmpSession;


/**
 * The SnmpProxy class provides an implementation of
 * an SNMP proxy. It is a MBean forwarding calls to a remote sub agent.
 * Multiple instances of SnmpProxy can run within an agent. Each of them
 * being a proxy for a remote agent.
 * <P> SnmpProxy implements the whole set of SNMP operations :
 * <ul>
 * <li> get </li>
 * <li> set </li>
 * <li> getNext </li>
 * <li> getBulk </li>
 * </ul>
 * </P>
 * <P> SnmpProxy fully translates SNMP V1 to SNMP V2 and SNMP V2 to SNMP V1.
 * It allows also V1 to V1 and V2 to V2 forwarding. SnmpProxy doesn't deal
 * with traps. </P>
 * <P> Handled manager protocols :
 * <ul>
 * <li> SNMP V1 </li>
 * <li> SNMP V2 </li>
 * <li> SNMP V3 </li>
 * </ul>
 *
 * <P> The V1 or V2 or V3 ==> V1 or V2 security parameters follow the rules
 * coded in the method  {@link
 * com.sun.management.snmp.agent.SnmpProxy#translateParameters translateParameters}.
 * If you want to change them, just overloaded this method. </P>
 * <P> The policy details are: </P>
 * <ul>
 * <li> SNMP V1 or V2 received call : The received community strings are
 *   reused. </li>
 * <li> SNMP V3 received call : If a context name is provided (means that
 *   the proxy has been registered in the scope of a context),
 *   <CODE>public</CODE> is used as the read community string and
 *   <CODE>private</CODE> as the write one.</li>
 *</ul>
 * <P> Java DMK 5.0 introduces the notion of SNMP engine. An engine
 * is generally associated to an SNMP entity. When embedding a proxy
 * within a Java DMK agent, the <CODE> SnmpV3AdaptorServer </CODE>
 * engine must be passed.
 *
 *
 * @since Java DMK 5.1
 */
public class SnmpProxy extends SnmpMibAgent
    implements Serializable {
    private static final long serialVersionUID = 5147896704225603584L;

    // CONSTRUCTORS
    //===============
    //

    /**
     * Initializes this SNMP proxy with a SnmpPeer to which calls are
     * forwarded. The passed root OID is used when registering the proxy
     * within the adaptor. The proxy name default value is "SnmpProxy".
     * @param engine The SNMP adaptor engine.
     * @param peer The peer representing the proxied agent.
     * @param rootOid The proxy root OID.
     * @exception IllegalArgumentException if the passed engine is null.
     * @exception SnmpStatusException An error occurred while accessing
     *            a MIB node.
     */

    public SnmpProxy(SnmpEngine engine,
                     SnmpPeer peer,
                     String rootOid)
        throws  SnmpStatusException, IllegalArgumentException {
        initializeProxy(engine,
                        peer,
                        rootOid,
                        "SnmpProxy");
    }

    /**
     * Initializes this SNMP proxy with a SnmpPeer to which calls are
     * forwarded. The passed root OID is used when registering the proxy
     * within the adaptor. The name is used when registering the proxy in
     * the <CODE> SnmpAdaptorServer </CODE>
     * @param engine The SNMP adaptor engine.
     * @param peer The peer representing the proxied agent.
     * @param rootOid The proxy root OID.
     * @param name The proxy name.
     * @exception IllegalArgumentException if the passed engine is null.
     * @exception SnmpStatusException An error occurred while accessing
     *            a MIB node.
     */

    public SnmpProxy(SnmpEngine engine,
                     SnmpPeer peer,
                     String rootOid,
                     String name)
        throws SnmpStatusException, IllegalArgumentException {
        initializeProxy(engine,
                        peer,
                        rootOid,
                        name);
    }

    /**
     * If the proxied agent is answering an error during a getnext or
     * getbulk, this proxy will throw a <CODE>SnmpStatusException</CODE>.
     * In some cases (multiple proxy registered in the same adaptor), it
     * can be useful to skip the error answer. To do so, you must call
     * this method passing it true.
     *
     *<P> By default a proxy will not skip the error.
     * @param skip True will skip, false will not.
     */
    public void skipError(boolean skip) {
        // Fix : 4673640 SnmpProxy should handle faulty sub agent in case
        // of getnext
        skipError = skip;
    }

    /**
     * True means that this proxy doesn't throw exception in the case of
     * erroneous getnext. False means that this proxy throws an exception.
     * @return The skip error status.
     */
    public boolean skipError() {
        //Fix : 4673640 SnmpProxy should handle faulty sub agent in case
        //      of getnext
        return skipError;
    }

    /**
     * Returns the SNMP engine the proxy is based on.
     * @return The SNMP engine used.
     */
    public SnmpEngine getEngine() {
        return engine;
    }

    /**
     * Gets the proxy / sub agent communication total timeout.
     *
     * @return The timeout in milliseconds.
     */
    public long getTimeout() {
        return syncGetTimeout();
    }

    /**
     * Sets the proxy / agent communication total timeout. This method is
     * thread-safe. Any ongoing communication will be not affected by the
     * setting. The default timeout is SnmpProxy.defaultTimeout.
     *
     * @param t The timeout in milliseconds.
     */

    public void setTimeout(long t) {
        syncSetTimeout(t);
    }


    // INITIALIZATION
    //===============
    /**
     * Initialization of the SnmpProxy with no registration in Java DMK.
     * @exception IllegalAccessException The used session cannot be
     *            initialized.
     */
    public void init() throws IllegalAccessException {

        if(initialized) return;

        if(logger.finestOn())
            logger.finer("init", "init");

        try {
            //First thing to do is to instantiate the default session.
            SnmpSession session = new SnmpSession(engine,
                                                  "Snmp session",
                                                  null);

            provider = new SnmpSessionProvider(peer,
                                               session);
        } catch(SnmpStatusException e) {
            if(logger.finerOn())
                logger.finer("init", e.toString());

            throw new IllegalAccessException(e.toString());
        }
        initialized = true;
    }

    /**
     * Initialization of the SnmpProxy with registration in Java DMK.
     * @param server The reference to the MBean server.
     * @param name The object name of this SnmpProxy.
     */

    public ObjectName preRegister(MBeanServer server, ObjectName name)
        throws Exception {
        this.server = server;
        init();
        return name;
    }

    /**
     * Initialization of the proxy stuff.
     *
     * @param peer   The SnmpPeer.
     * @param strOid The proxy root OID, in dot notation.
     * @param name   The proxy name.
     *
     * @exception SnmpStatusException An error occurred while accessing
     *            a MIB node.
     */
    private void initializeProxy(SnmpEngine engine,
                                 SnmpPeer peer,
                                 String strOid,
                                 String name)
        throws SnmpStatusException, IllegalArgumentException {
        if(engine == null)
            new IllegalArgumentException("Engine can't be null");
        if(logger.finerOn())
            logger.finer("initializeProxy",
                  "initializing snmp proxy with : \n" +
                  "engine : " + engine + "\n" +
                  "peer : " + peer.toString() + "\n" +
                  "root OID : " + strOid + "\n" +
                  "mib name : " + name + "\n");
        this.engine = engine;
        oid = strOid;
        mibName = name;
        this.peer = peer;
    }

    /**
     * Gets the distant agent MIB name.
     *
     * @return The distant agent MIB name.
     */

    public String getMibName()
    {
        return mibName;
    }

    /**
     * Implement the get method from the abstract SnmpMibAgent class.
     * Handles V1 to V2 and V2 to V1 SNMP protocol translations.
     *
     * @param inRequest The SnmpMibRequest object holding the list of
     *            variable to be retrieved. This list is composed of
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException  An error occurred during the operation.
     */

    public synchronized void get(SnmpMibRequest inRequest)
        throws SnmpStatusException {

        if (logger.finerOn())
            logger.finer("get", "Sending get request to SNMP sub-agent");
        provider.hideInvalidResponseError(hide);
        // Find out the session and set parameters.
        SnmpSession session = defineSession(inRequest);
        get(session.getDefaultPeer(), session, inRequest);
    }

    /**
     * Implement the set method from the abstract SnmpMibAgent class.
     * Handles V1 to V2 and V2 to V1 SNMP protocol translations.
     *
     * @param inRequest The SnmpMibRequest object holding the list of
     *            variable to be retrieved. This list is composed of
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException  An error occurred during the operation.
     */
    public synchronized void set(SnmpMibRequest inRequest)
        throws SnmpStatusException {
        if(!isSetRequestForwardedOnCheck()) {
            if (logger.finerOn())
                logger.finer("set", "Sending set request to SNMP sub-agent.");
            provider.hideInvalidResponseError(hide);
            // No fix on error for set.
            SnmpSession session = provider.getDefaultSession();
            //Parameters to use when forwarding the call.
            // Method protected that can be overloaded.
            SnmpParams params = translateParameters(inRequest);
            params.setProtocolVersion(session.getDefaultPeer().getParams().
                                      getProtocolVersion());
            //Set these params to the peer.
            session.getDefaultPeer().setParams(params);
            set(session.getDefaultPeer(), session, inRequest);
        }else {
            if(logger.finerOn())
                logger.finer("set",
                             "Doing nothing, set already done on check.");
        }
    }

    /**
     * By default, set requests are forwarded when
     * <CODE>set(SnmpMibRequest request)</CODE> is called. Doing so makes the
     * sub agent returned errors to be systematically mapped to undoFailed.
     * If you want more details on the sub agent error status, call this
     * method with true value. The set request will be then forwarded when
     * <CODE>check(SnmpMibRequest request) </CODE> is called. No error
     * translation will be done.
     * @param check True, the set request is forwarded on check, false the
     * set request is forwarded on set.
     */
    public synchronized void forwardSetRequestOnCheck(boolean check) {
        forwardOnCheck = check;
    }

    /**
     * Returns the way a set request is forwarded by this
     * <CODE>SnmpProxy</CODE>.
     * @return True, the set request is forwarded on check, false the set
     * request is forwarded on set.
     */
    public synchronized boolean isSetRequestForwardedOnCheck() {
        return forwardOnCheck;
    }

    /**
     * Hides invalid response errors.
     * <p>
     * Sub agent invalid response requests (e.g. invalid BER encoding) can lead to a request timeout.
     * This behavior can be changed by calling this method and passing it false. Providing false
     * will make this <CODE>SnmpProxy</CODE> to throw a GenError if an internal error occurs.
     * @param hide True, hide the internal errors, requests will fail in timeout. False throw a GenError.
     */
    public synchronized void hideInvalidResponseError(boolean hide) {
        this.hide = hide;
    }

    /**
     * Checks if invalid response errors are hidden.
     * <p>
     * Invalid response requests (e.g. invalid BER encoding) are dropped
     * by the manager API. This makes the requests to timeout. This behavior
     * can be changed by calling the method
     * <CODE>hideInvalidResponseError</CODE>. By default the errors are hidden.
     * @return true, the internal errors are hidden (Requests failing in timeout).
     *      False, the internal errors are not hidden (GenError is thrown).
     */
    public synchronized boolean isInvalidResponseErrorHidden() {
        return hide;
    }

    /**
     * Implement the check method from the abstract SnmpMibAgent class.
     * By default nothing is done on check. If
     * <CODE>forwardSetRequestOnCheck</CODE> has been set to true,
     * the set request is forwarded on the check.
     * <P> Doing the set on check allows you to debug easily your master agent.
     * The sub agent returned errors are forwarded to the manager without any
     * translation (undoFailed is the error status returned when the set is
     * done on the set).
     * Handles V1 to V2 and V2 to V1 SNMP protocol translations.
     *
     * @param inRequest The SnmpMibRequest object holding the list of variable
     * to be retrieved. This list is composed of <CODE>SnmpVarBind</CODE>
     * objects.
     *
     * @exception SnmpStatusException  An error occurred during the operation.
     */
    public synchronized void check(SnmpMibRequest inRequest)
        throws SnmpStatusException {
        provider.hideInvalidResponseError(hide);
        if(isSetRequestForwardedOnCheck()) {
            if (logger.finerOn())
                logger.finer("check",
                             "Sending set request to SNMP sub-agent.");
            // No fix on error for set.
            SnmpSession session = provider.getDefaultSession();
            //Parameters to use when forwarding the call.
            // Method protected that can be overloaded.
            SnmpParams params = translateParameters(inRequest);
            params.setProtocolVersion(session.getDefaultPeer().
                                      getParams().getProtocolVersion());
            //Set these params to the peer.
            session.getDefaultPeer().setParams(params);
            set(session.getDefaultPeer(), session, inRequest);
        }
        else {
            if (logger.finerOn())
                logger.finer("check", "Nothing to do, set forwarded on set");
        }
    }

    /**
     * Implement the getNext method from the abstract SnmpMibAgent class.
     * Handles V1 to V2 and V2 to V1 SNMP protocol translations.
     *
     * @param inRequest The SnmpMibRequest object holding the list of
     *            variable to be retrieved. This list is composed of
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException  An error occurred during the operation.
     */

    public synchronized void getNext(SnmpMibRequest inRequest)
        throws SnmpStatusException {

        if (logger.finerOn())
            logger.finer("getNext",
                         "Sending getNext request to SNMP sub-agent.");
        provider.hideInvalidResponseError(hide);
        // Find out the session and set parameters.
        SnmpSession session = defineSession(inRequest);
        getNext(session.getDefaultPeer(), session, inRequest);
    }

    /**
     * Implement the getBulk method from the abstract SnmpMibAgent class.
     * Handles V1 to V2 and V2 to V1 SNMP protocol translations.
     * You have to be aware that getBulk is implemented by issuing multiple
     * getNext. You will receive as many {@link
     * com.sun.management.snmp.agent.SnmpProxy#getNext} as repetition is needed.
     *
     * @param request The SnmpMibRequest object holding the list of variable to
     *            be retrieved. This list is composed of
     *            <CODE>SnmpVarBind</CODE> objects.
     * @param nonRepeat The number of variables, starting with the first
     *    variable in the variable-bindings, for which a single
     *    lexicographic successor is requested.
     *
     * @param maxRepeat The number of lexicographic successors requested
     *    for each of the last R variables. R is the number of variables
     *    following the first <CODE>nonRepeat</CODE> variables for which
     *    multiple lexicographic successors are requested.
     *
     * @exception SnmpStatusException  An error occurred during the operation.
     */

    public synchronized void getBulk(SnmpMibRequest request, int nonRepeat,
                                     int maxRepeat)
        throws SnmpStatusException {

        final int vers = request.getVersion();
        if(vers == SnmpDefinitions.snmpVersionOne){
            if (logger.finerOn())
            logger.finer("getBulk",
                   "Receive a getBulk in SnmpV1 protocol for sub-agent.");

            throw new SnmpStatusException(SnmpDefinitions.snmpRspGenErr, 0);
        }
        provider.hideInvalidResponseError(hide);
        if (logger.finerOn())
            logger.finer("getBulk", "Sending getBulk (using getNext) " +
                  "request to SNMP sub-agent.");


        getBulkWithGetNext(request, nonRepeat, maxRepeat);
    }

    /**
     * Returns the root object identifier of the MIB.
     *
     * @return The remote sub agent root OID.
     */

    public long[] getRootOid() {
        if(logger.finerOn())
        logger.finer("getRootOid", "Root oid to return : " + oid);

        rootOid = resolveOidString(oid);
        return rootOid;
    }

    /**
     * Returns the Peer associated with this proxy.
     * @return The peer modeling the distant host.
     */
    public final SnmpPeer getPeer() {
        return peer;
    }

    // PROTECTED METHODS, SnmpProxy extensible API.
    //=============================================
    //

    /**
     * Called when a report is received when forwarding a request.
     * If the returned value is true, the proxy will retry the current request.
     * @param request The request containing the received report.
     * @return True means retry the call, the report has been handled.
     * False means don't retry, the report wasn't fixed.
     * <P> The default implementation does nothing and returns false.
     */
    protected boolean handleReport(SnmpRequest request) {
        return false;
    }
    /**
     * Return the parameters to use when sending the call.
     * The info contained in the SnmpMibRequest PDU are reused in order to
     * construct new SnmpParams.
     * The returned parameters are passed to the session default peer.
     * Overload this method in order to change the default policy.
     * @param request The received request.
     * @return The parameters to use when forwarding the call.
     */
    protected SnmpParams translateParameters(SnmpMibRequest request)
        throws SnmpStatusException {
        SnmpPdu pdu = request.getPdu();
        // Old Adaptor way. V1 and V2 only. Not sure that could happen.
        if(pdu == null) return new SnmpParameters("public", "private");

        SnmpParameters params = new SnmpParameters("public", "private");
        if(logger.finestOn())
            logger.finest("translateParameters", "From version : " +
                          pdu.version);
        switch(pdu.version) {
        case SnmpDefinitions.snmpVersionOne:
        case SnmpDefinitions.snmpVersionTwo: {
            SnmpPduPacket pack = (SnmpPduPacket) pdu;
            if(pack.community != null) {
                params.setRdCommunity(new String(pack.community));
                params.setWrCommunity(new String(pack.community));
            }
            break;
        }
        case SnmpDefinitions.snmpVersionThree: {

            //SnmpScopedPduPacket pack = (SnmpScopedPduPacket) pdu;
            //if(pack.contextName != null) {
            //params.setRdCommunity(new String(pack.contextName));
            //params.setWrCommunity(new String(pack.contextName));
            //}
            break;
        }
        default:
            if(logger.finerOn())
                logger.finer("translateParameters",
                      "Unsupported snmp protocol version :" + pdu.version);
        }
        return params;
    }

    /**
     * The get implementation. Forward the call to the passed peer using
     * the passed SnmpSession.
     * <P>Be aware that session and peer parameters are SnmpProxy attributes.
     * Modifying them leads to unpredictable behavior. This method should
     * not be overridden.</P>
     *
     * @param peer The SnmpPeer on which the call is forwarded
     * @param session The session used to forward the call. If the proxy
     *   translates from SNMP V2 to SNMP V1, you must provide a fixed session.
     * @param inRequest The SnmpMibRequest object holding the list of
     *            variable to be retrieved. This list is composed of
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException  An error occurred during the operation.
     */

    protected synchronized void get(SnmpPeer peer,
                                    SnmpSession session,
                                    SnmpMibRequest inRequest)
        throws SnmpStatusException {
        boolean retry = true;
        SnmpRequest request = null;
        SnmpVarBindList varbindlist = new
            SnmpVarBindList("SnmpProxy varbind list",
                            inRequest.getSubList());
        while(retry) {
            if(logger.finestOn())
                logger.finest("get", "get(SnmpPeer " + peer + ",\n" +
                      "SnmpSession " + session  + ",\n" +
                      "SnmpMibRequest " + inRequest + ")");

            request = session.snmpGetRequest(peer,
                                             null,
                                             varbindlist);

            if(logger.finestOn())
                logger.finest("get(session, request)",
                      "\nRequest:\n" + request.toString());

            boolean completed = request.waitForCompletion(getTimeout());

            if (completed == false) {
                if(logger.finestOn())
                    logger.finest("get(session, request)",
                                  "Request timed out.");

                throw new SnmpStatusException(SnmpDefinitions.snmpRspGenErr,0);
            }
            if(request.isReport()) {
                retry = handleReport(request);
                if(!retry)
                    throw new SnmpStatusException(
                              SnmpDefinitions.snmpRspGenErr);
                if(logger.finestOn())
                    logger.finest("get", "Retrying");
            }
            else
                retry = false;
        }

        int errorStatus = request.getErrorStatus();
        int errorIndex = request.getErrorIndex() + 1;
        if (errorStatus != SnmpDefinitions.snmpRspNoError) {

            if(logger.finestOn())
                logger.finest("get(session, request)", "Error." +
                      "\nError status :" + SnmpRequest.
                      snmpErrorToString(errorStatus) +
                      "\nError index :" + errorIndex);

            throw new SnmpStatusException(errorStatus, errorIndex);
        }

        // Now we move the results.
        if(session.snmpOptions.isPduFixedOnError()) // V2 manager to v1 agent
            moveFixResult(request, inRequest);
        else
            moveResult(request, inRequest);
    }

    /**
     * The getNext implementation. Forward the call to the passed peer using
     * the passed SnmpSession.
     * <P>Be aware that session and peer parameters are SnmpProxy attributes.
     * Modifying them leads to unpredictable behavior. This method should
     * not be overridden.</P>
     *
     * @param peer The SnmpPeer on which the call is forwarded
     * @param session The session used to forward the call. If the proxy
     *   translates from SNMP V2 to SNMP V1, you must provide a fixed session.
     * @param inRequest The SnmpMibRequest object holding the list of
     *            variable to be retrieved. This list is composed of
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException  An error occurred during the operation.
     */

    protected synchronized void getNext(SnmpPeer peer,
                                        SnmpSession session,
                                        SnmpMibRequest inRequest)
        throws SnmpStatusException {
        boolean retry = true;
        SnmpRequest request = null;
        SnmpVarBindList varbindlist = new
            SnmpVarBindList("SnmpProxy varbind list",
                            inRequest.getSubList());
        while(retry) {
            if(logger.finestOn())
                logger.finest("get", "getnext(SnmpPeer " + peer + ",\n" +
                      "SnmpSession " + session  + ",\n" +
                      "SnmpMibRequest " + inRequest + ")");

            request = session.snmpGetNextRequest(peer,
                                                 null,
                                                 varbindlist);
            boolean completed = request.waitForCompletion(getTimeout());

            if (completed == false) {
                if(!skipError) {
                    if(logger.finestOn())
                        logger.finest("get(session, request)",
                                      "Request timed out.");
                    throw
                        new SnmpStatusException(SnmpDefinitions.snmpRspGenErr,
                                                0);
                } else {
                    if(logger.finestOn())
                        logger.finest("getNext(session, request)",
                              "Skipping error : timeout.");
                    return;
                }
            }

            if(request.isReport()) {
                retry = handleReport(request);
                if(!retry)
                    throw new SnmpStatusException(
                              SnmpDefinitions.snmpRspGenErr);
                if(logger.finestOn())
                    logger.finest("get", "Retrying");
            }
            else
                retry = false;
        }

        int errorStatus = request.getErrorStatus();
        int errorIndex = request.getErrorIndex() + 1;
        if (errorStatus != SnmpDefinitions.snmpRspNoError) {

            if(skipError) {
                //Fix : 4673640 SnmpProxy should handle faulty sub agent
                //      in case of getnext
                if(logger.finestOn())
                    logger.finest("getNext(session, request)",
                                  "Skipping error : "
                                  + errorStatus);
                for(Enumeration l = inRequest.getElements();
                    l.hasMoreElements();) {
                    SnmpVarBind varbind = (SnmpVarBind) l.nextElement();
                    varbind.setSnmpValue(SnmpVarBind.endOfMibView);
                }
                return;
            }
            else {
                if(logger.finestOn())
                    logger.finest("getNext(session, request)", "Error." +
                          "\nError status :" + SnmpRequest.
                          snmpErrorToString(errorStatus) +
                          "\nError index :" + errorIndex);

                throw new SnmpStatusException(errorStatus, errorIndex);
            }
        }

        //Now we move the results.
        if(session.snmpOptions.isPduFixedOnError()) // V2 manager to v1 agent
            moveFixGetNextResult(request, inRequest);
        else
            moveGetNextResult(request, inRequest);
    }


    /**
     * The set implementation. Forward the call to the passed peer using
     * the passed SnmpSession.
     * <P>Be aware that session and peer parameters are SnmpProxy attributes.
     * Modifying them leads to unpredictable behavior. This method should
     * not be overridden.</P>
     *
     * @param peer The SnmpPeer on which the call is forwarded
     * @param session The session used to forward the call. If the proxy
     *   translates from SNMP V2 to SNMP V1, you must provide a fixed session.
     * @param inRequest The SnmpMibRequest object holding the list of
     *            variable to be retrieved. This list is composed of
     *            <CODE>SnmpVarBind</CODE> objects.
     *
     * @exception SnmpStatusException  An error occurred during the operation.
     */

    protected synchronized void set(SnmpPeer peer,
                                    SnmpSession session,
                                    SnmpMibRequest inRequest)
        throws SnmpStatusException
    {
        boolean retry = true;
        SnmpRequest request = null;
        SnmpVarBindList varbindlist = new
            SnmpVarBindList("SnmpProxy varbind list",
                            inRequest.getSubList());
        while(retry) {
            if(logger.finestOn())
                logger.finest("get", "getnext(SnmpPeer " + peer + ",\n" +
                      "SnmpSession " + session  + ",\n" +
                      "SnmpMibRequest " + inRequest + ")");

            request = session.snmpSetRequest(peer,
                                             null,
                                             varbindlist);

            boolean completed = request.waitForCompletion(getTimeout());

            if (completed == false) {
                if(logger.finestOn())
                    logger.finest("set(session, request)",
                                  "Request timed out.");

                throw new SnmpStatusException(SnmpDefinitions.snmpRspGenErr,
                                              0);
            }
            if(request.isReport()) {
                retry = handleReport(request);
                if(logger.finestOn())
                    logger.finest("get", "Retrying");
            }
            else
                retry = false;
        }

        int errorStatus = request.getErrorStatus();
        int errorIndex = request.getErrorIndex() + 1;
        if (errorStatus != SnmpDefinitions.snmpRspNoError) {

            if(logger.finestOn())
                logger.finest("set(session, request)", "Error." +
                      "\nError status :" + SnmpRequest.
                      snmpErrorToString(errorStatus) +
                      "\nError index :" + errorIndex);
            throw new SnmpStatusException(errorStatus, errorIndex);
        }

        // Now we move the results
        moveResult(request, inRequest);
    }



    // PRIVATE METHODS
    //================
    //

    private synchronized void syncSetTimeout(long t)
    {
        timeout = t;
    }

    private synchronized long syncGetTimeout()
    {
        return timeout;
    }

    /**
     * Private method used when dealing with any SNMP call in order to
     * find out which session to use and configure it with parameters.
     */
    private SnmpSession defineSession(SnmpMibRequest inRequest)
        throws SnmpStatusException {
        if(logger.finestOn())
            logger.finest("defineSession", inRequest.toString());
        final int vers = inRequest.getVersion();
        SnmpSession session = provider.getSession(vers);
        // getProxyParameters can be overidden in order to implement
        //custome call forwarding policy
        SnmpParams params = null;
        params = translateParameters(inRequest);
        if(params == null)
            throw new SnmpStatusException(
                  "Unable to define parameters to send to distant peer.");
        //Set these params to the peer.
        params.setProtocolVersion(session.getDefaultPeer().
                                  getParams().getProtocolVersion());
        session.getDefaultPeer().setParams(params);
        return session;
    }


    /**
     * Translate V2 exception in V1 error + index.
     */

    private void handleException(SnmpVarBind varres, int index)
        throws SnmpStatusException{

        int status = varres.status;
        if(status != SnmpVarBind.stValueOk) {
            if(status == SnmpDefinitions.snmpRspNoSuchName ||
               status == SnmpDefinitions.snmpRspBadValue ||
               status == SnmpDefinitions.snmpRspGenErr)
                throw new SnmpStatusException(status, index + 1);
            else {
                throw new SnmpStatusException(SnmpStatusException.noSuchName,
                                              index + 1);
            }
        }
    }

    /**
     * Throw an exception if the passed Varbind is a SnmpCounter64.
     */

    private void handleCounter64(SnmpVarBind varres, int index)
        throws SnmpStatusException {
        if(varres.getSnmpValue() instanceof SnmpCounter64) {
            //Handle error
            //
            throw new SnmpStatusException(SnmpStatusException.noSuchName,
                                          index);
        }
    }

    // Update the inRequest parameter.
    // The varbinds in the result list are expected to be in the same
    // order than in the request, so we can safely loop sequentially
    // over both lists.
    //
    private void moveResult(SnmpRequest request, SnmpMibRequest inRequest)
        throws SnmpStatusException {
        if(logger.finestOn())
            logger.finest("moveResult", "Result to move with no error");

        final SnmpVarBindList result = request.getResponseVarBindList();
        Enumeration l = inRequest.getElements();
        int index = 0;
        for (Enumeration e = result.elements(); e.hasMoreElements();) {

            SnmpVarBind varres = (SnmpVarBind) e.nextElement();
            SnmpVarBind varbind = (SnmpVarBind) l.nextElement();

            if(inRequest.getVersion() == SnmpDefinitions.snmpVersionOne) {
                // Case V1 manager
                handleException(varres, index);
                handleCounter64(varres, index);
            }
            varbind.setSnmpValue(varres.getSnmpValue());

            index +=1;

            if(logger.finestOn())
                logger.finest("moveResult", "\nVarResult : " +
                              varres.toString() +
                              "\nVarResult status: " + varres.status +
                              "\nVarBind : " + varbind.toString() +
                              "\nVarBind status: " + varbind.status);
        }
    }

    // Update the inRequest parameter.
    // The varbinds in the result list are expected to be in the same
    // order than in the request, so we can safely loop sequentially
    // over both lists.
    //
    private void moveFixResult(SnmpRequest request, SnmpMibRequest inRequest)
    {
        if(logger.finestOn())
            logger.finest("moveFixResult", "Result to move with errors");

        final SnmpVarBindList result = request.getResponseVarBindList();
        Enumeration l = inRequest.getElements();

        for (Enumeration e = result.elements(); e.hasMoreElements();) {
            SnmpVarBind varres = (SnmpVarBind) e.nextElement();
            SnmpVarBind varbind = (SnmpVarBind) l.nextElement();
            if( (varres.status == SnmpVarBind.stValueNoSuchInstance) ||
                (varres.status == SnmpVarBind.stValueNoSuchObject) )
                varbind.setSnmpValue(SnmpVarBind.noSuchInstance);
            else
                varbind.setSnmpValue(varres.getSnmpValue());

            if(logger.finestOn())
                logger.finest("moveResult", "\nVarResult : " +
                              varres.toString() +
                              "\nVarResult status: " + varres.status +
                              "\nVarBind : " + varbind.toString() +
                              "\nVarBind status: " + varbind.status);
        }
    }

    // Update the inRequest parameter.
    // The varbinds in the result list are expected to be in the same
    // order than in the request, so we can safely loop sequentially
    // over both lists. Since we parse the result of a GET-NEXT
    // request, we must also copy the value of the returned OIDs
    //
    private void moveGetNextResult(SnmpRequest request,
                                   SnmpMibRequest inRequest)
        throws SnmpStatusException {
        if(logger.finestOn())
            logger.finest("moveGetNextResult", "Result to move with no error");

        final SnmpVarBindList result = request.getResponseVarBindList();
        Enumeration l = inRequest.getElements();
        int index = 0;
        for (Enumeration e = result.elements(); e.hasMoreElements();) {
            SnmpVarBind varres = (SnmpVarBind) e.nextElement();
            SnmpVarBind varbind = (SnmpVarBind) l.nextElement();

            //GetNext is a every time V2, but in case...
            //
            if(inRequest.getVersion() == SnmpDefinitions.snmpVersionOne) {
                handleException(varres, index);
                handleCounter64(varres, index);
            }

            varbind.setSnmpValue(varres.getSnmpValue());
            varbind.setOid(varres.getOid(), false);

            index += 1;

            if(logger.finestOn())
                logger.finest("moveGetNextResult", "\nVarResult : " +
                      varres.toString() +
                      "\nVarResult status: " + varres.status +
                      "\nVarBind : " + varbind.toString() +
                      "\nVarBind status: " + varbind.status);
        }
    }

    // Update the inRequest parameter.
    // The varbinds in the result list are expected to be in the same
    // order than in the request, so we can safely loop sequentially
    // over both lists. Since we parse the result of a GET-NEXT
    // request, we must also copy the value of the returned OIDs
    //
    private void moveFixGetNextResult(SnmpRequest request,
                                      SnmpMibRequest inRequest) {
        if(logger.finestOn())
            logger.finest("moveFixGetNextResult",
                          "Result to move with errors");
        final SnmpVarBindList result = request.getResponseVarBindList();
        Enumeration l = inRequest.getElements();
        for (Enumeration e = result.elements(); e.hasMoreElements();) {
            SnmpVarBind varres = (SnmpVarBind) e.nextElement();
            SnmpVarBind varbind = (SnmpVarBind) l.nextElement();
            varbind.setOid(varres.getOid(), false);
            if(varres.status == SnmpVarBind.stValueEndOfMibView)
                varbind.setSnmpValue(SnmpVarBind.endOfMibView);
            else
                varbind.setSnmpValue(varres.getSnmpValue());

            if(logger.finestOn())
                logger.finest("moveResult", "\nVarResult : " +
                              varres.toString() +
                              "\nVarResult status: " + varres.status +
                              "\nVarBind : " + varbind.toString() +
                              "\nVarBind status: " + varbind.status);
        }
    }

    /**
     * Parse the string oid and return the corresponding root object
     * identifier.
     */
    private long[] resolveOidString(String s) {
        SnmpOid oid = new SnmpOid(s);
        return oid.longValue();
    }


    private static final ClassLogger logger =
        new ClassLogger(ClassLogger.LOGGER_PROXY_SNMP, "SnmpProxy");
    /**
     * The default timeout is 10 seconds.
     */
    public static final long defaultTimeout = 10000;

    // PRIVATE VARIABLES
    //=================
    transient private SnmpEngine engine = null;
    private SnmpPeer peer = null;
    private String dbgTag = "SnmpProxy";
    private String mibName = null;
    private String oid = null;
    private long timeout = defaultTimeout;
    private SnmpSessionProvider provider = null;
    private transient long[] rootOid = null;
    private boolean initialized = false;
    private boolean skipError = false;
    private boolean forwardOnCheck = false;
    private boolean hide = true;
}
