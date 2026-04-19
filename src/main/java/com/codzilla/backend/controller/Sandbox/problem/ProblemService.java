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
@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final Judge0Client judge0Client;
    private final PolygonProblemService polygonProblemService;
    private final SubmissionRepository submissionRepository;
    private final PolygonClient polygonClient;

    public Problem createProblem(CreateProblemRequest request) {

        String polygonId = polygonClient.createProblem(request.getName());

        Problem problem = new Problem();
        problem.setPolygonToken(polygonId);
        problem.setType(request.getType());
        problem.setLevel(request.getLevel());

        if (request.getTests() != null) {
            for (int i = 0; i < request.getTests().size(); i++) {
                var t = request.getTests().get(i);
                polygonClient.saveTest(polygonId, i + 1, t.getInput(), t.getOutput());
            }
        }

        return problemRepository.save(problem);
    }

    public String submitSolution(Long userId, Long problemId, String sourceCode, int languageId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));
        List<PolygonProblem.Test> tests = polygonProblemService.getTests(problem.getPolygonToken());

        if (tests == null || tests.isEmpty()) {
            throw new RuntimeException("No tests found for problem " + problemId);
        }

        String lastToken = null;

        for (PolygonProblem.Test test : tests) {
            String token = judge0Client.submitAsync(
                    sourceCode,
                    languageId,
                    test.getInput(),
                    test.getOutput()
            );

            if (token == null) {
                throw new RuntimeException("Judge0 unavailable");
            }

            lastToken = token;
        }

        Submission sub = new Submission();
        sub.setProblemId(problemId);
        sub.setUserId(userId);
        sub.setSourceCode(sourceCode);
        sub.setLanguageId(languageId);
        sub.setJudge0Token(lastToken);
        sub.setStatus(Submission.Status.IN_QUEUE);

        submissionRepository.save(sub);

        return "Submitted! Your token: " + lastToken;
    }
}