package org.adjt.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.adjt.dto.AvaliacoesPorDiaDTO;
import org.adjt.dto.AvaliacoesPorUrgenciaDTO;
import org.adjt.dto.FeedbackResumoDTO;
import org.adjt.dto.RelatorioSemanalDTO;
import org.adjt.enums.Urgencia;
import org.adjt.service.RelatorioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class RelatorioResourceTest {

    @InjectMock
    RelatorioService relatorioService;

    @BeforeEach
    void configuraMock() {
        when(relatorioService.gerarRelatorioSemanal()).thenReturn(relatorioVazio());
        when(relatorioService.gerarRelatorio(any(), any())).thenReturn(relatorioVazio());
    }

    // -------------------------------------------------------------------------
    // GET /relatorio/semanal
    // -------------------------------------------------------------------------

    @Test
    void getRelatorioSemanal_deveRetornar200() {
        given()
            .when().get("/relatorio/semanal")
            .then()
            .statusCode(200);
    }

    @Test
    void getRelatorioSemanal_deveRetornarJSON() {
        given()
            .when().get("/relatorio/semanal")
            .then()
            .contentType(ContentType.JSON);
    }

    @Test
    void getRelatorioSemanal_deveConterCamposObrigatorios() {
        given()
            .when().get("/relatorio/semanal")
            .then()
            .statusCode(200)
            .body("periodoInicio",       notNullValue())
            .body("periodoFim",          notNullValue())
            .body("dataGeracao",         notNullValue())
            .body("totalAvaliacoes",     notNullValue())
            .body("mediaNotas",          notNullValue())
            .body("feedbacks",           notNullValue())
            .body("avaliacoesPorDia",    notNullValue())
            .body("avaliacoesPorUrgencia", notNullValue());
    }

    @Test
    void getRelatorioSemanal_deveConterTodasAsUrgencias() {
        when(relatorioService.gerarRelatorioSemanal()).thenReturn(relatorioComDados());

        given()
            .when().get("/relatorio/semanal")
            .then()
            .statusCode(200)
            .body("avaliacoesPorUrgencia.urgencia", hasItems("CRITICA", "MEDIA", "BOA"));
    }

    @Test
    void getRelatorioSemanal_deveRetornarTotalCorreto() {
        when(relatorioService.gerarRelatorioSemanal()).thenReturn(relatorioComDados());

        given()
            .when().get("/relatorio/semanal")
            .then()
            .statusCode(200)
            .body("totalAvaliacoes", equalTo(3))
            .body("mediaNotas",      equalTo(6.0f));
    }

    @Test
    void getRelatorioSemanal_deveChamarServico() {
        given()
            .when().get("/relatorio/semanal")
            .then()
            .statusCode(200);

        verify(relatorioService).gerarRelatorioSemanal();
    }

    // -------------------------------------------------------------------------
    // GET /relatorio/periodo
    // -------------------------------------------------------------------------

    @Test
    void getRelatorioPorPeriodo_deveRetornar200ComParametrosValidos() {
        given()
            .queryParam("inicio", "2024-01-01T00:00:00")
            .queryParam("fim",    "2024-01-07T23:59:59")
            .when().get("/relatorio/periodo")
            .then()
            .statusCode(200);
    }

    @Test
    void getRelatorioPorPeriodo_deveChamarServicoComDatasCorretas() {
        given()
            .queryParam("inicio", "2024-01-01T00:00:00")
            .queryParam("fim",    "2024-01-07T23:59:59")
            .when().get("/relatorio/periodo")
            .then()
            .statusCode(200);

        verify(relatorioService).gerarRelatorio(
                LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                LocalDateTime.of(2024, 1, 7, 23, 59, 59)
        );
    }

    @Test
    void getRelatorioPorPeriodo_deveRetornar400SemParametroInicio() {
        given()
            .queryParam("fim", "2024-01-07T23:59:59")
            .when().get("/relatorio/periodo")
            .then()
            .statusCode(400)
            .body("erro", notNullValue());
    }

    @Test
    void getRelatorioPorPeriodo_deveRetornar400SemParametroFim() {
        given()
            .queryParam("inicio", "2024-01-01T00:00:00")
            .when().get("/relatorio/periodo")
            .then()
            .statusCode(400)
            .body("erro", notNullValue());
    }

    @Test
    void getRelatorioPorPeriodo_deveRetornar400SemNenhumParametro() {
        given()
            .when().get("/relatorio/periodo")
            .then()
            .statusCode(400)
            .body("erro", notNullValue());
    }

    @Test
    void getRelatorioPorPeriodo_deveRetornar400ComFormatoDeDataInvalido() {
        given()
            .queryParam("inicio", "01/01/2024")        // formato errado
            .queryParam("fim",    "2024-01-07T23:59:59")
            .when().get("/relatorio/periodo")
            .then()
            .statusCode(400)
            .body("erro", containsString("Formato de data inválido"));
    }

    @Test
    void getRelatorioPorPeriodo_deveRetornar400QuandoInicioMaiorQueFim() {
        given()
            .queryParam("inicio", "2024-01-07T00:00:00")
            .queryParam("fim",    "2024-01-01T00:00:00")   // fim antes do início
            .when().get("/relatorio/periodo")
            .then()
            .statusCode(400)
            .body("erro", containsString("'inicio' deve ser anterior a 'fim'"));
    }

    @Test
    void getRelatorioPorPeriodo_deveAceitarInicioIgualAFim() {
        given()
            .queryParam("inicio", "2024-01-01T00:00:00")
            .queryParam("fim",    "2024-01-01T00:00:00")   // mesmo instante
            .when().get("/relatorio/periodo")
            .then()
            .statusCode(200);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private RelatorioSemanalDTO relatorioVazio() {
        LocalDateTime inicio = LocalDateTime.of(2024, 1, 8, 0, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2024, 1, 15, 23, 59, 59);
        return new RelatorioSemanalDTO(inicio, fim, LocalDateTime.now(), 0, 0.0,
                Collections.emptyList(), Collections.emptyList(),
                List.of(
                        new AvaliacoesPorUrgenciaDTO(Urgencia.CRITICA, 0, 0.0),
                        new AvaliacoesPorUrgenciaDTO(Urgencia.MEDIA,   0, 0.0),
                        new AvaliacoesPorUrgenciaDTO(Urgencia.BOA,     0, 0.0)
                ));
    }

    private RelatorioSemanalDTO relatorioComDados() {
        LocalDateTime inicio = LocalDateTime.of(2024, 1, 8, 0, 0, 0);
        LocalDateTime fim    = LocalDateTime.of(2024, 1, 15, 23, 59, 59);

        List<FeedbackResumoDTO> feedbacks = List.of(
                new FeedbackResumoDTO(UUID.randomUUID(), "Ótima aula",   9, Urgencia.BOA,    inicio.plusDays(1)),
                new FeedbackResumoDTO(UUID.randomUUID(), "Regular",      6, Urgencia.MEDIA,  inicio.plusDays(2)),
                new FeedbackResumoDTO(UUID.randomUUID(), "Ruim demais",  3, Urgencia.CRITICA, inicio.plusDays(3))
        );
        List<AvaliacoesPorDiaDTO> porDia = List.of(
                new AvaliacoesPorDiaDTO(inicio.plusDays(1).toLocalDate(), 1),
                new AvaliacoesPorDiaDTO(inicio.plusDays(2).toLocalDate(), 1),
                new AvaliacoesPorDiaDTO(inicio.plusDays(3).toLocalDate(), 1)
        );
        List<AvaliacoesPorUrgenciaDTO> porUrgencia = List.of(
                new AvaliacoesPorUrgenciaDTO(Urgencia.CRITICA, 1, 33.3),
                new AvaliacoesPorUrgenciaDTO(Urgencia.MEDIA,   1, 33.3),
                new AvaliacoesPorUrgenciaDTO(Urgencia.BOA,     1, 33.3)
        );

        return new RelatorioSemanalDTO(inicio, fim, LocalDateTime.now(), 3, 6.0, feedbacks, porDia, porUrgencia);
    }
}