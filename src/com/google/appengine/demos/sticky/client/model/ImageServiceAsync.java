package com.google.appengine.demos.sticky.client.model;

import com.google.appengine.demos.sticky.client.model.ImageService.Flip;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ImageServiceAsync {

	void getImageUrl(String key, AsyncCallback<String> callback);

	void rotateImage(String url, int degree, AsyncCallback<Void> callback);

	void flipImage(String key, Flip axis, AsyncCallback<Void> callback);

}
