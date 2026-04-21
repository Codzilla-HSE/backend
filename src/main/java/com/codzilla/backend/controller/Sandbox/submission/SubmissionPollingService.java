package com.codzilla.backend.controller.Sandbox.submission;

import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
import com.codzilla.backend.controller.Sandbox.polygon.PolygonProblemService;
import com.codzilla.backend.controller.Sandbox.problem.Problem;
import com.codzilla.backend.controller.Sandbox.problem.ProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.codzilla.backend.controller.Sandbox.submission.SubmissionUpdatedEvent; 

import org.springframework.context.ApplicationEventPublisher; 

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ApplicationEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public List<Submission> getPendingSubmissions() {
        return submissionRepository.findAllByStatus(Submission.Status.IN_QUEUE);
    }

    @Scheduled(fixedDelay = 2000)
    public void pollStatuses() {
        List<Submission> pending = getPendingSubmissions(); // транзакция закрылась здесь

        for (Submission sub : pending) { // Judge0 вызывается уже без открытой транзакции
            var response = judge0Client.getSubmissionStatus(sub.getJudge0Token());
            if (response != null && response.getStatus() != null && response.getStatus().getId() > 2) {
                updateSubmissionStatus(sub, response);
            }
        }
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

        eventPublisher.publishEvent(new SubmissionUpdatedEvent(sub.getUserId()));
    }
}