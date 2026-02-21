package com.diegchav.staywise.api.error;

import com.diegchav.staywise.api.dto.ErrorResponse;
import com.diegchav.staywise.exception.IdempotencyProcessingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IdempotencyProcessingException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyProcessingException(IdempotencyProcessingException ex) {
        var errorResponse = new ErrorResponse(
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorResponse);
    }
}
