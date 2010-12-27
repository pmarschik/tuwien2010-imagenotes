package com.google.appengine.demos.sticky.client;

import com.google.appengine.demos.sticky.client.model.*;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;

import java.util.Arrays;

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
	private Button rotateButton = new Button("Rotate Image");

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

		content.setStyleName("note-content");
		content.addValueChangeHandler(this);

        comments.setStyleName("note-comments");

        HorizontalPanel editPanel = new HorizontalPanel();
        mainPanel.add(editPanel);

        editPanel.add(uploader);
		editPanel.add(rotateButton);

        DisclosurePanel commentPanel = new DisclosurePanel("Comments");
        mainPanel.add(commentPanel);

        comments.setRowCount(note.getComments().length, true);
        comments.setRowData(0, Arrays.asList(note.getComments()));

        VerticalPanel commentsPanel = new VerticalPanel();
        commentPanel.setContent(commentsPanel);
        commentsPanel.add(comments);
        commentsPanel.add(newCommentContent);
        commentsPanel.add(newCommentButton);

        newCommentContent.setText("Add comment ...");
        newCommentButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                    NoteView.this.model.addCommentToNote(NoteView.this.note, newCommentContent.getText());
            }
        });

        rotateButton.setVisible(note.hasImage());
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
/*
        StringBuilder commentText=new StringBuilder("Comments:\n");

        if(note.getComments().length > 0)
            for(Comment comment : note.getComments()) {
                commentText.append(comment.getAuthor().getShortName());
                commentText.append(": ");
                commentText.append(comment.getContent());
                commentText.append('\n');
            }
        else
            commentText.append("no comments yet");

        comments.setText(commentText.toString());
*/
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
}