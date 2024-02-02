package com.lilbaek.simply.exceptions;

public class NoIdException extends RuntimeException {
    public NoIdException(final String name) {
        super("Persistent entity '" + name + "' should have primary key");
    }
}
