package com.codzilla.backend.judge.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class SqlServiceClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SqlServiceClient(@Value("${sqlservice.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    // Отправить SQL-решение → вернуть id посылки в SqlService
    public Long submitSolution(Long taskId, String userId, String query) {
        try {
            SubmitRequest request = new SubmitRequest();
            request.setTaskId(taskId);
            request.setUserId(userId);
            request.setQuery(query);

            String body = objectMapper.writeValueAsString(request);
            String raw = restClient.post()
                    .uri("/sqlservice/submissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            SubmitResponse response = objectMapper.readValue(raw, SubmitResponse.class);
            log.info("SqlService accepted submission id={}", response.getId());
            return response.getId();
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit SQL solution", e);
        }
    }

    // Получить статус SQL-посылки
    public SubmissionStatus getSubmissionStatus(Long submissionId) {
        try {
            String raw = restClient.get()
                    .uri("/sqlservice/submissions/" + submissionId)
                    .retrieve()
                    .body(String.class);
            return objectMapper.readValue(raw, SubmissionStatus.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get SQL submission " + submissionId, e);
        }
    }


    // Получить случайную SQL-задачу по уровню сложности
    public RandomSqlTaskResponse getRandomTask(String level) {
        try {
            String raw = restClient.get()
                    .uri("/sqlservice/tasks/random?level=" + level) //TODO сделать такую ручку
                    .retrieve()
                    .body(String.class);
            return objectMapper.readValue(raw, RandomSqlTaskResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch random SQL task for level " + level, e);
        }
    }

    // ── DTOs ──────────────────────────────────────────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RandomSqlTaskResponse {
        private Long id;          // externalId задачи в SqlService
        private String name;
        private String level;     // EASY, MEDIUM, HARD
    }

    @Data
    public static class SubmitRequest {
        private Long taskId;
        private String userId;
        private String query;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubmitResponse {
        private Long id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubmissionStatus {
        private Long id;
        private String status;
        private String verdict;
        private Long executionTimeMs;
    }
}