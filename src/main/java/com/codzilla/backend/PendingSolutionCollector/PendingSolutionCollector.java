package com.codzilla.backend.PendingSolutionCollector;

import com.codzilla.backend.Submissions.Submission;
import com.codzilla.backend.Submissions.SubmissionDBRepository;
import com.codzilla.backend.Submissions.SubmissionService;
import com.codzilla.backend.Submissions.SubmissionStatus;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.parser.TE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class PendingSolutionCollector {
    SubmissionService submissionService;
    private final int amountOfSubmissions = 50;
    public PendingSolutionCollector(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @Scheduled(fixedDelay = 1000)
    void startTestSubmissions() {
        var toTestSubmissions = // забираем под локом и сразу ставим Testing статус
                submissionService.getAndSetTestingStatusForPendingSubmissions(amountOfSubmissions);
        for (var submission : toTestSubmissions) {
            try {
                submissionService.sendToTest(submission);
            } catch (Exception e) {
                submissionService.setError(submission);
                log.info("error while testing " + submission);
            }
        }
    }
}
