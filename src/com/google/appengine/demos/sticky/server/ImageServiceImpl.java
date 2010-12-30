package com.google.appengine.demos.sticky.server;

import javax.jdo.JDOHelper;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesService.OutputEncoding;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.demos.sticky.client.model.ImageService;
import com.google.appengine.demos.sticky.server.Store.Note;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class ImageServiceImpl extends RemoteServiceServlet implements ImageService {
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
		Note note = Store.getInstance().getApi().getNote(KeyFactory.stringToKey(key));

		ImagesService imagesService = ImagesServiceFactory.getImagesService();
		Transform resize = ImagesServiceFactory.makeRotate(degree);
		Image oldImage = ImagesServiceFactory.makeImage(note.getImageData().getBytes());

		Image newImage = imagesService.applyTransform(resize, oldImage, OutputEncoding.valueOf(oldImage.getFormat().name()));
		newImage = resizeIfImageToBig(imagesService, oldImage, newImage);
		note.setImageData(new Blob(newImage.getImageData()));

		saveNote(note);
	}

	@Override
	public void flipImage(String key, Flip axis) {
		Note note = Store.getInstance().getApi().getNote(KeyFactory.stringToKey(key));
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
		Image oldImage = ImagesServiceFactory.makeImage(note.getImageData().getBytes());

		Image newImage = imagesService.applyTransform(flip, oldImage, OutputEncoding.valueOf(oldImage.getFormat().name()));
		newImage = resizeIfImageToBig(imagesService, oldImage, newImage);
		note.setImageData(new Blob(newImage.getImageData()));

		saveNote(note);
	}

	private Image resizeIfImageToBig(ImagesService imagesService, Image oldImage, Image newImage) {
		log.debug("converted image size in Kb: " + newImage.getImageData().length / 1024);
		if (newImage.getImageData().length / 1024 >= 1024) {
			log.debug(String.format("image after image manipulation %s x %s", newImage.getWidth(), newImage.getHeight()));
			int newWidth = (int) ((double) newImage.getWidth() * 1024d / (double) (newImage.getImageData().length / 1024d) * 0.8d);
			int newHeigth = (int) ((double) newImage.getHeight() * 1024d / (double) (newImage.getImageData().length / 1024d) * 0.8d);
			
			log.debug("converted image > 1024kB. trying to resize...");
			Transform resize = ImagesServiceFactory.makeResize(newWidth, newHeigth);
			newImage = imagesService.applyTransform(resize, newImage, OutputEncoding.valueOf(oldImage.getFormat().name()));

			log.debug(String.format("image after resizing %s x %s", newImage.getWidth(), newImage.getHeight()));
			log.debug("new size after resizing: " + newImage.getImageData().length / 1024);
		}
		return newImage;
	}

	private void saveNote(Note note) {
		JDOHelper.getPersistenceManager(note).currentTransaction().begin();
		JDOHelper.getPersistenceManager(note).makePersistent(note);
		JDOHelper.getPersistenceManager(note).currentTransaction().commit();
	}
}
