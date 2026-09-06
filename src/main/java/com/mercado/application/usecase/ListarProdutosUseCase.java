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
 * Caso de Uso: Listar Produtos do catálogo com filtro opcional por nome.
 */
@ApplicationScoped
public class ListarProdutosUseCase {

    private final ProdutoRepository produtoRepository;

    @Inject
    public ListarProdutosUseCase(ProdutoRepository produtoRepository) {
        this.produtoRepository = Objects.requireNonNull(produtoRepository, "ProdutoRepository é obrigatório.");
    }

    public List<ProdutoDTO> executar(UUID tenantIdUuid, String busca) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para listar produtos.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);

        if (busca != null && !busca.isBlank()) {
            return produtoRepository.buscarPorNome(busca.trim(), tenantId)
                .stream()
                .map(ProdutoDTO::from)
                .toList();
        }

        return produtoRepository.listarAtivos(tenantId)
            .stream()
            .map(ProdutoDTO::from)
            .toList();
    }
}
