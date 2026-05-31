package org.adjt.function;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.QueueTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.adjt.dto.FeedbackResumoDTO;
import org.adjt.service.EmailService;

import java.util.logging.Level;

@ApplicationScoped
public class NotificarQueueFunction {

    @Inject
    EmailService emailService;

    @Inject
    ObjectMapper objectMapper;

    @FunctionName("NotificarCriticos")
    public void run(
            @QueueTrigger(name = "msg", queueName = "feedbacks-criticos", connection = "AzureWebJobsStorage") String mensagemQueue,
            final ExecutionContext context) {

        context.getLogger().info("Feedback crítico recebido da fila: " + mensagemQueue);
        try {
            FeedbackResumoDTO feedback = objectMapper.readValue(mensagemQueue, FeedbackResumoDTO.class);
            emailService.enviarAlertaCritico(feedback);
            context.getLogger().info("Alerta de feedback crítico enviado — ID: " + feedback.id());
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Falha ao processar feedback crítico da fila", e);
            throw new RuntimeException(e);
        }
    }
}
