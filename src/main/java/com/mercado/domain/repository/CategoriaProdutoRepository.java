package com.mercado.domain.repository;

import com.mercado.domain.entity.CategoriaProduto;
import com.mercado.domain.valueobject.TenantId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Interface de repositório para operações com CategoriaProduto (DIP - Clean Architecture).
 */
public interface CategoriaProdutoRepository {

    void salvar(CategoriaProduto categoria);

    List<CategoriaProduto> listarPorTenant(TenantId tenantId);

    Optional<CategoriaProduto> buscarPorId(UUID id, TenantId tenantId);

    Optional<CategoriaProduto> buscarPorNome(String nome, TenantId tenantId);

    boolean existeVinculoComProduto(UUID categoriaId, TenantId tenantId);

    void excluir(UUID id, TenantId tenantId);
}
