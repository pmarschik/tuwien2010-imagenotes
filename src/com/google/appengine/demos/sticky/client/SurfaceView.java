/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.demos.sticky.client;

import com.google.appengine.demos.sticky.client.model.Model;
import com.google.appengine.demos.sticky.client.model.Note;
import com.google.appengine.demos.sticky.client.model.Surface;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.WidgetCollection;

/**
 * A widget to display the collection of notes that are on a particular
 * {@link Surface}.
 */
public class SurfaceView extends FlowPanel implements Model.DataObserver {

	private final Model model;

	private NoteView selectedNoteView;

	private int zIndex = 1;

	/**
	 * @param model
	 *            the model to which the Ui will bind itself
	 */
	public SurfaceView(Model model) {
		this.model = model;
		final Element elem = getElement();
		elem.setId("surface");
		elem.getStyle().setProperty("position", "absolute");
		model.addDataObserver(this);
	}

	public void onNoteCreated(Note note) {
		final NoteView view = new NoteView(note, model, callback);
		add(view);
		callback.select(view);
	}

	public void onSurfaceCreated(Surface group) {
	}

	public void onSurfaceNotesReceived(Note[] notes) {
		removeAllNotes();
		for (int i = 0, n = notes.length; i < n; ++i) {
			add(new NoteView(notes[i], model, callback));
		}
	}

	public void onSurfaceSelected(Surface nowSelected, Surface wasSelected) {
	}

	public void onSurfacesReceived(Surface[] surfaces) {
	}

	private int nextZIndex() {
		return zIndex++;
	}

	private void removeAllNotes() {
		final WidgetCollection kids = getChildren();
		while (kids.size() > 0) {
			remove(kids.size() - 1);
		}
	}

	private NoteSelectionCallback callback = new NoteSelectionCallback() {

		@Override
		public void select(NoteView noteView) {
			assert noteView != null;
			if (selectedNoteView != noteView) {
				noteView.select(nextZIndex());
				selectedNoteView = noteView;
			}
		}
	};
}
