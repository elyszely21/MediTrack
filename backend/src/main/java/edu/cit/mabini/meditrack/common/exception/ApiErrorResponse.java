package edu.cit.mabini.meditrack.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
public class ApiErrorResponse {
    private final boolean success;
    private final int status;
    private final String message;
    private final Instant timestamp;
    private final String path;
}

