package com.mercado.application.usecase;

import com.mercado.application.dto.AlterarPinInput;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.valueobject.PinGerente;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.util.Objects;

/**
 * Caso de Uso: Alterar PIN de Gerente.
 * Valida o PIN atual e define o novo PIN após validação de formato numérico de 4 a 6 dígitos.
 */
@ApplicationScoped
public class AlterarPinGerenteUseCase {

    private final TenantRepository tenantRepository;

    @Inject
    public AlterarPinGerenteUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository é obrigatório.");
    }

    @Transactional
    public void executar(AlterarPinInput input) {
        if (input == null || input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para alteração de PIN.");
        }
        PinGerente.validarFormato(input.novoPin());

        TenantId tenantId = TenantId.de(input.tenantId());
        Tenant tenant = tenantRepository.buscarPorId(tenantId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Tenant não encontrado com id: " + input.tenantId()));

        tenant.alterarPin(input.pinAtual(), input.novoPin());
        tenantRepository.atualizar(tenant);
    }
}
