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

package com.google.appengine.demos.sticky.client.model;

import com.google.appengine.demos.sticky.client.model.Service.GetNoteResult;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Date;

/**
 * The asynchronous interface for calls to {@link Service}.
 */
public interface ServiceAsync {

    /**
     * @param surfaceKey
     * @param email
     * @param callback
     * @see Service#addAuthorToSurface(String, String)
     */
    void addAuthorToSurface(String surfaceKey, String email,
                            AsyncCallback<Service.AddAuthorToSurfaceResult> callback);

    /**
     * @param noteKey
     * @param content
     * @param callback
     * @see Service#changeNoteContent(String, String)
     */
    void changeNoteContent(String noteKey, String content,
                           AsyncCallback<Date> callback);

    /**
     * @param noteKey
     * @param x
     * @param y
     * @param width
     * @param height
     * @param callback
     * @see Service#changeNotePosition(String, int, int, int, int)
     */
    void changeNotePosition(String noteKey, int x, int y, int width, int height,
                            AsyncCallback<Date> callback);

    /**
     * @param surfaceKey
     * @param x
     * @param y
     * @param width
     * @param height
     * @param callback
     * @see Service#createNote(user, int, int, int, int)
     */
    void createNote(String surfaceKey, int x, int y, int width, int height,
                    AsyncCallback<Service.CreateObjectResult> callback);

    /**
     * @param title
     * @param callback
     * @see Service#createSurface(String)
     */
    void createSurface(String title,
                       AsyncCallback<Service.CreateObjectResult> callback);

    /**
     * @param surfaceKey
     * @param timestamp
     * @param callback
     * @see Service#getNotes(String, String)
     */
    void getNotes(String surfaceKey, String timestamp,
                  AsyncCallback<Service.GetNotesResult> callback);

    /**
     * @param timestamp
     * @param callback
     * @see Service#getSurfaces(String)
     */
    void getSurfaces(String timestamp,
                     AsyncCallback<Service.GetSurfacesResult> callback);

    /**
     * @param callback
     * @see Service#getUserInfo()
     */
    void getUserInfo(AsyncCallback<Service.UserInfoResult> callback);

	void getNote(String noteKey, AsyncCallback<GetNoteResult> callback);

	void deleteNote(String noteKey, AsyncCallback<Void> callback);
}
