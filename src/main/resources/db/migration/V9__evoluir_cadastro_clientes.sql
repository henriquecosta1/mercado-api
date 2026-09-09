-- V9: Evolucao do cadastro de clientes para suporte a controle de risco, limite de credito e bloqueio fiado
ALTER TABLE clientes
    ADD COLUMN IF NOT EXISTS apelido VARCHAR(80),
    ADD COLUMN IF NOT EXISTS cpf VARCHAR(14),
    ADD COLUMN IF NOT EXISTS endereco VARCHAR(200),
    ADD COLUMN IF NOT EXISTS ponto_referencia VARCHAR(150),
    ADD COLUMN IF NOT EXISTS dia_vencimento INTEGER DEFAULT 10,
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    ADD COLUMN IF NOT EXISTS motivo_bloqueio VARCHAR(255),
    ADD COLUMN IF NOT EXISTS observacoes TEXT;

-- Indice para buscas por CPF dentro do tenant
CREATE INDEX IF NOT EXISTS idx_clientes_tenant_cpf ON clientes (tenant_id, cpf);

-- Indice para buscas por status dentro do tenant
CREATE INDEX IF NOT EXISTS idx_clientes_tenant_status ON clientes (tenant_id, status);