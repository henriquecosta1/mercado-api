package com.mercado.domain.entity;

import com.mercado.domain.exception.RegraDeNegocioException;
import com.mercado.domain.valueobject.Dinheiro;
import com.mercado.domain.valueobject.TenantId;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidade que representa uma movimentação avulsa de caixa (Sangria ou Suprimento).
 */
public class MovimentacaoCaixa {

    private final UUID id;
    private final TenantId tenantId;
    private final UUID caixaId;
    private final TipoMovimentacao tipo;
    private final Dinheiro valor;
    private final String motivo;
    private final Instant criadoEm;

    public MovimentacaoCaixa(UUID id,
                             TenantId tenantId,
                             UUID caixaId,
                             TipoMovimentacao tipo,
                             Dinheiro valor,
                             String motivo,
                             Instant criadoEm) {
        this.id = Objects.requireNonNull(id, "Id da movimentação não pode ser nulo.");
        this.tenantId = Objects.requireNonNull(tenantId, "TenantId não pode ser nulo.");
        this.caixaId = Objects.requireNonNull(caixaId, "CaixaId não pode ser nulo.");
        this.tipo = Objects.requireNonNull(tipo, "Tipo da movimentação não pode ser nulo.");
        this.valor = Objects.requireNonNull(valor, "Valor da movimentação não pode ser nulo.");
        this.motivo = validarMotivo(motivo);
        this.criadoEm = Objects.requireNonNull(criadoEm, "Data de criação não pode ser nula.");
    }

    public static MovimentacaoCaixa criar(TenantId tenantId,
                                          UUID caixaId,
                                          TipoMovimentacao tipo,
                                          Dinheiro valor,
                                          String motivo) {
        Objects.requireNonNull(valor, "Valor da movimentação não pode ser nulo.");
        if (valor.isNegativo() || valor.isZero()) {
            throw new RegraDeNegocioException("Valor da movimentação deve ser estritamente positivo.");
        }
        UUID id = UUID.randomUUID();
        Instant agora = Instant.now();
        return new MovimentacaoCaixa(id, tenantId, caixaId, tipo, valor, motivo, agora);
    }

    private static String validarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new RegraDeNegocioException("Motivo da movimentação é obrigatório.");
        }
        return motivo.trim();
    }

    public UUID getId() {
        return id;
    }

    public TenantId getTenantId() {
        return tenantId;
    }

    public UUID getCaixaId() {
        return caixaId;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public Dinheiro getValor() {
        return valor;
    }

    public String getMotivo() {
        return motivo;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }
}
