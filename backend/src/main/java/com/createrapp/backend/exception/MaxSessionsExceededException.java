package com.createrapp.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class MaxSessionsExceededException extends RuntimeException {

    public MaxSessionsExceededException(String message) {
        super(message);
    }

    public MaxSessionsExceededException(int maxSessions) {
        super(String.format("Maximum number of concurrent sessions (%d) exceeded. Oldest session will be terminated.", maxSessions));
    }
}
