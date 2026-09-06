package com.mercado.domain.entity;

public enum TipoMovimentacao {
    SANGRIA,
    SUPRIMENTO;

    public static TipoMovimentacao de(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Tipo de movimentação não pode ser nulo ou vazio.");
        }
        try {
            return TipoMovimentacao.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de movimentação inválido: '" + valor + "'. Tipos válidos: SANGRIA, SUPRIMENTO.");
        }
    }
}
