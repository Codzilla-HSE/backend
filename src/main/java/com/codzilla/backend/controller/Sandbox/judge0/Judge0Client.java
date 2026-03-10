package com.codzilla.backend.controller.Sandbox.judge0;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Slf4j
@Component
public class Judge0Client {

    private final RestClient restClient;

    public Judge0Client(@Value("${judge0.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    // отправить решение на один тест, вернуть токен
    public String submit(String sourceCode, int languageId, String stdin) {
        var request = new SubmissionRequest(sourceCode, languageId, stdin);

        SubmissionResponse response = restClient.post()
                .uri("/submissions?wait=true")  // wait=true — ждём результат сразу
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(SubmissionResponse.class);

        log.info("Judge0 response: status={}, stdout={}, stderr={}",
                response.getStatus(), response.getStdout(), response.getStderr());

        return response.getStatus().getDescription();
    }



    @Data
    public static class SubmissionRequest {
        @JsonProperty("source_code")
        private String sourceCode;

        @JsonProperty("language_id")
        private int languageId;

        private String stdin;

        public SubmissionRequest(String sourceCode, int languageId, String stdin) {
            this.sourceCode = sourceCode;
            this.languageId = languageId;
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