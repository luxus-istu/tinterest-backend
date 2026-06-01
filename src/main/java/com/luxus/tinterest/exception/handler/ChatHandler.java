package com.luxus.tinterest.exception.handler;

import com.luxus.tinterest.exception.ErrorCode;
import com.luxus.tinterest.exception.ErrorResponse;
import com.luxus.tinterest.exception.chat.ChatAccessDeniedException;
import com.luxus.tinterest.exception.chat.ChatNotFoundException;
import com.luxus.tinterest.exception.chat.InvalidChatOperationException;
import com.luxus.tinterest.exception.chat.InvalidChatRoleException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ChatHandler {

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleChatNotFound(ChatNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(ErrorCode.CHAT_NOT_FOUND.name(), ex.getMessage(), null));
    }

    @ExceptionHandler(ChatAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleChatAccessDenied(ChatAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ErrorCode.CHAT_ACCESS_DENIED.name(), ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidChatRoleException.class)
    public ResponseEntity<ErrorResponse> handleInvalidChatRole(InvalidChatRoleException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(ErrorCode.INVALID_CHAT_ROLE.name(), ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidChatOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidChatOperation(InvalidChatOperationException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ErrorCode.INVALID_CHAT_OPERATION.name(), ex.getMessage(), null));
    }
}
