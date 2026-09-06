CREATE TABLE movimentacoes_caixa (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    caixa_id UUID NOT NULL REFERENCES caixas(id),
    tipo VARCHAR(20) NOT NULL,
    valor NUMERIC(12, 2) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);
