package com.codzilla.backend.Submissions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.util.List;

@Slf4j
@Service
public class SubmissionService {

    final SubmissionDBRepository dbRepository;

    public SubmissionService(SubmissionDBRepository dbRepository) {
        this.dbRepository = dbRepository;
    }

    @Transactional
    public List<Submission> getAndSetTestingStatusForPendingSubmissions(int amount) {
        var pendingSubmissions =
                dbRepository.findByStatus(SubmissionStatus.PENDING, Limit.of(amount));
        for (var submission : pendingSubmissions) {
            submission.setStatus(SubmissionStatus.TESTING);
        }
        return dbRepository.saveAll(pendingSubmissions);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setError(Submission submission) {
        submission.setStatus(SubmissionStatus.ERROR);
        dbRepository.save(submission);
    }

    public void sendToTest(Submission s) throws CannotSendToTestException {
        log.info("Sending to test... : " + s.toString());
    }
}
