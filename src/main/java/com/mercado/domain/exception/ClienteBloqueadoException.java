package com.mercado.domain.exception;

public class ClienteBloqueadoException extends RegraDeNegocioException {

    public ClienteBloqueadoException(String message) {
        super(message);
    }

    public ClienteBloqueadoException(String message, Throwable cause) {
        super(message, cause);
    }
}