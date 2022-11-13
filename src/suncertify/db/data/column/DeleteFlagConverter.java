/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;


public class DeleteFlagConverter extends BooleanObjectConverter {

	@Override
	public byte[] encodeStorage(Column c, String str) {
		Boolean value = new Boolean(str);
		byte[] data = new byte[1];
		if (value) {
			data[0] = (byte)255; // java bytes are signed so; -128 == 255 == 0xFF
		} else {
			data[0] = 0; 
		}
		return data;
	}

	@Override
	public String decodeStorage(Column c, byte[] data) {
		if (data[0]==0) {
			return new Boolean(false).toString();
		} else if (data[0]==(byte)255) {
			return new Boolean(true).toString();
		}
		throw new IllegalArgumentException("Unknow delete flag value: '"+data[0]+"'");
	}
}
