package com.codzilla.backend.PreMatch.model;

public enum Category {
    Language(Language.class),
    ProblemType(ProblemType.class),
    ProblemLevel(ProblemLevel.class);

    private final Class<? extends Enum<?>> enumClass;

    Category(Class<? extends Enum<?>> enumClass) {
        this.enumClass = enumClass;
    }

    public Class<? extends Enum<?>> getEnumClass() {
        return enumClass;
    }
}