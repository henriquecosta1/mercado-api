package com.mercado.domain.entity;

public enum FormaPagamento {
    DINHEIRO,
    FIADO,
    PIX,
    CARTAO;

    public static FormaPagamento de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Forma de pagamento não pode ser nula ou vazia.");
        }
        try {
            return FormaPagamento.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Forma de pagamento inválida: '" + valor + "'. Formas válidas: DINHEIRO, FIADO, PIX, CARTAO.");
        }
    }
}
