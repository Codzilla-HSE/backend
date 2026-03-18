package com.codzilla.backend.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubmissionProducer {

    private final KafkaTemplate<String, SubmissionMessage> kafkaTemplate;

    public void send(SubmissionMessage message) {
        log.info("Sending to Kafka: {}", message.getSubmissionId());
        kafkaTemplate.send("submissions", message.getSubmissionId(), message);
    }
}