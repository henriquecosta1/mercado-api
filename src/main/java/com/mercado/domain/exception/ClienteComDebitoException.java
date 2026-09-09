package com.mercado.domain.exception;

public class ClienteComDebitoException extends RegraDeNegocioException {

    public ClienteComDebitoException() {
        super("Não é possível excluir um cliente que possui saldo devedor pendente.");
    }

    public ClienteComDebitoException(String message) {
        super(message);
    }

    public ClienteComDebitoException(String message, Throwable cause) {
        super(message, cause);
    }
}
