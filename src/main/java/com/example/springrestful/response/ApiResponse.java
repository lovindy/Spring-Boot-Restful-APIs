package com.example.springrestful.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private HttpStatus status;
    private int statusCode;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    // Static factory methods for different response scenarios
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(HttpStatus.OK);
        response.setStatusCode(HttpStatus.OK.value());
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> ApiResponse<T> created(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(HttpStatus.CREATED);
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> ApiResponse<T> notFound(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(HttpStatus.NOT_FOUND);
        response.setStatusCode(HttpStatus.NOT_FOUND.value());
        response.setMessage(message);
        response.setData(null);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }
}
