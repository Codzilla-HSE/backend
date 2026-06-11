package com.codzilla.backend.Sandbox.controller;

import com.codzilla.backend.PreMatch.MatchRoom.Match;
import com.codzilla.backend.PreMatch.MatchRoom.MatchService;
import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.ProblemType;
import com.codzilla.backend.PreMatch.model.Language;
import com.codzilla.backend.S3.S3Repository;
import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserService;
import com.codzilla.backend.judge.client.SqlServiceClient;
import com.codzilla.backend.judge.problem.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// Правильные импорты для Spring Boot 4
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProblemController.class)
@AutoConfigureMockMvc(addFilters = false) // Полностью отключаем все фильтры безопасности
class ProblemControllerTest {

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private com.codzilla.backend.Authentication.JWTRequestFilter.JWTRequestFilter jwtRequestFilter;

    @MockitoBean
    private com.codzilla.backend.Authentication.JWTUtils.JWTUtils jwtUtils;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProblemService problemService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private com.codzilla.backend.judge.submission.SubmissionRepository submissionRepository;

    @MockitoBean
    private SqlServiceClient sqlServiceClient;

    @MockitoBean
    private S3Repository s3Repository;

    private User mockUser;
    private UUID matchId;
    private Match match;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .email("test@mail.com")
                .password("password")
                .nickname("tester")
                .build();
        mockUser.setId(UUID.randomUUID());

        matchId = UUID.randomUUID();
        match = new Match();
        match.setId(matchId);

        Problem problem = new Problem();
        problem.setId(1L);
        match.setProblem(problem);

        Map<Category, String> options = new HashMap<>();
        options.put(Category.Language, "JAVA");
        match.setOptions(options);
    }

    @Test
    void createProblem_shouldReturnSavedProblem() throws Exception {
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setExternalId(517936L);
        problem.setType(ProblemType.ALGORITHM);
        problem.setLevel(Problem.ProblemLevel.EASY);

        when(problemService.createAlgoProblem(any())).thenReturn(problem);

        String json = """
        {
            "name": "test-problem",
            "timeLimit": 1000,
            "memoryLimit": 256,
            "statement": "Print sum",
            "generatorCode": "generator()",
            "inputs": "1 2",
            "level": "EASY"
        }
        """;

        mockMvc.perform(post("/problems/algo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("517936")));
    }

    @Test
    void submit_shouldReturnResult() throws Exception {
        when(matchService.getMatchById(matchId)).thenReturn(match);
        when(problemService.submitSolution(any(UUID.class), any(UUID.class), anyLong(), anyString(), anyInt()))
                .thenReturn("token-123");

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "solution.py",
                MediaType.TEXT_PLAIN_VALUE,
                "print(3)".getBytes()
        );

        mockMvc.perform(multipart("/problems/submit/file")
                        .file(file)
                        .param("matchId", matchId.toString())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("token-123")));
    }
}