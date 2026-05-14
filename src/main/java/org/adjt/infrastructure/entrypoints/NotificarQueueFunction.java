package org.adjt.infrastructure.entrypoints;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;

public class NotificarQueueFunction {
    @FunctionName("NotificarCriticos")
    public void run(
            // Gatilho ouvindo uma fila específica na Azure
            @QueueTrigger(name = "msg", queueName = "feedbacks-criticos", connection = "AzureWebJobsStorage") String mensagemQueue,
            final ExecutionContext context) {

        context.getLogger().info("Processando feedback crítico da fila.");
    }
}
