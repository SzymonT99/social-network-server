package com.server.springboot.exception;

public class ExistingDataException extends RuntimeException{

    public ExistingDataException(String data) {
        super("There is already a user with the given " + data);
    }
}
