package com.codzilla.backend.Sandbox.controller;

import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserService;
import com.codzilla.backend.controller.Sandbox.problem.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

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
    private UserService userService;

    @MockitoBean
    private com.codzilla.backend.controller.Sandbox.submission.SubmissionRepository submissionRepository;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .email("test@mail.com")
                .password("password")
                .nickname("tester")
                .build();
    }


    @Test
    void submitFile_shouldWork() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "solution.py",
                MediaType.TEXT_PLAIN_VALUE,
                "print(3)".getBytes()
        );

        when(userService.getIdByEmail("test@mail.com"))
                .thenReturn(UUID.randomUUID());

        when(problemService.submitSolution(any(UUID.class), anyLong(), anyString(), anyInt()))
                .thenReturn("Submitted!");

        mockMvc.perform(multipart("/problems/submit/file")
                        .file(file)
                        .param("problemId", "1")
                        .param("languageId", "71")
                        .with(user(mockUser)))
                .andExpect(status().isOk());
    }
}