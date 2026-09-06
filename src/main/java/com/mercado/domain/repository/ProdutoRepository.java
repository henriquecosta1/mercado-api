package com.mercado.domain.repository;

import com.mercado.domain.entity.Produto;
import com.mercado.domain.valueobject.TenantId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para a entidade Produto (DIP - Dependency Inversion Principle).
 */
public interface ProdutoRepository {

    void salvar(Produto produto);

    Optional<Produto> buscarPorId(UUID id, TenantId tenantId);

    List<Produto> buscarPorNome(String termo, TenantId tenantId);

    List<Produto> listarAtivos(TenantId tenantId);
}
