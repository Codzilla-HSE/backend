    package com.codzilla.backend.Sandbox.submission;

    import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
    import com.codzilla.backend.controller.Sandbox.submission.*;
    import org.junit.jupiter.api.Test;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;

    import java.util.List;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.mockito.Mockito.when;

    @org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
    class SubmissionPollingTest {

        @Mock
        private SubmissionRepository submissionRepository;

        @Mock
        private Judge0Client judge0Client;

        @InjectMocks
        private SubmissionPollingService pollingService;

        @Test
        void polling_shouldUpdateStatus() {

            Submission sub = new Submission();
            sub.setId(1L);
            sub.setStatus(Submission.Status.IN_QUEUE);
            sub.setJudge0Token("token");

            when(submissionRepository.findAllByStatus(Submission.Status.IN_QUEUE))
                    .thenReturn(List.of(sub));

            Judge0Client.SubmissionResponse response = new Judge0Client.SubmissionResponse();
            Judge0Client.SubmissionResponse.Status status =
                    new Judge0Client.SubmissionResponse.Status();

            status.setId(3);
            status.setDescription("Accepted");
            response.setStatus(status);

            when(judge0Client.getSubmissionStatus("token")).thenReturn(response);

            pollingService.pollStatuses();

            assertThat(sub.getStatus()).isEqualTo(Submission.Status.ACCEPTED);
        }
    }