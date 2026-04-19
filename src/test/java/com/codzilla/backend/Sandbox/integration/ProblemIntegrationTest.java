package com.codzilla.backend.Sandbox.integration;

import com.codzilla.backend.S3.S3Initialization;
import com.codzilla.backend.User.UserRepository;

import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
import com.codzilla.backend.controller.Sandbox.polygon.PolygonClient;
import com.codzilla.backend.controller.Sandbox.polygon.PolygonProblem;
import com.codzilla.backend.controller.Sandbox.problem.Problem;
import com.codzilla.backend.controller.Sandbox.problem.ProblemRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
                "spring.jpa.hibernate.ddl-auto=create-drop",

                "app.s3.enabled=false",

                "spring.autoconfigure.exclude=" +
                        "org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration," +
                        "org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration"
        }
)
@AutoConfigureMockMvc(addFilters = false)
class ProblemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProblemRepository problemRepository;
    @MockitoBean
    private S3Initialization s3Initialization;

    @MockitoBean
    private PolygonClient polygonClient;

    @MockitoBean
    private Judge0Client judge0Client;

    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private com.codzilla.backend.controller.Sandbox.polygon.PolygonProblemService polygonProblemService;

    @MockitoBean
    private com.codzilla.backend.controller.Sandbox.submission.SubmissionRepository submissionRepository;

    @Test
    void fullFlow_createAndSubmit() throws Exception {
        // подготовить problem который вернёт репозиторий
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setPolygonToken("517936");
        problem.setType(Problem.ProblemType.ALGORITHM);
        problem.setLevel(Problem.ProblemLevel.EASY);

        // мок polygon
        when(polygonClient.createProblem(anyString())).thenReturn("517936");

        // мок репозитория — save возвращает problem с id=1
        when(problemRepository.save(any())).thenReturn(problem);

        // мок для submit — findById возвращает problem
        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));

        // мок тестов из Polygon
        PolygonProblem.Test test = new PolygonProblem.Test();
        test.setIndex(1);
        test.setInput("1 2");
        test.setOutput("3");

        // нужен мок PolygonProblemService тоже
        // добавь его в класс:
        // @MockitoBean
        // private PolygonProblemService polygonProblemService;
        when(polygonProblemService.getTests("517936")).thenReturn(List.of(test));

        // мок judge0
        when(judge0Client.submitAsync(any(), anyInt(), any(), any())).thenReturn("token-123");

        // 1. CREATE
        mockMvc.perform(post("/problems/create")
                        .contentType("application/json")
                        .content("""
                    {
                      "name": "test-problem-check-1",
                      "type": "ALGORITHM",
                      "level": "EASY",
                      "tests": [{"input": "1 2", "output": "3"}]
                    }
                    """))
                .andExpect(status().isOk());

        // 2. SUBMIT
        mockMvc.perform(post("/problems/1/submit")
                        .param("languageId", "71")
                        .content("print(3)")
                        .contentType("text/plain"))
                .andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("token-123")));
    }
}