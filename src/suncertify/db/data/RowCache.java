/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RowCache stores one record and meta data of data for caching.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class RowCache {
	
	private Integer recordRow = null;
	private String[] record = null;
	private Date dateCreated = null;
	private Date dateAccess = null;
	private AtomicInteger countAccess = null;
	
	/**
	 * Creates new cache row and validates it contains data.
	 * @param record
	 */
	public RowCache(Integer recordRow,String[] record) {
		if (recordRow==null) {
			throw new NullPointerException("Can't cache null record row number.");
		}
		if (record==null) {
			throw new NullPointerException("Can't cache null record.");
		}
		if (record.length==0) {
			throw new NullPointerException("Can't cache empty record.");
		}
		this.record=record;
		dateCreated = new Date();
		dateAccess = new Date();
		countAccess = new AtomicInteger(0);
	}

	/**
	 * @return the record
	 */
	public String[] getRecord() {
		dateAccess = new Date();
		countAccess.incrementAndGet();
		return record;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {
		return dateCreated;
	}

	/**
	 * @return the dateAccess
	 */
	public Date getDateAccess() {
		return dateAccess;
	}

	/**
	 * note: use int here so we not cast to much.
	 * @return the countAccess
	 */
	public int getCountAccess() {
		return countAccess.get();
	}

	/**
	 * @return the recordRow
	 */
	public Integer getRecordRow() {
		return recordRow;
	}
}