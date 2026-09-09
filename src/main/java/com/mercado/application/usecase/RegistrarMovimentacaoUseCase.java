package com.mercado.application.usecase;

import com.mercado.application.dto.MovimentacaoInput;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.entity.MovimentacaoCaixa;
import com.mercado.domain.entity.TipoMovimentacao;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.repository.MovimentacaoCaixaRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Registrar Movimentação de Caixa (Sangria ou Suprimento).
 */
@ApplicationScoped
public class RegistrarMovimentacaoUseCase {

    private final CaixaRepository caixaRepository;
    private final MovimentacaoCaixaRepository movimentacaoCaixaRepository;
    private final ValidarPinGerenteUseCase validarPinGerenteUseCase;

    @Inject
    public RegistrarMovimentacaoUseCase(CaixaRepository caixaRepository,
                                       MovimentacaoCaixaRepository movimentacaoCaixaRepository,
                                       ValidarPinGerenteUseCase validarPinGerenteUseCase) {
        this.caixaRepository = Objects.requireNonNull(caixaRepository, "CaixaRepository é obrigatório.");
        this.movimentacaoCaixaRepository = Objects.requireNonNull(movimentacaoCaixaRepository, "MovimentacaoCaixaRepository é obrigatório.");
        this.validarPinGerenteUseCase = validarPinGerenteUseCase;
    }

    public RegistrarMovimentacaoUseCase(CaixaRepository caixaRepository,
                                       MovimentacaoCaixaRepository movimentacaoCaixaRepository) {
        this(caixaRepository, movimentacaoCaixaRepository, null);
    }

    @Transactional
    public UUID executar(MovimentacaoInput input) {
        Objects.requireNonNull(input, "Dados da movimentação não podem ser nulos.");

        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para registrar movimentação de caixa.");
        }
        if (input.tipo() == null || input.tipo().isBlank()) {
            throw new RegraDeNegocioException("Tipo de movimentação é obrigatório.");
        }
        if (input.valor() == null || input.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor da movimentação deve ser estritamente positivo.");
        }
        if (input.motivo() == null || input.motivo().isBlank()) {
            throw new RegraDeNegocioException("Motivo da movimentação é obrigatório.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());
        TipoMovimentacao tipo = TipoMovimentacao.de(input.tipo());
        Dinheiro valor = Dinheiro.de(input.valor());

        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId)
            .orElseThrow(() -> new RegraDeNegocioException("Não existe caixa aberto para o tenant especificado."));

        if (tipo == TipoMovimentacao.SANGRIA) {
            if (validarPinGerenteUseCase != null) {
                validarPinGerenteUseCase.executar(new com.mercado.application.dto.ValidarPinInput(input.tenantId(), input.pin()));
            }
            caixa.realizarSangria(valor, input.motivo().trim());
        } else if (tipo == TipoMovimentacao.SUPRIMENTO) {
            caixa.realizarSuprimento(valor, input.motivo().trim());
        }

        MovimentacaoCaixa mov = MovimentacaoCaixa.criar(
            tenantId,
            caixa.getId(),
            tipo,
            valor,
            input.motivo().trim()
        );

        movimentacaoCaixaRepository.salvar(mov);
        caixaRepository.atualizar(caixa);

        return mov.getId();
    }
}
