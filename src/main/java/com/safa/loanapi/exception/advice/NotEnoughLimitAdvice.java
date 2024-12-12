package com.safa.loanapi.exception.advice;

import com.safa.loanapi.exception.NotEnoughLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class NotEnoughLimitAdvice {
    @ExceptionHandler(NotEnoughLimitException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String notEnoughLimitHandler(NotEnoughLimitException ex) {
        return ex.getMessage();
    }
}