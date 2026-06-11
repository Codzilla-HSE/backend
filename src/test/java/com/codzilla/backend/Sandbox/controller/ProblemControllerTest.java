package com.codzilla.backend.Sandbox.controller;

import com.codzilla.backend.PreMatch.MatchRoom.Match;
import com.codzilla.backend.PreMatch.MatchRoom.MatchService;
import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.ProblemType;
import com.codzilla.backend.S3.S3Repository;
import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserService;
import com.codzilla.backend.judge.client.SqlServiceClient;
import com.codzilla.backend.judge.problem.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProblemControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private MatchService matchService;

    @MockitoBean
    private com.codzilla.backend.Authentication.JWTRequestFilter.JWTRequestFilter jwtRequestFilter;

    @MockitoBean
    private com.codzilla.backend.Authentication.JWTUtils.JWTUtils jwtUtils;

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
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

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

        var auth = new UsernamePasswordAuthenticationToken(
                mockUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(post("/problems/algo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .with(authentication(auth)))
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

        var auth = new UsernamePasswordAuthenticationToken(
                mockUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        mockMvc.perform(multipart("/problems/submit/file")
                        .file(file)
                        .param("matchId", matchId.toString())
                        .with(authentication(auth)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("token-123")));
    }
}