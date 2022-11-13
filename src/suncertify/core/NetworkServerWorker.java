/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */


package suncertify.core;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import suncertify.net.NetworkRequest;
import suncertify.net.NetworkResponse;
import suncertify.net.NetworkServer;
import suncertify.net.NetworkNIOConnector.ObjectHandler;
import suncertify.server.ServerManager;

/**
 * The NetworkServerWorker
 * 
 * This Runnable worker handlers the data from the nio server and decode it.
 * Then it will invoke the bean methode and return the result to the nio server.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */

public class NetworkServerWorker implements Runnable {

	private Logger logger = Logger.getLogger(NetworkServerWorker.class.getName());
	NetworkServer server = null;
	SocketChannel socket = null;
	private ServerManager serverManager = null;
	private ByteBuffer data = ByteBuffer.allocate(8192);
	
	/**
	 * Creates an new NetworkServerWorker which can handle requests from the server and executes them on the serverManager.
	 * @param serverManager
	 * @param server
	 * @param socket
	 * @param data
	 */
	public NetworkServerWorker(ServerManager serverManager,NetworkServer server,SocketChannel socket,byte[] data) {
		this.socket=socket;
		this.server=server;
		this.data.put(data);
		this.data.limit(data.length);
		this.serverManager=serverManager;
	}
	
	/**
	 * Gets an chuck of data from server and process an response to it.
	 */
	public void run() {
		String tName = Thread.currentThread().getName();
		long startTime = System.currentTimeMillis();
		logger.fine("Starting exec in "+tName);
		try {
			ByteBuffer leftOver = server.receiveObject(socket,data, new ObjectHandler() {
				public void processObject(Object object) {
					NetworkRequest request = (NetworkRequest)object;
					NetworkResponse response = new NetworkResponse(request.getRequestId());
					try {
						Object bean = serverManager.getServerBean(request.getBeanName());
						
				    	int pS = 0;
				    	if (request.getMethodArgs()!=null) {
				    		pS = request.getMethodArgs().length;
				    	}
				    	Class<?>[] para = new Class[pS];
				    	for (int i=0;i<pS;i++) {
				    		para[i] = request.getMethodArgs()[i].getClass();
				    	}
				    	Method mm = bean.getClass().getMethod(request.getMethodName(), para);
				    	Object resultObject = mm.invoke(bean,request.getMethodArgs());
				    	
				    	response.setResult(resultObject);
				    	server.send(socket,response);
				    	
					} catch (Exception e) {
						response.setResult(e);
						e.printStackTrace();
						try {
							server.send(socket,response);
						} catch (Exception ee) {
							logger.log(Level.WARNING,"Error while sending back request: "+e.getMessage(),e);
						}
					}
				}
			},0);
			if (leftOver==null) {
				return; // all oke
			} else {
				// should/will not happen in server mode ... but anyway
				throw new RuntimeException("Server can't handle leftOver data.");
			}
		} catch (Exception e) {
			logger.log(Level.WARNING,"Error while handling request: "+e.getMessage(),e);
		} finally {
			long stopTime = System.currentTimeMillis();
			logger.fine("Helped client"+tName+" in "+(stopTime-startTime)+" ms.");
		}
	}
}