package com.server.springboot.exception;

public class ConflictRequestException extends RuntimeException{

    public ConflictRequestException(String message) {
        super(message);
    }
}