package com.codzilla.backend.PendingSolutionCollector;

import com.codzilla.backend.Submissions.SubmissionDBRepository;
import com.codzilla.backend.Submissions.SubmissionStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PendingSolutionCollector {
    @Autowired
    SubmissionDBRepository dbRepository;

//    @Autowired
//    TestRunner testRunner;

    @Scheduled(fixedRate = 1000)
    void startTestSubmissions() {
        var toTestSubmissions = dbRepository.findTop50ByStatus(SubmissionStatus.PENDING);
        for (var submission : toTestSubmissions) {
            submission.setStatus(SubmissionStatus.TESTING);
            dbRepository.save(submission);
            try {
//                testRunner.startTest(submission);
                log.info("Start test: " + submission);
            } catch (Exception e) {
                log.info("error while testing " + submission.getId());
            }
        }
    }
}
