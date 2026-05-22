package org.adjt.service;

import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.adjt.dto.AvaliacoesPorDiaDTO;
import org.adjt.dto.AvaliacoesPorUrgenciaDTO;
import org.adjt.dto.FeedbackResumoDTO;
import org.adjt.dto.RelatorioSemanalDTO;
import org.adjt.enums.Urgencia;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class EmailServiceTest {

    @Inject
    EmailService emailService;

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    void limparMailbox() {
        mailbox.clear();
    }

    @Test
    void deveEnviarEmailParaDestinatarioCorreto() {
        emailService.enviarRelatorioSemanal(relatorioExemplo());

        // "admin@techchallenge.com.br" é o valor default em application.properties
        var emails = mailbox.getMailMessagesSentTo("admin@techchallenge.com.br");
        assertEquals(1, emails.size());
    }

    @Test
    void deveEnviarApenasUmEmail() {
        emailService.enviarRelatorioSemanal(relatorioExemplo());

        assertEquals(1, mailbox.getTotalMessagesSent());
    }

    @Test
    void deveIncluirPalavraChaveNoAssunto() {
        emailService.enviarRelatorioSemanal(relatorioExemplo());

        var mail = mailbox.getMailMessagesSentTo("admin@techchallenge.com.br").get(0);
        assertTrue(mail.getSubject().contains("Relatório Semanal de Feedbacks"),
                "Assunto deveria conter 'Relatório Semanal de Feedbacks'");
    }

    @Test
    void deveIncluirPeriodoNoAssunto() {
        emailService.enviarRelatorioSemanal(relatorioExemplo());

        var mail = mailbox.getMailMessagesSentTo("admin@techchallenge.com.br").get(0);
        // período: 2024-01-08 a 2024-01-15
        assertTrue(mail.getSubject().contains("2024-01-08"),
                "Assunto deveria conter a data de início do período");
    }

    @Test
    void corpoDeveSerHTML() {
        emailService.enviarRelatorioSemanal(relatorioExemplo());

        var mail = mailbox.getMailMessagesSentTo("admin@techchallenge.com.br").get(0);
        assertNotNull(mail.getHtml(), "E-mail deveria ter corpo HTML");
        assertTrue(mail.getHtml().startsWith("<!DOCTYPE html"),
                "Corpo deveria ser HTML válido");
    }

    @Test
    void corpoDeveConterSecaoDeUrgencias() {
        emailService.enviarRelatorioSemanal(relatorioExemplo());

        String html = mailbox.getMailMessagesSentTo("admin@techchallenge.com.br").get(0).getHtml();
        assertTrue(html.contains("Avaliações por Urgência"));
        assertTrue(html.contains("CRITICA"));
        assertTrue(html.contains("MEDIA"));
        assertTrue(html.contains("BOA"));
    }

    @Test
    void corpoDeveConterSecaoPorDia() {
        emailService.enviarRelatorioSemanal(relatorioExemplo());

        String html = mailbox.getMailMessagesSentTo("admin@techchallenge.com.br").get(0).getHtml();
        assertTrue(html.contains("Avaliações por Dia"));
    }

    @Test
    void corpoDeveConterDetalhesDosFeedbacks() {
        emailService.enviarRelatorioSemanal(relatorioExemplo());

        String html = mailbox.getMailMessagesSentTo("admin@techchallenge.com.br").get(0).getHtml();
        assertTrue(html.contains("Detalhes dos Feedbacks"));
        assertTrue(html.contains("Aula ótima"));
        assertTrue(html.contains("Péssima qualidade de áudio"));
    }

    @Test
    void corpoDeveConterTotaisEMedia() {
        emailService.enviarRelatorioSemanal(relatorioExemplo());

        String html = mailbox.getMailMessagesSentTo("admin@techchallenge.com.br").get(0).getHtml();
        assertTrue(html.contains("Total de avaliações"));
        assertTrue(html.contains("Média das notas"));
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private RelatorioSemanalDTO relatorioExemplo() {
        LocalDateTime inicio = LocalDateTime.of(2024, 1, 8, 0, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2024, 1, 15, 23, 59, 59);

        List<FeedbackResumoDTO> feedbacks = List.of(
                new FeedbackResumoDTO(UUID.randomUUID(), "Aula ótima",                 9, Urgencia.BOA,    inicio.plusDays(1)),
                new FeedbackResumoDTO(UUID.randomUUID(), "Gostei bastante",             8, Urgencia.BOA,    inicio.plusDays(2)),
                new FeedbackResumoDTO(UUID.randomUUID(), "Regular",                     6, Urgencia.MEDIA,  inicio.plusDays(3)),
                new FeedbackResumoDTO(UUID.randomUUID(), "Péssima qualidade de áudio",  1, Urgencia.CRITICA, inicio.plusDays(4))
        );

        List<AvaliacoesPorDiaDTO> porDia = List.of(
                new AvaliacoesPorDiaDTO(inicio.plusDays(1).toLocalDate(), 1),
                new AvaliacoesPorDiaDTO(inicio.plusDays(2).toLocalDate(), 1),
                new AvaliacoesPorDiaDTO(inicio.plusDays(3).toLocalDate(), 1),
                new AvaliacoesPorDiaDTO(inicio.plusDays(4).toLocalDate(), 1)
        );

        List<AvaliacoesPorUrgenciaDTO> porUrgencia = List.of(
                new AvaliacoesPorUrgenciaDTO(Urgencia.CRITICA, 1, 25.0),
                new AvaliacoesPorUrgenciaDTO(Urgencia.MEDIA,   1, 25.0),
                new AvaliacoesPorUrgenciaDTO(Urgencia.BOA,     2, 50.0)
        );

        return new RelatorioSemanalDTO(inicio, fim, LocalDateTime.now(), 4, 6.0, feedbacks, porDia, porUrgencia);
    }
}