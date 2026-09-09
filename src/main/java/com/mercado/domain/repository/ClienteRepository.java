package com.mercado.domain.repository;

import com.mercado.domain.entity.Cliente;
import com.mercado.domain.valueobject.TenantId;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface pura de repositório para a entidade Cliente (DIP - Dependency Inversion Principle).
 * Livre de qualquer acoplamento com frameworks ou JPA.
 */
public interface ClienteRepository {

    Optional<Cliente> buscarPorId(TenantId tenantId, UUID id);

    Optional<Cliente> buscarPorNomeOuTelefone(TenantId tenantId, String nome, String telefone);

    List<Cliente> listarComSaldoDevedor(TenantId tenantId);

    List<Cliente> buscarPorNome(String nome, TenantId tenantId);

    List<Cliente> listarTodos(TenantId tenantId, String busca, String status, Boolean apenasDevedores);

    default PageResult<Cliente> listarTodosPaginado(TenantId tenantId, String busca, String status, Boolean apenasDevedores, int page, int size) {
        List<Cliente> todos = listarTodos(tenantId, busca, status, apenasDevedores);
        int fromIndex = Math.min(page * size, todos.size());
        int toIndex = Math.min(fromIndex + size, todos.size());
        List<Cliente> subList = fromIndex <= toIndex ? todos.subList(fromIndex, toIndex) : Collections.emptyList();
        return new PageResult<>(subList, page, size, todos.size());
    }

    void excluir(UUID id, TenantId tenantId);

    default BigDecimal somarTotalSaldoDevedor(TenantId tenantId) {
        return listarComSaldoDevedor(tenantId).stream()
            .map(c -> c.getSaldoDevedor().valor())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    void salvar(Cliente cliente);

    void atualizar(Cliente cliente);
}
