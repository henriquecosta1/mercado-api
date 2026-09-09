-- V8: Cria tabela de usuarios do sistema com suporte a multi-tenancy e autenticacao JWT
CREATE TABLE usuarios (
    id          UUID PRIMARY KEY,
    tenant_id   UUID         NOT NULL REFERENCES tenants(id),
    nome        VARCHAR(100) NOT NULL,
    login       VARCHAR(50)  NOT NULL,
    senha_hash  VARCHAR(100) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL,
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   TIMESTAMP    NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usuario_tenant_login UNIQUE (tenant_id, login)
);

CREATE INDEX idx_usuarios_tenant_login ON usuarios (tenant_id, login);

-- Usuarios iniciais - senha '123456' hasheada com BCrypt custo 12
INSERT INTO usuarios (id, tenant_id, nome, login, senha_hash, perfil, ativo, criado_em)
VALUES
    (
        '10000000-0000-0000-0000-000000000001',
        '00000000-0000-0000-0000-000000000001',
        'Administrador',
        'admin',
        '/LewdBdXwtEnhWAXF2',
        'GERENTE',
        true,
        NOW()
    ),
    (
        '10000000-0000-0000-0000-000000000002',
        '00000000-0000-0000-0000-000000000001',
        'Operador de Caixa',
        'caixa',
        '/LewdBdXwtEnhWAXF2',
        'OPERADOR',
        true,
        NOW()
    );
