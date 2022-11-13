/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db;

import suncertify.db.data.Table;

/**
 * Wrapper class for interface DB , to Table object.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class Data implements DB {
	
	/** The table we wrap. */
	final private Table table;
	
	/**
	 * Creates an Data wrapper for an Table object.
	 * @param table
	 */
	public Data(Table table) {
		this.table=table;
	}
	
	/**
	 * @see suncertify.db.DB#read(int)
	 */
	@Override
	public String[] read(int row) throws RecordNotFoundException {
		return table.getInterfaceRow(row);
	}
	
	
	/**
	 * @see suncertify.db.DB#find(java.lang.String[])
	 */
	@Override
	public int[] find(String[] criteria) {
		return table.searchByInterfaceCriteria(criteria);
	}
	
	/**
	 * @see suncertify.db.DB#lock(int)
	 */
	@Override
	public long lock(int row) throws RecordNotFoundException {
		return table.lock(row).getLockId();
	}

	/**
	 * @see suncertify.db.DB#unlock(int, long)
	 */
	@Override
	public void unlock(int row, long lockId) throws RecordNotFoundException,SecurityException {
		table.unlock(row, lockId);
	}

	/**
	 * @see suncertify.db.DB#update(int, java.lang.String[], long)
	 */
	@Override
	public void update(int row, String[] data, long lockId) throws RecordNotFoundException, SecurityException {
		table.update(row, data, lockId);
	}
	
	/**
	 * @see suncertify.db.DB#delete(int, long)
	 */
	@Override
	public void delete(int row, long lockId) throws RecordNotFoundException, SecurityException {
		table.delete(row, lockId);
	}
	
	/**
	 * @see suncertify.db.DB#create(java.lang.String[])
	 */
	@Override
	public int create(String[] data) throws DuplicateKeyException {
		return table.create(data);
	}
}