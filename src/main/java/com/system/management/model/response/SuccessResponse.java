package com.system.management.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@JsonInclude(Include.NON_NULL)
public class SuccessResponse<T> {

    private final Integer status = HttpStatus.OK.value();
    private String timestamp;
    private String message;
    private T data;

    public SuccessResponse() {
        this.timestamp = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date());
        this.message = "Thành công";
    }

    public SuccessResponse(T data) {
        this.timestamp = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy").format(new Date());
        this.message = "Thành công";
        this.data = data;
    }
}
