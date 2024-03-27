package com.system.management.utils.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ProcessException extends RuntimeException {

    private final int status = HttpStatus.INTERNAL_SERVER_ERROR.value();

    public ProcessException(String message) {
        super(message);
    }
}
