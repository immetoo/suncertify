/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.net;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * The NetworkRequest is the object sent to the server for invoking a method on a bean.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class NetworkRequest implements Externalizable {
		
	/** The request ID, should be an uniq number */
	private Integer requestId = null;
	
	/** The beanName we do the request on. */
	private String beanName = null;
	
	/** The methodName of the bean to call. */
	private String methodName = null;
	
	/** The argruments of the method name. */
	private Object[] methodArgs = null;
	
	public NetworkRequest() {
	}
	
	/**
	 * Creates an NetworkRequest with an given requestId.
	 * @param requestId	The requestId of the response.
	 */
	public NetworkRequest(Integer requestId) {
		if (requestId==null) {
			throw new NullPointerException("can't handle null requestID.");
		}
		this.requestId=requestId;
	}
	
	/**
	 * Sets and check all info for request.
	 * @param beanName
	 * @param methodName
	 * @param methodArgs
	 */
	public void setRequestBean(String beanName,String methodName,Object[] methodArgs) {
		if (beanName==null) {
			throw new NullPointerException("Can't request null beanName.");
		}
		if (beanName.isEmpty()) {
			throw new IllegalArgumentException("Can't request empty beanName.");
		}
		if (methodName==null) {
			throw new NullPointerException("Can't request null methodName");
		}
		if (methodName.isEmpty()) {
			throw new NullPointerException("Can't request empty methodName");
		}
		this.beanName=beanName;
		this.methodName=methodName;
		this.methodArgs=methodArgs;
	}
	
	/**
	 * Returns the request id.
	 * @return	The requestId
	 */
	public Integer getRequestId() {
		return requestId;
	}
	
	/**
	 * Returns the beanName
	 * @return	The beanName to request
	 */
	public String getBeanName() {
		return beanName;
	}
	
	/**
	 * Returns the methodName
	 * @return The methodName to request
	 */
	public String getMethodName() {
		return methodName;
	}
	
	/**
	 * Returns the methodArgs
	 * @return The methodArgs to invoke on bean.
	 */
	public Object[] getMethodArgs() {
		return methodArgs;
	}
	
	/**
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		requestId		= in.readInt();
		beanName		= in.readUTF();
		methodName		= in.readUTF();
		int argsCount	= in.readInt();
		if (argsCount==0) {
			return;
		}
		methodArgs = new Object[argsCount];
		for (int i=0;i<argsCount;i++) {
			methodArgs[i]= in.readObject();
		}
	}

	/**
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt		(requestId);
		out.writeUTF		(beanName);
		out.writeUTF		(methodName);
		if (methodArgs==null) {
			out.writeInt(0);
			return;
		}
		out.writeInt(methodArgs.length);
		for (int i=0;i<methodArgs.length;i++) {
			out.writeObject(methodArgs[i]);
		}
	}
}