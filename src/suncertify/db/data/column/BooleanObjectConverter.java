/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;


public class BooleanObjectConverter extends AbstractColumnObjectConverter {

	public BooleanObjectConverter() {
		super(Boolean.class);
	}
	
	public Object decodeInterface(Column c,String object) {
		return new Boolean(object);
	}
	
	public String encodeInterface(Column c,Object object) {
		if ((object instanceof Boolean)==false) {
			throw new IllegalStateException("Data error, object is not Boolean.");
		}
		Boolean value = (Boolean)object;
		return value.toString();
	}
	
	@Override
	public String decodeStorage(Column c,byte[] data) {
		byte smoking = data[0];
		if (89==smoking) {
			return "true";
		}
		if (78==smoking) {
			return "false";
		}
		throw new IllegalStateException("Data error, unknow value: "+smoking);
	}
	
	@Override
	public byte[] encodeStorage(Column c,String object) {
		Boolean value = new Boolean(object);
		byte[] data = new byte[1];
		if (value) {
			data[0] = 89;
		} else {
			data[0] = 78;
		}
		return data;
	}
}
