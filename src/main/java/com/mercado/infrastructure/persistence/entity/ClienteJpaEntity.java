package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.Cliente;
import com.mercado.domain.entity.StatusCliente;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clientes")
public class ClienteJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "nome", nullable = false, length = 120)
    public String nome;

    @Column(name = "apelido", length = 80)
    public String apelido;

    @Column(name = "telefone", length = 20)
    public String telefone;

    @Column(name = "cpf", length = 14)
    public String cpf;

    @Column(name = "endereco", length = 200)
    public String endereco;

    @Column(name = "ponto_referencia", length = 150)
    public String pontoReferencia;

    @Column(name = "limite_credito", nullable = false, precision = 12, scale = 2)
    public BigDecimal limiteCredito;

    @Column(name = "saldo_devedor", nullable = false, precision = 12, scale = 2)
    public BigDecimal saldoDevedor;

    @Column(name = "dia_vencimento")
    public Integer diaVencimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public StatusCliente status;

    @Column(name = "motivo_bloqueio", length = 255)
    public String motivoBloqueio;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    public String observacoes;

    @Column(name = "criado_em", nullable = false)
    public LocalDateTime criadoEm;

    public ClienteJpaEntity() {
    }

    public static ClienteJpaEntity fromDomain(Cliente domain) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.id = domain.getId();
        entity.tenantId = domain.getTenantId().valor();
        entity.nome = domain.getNome();
        entity.apelido = domain.getApelido();
        entity.telefone = domain.getTelefone();
        entity.cpf = domain.getCpf();
        entity.endereco = domain.getEndereco();
        entity.pontoReferencia = domain.getPontoReferencia();
        entity.limiteCredito = domain.getLimiteCredito().valor();
        entity.saldoDevedor = domain.getSaldoDevedor().valor();
        entity.diaVencimento = domain.getDiaVencimento();
        entity.status = domain.getStatus();
        entity.motivoBloqueio = domain.getMotivoBloqueio();
        entity.observacoes = domain.getObservacoes();
        entity.criadoEm = domain.getCriadoEm();
        return entity;
    }

    public void updateFromDomain(Cliente domain) {
        this.nome = domain.getNome();
        this.apelido = domain.getApelido();
        this.telefone = domain.getTelefone();
        this.cpf = domain.getCpf();
        this.endereco = domain.getEndereco();
        this.pontoReferencia = domain.getPontoReferencia();
        this.limiteCredito = domain.getLimiteCredito().valor();
        this.saldoDevedor = domain.getSaldoDevedor().valor();
        this.diaVencimento = domain.getDiaVencimento();
        this.status = domain.getStatus();
        this.motivoBloqueio = domain.getMotivoBloqueio();
        this.observacoes = domain.getObservacoes();
    }

    public Cliente toDomain() {
        return new Cliente(
            this.id,
            TenantId.de(this.tenantId),
            this.nome,
            this.apelido,
            this.telefone,
            this.cpf,
            this.endereco,
            this.pontoReferencia,
            Dinheiro.de(this.limiteCredito),
            Dinheiro.de(this.saldoDevedor),
            this.diaVencimento,
            this.status,
            this.motivoBloqueio,
            this.observacoes,
            this.criadoEm
        );
    }
}