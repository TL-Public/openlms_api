package com.tl.reap_admin_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TraineeNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TraineeNotFoundException(String message) {
        super(message);
    }

    public TraineeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}