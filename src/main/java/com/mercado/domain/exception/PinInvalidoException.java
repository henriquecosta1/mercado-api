package com.mercado.domain.exception;

/**
 * Exceção disparada quando o PIN de gerente fornecido for inválido, ausente ou incorreto.
 * Mapeado para HTTP 403 Forbidden.
 */
public class PinInvalidoException extends DomainException {

    public PinInvalidoException() {
        super("PIN de gerente inválido ou não informado.");
    }

    public PinInvalidoException(String message) {
        super(message);
    }
}
