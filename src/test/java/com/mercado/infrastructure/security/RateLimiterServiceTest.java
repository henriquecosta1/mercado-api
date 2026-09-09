package com.mercado.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    @Test
    @DisplayName("Deve permitir ate 3 requisicoes de onboarding por minuto por IP e bloquear a 4ª")
    void deveLimitarOnboarding() {
        String ip = "192.168.1.100";

        assertTrue(rateLimiterService.tentarConsumirOnboarding(ip), "1ª requisicao deve ser permitida");
        assertTrue(rateLimiterService.tentarConsumirOnboarding(ip), "2ª requisicao deve ser permitida");
        assertTrue(rateLimiterService.tentarConsumirOnboarding(ip), "3ª requisicao deve ser permitida");

        // 4ª requisição deve ser bloqueada
        assertFalse(rateLimiterService.tentarConsumirOnboarding(ip), "4ª requisicao deve ser bloqueada");

        // Outro IP deve continuar permitido
        assertTrue(rateLimiterService.tentarConsumirOnboarding("10.0.0.1"), "IP diferente deve ter cota propria");
    }

    @Test
    @DisplayName("Deve permitir ate 5 tentativas de login por minuto e bloquear a 6ª (brute-force defense)")
    void deveLimitarLogin() {
        String ip = "200.150.10.5";

        for (int i = 1; i <= 5; i++) {
            assertTrue(rateLimiterService.tentarConsumirLogin(ip), "Requisicao " + i + " de login deve ser permitida");
        }

        assertFalse(rateLimiterService.tentarConsumirLogin(ip), "6ª tentativa de login deve ser bloqueada");

        // Outro IP deve ter cota intacta
        assertTrue(rateLimiterService.tentarConsumirLogin("200.150.10.6"));
    }

    @Test
    @DisplayName("Deve limitar validacao de PIN por IP e TenantId")
    void deveLimitarValidacaoPin() {
        String ip = "172.16.0.10";
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();

        for (int i = 1; i <= 5; i++) {
            assertTrue(rateLimiterService.tentarConsumirValidarPin(ip, tenantA));
        }

        assertFalse(rateLimiterService.tentarConsumirValidarPin(ip, tenantA), "6ª tentativa de PIN para Tenant A deve ser bloqueada");

        // Mesmo IP para outro Tenant ainda tem cota separada
        assertTrue(rateLimiterService.tentarConsumirValidarPin(ip, tenantB), "Tenant B deve ter cota isolada");
    }

    @Test
    @DisplayName("Deve permitir ate 100 requisicoes por minuto na cota padrao")
    void deveLimitarCotaPadrao() {
        String ip = "10.0.0.50";

        for (int i = 1; i <= 100; i++) {
            assertTrue(rateLimiterService.tentarConsumirPadrao(ip), "Requisicao padrao " + i + " deve ser permitida");
        }

        assertFalse(rateLimiterService.tentarConsumirPadrao(ip), "101ª requisicao padrao deve ser bloqueada");
    }

    @Test
    @DisplayName("Deve restaurar cotas ao limpar cache")
    void deveRestaurarAposLimparCache() {
        String ip = "10.0.0.99";

        for (int i = 1; i <= 5; i++) {
            rateLimiterService.tentarConsumirLogin(ip);
        }
        assertFalse(rateLimiterService.tentarConsumirLogin(ip));

        rateLimiterService.limparCache();

        assertTrue(rateLimiterService.tentarConsumirLogin(ip), "Apos limpar cache deve voltar a permitir");
    }
}
