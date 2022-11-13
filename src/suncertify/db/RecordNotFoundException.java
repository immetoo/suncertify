/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db;

/**
 * An RecordNotFoundException is thrown the the database when a record is not found.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
@SuppressWarnings("serial")
public class RecordNotFoundException extends Exception {

	/**
	 * Default constructor
	 */
	public RecordNotFoundException() {
	}
	
	/**
	 * Message constructor
	 * @param message	The error message
	 */
	public RecordNotFoundException(String message) {
		super(message);
	}
}
