package com.codzilla.backend.controller.Sandbox.problem;

import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
import com.codzilla.backend.controller.Sandbox.polygon.CreateProblemRequest;
import com.codzilla.backend.controller.Sandbox.polygon.PolygonClient;
import com.codzilla.backend.controller.Sandbox.polygon.PolygonProblem;
import com.codzilla.backend.test.Test;
import com.codzilla.backend.test.TestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

    private final PolygonClient polygonClient;
    private final ProblemRepository problemRepository;
    private final Judge0Client judge0Client;
    private final TestRepository testRepository;

    public Problem createProblem(CreateProblemRequest request) {
        String polygonId = polygonClient.createProblem(request.getName());

        Problem problem = new Problem();
        problem.setPolygonToken(polygonId);
        problem.setType(request.getType());
        problem.setLevel(request.getLevel());
        Problem saved = problemRepository.save(problem);

        // сохраняем тесты в Polygon И в нашу БД
        var tests = request.getTests();
        for (int i = 0; i < tests.size(); i++) {
            var t = tests.get(i);
            polygonClient.saveTest(polygonId, i + 1, t.getInput(), t.getOutput());

            Test test = new Test();
            test.setProblemId(saved.getId());
            test.setTestIndex(i + 1);
            test.setInput(t.getInput());
            test.setExpectedOutput(t.getOutput());
            testRepository.save(test);
        }

        return saved;
    }

    public String submitSolution(Long problemId, String sourceCode, int languageId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        // берём тесты из НАШЕЙ БД
        List<Test> tests = testRepository.findByProblemIdOrderByTestIndex(problemId);
        if (tests.isEmpty()) {
            throw new RuntimeException("No tests found for problem: " + problemId);
        }

        List<String> results = new ArrayList<>();
        for (Test test : tests) {
            String verdict = judge0Client.submit(sourceCode, languageId, test.getInput());
            results.add("Test " + test.getTestIndex() + ": " + verdict);
            if (!"Accepted".equals(verdict)) break;
        }

        return String.join("\n", results);
    }
}