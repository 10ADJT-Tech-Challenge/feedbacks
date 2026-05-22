package org.adjt.enums;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UrgenciaTest {

    @ParameterizedTest(name = "nota {0} -> {1}")
    @CsvSource({
            "0,  CRITICA",
            "2,  CRITICA",
            "4,  CRITICA",
            "5,  MEDIA",
            "6,  MEDIA",
            "7,  MEDIA",
            "8,  BOA",
            "9,  BOA",
            "10, BOA"
    })
    void deveClassificarCorretamente(int nota, Urgencia esperada) {
        assertEquals(esperada, Urgencia.fromNota(nota));
    }
}