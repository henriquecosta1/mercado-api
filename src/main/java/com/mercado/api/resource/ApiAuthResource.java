package com.mercado.api.resource;

import com.mercado.application.usecase.AutenticarUsuarioUseCase;
import com.mercado.domain.repository.UsuarioRepository;
import com.mercado.infrastructure.security.TenantSecurityContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

/**
 * Alias REST mapeado sob /api/auth, mantendo compatibilidade com frontends
 * que utilizam o prefixo /api (ex: Vite proxy).
 */
@Path("/api/auth")
public class ApiAuthResource extends AuthResource {

    @Inject
    public ApiAuthResource(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                           UsuarioRepository usuarioRepository,
                           TenantSecurityContext securityContext) {
        super(autenticarUsuarioUseCase, usuarioRepository, securityContext);
    }
}