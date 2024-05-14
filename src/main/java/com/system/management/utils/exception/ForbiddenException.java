package com.system.management.utils.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ForbiddenException extends RuntimeException {

    private final int status = HttpStatus.FORBIDDEN.value();

    public ForbiddenException(String message) {
        super(message);
    }
}
