package com.google.appengine.demos.sticky.server;

import javax.jdo.JDOHelper;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.demos.sticky.client.model.ImageService;
import com.google.appengine.demos.sticky.server.Store.Note;
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

	@Override
	public void rotateImage(String key, int degree) {
		Note note = Store.getInstance().getApi()
				.getNote(KeyFactory.stringToKey(key));
		
		ImagesService imagesService = ImagesServiceFactory.getImagesService();
		Transform resize = ImagesServiceFactory.makeRotate(degree);
		Image oldImage = ImagesServiceFactory.makeImage(note.getImageData()
				.getBytes());
		
		Image newImage = imagesService.applyTransform(resize, oldImage);
		note.setImageData(new Blob(newImage.getImageData()));

		saveNote(note);
	}


	@Override
	public void flipImage(String key, Flip axis) {
		Note note = Store.getInstance().getApi()
				.getNote(KeyFactory.stringToKey(key));
		ImagesService imagesService = ImagesServiceFactory.getImagesService();
		Transform flip = null;

		switch (axis) {
		case H:
			flip = ImagesServiceFactory.makeHorizontalFlip();
			break;
		case V:
			flip = ImagesServiceFactory.makeVerticalFlip();
			break;

		default:
			break;
		}
		
		Image oldImage = ImagesServiceFactory.makeImage(note.getImageData()
				.getBytes());
		Image newImage = imagesService.applyTransform(flip, oldImage);
		note.setImageData(new Blob(newImage.getImageData()));

		saveNote(note);

	}

	private void saveNote(Note note) {
		JDOHelper.getPersistenceManager(note).currentTransaction().begin();
		JDOHelper.getPersistenceManager(note).makePersistent(note);
		JDOHelper.getPersistenceManager(note).currentTransaction().commit();
	}
}
