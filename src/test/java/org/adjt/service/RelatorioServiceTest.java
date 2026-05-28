package org.adjt.service;

import org.adjt.dto.AvaliacoesPorUrgenciaDTO;
import org.adjt.dto.RelatorioSemanalDTO;
import org.adjt.entity.Feedback;
import org.adjt.enums.Urgencia;
import org.adjt.repository.FeedbackRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelatorioServiceTest {

    private static final LocalDateTime BASE = LocalDateTime.of(2024, 1, 8, 10, 0, 0);

    @Mock
    FeedbackRepository feedbackRepository;

    @InjectMocks
    RelatorioService relatorioService;

    // -------------------------------------------------------------------------
    // Relatório vazio
    // -------------------------------------------------------------------------

    @Test
    void deveRetornarRelatorioVazioQuandoNaoHaFeedbacks() {
        when(feedbackRepository.findByPeriodo(any(), any())).thenReturn(Collections.emptyList());

        RelatorioSemanalDTO relatorio = relatorioService.gerarRelatorioSemanal();

        assertEquals(0, relatorio.totalAvaliacoes());
        assertEquals(0.0, relatorio.mediaNotas());
        assertTrue(relatorio.feedbacks().isEmpty());
        assertTrue(relatorio.avaliacoesPorDia().isEmpty());
        // todas as urgências devem aparecer com quantidade 0
        assertEquals(3, relatorio.avaliacoesPorUrgencia().size());
        relatorio.avaliacoesPorUrgencia().forEach(u -> assertEquals(0L, u.quantidade()));
    }

    @Test
    void deveRetornarPercentualZeroQuandoSemFeedbacks() {
        when(feedbackRepository.findByPeriodo(any(), any())).thenReturn(Collections.emptyList());

        relatorioService.gerarRelatorioSemanal().avaliacoesPorUrgencia()
                .forEach(u -> assertEquals(0.0, u.percentual()));
    }

    // -------------------------------------------------------------------------
    // Média das notas
    // -------------------------------------------------------------------------

    @Test
    void deveCalcularMediaComUmFeedback() {
        when(feedbackRepository.findByPeriodo(any(), any()))
                .thenReturn(List.of(feedback(7, Urgencia.MEDIA, BASE)));

        assertEquals(7.0, relatorioService.gerarRelatorioSemanal().mediaNotas(), 0.01);
    }

    @Test
    void deveCalcularMediaComMultiplosFeedbacks() {
        when(feedbackRepository.findByPeriodo(any(), any()))
                .thenReturn(List.of(
                        feedback(9,  Urgencia.BOA,    BASE),
                        feedback(6,  Urgencia.MEDIA,  BASE.plusDays(1)),
                        feedback(3,  Urgencia.CRITICA, BASE.plusDays(2))
                ));

        // (9+6+3)/3 = 6.0
        assertEquals(6.0, relatorioService.gerarRelatorioSemanal().mediaNotas(), 0.01);
    }

    // -------------------------------------------------------------------------
    // Agrupamento por dia
    // -------------------------------------------------------------------------

    @Test
    void deveAgruparPorDiaEmOrdemCronologica() {
        when(feedbackRepository.findByPeriodo(any(), any()))
                .thenReturn(List.of(
                        feedback(8, Urgencia.BOA,    BASE),
                        feedback(7, Urgencia.MEDIA,  BASE),               // mesmo dia que o anterior
                        feedback(4, Urgencia.CRITICA, BASE.plusDays(2))
                ));

        var porDia = relatorioService.gerarRelatorioSemanal().avaliacoesPorDia();

        assertEquals(2, porDia.size());
        assertEquals(BASE.toLocalDate(),              porDia.get(0).data());
        assertEquals(2L,                              porDia.get(0).quantidade());
        assertEquals(BASE.plusDays(2).toLocalDate(),  porDia.get(1).data());
        assertEquals(1L,                              porDia.get(1).quantidade());
    }

    @Test
    void deveContarUmPorDiaQuandoCadaFeedbackEmDiaDiferente() {
        when(feedbackRepository.findByPeriodo(any(), any()))
                .thenReturn(List.of(
                        feedback(5, Urgencia.MEDIA, BASE),
                        feedback(5, Urgencia.MEDIA, BASE.plusDays(1)),
                        feedback(5, Urgencia.MEDIA, BASE.plusDays(2))
                ));

        var porDia = relatorioService.gerarRelatorioSemanal().avaliacoesPorDia();

        assertEquals(3, porDia.size());
        porDia.forEach(d -> assertEquals(1L, d.quantidade()));
    }

    // -------------------------------------------------------------------------
    // Agrupamento por urgência e percentuais
    // -------------------------------------------------------------------------

    @Test
    void deveAgruparPorUrgenciaCorretamente() {
        when(feedbackRepository.findByPeriodo(any(), any()))
                .thenReturn(List.of(
                        feedback(2, Urgencia.CRITICA, BASE),
                        feedback(6, Urgencia.MEDIA,   BASE),
                        feedback(9, Urgencia.BOA,     BASE),
                        feedback(10, Urgencia.BOA,    BASE)
                ));

        var porUrgencia = relatorioService.gerarRelatorioSemanal().avaliacoesPorUrgencia();

        assertEquals(1L, qtdPara(porUrgencia, Urgencia.CRITICA));
        assertEquals(1L, qtdPara(porUrgencia, Urgencia.MEDIA));
        assertEquals(2L, qtdPara(porUrgencia, Urgencia.BOA));
    }

    @Test
    void deveCalcularPercentuaisCorretos() {
        when(feedbackRepository.findByPeriodo(any(), any()))
                .thenReturn(List.of(
                        feedback(2, Urgencia.CRITICA, BASE),   // 1 de 4 = 25%
                        feedback(6, Urgencia.MEDIA,   BASE),   // 1 de 4 = 25%
                        feedback(9, Urgencia.BOA,     BASE),   // 2 de 4 = 50%
                        feedback(10, Urgencia.BOA,    BASE)
                ));

        var porUrgencia = relatorioService.gerarRelatorioSemanal().avaliacoesPorUrgencia();

        assertEquals(25.0, pctPara(porUrgencia, Urgencia.CRITICA), 0.1);
        assertEquals(25.0, pctPara(porUrgencia, Urgencia.MEDIA),   0.1);
        assertEquals(50.0, pctPara(porUrgencia, Urgencia.BOA),     0.1);
    }

    // -------------------------------------------------------------------------
    // Inferência de urgência quando nula na entidade
    // -------------------------------------------------------------------------

    @Test
    void deveInferirUrgenciaQuandoNulaNoEntity() {
        Feedback f = feedback(2, null, BASE); // urgencia null

        when(feedbackRepository.findByPeriodo(any(), any())).thenReturn(List.of(f));

        var resumo = relatorioService.gerarRelatorioSemanal().feedbacks().get(0);
        assertEquals(Urgencia.CRITICA, resumo.urgencia());
    }

    @Test
    void devePreservarUrgenciaQuandoPreenchidaNoEntity() {
        // urgência BOA, mas nota 2 (seria CRITICA se inferida)
        Feedback f = feedback(2, Urgencia.BOA, BASE);

        when(feedbackRepository.findByPeriodo(any(), any())).thenReturn(List.of(f));

        var resumo = relatorioService.gerarRelatorioSemanal().feedbacks().get(0);
        assertEquals(Urgencia.BOA, resumo.urgencia());
    }

    // -------------------------------------------------------------------------
    // Período customizado
    // -------------------------------------------------------------------------

    @Test
    void deveRepassarPeriodoCorretamenteAoRepositorio() {
        LocalDateTime inicio = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2024, 1, 7, 23, 59, 59);
        when(feedbackRepository.findByPeriodo(inicio, fim)).thenReturn(Collections.emptyList());

        var relatorio = relatorioService.gerarRelatorio(inicio, fim);

        assertEquals(inicio, relatorio.periodoInicio());
        assertEquals(fim,    relatorio.periodoFim());
        assertNotNull(relatorio.dataGeracao());
        verify(feedbackRepository).findByPeriodo(inicio, fim);
    }

    @Test
    void gerarRelatorioSemanal_deveChamarRepositorioComJanelaDe7Dias() {
        when(feedbackRepository.findByPeriodo(any(), any())).thenReturn(Collections.emptyList());
        ArgumentCaptor<LocalDateTime> capInicio = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> capFim    = ArgumentCaptor.forClass(LocalDateTime.class);

        relatorioService.gerarRelatorioSemanal();

        verify(feedbackRepository).findByPeriodo(capInicio.capture(), capFim.capture());
        long dias = java.time.Duration.between(capInicio.getValue(), capFim.getValue()).toDays();
        assertEquals(7, dias);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Feedback feedback(int nota, Urgencia urgencia, LocalDateTime data) {
        Feedback f = new Feedback();
        f.id = UUID.randomUUID();
        f.descricao = "Feedback nota " + nota;
        f.nota = nota;
        f.urgencia = urgencia;
        f.dataEnvio = data;
        return f;
    }

    private long qtdPara(List<AvaliacoesPorUrgenciaDTO> lista, Urgencia u) {
        return lista.stream().filter(x -> x.urgencia() == u).mapToLong(AvaliacoesPorUrgenciaDTO::quantidade).sum();
    }

    private double pctPara(List<AvaliacoesPorUrgenciaDTO> lista, Urgencia u) {
        return lista.stream().filter(x -> x.urgencia() == u).mapToDouble(AvaliacoesPorUrgenciaDTO::percentual).sum();
    }
}