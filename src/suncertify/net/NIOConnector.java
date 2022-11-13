/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.net;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;

import suncertify.net.NIOChangeRequest.ChangeType;

/**
 * The NIOConnector is an abstract base class for creating NIO client/servers.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
abstract public class NIOConnector implements Runnable {
	
	/** The hostAddress to bind or connect to. */
	private InetAddress hostAddress;
	/** The port of the hostAddress */
	private int port;

	/** The selector we'll be monitoring */
	protected Selector selector;

	/** The buffer into which we'll read data when it's available */
	private ByteBuffer readBuffer = ByteBuffer.allocate(8192);

	/** A list of PendingChange instances */
	private List<NIOChangeRequest> pendingChanges = null;

	/** Maps a SocketChannel to a list of ByteBuffer instances */
	private Map<SocketChannel,List<ByteBuffer>> pendingData = null;

	/** The connection counter */
	final protected AtomicInteger connectsCounter = new AtomicInteger();

	/** Counter for total connection */
	volatile protected int totalConnections = 0;
	
	/** The logger to log to */
	private Logger logger = Logger.getLogger(NIOConnector.class.getName());

	/** Flag to stop thread */
	volatile protected boolean stop = false;
	
	/**
	 * Creates an abstract NIOConnector
	 * @param hostAddress
	 * @param port
	 * @throws IOException
	 */
	public NIOConnector(InetAddress hostAddress, int port) {
		this.hostAddress = hostAddress;
		this.port = port;
		pendingChanges = new LinkedList<NIOChangeRequest>();
		pendingData = new HashMap<SocketChannel,List<ByteBuffer>>();
	}

	abstract public void start() throws IOException;
	abstract public void stop() throws IOException;
	
	/**
	 * Sends data to the socket, appends it to the pendingData queu
	 * @param socket
	 * @param data
	 */
	protected void send(SocketChannel socket,ByteBuffer data) {
		// Indicate we want the interest ops set changed
		addChangeRequest(socket, NIOChangeRequest.ChangeType.CHANGEOPS, SelectionKey.OP_WRITE);

		// And queue the data we want written
		synchronized (pendingData) {
			List<ByteBuffer> queue = pendingData.get(socket);
			if (queue == null) {
				queue = new ArrayList<ByteBuffer>();
				pendingData.put(socket, queue);
			}
			queue.add(data);
		}

		// Finally, wake up our selecting thread so it can make the required changes
		selector.wakeup();
	}

	/**
	 * The NIO IO selector runnable
	 */
	public void run() {
		logger.info("NIO network thread started.");
		while (stop==false) {
			try {
				synchronized (pendingChanges) {
					int totalChanges = pendingChanges.size();
					if (totalChanges>0) {
						for (int i=0;i<totalChanges;i++) {
							NIOChangeRequest change = pendingChanges.get(i);
							if (logger.isLoggable(Level.FINE)) {
								logger.fine("Changeing "+change.ops+" from: "+change.socket+" type: "+change.type);
							}
							switch (change.type) {
								case CHANGEOPS:
									SelectionKey key = change.socket.keyFor(selector);
									if (logger.isLoggable(Level.FINER)) {
										logger.finer("Changeing "+change.ops+" key: "+key);
									}
									if (key!=null && key.isValid()) {
										key.interestOps(change.ops);
									}
									break;
								case REGISTER:
									change.socket.register(selector, change.ops);
									break;
							}
						} 
						pendingChanges.clear();
					}
				}

				// Wait for an event one of the registered channels
				selector.select();
    
				// Iterate over the set of keys for which events are available
				Iterator<SelectionKey> selectedKeys = selector.selectedKeys().iterator();
				while (selectedKeys.hasNext()) {
					SelectionKey key = selectedKeys.next();
					selectedKeys.remove();

					if (key.isValid()==false) {
						continue;
					}
      

					// check on session object
					Object o = key.attachment();
					//mm we can only set attech ment after key is accepted
					if (o==null & key.isAcceptable()==false) {
						o = new Integer(connectsCounter.getAndIncrement());
						key.attach(o);
						totalConnections++;
						logger.info("Init Connection: "+key.attachment()+" total: "+totalConnections);
					}
      
					// Check what event is available and deal with it
					try {
						  if (key.isConnectable()) {
							  connect(key);
						  } else if (key.isAcceptable()) {
							  accept(key);
						  } else if (key.isReadable()) {
							  read(key);
						  } else if (key.isWritable()) {
							  write(key);
						  } else {
							  throw new IOException("NIO error, key could not be processed.");
						  }
					} catch (IOException ioe) {
						Integer conNr = (Integer)key.attachment();
						if (ioe.getMessage()!=null && ioe.getMessage().contains("reset by peer")) {
							logger.log(Level.WARNING,"Connection: "+conNr+" has disconnected on error: "+ioe.getMessage());
						} else {
							logger.log(Level.WARNING,"Connection: "+conNr+" has disconnected on error: "+ioe.getMessage(),ioe);
						}
						key.cancel();
					}
					
					if (key.isValid()==false) {
						totalConnections--;
					}
				}
			} catch (Exception e) {
				logger.log(Level.WARNING,"Error in NIO Thread: "+e.getMessage(),e);
			}
		}
		
		// cleaning resources
		pendingChanges.clear();
		pendingData.clear();
		totalConnections = 0;
		logger.info("NIO network thread stoped.");
	}
	
	/**
	 * Is called when the key is connected, emty method.
	 * @param key
	 * @throws IOException
	 */
	protected void connect(SelectionKey key) throws IOException {
	}

	/**
	 * Is called when the key is accepted, emty method.
	 * @param key
	 * @throws IOException
	 */
	protected void accept(SelectionKey key) throws IOException {
	}

	/**
	 * Reads the data from the key and hands it over to processData
	 * @param key
	 * @throws IOException
	 */
	protected void read(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();

		// Clear out our read buffer so it's ready for new data
		readBuffer.clear();
		
		int numRead = socketChannel.read(readBuffer);
		if (numRead == -1) {
			key.cancel();
			return;
		}
		readBuffer.rewind();
		readBuffer.limit(numRead);
		processData(socketChannel, readBuffer); 
	}
	
	/**
	 * Processes the readed data.
	 * 
	 * @param socket
	 * @param readBuffer
	 * @throws IOException
	 */
	abstract void processData(SocketChannel socket, ByteBuffer readBuffer) throws IOException;

	/**
	 * Writes the data from the key to the pendingData queue
	 * @param key
	 * @throws IOException
	 */
	protected void write(SelectionKey key) throws IOException {
		SocketChannel socketChannel = (SocketChannel) key.channel();
		
		synchronized (pendingData) {
			List<ByteBuffer> queue = pendingData.get(socketChannel);
			// Write until there's not more data ...
			while (queue.isEmpty()==false) {
				ByteBuffer buf = queue.get(0);
				socketChannel.write(buf);
				if (buf.remaining() > 0) {
					break;  // ... or the socket's buffer fills up
				}
				queue.remove(0);
			}
			if (queue.isEmpty()) {
				// We wrote away all data, so we're no longer interested
				// in writing on this socket. Switch back to waiting for data.
				addChangeRequest(socketChannel, NIOChangeRequest.ChangeType.CHANGEOPS, SelectionKey.OP_READ);
			}
		}
	}

	/**
	 * Opens the default Selector.
	 * @return
	 * @throws IOException
	 */
	protected Selector initSelector() throws IOException {
		return SelectorProvider.provider().openSelector();
	}
	
	/**
	 * Adds an ChangeRequest for the socket
	 * @param socket
	 * @param type
	 * @param ops
	 */
	public void addChangeRequest(SocketChannel socket, ChangeType type, int ops) {
		synchronized (pendingChanges) {
			pendingChanges.add(new NIOChangeRequest(socket, type, ops));
		}
	}

	/**
	 * @return the hostAddress
	 */
	public InetAddress getHostAddress() {
		return hostAddress;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}	
}