package org.adjt.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import jakarta.inject.Inject;
import org.adjt.service.FeedbackService;

import java.util.Optional;

public class FeedbackHttpFunction {
    @Inject
    FeedbackService service;

    @FunctionName("ReceberFeedback")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.ANONYMOUS) HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {

        final String descricao = request.getQueryParameters().get("descricao");
        final String notaParam = request.getQueryParameters().get("nota");

        if (descricao == null || descricao.isBlank() || notaParam == null || notaParam.isBlank()) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Parametros 'descricao' e 'nota' sao obrigatorios na query string")
                    .build();
        }

        final int nota;
        try {
            nota = Integer.parseInt(notaParam);
        } catch (NumberFormatException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body("Parametro 'nota' deve ser um numero inteiro")
                    .build();
        }

        service.salvar(descricao, nota);

        return request.createResponseBuilder(HttpStatus.CREATED).build();
    }
}
