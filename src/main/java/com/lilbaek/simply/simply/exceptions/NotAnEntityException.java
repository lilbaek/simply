package com.lilbaek.simply.simply.exceptions;

public class NotAnEntityException extends RuntimeException {
    public NotAnEntityException(final String name) {
        super("Persistent entity '" + name + "' should have @Entity attribute");
    }
}
