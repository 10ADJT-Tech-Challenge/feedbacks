package org.adjt.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public record AvaliacoesPorDiaDTO(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate data,
        long quantidade
) {}