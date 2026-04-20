package com.codzilla.backend.Sandbox;

import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
import com.codzilla.backend.controller.Sandbox.polygon.*;
import com.codzilla.backend.controller.Sandbox.problem.*;
import com.codzilla.backend.controller.Sandbox.submission.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SandboxTest {
    @Mock
    private ProblemTestRepository problemTestRepository;

    @Mock
    private SubmissionTestRepository submissionTestRepository;

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private Judge0Client judge0Client;

    @Mock
    private PolygonProblemService polygonProblemService;

    @Mock
    private SubmissionRepository submissionRepository;

    @Mock
    private PolygonClient polygonClient;

    @InjectMocks
    private ProblemService problemService;

    private Problem problem;
    private PolygonProblem.Test test;

    @BeforeEach
    void setUp() {
        problem = new Problem();
        problem.setId(1L);
        problem.setPolygonToken("test-problem-1");
        problem.setType(Problem.ProblemType.ALGORITHM);
        problem.setLevel(Problem.ProblemLevel.EASY);

        test = new PolygonProblem.Test();
        test.setIndex(1);
        test.setInput("1 2");
        test.setOutput("3");
    }


    @Test
    void createProblem_shouldSaveProblem() {
        CreateProblemRequest request = new CreateProblemRequest();
        request.setName("test-problem-1");
        request.setType(Problem.ProblemType.ALGORITHM);
        request.setLevel(Problem.ProblemLevel.EASY);

        when(polygonClient.createProblem(anyString())).thenReturn("517936");
        when(problemRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Problem result = problemService.createProblem(request);

        assertThat(result).isNotNull();
        assertThat(result.getPolygonToken()).isEqualTo("517936");
        verify(problemRepository, times(1)).save(any());
    }

    @Test
    void createProblem_shouldCallPolygonAPI() {
        when(polygonClient.createProblem(anyString())).thenReturn("517936");
        when(problemRepository.save(any())).thenReturn(problem);

        CreateProblemRequest request = new CreateProblemRequest();
        request.setName("test-problem-1");
        request.setType(Problem.ProblemType.ALGORITHM);
        request.setLevel(Problem.ProblemLevel.EASY);

        problemService.createProblem(request);

        verify(polygonClient, times(1)).createProblem("test-problem-1");
    }

    @Test
    void submitSolution_shouldThrowWhenProblemNotFound() {
        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.submitSolution(UUID.randomUUID(), 99L, "code", 71))
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
        when(problemTestRepository.findAllByProblemIdOrderByTestIndex(1L))
                .thenReturn(List.of(toProblemTest(test, 1L)));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), isNull()))
                .thenReturn("judge0-token-123");
        when(submissionRepository.save(any())).thenReturn(savedSub);
        when(submissionTestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = problemService.submitSolution(UUID.randomUUID(), 1L, "print(3)", 71);

        assertThat(result).isEqualTo("42");
    }

    @Test
    void submitSolution_shouldRunAllTests() {
        Submission savedSub = new Submission();
        savedSub.setId(42L);

        ProblemTest pt1 = toProblemTest(test, 1L);
        ProblemTest pt2 = new ProblemTest();
        pt2.setProblemId(1L);
        pt2.setTestIndex(2);
        pt2.setInput("5 10");
        pt2.setExpectedOutput("15");

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(problemTestRepository.findAllByProblemIdOrderByTestIndex(1L))
                .thenReturn(List.of(pt1, pt2));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), isNull()))
                .thenReturn("token");
        when(submissionRepository.save(any())).thenReturn(savedSub);
        when(submissionTestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        problemService.submitSolution(UUID.randomUUID(), 1L, "print(3)", 71);

        verify(judge0Client, times(2)).submitAsync(anyString(), anyInt(), anyString(), isNull());
    }

    @Test
    void submitSolution_shouldSaveSubmissionWithInQueueStatus() {
        Submission[] saved = new Submission[1];
        Submission savedSub = new Submission();
        savedSub.setId(42L);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(problemTestRepository.findAllByProblemIdOrderByTestIndex(1L))
                .thenReturn(List.of(toProblemTest(test, 1L)));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), isNull()))
                .thenReturn("token-123");
        when(submissionRepository.save(any())).thenAnswer(inv -> {
            saved[0] = inv.getArgument(0);
            saved[0].setId(42L);
            return saved[0];
        });
        when(submissionTestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        problemService.submitSolution(UUID.randomUUID(), 1L, "print(3)", 71);

        assertThat(saved[0]).isNotNull();
        assertThat(saved[0].getStatus()).isEqualTo(Submission.Status.IN_QUEUE);
        assertThat(saved[0].getProblemId()).isEqualTo(1L);
    }

    @Test
    void submitSolution_shouldHandleJudge0Failure() {
        Submission savedSub = new Submission();
        savedSub.setId(42L);

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(problemTestRepository.findAllByProblemIdOrderByTestIndex(1L))
                .thenReturn(List.of(toProblemTest(test, 1L)));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), isNull()))
                .thenReturn(null);
        when(submissionRepository.save(any())).thenReturn(savedSub);

        assertThatThrownBy(() -> problemService.submitSolution(UUID.randomUUID(), 1L, "print(3)", 71))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Judge0 unavailable");
    }

    private ProblemTest toProblemTest(PolygonProblem.Test t, Long problemId) {
        ProblemTest pt = new ProblemTest();
        pt.setProblemId(problemId);
        pt.setTestIndex(t.getIndex());
        pt.setInput(t.getInput());
        pt.setExpectedOutput(t.getOutput());
        return pt;
    }
}