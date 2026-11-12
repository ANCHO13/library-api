package com.example.library.dto;

// Projection for books-per-author stats
public interface AuthorBookCount {

    Long getAuthorId();

    String getAuthorName();

    Long getBookCount();
}
