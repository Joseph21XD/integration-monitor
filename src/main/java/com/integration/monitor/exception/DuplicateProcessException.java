package com.integration.monitor.exception;

public class DuplicateProcessException extends RuntimeException {

public DuplicateProcessException(String name) {
    super("An integration process already exists with name: " + name);
}

}
