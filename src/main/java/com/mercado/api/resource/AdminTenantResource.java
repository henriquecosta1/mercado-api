package com.mercado.api.resource;

import com.mercado.application.dto.AtivarTenantInput;
import com.mercado.application.usecase.GerenciarLicencaTenantUseCase;
import com.mercado.domain.entity.Tenant;
import io.quarkus.security.UnauthorizedException;
import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import org.jboss.logging.Logger;

/**
 * Recurso REST Administrativo para Gestão de Tenants, Ativação de Licença e Bloqueio de Inadimplência.
 * Protegido via chave secreta administrativa no cabeçalho HTTP "X-Admin-Key".
 */
@Path("/api/admin/tenants")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminTenantResource {

    private static final Logger LOG = Logger.getLogger(AdminTenantResource.class);

    private final GerenciarLicencaTenantUseCase gerenciarLicencaTenantUseCase;
    private final String secretKey;

    @Inject
    public AdminTenantResource(
            GerenciarLicencaTenantUseCase gerenciarLicencaTenantUseCase,
            @ConfigProperty(name = "mercado.admin.secret-key", defaultValue = "") String secretKey) {
        this.gerenciarLicencaTenantUseCase = Objects.requireNonNull(gerenciarLicencaTenantUseCase, "GerenciarLicencaTenantUseCase é obrigatório.");
        this.secretKey = secretKey;
    }

    /**
     * Ativa ou renova a licença de um estabelecimento por X dias.
     */
    @PUT
    @Path("/{id}/ativar")
    @RunOnVirtualThread
    public Response ativarTenant(@HeaderParam("X-Admin-Key") String adminKey,
                                @PathParam("id") UUID id,
                                AtivarTenantInput input) {
        validarChaveAdmin(adminKey);

        int dias = (input != null && input.diasValidade() > 0) ? input.diasValidade() : 30;
        Tenant tenant = gerenciarLicencaTenantUseCase.ativar(id, dias);

        return Response.ok(Map.of(
            "id", tenant.getId().valor(),
            "nome", tenant.getNome(),
            "status", tenant.getStatus().name(),
            "dataExpiracaoLicenca", tenant.getDataExpiracaoLicenca() != null ? tenant.getDataExpiracaoLicenca().toString() : "",
            "mensagem", "Estabelecimento ativado com sucesso por " + dias + " dias."
        )).build();
    }

    /**
     * Suspende o acesso de um estabelecimento (bloqueio administrativo ou inadimplência).
     */
    @PUT
    @Path("/{id}/suspender")
    @RunOnVirtualThread
    public Response suspenderTenant(@HeaderParam("X-Admin-Key") String adminKey,
                                   @PathParam("id") UUID id) {
        validarChaveAdmin(adminKey);

        Tenant tenant = gerenciarLicencaTenantUseCase.suspender(id);

        return Response.ok(Map.of(
            "id", tenant.getId().valor(),
            "nome", tenant.getNome(),
            "status", tenant.getStatus().name(),
            "mensagem", "Estabelecimento suspenso com sucesso."
        )).build();
    }

    /**
     * Lista todos os estabelecimentos cadastrados e o status de suas licenças.
     */
    @GET
    @RunOnVirtualThread
    public Response listarTodos(@HeaderParam("X-Admin-Key") String adminKey) {
        validarChaveAdmin(adminKey);

        List<Tenant> tenants = gerenciarLicencaTenantUseCase.listarTodos();

        List<Map<String, Object>> response = tenants.stream()
            .map(t -> Map.<String, Object>of(
                "id", t.getId().valor(),
                "nome", t.getNome(),
                "whatsapp", t.getWhatsapp() != null ? t.getWhatsapp() : "",
                "status", t.getStatus().name(),
                "dataExpiracaoLicenca", t.getDataExpiracaoLicenca() != null ? t.getDataExpiracaoLicenca().toString() : "",
                "isAtivo", t.isAtivo(),
                "isPendente", t.isPendente(),
                "isVencido", t.isVencido()
            ))
            .toList();

        return Response.ok(response).build();
    }
    private void validarChaveAdmin(String adminKey) {
        if (this.secretKey == null || this.secretKey.isBlank() || this.secretKey.trim().length() < 16) {
            LOG.warn("ALERTA DE SEGURANCA: Tentativa de acesso a rota administrativa, mas a chave secreta mercado.admin.secret-key nao esta configurada corretamente ou e muito curta (minimo 16 caracteres). Fail-Close ativado.");
            throw new UnauthorizedException("Acesso administrativo bloqueado por seguranca (chave ausente ou invalida no servidor).");
        }

        if (adminKey == null || adminKey.isBlank()) {
            throw new UnauthorizedException("Chave de administracao ausente no cabecalho X-Admin-Key.");
        }

        byte[] chaveEsperada = this.secretKey.getBytes(StandardCharsets.UTF_8);
        byte[] chaveRecebida = adminKey.trim().getBytes(StandardCharsets.UTF_8);

        boolean autorizada = MessageDigest.isEqual(chaveEsperada, chaveRecebida);

        if (!autorizada) {
            throw new UnauthorizedException("Chave de administracao invalida.");
        }
    }
}