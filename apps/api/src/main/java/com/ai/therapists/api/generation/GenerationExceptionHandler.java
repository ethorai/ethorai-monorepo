package com.ai.therapists.api.generation;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GenerationExceptionHandler {

    @ExceptionHandler(GenerationValidationException.class)
    public ResponseEntity<GenerationErrorResponse> handleValidation(GenerationValidationException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new GenerationErrorResponse("GENERATION_VALIDATION_FAILED", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(AiGenerationException.class)
    public ResponseEntity<GenerationErrorResponse> handleAi(AiGenerationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new GenerationErrorResponse("AI_GENERATION_FAILED", ex.getMessage(), Instant.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenerationErrorResponse> handleInvalidInput(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest()
                .body(new GenerationErrorResponse("INVALID_INPUT", "Request payload is invalid", Instant.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenerationErrorResponse> handleNotFound(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new GenerationErrorResponse("NOT_FOUND", ex.getMessage(), Instant.now()));
    }
}
