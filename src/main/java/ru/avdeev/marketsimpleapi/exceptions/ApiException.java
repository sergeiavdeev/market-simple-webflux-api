package ru.avdeev.marketsimpleapi.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ApiException extends RuntimeException{

    @Getter
    protected HttpStatus status = HttpStatus.BAD_REQUEST;

    public ApiException(String message) {
        super(message);
    }
}
