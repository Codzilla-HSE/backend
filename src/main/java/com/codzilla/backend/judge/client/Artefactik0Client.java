package com.codzilla.backend.judge.client;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

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

    // ──────────────────────────────────────────────
    // Создать задачу в Artefactik0
    // ──────────────────────────────────────────────

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
            log.info("Artefactik0 created problem with id={}", response.getId());
            return response.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create problem in Artefactik0", e);
        }
    }

    // ──────────────────────────────────────────────
    // Получить тесты задачи
    // ──────────────────────────────────────────────

    public List<TestCase> getTests(Long problemId) {
        try {
            String raw = restClient.get()
                    .uri("/api/problems/" + problemId + "/tests")
                    .retrieve()
                    .body(String.class);

            TestsResponse response = objectMapper.readValue(raw, TestsResponse.class);
            log.info("Artefactik0 returned {} tests for problem {}", response.getTests().size(), problemId);
            return response.getTests();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch tests from Artefactik0 for problem " + problemId, e);
        }
    }

    // ──────────────────────────────────────────────
    // DTOs
    // ──────────────────────────────────────────────

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
        private Integer timeLimit;
        private Integer memoryLimit;
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