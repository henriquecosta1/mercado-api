package com.mercado.api.resource;

import com.mercado.api.dto.RegistrarMercadoRequest;
import com.mercado.application.dto.LoginOutput;
import com.mercado.application.dto.RegistrarMercadoInput;
import com.mercado.application.usecase.OnboardingMercadoUseCase;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Objects;

/**
 * Recurso REST público para Onboarding de novos Mercados.
 * Mapeado sob /onboarding.
 */
@Path("/onboarding")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OnboardingResource {

    private final OnboardingMercadoUseCase onboardingMercadoUseCase;

    @Inject
    public OnboardingResource(OnboardingMercadoUseCase onboardingMercadoUseCase) {
        this.onboardingMercadoUseCase = Objects.requireNonNull(onboardingMercadoUseCase, "OnboardingMercadoUseCase é obrigatório.");
    }

    /**
     * Endpoint público para registro de novo mercado.
     * Cria a loja (Tenant), administrador inicial, categorias padrão e retorna JWT para login automático.
     */
    @POST
    @PermitAll
    @RunOnVirtualThread
    public Response registrarMercado(RegistrarMercadoRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisição não pode ser vazio.");
        }

        RegistrarMercadoInput input = new RegistrarMercadoInput(
            request.nomeMercado(),
            request.nomeResponsavel(),
            request.login(),
            request.senha(),
            request.telefone(),
            request.pinGerente()
        );

        LoginOutput output = onboardingMercadoUseCase.executar(input);
        return Response.status(Response.Status.CREATED)
            .entity(output)
            .build();
    }
}
