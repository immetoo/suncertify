

package suncertify.db;	

import suncertify.core.LoadHotelRoomDB;
import suncertify.db.Data;
import suncertify.db.data.RowLock;
import suncertify.db.data.Table;
import suncertify.models.HotelRoom;
import suncertify.models.HotelRoomDBConverter;
import junit.framework.TestCase;


public class DataTest extends TestCase {
	
	
	public void printRecord(String[] record) {
		for (String value:record) {
			System.out.print("'");
			System.out.print(value);
			System.out.print("'\t");
		}
		System.out.println("");
	}
	
	
	public void testData() throws Exception {
		
		DataBaseManager dataBaseManager = new DataBaseManager();
		new LoadHotelRoomDB().loadTable(dataBaseManager);
		Table table = dataBaseManager.getHotelRoomTable();
		
		HotelRoomDBConverter c = new HotelRoomDBConverter(table);
		
		Data d = new Data(dataBaseManager.getHotelRoomTable());
		printRecord(d.read(0));
		printRecord(d.read(1));
		printRecord(d.read(9));
		
		String[] first = d.read(8);
		printRecord(first);
		HotelRoom hr = c.decode(first);
		String[] back = c.encode(hr);
		printRecord(back);
		//assertEquals(first,back);
		
		table.closeTable();
	}
	
	public void testLock() throws Exception {
	
		DataBaseManager dataBaseManager = new DataBaseManager();
		new LoadHotelRoomDB().loadTable(dataBaseManager);
		Table table = dataBaseManager.getHotelRoomTable();
		
		String[] interfaceData = {"name-FIELD-Data"+System.nanoTime(),"location-FIELD-DATa","45","true","$349.34","2008/02/13","888"};
		int row = table.create(interfaceData);
		System.out.println("NEW Rcords row:"+row);
		
		RowLock lock = table.lock(2);
		table.unlock(2, lock.getLockId());
		lock = table.lock(2);
		table.unlock(2, lock.getLockId());
		lock = table.lock(2);
		table.unlock(2, lock.getLockId());
		
		// get lock
		lock = table.lock(2);
		
		boolean error = false;
		try {
			table.delete(20, lock.getLockId());
		} catch (SecurityException se) {
			error = true;
		}
		assertEquals(true,error);
		
		
		error = false;
		try {
			table.delete(200000, lock.getLockId());
		} catch (RecordNotFoundException se) {
			error = true;
		}
		assertEquals(true,error);
		
		//table.delete(2, lock.getLockId());
		table.unlock(2, lock.getLockId());
		
		error = false;
		try {
			Data d = new Data(table);
			printRecord(d.read(2));
		} catch (RecordNotFoundException re) {
			error = true;
		}
		//assertEquals(true,error);
		
		table.closeTable();
	}
	
}