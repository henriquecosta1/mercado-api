package com.mercado.domain.exception;

public class RegraDeNegocioException extends DomainException {

    public RegraDeNegocioException(String message) {
        super(message);
    }

    public RegraDeNegocioException(String message, Throwable cause) {
        super(message, cause);
    }
}
