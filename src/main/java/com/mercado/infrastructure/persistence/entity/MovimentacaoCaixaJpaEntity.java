package com.mercado.infrastructure.persistence.entity;

import com.mercado.domain.entity.MovimentacaoCaixa;
import com.mercado.domain.entity.TipoMovimentacao;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "movimentacoes_caixa")
public class MovimentacaoCaixaJpaEntity extends PanacheEntityBase {

    @Id
    @Column(name = "id", nullable = false)
    public UUID id;

    @Column(name = "tenant_id", nullable = false)
    public UUID tenantId;

    @Column(name = "caixa_id", nullable = false)
    public UUID caixaId;

    @Column(name = "tipo", nullable = false, length = 20)
    public String tipo;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    public BigDecimal valor;

    @Column(name = "motivo", nullable = false, length = 255)
    public String motivo;

    @Column(name = "criado_em", nullable = false)
    public Instant criadoEm;

    public MovimentacaoCaixaJpaEntity() {
    }

    public static MovimentacaoCaixaJpaEntity fromDomain(MovimentacaoCaixa domain) {
        MovimentacaoCaixaJpaEntity entity = new MovimentacaoCaixaJpaEntity();
        entity.id = domain.getId();
        entity.tenantId = domain.getTenantId().valor();
        entity.caixaId = domain.getCaixaId();
        entity.tipo = domain.getTipo().name();
        entity.valor = domain.getValor().valor();
        entity.motivo = domain.getMotivo();
        entity.criadoEm = domain.getCriadoEm();
        return entity;
    }

    public MovimentacaoCaixa toDomain() {
        return new MovimentacaoCaixa(
            this.id,
            TenantId.de(this.tenantId),
            this.caixaId,
            TipoMovimentacao.valueOf(this.tipo),
            Dinheiro.de(this.valor),
            this.motivo,
            this.criadoEm
        );
    }
}
