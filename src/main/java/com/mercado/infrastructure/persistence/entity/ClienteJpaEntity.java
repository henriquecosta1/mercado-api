package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.Cliente;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

    @Column(name = "telefone", length = 20)
    public String telefone;

    @Column(name = "limite_credito", nullable = false, precision = 12, scale = 2)
    public BigDecimal limiteCredito;

    @Column(name = "saldo_devedor", nullable = false, precision = 12, scale = 2)
    public BigDecimal saldoDevedor;

    @Column(name = "criado_em", nullable = false)
    public LocalDateTime criadoEm;

    public ClienteJpaEntity() {
    }

    public static ClienteJpaEntity fromDomain(Cliente domain) {
        ClienteJpaEntity entity = new ClienteJpaEntity();
        entity.id = domain.getId();
        entity.tenantId = domain.getTenantId().valor();
        entity.nome = domain.getNome();
        entity.telefone = domain.getTelefone();
        entity.limiteCredito = domain.getLimiteCredito().valor();
        entity.saldoDevedor = domain.getSaldoDevedor().valor();
        entity.criadoEm = domain.getCriadoEm();
        return entity;
    }

    public void updateFromDomain(Cliente domain) {
        this.nome = domain.getNome();
        this.telefone = domain.getTelefone();
        this.limiteCredito = domain.getLimiteCredito().valor();
        this.saldoDevedor = domain.getSaldoDevedor().valor();
    }

    public Cliente toDomain() {
        return new Cliente(
            this.id,
            TenantId.de(this.tenantId),
            this.nome,
            this.telefone,
            Dinheiro.de(this.limiteCredito),
            Dinheiro.de(this.saldoDevedor),
            this.criadoEm
        );
    }
}
