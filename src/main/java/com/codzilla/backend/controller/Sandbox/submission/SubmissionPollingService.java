package com.codzilla.backend.controller.Sandbox.submission;

import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
import com.codzilla.backend.controller.Sandbox.polygon.PolygonProblemService;
import com.codzilla.backend.controller.Sandbox.problem.Problem;
import com.codzilla.backend.controller.Sandbox.problem.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionPollingService {

    private final SubmissionRepository submissionRepository;
    private final Judge0Client judge0Client;
    final private PolygonProblemService polygonProblemService;
    final private ProblemRepository problemRepository;

    @Scheduled(fixedDelay = 2000)
    public void pollStatuses() {

        List<Submission> pendingSubmissions = submissionRepository.findAllByStatus(Submission.Status.IN_QUEUE);

        for (Submission sub : pendingSubmissions) {

            if (sub.getUpdatedAt().isBefore(LocalDateTime.now().minusSeconds(30))) {
                if (sub.getRetryCount() < 3) {
                    log.warn("Submission {} timed out. Retrying... (Attempt {})", sub.getId(), sub.getRetryCount() + 1);
                    restartSubmission(sub);
                    continue;
                } else {
                    sub.setStatus(Submission.Status.INTERNAL_ERROR);
                    sub.setResultDetails("Max retries reached. Judge0 is not responding.");
                    submissionRepository.save(sub);
                    continue;
                }
            }


            var response = judge0Client.getSubmissionStatus(sub.getJudge0Token());
            if (response != null && response.getStatus() != null) {
                if (response.getStatus().getId() > 2) {
                    updateSubmissionStatus(sub, response);
                }
            }
        }

    }

    private void restartSubmission(Submission sub) {

        Problem problem = problemRepository.findById(sub.getProblemId()).orElseThrow();
        var tests = polygonProblemService.getTests(problem.getPolygonToken());
        var mainTest = tests.get(0);


        String newToken = judge0Client.submitAsync(sub.getSourceCode(), sub.getLanguageId(), mainTest.getInput(), mainTest.getOutput());

        sub.setJudge0Token(newToken);
        sub.setRetryCount(sub.getRetryCount() + 1);
        sub.setUpdatedAt(LocalDateTime.now());
        submissionRepository.save(sub);
    }

    private void updateSubmissionStatus(Submission sub, Judge0Client.SubmissionResponse response) {
        String description = response.getStatus().getDescription();

        if ("Accepted".equals(description)) {
            sub.setStatus(Submission.Status.ACCEPTED);
        } else if (description.contains("Compile Error")) {
            sub.setStatus(Submission.Status.COMPILE_ERROR);
        } else if (description.contains("Wrong Answer")) {
            sub.setStatus(Submission.Status.WRONG_ANSWER);
        } else {
            sub.setStatus(Submission.Status.RUNTIME_ERROR);
        }

        sub.setResultDetails(description);
        submissionRepository.save(sub);

        log.info("Submission {} finished with verdict: {}", sub.getId(), description);



    }
}