/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;


abstract public class AbstractColumnObjectConverter implements ColumnObjectConverter {

	protected Class<?> convertClass = null;
	abstract public Object decodeInterface(Column c,String object);
	abstract public String encodeInterface(Column c,Object object);
	
	public AbstractColumnObjectConverter(Class<?> convertClass) {
		this.convertClass=convertClass;
	}
	
	/**
	 * @see suncertify.db.data.column.ColumnObjectConverter#getConvertClass()
	 */
	@Override
	public Class<?> getConvertClass() {
		return convertClass;
	}
	/**
	 * @see suncertify.db.data.column.ColumnObjectConverter#decodeStorage(suncertify.db.data.column.Column, java.lang.Object)
	 */
	@Override
	public byte[] encodeStorage(Column c, String str) {
		if (str.length()>c.getFieldLength()) {
			throw new IllegalArgumentException("String is to large to DB");
		}
		byte[] data = str.getBytes(c.getCharset());
		
		// fill out space
		if (data.length<c.getFieldLength()) {
			//int space = c.getFieldLength()-data.length;
			byte[] data2 = new byte[c.getFieldLength()];
			for (int i=0;i<data2.length;i++) {
				if (i<data.length) {
					data2[i]=data[i];
				} else {
					data2[i]=' ';
				}
			}
			return data2;
		}
		
		return data;
	}

	/**
	 * @see suncertify.db.data.column.ColumnObjectConverter#encodeStorage(suncertify.db.data.column.Column, byte[])
	 */
	@Override
	public String decodeStorage(Column c, byte[] data) {
		String result = new String(data,c.getCharset()).trim();
		return result;
	}
}
