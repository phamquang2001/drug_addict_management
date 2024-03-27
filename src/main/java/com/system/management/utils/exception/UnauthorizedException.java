package com.system.management.utils.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class UnauthorizedException extends RuntimeException {

    private int status = HttpStatus.UNAUTHORIZED.value();

    public UnauthorizedException(Integer status, String message) {
        super(message);
        this.status = status;
    }

    public UnauthorizedException(String message) {
        super(message);
    }
}
