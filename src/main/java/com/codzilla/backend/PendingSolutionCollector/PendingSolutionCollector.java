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

    public PendingSolutionCollector(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }
//    @Autowired
//    TestRunner testRunner;

    @Scheduled(fixedRate = 1000)
    void startTestSubmissions() {
        var toTestSubmissions = submissionService.getAndSetTestingStatusFor50PendingSubmissions();

        for (var submission : toTestSubmissions) {
            try {
//                testRunner.startTest(submission);
                log.info("Start test: " + submission);
            } catch (Exception e) {
                log.info("error while testing " + submission);
            }
        }
    }
}
