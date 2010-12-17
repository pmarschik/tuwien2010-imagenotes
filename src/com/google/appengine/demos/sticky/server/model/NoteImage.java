package com.google.appengine.demos.sticky.server.model;

import java.util.Date;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;

@PersistenceCapable
public class NoteImage {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
	private String key;
	
	@Persistent
	private Blob imageData;
	
	@Persistent
	private String contentType;
	
	@Persistent
	private Date uploadDate;
	
	@Persistent
	private Date lastModified;
	
	@Persistent
	private String userEmail;
	
	public NoteImage() {}
	
	public NoteImage(byte[] imageData, String contentType, String userEmail, Date uploadDate, Date lastModified) {
		this.imageData = new Blob(imageData);
		this.contentType = contentType;
		this.userEmail = userEmail;
		this.uploadDate = uploadDate;
		this.lastModified = lastModified;
	}
	
	public Date getUploadDate() {
		return uploadDate;
	}
	public void setUploadDate(Date uploadDate) {
		this.uploadDate = uploadDate;
	}
	public Date getLastModified() {
		return lastModified;
	}
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}	
	public void setKey(String key) {
		this.key = key;
	}
	public String getKey() {
		return key;
	}

	public void setImageData(Blob imageData) {
		this.imageData = imageData;
	}

	public Blob getImageData() {
		return imageData;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentType() {
		return contentType;
	}

}
