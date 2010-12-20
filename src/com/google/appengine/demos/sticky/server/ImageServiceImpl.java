package com.google.appengine.demos.sticky.server;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.demos.sticky.client.model.ImageService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ImageServiceImpl extends RemoteServiceServlet implements
		ImageService {
	private static Logger log = LoggerFactory.getLogger(ImageServiceImpl.class);
	
	private static String FETCH_SERVLET = "/sticky/imageFetch/";

	@Override
	public String getImageUrl(String key) throws ImageNotFoundException {
		log.debug("getImageUrl called");
		
		HttpServletRequest request = getThreadLocalRequest();
		String url = "http://";
		url += request.getServerName();
		url += ":" + request.getServerPort();
		url += FETCH_SERVLET + key;
		
		log.debug(url);
		return url;
	}

}
