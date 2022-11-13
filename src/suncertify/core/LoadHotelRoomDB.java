/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.core;

import java.io.File;
import java.io.IOException;

import suncertify.db.DataBaseManager;
import suncertify.db.data.Table;
import suncertify.db.data.column.BooleanObjectConverter;
import suncertify.db.data.column.Column;
import suncertify.db.data.column.DateObjectConverter;
import suncertify.db.data.column.IntegerObjectConverter;
import suncertify.db.data.column.LongObjectConverter;
import suncertify.db.data.column.StringColumnSearcher;
import suncertify.db.data.column.StringObjectConverter;


/**
 * LoadHotelRoomDB loads the db-1x3.db database file.
 * And fills in some meta data in the Table model which is missing in the db file.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class LoadHotelRoomDB {
	

	/**
	 * Loads the database and configs the fields acouring the file specefications.
	 * @param dbm
	 * @return
	 * @throws IOException
	 */
	public Table loadTable(DataBaseManager dbm) throws IOException {
		
		File f = new File("db-1x3.db");
		dbm.openTable(f);
				
		Table table = dbm.getTable(f.getName());
		// manual config for data safe type
		for (Column c:table.getColumns()) {
			if (1==c.getColumnIndex() | 2==c.getColumnIndex()) {
				c.setObjectConverter(new StringObjectConverter());
				c.setColumnSearcher(new StringColumnSearcher());
			}
			if (3==c.getColumnIndex() | 7==c.getColumnIndex()) {
				c.setObjectConverter(new IntegerObjectConverter());
			}
			if (4==c.getColumnIndex()) {
				c.setObjectConverter(new BooleanObjectConverter());
			}
			if (5==c.getColumnIndex()) {
				c.setObjectConverter(new LongObjectConverter());
			}
			if (6==c.getColumnIndex()) {
				c.setObjectConverter(new DateObjectConverter());
			}
		}
		return table;
	}
	
}