package org.adjt.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.adjt.dto.RelatorioSemanalDTO;
import org.adjt.service.RelatorioService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Endpoint REST exposto como Azure HTTP Function via quarkus-azure-functions-http.
 * Rotas disponíveis:
 *   GET /api/relatorio/semanal              → relatório dos últimos 7 dias
 *   GET /api/relatorio/periodo?inicio=&fim= → relatório de período customizado
 *       Formato dos parâmetros: yyyy-MM-dd'T'HH:mm:ss (ex.: 2024-01-15T00:00:00)
 */
@Path("/relatorio")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Relatório", description = "Geração de relatórios semanais de feedbacks")
public class RelatorioResource {

    @Inject
    RelatorioService relatorioService;

    @GET
    @Path("/semanal")
    @Operation(summary = "Relatório dos últimos 7 dias", description = "Retorna o relatório com todos os feedbacks dos últimos 7 dias, médias, contagens por dia e por urgência.")
    public RelatorioSemanalDTO getRelatorioSemanal() {
        return relatorioService.gerarRelatorioSemanal();
    }

    @GET
    @Path("/periodo")
    @Operation(summary = "Relatório de período customizado", description = "Retorna o relatório para o período especificado pelos parâmetros inicio e fim (formato ISO: yyyy-MM-dd'T'HH:mm:ss).")
    public Response getRelatorioPorPeriodo(
            @QueryParam("inicio") String inicio,
            @QueryParam("fim") String fim
    ) {
        if (inicio == null || fim == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"erro\":\"Parâmetros 'inicio' e 'fim' são obrigatórios. Formato: yyyy-MM-dd'T'HH:mm:ss\"}")
                    .build();
        }
        try {
            LocalDateTime dataInicio = LocalDateTime.parse(inicio);
            LocalDateTime dataFim = LocalDateTime.parse(fim);

            if (dataInicio.isAfter(dataFim)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"erro\":\"'inicio' deve ser anterior a 'fim'\"}")
                        .build();
            }

            return Response.ok(relatorioService.gerarRelatorio(dataInicio, dataFim)).build();
        } catch (DateTimeParseException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"erro\":\"Formato de data inválido. Use: yyyy-MM-dd'T'HH:mm:ss\"}")
                    .build();
        }
    }
}