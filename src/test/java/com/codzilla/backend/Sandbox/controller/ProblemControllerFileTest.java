package com.codzilla.backend.Sandbox.controller;

import com.codzilla.backend.PreMatch.MatchRoom.Match;
import com.codzilla.backend.PreMatch.MatchRoom.MatchService;
import com.codzilla.backend.S3.S3Repository;
import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserService;
import com.codzilla.backend.judge.client.SqlServiceClient;
import com.codzilla.backend.judge.problem.*;
import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProblemController.class,
        excludeAutoConfiguration = {
                org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration.class,
                org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration.class,
                org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration.class
        }
)
class ProblemControllerFileTest {

    @MockitoBean
    private com.codzilla.backend.Authentication.JWTRequestFilter.JWTRequestFilter jwtRequestFilter;

    @MockitoBean
    private com.codzilla.backend.Authentication.JWTUtils.JWTUtils jwtUtils;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProblemService problemService;

    @MockitoBean
    private MatchService matchService;

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
        mockUser.setId(UUID.randomUUID());   // важно для submitFileByMatch

        matchId = UUID.randomUUID();

        // Создаём Match с нужными полями
        match = new Match();
        match.setId(matchId);
        match.setProblem(new Problem());
        match.getProblem().setId(1L);        // ID задачи
        Map<Category, String> options = new HashMap<>();
        options.put(Category.Language, "PYTHON");   // languageId = 71 (Python)
        match.setOptions(options);
    }

    @Test
    void submitFile_shouldWork() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "solution.py",
                MediaType.TEXT_PLAIN_VALUE,
                "print(3)".getBytes()
        );

        when(matchService.getMatchById(matchId)).thenReturn(match);
        when(problemService.submitSolution(any(UUID.class), any(UUID.class), anyLong(), anyString(), anyInt()))
                .thenReturn("Submitted!");

        mockMvc.perform(multipart("/problems/submit/file")
                        .file(file)
                        .param("matchId", matchId.toString())
                        .with(user(mockUser)))
                .andExpect(status().isOk());
    }
}