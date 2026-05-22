package org.adjt.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.adjt.entity.Feedback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class FeedbackRepository implements PanacheRepositoryBase<Feedback, UUID> {

    public List<Feedback> findByPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return list("dataEnvio >= ?1 and dataEnvio <= ?2", Sort.ascending("dataEnvio"), inicio, fim);
    }
}