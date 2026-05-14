package org.adjt.application.services;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FeedbackService {
    public String avaliacao(String nota) {
        return "Nota " + nota;
    }
}
