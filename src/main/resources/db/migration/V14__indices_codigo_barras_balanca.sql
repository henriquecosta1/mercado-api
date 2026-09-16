-- V14: Suporte a leitura de código de barras e balança para checkout rápido de PDV
ALTER TABLE produtos
    ADD COLUMN IF NOT EXISTS codigo_barras VARCHAR(100),
    ADD COLUMN IF NOT EXISTS codigo_interno VARCHAR(50),
    ADD COLUMN IF NOT EXISTS preco_promocional NUMERIC(12, 2),
    ADD COLUMN IF NOT EXISTS permite_fracionado BOOLEAN NOT NULL DEFAULT FALSE;

-- Índices compostos com tenant_id para aceleração máxima de busca no balcão (< 50ms)
CREATE INDEX IF NOT EXISTS idx_produtos_tenant_busca_codigo ON produtos (tenant_id, codigo_barras);
CREATE INDEX IF NOT EXISTS idx_produtos_tenant_codigo_interno ON produtos (tenant_id, codigo_interno);

-- Atualização dos produtos existentes com unidade pesável/fracionável para permite_fracionado = true
UPDATE produtos
SET permite_fracionado = TRUE
WHERE UPPER(unidade) IN ('KG', 'G', 'LT', 'L', 'M', 'MT');
