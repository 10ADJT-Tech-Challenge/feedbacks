package org.adjt.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.adjt.enums.Urgencia;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback")
public class Feedback extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    public String descricao;

    @Column(name = "nota", nullable = false)
    public Integer nota;

    @Enumerated(EnumType.STRING)
    @Column(name = "urgencia", nullable = false)
    public Urgencia urgencia;

    @Column(name = "data_envio", nullable = false)
    public LocalDateTime dataEnvio;
}