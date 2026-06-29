package com.codzilla.backend.judge.client;

import com.codzilla.backend.judge.problem.ProblemSqlResponseDTO;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.UUID;

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



    public Long submitSolution(Long taskId, String userId, String query, UUID matchId) {
        try {
            SubmitRequest request = new SubmitRequest();
            request.setTaskId(taskId);
            request.setMatchId(matchId);
            request.setUserId(userId);
            request.setUserSqlQuery(query);

            String body = objectMapper.writeValueAsString(request);
            String raw = restClient.post()
                    .uri("/sqlservice/submissions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            log.info("SqlService submit raw response: {}", raw);


            JsonNode root = objectMapper.readTree(raw);
            if (!root.has("success") || !root.get("success").asBoolean()) {
                String error = root.has("error") ? root.get("error").asText() : "Unknown error";
                throw new RuntimeException("SqlService error: " + error);
            }
            long id = root.get("data").asLong();
            log.info("SqlService accepted submission id={}", id);
            return id;
        } catch (Exception e) {
            throw new RuntimeException("Failed to submit SQL solution", e);
        }
    }



    public SubmissionStatus getSubmissionStatus(Long submissionId) {
        try {
            String raw = restClient.get()
                    .uri("/sqlservice/submissions/" + submissionId)
                    .retrieve()
                    .body(String.class);

            log.debug("SqlService status raw response: {}", raw);


            JsonNode root = objectMapper.readTree(raw);
            if (!root.has("success") || !root.get("success").asBoolean()) {
                String error = root.has("error") ? root.get("error").asText() : "Unknown error";
                throw new RuntimeException("SqlService error: " + error);
            }
            JsonNode data = root.get("data");
            return objectMapper.treeToValue(data, SubmissionStatus.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get SQL submission " + submissionId, e);
        }
    }



    public RandomSqlTaskResponse getRandomTask(String level) {
        try {
            String raw = restClient.get()
                    .uri("/sqlservice/tasks/random?level=" + level.toUpperCase())
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            if (!root.get("success").asBoolean()) {
                String error = root.has("error") ? root.get("error").asText() : "Unknown error";
                throw new RuntimeException("SqlService error: " + error);
            }
            JsonNode data = root.get("data");

            RandomSqlTaskResponse response = new RandomSqlTaskResponse();
            response.setId(data.get("taskId").asLong());
            response.setName(data.get("title").asText());
            response.setLevel(data.get("complexity").asText());
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch random SQL task for level " + level, e);
        }
    }

    public ProblemSqlResponseDTO getArtefactsOfSqlProblem(Long problemId) {
        try {
            String raw = restClient.get()
                                   .uri("/sqlservice/tasks/" + problemId)
                                   .retrieve()
                                   .body(String.class);

            JsonNode root = objectMapper.readTree(raw);
            if (!root.get("success").asBoolean()) {
                String error = root.has("error") ? root.get("error").asText() : "Unknown error";
                throw new RuntimeException("SqlService error: " + error);
            }
            JsonNode data = root.get("data");

            ProblemSqlResponseDTO response = new ProblemSqlResponseDTO();
            response.setName(data.get("title").asText());
            response.setDescription(data.get("description").asText());
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get artefacts of problem: {}" , e);
        }
    }



    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RandomSqlTaskResponse {
        private Long id;
        private String name;
        private String level;
    }

    @Data
    public static class SubmitRequest {
        private Long taskId;
        private String userId;
        private String userSqlQuery;
        private UUID matchId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubmitResponse {
        private Long id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubmissionStatus {
        private Long submissionId;
        private String status;
        private String verdict;
        private Long executionTimeMs;
    }
}