/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;


public class LongObjectConverter extends AbstractColumnObjectConverter {
	
	public LongObjectConverter() {
		super(Long.class);
	}
	
	public Object decodeInterface(Column c,String object) {
		Long realRate = null;
		try {
	        Number number = NumberFormat.getCurrencyInstance(Locale.getDefault()).parse(object);
	        // 123.45
	        if (number instanceof Long) {
	            // Long value
	        	realRate = (Long)number;
	        } else if (number instanceof Double) {
	           realRate = ((Double)number).longValue();
	        } else {
	        	System.out.println("unknow number: "+number.getClass());
	        }
	    } catch (ParseException e) {
	    	e.printStackTrace();
	    }
		return realRate;
	}
	
	public String encodeInterface(Column c,Object object) {
		if ((object instanceof Long)==false) {
			throw new IllegalStateException("Data err");
		}
		Long value = (Long)object;
		String str = "$"+value;
		if (str.length()>c.getFieldLength()) {
			return null;
		}
		return str;
	}
}
