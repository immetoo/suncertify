/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.net;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * The NetworkNIOConnector for creating object based serialezed nio client/servers
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
abstract public class NetworkNIOConnector extends NIOConnector {
	
	/** The logger to log to. */
	private Logger logger = Logger.getLogger(NetworkNIOConnector.class.getName());
	
	/** Counter for send Objects */
	volatile private int sendObjectCounter = 0;
	
	/** indicating the milisecond value for 1min.. */
	static final private long ONCE_PER_MINUTE = 1000*60;
	
	/** Timer object to print stats. */
	private Timer statsTimer = null;
	
	/**
	 * Creates an NetworkNIOConnector.
	 * 
	 * @param hostAddress
	 * @param port
	 * @throws IOException
	 */
	public NetworkNIOConnector(InetAddress hostAddress, int port) throws IOException {
		super(hostAddress,port);
	}
	
	/**
	 * Starts this Connector by creating the selector
	 * and starts the backend nio thread.
	 * Creates an Timer for printing the stats of this connector.
	 */
	@Override
	public void start() throws IOException {
		// create selectort
		selector = initSelector();
		
		// Start the backend thread
		Thread t = new Thread(this,"net-nio");
		t.setDaemon(true);
		t.start();
		
		// start the stats timers
		statsTimer = new Timer("net-stats");
		statsTimer.scheduleAtFixedRate(new TimerTask() {
			  public void run() {
				  try {
					  logger.info("sendObject() Statistics"+
							  " TPM: "+sendObjectCounter+
							  " TPS: "+(sendObjectCounter/60)+
							  " totalEstablisedConnections: "+totalConnections+
							  " totalHandledConnections: "+connectsCounter.get());
				  } finally {
					  sendObjectCounter=0;
				  }
			  }
		}, 0, ONCE_PER_MINUTE);
		
		// reset stats counters
		sendObjectCounter=0;
	}
	
	/**
	 * Stops the connector and backend threads.
	 * cleaning all resources and closing the connections/selector.
	 */
	@Override
	public void stop() throws IOException {
		if (stop) {
			throw new IllegalStateException("Can't stop already stopped connector.");
		}
		stop = true; // note: thread stops his resources (and selector)
		try {
			selector.wakeup();
		} finally {
			try {
				statsTimer.cancel();
			} finally {
				
				// simple stop check
				int max=10;
				while (totalConnections!=0 & max!=0) {
					try {
						max--;
						Thread.sleep(200);
					} catch (InterruptedException e) {
					}
				}
				selector.close();
			}
		}
	}
	
	public void sendObject(SocketChannel socket,Object object) throws IOException {
		
		// write request		
		ByteArrayOutputStream dataArray = new ByteArrayOutputStream(256); // it grows when needed
		ObjectOutputStream objOutstream = new ObjectOutputStream(dataArray);
		objOutstream.writeObject(object);
		objOutstream.close();
		  
		int objectSize = dataArray.size();
		
		// write request size
		ByteArrayOutputStream dataArray2 = new ByteArrayOutputStream(objectSize+8);
		ObjectOutputStream objOutstream2 = new ObjectOutputStream(dataArray2); // todo: remove this new statement
		objOutstream2.writeInt(objectSize);
		objOutstream2.close();
		
		// add request object
		dataArray2.write(dataArray.toByteArray());
		super.send(socket, ByteBuffer.wrap(dataArray2.toByteArray()));
		sendObjectCounter++;
	}
	
	public ByteBuffer receiveObject(SocketChannel socket,ByteBuffer readBuffer,ObjectHandler objectHandler,int offset) throws IOException {
		
		if (readBuffer.limit()<8) {
			logger.fine("Missing bytes, could not read object Size bufLimit: "+readBuffer.limit());
			return readBuffer;
		}
		
		ByteArrayInputStream in = new ByteArrayInputStream(readBuffer.array(),0,readBuffer.limit());
		
		//System.out.println("in: "+in.available()+" off: "+offset);
		if ((in.available()-offset)<8) {
			logger.info("--------------------------------- TODO: error, fix");
			return readBuffer;
		}
		
		in.skip(offset);
		
		ObjectInputStream objstream = new ObjectInputStream(in);
		int objectSize = objstream.readInt();
		objstream.close();
		
		if (objectSize>in.available()) {
			logger.fine("Not all bytes gotten: objSize: "+objectSize+" inA: "+in.available());
			readBuffer.position(offset);
			return readBuffer;
		}
		
		
    	objstream = new ObjectInputStream(in);
    	Object result = null;
		try {
			result = objstream.readObject();
			objectHandler.processObject(result);
		} catch (ClassNotFoundException e) {
			throw new IOException("Could not load object class: "+e.getMessage(),e);
		} finally {
			if (in.available()>0 ) {
				logger.fine("==== dubbel request added left over: "+in.available()+" offset: "+(readBuffer.limit()-in.available()));
				return receiveObject(socket,readBuffer,objectHandler,(readBuffer.limit()-in.available()));
				//return null;
			}
		}
    	return null;
	}
	
	public interface ObjectHandler {
		public void processObject(Object object);
	}
}