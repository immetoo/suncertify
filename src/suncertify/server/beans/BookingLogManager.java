/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.server.beans;

import java.util.ArrayList;
import java.util.List;

/**
 * The BookingLogManager
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class BookingLogManager implements BookingLogManagerRemote {
	
	public List<String> getLastBookings(int logID) {
		List<String> result = new ArrayList<String>(1);
		return result;
	}
}