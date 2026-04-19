package com.codzilla.backend.controller.Sandbox.submission;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class SubmissionService {

    @Autowired
    SubmissionRepository submissionRepository;

    Submission.Status getSubmissionStatus(Long id) throws NoSuchElementException {
        Submission submission = submissionRepository.findById(id).get();
        return submission.getStatus();
    }
}
