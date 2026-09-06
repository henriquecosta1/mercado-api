package com.mercado.domain.repository;

import com.mercado.domain.entity.Venda;
import com.mercado.domain.valueobject.TenantId;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface pura de repositório para a entidade Venda (DIP - Dependency Inversion Principle).
 * Livre de qualquer acoplamento com frameworks ou JPA.
 */
public interface VendaRepository {

    void salvar(Venda venda);

    Optional<Venda> buscarPorId(TenantId tenantId, UUID id);

    default Optional<Venda> buscarPorId(UUID id, TenantId tenantId) {
        return buscarPorId(tenantId, id);
    }

    default Optional<Venda> buscarPorIdComItens(UUID vendaId, TenantId tenantId) {
        return buscarPorId(tenantId, vendaId);
    }

    default List<Venda> listarPorCaixa(UUID caixaId, TenantId tenantId) {
        return listarPorCaixa(tenantId, caixaId);
    }

    default List<Venda> listarPorCaixa(TenantId tenantId, UUID caixaId) {
        return Collections.emptyList();
    }
}
