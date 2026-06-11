package com.codzilla.backend.judge.problem;

import com.codzilla.backend.PreMatch.model.ProblemType;
import com.codzilla.backend.judge.client.Artefactik0Client;
import com.codzilla.backend.judge.client.SqlServiceClient;
import com.codzilla.backend.judge.judge0.Judge0Client;
import com.codzilla.backend.judge.submission.Submission;
import com.codzilla.backend.judge.submission.SubmissionRepository;
import com.codzilla.backend.judge.submission.SubmissionTest;
import com.codzilla.backend.judge.submission.SubmissionTestRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final Judge0Client judge0Client;
    private final Artefactik0Client artefactik0Client;
    private final SqlServiceClient sqlServiceClient;
    private final SubmissionRepository submissionRepository;
    private final SubmissionTestRepository submissionTestRepository;

    // Создать ALGO-задачу: регистрируем в Artefactik0, сохраняем externalId
    public Problem createAlgoProblem(CreateAlgoProblemRequest request) {
        Artefactik0Client.CreateProblemRequest artefaktRequest =
                new Artefactik0Client.CreateProblemRequest();
        artefaktRequest.setName(request.getName());
        artefaktRequest.setTimeLimit(request.getTimeLimit());
        artefaktRequest.setMemoryLimit(request.getMemoryLimit());
        artefaktRequest.setStatement(request.getStatement());
        artefaktRequest.setGeneratorCode(request.getGeneratorCode());
        artefaktRequest.setInputs(request.getInputs());

        Long externalId = artefactik0Client.createProblem(artefaktRequest);

        Problem problem = new Problem();
        problem.setName(request.getName());
        problem.setExternalId(externalId);
        problem.setType(ProblemType.ALGORITHM);
        problem.setLevel(request.getLevel());
        return problemRepository.save(problem);
    }

    // Зарегистрировать SQL-задачу: задача уже есть в SqlService, просто сохраняем ссылку
    public Problem registerSqlProblem(RegisterSqlProblemRequest request) {
        Problem problem = new Problem();
        problem.setName(request.getName());
        problem.setExternalId(request.getSqlServiceTaskId());
        problem.setType(ProblemType.SQL);
        problem.setLevel(request.getLevel());
        return problemRepository.save(problem);
    }

    // Отправить решение — ветвление по типу задачи
    public String submitSolution(UUID userId, Long problemId, String sourceCode, int languageId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        return switch (problem.getType()) {
            case ALGORITHM -> submitAlgo(userId, problem, sourceCode, languageId);
            case SQL  -> submitSql(userId, problem, sourceCode);
            //TODO ( не понятно что к чему )
            case MATH -> null;
            case DATA_STRUCTURES -> null;
        };
    }

    // ALGO: тесты из Artefactik0 → каждый тест в Judge0
    private String submitAlgo(UUID userId, Problem problem, String sourceCode, int languageId) {
        List<Artefactik0Client.TestCase> tests =
                artefactik0Client.getTests(problem.getExternalId());

        if (tests == null || tests.isEmpty()) {
            throw new RuntimeException(
                    "No tests in Artefactik0 for problem " + problem.getId());
        }

        Submission sub = new Submission();
        sub.setProblemId(problem.getId());
        sub.setUserId(userId);
        sub.setLanguageId(languageId);
        sub.setStatus(Submission.Status.IN_QUEUE);
        Submission saved = submissionRepository.save(sub);

        for (int i = 0; i < tests.size(); i++) {
            Artefactik0Client.TestCase test = tests.get(i);

            String token = judge0Client.submitAsync(
                    sourceCode, languageId, test.getInput(), null);

            if (token == null) {
                throw new RuntimeException("Judge0 unavailable");
            }

            SubmissionTest subTest = new SubmissionTest();
            subTest.setSubmissionId(saved.getId());
            subTest.setTestIndex(i + 1);
            subTest.setJudge0Token(token);
            subTest.setExpectedOutput(
                    test.getOutput() == null ? "" : test.getOutput().trim());
            subTest.setStatus(SubmissionTest.Status.IN_QUEUE);
            submissionTestRepository.save(subTest);
        }

        log.info("ALGO submission {} for problem {} with {} tests",
                saved.getId(), problem.getId(), tests.size());
        return saved.getId().toString();
    }

    // SQL: полностью делегируем в SqlService
    private String submitSql(UUID userId, Problem problem, String query) {
        Long sqlSubmissionId = sqlServiceClient.submitSolution(
                problem.getExternalId(),
                userId.toString(),
                query
        );
        log.info("SQL submission delegated, sqlSubmissionId={}", sqlSubmissionId);
        // Префикс "sql:" — чтобы /status знал куда идти
        return "sql:" + sqlSubmissionId;
    }

    /**
     * Получить или создать алгоритмическую задачу (ALGORITHM / DATA_STRUCTURES / MATH)
     * @param type  тип задачи (пока всегда "ALGORITHM" как заглушка)
     * @param level уровень сложности
     */
    @Transactional
    public Problem getOrCreateRandomAlgoProblem(String type, String level) {
        // 1. Запросить случайную задачу из Artefactik0
        Artefactik0Client.RandomProblemResponse external =
                artefactik0Client.getRandomProblem(type, level);

        // 2. Преобразовать строковый тип в enum (пока заглушка -> ALGORITHM)
        ProblemType problemType;
        try {
            problemType = ProblemType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            problemType = ProblemType.ALGORITHM;
        }

        // 3. Поиск в локальной БД
        ProblemType finalProblemType = problemType;
        return problemRepository.findByExternalIdAndType(external.getId(), problemType)
                .orElseGet(() -> {
                    Problem problem = new Problem();
                    problem.setName(external.getName());
                    problem.setExternalId(external.getId());
                    problem.setType(finalProblemType);
                    problem.setLevel(Problem.ProblemLevel.valueOf(external.getLevel().toUpperCase()));
                    log.info("Created new ALGO problem in local DB: id={}, externalId={}",
                            problem.getId(), problem.getExternalId());
                    return problemRepository.save(problem);
                });
    }

    /**
     * Получить или создать SQL-задачу
     */
    @Transactional
    public Problem getOrCreateRandomSqlProblem(String level) {
        SqlServiceClient.RandomSqlTaskResponse external = sqlServiceClient.getRandomTask(level);

        return problemRepository.findByExternalIdAndType(external.getId(), ProblemType.SQL)
                .orElseGet(() -> {
                    Problem problem = new Problem();
                    problem.setName(external.getName());
                    problem.setExternalId(external.getId());
                    problem.setType(ProblemType.SQL);
                    problem.setLevel(Problem.ProblemLevel.valueOf(external.getLevel().toUpperCase()));
                    log.info("Created new SQL problem in local DB: id={}, externalId={}",
                            problem.getId(), problem.getExternalId());
                    return problemRepository.save(problem);
                });
    }
}
