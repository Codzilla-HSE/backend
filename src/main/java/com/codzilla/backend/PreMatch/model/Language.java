package com.codzilla.backend.PreMatch.model;

public enum Language implements Option {
    CPP(54),
    PY(71),
    SQL(0),
    JAVA(63);

    private final int value;

    Language(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
