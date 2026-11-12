package com.example.library.dto;

/**
 * Projection for the "books per author" statistics query.
 */
public interface AuthorBookCount {

    Long getAuthorId();

    String getAuthorName();

    Long getBookCount();
}
