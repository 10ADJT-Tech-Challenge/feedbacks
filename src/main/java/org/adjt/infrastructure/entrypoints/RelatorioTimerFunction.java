package org.adjt.infrastructure.entrypoints;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

public class RelatorioTimerFunction {
    
    @FunctionName("GerarRelatorioSemanal")
    public void run(
            // Expressão CRON: Roda a cada 30 segundos
            @TimerTrigger(name = "timerInfo", schedule = "*/30 * * * * *") String timerInfo,
            final ExecutionContext context) {

        context.getLogger().info("Iniciando geração de relatório semanal...");
    }
}
