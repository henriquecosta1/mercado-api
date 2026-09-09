package com.mercado.api.resource;

import com.mercado.application.usecase.OnboardingMercadoUseCase;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;

/**
 * Alias REST mapeado sob /api/onboarding para garantir compatibilidade com clientes
 * que utilizam o prefixo /api (ex: Vite proxy).
 */
@Path("/api/onboarding")
public class ApiOnboardingResource extends OnboardingResource {

    @Inject
    public ApiOnboardingResource(OnboardingMercadoUseCase onboardingMercadoUseCase) {
        super(onboardingMercadoUseCase);
    }
}
