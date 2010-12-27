package com.google.appengine.demos.sticky.client.model;

import com.google.gwt.core.client.GWT;

import java.io.Serializable;

public class Comment implements Serializable {
    private String key;
    private Author author;
    private String content;

    public Comment(Author author, String content) {
        assert GWT.isClient();
        this.author = author;
        this.content = content;
    }

    public Comment(String key, Author author, String content) {
        assert !GWT.isClient();
        this.key = key;
        this.author = author;
        this.content = content;
    }

    public Comment() {}

    public String getKey() {
        return key;
    }

    public Author getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }
}
