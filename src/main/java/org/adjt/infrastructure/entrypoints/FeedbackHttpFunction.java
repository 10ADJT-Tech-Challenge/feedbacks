package org.adjt.infrastructure.entrypoints;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import jakarta.inject.Inject;
import org.adjt.application.services.FeedbackService;

import java.util.Optional;

public class FeedbackHttpFunction {
    @Inject
    FeedbackService service;

    @FunctionName("ReceberFeedback")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        context.getLogger().info("Java HTTP trigger processed a request.");

        // Repassa o JSON para o caso de uso persistir no banco
        // useCase.executar(request.getBody());

        // Parse query parameter
        final String query = request.getQueryParameters().get("grade");
        final String nota = request.getBody().orElse(query);

        if (nota == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass a 'grade' on the query string or in the request body").build();
        } else {
            return request.createResponseBuilder(HttpStatus.CREATED).body(service.avaliacao(nota) + "\n Feedback salvo!").build();
        }
    }
}
