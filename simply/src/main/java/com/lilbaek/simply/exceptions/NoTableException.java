package com.lilbaek.simply.exceptions;

public class NoTableException extends RuntimeException {
    public NoTableException(final String name) {
        super("Persistent entity '" + name + "' should have @Table attribute");
    }
}
