CREATE TABLE produtos (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    nome VARCHAR(150) NOT NULL,
    preco_venda NUMERIC(12, 2) NOT NULL,
    unidade VARCHAR(10) NOT NULL DEFAULT 'UN',
    estoque_atual NUMERIC(12, 3) NOT NULL DEFAULT 0.000,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_produtos_tenant_nome ON produtos(tenant_id, nome);

CREATE TABLE itens_venda (
    id UUID PRIMARY KEY,
    venda_id UUID NOT NULL REFERENCES vendas(id),
    produto_id UUID REFERENCES produtos(id),
    descricao VARCHAR(150) NOT NULL,
    quantidade NUMERIC(12, 3) NOT NULL,
    preco_unitario NUMERIC(12, 2) NOT NULL,
    subtotal NUMERIC(12, 2) NOT NULL
);

CREATE INDEX idx_itens_venda_venda_id ON itens_venda(venda_id);
