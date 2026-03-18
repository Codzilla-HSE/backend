package com.codzilla.backend.kafka;

import com.codzilla.backend.controller.Sandbox.problem.ProblemService;
import com.codzilla.backend.websocket.ResultWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionConsumer {

    private final ProblemService problemService;
    private final ResultWebSocketHandler webSocketHandler;

    @KafkaListener(topics = "submissions", groupId = "codzilla-group")
    public void consume(SubmissionMessage message) {
        log.info("Received from Kafka: {}", message.getSubmissionId());
        try {
            String result = problemService.submitSolution(
                    message.getProblemId(),
                    message.getSourceCode(),
                    message.getLanguageId()
            );
            webSocketHandler.sendResult(message.getSubmissionId(), result);
        } catch (Exception e) {
            log.error("Error processing submission: {}", e.getMessage());
            webSocketHandler.sendResult(message.getSubmissionId(), "Error: " + e.getMessage());
        }
    }
}