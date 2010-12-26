package com.google.appengine.demos.sticky.client;

import com.google.appengine.demos.sticky.client.model.Note;


public interface NoteSelectionCallback {
	
	public void select(NoteView note);

	public void delete(Note note);

}
