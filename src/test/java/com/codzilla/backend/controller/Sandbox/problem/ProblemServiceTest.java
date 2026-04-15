//package com.codzilla.backend.controller.Sandbox.problem;
//
//import com.codzilla.backend.controller.Sandbox.polygon.CreateProblemRequest;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class ProblemServiceTest {
//
//    @Autowired
//    ProblemService problemService;
//
//    @Test
//    void createProblemInit() {
//        CreateProblemRequest request =
//                CreateProblemRequest.builder()
//                                    .name("test")
//                                    .type(Problem.ProblemType.ALGORITHM)
//                                    .level(Problem.ProblemLevel.EASY)
//                                    .tests(List.of(new CreateProblemRequest.TestCase(
//                                            "1 2",
//                                            "3"
//                                    )))
//                                    .build();
//        problemService.createProblem(request);
//    }
//}