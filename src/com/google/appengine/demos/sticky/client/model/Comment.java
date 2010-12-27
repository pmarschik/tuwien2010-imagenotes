package com.google.appengine.demos.sticky.client.model;

import java.io.Serializable;

public class Comment implements Serializable {
    private String key;
    private Author author;
    private String content;

    public Comment(String key, Author author, String content) {
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
