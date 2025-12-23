package com.vaadin.starter.bakery.backend.service;

/**
 * A data integrity violation exception containing a message intended to be
 * shown to the end user.
 */
public class UserFriendlyDataException extends RuntimeException {

    public UserFriendlyDataException(String message) {
        super(message);
    }
}
