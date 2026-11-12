package com.phong.sochitieu.common;

import org.springframework.http.HttpStatus;

/** Lỗi nghiệp vụ có kèm HTTP status, được GlobalExceptionHandler chuyển thành JSON. */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
