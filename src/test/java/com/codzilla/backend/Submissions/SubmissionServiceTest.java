package com.codzilla.backend.Submissions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class SubmissionServiceTest {
    @Mock
    SubmissionDBRepository repository;

    @InjectMocks
    SubmissionService service;

    @Test
    void getAndSetTestingStatusForPendingSubmissions_DefaultCase() {
        Submission testSubmission = new Submission();
        testSubmission.setStatus(SubmissionStatus.PENDING);
        when(repository.findByStatus(SubmissionStatus.PENDING, Limit.of(1)))
                .thenReturn(List.of(testSubmission));
        service.getAndSetTestingStatusForPendingSubmissions(1);
        assertEquals(SubmissionStatus.TESTING, testSubmission.getStatus());
        verify(repository, times(1)).findByStatus(SubmissionStatus.PENDING, Limit.of(1));
        verify(repository, times(1)).saveAll(List.of(testSubmission));
    }

    @Test
    void setError_DefaultCase() {
        Submission testSubmission = new Submission();
        service.setError(testSubmission);
        assertEquals(SubmissionStatus.ERROR, testSubmission.getStatus());
        verify(repository, times(1)).save(testSubmission);
    }
}