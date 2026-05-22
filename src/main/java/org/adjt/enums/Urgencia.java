package org.adjt.enums;

public enum Urgencia {

    CRITICA,
    MEDIA,
    BOA;

    public static Urgencia fromNota(int nota) {
        if (nota <= 4) return CRITICA;
        if (nota <= 7) return MEDIA;
        return BOA;
    }
}