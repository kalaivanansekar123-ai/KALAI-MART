package com.mahadev.mahadevmart.exception;

/** Thrown when a request has no valid session. Maps to HTTP 401. */
public class UnauthorizedException extends AppException {

    public UnauthorizedException(String message) {
        super("UNAUTHENTICATED", message);
    }
}
