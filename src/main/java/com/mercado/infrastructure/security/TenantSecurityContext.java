package com.mercado.infrastructure.security;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.security.UnauthorizedException;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.UUID;

/**
 * Utilitario de escopo de requisicao que extrai claims do JWT autenticado.
 * Injeta o {@link JsonWebToken} fornecido pelo SmallRye JWT e disponibiliza
 * metodos tipados para acessar tenant_id, sub e perfil sem boilerplate nos Resources.
 *
 * Uso nos Resources:
 * <pre>
 *   &#64;Inject TenantSecurityContext securityContext;
 *   UUID tenantId = securityContext.getTenantId().valor();
 * </pre>
 */
@RequestScoped
public class TenantSecurityContext {

    private final JsonWebToken jwt;

    @Inject
    public TenantSecurityContext(JsonWebToken jwt) {
        this.jwt = jwt;
    }

    /**
     * Extrai o TenantId da claim "tenant_id" do JWT autenticado.
     *
     * @return TenantId do usuario autenticado
     * @throws UnauthorizedException se a claim estiver ausente ou invalida
     */
    public TenantId getTenantId() {
        String tenantIdStr = jwt.getClaim("tenant_id");
        if (tenantIdStr == null || tenantIdStr.isBlank()) {
            throw new UnauthorizedException("Claim 'tenant_id' ausente no token JWT.");
        }
        try {
            return TenantId.de(UUID.fromString(tenantIdStr.trim()));
        } catch (IllegalArgumentException e) {
            throw new RegraDeNegocioException("Claim 'tenant_id' contem UUID invalido: " + tenantIdStr);
        }
    }

    /**
     * Extrai o UUID do usuario autenticado a partir da claim "sub".
     *
     * @return UUID do usuario
     */
    public UUID getUsuarioId() {
        String sub = jwt.getSubject();
        if (sub == null || sub.isBlank()) {
            throw new UnauthorizedException("Claim 'sub' ausente no token JWT.");
        }
        return UUID.fromString(sub.trim());
    }

    /**
     * Retorna o login (UPN) do usuario autenticado.
     *
     * @return login do usuario
     */
    public String getLogin() {
        return jwt.getName();
    }

    /**
     * Retorna o nome completo do usuario autenticado a partir da claim "nome".
     *
     * @return nome do usuario
     */
    public String getNome() {
        return jwt.getClaim("nome");
    }

    /**
     * Retorna o nome do mercado/estabelecimento a partir da claim "nome_mercado".
     *
     * @return nome do mercado/tenant ou null se ausente
     */
    public String getNomeMercado() {
        return jwt.getClaim("nome_mercado");
    }

    /**
     * Verifica se o usuario autenticado possui o perfil de GERENTE.
     *
     * @return true se o usuario for GERENTE
     */
    public boolean isGerente() {
        return jwt.getGroups() != null && jwt.getGroups().contains("GERENTE");
    }

    /**
     * Retorna o JWT bruto para casos de necessidade avancada.
     */
    public JsonWebToken getJwt() {
        return jwt;
    }
}