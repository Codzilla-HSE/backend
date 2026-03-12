package com.codzilla.backend.controller.Sandbox.problem;

import com.codzilla.backend.controller.Sandbox.judge0.Judge0Client;
import com.codzilla.backend.controller.Sandbox.polygon.CreateProblemRequest;
import com.codzilla.backend.controller.Sandbox.polygon.PolygonClient;
import com.codzilla.backend.controller.Sandbox.polygon.PolygonProblem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemService {

    private final PolygonClient polygonClient;
    private final ProblemRepository problemRepository;
    private final Judge0Client judge0Client;


    public Problem createProblem(CreateProblemRequest request) {
        String polygonId = polygonClient.createProblem(request.getName());

        var tests = request.getTests();
        for (int i = 0; i < tests.size(); i++) {
            var test = tests.get(i);
            polygonClient.saveTest(polygonId, i + 1, test.getInput(), test.getOutput());
        }

        Problem problem = new Problem();
        problem.setPolygonToken(polygonId);
        problem.setType(request.getType());
        problem.setLevel(request.getLevel());

        return problemRepository.save(problem);
    }

    public String submitSolution(Long problemId, String sourceCode, int languageId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        PolygonProblem polygonProblem = polygonClient.getProblemTests(problem.getPolygonToken());


        List<String> results = new ArrayList<>();
        for (var test : polygonProblem.getResult()) {
            String verdict = judge0Client.submit(sourceCode, languageId, test.getInput());
            results.add("Test " + test.getIndex() + ": " + verdict);
            if (!"Accepted".equals(verdict)) break;
        }

        return String.join("\n", results);
    }

}