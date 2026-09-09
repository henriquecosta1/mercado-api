package com.mercado.api.resource;

import com.mercado.api.dto.AberturaCaixaRequest;
import com.mercado.api.dto.MovimentacaoRequest;
import com.mercado.application.dto.AbrirCaixaInput;
import com.mercado.application.dto.FecharCaixaOutput;
import com.mercado.application.dto.MovimentacaoInput;
import com.mercado.application.dto.ResumoCaixaOutput;
import com.mercado.application.usecase.AbrirCaixaUseCase;
import com.mercado.application.usecase.FecharCaixaUseCase;
import com.mercado.application.usecase.ObterResumoCaixaUseCase;
import com.mercado.application.usecase.RegistrarMovimentacaoUseCase;
import com.mercado.infrastructure.security.TenantSecurityContext;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Recurso REST para gestao do Caixa (Resumo, Abertura, Fechamento e Movimentacoes).
 * Mapeado sob /api/caixas e executando com Virtual Threads do Java 21.
 * Requer autenticacao JWT valida (@Authenticated).
 * O tenant_id e extraido diretamente das claims do JWT via TenantSecurityContext.
 */
@Authenticated
@Path("/api/caixas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CaixaResource {

    /** @deprecated Mantido por compatibilidade de integracao; use JWT claims. */
    @Deprecated
    public static final String HEADER_GERENTE_PIN = "X-Gerente-Pin";

    private final ObterResumoCaixaUseCase obterResumoCaixaUseCase;
    private final AbrirCaixaUseCase abrirCaixaUseCase;
    private final RegistrarMovimentacaoUseCase registrarMovimentacaoUseCase;
    private final FecharCaixaUseCase fecharCaixaUseCase;
    private final TenantSecurityContext securityContext;

    @Inject
    public CaixaResource(ObterResumoCaixaUseCase obterResumoCaixaUseCase,
                         AbrirCaixaUseCase abrirCaixaUseCase,
                         RegistrarMovimentacaoUseCase registrarMovimentacaoUseCase,
                         FecharCaixaUseCase fecharCaixaUseCase,
                         TenantSecurityContext securityContext) {
        this.obterResumoCaixaUseCase = Objects.requireNonNull(obterResumoCaixaUseCase, "ObterResumoCaixaUseCase e obrigatorio.");
        this.abrirCaixaUseCase = Objects.requireNonNull(abrirCaixaUseCase, "AbrirCaixaUseCase e obrigatorio.");
        this.registrarMovimentacaoUseCase = Objects.requireNonNull(registrarMovimentacaoUseCase, "RegistrarMovimentacaoUseCase e obrigatorio.");
        this.fecharCaixaUseCase = Objects.requireNonNull(fecharCaixaUseCase, "FecharCaixaUseCase e obrigatorio.");
        this.securityContext = Objects.requireNonNull(securityContext, "TenantSecurityContext e obrigatorio.");
    }

    @GET
    @Path("/atual")
    @RunOnVirtualThread
    public Response obterResumoAtual() {
        UUID tenantId = securityContext.getTenantId().valor();
        ResumoCaixaOutput resumo = obterResumoCaixaUseCase.executar(tenantId);
        return Response.ok(resumo).build();
    }

    @POST
    @Path("/abrir")
    @RunOnVirtualThread
    public Response abrirCaixa(AberturaCaixaRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (request == null || request.saldoInicial() == null) {
            throw new IllegalArgumentException("Saldo inicial e obrigatorio para abertura de caixa.");
        }

        UUID caixaId = abrirCaixaUseCase.executar(new AbrirCaixaInput(tenantId, request.saldoInicial()));
        return Response.status(Response.Status.CREATED)
            .entity(Map.of("caixaId", caixaId, "mensagem", "Caixa aberto com sucesso."))
            .build();
    }

    @POST
    @Path("/movimentacoes")
    @RunOnVirtualThread
    public Response registrarMovimentacao(@HeaderParam(HEADER_GERENTE_PIN) String gerentePin,
                                         MovimentacaoRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisicao nao pode ser vazio.");
        }

        MovimentacaoInput input = new MovimentacaoInput(
            tenantId,
            request.tipo(),
            request.valor(),
            request.motivo(),
            gerentePin
        );

        UUID movimentacaoId = registrarMovimentacaoUseCase.executar(input);
        return Response.status(Response.Status.CREATED)
            .entity(Map.of("movimentacaoId", movimentacaoId, "mensagem", "Movimentacao registrada com sucesso."))
            .build();
    }

    @POST
    @Path("/fechar")
    @RunOnVirtualThread
    public Response fecharCaixa() {
        UUID tenantId = securityContext.getTenantId().valor();
        FecharCaixaOutput output = fecharCaixaUseCase.executar(tenantId);
        return Response.ok(output).build();
    }
}