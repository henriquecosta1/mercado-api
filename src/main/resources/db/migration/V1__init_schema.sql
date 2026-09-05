CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE caixas (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    saldo_inicial NUMERIC(12, 2) NOT NULL,
    saldo_dinheiro NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ABERTO',
    aberto_em TIMESTAMP NOT NULL DEFAULT NOW(),
    fechado_em TIMESTAMP
);

CREATE TABLE clientes (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    nome VARCHAR(120) NOT NULL,
    telefone VARCHAR(20),
    limite_credito NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    saldo_devedor NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE vendas (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    caixa_id UUID NOT NULL REFERENCES caixas(id),
    valor_total NUMERIC(12, 2) NOT NULL,
    forma_pagamento VARCHAR(20) NOT NULL,
    troco NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    descricao VARCHAR(255),
    criado_em TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Seed para testes imediatos
INSERT INTO tenants (id, nome)
VALUES ('00000000-0000-0000-0000-000000000001', 'Mercado');

INSERT INTO caixas (id, tenant_id, saldo_inicial, saldo_dinheiro, status, aberto_em)
VALUES ('00000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 50.00, 50.00, 'ABERTO', NOW());