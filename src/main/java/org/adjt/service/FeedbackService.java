package org.adjt.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.adjt.entity.Feedback;
import org.adjt.enums.Urgencia;
import org.adjt.repository.FeedbackRepository;

@ApplicationScoped
public class FeedbackService {

    @Inject
    FeedbackRepository feedbackRepository;

    @Transactional
    public void salvar(String descricao, int nota) {
        Feedback feedback = new Feedback();
        feedback.descricao = descricao;
        feedback.nota = nota;
        feedback.urgencia = Urgencia.fromNota(nota);
        feedbackRepository.salvar(feedback);
    }
}
