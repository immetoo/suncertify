/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db;

/**
 * An DuplicateKeyException is thrown the the database when a record already execits in the database.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
@SuppressWarnings("serial")
public class DuplicateKeyException extends Exception {

	/**
	 * Default constructor
	 */
	public DuplicateKeyException() {
	}
	
	/**
	 * Message constructor
	 * @param message	The error message
	 */
	public DuplicateKeyException(String message) {
		super(message);
	}
	
}
