/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;


public class StringObjectConverter extends AbstractColumnObjectConverter {

	public StringObjectConverter() {
		super(String.class);
	}
	
	/**
	 * @see suncertify.db.data.column.ColumnObjectConverter#decodeInterface(suncertify.db.data.column.Column, java.lang.String)
	 */
	@Override
	public Object decodeInterface(Column c, String object) {
		return object;
	}
	
	/**
	 * @see suncertify.db.data.column.ColumnObjectConverter#encodeInterface(suncertify.db.data.column.Column, java.lang.Object)
	 */
	@Override
	public String encodeInterface(Column c, Object object) {
		if ((object instanceof String)==false) {
			throw new IllegalStateException("Can only do string");
		}
		return (String)object;
	}
}
