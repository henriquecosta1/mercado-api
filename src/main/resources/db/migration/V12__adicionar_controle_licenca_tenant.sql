-- V12: Adicionar controle de status, licença e whatsapp para tenants/estabelecimentos

ALTER TABLE tenants
    ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    ADD COLUMN IF NOT EXISTS data_expiracao_licenca TIMESTAMP,
    ADD COLUMN IF NOT EXISTS whatsapp VARCHAR(30);

-- Garantir que tenants existentes permaneçam ativos com licença estendida
UPDATE tenants
SET status = 'ATIVO',
    data_expiracao_licenca = NOW() + INTERVAL '365 days'
WHERE status IS NULL OR status = 'ATIVO';

CREATE INDEX IF NOT EXISTS idx_tenants_status ON tenants(status);
