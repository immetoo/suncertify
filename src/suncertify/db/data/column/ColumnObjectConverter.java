/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;


public interface ColumnObjectConverter {

	public Class<?> getConvertClass();
	
	public Object decodeInterface(Column c,String object);
	public String encodeInterface(Column c,Object object);
	
	public String decodeStorage(Column c,byte[] data);
	public byte[] encodeStorage(Column c,String object);
}
