package com.codzilla.backend.PreMatch.PicksBans;


import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.Language;
import com.codzilla.backend.PreMatch.model.ProblemLevel;
import com.codzilla.backend.PreMatch.model.ProblemType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Configuration
public class PicksBansConfig {

    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        return scheduler;
    }
}
