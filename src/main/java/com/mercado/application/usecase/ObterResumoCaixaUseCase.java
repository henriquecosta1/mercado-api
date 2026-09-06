package com.mercado.application.usecase;

import com.mercado.application.dto.ResumoCaixaOutput;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.FormaPagamento;
import com.mercado.domain.entity.MovimentacaoCaixa;
import com.mercado.domain.entity.TipoMovimentacao;
import com.mercado.domain.entity.Venda;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.MovimentacaoCaixaRepository;
import com.mercado.domain.repository.VendaRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Obter Resumo Consolidado do Caixa Aberto.
 * Desconsidera vendas canceladas nos totais financeiros e na contagem.
 */
@ApplicationScoped
public class ObterResumoCaixaUseCase {

    private final CaixaRepository caixaRepository;
    private final VendaRepository vendaRepository;
    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;

    @Inject
    public ObterResumoCaixaUseCase(CaixaRepository caixaRepository,
                                  VendaRepository vendaRepository,
                                  MovimentacaoCaixaRepository movimentacaoCaixaRepository) {
        this.caixaRepository = Objects.requireNonNull(caixaRepository, "CaixaRepository é obrigatório.");
        this.vendaRepository = Objects.requireNonNull(vendaRepository, "VendaRepository é obrigatório.");
        this.movimentacaoCaixaRepository = Objects.requireNonNull(movimentacaoCaixaRepository, "MovimentacaoCaixaRepository é obrigatório.");
    }

    public ResumoCaixaOutput executar(UUID tenantIdUuid) {
        if (tenantIdUuid == null) {
            throw new IllegalArgumentException("TenantId é obrigatório para obter o resumo do caixa.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);

        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Não existe caixa aberto para o tenant especificado."));

        List<Venda> vendas = vendaRepository.listarPorCaixa(tenantId, caixa.getId());
        List<MovimentacaoCaixa> movimentacoes = movimentacaoCaixaRepository.listarPorCaixa(caixa.getId(), tenantId);

        List<Venda> vendasAtivas = vendas.stream()
            .filter(v -> !v.isCancelada())
            .toList();

        BigDecimal totalVendasDinheiro = somarVendasPorForma(vendasAtivas, FormaPagamento.DINHEIRO);
        BigDecimal totalVendasPix = somarVendasPorForma(vendasAtivas, FormaPagamento.PIX);
        BigDecimal totalVendasCartao = somarVendasPorForma(vendasAtivas, FormaPagamento.CARTAO);
        BigDecimal totalVendasFiado = somarVendasPorForma(vendasAtivas, FormaPagamento.FIADO);

        BigDecimal totalSangrias = somarMovimentacoesPorTipo(movimentacoes, TipoMovimentacao.SANGRIA);
        BigDecimal totalSuprimentos = somarMovimentacoesPorTipo(movimentacoes, TipoMovimentacao.SUPRIMENTO);

        return new ResumoCaixaOutput(
            caixa.getId(),
            caixa.getStatus().name(),
            caixa.getAbertoEm(),
            caixa.getSaldoInicial().valor(),
            caixa.getSaldoDinheiro().valor(),
            totalVendasDinheiro,
            totalVendasPix,
            totalVendasCartao,
            totalVendasFiado,
            totalSangrias,
            totalSuprimentos,
            vendasAtivas.size()
        );
    }

    private BigDecimal somarVendasPorForma(List<Venda> vendas, FormaPagamento forma) {
        return vendas.stream()
            .filter(v -> !v.isCancelada() && v.getFormaPagamento() == forma)
            .map(v -> v.getValorTotal().valor())
            .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN), BigDecimal::add);
    }

    private BigDecimal somarMovimentacoesPorTipo(List<MovimentacaoCaixa> movimentacoes, TipoMovimentacao tipo) {
        return movimentacoes.stream()
            .filter(m -> m.getTipo() == tipo)
            .map(m -> m.getValor().valor())
            .reduce(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN), BigDecimal::add);
    }
}
