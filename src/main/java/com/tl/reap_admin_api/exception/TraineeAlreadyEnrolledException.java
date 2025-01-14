package com.tl.reap_admin_api.exception;

public class TraineeAlreadyEnrolledException extends RuntimeException {
    public TraineeAlreadyEnrolledException(String message) {
        super(message);
    }
}