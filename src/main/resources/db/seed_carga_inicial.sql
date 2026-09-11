-- =========================================================================
-- SCRIPT DE CARGA INICIAL: 50 PRODUTOS E 50 CLIENTES COM CPFS VÁLIDOS
-- Compatível com PostgreSQL / Quarkus 3 / mercado-api
-- =========================================================================

do $$
DECLARE
    tenant_id UUID;
BEGIN
    -- 1. Identifica o tenant alvo (pega o primeiro cadastrado ou o padrão de desenvolvimento)
    SELECT id INTO tenant_id FROM tenants ORDER BY criado_em ASC LIMIT 1;
    IF tenant_id IS NULL THEN
        tenant_id := '00000000-0000-0000-0000-000000000001'::uuid;
        INSERT INTO tenants (id, nome, criado_em)
        VALUES (tenant_id, 'Mercado Central', NOW())
        ON CONFLICT (id) DO NOTHING;
    END IF;

    -- 2. Carga de 50 Clientes com CPFs Válidos e Dados Completos
    RAISE NOTICE 'Inserindo 50 clientes com CPFs válidos para o tenant %...', tenant_id;

    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Ana Paula Souza', 'Paulinha', '11987654321', '101.854.521-24', 'Rua das Flores, 120', 'Próximo à Padaria Central', 500.00, 0.00, 10, 'ATIVO', NULL, 'Cliente pontual', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Carlos Eduardo Lima', 'Cadu', '11987651122', '972.062.473-69', 'Av. Brasil, 450, Apto 12', 'Em frente ao Mercado', 800.00, 120.50, 15, 'ATIVO', NULL, 'Paga no dia 15', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Mariana Oliveira Santos', 'Mari', '11987653344', '251.126.815-94', 'Rua Sete de Setembro, 88', 'Ao lado da Farmácia', 400.00, 0.00, 5, 'ATIVO', NULL, 'Prefere compras aos sábados', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'José Roberto Ferreira', 'Betão', '11987655566', '319.971.535-48', 'Rua Amazonas, 310', 'Casa de esquina com portão branco', 1200.00, 340.00, 10, 'ATIVO', NULL, 'Dono da oficina mecânica', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Juliana Mendes Costa', 'Ju', '11987657788', '210.862.988-29', 'Rua Rio de Janeiro, 54', 'Fundos', 600.00, 0.00, 20, 'ATIVO', NULL, 'Paga via PIX', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Lucas Albuquerque Silva', 'Luquinhas', '11987659900', '021.084.448-58', 'Travessa dos Pinheiros, 15', 'Próximo ao campo de futebol', 350.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Beatriz Helena Ramos', 'Bia', '11987112233', '098.547.473-44', 'Rua Bela Vista, 201', 'Prédio azul, 3º andar', 750.00, 85.00, 5, 'ATIVO', NULL, 'Cliente antiga da mercearia', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Fernando Henrique Dias', 'Nando', '11987223344', '839.030.961-05', 'Rua Minas Gerais, 990', 'Ao lado da igreja', 1000.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Camila Rocha Nascimento', 'Cami', '11987334455', '868.692.253-89', 'Av. Central, 1230', 'Em frente ao posto de saúde', 500.00, 0.00, 15, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Rodrigo Silveira Pinto', 'Digão', '11987445566', '024.549.586-00', 'Rua Rui Barbosa, 67', 'Casa amarela', 900.00, 450.00, 10, 'ATIVO', NULL, 'Avisa antes de vir pagar', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Patricia Gomes Vieira', 'Pati', '11987556677', '793.711.564-57', 'Rua Marechal Deodoro, 412', 'Perto da escola municipal', 650.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Marcos Vinicius Castro', 'Vini', '11987667788', '704.516.969-80', 'Rua Duque de Caxias, 78', 'Vila Nova', 400.00, 0.00, 5, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Fernanda Leal Martins', 'Nanda', '11987778899', '044.526.606-65', 'Rua XV de Novembro, 560', 'Sobrado verde', 850.00, 190.00, 25, 'ATIVO', NULL, 'Paga no final do mês', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Gabriel Antunes Farias', 'Gabi', '11987889900', '143.452.536-83', 'Rua São Paulo, 1040', 'Apto 204', 1500.00, 0.00, 10, 'ATIVO', NULL, 'Cliente VIP', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Larissa Cristina Moreira', 'Lari', '11987990011', '535.784.903-00', 'Rua Santa Catarina, 32', 'Próximo à praça central', 550.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Renato Augusto Barbosa', 'Renatinho', '11988001122', '169.998.630-40', 'Rua Paraná, 876', 'Casa com muro cinza', 700.00, 0.00, 15, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Aline Borges Carvalho', 'Line', '11988112233', '227.881.687-05', 'Rua Goiás, 145', 'Ao lado do pet shop', 450.00, 220.00, 10, 'ATIVO', NULL, 'Atraso leve eventual', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Thiago Henrique Macedo', 'Thiaguinho', '11988223344', '786.854.452-05', 'Rua Bahia, 512', 'Em frente à academia', 800.00, 0.00, 5, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Tatiane Aparecida Nunes', 'Tati', '11988334455', '667.649.718-20', 'Av. Paulista, 2300', 'Bloco B', 600.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Marcelo Peixoto Guimarães', 'Peixoto', '11988445566', '085.717.045-70', 'Rua Espírito Santo, 98', 'Esquina com a Rua 3', 1100.00, 560.00, 10, 'ATIVO', NULL, 'Conta de família', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Vanessa Diniz Ribeiro', 'Vane', '11988556677', '837.390.320-81', 'Rua Maranhão, 340', 'Casa 2', 500.00, 0.00, 20, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Diego Souza Rezende', 'Dieguinho', '11988667788', '821.690.891-10', 'Rua Alagoas, 77', 'Perto do depósito', 650.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Sabrina Prado Monteiro', 'Sá', '11988778899', '809.148.058-00', 'Rua Sergipe, 189', 'Casa de tijolinho', 400.00, 0.00, 5, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Leandro Vasconcelos Brito', 'Léo', '11988889900', '753.253.279-84', 'Rua Paraíba, 420', 'Condomínio Primavera', 950.00, 110.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Priscila Toledo Fontes', 'Pri', '11988990011', '201.359.608-19', 'Rua Ceará, 630', 'Em frente à quadra', 750.00, 0.00, 15, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Danilo Fonseca Duarte', 'Dani', '11989001122', '716.332.687-48', 'Rua Piauí, 115', 'Casa térrea', 500.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Renata Coimbra Meireles', 'Rê', '11989112233', '113.358.869-74', 'Rua Pernambuco, 890', 'Apto 101', 850.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Felipe Siqueira Gusmão', 'Lipe', '11989223344', '907.864.491-52', 'Rua Rio Grande do Norte, 45', 'Ao lado do restaurante', 600.00, 0.00, 5, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Monique Valente Correa', 'Moni', '11989334455', '776.215.501-55', 'Rua Paraíba, 230', 'Fundos', 450.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Vinicius Nogueira Prado', 'Vina', '11989445566', '044.602.544-50', 'Rua Amazonas, 789', 'Portão de madeira', 1000.00, 780.00, 10, 'BLOQUEADO', 'Inadimplência superior a 60 dias', 'Cobrar presencialmente', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Roberta Arruda Paiva', 'Beta', '11989556677', '106.910.583-00', 'Rua Acre, 56', 'Vila Progresso', 550.00, 0.00, 15, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Guilherme Figueiredo Rosa', 'Gui', '11989667788', '898.369.112-35', 'Rua Amapá, 140', 'Casa verde claro', 700.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Débora Freitas Brandão', 'Deby', '11989778899', '794.837.490-69', 'Rua Roraima, 95', 'Ao lado da mercearia antiga', 400.00, 0.00, 5, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Alexandre Mendonça Tavares', 'Xande', '11989889900', '664.886.692-65', 'Rua Rondônia, 312', 'Sobrado 4', 1200.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Claudia Regina Neves', 'Clau', '11989990011', '949.162.080-05', 'Rua Tocantins, 540', 'Casa de frente', 650.00, 310.00, 10, 'ATIVO', NULL, 'Cliente fiel', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Wagner Pires Silveira', 'Waguinho', '11990001122', '856.410.375-32', 'Rua Mato Grosso, 88', 'Perto do ponto de ônibus', 800.00, 0.00, 20, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Elaine Cristina Barros', 'Lani', '11991112233', '315.895.028-68', 'Rua Mato Grosso do Sul, 760', 'Casa amarela com varanda', 500.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Bruno César Barreto', 'Brunão', '11992223344', '273.981.211-64', 'Rua Brasília, 19', 'Prédio residencial', 900.00, 0.00, 5, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Helena Maria Lacerda', 'Dona Helena', '11993334455', '747.345.887-67', 'Rua Curitiba, 405', 'Ao lado da costureira', 600.00, 45.00, 10, 'ATIVO', NULL, 'Paga certinho toda semana', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Otávio Miranda Albuquerque', 'Tavinho', '11994445566', '216.438.780-50', 'Rua Porto Alegre, 128', 'Casa 3', 750.00, 0.00, 15, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Gisele Vasconcelos Paz', 'Gi', '11995556677', '958.904.703-37', 'Rua Florianópolis, 370', 'Fundos', 450.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Eduardo Simões Queiroz', 'Edu', '11996667788', '939.619.086-13', 'Rua Salvador, 810', 'Casa com cerca de madeira', 1100.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Simone Pacheco Garcia', 'Si', '11997778899', '617.952.615-01', 'Rua Recife, 94', 'Em frente à praça da árvore', 550.00, 0.00, 5, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Sérgio Murilo Evangelista', 'Serginho', '11998889900', '943.428.083-40', 'Rua Fortaleza, 620', 'Portão preto', 1000.00, 950.00, 10, 'BLOQUEADO', 'Limite de crédito ultrapassado', 'Aguardando quitação', NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Luciana Bittencourt Sales', 'Lu', '11999990011', '772.537.457-44', 'Rua Natal, 150', 'Apto 302', 700.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Fabio Henrique Xavier', 'Fabinho', '11981234567', '419.513.086-70', 'Rua João Pessoa, 275', 'Próximo à oficina', 850.00, 0.00, 15, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Tânia Mara Medeiros', 'Dona Tânia', '11982345678', '383.013.495-90', 'Rua Maceió, 430', 'Casa de esquina', 500.00, 150.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Robson Cristiano Viana', 'Robinho', '11983456789', '748.620.355-31', 'Rua Aracaju, 70', 'Perto do mercadinho', 650.00, 0.00, 5, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Adriana Teles Fagundes', 'Dri', '11984567890', '134.432.074-05', 'Rua Teresina, 515', 'Sobrado azul', 900.00, 0.00, 10, 'ATIVO', NULL, NULL, NOW());
    INSERT INTO clientes (id, tenant_id, nome, apelido, telefone, cpf, endereco, ponto_referencia, limite_credito, saldo_devedor, dia_vencimento, status, motivo_bloqueio, observacoes, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Maurício Antunes Sampaio', 'Mau', '11985678901', '603.824.949-46', 'Rua Belém, 380', 'Vila Esperança', 1300.00, 280.00, 10, 'ATIVO', NULL, 'Cliente mensal', NOW());

    -- 3. Carga de 50 Produtos Diversificados em Várias Categorias
    RAISE NOTICE 'Inserindo 50 produtos diversificados com preço de custo e estoque para o tenant %...', tenant_id;

    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Arroz Tipo 1 5kg', 'Mercearia', 24.50, 32.90, 'PCT', 85.000, 15.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Feijão Carioca 1kg', 'Mercearia', 6.20, 8.90, 'PCT', 110.000, 20.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Óleo de Soja 900ml', 'Mercearia', 5.10, 7.49, 'UN', 140.000, 24.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Açúcar Refinado 1kg', 'Mercearia', 3.40, 4.89, 'PCT', 95.000, 20.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Sal Refinado 1kg', 'Mercearia', 1.60, 2.49, 'PCT', 60.000, 10.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Café Torrado e Moído 500g', 'Mercearia', 14.20, 19.90, 'PCT', 75.000, 15.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Macarrão Espaguete 500g', 'Mercearia', 2.80, 4.19, 'PCT', 130.000, 25.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Molho de Tomate Tradicional 300g', 'Mercearia', 1.45, 2.29, 'UN', 160.000, 30.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Farinha de Trigo Tradicional 1kg', 'Mercearia', 3.80, 5.49, 'PCT', 80.000, 15.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Milho Verde em Conserva 170g', 'Mercearia', 2.10, 3.29, 'LATA', 90.000, 15.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Ervilha em Conserva 170g', 'Mercearia', 2.00, 3.19, 'LATA', 85.000, 15.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Sardinha em Óleo 125g', 'Mercearia', 3.90, 5.79, 'LATA', 70.000, 12.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Refrigerante Coca-Cola 2L', 'Bebidas', 7.80, 10.99, 'GARRAFA', 120.000, 24.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Refrigerante Guaraná Antarctica 2L', 'Bebidas', 6.50, 8.99, 'GARRAFA', 100.000, 20.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Cerveja Heineken Long Neck 330ml', 'Bebidas', 5.10, 7.49, 'UN', 180.000, 36.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Cerveja Brahma Chopp Lata 350ml', 'Bebidas', 2.90, 4.19, 'LATA', 240.000, 48.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Suco Integral de Uva 1.5L', 'Bebidas', 11.50, 16.90, 'GARRAFA', 45.000, 10.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Água Mineral sem Gás 500ml', 'Bebidas', 0.90, 1.99, 'UN', 200.000, 40.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Água Mineral com Gás 500ml', 'Bebidas', 1.10, 2.29, 'UN', 150.000, 30.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Energético Red Bull 250ml', 'Bebidas', 6.90, 9.99, 'LATA', 60.000, 12.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Leite Integral UHT 1L', 'Frios & Laticínios', 4.10, 5.49, 'CX', 180.000, 36.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Manteiga com Sal 200g', 'Frios & Laticínios', 7.90, 11.49, 'UN', 50.000, 10.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Queijo Mussarela Fatiado kg', 'Frios & Laticínios', 32.00, 46.90, 'KG', 25.000, 5.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Presunto Cozido Fatiado kg', 'Frios & Laticínios', 22.00, 32.90, 'KG', 22.000, 5.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Requeijão Cremoso Tradicional 200g', 'Frios & Laticínios', 5.50, 7.99, 'UN', 65.000, 12.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Iogurte de Morango 170g', 'Frios & Laticínios', 2.30, 3.49, 'UN', 80.000, 15.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Ovos Brancos Médios Dúzia', 'Frios & Laticínios', 8.50, 12.49, 'DZ', 70.000, 15.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Coxão Mole Bovino kg', 'Açougue', 34.00, 48.90, 'KG', 35.000, 8.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Peito de Frango Congelado kg', 'Açougue', 14.50, 21.90, 'KG', 50.000, 10.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Linguiça Toscana Sadia kg', 'Açougue', 16.80, 24.90, 'KG', 40.000, 8.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Carne Moída de Primeira kg', 'Açougue', 28.00, 39.90, 'KG', 30.000, 6.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Bisteca Suína kg', 'Açougue', 17.00, 25.90, 'KG', 28.000, 6.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Bacon Fatiado 250g', 'Açougue', 8.20, 12.90, 'PCT', 45.000, 10.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Banana Prata kg', 'Hortifrúti', 4.20, 6.99, 'KG', 45.000, 10.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Maçã Gala Nacional kg', 'Hortifrúti', 6.50, 9.90, 'KG', 35.000, 8.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Tomate Longa Vida kg', 'Hortifrúti', 5.10, 7.99, 'KG', 40.000, 8.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Cebola Nacional kg', 'Hortifrúti', 3.80, 5.99, 'KG', 55.000, 12.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Batata Inglesa Lavada kg', 'Hortifrúti', 4.00, 6.49, 'KG', 60.000, 15.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Alface Crespa Unidade', 'Hortifrúti', 1.80, 3.29, 'UN', 30.000, 5.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Pão Francês Quentinho kg', 'Padaria', 10.50, 16.90, 'KG', 30.000, 5.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Pão de Forma Tradicional 500g', 'Padaria', 5.20, 7.89, 'PCT', 40.000, 8.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Bolo Caseiro de Cenoura com Chocolate', 'Padaria', 12.00, 18.50, 'UN', 15.000, 3.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Torrada Tradicional 140g', 'Padaria', 3.10, 4.69, 'PCT', 50.000, 10.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Detergente Líquido Neutro 500ml', 'Limpeza', 1.70, 2.59, 'UN', 150.000, 30.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Sabão em Pó 800g', 'Limpeza', 8.20, 11.90, 'PCT', 70.000, 15.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Amaciante Concentrado 500ml', 'Limpeza', 7.40, 10.90, 'UN', 60.000, 12.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Água Sanitária 2L', 'Limpeza', 4.30, 6.49, 'UN', 80.000, 16.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Desinfetante Lavanda 1L', 'Limpeza', 3.90, 5.89, 'UN', 75.000, 15.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Sabonete em Barra 85g', 'Higiene', 1.60, 2.49, 'UN', 140.000, 30.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Creme Dental Tripla Ação 90g', 'Higiene', 3.20, 4.79, 'UN', 90.000, 20.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Shampoo Anticaspa 200ml', 'Higiene', 11.80, 16.90, 'UN', 40.000, 8.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Papel Higiênico Folha Dupla 4 rolos', 'Higiene', 5.90, 8.79, 'PCT', 80.000, 16.000, true, NOW());
    INSERT INTO produtos (id, tenant_id, nome, categoria, preco_custo, preco_venda, unidade, estoque_atual, estoque_minimo, ativo, criado_em)
    VALUES (gen_random_uuid(), tenant_id, 'Desodorante Aerosol 150ml', 'Higiene', 9.50, 13.90, 'UN', 50.000, 10.000, true, NOW());

    RAISE NOTICE 'Carga concluída com sucesso: 50 clientes e 50 produtos inseridos!';
END $$;