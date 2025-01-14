package com.tl.reap_admin_api.exception;

public class DuplicateCourseCodeException extends RuntimeException {
    public DuplicateCourseCodeException(String courseCode) {
        super("Course with code " + courseCode + " already exists");
    }
}
