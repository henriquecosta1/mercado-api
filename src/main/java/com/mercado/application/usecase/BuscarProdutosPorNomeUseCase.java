package com.mercado.application.usecase;

import com.mercado.application.dto.ProdutoDTO;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.ProdutoRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Buscar Produtos por Nome no catálogo.
 */
@ApplicationScoped
public class BuscarProdutosPorNomeUseCase {

    private final ProdutoRepository produtoRepository;

    @Inject
    public BuscarProdutosPorNomeUseCase(ProdutoRepository produtoRepository) {
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository é obrigatório.");
    }

    public List<ProdutoDTO> executar(UUID tenantIdUuid, String termo) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para buscar produtos.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);

        if (termo == null || termo.isBlank()) {
            return produtoRepository.listarAtivos(tenantId)
                .stream()
                .map(ProdutoDTO::from)
                .toList();
        }

        return produtoRepository.buscarPorNome(termo.trim(), tenantId)
            .stream()
            .map(ProdutoDTO::from)
            .toList();
    }
}
