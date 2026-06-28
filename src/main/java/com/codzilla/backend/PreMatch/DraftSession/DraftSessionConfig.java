package com.codzilla.backend.PreMatch.DraftSession;


import com.codzilla.backend.PreMatch.model.*;
import com.codzilla.backend.judge.problem.Problem;
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
                Category.ProblemLevel,
                Language.CPP.name(),
                Category.ProblemLevel,
                Language.JAVA.name(),
                Category.ProblemLevel,
                ProblemType.ALGORITHM.name(),
                Category.Language,
                ProblemType.MATH.name(),
                Category.Language,
                ProblemType.DATA_STRUCTURES.name(),
                Category.Language,
                ProblemType.SQL.name(),
                Category.ProblemLevel
                );
    }
}
