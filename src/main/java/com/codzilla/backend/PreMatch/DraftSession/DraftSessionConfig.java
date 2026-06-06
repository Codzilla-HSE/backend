package com.codzilla.backend.PreMatch.DraftSession;


import com.codzilla.backend.PreMatch.model.*;
import com.codzilla.backend.controller.Sandbox.problem.Problem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.Map;

@Configuration
public class DraftSessionConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        return scheduler;
    }

    @Bean
    public Map<String, Category> categorySequence() {
        return Map.of(
                Language.PY.name(),
                Category.ProblemType,
                Language.CPP.name(),
                Category.ProblemType,
                Language.JAVA.name(),
                Category.ProblemType,
                Language.SQL.name(),
                Category.ProblemLevel,

                Problem.ProblemType.ALGORITHM.name(),
                Category.ProblemLevel,
                Problem.ProblemType.MATH.name(),
                Category.ProblemLevel,
                Problem.ProblemType.DATA_STRUCTURES.name(),
                Category.ProblemLevel
                );
    }
}
