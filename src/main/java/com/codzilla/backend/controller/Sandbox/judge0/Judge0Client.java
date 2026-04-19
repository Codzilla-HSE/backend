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


    public String submitAsync(String sourceCode, int languageId, String stdin,
                              String expectedOutput) {
        try {
            String body = objectMapper.writeValueAsString(
                    new SubmissionRequest(
                            sourceCode,
                            languageId,
                            stdin,
                            expectedOutput
                    )
            );


            String raw = restClient.post()
                                   .uri("/submissions?base64_encoded=false&fields=stdout")
                                   .contentType(MediaType.APPLICATION_JSON)
                                   .body(body)
                                   .retrieve()
                                   .body(String.class);

            TokenResponse tokenResponse = objectMapper.readValue(
                    raw,
                    TokenResponse.class
            );
            log.info(
                    "Submission get token: {}",
                    tokenResponse.getToken()
            );
            return tokenResponse.getToken();

        } catch (Exception e) {
            log.error(
                    "Judge0 submission failed",
                    e
            );
            return null;
        }
    }

    public SubmissionResponse getSubmissionStatus(String token) {
        try {
            String raw = restClient.get()
                                   .uri("/submissions/" + token + "?base64_encoded=false")
                                   .retrieve()
                                   .body(String.class);
            log.info(
                    "Raw data from judge: {}",
                    raw
            );
            return objectMapper.readValue(
                    raw,
                    SubmissionResponse.class
            );
        } catch (Exception e) {
            log.error(
                    "Failed to fetch status for token: " + token,
                    e
            );
            return null;
        }
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TokenResponse {
        private String token;
    }


    public static class SubmissionRequest {
        public String source_code;
        public Integer language_id;
        public String stdin;
        String expectedOutput;

        public SubmissionRequest(String sourceCode, int languageId, String stdin,
                                 String expectedOutput) {
            this.source_code = sourceCode;
            this.language_id = languageId;
            this.stdin = stdin;
            this.expectedOutput = expectedOutput;

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