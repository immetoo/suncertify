/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */


package suncertify.net;

import java.nio.channels.SocketChannel;

/**
 * The NIOChangeRequest is used by the NIOConnector to register of change the operation key.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class NIOChangeRequest {
	
	public enum ChangeType {
		REGISTER,
		CHANGEOPS
	}
  
	public SocketChannel socket;
	public ChangeType type;
	public int ops;
  
	public NIOChangeRequest(SocketChannel socket, ChangeType type, int ops) {
		this.socket = socket;
		this.type = type;
		this.ops = ops;
	}
}