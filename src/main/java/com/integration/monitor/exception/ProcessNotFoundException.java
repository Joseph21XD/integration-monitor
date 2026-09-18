package com.integration.monitor.exception;

public class ProcessNotFoundException extends RuntimeException {

    public ProcessNotFoundException(Long id) {
        super("Integration process not found with ID: " + id);
    }
}