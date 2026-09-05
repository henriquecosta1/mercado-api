package com.mercado.domain.repository;

import com.mercado.domain.entity.Venda;
import com.mercado.domain.valueobject.TenantId;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface pura de repositório para a entidade Venda (DIP - Dependency Inversion Principle).
 * Livre de qualquer acoplamento com frameworks ou JPA.
 */
public interface VendaRepository {

    void salvar(Venda venda);

    Optional<Venda> buscarPorId(TenantId tenantId, UUID id);
}
