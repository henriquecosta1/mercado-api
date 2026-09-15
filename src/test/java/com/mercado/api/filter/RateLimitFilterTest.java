package com.mercado.api.filter;

import com.mercado.infrastructure.security.RateLimiterService;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Cookie;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Testes de Filtro: RateLimitFilter")
class RateLimitFilterTest {

    private RateLimiterService rateLimiterService;
    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
        filter = new RateLimitFilter(rateLimiterService);
    }

    @Test
    @DisplayName("Deve ignorar requisicoes OPTIONS (CORS preflight)")
    void deveIgnorarOptions() throws IOException {
        FakeContainerRequestContext context = new FakeContainerRequestContext("OPTIONS", "/api/auth/login");
        filter.filter(context);

        assertNull(context.getAbortedWith(), "Requisicao OPTIONS nao deve ser abortada");
    }

    @Test
    @DisplayName("Deve ignorar rotas internas do Quarkus e OpenAPI")
    void deveIgnorarRotasDevOuOpenApi() throws IOException {
        FakeContainerRequestContext context = new FakeContainerRequestContext("GET", "/q/health");
        filter.filter(context);

        assertNull(context.getAbortedWith(), "Rota /q/health nao deve ser abortada");
    }

    @Test
    @DisplayName("Deve bloquear login excedente com HTTP 429 e mensagem amigavel")
    void deveBloquearLoginExcedente() throws IOException {
        String ip = "189.20.30.40";

        // Consumir as 5 requisições permitidas
        for (int i = 0; i < 5; i++) {
            FakeContainerRequestContext ctx = new FakeContainerRequestContext("POST", "/api/auth/login");
            ctx.getHeaders().add("X-Forwarded-For", ip + ", 10.0.0.1");
            filter.filter(ctx);
            assertNull(ctx.getAbortedWith(), "Tentativa " + (i + 1) + " deve passar");
        }

        // 6ª requisição deve disparar 429
        FakeContainerRequestContext ctxExcedente = new FakeContainerRequestContext("POST", "/api/auth/login");
        ctxExcedente.getHeaders().add("X-Forwarded-For", ip + ", 10.0.0.1");
        filter.filter(ctxExcedente);

        Response response = ctxExcedente.getAbortedWith();
        assertNotNull(response, "6ª tentativa deve ser abortada");
        assertEquals(429, response.getStatus());
        assertEquals("60", response.getHeaderString("Retry-After"));
        assertTrue(response.getEntity() instanceof Map);
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertEquals("Muitas tentativas de login. Aguarde um minuto para tentar de novo.", entity.get("erro"));
    }

    @Test
    @DisplayName("Deve bloquear onboarding excedente com HTTP 429 e mensagem de cadastro")
    void deveBloquearOnboardingExcedente() throws IOException {
        String ip = "187.100.200.10";

        // Consumir 3 permitidos
        for (int i = 0; i < 3; i++) {
            FakeContainerRequestContext ctx = new FakeContainerRequestContext("POST", "/onboarding");
            ctx.getHeaders().add("X-Real-IP", ip);
            filter.filter(ctx);
            assertNull(ctx.getAbortedWith(), "Tentativa " + (i + 1) + " de onboarding deve passar");
        }

        // 4ª requisição aborta com 429
        FakeContainerRequestContext ctxExcedente = new FakeContainerRequestContext("POST", "/onboarding");
        ctxExcedente.getHeaders().add("X-Real-IP", ip);
        filter.filter(ctxExcedente);

        Response response = ctxExcedente.getAbortedWith();
        assertNotNull(response, "4ª tentativa deve ser abortada com 429");
        assertEquals(429, response.getStatus());
        assertEquals("60", response.getHeaderString("Retry-After"));
        Map<?, ?> entity = (Map<?, ?>) response.getEntity();
        assertEquals("Muitas tentativas de cadastro. Aguarde um minuto antes de tentar novamente.", entity.get("erro"));
    }

    @Test
    @DisplayName("Deve extrair o primeiro IP de X-Forwarded-For com multiplos proxies")
    void deveExtrairPrimeiroIpDeXForwardedFor() {
        FakeContainerRequestContext ctx = new FakeContainerRequestContext("GET", "/clientes");
        ctx.getHeaders().add("X-Forwarded-For", " 201.55.66.77 , 10.0.0.2, 172.16.0.1 ");

        String ip = filter.extrairIp(ctx);
        assertEquals("201.55.66.77", ip);
    }

    @Test
    @DisplayName("Deve extrair X-Real-IP quando X-Forwarded-For estiver ausente")
    void deveExtrairXRealIp() {
        FakeContainerRequestContext ctx = new FakeContainerRequestContext("GET", "/produtos");
        ctx.getHeaders().add("X-Real-IP", " 177.12.34.56 ");

        String ip = filter.extrairIp(ctx);
        assertEquals("177.12.34.56", ip);
    }

    @Test
    @DisplayName("Deve usar fallback para 127.0.0.1 quando headers e socket forem nulos")
    void deveUsarFallbackIp() {
        FakeContainerRequestContext ctx = new FakeContainerRequestContext("GET", "/produtos");

        String ip = filter.extrairIp(ctx);
        assertEquals("127.0.0.1", ip);
    }

        

        

            @Test
    @DisplayName("Deve ignorar IP malicioso ou invalido no X-Forwarded-For e usar fallback")
    void deveIgnorarIpInvalido() {
        FakeContainerRequestContext ctx = new FakeContainerRequestContext("GET", "/produtos");
        ctx.getHeaders().add("X-Forwarded-For", " drop table usuarios; , 10.0.0.2");
        ctx.getHeaders().add("X-Real-IP", "177.12.34.56");

        String ip = filter.extrairIp(ctx);
        assertEquals("177.12.34.56", ip);
    }

    @Test
    @DisplayName("Deve ignorar X-Real-IP invalido e cair no socket/localhost")
    void deveIgnorarXRealIpInvalido() {
        FakeContainerRequestContext ctx = new FakeContainerRequestContext("GET", "/produtos");
        ctx.getHeaders().add("X-Real-IP", "invalid_ip");

        String ip = filter.extrairIp(ctx);
        assertEquals("127.0.0.1", ip); // Fallback absoluto
    }

    // ==========================================
    // FAKE CONTAINER REQUEST CONTEXT
        

        

        // ==========================================
    static class FakeContainerRequestContext implements ContainerRequestContext {
        private String method;
        private String path;
        private final MultivaluedMap<String, String> headers = new MultivaluedHashMap<>();
        private Response abortedWith;

        public FakeContainerRequestContext(String method, String path) {
            this.method = method;
            this.path = path;
        }

        public Response getAbortedWith() {
            return abortedWith;
        }

        @Override
        public String getMethod() {
            return method;
        }

        @Override
        public void setMethod(String method) {
            this.method = method;
        }

        @Override
        public MultivaluedMap<String, String> getHeaders() {
            return headers;
        }

        @Override
        public String getHeaderString(String name) {
            List<String> values = headers.get(name);
            return (values != null && !values.isEmpty()) ? values.get(0) : null;
        }

        @Override
        public void abortWith(Response response) {
            this.abortedWith = response;
        }

        @Override
        public UriInfo getUriInfo() {
            return new FakeUriInfo(path);
        }

        @Override public Object getProperty(String name) { return null; }
        @Override public Collection<String> getPropertyNames() { return List.of(); }
        @Override public void setProperty(String name, Object object) {}
        @Override public void removeProperty(String name) {}
        @Override public void setRequestUri(URI requestUri) {}
        @Override public void setRequestUri(URI baseUri, URI requestUri) {}
        @Override public Request getRequest() { return null; }
        @Override public Date getDate() { return null; }
        @Override public Locale getLanguage() { return null; }
        @Override public int getLength() { return 0; }
        @Override public MediaType getMediaType() { return null; }
        @Override public List<MediaType> getAcceptableMediaTypes() { return List.of(); }
        @Override public List<Locale> getAcceptableLanguages() { return List.of(); }
        @Override public Map<String, Cookie> getCookies() { return Map.of(); }
        @Override public boolean hasEntity() { return false; }
        @Override public InputStream getEntityStream() { return null; }
        @Override public void setEntityStream(InputStream input) {}
        @Override public SecurityContext getSecurityContext() { return null; }
        @Override public void setSecurityContext(SecurityContext context) {}
    }

    static class FakeUriInfo implements UriInfo {
        private final String path;

        public FakeUriInfo(String path) {
            this.path = path;
        }

        @Override public String getPath() { return path; }
        @Override public String getPath(boolean decode) { return path; }
        @Override public List<jakarta.ws.rs.core.PathSegment> getPathSegments() { return List.of(); }
        @Override public List<jakarta.ws.rs.core.PathSegment> getPathSegments(boolean decode) { return List.of(); }
        @Override public URI getRequestUri() { return URI.create("http://localhost" + path); }
        @Override public jakarta.ws.rs.core.UriBuilder getRequestUriBuilder() { return null; }
        @Override public URI getAbsolutePath() { return URI.create("http://localhost" + path); }
        @Override public jakarta.ws.rs.core.UriBuilder getAbsolutePathBuilder() { return null; }
        @Override public URI getBaseUri() { return URI.create("http://localhost"); }
        @Override public jakarta.ws.rs.core.UriBuilder getBaseUriBuilder() { return null; }
        @Override public MultivaluedMap<String, String> getPathParameters() { return new MultivaluedHashMap<>(); }
        @Override public MultivaluedMap<String, String> getPathParameters(boolean decode) { return new MultivaluedHashMap<>(); }
        @Override public MultivaluedMap<String, String> getQueryParameters() { return new MultivaluedHashMap<>(); }
        @Override public MultivaluedMap<String, String> getQueryParameters(boolean decode) { return new MultivaluedHashMap<>(); }
        @Override public List<String> getMatchedURIs() { return List.of(); }
        @Override public List<String> getMatchedURIs(boolean decode) { return List.of(); }
        @Override public List<Object> getMatchedResources() { return List.of(); }
        @Override public URI resolve(URI uri) { return uri; }
        @Override public URI relativize(URI uri) { return uri; }
    }
}
