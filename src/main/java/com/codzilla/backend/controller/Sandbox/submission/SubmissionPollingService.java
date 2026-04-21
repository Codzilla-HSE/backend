package com.codzilla.backend.controller.Sandbox.submission;

import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionPollingService {

    private final SubmissionRepository submissionRepository;
    private final SubmissionTestRepository submissionTestRepository;
    private final Judge0Client judge0Client;

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
        String actual = response.getStdout() == null ? "" : response.getStdout().trim();

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
        log.info("Test {} of submission {} → {}",
                subTest.getTestIndex(), subTest.getSubmissionId(), subTest.getStatus());
    }

    private void updateSubmissionStatus(Long submissionId) {
        List<SubmissionTest> allTests = submissionTestRepository
                .findAllBySubmissionIdOrderByTestIndex(submissionId);

        boolean allDone = allTests.stream()
                .allMatch(t -> t.getStatus() != SubmissionTest.Status.IN_QUEUE);

        if (!allDone) return;

        // ищем первый провальный тест
        SubmissionTest firstFailed = allTests.stream()
                .filter(t -> t.getStatus() != SubmissionTest.Status.ACCEPTED)
                .findFirst()
                .orElse(null);

        Submission sub = submissionRepository.findById(submissionId).orElse(null);
        if (sub == null) return;

        if (firstFailed == null) {
            sub.setStatus(Submission.Status.ACCEPTED);
            sub.setResultDetails("All " + allTests.size() + " tests passed");
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
    }
}