package com.mercado.api.resource;

import com.mercado.api.dto.AlterarPinRequest;
import com.mercado.api.dto.ValidarPinRequest;
import com.mercado.application.dto.AlterarPinInput;
import com.mercado.application.dto.ValidarPinInput;
import com.mercado.application.usecase.AlterarPinGerenteUseCase;
import com.mercado.application.usecase.ValidarPinGerenteUseCase;
import com.mercado.infrastructure.security.TenantSecurityContext;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Recurso REST para operacoes de Seguranca e Permissoes por PIN de Gerente.
 * Mapeado sob /seguranca.
 * Executa sob Virtual Threads do Java 21 via @RunOnVirtualThread.
 * Requer autenticacao JWT valida (@Authenticated).
 * O tenant_id e extraido diretamente das claims do JWT via TenantSecurityContext.
 */
@Authenticated
@Path("/seguranca")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SegurancaResource {

    private final ValidarPinGerenteUseCase validarPinGerenteUseCase;
    private final AlterarPinGerenteUseCase alterarPinGerenteUseCase;
    private final TenantSecurityContext securityContext;

    @Inject
    public SegurancaResource(ValidarPinGerenteUseCase validarPinGerenteUseCase,
                             AlterarPinGerenteUseCase alterarPinGerenteUseCase,
                             TenantSecurityContext securityContext) {
        this.validarPinGerenteUseCase = Objects.requireNonNull(validarPinGerenteUseCase, "ValidarPinGerenteUseCase e obrigatorio.");
        this.alterarPinGerenteUseCase = Objects.requireNonNull(alterarPinGerenteUseCase, "AlterarPinGerenteUseCase e obrigatorio.");
        this.securityContext = Objects.requireNonNull(securityContext, "TenantSecurityContext e obrigatorio.");
    }

    @POST
    @Path("/validar-pin")
    @RunOnVirtualThread
    public Response validarPin(ValidarPinRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        String pin = request != null ? request.pin() : null;
        validarPinGerenteUseCase.executar(new ValidarPinInput(tenantId, pin));
        return Response.ok(Map.of("valido", true)).build();
    }

    @PUT
    @Path("/alterar-pin")
    @RunOnVirtualThread
    public Response alterarPin(AlterarPinRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisicao nao pode ser vazio.");
        }
        alterarPinGerenteUseCase.executar(new AlterarPinInput(tenantId, request.pinAtual(), request.novoPin()));
        return Response.ok(Map.of("mensagem", "PIN alterado com sucesso.")).build();
    }
}