CREATE TABLE amortizacoes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    cliente_id UUID NOT NULL REFERENCES clientes(id),
    caixa_id UUID NOT NULL REFERENCES caixas(id),
    valor NUMERIC(12, 2) NOT NULL,
    forma_pagamento VARCHAR(20) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_amortizacoes_tenant_cliente ON amortizacoes(tenant_id, cliente_id);
CREATE INDEX idx_amortizacoes_caixa_id ON amortizacoes(caixa_id);
