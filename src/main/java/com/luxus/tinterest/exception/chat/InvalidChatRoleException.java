package com.luxus.tinterest.exception.chat;

import com.luxus.tinterest.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class InvalidChatRoleException extends RuntimeException {
    public InvalidChatRoleException(String message) {
        super(message);
    }
}
