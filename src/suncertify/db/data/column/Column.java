/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.db.data.column;

import java.nio.charset.Charset;

public class Column {
	
	private int columnIndex = 0;
	private int fieldNameLength = 0;
	private String fieldName = null;
	private int fieldLength = 0;
	private ColumnObjectConverter objectConverter = null;
	private Charset charset = null;
	private ColumnSearcher columnSearcher = null;
	
	/**
	 * @return the columnIndex
	 */
	public int getColumnIndex() {
		return columnIndex;
	}
	
	/**
	 * @param columnIndex the columnIndex to set
	 */
	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	
	/**
	 * @return the fieldNameLength
	 */
	public int getFieldNameLength() {
		return fieldNameLength;
	}
	
	/**
	 * @param fieldNameLength the fieldNameLength to set
	 */
	public void setFieldNameLength(int fieldNameLength) {
		this.fieldNameLength = fieldNameLength;
	}
	
	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}
	
	/**
	 * @param fieldName the fieldName to set
	 */
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	
	/**
	 * @return the fieldLength
	 */
	public int getFieldLength() {
		return fieldLength;
	}
	
	/**
	 * @param fieldLength the fieldLength to set
	 */
	public void setFieldLength(int fieldLength) {
		this.fieldLength = fieldLength;
	}
	
	/**
	 * @return the objectConverter
	 */
	public ColumnObjectConverter getObjectConverter() {
		return objectConverter;
	}
	
	/**
	 * @param objectConverter the objectConverter to set
	 */
	public void setObjectConverter(ColumnObjectConverter objectConverter) {
		this.objectConverter = objectConverter;
	}

	/**
	 * @return the charset
	 */
	public Charset getCharset() {
		return charset;
	}
	
	/**
	 * @param charset the charset to set
	 */
	public void setCharset(Charset charset) {
		this.charset = charset;
	}

	/**
	 * @return the columnSearcher
	 */
	public ColumnSearcher getColumnSearcher() {
		return columnSearcher;
	}

	/**
	 * @param columnSearcher the columnSearcher to set
	 */
	public void setColumnSearcher(ColumnSearcher columnSearcher) {
		this.columnSearcher = columnSearcher;
	}
}
