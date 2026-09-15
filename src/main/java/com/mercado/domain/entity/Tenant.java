package com.mercado.domain.entity;

import com.mercado.domain.exception.PinInvalidoException;
import com.mercado.domain.valueobject.PinGerente;
import com.mercado.domain.valueobject.TenantId;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade rica representando o Inquilino / Estabelecimento (Tenant).
 */
public class Tenant {

    private final TenantId id;
    private String nome;
    private String whatsapp;
    private StatusTenant status;
    private Instant dataExpiracaoLicenca;
    private PinGerente pinGerente;

    public Tenant(TenantId id, String nome, String whatsapp, StatusTenant status, Instant dataExpiracaoLicenca, PinGerente pinGerente) {
        this.id = Objects.requireNonNull(id, "TenantId não pode ser nulo.");
        this.nome = Objects.requireNonNull(nome, "Nome do tenant não pode ser nulo.");
        this.whatsapp = whatsapp;
        this.status = status != null ? status : StatusTenant.ATIVO;
        this.dataExpiracaoLicenca = dataExpiracaoLicenca;
        this.pinGerente = Objects.requireNonNull(pinGerente, "PinGerente não pode ser nulo.");
    }

    public Tenant(TenantId id, String nome, PinGerente pinGerente) {
        this(id, nome, null, StatusTenant.ATIVO, null, pinGerente);
    }

        public static Tenant criar(String nome, String pinGerente) {
        TenantId tenantId = TenantId.de(UUID.randomUUID());
        
        String pinValido = (pinGerente != null && !pinGerente.isBlank()) ? pinGerente.trim() : null;
        if (pinValido == null) {
            // Gerar PIN seguro e aleatorio se nao fornecido
            java.security.SecureRandom random = new java.security.SecureRandom();
            int p = 1000 + random.nextInt(9000);
            pinValido = String.valueOf(p);
        }
        
        return new Tenant(tenantId, nome.trim(), null, StatusTenant.ATIVO, Instant.now().plus(Duration.ofDays(365)), PinGerente.criar(pinValido));
    }

    public static Tenant criar(String nome) {
        return criar(nome, "1234");
    }

    public static Tenant autoCadastrar(String nome, String whatsapp, String pinGerente) {
        TenantId tenantId = TenantId.de(UUID.randomUUID());
        String pinValido = (pinGerente != null && !pinGerente.isBlank()) ? pinGerente : "1234";
        return new Tenant(
            tenantId,
            nome.trim(),
            whatsapp != null ? whatsapp.trim() : null,
            StatusTenant.PENDENTE,
            null,
            PinGerente.criar(pinValido)
        );
    }

    public boolean isPendente() {
        return this.status == StatusTenant.PENDENTE;
    }

    public boolean isVencido() {
        if (this.status == StatusTenant.BLOQUEADO) {
            return true;
        }
        if (this.status == StatusTenant.PENDENTE) {
            return false;
        }
        return this.dataExpiracaoLicenca != null && Instant.now().isAfter(this.dataExpiracaoLicenca);
    }

    public boolean isAtivo() {
        return this.status == StatusTenant.ATIVO && !isVencido();
    }

    public void ativarPorDias(int dias) {
        if (dias <= 0) {
            throw new IllegalArgumentException("Quantidade de dias para ativação deve ser maior que zero.");
        }
        this.status = StatusTenant.ATIVO;
        Instant base = (this.dataExpiracaoLicenca != null && this.dataExpiracaoLicenca.isAfter(Instant.now()))
            ? this.dataExpiracaoLicenca
            : Instant.now();
        this.dataExpiracaoLicenca = base.plus(Duration.ofDays(dias));
    }

    public void suspender() {
        this.status = StatusTenant.BLOQUEADO;
    }

    public void validarPin(String pinCandidato) {
        if (pinCandidato == null || !this.pinGerente.verificar(pinCandidato)) {
            throw new PinInvalidoException("PIN de gerente inválido ou não informado.");
        }
    }

    public void alterarPin(String pinAtual, String novoPin) {
        if (pinAtual == null || !this.pinGerente.verificar(pinAtual)) {
            throw new PinInvalidoException("PIN atual incorreto.");
        }
        this.pinGerente = PinGerente.criar(novoPin);
    }

    public TenantId getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public StatusTenant getStatus() {
        return status;
    }

    public Instant getDataExpiracaoLicenca() {
        return dataExpiracaoLicenca;
    }

    public PinGerente getPinGerente() {
        return pinGerente;
    }
}
