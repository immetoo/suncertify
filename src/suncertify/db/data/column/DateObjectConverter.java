/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DateObjectConverter extends AbstractColumnObjectConverter {

	public DateObjectConverter() {
		super(Date.class);
	}
	
	public Object decodeInterface(Column c,String object) {
		Date realDate = null;
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	        realDate = dateFormat.parse(object);
	    } catch (ParseException e) {
	    	throw new IllegalStateException("Date format error on: '"+object+"'");
	    }
		return realDate;
	}
	
	public String encodeInterface(Column c,Object object) {
		if ((object instanceof Date)==false) {
			throw new IllegalStateException("Object is no date");
		}
		String value = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
		value = dateFormat.format((Date)object);
		return value;
	}
}
