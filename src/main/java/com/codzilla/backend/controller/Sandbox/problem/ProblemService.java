package com.codzilla.backend.controller.Sandbox.problem;

import com.codzilla.backend.S3.S3Repository;
import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
import com.codzilla.backend.controller.Sandbox.polygon.*;

import com.codzilla.backend.controller.Sandbox.submission.Submission;
import com.codzilla.backend.controller.Sandbox.submission.SubmissionRepository;
import com.codzilla.backend.controller.Sandbox.submission.SubmissionTest;
import com.codzilla.backend.controller.Sandbox.submission.SubmissionTestRepository;
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
    private final PolygonClient polygonClient;
    private final S3Repository s3Repository;
    private final SubmissionTestRepository submissionTestRepository;
    private final ProblemTestRepository problemTestRepository;

    public Problem createProblem(CreateProblemRequest request) {
        String polygonId = polygonClient.createProblem(request.getName());

        Problem problem = new Problem();
        problem.setPolygonToken(polygonId);
        problem.setType(request.getType());
        problem.setLevel(request.getLevel());
        Problem saved = problemRepository.save(problem);

        if (request.getTests() != null) {
            for (int i = 0; i < request.getTests().size(); i++) {
                var t = request.getTests().get(i);

                polygonClient.saveTest(polygonId, i + 1, t.getInput(), t.getOutput());


                ProblemTest pt = new ProblemTest();
                pt.setProblemId(saved.getId());
                pt.setTestIndex(i + 1);
                pt.setInput(t.getInput());
                pt.setExpectedOutput(t.getOutput());
                problemTestRepository.save(pt);
            }
        }

        return saved;
    }

    public String submitSolution(UUID userId, Long problemId, String sourceCode, int languageId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));


        List<ProblemTest> tests = problemTestRepository
                .findAllByProblemIdOrderByTestIndex(problemId);

        if (tests.isEmpty()) {
            throw new RuntimeException("No tests found for problem " + problemId);
        }

        Submission sub = new Submission();
        sub.setProblemId(problemId);
        sub.setUserId(userId);

        sub.setLanguageId(languageId);
        sub.setStatus(Submission.Status.IN_QUEUE);
        Submission saved = submissionRepository.save(sub);
        s3Repository.save(sourceCode.getBytes(), "submissions/" + saved.getId());
        for (int i = 0; i < tests.size(); i++) {
            ProblemTest test = tests.get(i);

            String token = judge0Client.submitAsync(
                    sourceCode, languageId, test.getInput(), null
            );

            if (token == null) {
                throw new RuntimeException("Judge0 unavailable");
            }

            SubmissionTest subTest = new SubmissionTest();
            subTest.setSubmissionId(saved.getId());
            subTest.setTestIndex(i + 1);
            subTest.setJudge0Token(token);
            subTest.setExpectedOutput(test.getExpectedOutput().trim());
            subTest.setStatus(SubmissionTest.Status.IN_QUEUE);
            submissionTestRepository.save(subTest);
        }

        return saved.getId().toString();
    }
}