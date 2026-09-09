package com.mercado.api.resource;

import com.mercado.application.usecase.AlterarPinGerenteUseCase;
import com.mercado.application.usecase.ValidarPinGerenteUseCase;
import com.mercado.infrastructure.security.TenantSecurityContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

/**
 * Alias REST mapeado sob /api/seguranca, mantendo total compatibilidade
 * com clientes frontend que utilizam o prefixo de rota /api.
 */
@Path("/api/seguranca")
public class ApiSegurancaResource extends SegurancaResource {

    @Inject
    public ApiSegurancaResource(ValidarPinGerenteUseCase validarPinGerenteUseCase,
                                AlterarPinGerenteUseCase alterarPinGerenteUseCase,
                                TenantSecurityContext securityContext) {
        super(validarPinGerenteUseCase, alterarPinGerenteUseCase, securityContext);
    }
}