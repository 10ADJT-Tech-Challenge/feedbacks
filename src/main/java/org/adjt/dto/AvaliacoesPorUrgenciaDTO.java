package org.adjt.dto;

import org.adjt.enums.Urgencia;

public record AvaliacoesPorUrgenciaDTO(
        Urgencia urgencia,
        long quantidade,
        double percentual
) {}