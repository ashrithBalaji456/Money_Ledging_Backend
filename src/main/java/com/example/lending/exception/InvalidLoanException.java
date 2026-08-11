package com.example.lending.exception;

public class InvalidLoanException extends RuntimeException {
    public InvalidLoanException(String message) {
        super(message);
    }
}
