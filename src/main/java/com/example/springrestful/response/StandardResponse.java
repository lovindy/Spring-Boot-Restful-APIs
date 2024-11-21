package com.example.springrestful.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {
    private int status;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private List<String> errors;

    // Static factory methods
    public static <T> StandardResponse<T> success(T data) {
        return StandardResponse.<T>builder()
                .status(HttpStatus.OK.value())
                .message("Operation successful")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> StandardResponse<T> created(T data) {
        return StandardResponse.<T>builder()
                .status(HttpStatus.CREATED.value())
                .message("Resource created successfully")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> StandardResponse<T> error(String message, HttpStatus status) {
        return StandardResponse.<T>builder()
                .status(status.value())
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> StandardResponse<T> validationError(List<String> errors) {
        return StandardResponse.<T>builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
