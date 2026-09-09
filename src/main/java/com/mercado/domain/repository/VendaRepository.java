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

    default PageResult<Venda> listarPorCaixaPaginado(TenantId tenantId, UUID caixaId, int page, int size) {
        List<Venda> todas = listarPorCaixa(tenantId, caixaId);
        int fromIndex = Math.min(page * size, todas.size());
        int toIndex = Math.min(fromIndex + size, todas.size());
        List<Venda> subList = fromIndex <= toIndex ? todas.subList(fromIndex, toIndex) : Collections.emptyList();
        return new PageResult<>(subList, page, size, todas.size());
    }

    default PageResult<Venda> listarVendasPaginado(TenantId tenantId, UUID caixaId, String status, java.time.Instant de, java.time.Instant ate, int page, int size) {
        List<Venda> todas = caixaId != null ? listarPorCaixa(tenantId, caixaId) : Collections.emptyList();
        int fromIndex = Math.min(page * size, todas.size());
        int toIndex = Math.min(fromIndex + size, todas.size());
        List<Venda> subList = fromIndex <= toIndex ? todas.subList(fromIndex, toIndex) : Collections.emptyList();
        return new PageResult<>(subList, page, size, todas.size());
    }

    default List<Venda> listarFiadoPorCliente(UUID clienteId, TenantId tenantId) {
        return Collections.emptyList();
    }
}
