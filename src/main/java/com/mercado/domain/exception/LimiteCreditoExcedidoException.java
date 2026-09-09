package com.mercado.domain.exception;

public class LimiteCreditoExcedidoException extends RegraDeNegocioException {

    public LimiteCreditoExcedidoException(String message) {
        super(message);
    }

    public LimiteCreditoExcedidoException(String message, Throwable cause) {
        super(message, cause);
    }
}