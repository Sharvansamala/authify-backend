package com.sharvan.authify.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
public class ApiError {
    private LocalDateTime timeStamp;
    private String error;
    private HttpStatus statusCode;
    ApiError(){
        this.timeStamp = LocalDateTime.now();
    }
    ApiError(String error,HttpStatus statusCode){
        this();
        this.error=error;
        this.statusCode=statusCode;
    }
}
