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

    // ✅ ДОЛЖЕН ПРОХОДИТЬ — задача сохраняется в БД
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

    // ❌ ПАДАЕТ — createProblem не вызывает polygonClient.createProblem()
    // Polygon не знает о задаче, polygonToken содержит просто name а не реальный ID
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

    // ✅ ДОЛЖЕН ПРОХОДИТЬ — сабмит возвращает токен
    @Test
    void submitSolution_shouldReturnToken() {
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(polygonProblemService.getTests("test-problem-1")).thenReturn(List.of(test));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn("judge0-token-123");
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        String result = problemService.submitSolution(1L, 1L, "print(3)", 71);

        assertThat(result).contains("judge0-token-123");
    }

    // ❌ ПАДАЕТ — submitSolution берёт только tests.get(0), игнорирует остальные тесты
    @Test
    void submitSolution_shouldRunAllTests() {
        PolygonProblem.Test test2 = new PolygonProblem.Test();
        test2.setIndex(2);
        test2.setInput("5 10");
        test2.setOutput("15");

        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(polygonProblemService.getTests("test-problem-1")).thenReturn(List.of(test, test2));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn("token");
        when(submissionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        problemService.submitSolution(1L, 1L, "print(3)", 71);

        // Падает — judge0Client вызывается только 1 раз вместо 2
        verify(judge0Client, times(2)).submitAsync(anyString(), anyInt(), anyString(), anyString());
    }

    // ❌ ПАДАЕТ — problem not found должен бросать исключение
    @Test
    void submitSolution_shouldThrowWhenProblemNotFound() {
        when(problemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> problemService.submitSolution(1L, 99L, "code", 71))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Problem not found");
    }

    // ❌ ПАДАЕТ — expectedOutput не сериализуется правильно в Judge0
    // поле называется expectedOutput но Judge0 ожидает expected_output
    @Test
    void judge0Client_expectedOutputFieldNameShouldBeCorrect() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper =
                new com.fasterxml.jackson.databind.ObjectMapper();

        Judge0Client.SubmissionRequest request =
                new Judge0Client.SubmissionRequest("print(3)", 71, "1 2", "3");

        String json = mapper.writeValueAsString(request);

        // Падает — в JSON будет "expectedOutput" а не "expected_output"
        assertThat(json).contains("\"expected_output\"");
        assertThat(json).contains("\"3\"");
    }

    // ✅ ДОЛЖЕН ПРОХОДИТЬ — submission сохраняется со статусом IN_QUEUE
    @Test
    void submitSolution_shouldSaveSubmissionWithInQueueStatus() {
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(polygonProblemService.getTests("test-problem-1")).thenReturn(List.of(test));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn("token-123");

        Submission[] saved = new Submission[1];
        when(submissionRepository.save(any())).thenAnswer(inv -> {
            saved[0] = inv.getArgument(0);
            return saved[0];
        });

        problemService.submitSolution(1L, 1L, "print(3)", 71);

        assertThat(saved[0]).isNotNull();
        assertThat(saved[0].getStatus()).isEqualTo(Submission.Status.IN_QUEUE);
        assertThat(saved[0].getJudge0Token()).isEqualTo("token-123");
        assertThat(saved[0].getProblemId()).isEqualTo(1L);
    }

    // ❌ ПАДАЕТ — когда judge0Client возвращает null (не смог отправить)
    // сабмит всё равно сохраняется с null токеном — это баг
    @Test
    void submitSolution_shouldHandleJudge0Failure() {
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));
        when(polygonProblemService.getTests("test-problem-1")).thenReturn(List.of(test));
        when(judge0Client.submitAsync(anyString(), anyInt(), anyString(), anyString()))
                .thenReturn(null); // Judge0 недоступен

        // Должен бросить исключение, но не бросает — сохраняет null токен
        assertThatThrownBy(() -> problemService.submitSolution(1L, 1L, "print(3)", 71))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Judge0 unavailable");
    }
    
}