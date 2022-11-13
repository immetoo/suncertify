/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;


public class IntegerObjectConverter extends AbstractColumnObjectConverter {

	public IntegerObjectConverter() {
		super(Integer.class);
	}
	
	public Object decodeInterface(Column c,String object) {
		if ("".equals(object)) {
			return 0;
		}
		Integer result = new Integer(object);
		return result;
	}
	
	public String encodeInterface(Column c,Object object) {
		if ((object instanceof Integer)==false) {
			throw new IllegalStateException("wrong data type");
		}
		Integer value = (Integer)object;
		return value.toString();
	}
}
