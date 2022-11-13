/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.server.beans;

import java.util.ArrayList;
import java.util.List;

import suncertify.db.DB;
import suncertify.db.DuplicateKeyException;
import suncertify.db.RecordNotFoundException;
import suncertify.models.HotelRoom;
import suncertify.models.HotelRoomDBConverter;
import suncertify.server.ServerResource;

/**
 * The HotelRoomManager
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class HotelRoomManager implements HotelRoomManagerRemote {

	@ServerResource(beanName="hotelRoom.tableBackend")
	public DB backend = null;
	
	@ServerResource(beanName="hotelRoom.beanConverter")
	public HotelRoomDBConverter hotelRoomDBConverter = null;
	
	/**
	 * @see suncertify.server.beans.HotelRoomManagerRemote#findByCriteria(suncertify.models.HotelRoom)
	 */
	@Override
	public List<HotelRoom> findByCriteria(HotelRoom hotelRoom) throws RecordNotFoundException {
		String[] data = hotelRoomDBConverter.encode(hotelRoom);
		int[] result = backend.find(data);
		List<HotelRoom> rooms = new ArrayList<HotelRoom>(result.length);
		for (int i:result) {
			String[] rec = backend.read(i);
			HotelRoom hr = hotelRoomDBConverter.decode(rec);
			hr.setId(i);
			rooms.add(hr);
		}
		return rooms;
	}

	/**
	 * @see suncertify.server.beans.HotelRoomManagerRemote#getAllHotelRooms()
	 */
	@Override
	public List<HotelRoom> getAllHotelRooms() throws RecordNotFoundException  {
		HotelRoom room = new HotelRoom();
		room.setName("a");
		return findByCriteria(room); 		// leaven all values null does an select all
	}
	
	/**
	 * @see suncertify.server.beans.HotelRoomManagerRemote#delete(suncertify.models.HotelRoom, java.lang.Long)
	 */
	@Override
	public void delete(HotelRoom hotelRoom, Long lockId) throws RecordNotFoundException {
		backend.delete(hotelRoom.getId(), lockId);
	}
	
	/**
	 * @see suncertify.server.beans.HotelRoomManagerRemote#merge(suncertify.models.HotelRoom, java.lang.Long)
	 */
	@Override
	public void merge(HotelRoom hotelRoom, Long lockId) throws RecordNotFoundException, SecurityException {
		String[] data = hotelRoomDBConverter.encode(hotelRoom);
		backend.update(hotelRoom.getId(), data, lockId);
	}

	/**
	 * @see suncertify.server.beans.HotelRoomManagerRemote#persist(suncertify.models.HotelRoom)
	 */
	@Override
	public Integer persist(HotelRoom hotelRoom) throws DuplicateKeyException {
		String[] data = hotelRoomDBConverter.encode(hotelRoom);
		return backend.create(data);
	}

	
	/**
	 * @see suncertify.server.beans.HotelRoomManagerRemote#lockHotelRoom(java.lang.Integer)
	 */
	@Override
	public Long lockHotelRoom(Integer id) throws RecordNotFoundException {
		return backend.lock(id);
	}
	
	/**
	 * @see suncertify.server.beans.HotelRoomManagerRemote#unlockHotelRoom(java.lang.Integer, java.lang.Long)
	 */
	@Override
	public void unlockHotelRoom(Integer id, Long lockId) throws RecordNotFoundException {
		backend.unlock(id, lockId);
	}
}