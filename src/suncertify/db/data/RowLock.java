/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * RowLock lock a certain row and has timeout of the lock.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class RowLock {
	
	private Integer recordRow = null;
	private Long lockId = null;
	private Date dateCreated = null;
	private Date dateAccess = null;
	private Date dateRelease = null;
	private AtomicInteger countAccess = null;
	
	/**
	 * Creates an RowLock
	 * @param recordRow		The row index to lock.
	 * @param lockTimeout	The lock timeout
	 * @param lockId		The id of the lock
	 */
	public RowLock(Integer recordRow,long lockTimeout,long lockId) {
		if (recordRow==null) {
			throw new NullPointerException("Can't lock null record row number.");
		}
		dateCreated = new Date();
		dateAccess = new Date();
		dateRelease = new Date(dateAccess.getTime()+lockTimeout);
		countAccess = new AtomicInteger(0);
		this.lockId = lockId;
	}

	/**
	 * @return the recordRow
	 */
	public Integer getRecordRow() {
		return recordRow;
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
	 * @return the dateRelease
	 */
	public Date getDateRelease() {
		return dateRelease;
	}

	/**
	 * note: use int here so we not cast to much.
	 * @return the countAccess
	 */
	public int getCountAccess() {
		return countAccess.get();
	}
	
	/**
	 * @return the lockId
	 */
	public Long getLockId() {
		return lockId;
	}
}