/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import suncertify.db.data.Table;

/**
 * Manages all open database files in the application.
 * 
 * At the moment this is only one.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class DataBaseManager {
	
	private Logger logger = Logger.getLogger(DataBaseManager.class.getName());
	private Map<String,Table> tables = null;
	
	/**
	 * Creates the DataBaseManager.
	 */
	public DataBaseManager() {
		tables = new HashMap<String,Table>(5);
	}
	
	/**
	 * @return the only managed database table namely the hotelRoom table.
	 */
	public DB getHotelRoomDB() {
		Table table = getTable("db-1x3.db");
		DB db = new Data(table);
		return db;
	}

	// hackje voor server->DB-api converters
	public Table getHotelRoomTable() {
		return getTable("db-1x3.db");
	}

	/**
	 * Starts the database
	 */
	public void start() {
	}
	
	/**
	 * Stops the database manager
	 */
	public void stop() {
		for (Table t:tables.values()) {
			try {
				closeTable(t);
			} catch (Exception e) {
				logger.log(Level.WARNING,"Error while closing table: "+t.getName()+" error: "+e.getMessage(),e);
			}
		}
	}
	
	public Table openTable(File file) throws IOException {
		Table t = new Table();
		t.openTable(file);
		tables.put(t.getName(),t);
		logger.fine("Opened table: "+t.getName());
		return t;		
	}
	
	public void closeTable(Table table) throws IOException {
		logger.fine("Closing table: "+table.getName());
		table.closeTable();
	}
	
	public Table getTable(String name) {
		return tables.get(name);
	}
	
}