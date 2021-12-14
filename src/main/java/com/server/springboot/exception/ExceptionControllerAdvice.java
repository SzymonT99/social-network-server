package com.server.springboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String field = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(field, errorMessage);
        });
        return errors;
    }

    @ExceptionHandler(ForbiddenException.class )
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorMessage handleForbiddenDataException(RuntimeException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.FORBIDDEN,
                HttpStatus.FORBIDDEN.value(),
                ex.getMessage(),
                request.getDescription(false).substring(4));
    }

    @ExceptionHandler(value = {BadRequestException.class, IllegalStateException.class, DateTimeParseException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorMessage handleBadRequestException(RuntimeException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.BAD_REQUEST,
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                request.getDescription(false).substring(4));
    }

    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorMessage handleNotFoundException(NotFoundException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.NOT_FOUND,
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                request.getDescription(false).substring(4));
    }

    @ExceptionHandler(value = ResourceGoneException.class)
    @ResponseStatus(HttpStatus.GONE)
    public ErrorMessage handleNotFoundException(ResourceGoneException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.GONE,
                HttpStatus.GONE.value(),
                ex.getMessage(),
                request.getDescription(false).substring(4));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    public ErrorMessage handleFileMaxSizeException(MaxUploadSizeExceededException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.EXPECTATION_FAILED,
                HttpStatus.EXPECTATION_FAILED.value(),
                ex.getMessage(),
                request.getDescription(false).substring(4));
    }

    @ExceptionHandler(value = ConflictRequestException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorMessage handleConflictRequestException(ConflictRequestException ex, WebRequest request) {
        return new ErrorMessage(
                HttpStatus.CONFLICT,
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                request.getDescription(false).substring(4));
    }
}
