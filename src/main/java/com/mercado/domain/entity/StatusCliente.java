package com.mercado.domain.entity;

public enum StatusCliente {
    ATIVO,
    BLOQUEADO,
    INATIVO;

    public static StatusCliente de(String valor) {
        if (valor == null || valor.isBlank()) {
            return ATIVO;
        }
        try {
            return StatusCliente.valueOf(valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Status de cliente invalido: " + valor);
        }
    }
}