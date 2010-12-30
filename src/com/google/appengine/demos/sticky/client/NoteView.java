package com.google.appengine.demos.sticky.client;

import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;

import java.util.Arrays;

import com.google.appengine.demos.sticky.client.model.Comment;
import com.google.appengine.demos.sticky.client.model.ImageService;
import com.google.appengine.demos.sticky.client.model.ImageServiceAsync;
import com.google.appengine.demos.sticky.client.model.Model;
import com.google.appengine.demos.sticky.client.model.Note;
import com.google.gwt.cell.client.AbstractCell;
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
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/* Copyright (c) 2009 Google Inc.


 /**
 * A widget to display the collection of notes that are on a particular
 * {@link Surface}.
 */

class NoteView extends SimplePanel implements Note.Observer, MouseUpHandler, MouseDownHandler, MouseMoveHandler, Model.SuccessCallback,
		ValueChangeHandler<String>, IUploader.OnFinishUploaderHandler {
	private final class DefaultImageManipulationCallback implements AsyncCallback<Void> {
		@Override
		public void onFailure(Throwable caught) {
			System.out.println("can't perform image manipulation " + caught.getMessage());
		}

		@Override
		public void onSuccess(Void result) {
			forceImageReload();

		}
	}

	private final Note note;
	private final Model model;

	private final DivElement titleElement;

	private final ImageServiceAsync imageService = GWT.create(ImageService.class);

	private Image image = new Image();
	private Image popupImage;
	private SingleUploader uploader = new SingleUploader();
	private Hidden uploaderNoteKey = new Hidden("noteKey");
	private Button rotateButton = new Button("Rotate");
	private Button flipHButton = new Button("Flip Horizontally");
	private Button flipVButton = new Button("Flip Vertically");
	private Button deletNoteButton = new Button("Delete Note");

	private final CellList<Comment> comments = new CellList<Comment>(new CommentCell());
	private Button newCommentButton = new Button("Add Comment");
	private TextArea newCommentContent = new TextArea();

	// Dragging state.
	private boolean dragging;

	private int dragOffsetX, dragOffsetY;
	private NoteSelectionCallback callback;

	private static class CommentCell extends AbstractCell<Comment> {

		@Override
		public void render(Comment value, Object key, SafeHtmlBuilder sb) {
			sb.appendEscapedLines(value.getAuthor().getShortName() + ": ");
			sb.appendEscaped(value.getContent());
		}
	}

	private class ImagePopUp extends PopupPanel {

		public ImagePopUp() {
			super(true);
			if (popupImage == null) {
				popupImage = new Image();
				popupImage.setHeight(String.valueOf(350) + "px");
			}

			popupImage.setUrl(image.getUrl() + "=reload=" + Math.random());
			popupImage.getElement().getStyle().setProperty("zIndex", "" + "99999999999");
			popupImage.getElement().getStyle().setProperty("position", "relative");

			setWidget(popupImage);
		}
	}

	/**
	 * @param note
	 *            the note to render
	 */
	public NoteView(final Note note, Model model, NoteSelectionCallback callback) {
		this.callback = callback;
		this.model = model;
		this.note = note;
		image.setWidth("225px");
		image.setStyleName("picture");
		image.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				final ImagePopUp popup = new ImagePopUp();
				popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
					public void setPosition(int offsetWidth, int offsetHeight) {
						int left = (Window.getClientWidth() - offsetWidth) / 3;
						int top = (Window.getClientHeight() - offsetHeight) / 3;
						popup.setPopupPosition(left, top);
					}
				});
			}
		});
		setStyleName("note");
		note.setObserver(this);

		// Build simple DOM Structure.
		final Element elem = getElement();
		elem.getStyle().setProperty("position", "absolute");
		titleElement = elem.appendChild(Document.get().createDivElement());
		titleElement.setClassName("note-title");
		VerticalPanel mainPanel = new VerticalPanel();

		uploader.setStyleName("upload");
		mainPanel.add(image);

		if (!note.hasImage()) {
			mainPanel.add(uploader);
		}

		if (note.isOwnedByCurrentUser()) {
			mainPanel.add(createUploadEditWidgets(note));
		}

		mainPanel.add(createCommentWidgets(note));

		setWidget(mainPanel);

		render();

		addDomHandler(this, MouseDownEvent.getType());
		addDomHandler(this, MouseMoveEvent.getType());
		addDomHandler(this, MouseUpEvent.getType());
	}

	private DisclosurePanel createCommentWidgets(Note note) {
		DisclosurePanel commentPanel = new DisclosurePanel("Comments");

		VerticalPanel commentsPanel = new VerticalPanel();
		commentsPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		commentPanel.setStyleName("commentpanel");
		commentPanel.setContent(commentsPanel);
		commentsPanel.add(comments);
		commentsPanel.add(newCommentContent);
		commentsPanel.add(newCommentButton);
		comments.setStyleName("comments");

		newCommentContent.setText("Add comment ...");
		newCommentButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				NoteView.this.model.addCommentToNote(NoteView.this.note, newCommentContent.getText());
				render();
			}
		});
		newCommentButton.setStyleName("commentaction");

		return commentPanel;
	}

	private Panel createUploadEditWidgets(Note note) {
		uploader.add(uploaderNoteKey);
		uploader.setServletPath("/sticky/imageUpload");
		uploader.addOnFinishUploadHandler(this);
		uploader.setVisible(!note.hasImage());
		uploaderNoteKey.setValue(note.getKey());

		deletNoteButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				NoteView.this.callback.delete(NoteView.this.note);
			}
		});

		rotateButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				imageService.rotateImage(NoteView.this.note.getKey(), 90, new DefaultImageManipulationCallback());
			}
		});

		flipHButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				imageService.flipImage(NoteView.this.note.getKey(), ImageService.Flip.H, new DefaultImageManipulationCallback());
			}

		});

		flipVButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				imageService.flipImage(NoteView.this.note.getKey(), ImageService.Flip.V, new DefaultImageManipulationCallback());
			}
		});

		VerticalPanel horizontalPanel = new VerticalPanel();
		horizontalPanel.setStyleName("actions");

		horizontalPanel.add(rotateButton);
		horizontalPanel.add(flipHButton);
		horizontalPanel.add(flipVButton);
		horizontalPanel.add(deletNoteButton);

		return horizontalPanel;
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

	public Note getNote() {
		return note;
	}

	public void onMouseMove(MouseMoveEvent event) {
		if (dragging) {
			setPixelPosition(event.getX() + getAbsoluteLeft() - dragOffsetX, event.getY() + getAbsoluteTop() - dragOffsetY);
			event.preventDefault();
		}
	}

	public void onMouseUp(MouseUpEvent event) {
		if (dragging) {
			dragging = false;
			DOM.releaseCapture(getElement());
			event.preventDefault();
			model.updateNotePosition(note, getAbsoluteLeft(), getAbsoluteTop(), note.getWidth(), note.getHeight());
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
	}

	private void render() {
		setPixelPosition(note.getX(), note.getY());
		setPixelSize(note.getWidth(), note.getHeight());
		titleElement.setInnerHTML(note.getAuthorName());
		comments.setRowCount(note.getComments().length, true);
		comments.setRowData(0, Arrays.asList(note.getComments()));
		comments.redraw();
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
		rotateButton.setVisible(true);
		model.getImageUrlForNote(note);
	}

	@Override
	public void onResponse(boolean success) {
		model.getImageUrlForNote(note);
	}

	private void forceImageReload() {
		image.setUrl(image.getUrl() + "=reload=" + Math.random());
		render();
	}
}