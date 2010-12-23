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
import com.google.appengine.demos.sticky.server.Store.Note;

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
		log.debug("Fetching image for note with key: " + key);
		
		Note note = getNoteFromStore(key);
		
		if(note!=null) {
			response.setContentType(note.getContentType());
			response.getOutputStream().write(note.getImageData().getBytes());
		} else {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	private Note getNoteFromStore(String strKey) {
		Store.Api api = store.getApi();
		Note note = null;
		
		try {
			Key key = KeyFactory.stringToKey(strKey);
			Transaction tx = api.begin();
			note = api.getNote(key);
			tx.commit();
			
		} catch(Exception ex) {
			log.error(ex.getMessage(), ex);
		} finally {
			api.close();
		}
		
		return note;
	}

}
