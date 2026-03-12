package com.codzilla.backend.controller.Sandbox.judge0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

@Slf4j
@Component
public class Judge0Client {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Judge0Client(@Value("${judge0.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String submit(String sourceCode, int languageId, String stdin) {
        try {
            String body = objectMapper.writeValueAsString(new SubmissionRequest(sourceCode, languageId, stdin));
            log.info("Sending to Judge0 body: {}", body);

            String raw = restClient.post()
                    .uri("/submissions?wait=true")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .onStatus(status -> true, (req, res) -> {})
                    .body(String.class);

            log.info("Judge0 response: {}", raw);

            SubmissionResponse response = objectMapper.readValue(raw, SubmissionResponse.class);
            return response.getStatus().getDescription();

        } catch (Exception e) {
            throw new RuntimeException("Judge0 error: " + e.getMessage(), e);
        }
    }

    public static class SubmissionRequest {
        public String source_code;
        public Integer language_id;
        public String stdin;

        public SubmissionRequest(String sourceCode, int languageId, String stdin) {
            this.source_code = sourceCode;
            this.language_id = languageId;
            this.stdin = stdin;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubmissionResponse {
        private String stdout;
        private String stderr;
        private String compile_output;
        private Status status;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Status {
            private int id;
            private String description;
        }
    }
}