/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * The PropertiesManager 
 * 
 * @author Willem Cazander
 * @version 1.0 Jan 5, 2009
 */
public class PropertiesManager {

	/** The filename we may use to save the properties of the application. */
	static final String FILENAME = "suncertify.properties";
	/** The properties managed by this manager. */
    private Properties properties = new Properties();
    
    /**
     * Reads in the properties.
     * @throws IOException
     */
    public void start() throws IOException {
    	InputStream in = null;
    	try {
    		in = new FileInputStream(FILENAME);
    		properties.load(in);
    	} catch (FileNotFoundException fnfe) {
    		// we discard this one because one first startup we don't have a file.
    	} finally {
    		if (in!=null) {
    			in.close();
    		}
    	}
    }
	
    /**
     * Saves the properties.
     * @throws IOException
     */
    public void stop() throws IOException {
    	OutputStream out = null;
    	try {
    		out = new FileOutputStream(FILENAME);
    		properties.store(out, "Created by: "+PropertiesManager.class.getSimpleName());
    	} finally {
    		if (out!=null) {
    			out.close();
    		}
    	}
    }
    
    /**
     * Sets an properties.
     * @param key	The key of the property.
     * @param value	The value of the property.
     */
    public void setProperty(String key,String value) {
    	properties.setProperty(key,value);
    }
    
    /**
     * Returns the value of the property.
     * @param key	The key of the property.
     * @return	The value of the property.
     */
    public String getProperty(String key) {
    	return properties.getProperty(key);
    }
}
