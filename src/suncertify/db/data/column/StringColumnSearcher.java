/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;

import suncertify.db.RecordNotFoundException;
import suncertify.db.data.Table;

public class StringColumnSearcher implements ColumnSearcher {

	/**
	 * @see suncertify.db.data.column.ColumnSearcher#searchColumn(suncertify.db.data.Table, suncertify.db.data.column.Column, java.lang.String, java.util.Set)
	 */
	@Override
	public boolean searchColumn(Table table, Column column,String searchString,String columnData) throws RecordNotFoundException {
		return columnData.contains(searchString);
	}
	
	public void indexColumn(Table table, Column column,String columnData) throws RecordNotFoundException {
		
	}
}