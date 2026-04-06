package com.codzilla.backend.Submissions;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SubmissionService {

    final
    SubmissionDBRepository dbRepository;

    public SubmissionService(SubmissionDBRepository dbRepository) {
        this.dbRepository = dbRepository;
    }

    @Transactional
    public List<Submission> getAndSetTestingStatusFor50PendingSubmissions() {
        var pendingSubmissions = dbRepository.findTop50ByStatus(SubmissionStatus.PENDING);
        for (var submission : pendingSubmissions) {
            submission.setStatus(SubmissionStatus.TESTING);
        }
        return dbRepository.saveAll(pendingSubmissions);
    }
}
