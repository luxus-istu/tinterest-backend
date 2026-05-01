package com.luxus.tinterest.exception.handler;

import com.luxus.tinterest.exception.ErrorCode;
import com.luxus.tinterest.exception.ErrorResponse;
import com.luxus.tinterest.exception.profile.InvalidAvatarFileException;
import com.luxus.tinterest.exception.profile.StorageOperationException;
import com.luxus.tinterest.exception.profile.UnknownInterestsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;

@RestControllerAdvice
public class ProfileHandler {

    @ExceptionHandler(InvalidAvatarFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidAvatarFile(InvalidAvatarFileException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(ErrorCode.INVALID_AVATAR_FILE.name(), ex.getMessage(), null));
    }

    @ExceptionHandler(UnknownInterestsException.class)
    public ResponseEntity<ErrorResponse> handleUnknownInterests(UnknownInterestsException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                        ErrorCode.INVALID_INTERESTS.name(),
                        ex.getMessage(),
                        Map.of("interests", String.join(", ", ex.getUnknownInterests()))
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(ErrorCode.INVALID_AVATAR_FILE.name(), "Avatar file is too large", null));
    }

    @ExceptionHandler(StorageOperationException.class)
    public ResponseEntity<ErrorResponse> handleStorageOperation(StorageOperationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(ErrorCode.STORAGE_ERROR.name(), ex.getMessage(), null));
    }
}
