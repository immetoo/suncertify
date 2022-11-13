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
 * The NetworkResponse  is the object response sent by the server to the client after the request has been processed.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class NetworkResponse implements Externalizable {

	/** The request ID for which this response is created. */
	private Integer requestId = null;
	/** The object of the resonse to return */
	private Object result = null;
	
	public NetworkResponse() {
	}
	
	/**
	 * Creates an NetworkResponse with an given requestId.
	 * @param requestId	The requestId of the response.
	 */
	public NetworkResponse(Integer requestId) {
		if (requestId==null) {
			throw new NullPointerException("can't handle null requestID.");
		}
		this.requestId=requestId;
	}
	
	/**
	 * Returns the request id.
	 * @return	The requestId
	 */
	public Integer getRequestId() {
		return requestId;
	}
	
	/**
	 * Returns the Result
	 * @return	Returns the result.
	 */
	public Object getResult() {
		return result;
	}
	
	/**
	 * Set the result.
	 * @param result	The result to response.
	 */
	public void setResult(Object result) {
		this.result=result;
	}
	
	/**
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		requestId		= in.readInt();
		result			= in.readObject();
	}

	/**
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt		(requestId);
		out.writeObject		(result);
	}
}