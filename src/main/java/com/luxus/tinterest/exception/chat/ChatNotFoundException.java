package com.luxus.tinterest.exception.chat;

public class ChatNotFoundException extends RuntimeException {
    public ChatNotFoundException() {
        super("Chat was not found");
    }
}
