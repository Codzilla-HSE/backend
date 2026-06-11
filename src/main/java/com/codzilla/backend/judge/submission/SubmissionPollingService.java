package com.codzilla.backend.judge.submission;

import com.codzilla.backend.judge.client.SqlServiceClient;
import com.codzilla.backend.judge.judge0.Judge0Client;
import com.codzilla.backend.PreMatch.MatchRoom.MatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.ApplicationEventPublisher;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionPollingService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionTestRepository submissionTestRepository;
    private final Judge0Client judge0Client;
    private final SqlServiceClient sqlServiceClient;
    private final ApplicationEventPublisher eventPublisher;
    private final MatchService matchService;

    @Transactional(readOnly = true)
    public List<Submission> getPendingSubmissions() {
        return submissionRepository.findAllByStatus(Submission.Status.IN_QUEUE);
    }

    @Scheduled(fixedDelay = 2000)
    public void pollStatuses() {
        List<SubmissionTest> pending = submissionTestRepository
                .findAllByStatus(SubmissionTest.Status.IN_QUEUE);

        for (SubmissionTest subTest : pending) {
            var response = judge0Client.getSubmissionStatus(subTest.getJudge0Token());
            if (response == null || response.getStatus() == null) continue;
            if (response.getStatus().getId() <= 2) continue; // ещё обрабатывается

            updateTestStatus(subTest, response);
            updateSubmissionStatus(subTest.getSubmissionId());
        }
    }

    private void updateTestStatus(SubmissionTest subTest, Judge0Client.SubmissionResponse response) {
        int statusId = response.getStatus().getId();

        // Декодируем stdout, если он пришёл в Base64
        String actual = response.getStdout();
        if (actual != null && !actual.isBlank()) {
            try {
                byte[] decoded = Base64.getDecoder().decode(actual);
                actual = new String(decoded, StandardCharsets.UTF_8).trim();
            } catch (IllegalArgumentException e) {
                // Не Base64 — оставляем как есть
                actual = actual.trim();
            }
        } else {
            actual = "";
        }

        subTest.setActualOutput(actual);

        if (statusId == 6) {
            subTest.setStatus(SubmissionTest.Status.COMPILE_ERROR);
        } else if (statusId >= 7 && statusId <= 12) {
            subTest.setStatus(SubmissionTest.Status.RUNTIME_ERROR);
        } else if (statusId == 3) {
            String expected = subTest.getExpectedOutput() == null ? "" : subTest.getExpectedOutput().trim();
            if (actual.equals(expected)) {
                subTest.setStatus(SubmissionTest.Status.ACCEPTED);
            } else {
                subTest.setStatus(SubmissionTest.Status.WRONG_ANSWER);
            }
        } else {
            subTest.setStatus(SubmissionTest.Status.RUNTIME_ERROR);
        }

        submissionTestRepository.save(subTest);
        log.info("Test {} of submission {} → {}", subTest.getTestIndex(), subTest.getSubmissionId(), subTest.getStatus());
    }

    private void updateSubmissionStatus(Long submissionId) {
        List<SubmissionTest> allTests = submissionTestRepository
                .findAllBySubmissionIdOrderByTestIndex(submissionId);

        boolean allDone = allTests.stream()
                .allMatch(t -> t.getStatus() != SubmissionTest.Status.IN_QUEUE);

        if (!allDone) return;

        SubmissionTest firstFailed = allTests.stream()
                .filter(t -> t.getStatus() != SubmissionTest.Status.ACCEPTED)
                .findFirst()
                .orElse(null);

        Submission sub = submissionRepository.findById(submissionId).orElse(null);
        if (sub == null) return;

        if (firstFailed == null) {
            sub.setStatus(Submission.Status.ACCEPTED);
            sub.setResultDetails("All " + allTests.size() + " tests passed");
            if (sub.getMatchId() != null) {
                matchService.finishMatch(sub.getMatchId(), sub.getUserId());
            }
        } else {
            Submission.Status status = switch (firstFailed.getStatus()) {
                case WRONG_ANSWER -> Submission.Status.WRONG_ANSWER;
                case COMPILE_ERROR -> Submission.Status.COMPILE_ERROR;
                default -> Submission.Status.RUNTIME_ERROR;
            };
            sub.setStatus(status);
            sub.setResultDetails(
                    "Failed on test " + firstFailed.getTestIndex() +
                            "\nExpected: " + firstFailed.getExpectedOutput() +
                            "\nGot: " + firstFailed.getActualOutput()
            );
        }

        submissionRepository.save(sub);
        log.info("Submission {} final verdict: {}", submissionId, sub.getStatus());
        eventPublisher.publishEvent(new SubmissionUpdatedEvent(sub.getUserId()));
    }

    @Scheduled(fixedDelay = 2000)
    public void pollSqlStatuses() {
        List<Submission> pendingSql = submissionRepository
                .findAllByStatusAndSqlSubmissionIdIsNotNull(Submission.Status.IN_QUEUE);

        for (Submission sub : pendingSql) {
            try {
                SqlServiceClient.SubmissionStatus sqlStatus =
                        sqlServiceClient.getSubmissionStatus(sub.getSqlSubmissionId());

                if (sqlStatus == null || sqlStatus.getStatus() == null) continue;

                String status = sqlStatus.getStatus();

                // SqlService возвращает PENDING пока обрабатывает
                if ("PENDING".equals(status)) continue;

                updateSqlSubmissionStatus(sub, sqlStatus);
            } catch (Exception e) {
                log.error("Failed to poll SQL submission {}: {}", sub.getId(), e.getMessage());
            }
        }
    }

    private void updateSqlSubmissionStatus(Submission sub, SqlServiceClient.SubmissionStatus sqlStatus) {
        String status = sqlStatus.getStatus();
        String verdict = sqlStatus.getVerdict();

        if ("DONE".equals(status) || "ACCEPTED".equals(status) || "WRONG_ANSWER".equals(status) || "ERROR".equals(status)) {
            boolean accepted = "ACCEPTED".equals(verdict);

            if (accepted) {
                sub.setStatus(Submission.Status.ACCEPTED);
                sub.setResultDetails("Accepted");
                if (sub.getMatchId() != null) {
                    matchService.finishMatch(sub.getMatchId(), sub.getUserId());
                }
            } else {
                Submission.Status localStatus = switch (verdict != null ? verdict : "") {
                    case "WRONG_ANSWER" -> Submission.Status.WRONG_ANSWER;
                    case "COMPILATION_ERROR" -> Submission.Status.COMPILE_ERROR;
                    case "TIME_LIMIT_EXCEEDED" -> Submission.Status.RUNTIME_ERROR;
                    default -> Submission.Status.RUNTIME_ERROR;
                };
                sub.setStatus(localStatus);
                sub.setResultDetails(sqlStatus.getVerdict() != null ? sqlStatus.getVerdict() : "Error");
            }

            submissionRepository.save(sub);
            log.info("SQL Submission {} final verdict: {}", sub.getId(), sub.getStatus());
            eventPublisher.publishEvent(new SubmissionUpdatedEvent(sub.getUserId()));
        }
    }
}