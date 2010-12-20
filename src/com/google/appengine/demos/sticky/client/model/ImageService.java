package com.google.appengine.demos.sticky.client.model;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("imageService")
public interface ImageService extends RemoteService {
	
	public String getImageUrl(String key) throws ImageNotFoundException;
	
	
	@SuppressWarnings("serial")
	static class ImageNotFoundException extends Exception {
    }
}
