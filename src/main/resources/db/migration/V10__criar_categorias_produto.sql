-- V10: Criar tabela de categorias de produto customizadas por tenant e vincular aos produtos
CREATE TABLE IF NOT EXISTS categorias_produto (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    nome VARCHAR(60) NOT NULL,
    icone VARCHAR(30),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_categoria_tenant_nome UNIQUE (tenant_id, nome)
);

CREATE INDEX IF NOT EXISTS idx_categorias_tenant ON categorias_produto(tenant_id);

ALTER TABLE produtos
    ADD COLUMN IF NOT EXISTS categoria_id UUID REFERENCES categorias_produto(id);

CREATE INDEX IF NOT EXISTS idx_produtos_categoria_id ON produtos(categoria_id);

-- Seed de categorias padrão para o tenant de desenvolvimento inicial
INSERT INTO categorias_produto (id, tenant_id, nome, icone, ativo, criado_em)
VALUES
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'Mercearia', 'cart', true, NOW()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'Bebidas', 'beer', true, NOW()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'Frios & Laticínios', 'cheese', true, NOW()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'Hortifrúti', 'apple', true, NOW()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'Padaria', 'bread', true, NOW()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'Limpeza', 'sparkles', true, NOW()),
    (gen_random_uuid(), '00000000-0000-0000-0000-000000000001', 'Higiene', 'heart', true, NOW())
ON CONFLICT DO NOTHING;
