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
 * Recurso REST para gestão do Caixa (Resumo, Abertura, Fechamento e Movimentações).
 * Mapeado sob /api/caixas e executando com Virtual Threads do Java 21.
 */
@Path("/api/caixas")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CaixaResource {

    public static final String HEADER_TENANT_ID = "X-Tenant-Id";

    private final ObterResumoCaixaUseCase obterResumoCaixaUseCase;
    private final AbrirCaixaUseCase abrirCaixaUseCase;
    private final RegistrarMovimentacaoUseCase registrarMovimentacaoUseCase;
    private final FecharCaixaUseCase fecharCaixaUseCase;

    @Inject
    public CaixaResource(ObterResumoCaixaUseCase obterResumoCaixaUseCase,
                         AbrirCaixaUseCase abrirCaixaUseCase,
                         RegistrarMovimentacaoUseCase registrarMovimentacaoUseCase,
                         FecharCaixaUseCase fecharCaixaUseCase) {
        this.obterResumoCaixaUseCase = Objects.requireNonNull(obterResumoCaixaUseCase, "ObterResumoCaixaUseCase é obrigatório.");
        this.abrirCaixaUseCase = Objects.requireNonNull(abrirCaixaUseCase, "AbrirCaixaUseCase é obrigatório.");
        this.registrarMovimentacaoUseCase = Objects.requireNonNull(registrarMovimentacaoUseCase, "RegistrarMovimentacaoUseCase é obrigatório.");
        this.fecharCaixaUseCase = Objects.requireNonNull(fecharCaixaUseCase, "FecharCaixaUseCase é obrigatório.");
    }

    @GET
    @Path("/atual")
    @RunOnVirtualThread
    public Response obterResumoAtual(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        ResumoCaixaOutput resumo = obterResumoCaixaUseCase.executar(tenantId);
        return Response.ok(resumo).build();
    }

    @POST
    @Path("/abrir")
    @RunOnVirtualThread
    public Response abrirCaixa(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                               AberturaCaixaRequest request) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        if (request == null || request.saldoInicial() == null) {
            throw new IllegalArgumentException("Saldo inicial é obrigatório para abertura de caixa.");
        }

        UUID caixaId = abrirCaixaUseCase.executar(new AbrirCaixaInput(tenantId, request.saldoInicial()));
        return Response.status(Response.Status.CREATED)
            .entity(Map.of("caixaId", caixaId, "mensagem", "Caixa aberto com sucesso."))
            .build();
    }

    @POST
    @Path("/movimentacoes")
    @RunOnVirtualThread
    public Response registrarMovimentacao(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader,
                                         MovimentacaoRequest request) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisição não pode ser vazio.");
        }

        MovimentacaoInput input = new MovimentacaoInput(
            tenantId,
            request.tipo(),
            request.valor(),
            request.motivo()
        );

        UUID movimentacaoId = registrarMovimentacaoUseCase.executar(input);
        return Response.status(Response.Status.CREATED)
            .entity(Map.of("movimentacaoId", movimentacaoId, "mensagem", "Movimentação registrada com sucesso."))
            .build();
    }

    @POST
    @Path("/fechar")
    @RunOnVirtualThread
    public Response fecharCaixa(@HeaderParam(HEADER_TENANT_ID) String tenantIdHeader) {
        UUID tenantId = extrairTenantId(tenantIdHeader);
        FecharCaixaOutput output = fecharCaixaUseCase.executar(tenantId);
        return Response.ok(output).build();
    }

    private UUID extrairTenantId(String tenantIdHeader) {
        if (tenantIdHeader == null || tenantIdHeader.isBlank()) {
            throw new IllegalArgumentException("O cabeçalho obrigatório '" + HEADER_TENANT_ID + "' não foi informado.");
        }
        try {
            return UUID.fromString(tenantIdHeader.trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Identificador de Tenant ('" + HEADER_TENANT_ID + "') com formato UUID inválido: " + tenantIdHeader);
        }
    }
}
