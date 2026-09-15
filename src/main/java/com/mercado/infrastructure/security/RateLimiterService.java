package com.mercado.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Duration;
import java.util.UUID;

/**
 * Servico responsavel pelo gerenciamento de cotas de requisicoes (Rate Limiting)
 * e protecao contra ataques de forca bruta (Brute-force defense).
 *
 * Utiliza Bucket4j para algoritmo Token Bucket e cache Caffeine in-memory com TTL
 * de retencao para evitar vazamentos de memoria com IPs inativos.
 */
@ApplicationScoped
public class RateLimiterService {

    private final Cache<String, Bucket> buckets;

    public RateLimiterService() {
        this.buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(50_000)
            .build();
    }

    /**
     * Limite para Onboarding de novos mercados.
     * Capacidade: 3 tokens por minuto, recarga de 3 tokens por minuto.
     */
    public boolean tentarConsumirOnboarding(String ip, String contexto) {
        String chave = "ONBOARDING:" + normalizarIp(ip) + ":" + contexto;
        Bucket bucket = buckets.get(chave, k -> criarBucket(3, 3));
        return bucket.tryConsume(1);
    }

    /**
     * Limite para Login (protecao contra forca bruta em senhas).
     * Capacidade: 5 tokens por minuto, recarga de 5 tokens por minuto.
     */
    public boolean tentarConsumirLogin(String ip, String contexto) {
        String chave = "LOGIN:" + normalizarIp(ip) + ":" + contexto;
        Bucket bucket = buckets.get(chave, k -> criarBucket(5, 5));
        return bucket.tryConsume(1);
    }

    /**
     * Limite para Validacao de PIN de Gerente.
     * Capacidade: 5 tokens por minuto, isolado por IP e TenantId.
     */
    public boolean tentarConsumirValidarPin(String ip, UUID tenantId) {
        String chave = "VALIDAR_PIN:" + normalizarIp(ip) + ":" + (tenantId != null ? tenantId.toString() : "global");
        Bucket bucket = buckets.get(chave, k -> criarBucket(5, 5));
        return bucket.tryConsume(1);
    }

    /**
     * Limite padrao para demais rotas da aplicacao.
     * Capacidade: 100 tokens por minuto, recarga de 100 tokens por minuto.
     */
    public boolean tentarConsumirPadrao(String ip, String contexto) {
        String chave = "DEFAULT:" + normalizarIp(ip) + ":" + contexto;
        Bucket bucket = buckets.get(chave, k -> criarBucket(100, 100));
        return bucket.tryConsume(1);
    }

    /**
     * Cria uma instancia de Bucket com capacidade e recarga por minuto especificadas.
     */
    private Bucket criarBucket(long capacidade, long recargaPorMinuto) {
        Bandwidth limit = Bandwidth.builder()
            .capacity(capacidade)
            .refillGreedy(recargaPorMinuto, Duration.ofMinutes(1))
            .build();

        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Limpa o cache de buckets em memoria (util para testes).
     */
    public void limparCache() {
        this.buckets.invalidateAll();
    }

    private String normalizarIp(String ip) {
        return (ip != null && !ip.isBlank()) ? ip.trim() : "127.0.0.1";
    }
}
