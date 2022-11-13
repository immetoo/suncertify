/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.net;

import java.io.IOException;

/**
 * The NetworkResponseHandler
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class NetworkResponseHandler {
	
	/** Stores the request */
	private NetworkRequest request = null;
	/** Stores the response */
	private NetworkResponse response = null;
	/** The network client */
	private NetworkClient client = null;

	/**
	 * Creates an NetworkResponseHandler 
	 * @param request	The request for which this response handler is.
	 * @param client	The client
	 */
	public NetworkResponseHandler(NetworkRequest request,NetworkClient client) {
		if (request==null) {
			throw new NullPointerException("Can't handle response with null request.");
		}
		if (client==null) {
			throw new NullPointerException("Can't handle response with null client.");
		}
		this.request=request;
		this.client=client;
	}

	/**
	 * Is called when is the response is ready.
	 * 
	 * @param response The NetworkResponse
	 */
	public void handleResponse(NetworkResponse response) {
		this.response=response;
		synchronized (this) {
			this.notify();
		}
	}

	/**
	 * Blocks for thread while waiting on respones.
	 * 
	 * @throws InterruptedException
	 */
	public void waitForResponse() throws InterruptedException {
		while(this.response == null) {
			synchronized (this) {
				this.wait(3000); // 3secs
			}
			if (response==null) {
				// we miss sometimes a result when working with 40+ threads client side.
				// lets resend and hopes its get returned this time.
				try {
					client.send(request,this);
				} catch (IOException e) {
					throw new InterruptedException("Error while resending missed request: "+e.getMessage());	
				}
				synchronized (this) {
					this.wait(3000);
				}
			}
			if (response==null) {
				throw new InterruptedException("no result of Id: "+request.getRequestId()+" in time, in thead: "+Thread.currentThread().getName());
			}
			if (response.getResult() instanceof Exception) {
				Exception e = (Exception)response.getResult();
				throw new RuntimeException("Server got error in request handling: "+e.getMessage(),e);
			}
		}
	}
	
	/**
	 * @return	Returns the NetworkRequest for which the response is active.
	 */
	public NetworkRequest getNetworkRequest() {
		return request;
	}
  
	/**
	 * @return	Returns the real result from the response.
	 */
	public Object getResult() {
		if (response==null) {
			throw new IllegalStateException("No response object, do not call getResult() before waitForResponse().");
		}
		return response.getResult();
	}
}