/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.client;

import suncertify.server.beans.HotelRoomManagerRemote;

public class ClientController {
	
	
	
	private HotelRoomManagerRemote hotelRoomManager = null;

	/**
	 * @return the hotelRoomManager
	 */
	public HotelRoomManagerRemote getHotelRoomManager() {
		return hotelRoomManager;
	}

	/**
	 * @param hotelRoomManager the hotelRoomManager to set
	 */
	public void setHotelRoomManager(HotelRoomManagerRemote hotelRoomManager) {
		this.hotelRoomManager = hotelRoomManager;
	}	
}