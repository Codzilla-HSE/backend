package com.codzilla.backend.controller.Sandbox.judge0;

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
                response.status(), response.stdout(), response.stderr());

        return response.status().description();
    }

    record SubmissionRequest(
            String source_code,
            int language_id,
            String stdin
    ) {}

    record SubmissionResponse(
            String stdout,
            String stderr,
            String compile_output,
            Status status
    ) {
        record Status(int id, String description) {}
    }
}