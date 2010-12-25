package com.google.appengine.demos.sticky.client;

import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;

import com.google.appengine.demos.sticky.client.model.ImageService;
import com.google.appengine.demos.sticky.client.model.ImageServiceAsync;
import com.google.appengine.demos.sticky.client.model.Model;
import com.google.appengine.demos.sticky.client.model.Note;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/* Copyright (c) 2009 Google Inc.


 /**
 * A widget to display the collection of notes that are on a particular
 * {@link Surface}.
 */

class NoteView extends SimplePanel implements Note.Observer, MouseUpHandler,
		MouseDownHandler, MouseMoveHandler, Model.SuccessCallback,
		ValueChangeHandler<String>, IUploader.OnFinishUploaderHandler {
	private final Note note;
	private final Model model;

	private final DivElement titleElement;

	private final ImageServiceAsync imageService = GWT
			.create(ImageService.class);
	private final TextArea content = new TextArea();

	private Image image = new Image();
	private SingleUploader uploader = new SingleUploader();
	private Hidden uploaderNoteKey = new Hidden("noteKey");
	private Button rotateButton;

	// Dragging state.
	private boolean dragging;

	private int dragOffsetX, dragOffsetY;
	private NoteSelectionCallback callback;

	/**
	 * @param note
	 *            the note to render
	 */
	public NoteView(final Note note, Model model, NoteSelectionCallback callback) {
		this.callback = callback;
		this.model = model;
		this.note = note;

		setStyleName("note");
		note.setObserver(this);

		// Build simple DOM Structure.
		final Element elem = getElement();
		elem.getStyle().setProperty("position", "absolute");
		titleElement = elem.appendChild(Document.get().createDivElement());
		titleElement.setClassName("note-title");

		// Create Upload widget
		uploader.add(uploaderNoteKey);
		uploader.setServletPath("/sticky/imageUpload");
		uploader.addOnFinishUploadHandler(this);
		uploader.setVisible(!note.hasImage());
		uploaderNoteKey.setValue(note.getKey());

		VerticalPanel mainPanel = new VerticalPanel();
		mainPanel.add(image);
		mainPanel.add(content);
		mainPanel.add(uploader);
		rotateButton = new Button("rotate");
		rotateButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				imageService.rotateImage(NoteView.this.note.getKey(), 90,
						new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable caught) {
								System.out.println("can't rotate image "
										+ caught.getMessage());
							}

							@Override
							public void onSuccess(Void result) {
								image.setUrl(image.getUrl()+"=reload="+Math.random());
								render();
								
							}
						});
			}
		});
		mainPanel.add(rotateButton);

		content.setStyleName("note-content");
		content.addValueChangeHandler(this);

		setWidget(mainPanel);

		render();

		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseMoveEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
	}

	public void onMouseDown(MouseDownEvent event) {
		callback.select(this);
		if (!note.isOwnedByCurrentUser()) {
			return;
		}

		final EventTarget target = event.getNativeEvent().getEventTarget();
		assert Element.is(target);
		if (!Element.is(target)) {
			return;
		}

		if (titleElement.isOrHasChild(Element.as(target))) {
			dragging = true;
			final Element elem = getElement().cast();
			dragOffsetX = event.getX();
			dragOffsetY = event.getY();
			DOM.setCapture(elem);
			event.preventDefault();
		}
	}

	public void onMouseMove(MouseMoveEvent event) {
		if (dragging) {
			setPixelPosition(event.getX() + getAbsoluteLeft() - dragOffsetX,
					event.getY() + getAbsoluteTop() - dragOffsetY);
			event.preventDefault();
		}
	}

	public void onMouseUp(MouseUpEvent event) {
		if (dragging) {
			dragging = false;
			DOM.releaseCapture(getElement());
			event.preventDefault();
			model.updateNotePosition(note, getAbsoluteLeft(), getAbsoluteTop(),
					note.getWidth(), note.getHeight());
		}
	}

	public void onUpdate(Note note) {
		if (!this.note.hasImage())
			uploader.setVisible(true);
		render();
	}

	public void onNoteKeySuccessfullySet(Note note) {
		uploaderNoteKey.setValue(note.getKey());
		uploader.setVisible(true);
		render();
	}

	public void onImageUpdate(Note note) {
		image.setUrl(note.getImageUrl());
		render();
	}

	public void onValueChange(ValueChangeEvent<String> event) {
		model.updateNoteContent(note, event.getValue());
	}

	public void setPixelPosition(int x, int y) {
		final Style style = getElement().getStyle();
		style.setPropertyPx("left", x);
		style.setPropertyPx("top", y);
	}

	public void setPixelSize(int width, int height) {
		content.setPixelSize(width, height);
	}

	private void render() {
		setPixelPosition(note.getX(), note.getY());

		setPixelSize(note.getWidth(), note.getHeight());

		titleElement.setInnerHTML(note.getAuthorName());

		final String noteContent = note.getContent();

		content.setText((noteContent == null) ? "" : noteContent);

		content.setReadOnly(!note.isOwnedByCurrentUser());
	}

	public void select(int zIndex) {
		getElement().getStyle().setProperty("zIndex", "" + zIndex);
	}

	/**
	 * Called by the uploader, when the upload is finished.
	 */
	@Override
	public void onFinish(IUploader val) {
		uploader.setVisible(false);
		model.getImageUrlForNote(note);
	}

	@Override
	public void onResponse(boolean success) {
		model.getImageUrlForNote(note);
	}
}