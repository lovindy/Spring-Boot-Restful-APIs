package com.example.springrestful.exception;

public class DuplicateRegistrationNumberException extends RuntimeException {
    public DuplicateRegistrationNumberException(String registrationNumber) {
        super("An organization with registration number " + registrationNumber + " already exists.");
    }
}
