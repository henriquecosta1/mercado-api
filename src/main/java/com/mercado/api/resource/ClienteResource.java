package com.mercado.api.resource;

import com.mercado.api.dto.AmortizacaoRequest;
import com.mercado.api.dto.BloquearClienteRequest;
import com.mercado.api.dto.SalvarClienteRequest;
import com.mercado.application.dto.*;
import com.mercado.application.usecase.*;
import com.mercado.domain.entity.Cliente;
import com.mercado.domain.entity.StatusCliente;
import com.mercado.domain.exception.RecursoNaoEncontradoException;
import com.mercado.domain.repository.ClienteRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.security.TenantSecurityContext;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Recurso REST para operacoes relacionadas a Clientes, Analise de Risco e Fiado.
 * Mapeado sob o prefixo /api/clientes.
 * Executa em Virtual Threads do Java 21 via @RunOnVirtualThread.
 * Requer autenticacao JWT valida (@Authenticated).
 * O tenant_id e extraido diretamente das claims do JWT via TenantSecurityContext.
 */
@Authenticated
@Path("/api/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClienteResource {

    public static final String HEADER_GERENTE_PIN = "X-Gerente-Pin";

    private final ListarClientesUseCase listarClientesUseCase;
    private final ListarClientesFiadoUseCase listarClientesFiadoUseCase;
    private final ExcluirClienteUseCase excluirClienteUseCase;
    private final AmortizarFiadoUseCase amortizarFiadoUseCase;
    private final ObterExtratoClienteUseCase obterExtratoClienteUseCase;
    private final SalvarClienteUseCase salvarClienteUseCase;
    private final AlterarStatusClienteUseCase alterarStatusClienteUseCase;
    private final ClienteRepository clienteRepository;
    private final TenantSecurityContext securityContext;

    @Inject
    public ClienteResource(ListarClientesUseCase listarClientesUseCase,
                           ListarClientesFiadoUseCase listarClientesFiadoUseCase,
                           ExcluirClienteUseCase excluirClienteUseCase,
                           AmortizarFiadoUseCase amortizarFiadoUseCase,
                           ObterExtratoClienteUseCase obterExtratoClienteUseCase,
                           SalvarClienteUseCase salvarClienteUseCase,
                           AlterarStatusClienteUseCase alterarStatusClienteUseCase,
                           ClienteRepository clienteRepository,
                           TenantSecurityContext securityContext) {
        this.listarClientesUseCase = Objects.requireNonNull(listarClientesUseCase, "ListarClientesUseCase e obrigatorio.");
        this.listarClientesFiadoUseCase = Objects.requireNonNull(listarClientesFiadoUseCase, "ListarClientesFiadoUseCase e obrigatorio.");
        this.excluirClienteUseCase = Objects.requireNonNull(excluirClienteUseCase, "ExcluirClienteUseCase e obrigatorio.");
        this.amortizarFiadoUseCase = Objects.requireNonNull(amortizarFiadoUseCase, "AmortizarFiadoUseCase e obrigatorio.");
        this.obterExtratoClienteUseCase = Objects.requireNonNull(obterExtratoClienteUseCase, "ObterExtratoClienteUseCase e obrigatorio.");
        this.salvarClienteUseCase = Objects.requireNonNull(salvarClienteUseCase, "SalvarClienteUseCase e obrigatorio.");
        this.alterarStatusClienteUseCase = Objects.requireNonNull(alterarStatusClienteUseCase, "AlterarStatusClienteUseCase e obrigatorio.");
        this.clienteRepository = Objects.requireNonNull(clienteRepository, "ClienteRepository e obrigatorio.");
        this.securityContext = Objects.requireNonNull(securityContext, "TenantSecurityContext e obrigatorio.");
    }

    @GET
    @RunOnVirtualThread
    public Response listarClientes(@QueryParam("busca") String busca,
                                   @QueryParam("status") String status,
                                   @QueryParam("apenasDevedores") @DefaultValue("false") boolean apenasDevedores,
                                   @QueryParam("page") Integer page,
                                   @QueryParam("size") Integer size) {
        UUID tenantId = securityContext.getTenantId().valor();

        if (page != null || size != null) {
            int pagina = page != null ? page : 0;
            int tamanho = size != null ? size : 10;
            PageDTO<ClienteDTO> paginado = listarClientesUseCase.executarPaginado(
                tenantId, busca, status, apenasDevedores, pagina, tamanho
            );
            return Response.ok(paginado).build();
        }

        List<ClienteDTO> clientes = listarClientesUseCase.executar(tenantId, busca, status, apenasDevedores);
        return Response.ok(clientes).build();
    }

    @DELETE
    @Path("/{id}")
    @RunOnVirtualThread
    public Response excluirCliente(@PathParam("id") UUID id,
                                   @HeaderParam(HEADER_GERENTE_PIN) String gerentePin) {
        UUID tenantId = securityContext.getTenantId().valor();
        excluirClienteUseCase.executar(tenantId, id, gerentePin);
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}")
    @RunOnVirtualThread
    public Response buscarPorId(@PathParam("id") UUID id) {
        TenantId tenantId = securityContext.getTenantId();
        Cliente cliente = clienteRepository.buscarPorId(tenantId, id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado com id: " + id));

        return Response.ok(ClienteDetalhadoDTO.from(cliente)).build();
    }

    @POST
    @RunOnVirtualThread
    public Response cadastrarCliente(SalvarClienteRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisicao nao pode ser vazio.");
        }

        SalvarClienteInput input = new SalvarClienteInput(
            tenantId,
            null,
            request.nome(),
            request.apelido(),
            request.telefone(),
            request.cpf(),
            request.endereco(),
            request.pontoReferencia(),
            request.limiteCredito(),
            request.diaVencimento(),
            request.observacoes()
        );

        ClienteDetalhadoDTO clienteCriado = salvarClienteUseCase.executar(input);
        return Response.status(Response.Status.CREATED)
            .entity(clienteCriado)
            .build();
    }

    @PUT
    @Path("/{id}")
    @RunOnVirtualThread
    public Response atualizarCliente(@PathParam("id") UUID id,
                                     SalvarClienteRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        if (request == null) {
            throw new IllegalArgumentException("Corpo da requisicao nao pode ser vazio.");
        }

        SalvarClienteInput input = new SalvarClienteInput(
            tenantId,
            id,
            request.nome(),
            request.apelido(),
            request.telefone(),
            request.cpf(),
            request.endereco(),
            request.pontoReferencia(),
            request.limiteCredito(),
            request.diaVencimento(),
            request.observacoes()
        );

        ClienteDetalhadoDTO clienteAtualizado = salvarClienteUseCase.executar(input);
        return Response.ok(clienteAtualizado).build();
    }

    @PATCH
    @Path("/{id}/status")
    @RunOnVirtualThread
    public Response alterarStatus(@PathParam("id") UUID id,
                                  BloquearClienteRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();
        StatusCliente novoStatus = (request != null && request.status() != null && !request.status().isBlank())
            ? StatusCliente.de(request.status())
            : StatusCliente.BLOQUEADO;

        String motivo = request != null ? request.motivo() : null;

        AlterarStatusClienteInput input = new AlterarStatusClienteInput(
            tenantId,
            id,
            novoStatus,
            motivo
        );

        ClienteDetalhadoDTO clienteAtualizado = alterarStatusClienteUseCase.executar(input);
        return Response.ok(clienteAtualizado).build();
    }

    @GET
    @Path("/{id}/extrato")
    @RunOnVirtualThread
    public Response obterExtrato(@PathParam("id") UUID clienteId) {
        UUID tenantId = securityContext.getTenantId().valor();
        ExtratoClienteOutput extrato = obterExtratoClienteUseCase.executar(tenantId, clienteId);
        return Response.ok(extrato).build();
    }

    @POST
    @Path("/{id}/amortizacoes")
    @RunOnVirtualThread
    public Response amortizarFiado(@PathParam("id") UUID clienteId,
                                   AmortizacaoRequest request) {
        UUID tenantId = securityContext.getTenantId().valor();

        if (clienteId == null) {
            throw new IllegalArgumentException("Identificador do cliente e obrigatorio na URL.");
        }
        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisicao nao pode ser vazio.");
        }

        AmortizarFiadoInput input = new AmortizarFiadoInput(
            tenantId,
            clienteId,
            request.valorPago(),
            request.formaPagamento()
        );

        AmortizarFiadoOutput output = amortizarFiadoUseCase.executar(input);
        return Response.ok(output).build();
    }
}