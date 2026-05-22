package org.adjt.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.adjt.dto.AvaliacoesPorDiaDTO;
import org.adjt.dto.AvaliacoesPorUrgenciaDTO;
import org.adjt.dto.FeedbackResumoDTO;
import org.adjt.dto.RelatorioSemanalDTO;
import org.adjt.entity.Feedback;
import org.adjt.enums.Urgencia;
import org.adjt.repository.FeedbackRepository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@ApplicationScoped
public class RelatorioService {

    @Inject
    FeedbackRepository feedbackRepository;

    /**
     * Gera o relatório dos últimos 7 dias (relatório semanal).
     */
    @Transactional
    public RelatorioSemanalDTO gerarRelatorioSemanal() {
        LocalDateTime fim = LocalDateTime.now();
        LocalDateTime inicio = fim.minusDays(7);
        return gerarRelatorio(inicio, fim);
    }

    /**
     * Gera o relatório para um período customizado.
     */
    @Transactional
    public RelatorioSemanalDTO gerarRelatorio(LocalDateTime inicio, LocalDateTime fim) {
        List<Feedback> feedbacks = feedbackRepository.findByPeriodo(inicio, fim);

        List<FeedbackResumoDTO> resumos = feedbacks.stream()
                .map(f -> new FeedbackResumoDTO(f.id, f.descricao, f.nota, resolverUrgencia(f), f.dataEnvio))
                .collect(Collectors.toList());

        double media = feedbacks.stream()
                .mapToInt(f -> f.nota)
                .average()
                .orElse(0.0);

        List<AvaliacoesPorDiaDTO> porDia = feedbacks.stream()
                .collect(Collectors.groupingBy(
                        f -> f.dataEnvio.toLocalDate(),
                        TreeMap::new,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .map(e -> new AvaliacoesPorDiaDTO(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        long total = feedbacks.size();
        Map<Urgencia, Long> contagemPorUrgencia = feedbacks.stream()
                .collect(Collectors.groupingBy(f -> resolverUrgencia(f), Collectors.counting()));

        List<AvaliacoesPorUrgenciaDTO> porUrgencia = Arrays.stream(Urgencia.values())
                .map(u -> {
                    long qtd = contagemPorUrgencia.getOrDefault(u, 0L);
                    double pct = total > 0 ? Math.round((qtd * 100.0 / total) * 10.0) / 10.0 : 0.0;
                    return new AvaliacoesPorUrgenciaDTO(u, qtd, pct);
                })
                .collect(Collectors.toList());

        return new RelatorioSemanalDTO(
                inicio,
                fim,
                LocalDateTime.now(),
                total,
                Math.round(media * 100.0) / 100.0,
                resumos,
                porDia,
                porUrgencia
        );
    }

    private Urgencia resolverUrgencia(Feedback feedback) {
        // urgencia pode já estar preenchida pelo serviço de submissão;
        // caso contrário, deriva da nota
        if (feedback.urgencia != null) {
            return feedback.urgencia;
        }
        return Urgencia.fromNota(feedback.nota);
    }
}