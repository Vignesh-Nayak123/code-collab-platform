package com.cce.codeeditor.service;

public interface DocumentService {
    void updateContent(String roomCode, String content, Long version);
    String getContent(String roomCode);
    void flushToDatabase();
}
