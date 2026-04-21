package com.codzilla.backend.Sandbox.integration;

import com.codzilla.backend.S3.S3Initialization;
import com.codzilla.backend.User.UserRepository;

import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
import com.codzilla.backend.controller.Sandbox.polygon.PolygonClient;
import com.codzilla.backend.controller.Sandbox.polygon.PolygonProblem;
import com.codzilla.backend.controller.Sandbox.problem.Problem;
import com.codzilla.backend.controller.Sandbox.problem.ProblemRepository;
import com.codzilla.backend.controller.Sandbox.submission.Submission;
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
    private com.codzilla.backend.controller.Sandbox.problem.ProblemTestRepository problemTestRepository;

    @MockitoBean
    private com.codzilla.backend.controller.Sandbox.submission.SubmissionTestRepository submissionTestRepository;

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
        Problem problem = new Problem();
        problem.setId(1L);
        problem.setPolygonToken("533472");
        problem.setType(Problem.ProblemType.ALGORITHM);
        problem.setLevel(Problem.ProblemLevel.EASY);


        when(polygonClient.createProblem(anyString())).thenReturn("533472");


        when(problemRepository.save(any())).thenReturn(problem);


        when(problemRepository.findById(1L)).thenReturn(Optional.of(problem));


        PolygonProblem.Test test = new PolygonProblem.Test();
        test.setIndex(1);
        test.setInput("1 2");
        test.setOutput("3");

        when(polygonProblemService.getTests("517936")).thenReturn(List.of(test));
        when(judge0Client.submitAsync(any(), anyInt(), any(), any())).thenReturn("token-123");
        com.codzilla.backend.controller.Sandbox.problem.ProblemTest pt =
                new com.codzilla.backend.controller.Sandbox.problem.ProblemTest();
        pt.setProblemId(1L);
        pt.setTestIndex(1);
        pt.setInput("1 2");
        pt.setExpectedOutput("3");

        when(problemTestRepository.findAllByProblemIdOrderByTestIndex(1L))
                .thenReturn(List.of(pt));

        Submission savedSub = new Submission();
        savedSub.setId(42L);
        savedSub.setStatus(Submission.Status.IN_QUEUE);
        when(submissionRepository.save(any())).thenReturn(savedSub);
        when(submissionTestRepository.save(any())).thenAnswer(i -> i.getArgument(0));

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

        mockMvc.perform(post("/problems/1/submit")
                        .param("languageId", "71")
                        .content("print(3)")
                        .contentType("text/plain"))
                .andExpect(status().isOk());
    }
}