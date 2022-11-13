/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;


import suncertify.db.RecordNotFoundException;
import suncertify.db.data.Table;


public interface ColumnSearcher {
	
	public boolean searchColumn(Table table, Column column,String searchString,String columnData) throws RecordNotFoundException;
}
