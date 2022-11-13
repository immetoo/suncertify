/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.core;

import java.io.IOException;

import suncertify.db.DataBaseManager;
import suncertify.models.HotelRoomDBConverter;
import suncertify.server.ServerManager;
import suncertify.server.beans.BookingLogManager;
import suncertify.server.beans.BookingLogManagerRemote;
import suncertify.server.beans.HotelRoomManager;
import suncertify.server.beans.HotelRoomManagerRemote;


/**
 * Loads the application needed beans into the ServerManager
 * 
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class LoadServerBeans {
	
	/**
	 * Loads the application backend beans into the ServerManager
	 * @param server	The server to initation with beans.
	 * @param dataBaseManager	The databaseManager is needed for injection into beans.
	 * @throws IOException
	 */
	public void loadBeans(ServerManager server,DataBaseManager dataBaseManager) throws IOException {
		
		server.putServerInitBean("hotelRoom.tableBackend", dataBaseManager.getHotelRoomDB());
		server.putServerInitBean("hotelRoom.beanConverter", new HotelRoomDBConverter(dataBaseManager.getHotelRoomTable()));
		
		server.putServerBean(HotelRoomManagerRemote.class,HotelRoomManager.class);
		server.putServerBean(BookingLogManagerRemote.class,BookingLogManager.class);
	}
}