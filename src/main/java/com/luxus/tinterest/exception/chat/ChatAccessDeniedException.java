package com.luxus.tinterest.exception.chat;

public class ChatAccessDeniedException extends RuntimeException {
    public ChatAccessDeniedException() {
        super("User is not a member of this chat");
    }
}
