package com.cce.codeeditor.service;

public interface RedisPubSubService {
    void publish(String channel, Object message);
}
