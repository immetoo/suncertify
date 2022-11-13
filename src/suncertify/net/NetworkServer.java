/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */


package suncertify.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

/**
 * The NetworkServer provides support to executing beans by remote requests on the serverBackendProvider
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class NetworkServer extends NetworkNIOConnector {
	
	private ServerBackendProvider serverBackendProvider = null;
	
	private Logger logger = Logger.getLogger(NetworkServer.class.getName());;
  
	public NetworkServer(InetAddress hostAddress, int port, ServerBackendProvider serverBackendProvider) throws IOException {
		super(hostAddress,port);
		this.serverBackendProvider = serverBackendProvider;				
	}

	/**
	 * Send the response back to the client.
	 * @param socket
	 * @param response
	 * @throws IOException
	 */
	public void send(SocketChannel socket,NetworkResponse response) throws IOException {
		sendObject(socket,response);
		logger.fine("SEND requestId: "+response.getRequestId());
	}
	
	/**
	 * Accepts the key and makes the socket non-blocking
	 */
	@Override
	protected void accept(SelectionKey key) throws IOException {
	  
		// For an accept to be pending the channel must be a server socket channel.
		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
    
		// Accept the connection and make it non-blocking
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);

		// Register the new SocketChannel with our Selector, indicating
		// we'd like to be notified when there's data waiting to be read
		socketChannel.register(key.selector(), SelectionKey.OP_READ);     
	}

	/**
	 * Creates the Server socket non-blocking
	 */
	@Override
	protected Selector initSelector() throws IOException {
		Selector socketSelector = super.initSelector();

		// Create a new non-blocking server socket channel
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
    
		// Bind the server socket to the specified address and port
		InetSocketAddress isa = new InetSocketAddress(getHostAddress(), getPort());
		serverChannel.socket().bind(isa);

		// Register the server socket channel, indicating an interest in 
		// accepting new connections
		serverChannel.register(socketSelector, SelectionKey.OP_ACCEPT);
		return socketSelector;
	}	

	/**
	 * Process the data in the ServerBackendProvider
	 */
	@Override
	protected void processData(SocketChannel socket,ByteBuffer readBuffer) throws IOException {
		
		// copy data
		byte[] data = new byte[readBuffer.remaining()];
		readBuffer.get(data);
        
		// give to handerl
		Runnable worker = serverBackendProvider.dataWorker(this,socket,data);
		serverBackendProvider.executeWorker(worker);
	}
	
	
	public interface ServerBackendProvider {
		public Runnable dataWorker(NetworkServer server,SocketChannel socket,byte[] data);
		public void executeWorker(Runnable runable);
	}
}
