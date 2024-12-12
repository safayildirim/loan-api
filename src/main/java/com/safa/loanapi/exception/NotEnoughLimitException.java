package com.safa.loanapi.exception;

public class NotEnoughLimitException extends RuntimeException {
    public NotEnoughLimitException(Long id) {
        super(String.format("Customer %d do not have enough credit limit", id));
    }
}
