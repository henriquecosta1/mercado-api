package com.mercado.api.resource;

import com.mercado.api.dto.CadastroComercioRequest;
import com.mercado.api.dto.LoginRequest;
import com.mercado.application.dto.CadastroComercioInput;
import com.mercado.application.dto.CadastroComercioOutput;
import com.mercado.application.dto.LoginInput;
import com.mercado.application.dto.LoginOutput;
import com.mercado.application.usecase.AutenticarUsuarioUseCase;
import com.mercado.application.usecase.CadastrarComercioUseCase;
import com.mercado.domain.entity.Tenant;
import com.mercado.domain.repository.TenantRepository;
import com.mercado.domain.repository.UsuarioRepository;
import com.mercado.domain.valueobject.TenantId;
import com.mercado.infrastructure.security.TenantSecurityContext;
import io.quarkus.security.Authenticated;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.UUID;
import java.util.Objects;

/**
 * Recurso REST de Autenticacao JWT e Auto-cadastro de Estabelecimentos.
 * <ul>
 *   <li>POST /auth/login - publico, emite Bearer token com claims de tenant, perfil e nome do mercado</li>
 *   <li>POST /auth/cadastrar-comercio - publico, auto-cadastro de novos estabelecimentos (status PENDENTE)</li>
 *   <li>GET  /auth/me    - protegido, retorna dados do usuario autenticado e nome do mercado</li>
 * </ul>
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AutenticarUsuarioUseCase autenticarUsuarioUseCase;
    private final CadastrarComercioUseCase cadastrarComercioUseCase;
    private final UsuarioRepository usuarioRepository;
    private final TenantRepository tenantRepository;
    private final TenantSecurityContext securityContext;

    @Inject
    public AuthResource(AutenticarUsuarioUseCase autenticarUsuarioUseCase,
                        CadastrarComercioUseCase cadastrarComercioUseCase,
                        UsuarioRepository usuarioRepository,
                        TenantRepository tenantRepository,
                        TenantSecurityContext securityContext) {
        this.autenticarUsuarioUseCase = Objects.requireNonNull(autenticarUsuarioUseCase, "AutenticarUsuarioUseCase e obrigatorio.");
        this.cadastrarComercioUseCase = Objects.requireNonNull(cadastrarComercioUseCase, "CadastrarComercioUseCase e obrigatorio.");
        this.usuarioRepository = Objects.requireNonNull(usuarioRepository, "UsuarioRepository e obrigatorio.");
        this.tenantRepository = Objects.requireNonNull(tenantRepository, "TenantRepository e obrigatorio.");
        this.securityContext = Objects.requireNonNull(securityContext, "TenantSecurityContext e obrigatorio.");
    }

    /**
     * Endpoint publico de login. Nao requer autenticacao previa.
     * Recebe login, senha e tenantId; retorna Bearer JWT se as credenciais forem validas.
     */
    @POST
    @Path("/login")
    @PermitAll
    @RunOnVirtualThread
        public Response login(@jakarta.ws.rs.HeaderParam("X-Tenant-Id") UUID headerTenantId, LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisicao nao pode ser vazio.");
        }

        UUID tenantId = request.tenantId() != null ? request.tenantId() : headerTenantId;

        LoginInput input = new LoginInput(
            request.login(),
            request.senha(),
            tenantId
        );

        LoginOutput output = autenticarUsuarioUseCase.executar(input);
        return Response.ok(output).build();
    }

    /**
     * Endpoint publico para auto-cadastro de novos comercios/estabelecimentos.
     * Cria o Tenant com status PENDENTE para aprovacao administrativa e registra o primeiro Gerente.
     */
    @POST
    @Path("/cadastrar-comercio")
    @PermitAll
    @RunOnVirtualThread
    public Response cadastrarComercio(CadastroComercioRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("O corpo da requisicao nao pode ser vazio.");
        }

        CadastroComercioInput input = new CadastroComercioInput(
            request.nomeComercio(),
            request.whatsapp(),
            request.nomeGerente(),
            request.login(),
            request.senha(),
            request.pinGerente()
        );

        CadastroComercioOutput output = cadastrarComercioUseCase.executar(input);
        return Response.status(Response.Status.CREATED)
            .entity(output)
            .build();
    }

    /**
     * Endpoint protegido. Retorna os dados do usuario autenticado no token JWT.
     * Requer header: Authorization: Bearer {token}
     */
    @GET
    @Path("/me")
    @Authenticated
    @RunOnVirtualThread
    public Response me() {
        TenantId tenantId = securityContext.getTenantId();
        var usuarioOpt = usuarioRepository.buscarPorId(securityContext.getUsuarioId(), tenantId);

        if (usuarioOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("erro", "Usuario nao encontrado."))
                .build();
        }

        var usuario = usuarioOpt.get();

        String nomeMercado = securityContext.getNomeMercado();
        if (nomeMercado == null || nomeMercado.isBlank()) {
            nomeMercado = tenantRepository.buscarPorId(tenantId)
                .map(Tenant::getNome)
                .orElse("");
        }

        return Response.ok(Map.of(
            "id", usuario.getId(),
            "tenantId", usuario.getTenantId().valor(),
            "nomeMercado", nomeMercado != null ? nomeMercado : "",
            "nome", usuario.getNome(),
            "login", usuario.getLogin(),
            "perfil", usuario.getPerfil().name(),
            "ativo", usuario.isAtivo()
        )).build();
    }
}