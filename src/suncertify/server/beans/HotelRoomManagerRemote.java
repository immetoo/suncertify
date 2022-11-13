/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.server.beans;

import java.util.List;

import suncertify.db.DuplicateKeyException;
import suncertify.db.RecordNotFoundException;
import suncertify.models.HotelRoom;

/**
 * The HotelRoomManagerRemote manages the HotelRooms.
 * 
 * 
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public interface HotelRoomManagerRemote {
	
	// TEMP !!!
	public List<HotelRoom> getAllHotelRooms() throws RecordNotFoundException;
	
	/**
	 * 
	 * @param hotelRoom
	 * @return
	 * @throws RecordNotFoundException
	 */
	public List<HotelRoom> findByCriteria(HotelRoom hotelRoom) throws RecordNotFoundException;
	
	/**
	 * 
	 * @param id
	 * @return
	 * @throws RecordNotFoundException
	 */
	public Long lockHotelRoom(Integer id) throws RecordNotFoundException;
	
	/**
	 * 
	 * @param id
	 * @param lockId
	 * @throws RecordNotFoundException
	 */
	public void unlockHotelRoom(Integer id,Long lockId) throws RecordNotFoundException;
	
	/**
	 * Persists an HotelRoom
	 * @param hotelRoom
	 * @return
	 * @throws DuplicateKeyException
	 */
	public Integer persist(HotelRoom hotelRoom) throws DuplicateKeyException;
	
	/**
	 * Merges an HotelRoom
	 * @param hotelRoom
	 * @param lockId
	 * @throws RecordNotFoundException
	 * @throws SecurityException
	 */
	public void merge(HotelRoom hotelRoom,Long lockId) throws RecordNotFoundException, SecurityException;
	
	/**
	 * Deletes an HotelRoom
	 * @param hotelRoom
	 * @param lockId
	 * @throws RecordNotFoundException
	 * @throws SecurityException
	 */
	public void delete(HotelRoom hotelRoom,Long lockId) throws RecordNotFoundException, SecurityException;
}