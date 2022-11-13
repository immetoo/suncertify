/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The NetworkClient
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class NetworkClient extends NetworkNIOConnector {
	
	/** Maps a SocketChannel to a RspHandler */
	private Map<Integer,NetworkResponseHandler> responseHandlers = null;
	/** Keeps track of the request IDs. */
	private AtomicInteger requestIds = new AtomicInteger(0);
	/** The sockets wo connect to */
	private SocketChannel socket = null;
	/** The logger to log to. */
	private Logger logger = Logger.getLogger(NetworkClient.class.getName());
	/** The left over buffer, used to fix multi threaded requests. */
	private byte[] leftBuf = null;
	
	/**
	 * Creates an NetworkClient.
	 * 
	 * @param hostAddress
	 * @param port
	 * @throws IOException
	 */
	public NetworkClient(InetAddress hostAddress, int port) throws IOException {
		super(hostAddress,port);
		responseHandlers = Collections.synchronizedMap(new HashMap<Integer,NetworkResponseHandler>(100));
	}
	
	/**
	 * We override stop to clean our private resources here
	 * @see suncertify.net.NetworkNIOConnector#stop()
	 */
	@Override
	public void stop() throws IOException {
		try {
			super.stop();
		} finally {
			leftBuf = null;
			try {
				if (socket!=null) {  // start,stop when nothing happend, then socket is still null.
					socket.close();
				}
			} finally {
				responseHandlers.clear();
			}
		}
	}

	/**
	 * Sends an NetworkRequest to the server.
	 * @param request
	 * @param handler
	 * @throws IOException
	 */
	public void send(NetworkRequest request, NetworkResponseHandler handler) throws IOException {
		// We connect on first send
		if (socket==null) {
			socket = initiateConnection();
		}
		// Register the response handler
		responseHandlers.put(handler.getNetworkRequest().getRequestId(), handler);
		
		// send object to socket
		sendObject(socket,request);
	}

	/**
	 * Process data from the server.
	 */
	@Override
	protected void processData(SocketChannel socket,ByteBuffer readBuffer) throws IOException {
		
		// we should make sure the happens less often or not at all, because the allocate is an heavy function.
		if (leftBuf!=null) {
			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Fixing data: appendSize: "+leftBuf.length+" buf: "+readBuffer.remaining());
			}
			
			ByteBuffer buffer = ByteBuffer.allocate(8192+leftBuf.length+readBuffer.remaining());
			buffer.limit(leftBuf.length+readBuffer.remaining());
			
			buffer.put(leftBuf);
			buffer.put(readBuffer);
			
			buffer.rewind();
			readBuffer.clear();
			readBuffer = buffer;
			leftBuf = null;
    	}
		
		ByteBuffer leftOver = receiveObject(socket,readBuffer, new ObjectHandler() {
			public void processObject(Object object) {
		    	NetworkResponse response = (NetworkResponse)object;
		    	NetworkResponseHandler handler = responseHandlers.remove(response.getRequestId());
		        if (handler==null) {
		        	logger.warning("Error no handler for read data of request "+response.getRequestId());
		        	return;
		        }		        
		        // And pass the response to it
		        handler.handleResponse(response);
			}
		},0);
		if (leftOver==null) {
			return; // all oke
		}
		
		// save left over for when next bytes come in.
		byte[] data2 = new byte[leftOver.remaining()];
        leftOver.get(data2);
        leftBuf = data2;
        
        if (logger.isLoggable(Level.FINE)) {
        	ByteArrayInputStream in = new ByteArrayInputStream(leftBuf);
        	ObjectInputStream objstream = new ObjectInputStream(in);
        	int objectSize = objstream.readInt();
        	objstream.close();
        
        	logger.fine("Pasting LeftOver size: "+data2.length+" Next object size: "+objectSize);
        }
	}

	/**
	 * Creates our connection to the server.
	 * @return
	 * @throws IOException
	 */
	private SocketChannel initiateConnection() throws IOException {
		// Create nonblocking socket channel
		SocketChannel socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
  
		// Connect to server
		socketChannel.connect(new InetSocketAddress(getHostAddress(),getPort()));	  
		socketChannel.finishConnect();
    
		// select register on selector in nio thread
		addChangeRequest(socketChannel, NIOChangeRequest.ChangeType.REGISTER, SelectionKey.OP_CONNECT);      
		return socketChannel;
	}

	/**
	 * Returns an InvocationHandler for creating proxed objects.
	 * @param obj
	 * @return
	 */
	public InvocationHandler getInvocationHandler(Class<?> obj) {
		return new NetworkInvocationHandler(obj,this);
	}
	
	/**
	 * InvocationHandler for handeling the method calls of the proxy bean.
	 *
	 */
	private class NetworkInvocationHandler implements InvocationHandler {
		private String beanName = null;
		private NetworkClient client = null;
	  
		public NetworkInvocationHandler(Class<?> obj,NetworkClient client) {
			beanName = obj.getName();
			this.client=client;
		}
	  
		public Object invoke(Object proxy, Method method,Object[] args) throws Throwable {
			// create request and fill it.
			NetworkRequest request = new NetworkRequest(requestIds.getAndIncrement());
			request.setRequestBean(beanName, method.getName(), args);
			// Create handler for when we get response.
			NetworkResponseHandler handler = new NetworkResponseHandler(request,client);
			// send and wait..
			client.send(request, handler);
			handler.waitForResponse();
			// we have it.
			Object result = handler.getResult();
			return result;
		}
	}
}