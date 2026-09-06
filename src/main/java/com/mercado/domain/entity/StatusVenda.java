package com.mercado.domain.entity;

public enum StatusVenda {
    CONCLUIDA,
    CANCELADA;

    public static StatusVenda de(String valor) {
        if (valor == null || valor.isBlank()) {
            return CONCLUIDA;
        }
        try {
            return StatusVenda.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status de venda inválido: '" + valor + "'. Valores válidos: CONCLUIDA, CANCELADA.");
        }
    }
}
