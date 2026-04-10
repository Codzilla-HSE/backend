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

    public Problem createProblem(CreateProblemRequest request) {
        // Мы не создаем задачу в Polygon через API (это невозможно),
        // но мы сохраняем её в нашу БД, указывая ID уже созданной в Polygon задачи.

        Problem problem = new Problem();
        // ВАЖНО: В запросе теперь должен приходить polygonId вручную созданной задачи
        problem.setPolygonToken(request.getName()); // или добавь поле polygonId в Request
        problem.setType(request.getType());
        problem.setLevel(request.getLevel());

        return problemRepository.save(problem);
    }

    public String submitSolution(Long userId, Long problemId, String sourceCode, int languageId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        // Берем тесты через наш новый идеальный прокси (с кэшем)
        List<PolygonProblem.Test> tests = polygonProblemService.getTests(problem.getPolygonToken());

        // Для простоты примера берем первый тест (в боевой системе нужно прогонять все)
        PolygonProblem.Test mainTest = tests.get(0);

        // 1. Отправляем в Judge0 и получаем токен
        String token = judge0Client.submitAsync(sourceCode, languageId, mainTest.getInput(), mainTest.getOutput());

        // 2. Сохраняем в БД со статусом IN_QUEUE
        Submission sub = new Submission();
        sub.setProblemId(problemId);
        sub.setSourceCode(sourceCode);
        sub.setLanguageId(languageId);
        sub.setJudge0Token(token);
        sub.setStatus(Submission.Status.IN_QUEUE);
        submissionRepository.save(sub);

        return "Submitted! Your token: " + token;
    }
}