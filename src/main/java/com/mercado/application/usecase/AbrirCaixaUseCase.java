package com.mercado.application.usecase;

import com.mercado.application.dto.AbrirCaixaInput;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Abrir Novo Caixa.
 */
@ApplicationScoped
public class AbrirCaixaUseCase {

    private final CaixaRepository caixaRepository;

    @Inject
    public AbrirCaixaUseCase(CaixaRepository caixaRepository) {
        this.caixaRepository = Objects.requireNonNull(caixaRepository, "CaixaRepository é obrigatório.");
    }

    @Transactional
    public UUID executar(AbrirCaixaInput input) {
        Objects.requireNonNull(input, "Dados de abertura do caixa não podem ser nulos.");

        if (input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para abrir um caixa.");
        }
        if (input.saldoInicial() == null || input.saldoInicial().compareTo(BigDecimal.ZERO) < 0) {
            throw new RegraDeNegocioException("Saldo inicial do caixa não pode ser nulo ou negativo.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());

        if (caixaRepository.buscarCaixaAberto(tenantId).isPresent()) {
            throw new RegraDeNegocioException("Já existe um caixa aberto para o tenant especificado.");
        }

        Caixa caixa = Caixa.abrir(tenantId, Dinheiro.de(input.saldoInicial()));
        caixaRepository.salvar(caixa);

        return caixa.getId();
    }
}
