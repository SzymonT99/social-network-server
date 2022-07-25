package com.server.springboot.exception;

public class ResourceGoneException extends RuntimeException {

    public ResourceGoneException(String message) {
        super(message);

    }
}
