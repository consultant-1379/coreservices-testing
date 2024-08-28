package com.ericsson.arrest_it.common;

public class ArrestItException extends Exception {
    public ArrestItException(final String message) {
        super(message);
    }

    public ArrestItException(final Exception e) {
        super(e);
    }
}
