package com.safa.loanapi.exception;

public class LoanNotFoundException extends RuntimeException {
    public LoanNotFoundException(Long id) {
        super(String.format("Loan with id %d not found", id));
    }
}
