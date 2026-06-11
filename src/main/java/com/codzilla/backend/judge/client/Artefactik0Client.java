package com.codzilla.backend.judge.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
public class Artefactik0Client {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Artefactik0Client(@Value("${artefactik0.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // Создать задачу в Artefactik0, вернуть её id
    public Long createProblem(CreateProblemRequest request) {
        try {
            String body = objectMapper.writeValueAsString(request);
            String raw = restClient.post()
                    .uri("/api/problems")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            ProblemResponse response = objectMapper.readValue(raw, ProblemResponse.class);
            log.info("Artefactik0 created problem id={}", response.getId());
            return response.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create problem in Artefactik0", e);
        }
    }

    // Получить тесты задачи по её id в Artefactik0
    public List<TestCase> getTests(Long problemId) {
        try {
            // Используем ParameterizedTypeReference для получения списка
            List<TestCase> tests = restClient.get()
                    .uri("/api/problems/" + problemId + "/tests")
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<TestCase>>() {});
            if (tests == null) {
                throw new RuntimeException("No tests returned from Artefactik0 for problem " + problemId);
            }
            log.info("Artefactik0 returned {} tests for problem {}", tests.size(), problemId);
            return tests;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch tests from Artefactik0 for problem " + problemId, e);
        }
    }


    /**
     * Получить случайную задачу из Artefactik0 по типу и сложности.
     * @param type   ALGORITHM, DATA_STRUCTURES, MATH (пока заглушка -> ALGORITHM)
     * @param level  EASY, MEDIUM, HARD
     */
    public RandomProblemResponse getRandomProblem(String type, String level) {
        try {
            // Пока type не поддерживается, но передаём для будущей совместимости
            String uri = UriComponentsBuilder.fromPath("/api/problems/random")
                    .queryParam("type", type.toUpperCase())
                    .queryParam("complexity", level.toUpperCase())
                    .build().toString();

            String raw = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readValue(raw, RandomProblemResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch random problem from Artefactik0", e);
        }
    }

    // ── DTOs ──────────────────────────────────────────────────────


    // DTO для ответа (только нужные поля)
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RandomProblemResponse {
        private Long id;
        private String name;
        private String level;   // EASY, MEDIUM, HARD
    }

    @Data
    public static class CreateProblemRequest {
        private String name;
        private Integer timeLimit;
        private Integer memoryLimit;
        private String statement;
        private String generatorCode;
        private List<String> inputs;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProblemResponse {
        private Long id;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TestsResponse {
        private List<TestCase> tests;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TestCase {
        private int index;
        private String input;
        private String output;
    }
}