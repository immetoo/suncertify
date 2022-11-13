/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.models;

import java.util.Date;

import suncertify.db.data.Table;
import suncertify.db.data.column.Column;

/**
 * The HotelRoomDBConverter
 * 
 * Converts betreen the HotelRoom model and the binany DB data.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class HotelRoomDBConverter  {
	
	private Table table = null;
	
	/**
	 * Creates an HotelRoomDBConverter for the given table.
	 * @param table	The DB table to do converts for.
	 */
	public HotelRoomDBConverter(Table table) {
		if (table==null) {
			throw new NullPointerException("Table may not be null.");
		}
		this.table=table;
	}
	
	/**
	 * Converts String[] data to en HotelRoom data model.
	 * 
	 * @param data	The string data.	
	 * @return		The HotelRoom model.
	 */
	public HotelRoom decode(String[] data) {		
		HotelRoom hr = new HotelRoom();
		for (int i=0;i<=6;i++) {
			Column c = table.getColumns().get(i+1); // todo bounds
			Object object = c.getObjectConverter().decodeInterface(c,data[i]);
			switch(i) {
			case 0:
				hr.setName((String)object);
				break;
			case 1:
				hr.setLocation((String)object);
				break;
			case 2:
				hr.setSize((Integer)object);
				break;
			case 3:
				hr.setSmoking((Boolean)object);
				break;
			case 4:
				hr.setPriceRate((Long)object);
				break;
			case 5:
				hr.setDateAvailable((Date)object);
				break;
			case 6:
				hr.setCustomerId((Integer)object);
				break;
			default:
				throw new IllegalStateException("out of bounds");
			}
		}
		return hr;
	}
	
	/**
	 * Converts an HotelRoom model to the String data.
	 * 
	 * @param hr	The hotelRoom model
	 * @return		The data as an String array.
	 */
	public String[] encode(HotelRoom hr) {
		String[] result = new String[7];
		for (int i=0;i<=6;i++) {
			Column c = table.getColumns().get(i+1); // todo bounds
			Object object = null;
			switch(i) {
			case 0:
				object = hr.getName();
				break;
			case 1:
				object = hr.getLocation();
				break;
			case 2:
				object = hr.getSize();
				break;
			case 3:
				object = hr.getSmoking();
				break;
			case 4:
				object = hr.getPriceRate();
				break;
			case 5:
				object = hr.getDateAvailable();
				break;
			case 6:
				object = hr.getCustomerId();
				break;
			default:
				throw new IllegalStateException("out of bounds");
			}
			String r = null;
			if (object!=null) {
				r = c.getObjectConverter().encodeInterface(c, object);
			} else {
				r = "";
			}
			result[i]=r;
		}
		return result;
	}
}