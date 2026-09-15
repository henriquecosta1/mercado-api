package com.mercado.api.filter;

import com.mercado.infrastructure.security.RateLimiterService;
import io.vertx.core.http.HttpServerRequest;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Filtro JAX-RS para limitacao de taxa de requisicoes (Rate Limiting) e
 * protecao contra forca bruta (Brute-Force Attack Prevention).
 *
 * Intercepta as requisicoes HTTP antes da execucao dos recursos REST,
 * avalia o IP de origem e consome tokens das cotas gerenciadas pelo RateLimiterService.
 * Retorna HTTP 429 Too Many Requests com cabeçalho "Retry-After: 60" quando excedido.
 */
@Provider
@Priority(Priorities.AUTHENTICATION - 10)
public class RateLimitFilter implements ContainerRequestFilter {

    private final RateLimiterService rateLimiterService;

    @Context
    HttpServerRequest httpServerRequest;

    @Inject
    JsonWebToken jwt;

    @Inject
    public RateLimitFilter(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();

        // Requisicoes CORS Preflight (OPTIONS) nao consomem cotas de rate limit
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return;
        }

        String rawPath = requestContext.getUriInfo().getPath();
        String path = normalizarPath(rawPath);

        // Ignorar endpoints internos/dev do Quarkus e documentacao OpenAPI
        if (path.startsWith("/q/") || path.startsWith("/swagger-ui") || path.startsWith("/openapi")) {
            return;
        }

        String ip = extrairIp(requestContext);

        // 1. Endpoint sensivel: Onboarding e Cadastro de Comercio (POST /api/onboarding, /onboarding, /api/auth/cadastrar-comercio, /auth/cadastrar-comercio)
        if ("POST".equalsIgnoreCase(method) && (path.equals("/api/onboarding") || path.equals("/onboarding") || path.equals("/api/auth/cadastrar-comercio") || path.equals("/auth/cadastrar-comercio"))) {
            if (!rateLimiterService.tentarConsumirOnboarding(ip, path)) {
                abortarCom429(requestContext, "Muitas tentativas de cadastro. Aguarde um minuto antes de tentar novamente.");
                return;
            }
            return;
        }

        // 2. Endpoint sensivel: Login / Brute Force (POST /api/auth/login ou /auth/login)
        if ("POST".equalsIgnoreCase(method) && (path.equals("/api/auth/login") || path.equals("/auth/login"))) {
            if (!rateLimiterService.tentarConsumirLogin(ip, path)) {
                abortarCom429(requestContext, "Muitas tentativas de login. Aguarde um minuto para tentar de novo.");
                return;
            }
            return;
        }

        // 3. Endpoint sensivel: Validacao de PIN Gerente (POST /api/seguranca/validar-pin ou /seguranca/validar-pin)
        if ("POST".equalsIgnoreCase(method) && (path.equals("/api/seguranca/validar-pin") || path.equals("/seguranca/validar-pin"))) {
            UUID tenantId = extrairTenantId();
            if (!rateLimiterService.tentarConsumirValidarPin(ip, tenantId)) {
                abortarCom429(requestContext, "Muitas tentativas de validação de PIN. Aguarde um minuto para tentar de novo.");
                return;
            }
            return;
        }

        // 4. Demais rotas da aplicacao (limite volumetrico padrao de 100 req/min)
        if (!rateLimiterService.tentarConsumirPadrao(ip, path)) {
            abortarCom429(requestContext, "Limite de requisições excedido. Aguarde um minuto antes de tentar novamente.");
        }
    }

    /**
     * Extrai o IP real do cliente considerando proxies reversos (Nginx, Cloudflare, Traefik, AWS ALB)
     * e fallback para o endereco remoto do socket TCP Vert.x.
     */
        public String extrairIp(ContainerRequestContext requestContext) {
        if (httpServerRequest != null && httpServerRequest.remoteAddress() != null) {
            String host = httpServerRequest.remoteAddress().host();
            if (isIpValido(host)) {
                return host;
            }
        }

        String xForwardedFor = requestContext.getHeaderString("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            String[] ips = xForwardedFor.split(",");
            String clientIp = ips[0].trim();
            if (isIpValido(clientIp)) {
                return clientIp;
            }
        }

        String xRealIp = requestContext.getHeaderString("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank() && isIpValido(xRealIp.trim())) {
            return xRealIp.trim();
        }

        return "127.0.0.1";
    }

    private boolean isIpValido(String ip) {
        if (ip == null || ip.isBlank()) return false;
        try {
            java.net.InetAddress.getByName(ip);
            return true;
        } catch (java.net.UnknownHostException e) {
            return false;
        }
    }

    /**
     * Extrai o tenant_id do JWT autenticado, se disponivel.
     */
    private UUID extrairTenantId() {
        try {
            if (jwt != null && jwt.getRawToken() != null) {
                String claimTenantId = jwt.getClaim("tenant_id");
                if (claimTenantId != null && !claimTenantId.isBlank()) {
                    return UUID.fromString(claimTenantId.trim());
                }
            }
        } catch (Exception ignored) {
            // Em caso de falha de parsing, segue sem tenantId (chave global)
        }
        return null;
    }

    private String normalizarPath(String path) {
        if (path == null) {
            return "/";
        }
        String normalizado = path.trim();
        if (!normalizado.startsWith("/")) {
            normalizado = "/" + normalizado;
        }
        if (normalizado.length() > 1 && normalizado.endsWith("/")) {
            normalizado = normalizado.substring(0, normalizado.length() - 1);
        }
        return normalizado;
    }

    private void abortarCom429(ContainerRequestContext requestContext, String mensagem) {
        requestContext.abortWith(
            Response.status(429)
                .header("Retry-After", "60")
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("erro", mensagem))
                .build()
        );
    }
}
