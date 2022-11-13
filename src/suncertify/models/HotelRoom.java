/*
 * Copyright 2008 Willem Cazander.
 * Created for Sun Certified Developer for the Java 2 Platform
 * Application Submission (Version 1.1.3)
 */

package suncertify.models;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;


/**
 * The HotelRoom model class.
 * 
 * This stores all the data which belongs to one hotelroom.
 * 
 * @author Willem Cazander
 * @version 1.0 Dec 14, 2008
 */
public class HotelRoom implements Externalizable  {
	
	private Integer id = null;
	private String name = null;
	private String location = null;
	private Integer size = null;
	private Boolean smoking = null;
	private Long priceRate = null;
	private Date dateAvailable = null;
	private Integer customerId = null;
	
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}
	
	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	
	/**
	 * @return the name
	 * 
	 * 
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return the location
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * @param location the location to set
	 */
	public void setLocation(String location) {
		this.location = location;
	}
	
	/**
	 * @return the size
	 */
	public Integer getSize() {
		return size;
	}
	
	/**
	 * @param size the size to set
	 */
	public void setSize(Integer size) {
		this.size = size;
	}
	
	/**
	 * @return the smoking
	 */
	public Boolean getSmoking() {
		return smoking;
	}
	
	/**
	 * @param smoking the smoking to set
	 */
	public void setSmoking(Boolean smoking) {
		this.smoking = smoking;
	}
	
	/**
	 * @return the priceRate
	 */
	public Long getPriceRate() {
		return priceRate;
	}
	
	/**
	 * @param priceRate the priceRate to set
	 */
	public void setPriceRate(Long priceRate) {
		this.priceRate = priceRate;
	}
	
	/**
	 * @return the dateAvailable
	 */
	public Date getDateAvailable() {
		return dateAvailable;
	}
	
	/**
	 * @param dateAvailable the dateAvailable to set
	 */
	public void setDateAvailable(Date dateAvailable) {
		this.dateAvailable = dateAvailable;
	}
	
	/**
	 * @return the customerId
	 */
	public Integer getCustomerId() {
		return customerId;
	}
	
	/**
	 * @param customerId the customerId to set
	 */
	public void setCustomerId(Integer customerId) {
		this.customerId = customerId;
	}
	
	
	
	/**
	 * We support reading null fields because BU checks should not take place here.
	 * This is here for speed.
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		boolean	nullRes = in.readBoolean();
		id 				= nullRes?in.readInt():null;
		nullRes 		= in.readBoolean();
		name			= nullRes?in.readUTF():null;
		nullRes 		= in.readBoolean();
		location		= nullRes?in.readUTF():null;
		nullRes 		= in.readBoolean();
		size			= nullRes?in.readInt():null;
		nullRes 		= in.readBoolean();
		smoking			= nullRes?in.readBoolean():null;
		nullRes 		= in.readBoolean();
		priceRate		= nullRes?in.readLong():null;
		nullRes 		= in.readBoolean();
		dateAvailable	= nullRes?new Date(in.readLong()):null;
		nullRes 		= in.readBoolean();
		customerId		= nullRes?in.readInt():null;
	}

	/**
	 * We support writeing null fields because BU checks should not take place here.
	 * This is here for speed.
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		
		if (id==null) {
			out.writeBoolean	(false);
		} else {
			out.writeBoolean	(true);
			out.writeInt		(id);
		}
		if (name==null) {
			out.writeBoolean	(false);
		} else {
			out.writeBoolean	(true);
			out.writeUTF		(name);
		}
		if (location==null) {
			out.writeBoolean	(false);
		} else {
			out.writeBoolean	(true);
			out.writeUTF		(location);	
		}
		if (size==null) {
			out.writeBoolean	(false);
		} else {
			out.writeBoolean	(true);
			out.writeInt		(size);
		}
		if (smoking==null) {
			out.writeBoolean	(false);
		} else {
			out.writeBoolean	(true);
			out.writeBoolean	(smoking);
		}
		if (priceRate==null) {
			out.writeBoolean	(false);
		} else {
			out.writeBoolean	(true);
			out.writeLong		(priceRate);
		}
		if (dateAvailable==null) {
			out.writeBoolean	(false);
		} else {
			out.writeBoolean	(true);
			out.writeLong		(dateAvailable.getTime());
		}
		if (customerId==null) {
			out.writeBoolean	(false);
		} else {
			out.writeBoolean	(true);
			out.writeInt		(customerId);
		}
	}
}