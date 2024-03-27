package com.system.management.utils.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class BadRequestException extends RuntimeException {

    private final int status = HttpStatus.BAD_REQUEST.value();

    public BadRequestException(String message) {
        super(message);
    }
}
