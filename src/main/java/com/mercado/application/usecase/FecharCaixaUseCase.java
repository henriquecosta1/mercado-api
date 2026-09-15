package com.mercado.application.usecase;

import com.mercado.application.dto.FecharCaixaOutput;
import com.mercado.domain.entity.Caixa;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.CaixaRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Caso de Uso: Fechar o Caixa Aberto.
 */
@ApplicationScoped
public class FecharCaixaUseCase {

    private final CaixaRepository caixaRepository;

    @Inject
    public FecharCaixaUseCase(CaixaRepository caixaRepository) {
        this.caixaRepository = Objects.requireNonNull(caixaRepository, "CaixaRepository é obrigatório.");
    }

    @Transactional
    public FecharCaixaOutput executar(UUID tenantIdUuid) {
        if (tenantIdUuid == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para fechar o caixa.");
        }

        TenantId tenantId = TenantId.de(tenantIdUuid);

        Caixa caixa = caixaRepository.buscarCaixaAberto(tenantId)
            .orElseThrow(() -> new RegraDeNegocioException("Não existe caixa aberto para o mercado especificado."));

        Instant agora = Instant.now();
        caixa.fechar(agora);
        caixaRepository.atualizar(caixa);

        return new FecharCaixaOutput(
            caixa.getId(),
            caixa.getSaldoDinheiro().valor(),
            agora
        );
    }
}
