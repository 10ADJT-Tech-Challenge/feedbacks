package org.adjt.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.microsoft.azure.functions.annotation.QueueOutput;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.adjt.dto.FeedbackEntradaDTO;
import org.adjt.dto.FeedbackResumoDTO;
import org.adjt.entity.Feedback;
import org.adjt.enums.Urgencia;
import org.adjt.service.FeedbackService;

import java.util.Optional;
import java.util.logging.Level;

@ApplicationScoped
public class FeedbackHttpFunction {

    @Inject
    FeedbackService feedbackService;

    @Inject
    ObjectMapper objectMapper;

    @FunctionName("ReceberFeedback")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            @QueueOutput(name = "outputQueue", queueName = "feedbacks-criticos", connection = "AzureWebJobsStorage")
            OutputBinding<String> outputQueue,
            final ExecutionContext context) {

        String body = request.getBody().orElse(null);
        if (body == null || body.isBlank()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Corpo da requisição obrigatório: {\"descricao\": \"...\", \"nota\": 0-10}")
                    .build();
        }

        try {
            FeedbackEntradaDTO entrada = objectMapper.readValue(body, FeedbackEntradaDTO.class);

            if (entrada.descricao() == null || entrada.descricao().isBlank()) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Campo 'descricao' é obrigatório")
                        .build();
            }
            if (entrada.nota() < 0 || entrada.nota() > 10) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .body("Campo 'nota' deve ser entre 0 e 10")
                        .build();
            }

            Feedback feedback = feedbackService.salvar(entrada.descricao(), entrada.nota());

            if (feedback.urgencia == Urgencia.CRITICA) {
                FeedbackResumoDTO mensagem = new FeedbackResumoDTO(
                        feedback.id, feedback.descricao, feedback.nota, feedback.urgencia, feedback.dataEnvio);
                outputQueue.setValue(objectMapper.writeValueAsString(mensagem));
                context.getLogger().info("Feedback crítico enfileirado — ID: " + feedback.id);
            }

            FeedbackResumoDTO resposta = new FeedbackResumoDTO(
                    feedback.id, feedback.descricao, feedback.nota, feedback.urgencia, feedback.dataEnvio);
            return request.createResponseBuilder(HttpStatus.CREATED)
                    .header("Content-Type", "application/json")
                    .body(objectMapper.writeValueAsString(resposta))
                    .build();

        } catch (JsonProcessingException e) {
            context.getLogger().warning("Payload inválido: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Payload JSON inválido")
                    .build();
        } catch (Exception e) {
            context.getLogger().log(Level.SEVERE, "Erro ao processar feedback", e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro interno ao salvar feedback")
                    .build();
        }
    }
}
