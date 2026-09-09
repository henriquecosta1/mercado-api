package com.mercado.application.usecase;

import com.mercado.application.dto.ValidarPinInput;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.valueobject.TenantId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Objects;

/**
 * Caso de Uso: Validar PIN de Gerente.
 * Lança PinInvalidoException (HTTP 403) caso o PIN informado esteja incorreto ou ausente.
 */
@ApplicationScoped
public class ValidarPinGerenteUseCase {

    private final TenantRepository tenantRepository;

    @Inject
    public ValidarPinGerenteUseCase(TenantRepository tenantRepository) {
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository é obrigatório.");
    }

    public void executar(ValidarPinInput input) {
        if (input == null || input.tenantId() == null) {
            throw new RegraDeNegocioException("TenantId é obrigatório para validação de PIN.");
        }

        TenantId tenantId = TenantId.de(input.tenantId());
        Tenant tenant = tenantRepository.buscarPorId(tenantId)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Tenant não encontrado com id: " + input.tenantId()));

        tenant.validarPin(input.pin());
    }
}
