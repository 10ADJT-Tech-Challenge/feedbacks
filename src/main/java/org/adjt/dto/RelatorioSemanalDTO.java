package org.adjt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO com todos os dados do relatório semanal de feedbacks.
 * Campos obrigatórios conforme especificação do Tech Challenge:
 * - descrição e urgência de cada feedback
 * - data de envio de cada feedback
 * - quantidade de avaliações por dia
 * - quantidade de avaliações por urgência
 * - média das notas
 */
public record RelatorioSemanalDTO(

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime periodoInicio,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime periodoFim,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime dataGeracao,

        long totalAvaliacoes,

        double mediaNotas,

        List<FeedbackResumoDTO> feedbacks,

        List<AvaliacoesPorDiaDTO> avaliacoesPorDia,

        List<AvaliacoesPorUrgenciaDTO> avaliacoesPorUrgencia
) {}