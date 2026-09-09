package com.mercado.application.usecase;

import com.mercado.application.dto.PageDTO;
import com.mercado.application.dto.VendaResumoDTO;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.repository.PageResult;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Listar histórico de vendas com filtros e paginação Server-Side.
 */
@ApplicationScoped
public class ListarVendasUseCase {

    private final VendaRepository vendaRepository;

    @Inject
    public ListarVendasUseCase(VendaRepository vendaRepository) {
        this.vendaRepository = Objects.requireNonNull(vendaRepository, "VendaRepository é obrigatório.");
    }

    public PageDTO<VendaResumoDTO> executar(UUID tenantIdUuid, UUID caixaId, String status, Instant de, Instant ate, int page, int size) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId é obrigatório para listar vendas.");
        }

        int paginaEfetiva = Math.max(0, page);
        int tamanhoEfetivo = Math.min(Math.max(1, size), 100);

        TenantId tenantId = TenantId.de(tenantIdUuid);
        PageResult<Venda> pageResult = vendaRepository.listarVendasPaginado(
            tenantId, caixaId, status, de, ate, paginaEfetiva, tamanhoEfetivo
        );

        List<VendaResumoDTO> dtos = pageResult.content().stream()
            .map(VendaResumoDTO::from)
            .toList();

        return PageDTO.of(dtos, pageResult.page(), pageResult.size(), pageResult.totalElements());
    }
}
