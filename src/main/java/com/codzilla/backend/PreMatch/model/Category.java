package com.codzilla.backend.PreMatch.model;

import com.codzilla.backend.judge.problem.Problem;
import lombok.Getter;

@Getter
public enum Category {
    Language(Language.class),
    ProblemType(ProblemType.class),
    ProblemLevel(ProblemLevel.class);

    private final Class<? extends Option> optionClass;

    Category(Class<? extends Option> enumClass) {
        this.optionClass = enumClass;
    }
}