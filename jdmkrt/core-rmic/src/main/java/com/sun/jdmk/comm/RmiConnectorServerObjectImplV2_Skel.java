/*
 * @(#)RmiConnectorServerObjectImplV2_Skel.java	1.5
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
// Skeleton class generated by rmic, do not edit.
// Contents subject to change without notice.

package com.sun.jdmk.comm;

public final class RmiConnectorServerObjectImplV2_Skel
    implements java.rmi.server.Skeleton
{
    private static final java.rmi.server.Operation[] operations = {
	new java.rmi.server.Operation("javax.management.ObjectInstance createMBean(java.lang.String, javax.management.ObjectName, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("javax.management.ObjectInstance createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("javax.management.ObjectInstance createMBean(java.lang.String, javax.management.ObjectName, javax.management.ObjectName, java.lang.Object[], java.lang.String[], com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("javax.management.ObjectInstance createMBean(java.lang.String, javax.management.ObjectName, java.lang.Object[], java.lang.String[], com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("java.lang.Object getAttribute(javax.management.ObjectName, java.lang.String, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("javax.management.AttributeList getAttributes(javax.management.ObjectName, java.lang.String[], com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("java.lang.String getDefaultDomain(com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("java.lang.Integer getMBeanCount(com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("javax.management.MBeanInfo getMBeanInfo(javax.management.ObjectName, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("javax.management.ObjectInstance getObjectInstance(javax.management.ObjectName, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("java.lang.Object invoke(javax.management.ObjectName, java.lang.String, java.lang.Object[], java.lang.String[], com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("boolean isInstanceOf(javax.management.ObjectName, java.lang.String, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("boolean isRegistered(javax.management.ObjectName, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("java.lang.String pingHeartBeatServer(java.lang.String, int, int, java.lang.Long, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("java.util.Set queryMBeans(javax.management.ObjectName, javax.management.QueryExp, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("java.util.Set queryNames(javax.management.ObjectName, javax.management.QueryExp, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("java.lang.Object remoteRequest(int, java.lang.Object[], com.sun.jdmk.OperationContext)[]"),
	new java.rmi.server.Operation("void setAttribute(javax.management.ObjectName, javax.management.Attribute, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("javax.management.AttributeList setAttributes(javax.management.ObjectName, javax.management.AttributeList, com.sun.jdmk.OperationContext)"),
	new java.rmi.server.Operation("void unregisterMBean(javax.management.ObjectName, com.sun.jdmk.OperationContext)")
    };
    
    private static final long interfaceHash = 2810310541300557269L;
    
    public java.rmi.server.Operation[] getOperations() {
	return (java.rmi.server.Operation[]) operations.clone();
    }
    
    public void dispatch(java.rmi.Remote obj, java.rmi.server.RemoteCall call, int opnum, long hash)
	throws java.lang.Exception
    {
	if (opnum < 0) {
	    if (hash == 2440010322598766993L) {
		opnum = 0;
	    } else if (hash == 2169275250666070156L) {
		opnum = 1;
	    } else if (hash == 5370333889440009947L) {
		opnum = 2;
	    } else if (hash == 977318789919612498L) {
		opnum = 3;
	    } else if (hash == -410295849957453740L) {
		opnum = 4;
	    } else if (hash == 3099299699664743513L) {
		opnum = 5;
	    } else if (hash == 512409007936098290L) {
		opnum = 6;
	    } else if (hash == -3425292753612071307L) {
		opnum = 7;
	    } else if (hash == 1921649590489468780L) {
		opnum = 8;
	    } else if (hash == 4858459067333490289L) {
		opnum = 9;
	    } else if (hash == 6940807516347053750L) {
		opnum = 10;
	    } else if (hash == 260369099243055722L) {
		opnum = 11;
	    } else if (hash == -7609211420371661371L) {
		opnum = 12;
	    } else if (hash == 436319371904843320L) {
		opnum = 13;
	    } else if (hash == 3187052459013828740L) {
		opnum = 14;
	    } else if (hash == 7107001389539938448L) {
		opnum = 15;
	    } else if (hash == -2341243041480682483L) {
		opnum = 16;
	    } else if (hash == 771897399327456434L) {
		opnum = 17;
	    } else if (hash == -1801622868570705949L) {
		opnum = 18;
	    } else if (hash == 1828313826367654983L) {
		opnum = 19;
	    } else {
		throw new java.rmi.UnmarshalException("invalid method hash");
	    }
	} else {
	    if (hash != interfaceHash)
		throw new java.rmi.server.SkeletonMismatchException("interface hash mismatch");
	}
	
	com.sun.jdmk.comm.RmiConnectorServerObjectImplV2 server = (com.sun.jdmk.comm.RmiConnectorServerObjectImplV2) obj;
	switch (opnum) {
	case 0: // createMBean(String, ObjectName, OperationContext)
	{
	    java.lang.String $param_String_1;
	    javax.management.ObjectName $param_ObjectName_2;
	    com.sun.jdmk.OperationContext $param_OperationContext_3;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_String_1 = (java.lang.String) in.readObject();
		$param_ObjectName_2 = (javax.management.ObjectName) in.readObject();
		$param_OperationContext_3 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    javax.management.ObjectInstance $result = server.createMBean($param_String_1, $param_ObjectName_2, $param_OperationContext_3);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 1: // createMBean(String, ObjectName, ObjectName, OperationContext)
	{
	    java.lang.String $param_String_1;
	    javax.management.ObjectName $param_ObjectName_2;
	    javax.management.ObjectName $param_ObjectName_3;
	    com.sun.jdmk.OperationContext $param_OperationContext_4;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_String_1 = (java.lang.String) in.readObject();
		$param_ObjectName_2 = (javax.management.ObjectName) in.readObject();
		$param_ObjectName_3 = (javax.management.ObjectName) in.readObject();
		$param_OperationContext_4 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    javax.management.ObjectInstance $result = server.createMBean($param_String_1, $param_ObjectName_2, $param_ObjectName_3, $param_OperationContext_4);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 2: // createMBean(String, ObjectName, ObjectName, Object[], String[], OperationContext)
	{
	    java.lang.String $param_String_1;
	    javax.management.ObjectName $param_ObjectName_2;
	    javax.management.ObjectName $param_ObjectName_3;
	    java.lang.Object[] $param_arrayOf_Object_4;
	    java.lang.String[] $param_arrayOf_String_5;
	    com.sun.jdmk.OperationContext $param_OperationContext_6;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_String_1 = (java.lang.String) in.readObject();
		$param_ObjectName_2 = (javax.management.ObjectName) in.readObject();
		$param_ObjectName_3 = (javax.management.ObjectName) in.readObject();
		$param_arrayOf_Object_4 = (java.lang.Object[]) in.readObject();
		$param_arrayOf_String_5 = (java.lang.String[]) in.readObject();
		$param_OperationContext_6 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    javax.management.ObjectInstance $result = server.createMBean($param_String_1, $param_ObjectName_2, $param_ObjectName_3, $param_arrayOf_Object_4, $param_arrayOf_String_5, $param_OperationContext_6);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 3: // createMBean(String, ObjectName, Object[], String[], OperationContext)
	{
	    java.lang.String $param_String_1;
	    javax.management.ObjectName $param_ObjectName_2;
	    java.lang.Object[] $param_arrayOf_Object_3;
	    java.lang.String[] $param_arrayOf_String_4;
	    com.sun.jdmk.OperationContext $param_OperationContext_5;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_String_1 = (java.lang.String) in.readObject();
		$param_ObjectName_2 = (javax.management.ObjectName) in.readObject();
		$param_arrayOf_Object_3 = (java.lang.Object[]) in.readObject();
		$param_arrayOf_String_4 = (java.lang.String[]) in.readObject();
		$param_OperationContext_5 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    javax.management.ObjectInstance $result = server.createMBean($param_String_1, $param_ObjectName_2, $param_arrayOf_Object_3, $param_arrayOf_String_4, $param_OperationContext_5);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 4: // getAttribute(ObjectName, String, OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    java.lang.String $param_String_2;
	    com.sun.jdmk.OperationContext $param_OperationContext_3;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_String_2 = (java.lang.String) in.readObject();
		$param_OperationContext_3 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    java.lang.Object $result = server.getAttribute($param_ObjectName_1, $param_String_2, $param_OperationContext_3);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 5: // getAttributes(ObjectName, String[], OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    java.lang.String[] $param_arrayOf_String_2;
	    com.sun.jdmk.OperationContext $param_OperationContext_3;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_arrayOf_String_2 = (java.lang.String[]) in.readObject();
		$param_OperationContext_3 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    javax.management.AttributeList $result = server.getAttributes($param_ObjectName_1, $param_arrayOf_String_2, $param_OperationContext_3);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 6: // getDefaultDomain(OperationContext)
	{
	    com.sun.jdmk.OperationContext $param_OperationContext_1;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_OperationContext_1 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    java.lang.String $result = server.getDefaultDomain($param_OperationContext_1);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 7: // getMBeanCount(OperationContext)
	{
	    com.sun.jdmk.OperationContext $param_OperationContext_1;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_OperationContext_1 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    java.lang.Integer $result = server.getMBeanCount($param_OperationContext_1);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 8: // getMBeanInfo(ObjectName, OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    com.sun.jdmk.OperationContext $param_OperationContext_2;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_OperationContext_2 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    javax.management.MBeanInfo $result = server.getMBeanInfo($param_ObjectName_1, $param_OperationContext_2);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 9: // getObjectInstance(ObjectName, OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    com.sun.jdmk.OperationContext $param_OperationContext_2;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_OperationContext_2 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    javax.management.ObjectInstance $result = server.getObjectInstance($param_ObjectName_1, $param_OperationContext_2);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 10: // invoke(ObjectName, String, Object[], String[], OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    java.lang.String $param_String_2;
	    java.lang.Object[] $param_arrayOf_Object_3;
	    java.lang.String[] $param_arrayOf_String_4;
	    com.sun.jdmk.OperationContext $param_OperationContext_5;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_String_2 = (java.lang.String) in.readObject();
		$param_arrayOf_Object_3 = (java.lang.Object[]) in.readObject();
		$param_arrayOf_String_4 = (java.lang.String[]) in.readObject();
		$param_OperationContext_5 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    java.lang.Object $result = server.invoke($param_ObjectName_1, $param_String_2, $param_arrayOf_Object_3, $param_arrayOf_String_4, $param_OperationContext_5);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 11: // isInstanceOf(ObjectName, String, OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    java.lang.String $param_String_2;
	    com.sun.jdmk.OperationContext $param_OperationContext_3;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_String_2 = (java.lang.String) in.readObject();
		$param_OperationContext_3 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    boolean $result = server.isInstanceOf($param_ObjectName_1, $param_String_2, $param_OperationContext_3);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeBoolean($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 12: // isRegistered(ObjectName, OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    com.sun.jdmk.OperationContext $param_OperationContext_2;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_OperationContext_2 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    boolean $result = server.isRegistered($param_ObjectName_1, $param_OperationContext_2);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeBoolean($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 13: // pingHeartBeatServer(String, int, int, Long, OperationContext)
	{
	    java.lang.String $param_String_1;
	    int $param_int_2;
	    int $param_int_3;
	    java.lang.Long $param_Long_4;
	    com.sun.jdmk.OperationContext $param_OperationContext_5;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_String_1 = (java.lang.String) in.readObject();
		$param_int_2 = in.readInt();
		$param_int_3 = in.readInt();
		$param_Long_4 = (java.lang.Long) in.readObject();
		$param_OperationContext_5 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    java.lang.String $result = server.pingHeartBeatServer($param_String_1, $param_int_2, $param_int_3, $param_Long_4, $param_OperationContext_5);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 14: // queryMBeans(ObjectName, QueryExp, OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    javax.management.QueryExp $param_QueryExp_2;
	    com.sun.jdmk.OperationContext $param_OperationContext_3;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_QueryExp_2 = (javax.management.QueryExp) in.readObject();
		$param_OperationContext_3 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    java.util.Set $result = server.queryMBeans($param_ObjectName_1, $param_QueryExp_2, $param_OperationContext_3);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 15: // queryNames(ObjectName, QueryExp, OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    javax.management.QueryExp $param_QueryExp_2;
	    com.sun.jdmk.OperationContext $param_OperationContext_3;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_QueryExp_2 = (javax.management.QueryExp) in.readObject();
		$param_OperationContext_3 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    java.util.Set $result = server.queryNames($param_ObjectName_1, $param_QueryExp_2, $param_OperationContext_3);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 16: // remoteRequest(int, Object[], OperationContext)
	{
	    int $param_int_1;
	    java.lang.Object[] $param_arrayOf_Object_2;
	    com.sun.jdmk.OperationContext $param_OperationContext_3;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_int_1 = in.readInt();
		$param_arrayOf_Object_2 = (java.lang.Object[]) in.readObject();
		$param_OperationContext_3 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    java.lang.Object[] $result = server.remoteRequest($param_int_1, $param_arrayOf_Object_2, $param_OperationContext_3);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 17: // setAttribute(ObjectName, Attribute, OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    javax.management.Attribute $param_Attribute_2;
	    com.sun.jdmk.OperationContext $param_OperationContext_3;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_Attribute_2 = (javax.management.Attribute) in.readObject();
		$param_OperationContext_3 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    server.setAttribute($param_ObjectName_1, $param_Attribute_2, $param_OperationContext_3);
	    try {
		call.getResultStream(true);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 18: // setAttributes(ObjectName, AttributeList, OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    javax.management.AttributeList $param_AttributeList_2;
	    com.sun.jdmk.OperationContext $param_OperationContext_3;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_AttributeList_2 = (javax.management.AttributeList) in.readObject();
		$param_OperationContext_3 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    javax.management.AttributeList $result = server.setAttributes($param_ObjectName_1, $param_AttributeList_2, $param_OperationContext_3);
	    try {
		java.io.ObjectOutput out = call.getResultStream(true);
		out.writeObject($result);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	case 19: // unregisterMBean(ObjectName, OperationContext)
	{
	    javax.management.ObjectName $param_ObjectName_1;
	    com.sun.jdmk.OperationContext $param_OperationContext_2;
	    try {
		java.io.ObjectInput in = call.getInputStream();
		$param_ObjectName_1 = (javax.management.ObjectName) in.readObject();
		$param_OperationContext_2 = (com.sun.jdmk.OperationContext) in.readObject();
	    } catch (java.io.IOException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } catch (java.lang.ClassNotFoundException e) {
		throw new java.rmi.UnmarshalException("error unmarshalling arguments", e);
	    } finally {
		call.releaseInputStream();
	    }
	    server.unregisterMBean($param_ObjectName_1, $param_OperationContext_2);
	    try {
		call.getResultStream(true);
	    } catch (java.io.IOException e) {
		throw new java.rmi.MarshalException("error marshalling return", e);
	    }
	    break;
	}
	    
	default:
	    throw new java.rmi.UnmarshalException("invalid method number");
	}
    }
}