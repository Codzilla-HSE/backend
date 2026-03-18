package com.codzilla.backend.submission;

import com.codzilla.backend.kafka.SubmissionMessage;
import com.codzilla.backend.kafka.SubmissionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionProducer submissionProducer;

    public String upload(Long problemId, int languageId, MultipartFile file) throws Exception {
        // 1. читаем байты файла
        byte[] bytes = file.getBytes();

        // 2. сохраняем в БД
        String submissionUuid = UUID.randomUUID().toString();
        Submission submission = new Submission();
        submission.setProblemId(problemId);
        submission.setLanguageId(languageId);
        submission.setSourceCode(bytes);
        submission.setStatus("PENDING");
        submission.setSubmissionUuid(submissionUuid);
        submissionRepository.save(submission);

        // 3. отправляем в Kafka (байты → String)
        String sourceCode = new String(bytes);
        submissionProducer.send(new SubmissionMessage(submissionUuid, problemId, sourceCode, languageId));

        return submissionUuid;
    }

    public void updateStatus(String submissionUuid, String status) {
        submissionRepository.findAll().stream()
                .filter(s -> submissionUuid.equals(s.getSubmissionUuid()))
                .findFirst()
                .ifPresent(s -> {
                    s.setStatus(status);
                    submissionRepository.save(s);
                });
    }
}