/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.client;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import suncertify.models.HotelRoom;
import suncertify.server.beans.HotelRoomManagerRemote;

@SuppressWarnings("serial")
public class HotelRoomTableModel extends AbstractTableModel {
	
	private List<HotelRoom> data = new ArrayList<HotelRoom>(0);
	
	private HotelRoomManagerRemote hotelRoomManager = null;
	
	public HotelRoomTableModel(HotelRoomManagerRemote hotelRoomManager) {
		if (hotelRoomManager==null) {
			throw new NullPointerException("Can't get data from null hotelRoomManager");
		}
		this.hotelRoomManager=hotelRoomManager;
	}
	
	public void updateByCriteria(HotelRoom room) {
		try {
			data = hotelRoomManager.findByCriteria(room);
			System.out.println("Got size from search: "+data.size());
			this.fireTableDataChanged();
		} catch (Exception e) {
			throw new IllegalStateException("Could not get init all rooms: "+e.getMessage(),e);
		}
	}
	
	public HotelRoom getHotelRoom(int rowIndex) {
		return data.get(rowIndex);
	}
	

	// ============ TableModel
	
	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 5;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {
		return data.size();
	}

	
	
	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return "Name";
		case 1:
			return "Location";
		case 2:
			return "Size";
		case 3:
			return "Date";
		default:
			return "<DEFAULT>";		
		}
	}



	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		HotelRoom room = data.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return room.getName();
		case 1:
			return room.getLocation();
		case 2:
			return room.getSize();
		case 3:
			return room.getDateAvailable();
		default:
			return "<DEFAULT>";
		}
	}
}