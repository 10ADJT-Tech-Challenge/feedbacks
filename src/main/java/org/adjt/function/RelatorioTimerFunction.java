package org.adjt.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.adjt.dto.RelatorioSemanalDTO;
import org.adjt.service.EmailService;
import org.adjt.service.RelatorioService;

import java.util.logging.Level;

/**
 * Azure Function com Timer Trigger que executa toda semana (domingo à meia-noite).
 * Schedule no formato NCRONTAB: {segundo} {minuto} {hora} {dia} {mês} {dia-da-semana}
 * "0 0 0 * * 0" = todo domingo à 00:00:00
 * A variável de ambiente RELATORIO_SCHEDULE permite sobrescrever o agendamento
 * sem recompilar (ex.: "0 0 8 * * 1" para toda segunda às 8h).
 */
@ApplicationScoped
public class RelatorioTimerFunction {

    @Inject
    RelatorioService relatorioService;

    @Inject
    EmailService emailService;

    @Transactional
    @FunctionName("relatorio-semanal-timer")
    public void executar(
            @TimerTrigger(name = "timerInfo", schedule = "%RELATORIO_SCHEDULE%") String timerInfo,
            ExecutionContext context
    ) {
        context.getLogger().info("Timer acionado — iniciando geração do relatório semanal. Info: " + timerInfo);
        try {
            RelatorioSemanalDTO relatorio = relatorioService.gerarRelatorioSemanal();
            emailService.enviarRelatorioSemanal(relatorio);
            context.getLogger().info(
                    "Relatório semanal gerado e enviado com sucesso. Total de avaliações: " + relatorio.totalAvaliacoes()
            );
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Falha ao gerar ou enviar o relatório semanal", e);
            throw e;
        }
    }
}
