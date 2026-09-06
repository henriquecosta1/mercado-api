package com.mercado.application.usecase;

import com.mercado.application.dto.VendaResumoDTO;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Listar todas as vendas do caixa atualmente aberto do tenant.
 * Retorna as vendas ordenadas da mais recente para a mais antiga.
 */
@ApplicationScoped
public class ListarVendasCaixaAtualUseCase {

    private final CaixaRepository caixaRepository;
    private final VendaRepository vendaRepository;

    @Inject
    public ListarVendasCaixaAtualUseCase(CaixaRepository caixaRepository,
                                         VendaRepository vendaRepository) {
        this.caixaRepository = Objects.requireNonNull(caixaRepository, "CaixaRepository é obrigatório.");
        this.vendaRepository = Objects.requireNonNull(vendaRepository, "VendaRepository é obrigatório.");
    }

    public List<VendaResumoDTO> executar(UUID tenantIdUuid) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId é obrigatório para listar vendas do caixa.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);

        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Não existe caixa aberto para o tenant especificado."));

        List<Venda> vendas = vendaRepository.listarPorCaixa(caixa.getId(), tenantId);

        return vendas.stream()
            .map(VendaResumoDTO::from)
            .toList();
    }
}
