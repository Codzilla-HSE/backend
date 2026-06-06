package com.codzilla.backend.PreMatch.model;

import com.codzilla.backend.controller.Sandbox.problem.Problem;
import lombok.Getter;

@Getter
public enum Category {
    Language(Language.class),
    ProblemType(Problem.ProblemType.class),
    ProblemLevel(Problem.ProblemLevel.class);

    private final Class<? extends Option> optionClass;

    Category(Class<? extends Option> enumClass) {
        this.optionClass = enumClass;
    }
}