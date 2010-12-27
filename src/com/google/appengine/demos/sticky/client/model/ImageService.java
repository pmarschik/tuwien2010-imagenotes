package com.google.appengine.demos.sticky.client.model;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("imageService")
public interface ImageService extends RemoteService {
	enum Flip{
		H, V
	}
	
	public String getImageUrl(String key) throws ImageNotFoundException;
	public void rotateImage(String key, int degree);
	public void flipImage(String key, Flip axis);
	
	
	@SuppressWarnings("serial")
	static class ImageNotFoundException extends Exception {
    }
}
