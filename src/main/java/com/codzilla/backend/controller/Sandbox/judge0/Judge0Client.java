package com.codzilla.backend.controller.Sandbox.judge0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class Judge0Client {

    private final RestClient restClient;

    public Judge0Client(@Value("${judge0.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String submit(String sourceCode, int languageId, String stdin) {
        var request = new SubmissionRequest(sourceCode, languageId, stdin);

        log.info("Sending to Judge0: source_code={}, language_id={}, stdin={}",
                request.source_code, request.language_id, request.stdin);

        SubmissionResponse response = restClient.post()
                .uri("/submissions?wait=true")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(SubmissionResponse.class);

        assert response != null;
        log.info("Judge0 response: status={}, stdout={}, stderr={}",
                response.getStatus(), response.getStdout(), response.getStderr());

        return response.getStatus().getDescription();
    }

    // БЕЗ @Data и БЕЗ private — публичные поля, Jackson читает напрямую
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