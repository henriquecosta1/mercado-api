ALTER TABLE produtos
    ADD COLUMN IF NOT EXISTS categoria VARCHAR(50) DEFAULT 'Geral',
    ADD COLUMN IF NOT EXISTS preco_custo NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS estoque_minimo NUMERIC(12, 3) NOT NULL DEFAULT 5.000;

CREATE INDEX IF NOT EXISTS idx_produtos_tenant_categoria ON produtos(tenant_id, categoria);
CREATE INDEX IF NOT EXISTS idx_produtos_tenant_estoque_baixo ON produtos(tenant_id, estoque_atual, estoque_minimo);
