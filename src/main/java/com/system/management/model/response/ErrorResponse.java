package com.system.management.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class ErrorResponse {
    private Integer status;
    private String message;
    private String path;
    private String timestamp;

    public ErrorResponse(Integer status, String error, String path) {
        this.status = status;
        this.message = error;
        this.path = path;
        this.timestamp = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date());
    }
}
