-- =============================================================================
-- Script de dados de teste para desenvolvimento local
-- Banco: PostgreSQL (docker-compose pg-dev)
--
-- Execute uma vez após subir o docker-compose:
--   docker exec -i pg-dev psql -U postgres -d postgres < scripts/insert-test-data.sql
--
-- Para limpar e reinserir:
--   docker exec -i pg-dev psql -U postgres -d postgres -c "DELETE FROM feedback;"
--   docker exec -i pg-dev psql -U postgres -d postgres < scripts/insert-test-data.sql
-- =============================================================================

-- Garante que a tabela existe (caso o Flyway ainda não tenha rodado)
-- Se a tabela já existir, este bloco é ignorado
DO $$
BEGIN
    IF NOT EXISTS (SELECT FROM information_schema.tables WHERE table_name = 'feedback') THEN
        RAISE EXCEPTION 'Tabela feedback não encontrada. Inicie a aplicação primeiro para o Flyway criar o schema.';
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- Semana atual (últimos 7 dias) — aparece no relatório semanal
-- -----------------------------------------------------------------------------

-- Hoje
INSERT INTO feedback (id, descricao, nota, urgencia, data_envio) VALUES
    (gen_random_uuid(), 'Excelente aula sobre microsserviços, professor muito didático!', 10, 'BOA',    NOW()),
    (gen_random_uuid(), 'Conteúdo bom, mas o ritmo estava um pouco acelerado.',           6,  'MEDIA',  NOW() - INTERVAL '2 hours'),
    (gen_random_uuid(), 'Plataforma travou durante a aula ao vivo, muito frustrante.',    2,  'CRITICA', NOW() - INTERVAL '4 hours');

-- Ontem
INSERT INTO feedback (id, descricao, nota, urgencia, data_envio) VALUES
    (gen_random_uuid(), 'Ótima explicação sobre Docker e Kubernetes.',                     9,  'BOA',    NOW() - INTERVAL '1 day'),
    (gen_random_uuid(), 'Exercícios práticos muito relevantes para o mercado.',            8,  'BOA',    NOW() - INTERVAL '1 day' - INTERVAL '3 hours'),
    (gen_random_uuid(), 'Áudio com eco durante toda a live, dificulta o entendimento.',   3,  'CRITICA', NOW() - INTERVAL '1 day' - INTERVAL '5 hours');

-- 2 dias atrás
INSERT INTO feedback (id, descricao, nota, urgencia, data_envio) VALUES
    (gen_random_uuid(), 'Material complementar muito bem elaborado.',                      8,  'BOA',    NOW() - INTERVAL '2 days'),
    (gen_random_uuid(), 'Faltou mais exemplos práticos, ficou muito teórico.',             5,  'MEDIA',  NOW() - INTERVAL '2 days' - INTERVAL '1 hour'),
    (gen_random_uuid(), 'Professor respondeu todas as dúvidas no chat, parabéns!',         9,  'BOA',    NOW() - INTERVAL '2 days' - INTERVAL '6 hours');

-- 3 dias atrás
INSERT INTO feedback (id, descricao, nota, urgencia, data_envio) VALUES
    (gen_random_uuid(), 'Aula sobre Cloud Computing foi transformadora para mim.',         10, 'BOA',    NOW() - INTERVAL '3 days'),
    (gen_random_uuid(), 'Conteúdo desatualizado, tecnologias mais antigas do mercado.',    4,  'CRITICA', NOW() - INTERVAL '3 days' - INTERVAL '2 hours'),
    (gen_random_uuid(), 'A qualidade do vídeo gravado está boa, sem problemas.',           7,  'MEDIA',  NOW() - INTERVAL '3 days' - INTERVAL '4 hours');

-- 4 dias atrás
INSERT INTO feedback (id, descricao, nota, urgencia, data_envio) VALUES
    (gen_random_uuid(), 'Excelente abordagem sobre serverless e Azure Functions.',         9,  'BOA',    NOW() - INTERVAL '4 days'),
    (gen_random_uuid(), 'Slides com letras pequenas demais, difíceis de ler.',             5,  'MEDIA',  NOW() - INTERVAL '4 days' - INTERVAL '3 hours'),
    (gen_random_uuid(), 'Professor não respondeu nenhuma pergunta no chat durante a live.',1,  'CRITICA', NOW() - INTERVAL '4 days' - INTERVAL '7 hours');

-- 5 dias atrás
INSERT INTO feedback (id, descricao, nota, urgencia, data_envio) VALUES
    (gen_random_uuid(), 'Conteúdo muito bem estruturado e progressivo.',                   8,  'BOA',    NOW() - INTERVAL '5 days'),
    (gen_random_uuid(), 'Duração muito longa, poderia ser dividida em duas partes.',       6,  'MEDIA',  NOW() - INTERVAL '5 days' - INTERVAL '2 hours');

-- 6 dias atrás
INSERT INTO feedback (id, descricao, nota, urgencia, data_envio) VALUES
    (gen_random_uuid(), 'Melhor aula do módulo, muito prática e direta.',                  10, 'BOA',    NOW() - INTERVAL '6 days'),
    (gen_random_uuid(), 'Exemplos de código com erros, causou confusão na turma.',         3,  'CRITICA', NOW() - INTERVAL '6 days' - INTERVAL '3 hours'),
    (gen_random_uuid(), 'Regular, nada novo em relação ao que já foi visto.',              5,  'MEDIA',  NOW() - INTERVAL '6 days' - INTERVAL '5 hours');

-- -----------------------------------------------------------------------------
-- Semana anterior (8-14 dias atrás) — NÃO aparece no relatório semanal
-- Útil para testar o endpoint /relatorio/periodo
-- -----------------------------------------------------------------------------

INSERT INTO feedback (id, descricao, nota, urgencia, data_envio) VALUES
    (gen_random_uuid(), 'Aula anterior muito bem avaliada pela turma.',                   9,  'BOA',    NOW() - INTERVAL '9 days'),
    (gen_random_uuid(), 'Problemas técnicos na semana passada também.',                   2,  'CRITICA', NOW() - INTERVAL '10 days'),
    (gen_random_uuid(), 'Conteúdo relevante, mas poderia ser mais aprofundado.',          6,  'MEDIA',  NOW() - INTERVAL '12 days'),
    (gen_random_uuid(), 'Excelente semana de conteúdo no geral.',                         9,  'BOA',    NOW() - INTERVAL '13 days');

-- Confirmação
SELECT
    urgencia,
    COUNT(*)                                          AS total,
    ROUND(AVG(nota), 2)                               AS media_nota,
    COUNT(*) FILTER (WHERE data_envio >= NOW() - INTERVAL '7 days') AS na_semana_atual
FROM feedback
GROUP BY urgencia
ORDER BY urgencia;