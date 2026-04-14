package com.codzilla.backend.controller.Sandbox.problem;

import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
import com.codzilla.backend.controller.Sandbox.polygon.*;

import com.codzilla.backend.controller.Sandbox.submission.Submission;
import com.codzilla.backend.controller.Sandbox.submission.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final Judge0Client judge0Client;
    private final PolygonProblemService polygonProblemService;
    private final SubmissionRepository submissionRepository;

    public Problem createProblem(CreateProblemRequest request) {

        Problem problem = new Problem();

        problem.setPolygonToken(request.getName());
        problem.setType(request.getType());
        problem.setLevel(request.getLevel());

        return problemRepository.save(problem);
    }

    public String submitSolution(UUID userId, Long problemId, String sourceCode, int languageId) {
        Problem problem = problemRepository.findById(problemId)
                                           .orElseThrow(() -> new RuntimeException(
                                                   "Problem not found: " + problemId));


        List<PolygonProblem.Test> tests = polygonProblemService.getTests(problem.getPolygonToken());
        PolygonProblem.Test mainTest = tests.get(0);
        String token = judge0Client.submitAsync(
                sourceCode,
                languageId,
                mainTest.getInput(),
                mainTest.getOutput()
        );


        Submission sub = new Submission();
        sub.setProblemId(problemId);
        sub.setSourceCode(sourceCode);
        sub.setLanguageId(languageId);
        sub.setJudge0Token(token);
        sub.setStatus(Submission.Status.IN_QUEUE);
        sub.setUserId(userId);
        submissionRepository.save(sub);

        return "Submitted! Your token: " + token;
    }
}