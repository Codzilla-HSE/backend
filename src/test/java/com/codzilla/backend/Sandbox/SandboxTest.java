package com.codzilla.backend.Sandbox;

import com.codzilla.backend.PreMatch.model.ProblemType;
import com.codzilla.backend.judge.client.Artefactik0Client;
import com.codzilla.backend.judge.judge0.Judge0Client;
import com.codzilla.backend.judge.problem.*;
import com.codzilla.backend.judge.submission.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SandboxTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private Judge0Client judge0Client;

    @Mock
    private Artefactik0Client artefactik0Client;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private SubmissionTestRepository submissionTestRepository;

    @Mock
    private com.codzilla.backend.judge.client.SqlServiceClient sqlServiceClient;

    @Mock
    private com.codzilla.backend.S3.S3Repository s3Repository;

    @InjectMocks
    private ProblemService problemService;

    private Problem problem;
    private Artefactik0Client.TestCase testCase;

    @BeforeEach
    void setUp() {
        problem = new Problem();
        problem.setId(1L);
        problem.setExternalId(1001L);        // бывший polygonToken
        problem.setType(ProblemType.ALGORITHM);
        problem.setLevel(Problem.ProblemLevel.EASY);

        testCase = new Artefactik0Client.TestCase();
        testCase.setInput("1 2");
        testCase.setOutput("3");
    }

    @Test
    void createAlgoProblem_shouldSaveProblem() {
        CreateAlgoProblemRequest request = new CreateAlgoProblemRequest();
        request.setName("test-problem-1");
        request.setTimeLimit(1000);
        request.setMemoryLimit(256);
        request.setStatement("...");
        request.setGeneratorCode("...");
        request.setInputs(Collections.singletonList("..."));

        when(artefactik0Client.createProblem(any()))
                .thenReturn(12345L);
        when(problemRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Problem result = problemService.createAlgoProblem(request);

        assertThat(result).isNotNull();
        assertThat(result.getExternalId()).isEqualTo(12345L);
        verify(problemRepository, times(1)).save(any());
    }

    @Test
    void submitSolution_shouldThrowWhenProblemNotFound() {
        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                problemService.submitSolution(UUID.randomUUID(), null, 99L, "code", 71))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Problem not found");
    }

    @Test
    void judge0Client_expectedOutputFieldNameShouldBeCorrect() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        Judge0Client.SubmissionRequest request =
                new Judge0Client.SubmissionRequest("print(3)", 71, "1 2", "3");

        String json = mapper.writeValueAsString(request);

        assertThat(json).contains("\"expected_output\"");
        assertThat(json).contains("\"3\"");
    }

    @Test
    void submitSolution_shouldReturnToken() {
        Submission savedSub = new Submission();
        savedSub.setId(42L);
        savedSub.setStatus(Submission.Status.IN_QUEUE);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(artefactik0Client.getTests(problem.getExternalId()))
                .thenReturn(List.of(testCase));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), isNull()))
                .thenReturn("judge0-token-123");
        when(submissionRepository.save(any())).thenReturn(savedSub);
        when(submissionTestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = problemService.submitSolution(UUID.randomUUID(), null, 1L, "print(3)", 71);

        assertThat(result).isEqualTo("42");
    }

    @Test
    void submitSolution_shouldRunAllTests() {
        Submission savedSub = new Submission();
        savedSub.setId(42L);

        Artefactik0Client.TestCase test1 = testCase;
        Artefactik0Client.TestCase test2 = new Artefactik0Client.TestCase();
        test2.setInput("5 10");
        test2.setOutput("15");

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(artefactik0Client.getTests(problem.getExternalId()))
                .thenReturn(List.of(test1, test2));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), isNull()))
                .thenReturn("token");
        when(submissionRepository.save(any())).thenReturn(savedSub);
        when(submissionTestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        problemService.submitSolution(UUID.randomUUID(), null, 1L, "print(3)", 71);

        verify(judge0Client, times(2)).submitAsync(anyString(), anyInt(), anyString(), isNull());
    }

    @Test
    void submitSolution_shouldSaveSubmissionWithInQueueStatus() {
        Submission[] saved = new Submission[1];
        Submission savedSub = new Submission();
        savedSub.setId(42L);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(artefactik0Client.getTests(problem.getExternalId()))
                .thenReturn(List.of(testCase));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), isNull()))
                .thenReturn("token-123");
        when(submissionRepository.save(any())).thenAnswer(inv -> {
            saved[0] = inv.getArgument(0);
            saved[0].setId(42L);
            return saved[0];
        });
        when(submissionTestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        problemService.submitSolution(UUID.randomUUID(), null, 1L, "print(3)", 71);

        assertThat(saved[0]).isNotNull();
        assertThat(saved[0].getStatus()).isEqualTo(Submission.Status.IN_QUEUE);
        assertThat(saved[0].getProblemId()).isEqualTo(1L);
    }

    @Test
    void submitSolution_shouldHandleJudge0Failure() {
        Submission savedSub = new Submission();
        savedSub.setId(42L);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(artefactik0Client.getTests(problem.getExternalId()))
                .thenReturn(List.of(testCase));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), isNull()))
                .thenReturn(null);
        when(submissionRepository.save(any())).thenReturn(savedSub);

        assertThatThrownBy(() ->
                problemService.submitSolution(UUID.randomUUID(), null, 1L, "print(3)", 71))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Judge0 unavailable");
    }
}