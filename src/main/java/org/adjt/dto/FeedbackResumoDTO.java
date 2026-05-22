package org.adjt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.adjt.enums.Urgencia;

import java.time.LocalDateTime;
import java.util.UUID;

public record FeedbackResumoDTO(
        UUID id,
        String descricao,
        int nota,
        Urgencia urgencia,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dataEnvio
) {}