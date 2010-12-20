package com.google.appengine.demos.sticky.server;

import java.io.IOException;

import javax.jdo.Transaction;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.demos.sticky.server.model.NoteImage;

@SuppressWarnings("serial")
public class ImageFetchServlet extends HttpServlet {
	private static Logger log = LoggerFactory.getLogger(ImageFetchServlet.class);
	
	private final Store store = Store.getInstance();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		log.debug("doGet called");
		log.debug("request-uri: " + request.getRequestURI());
		
		String uri = request.getRequestURI();
		String list[] = uri.split("/");
		String key = list[list.length-1];
		log.debug("Fetching image with key: " + key);
		
		NoteImage image = getImageFromStore(key);
		
		if(image!=null) {
			response.setContentType(image.getContentType());
			response.getOutputStream().write(image.getImageData().getBytes());
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	private NoteImage getImageFromStore(String strKey) {
		Store.Api api = store.getApi();
		NoteImage image = null;
		
		try {
			Key key = KeyFactory.stringToKey(strKey);
			Transaction tx = api.begin();
			image = api.getNoteImage(key);
			tx.commit();
			
		} catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			api.close();
		}
		
		return image;
	}

}
