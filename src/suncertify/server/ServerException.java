/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.server;

/**
 * The ServerException is thrown when the server has an exception.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
@SuppressWarnings("serial")
public class ServerException extends Exception {

	/**
	 * Message constructor
	 * @param message	The error message
	 */
	public ServerException(String message) {
		super(message);
	}
	
	/**
	 * Full constructor
	 * @param message	The error message
	 * @param exception	The error exception
	 */
	public ServerException(String message,Exception exception) {
		super(message,exception);
	}
}