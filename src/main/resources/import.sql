-- Dados de exemplo para os perfis dev e test (sintaxe compatível com H2)
INSERT INTO feedback (id, descricao, nota, urgencia, data_envio) VALUES
    ('a1b2c3d4-0001-0000-0000-000000000001', 'Aula excelente, conteúdo muito bem explicado!',       10, 'BOA',     DATEADD('DAY', -6, NOW())),
    ('a1b2c3d4-0002-0000-0000-000000000002', 'Boa aula, mas poderia ter mais exemplos práticos.',     7, 'MEDIA',   DATEADD('DAY', -5, NOW())),
    ('a1b2c3d4-0003-0000-0000-000000000003', 'Péssima qualidade de áudio, não consegui ouvir nada.', 1, 'CRITICA', DATEADD('DAY', -4, NOW())),
    ('a1b2c3d4-0004-0000-0000-000000000004', 'Conteúdo relevante, professor muito didático.',         9, 'BOA',     DATEADD('DAY', -4, NOW())),
    ('a1b2c3d4-0005-0000-0000-000000000005', 'Aula repetitiva e longa demais.',                       4, 'CRITICA', DATEADD('DAY', -3, NOW())),
    ('a1b2c3d4-0006-0000-0000-000000000006', 'Material de apoio ótimo, complementa bem a aula.',      8, 'BOA',     DATEADD('DAY', -2, NOW())),
    ('a1b2c3d4-0007-0000-0000-000000000007', 'Exercícios difíceis demais sem explicação adequada.',   3, 'CRITICA', DATEADD('DAY', -1, NOW())),
    ('a1b2c3d4-0008-0000-0000-000000000008', 'Gostei da aula, mas o tempo foi curto.',                6, 'MEDIA',   NOW());