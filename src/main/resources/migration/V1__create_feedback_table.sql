-- =============================================================================
-- Criação da tabela de feedbacks
-- Compartilhada com o serviço de submissão (tech-challenge-avaliacao)
-- =============================================================================

CREATE TABLE IF NOT EXISTS feedback (
    id         UUID         NOT NULL DEFAULT gen_random_uuid(),
    descricao  TEXT         NOT NULL,
    nota       INTEGER      NOT NULL CHECK (nota >= 0 AND nota <= 10),
    urgencia   VARCHAR(10)  NOT NULL CHECK (urgencia IN ('CRITICA', 'MEDIA', 'BOA')),
    data_envio TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_feedback PRIMARY KEY (id)
);

-- Índices para as queries mais comuns do serviço de relatório
CREATE INDEX IF NOT EXISTS idx_feedback_data_envio ON feedback (data_envio);
CREATE INDEX IF NOT EXISTS idx_feedback_urgencia   ON feedback (urgencia);
CREATE INDEX IF NOT EXISTS idx_feedback_data_urgencia ON feedback (data_envio, urgencia);