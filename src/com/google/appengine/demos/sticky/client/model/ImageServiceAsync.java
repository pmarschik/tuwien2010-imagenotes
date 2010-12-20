package com.google.appengine.demos.sticky.client.model;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface ImageServiceAsync {

	void getImageUrl(String key, AsyncCallback<String> callback);

}
