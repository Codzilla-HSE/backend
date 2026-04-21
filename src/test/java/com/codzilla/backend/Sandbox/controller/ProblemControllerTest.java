package com.codzilla.backend.Sandbox.controller;

import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserService;
import com.codzilla.backend.controller.Sandbox.problem.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
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
@AutoConfigureMockMvc(addFilters = false)
class ProblemControllerTest {

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
    private com.codzilla.backend.controller.Sandbox.submission.SubmissionRepository submissionRepository;


    @Test
    void createProblem_shouldReturnSavedProblem() throws Exception {

        Problem problem = new Problem();
        problem.setId(1L);
        problem.setPolygonToken("517936");
        problem.setType(Problem.ProblemType.ALGORITHM);
        problem.setLevel(Problem.ProblemLevel.EASY);

        when(problemService.createProblem(any())).thenReturn(problem);

        String json = """
        {
          "name": "test-problem",
          "type": "ALGORITHM",
          "level": "EASY"
        }
        """;

        mockMvc.perform(post("/problems/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.polygonToken").value("517936"));
    }


    @Test
    void submit_shouldReturnResult() throws Exception {

        when(problemService.submitSolution(any(UUID.class), anyLong(), anyString(), anyInt()))
                .thenReturn("Submitted! token-123");

        mockMvc.perform(post("/problems/1/submit")
                        .param("languageId", "71")
                        .content("print(3)")
                        .contentType(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("token-123")));
    }

}