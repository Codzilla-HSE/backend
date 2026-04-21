    package com.codzilla.backend.Sandbox.submission;

    import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
    import com.codzilla.backend.controller.Sandbox.submission.*;
    import org.junit.jupiter.api.Test;
    import org.mockito.InjectMocks;
    import org.mockito.Mock;
    import org.mockito.junit.jupiter.MockitoExtension;
    import org.springframework.context.ApplicationEventPublisher;

    import java.util.List;
    import java.util.Optional;

    import static org.assertj.core.api.Assertions.assertThat;
    import static org.mockito.ArgumentMatchers.any;
    import static org.mockito.Mockito.when;

    @org.junit.jupiter.api.extension.ExtendWith(MockitoExtension.class)
    class SubmissionPollingTest {

        @Mock
        private SubmissionRepository submissionRepository;

        @Mock
        private Judge0Client judge0Client;

        @Mock
        ApplicationEventPublisher eventPublisher;

        @InjectMocks
        private SubmissionPollingService pollingService;
        @Mock
        private SubmissionTestRepository submissionTestRepository;

        @Test
        void polling_shouldUpdateStatus() {
            SubmissionTest subTest = new SubmissionTest();
            subTest.setId(1L);
            subTest.setSubmissionId(1L);
            subTest.setTestIndex(1);
            subTest.setJudge0Token("token");
            subTest.setExpectedOutput("3");
            subTest.setStatus(SubmissionTest.Status.IN_QUEUE);

            when(submissionTestRepository.findAllByStatus(SubmissionTest.Status.IN_QUEUE))
                    .thenReturn(List.of(subTest));

            Judge0Client.SubmissionResponse response = new Judge0Client.SubmissionResponse();
            Judge0Client.SubmissionResponse.Status status = new Judge0Client.SubmissionResponse.Status();
            status.setId(3);
            status.setDescription("Accepted");
            response.setStatus(status);
            response.setStdout("3\n");

            when(judge0Client.getSubmissionStatus("token")).thenReturn(response);

            Submission submission = new Submission();
            submission.setId(1L);
            submission.setStatus(Submission.Status.IN_QUEUE);

            when(submissionTestRepository.findAllBySubmissionIdOrderByTestIndex(1L))
                    .thenReturn(List.of(subTest));
            when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
            when(submissionTestRepository.save(any())).thenAnswer(i -> i.getArgument(0));
            when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

            pollingService.pollStatuses();

            assertThat(subTest.getStatus()).isEqualTo(SubmissionTest.Status.ACCEPTED);
            assertThat(submission.getStatus()).isEqualTo(Submission.Status.ACCEPTED);
        }
    }