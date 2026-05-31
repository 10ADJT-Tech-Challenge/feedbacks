package org.adjt.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.adjt.entity.Feedback;
import org.adjt.enums.Urgencia;
import org.adjt.repository.FeedbackRepository;

import java.time.LocalDateTime;

@ApplicationScoped
public class FeedbackService {

    @Inject
    FeedbackRepository repository;

    @Transactional
    public Feedback salvar(String descricao, int nota) {
        Feedback feedback = new Feedback();
        feedback.descricao = descricao;
        feedback.nota = nota;
        feedback.urgencia = Urgencia.fromNota(nota);
        feedback.dataEnvio = LocalDateTime.now();
        repository.persist(feedback);
        return feedback;
    }
}
