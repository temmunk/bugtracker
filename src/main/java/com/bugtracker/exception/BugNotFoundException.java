package com.bugtracker.exception;

public class BugNotFoundException extends RuntimeException {

    public BugNotFoundException(Long id) {
        super("Bug not found with id: " + id);
    }
}
