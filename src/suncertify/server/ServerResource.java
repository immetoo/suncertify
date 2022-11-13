/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The ServerResource is a annotation that can be used to inject beans into your server manages bean.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ServerResource {
	
	/**
	 * The beanName to resolve, defaults to the className of the field.
	 * @return
	 */
    String beanName() default "null";
}