ALTER TABLE vendas ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'CONCLUIDA';
ALTER TABLE vendas ADD COLUMN motivo_cancelamento VARCHAR(255);
ALTER TABLE vendas ADD COLUMN cancelada_em TIMESTAMP;
ALTER TABLE vendas ADD COLUMN cliente_id UUID REFERENCES clientes(id);
ALTER TABLE vendas ADD COLUMN nome_cliente VARCHAR(120);

CREATE INDEX idx_vendas_caixa_id ON vendas(caixa_id);
CREATE INDEX idx_vendas_status ON vendas(status);
