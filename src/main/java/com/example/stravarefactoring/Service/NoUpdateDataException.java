package com.example.stravarefactoring.Service;

public class NoUpdateDataException extends RuntimeException{
    public NoUpdateDataException(String cause) {
        super(cause);
    }
}
