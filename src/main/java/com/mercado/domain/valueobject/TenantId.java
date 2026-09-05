package com.mercado.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

/**
 * Value Object que encapsula o identificador único do Tenant (Multi-tenancy).
 */
public record TenantId(UUID valor) {

    public TenantId {
        Objects.requireNonNull(valor, "TenantId não pode ser nulo.");
    }

    public static TenantId de(UUID valor) {
        return new TenantId(valor);
    }

    public static TenantId de(String valor) {
        Objects.requireNonNull(valor, "TenantId em texto não pode ser nulo.");
        try {
            return new TenantId(UUID.fromString(valor.trim()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Formato de UUID inválido para TenantId: " + valor, e);
        }
    }

    @Override
    public String toString() {
        return valor.toString();
    }
}
