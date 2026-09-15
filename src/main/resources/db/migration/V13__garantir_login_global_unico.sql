-- V13: Garantir unicidade global de logins no sistema
-- Remove a constraint antiga (composta por tenant_id e login)
ALTER TABLE usuarios DROP CONSTRAINT uq_usuario_tenant_login;

-- Adiciona a nova constraint garantindo que o login seja globalmente unico
ALTER TABLE usuarios ADD CONSTRAINT uk_usuarios_login UNIQUE (login);
