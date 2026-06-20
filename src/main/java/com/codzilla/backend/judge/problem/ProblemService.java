package com.codzilla.backend.judge.problem;

import com.codzilla.backend.PreMatch.model.ProblemType;
import com.codzilla.backend.S3.S3Repository;
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

import java.util.*;

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
    private final S3Repository s3Repository;


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


    public Problem registerSqlProblem(RegisterSqlProblemRequest request) {
        Problem problem = new Problem();
        problem.setName(request.getName());
        problem.setExternalId(request.getSqlServiceTaskId());
        problem.setType(ProblemType.SQL);
        problem.setLevel(request.getLevel());
        return problemRepository.save(problem);
    }


    public String submitSolution(UUID userId, UUID matchId ,  Long problemId, String sourceCode, int languageId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found: " + problemId));

        return switch (problem.getType()) {
            case ALGORITHM, MATH, DATA_STRUCTURES -> submitAlgo(userId, problem, sourceCode, languageId , matchId);
            case SQL  -> submitSql(userId, problem, sourceCode , matchId);
        };
    }


    private String submitAlgo(UUID userId, Problem problem, String sourceCode, int languageId , UUID matchId) {
        List<Artefactik0Client.TestCase> tests =
                artefactik0Client.getTests(problem.getExternalId());

        if (tests == null || tests.isEmpty()) {
            throw new RuntimeException(
                    "No tests in Artefactik0 for problem " + problem.getId());
        }

        Submission sub = new Submission();
        sub.setProblemId(problem.getId());
        sub.setUserId(userId);
        sub.setMatchId(matchId);
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


    private String submitSql(UUID userId, Problem problem, String query, UUID matchId) {
        if (query == null || query.isBlank()) {
            throw new RuntimeException("SQL query cannot be empty");
        }

        Long sqlSubmissionId = sqlServiceClient.submitSolution(
                problem.getExternalId(),
                userId.toString(),
                query,
                matchId
        );
        log.info("SQL submission delegated, sqlSubmissionId={}", sqlSubmissionId);

        Submission sub = new Submission();
        sub.setProblemId(problem.getId());
        sub.setUserId(userId);
        sub.setMatchId(matchId);
        sub.setLanguageId(0);
        sub.setStatus(Submission.Status.IN_QUEUE);
        sub.setSqlSubmissionId(sqlSubmissionId);
        Submission saved = submissionRepository.save(sub);

        log.info("Local SQL submission {} created, sqlSubmissionId={}", saved.getId(), sqlSubmissionId);
        return saved.getId().toString();
    }

    /**
     * Получить или создать алгоритмическую задачу (ALGORITHM / DATA_STRUCTURES / MATH)
     * @param type  тип задачи (пока всегда "ALGORITHM" как заглушка)
     * @param level уровень сложности
     */
    @Transactional
    public Problem getOrCreateRandomAlgoProblem(String type, String level) {
        Artefactik0Client.RandomProblemResponse external =
                artefactik0Client.getRandomProblem(type, level);
        ProblemType problemType;
        try {
            problemType = ProblemType.valueOf(
                    external.getType() != null ? external.getType().toUpperCase() : type.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            problemType = ProblemType.ALGORITHM;
        }

        ProblemType finalProblemType = problemType;
        return problemRepository.findByExternalIdAndType(external.getId(), problemType)
                .orElseGet(() -> {
                    Problem problem = new Problem();
                    problem.setName(external.getName());
                    problem.setExternalId(external.getId());
                    problem.setType(finalProblemType);
                    problem.setLevel(Problem.ProblemLevel.valueOf(
                            external.getLevel().toUpperCase()));
                    log.info("Created new {} problem in local DB: externalId={}",
                            finalProblemType, problem.getExternalId());
                    return problemRepository.save(problem);
                });
    }

    /**
     * Получить или создать SQL-задачу
     */
    public Problem getOrCreateRandomSqlProblem(String level) {
        List<String> levelsToTry = Arrays.asList(level.toUpperCase(), "MEDIUM", "EASY");

        Set<String> uniqueLevels = new LinkedHashSet<>(levelsToTry);

        for (String lvl : uniqueLevels) {
            try {
                log.info("Trying to fetch SQL task for level: {}", lvl);
                SqlServiceClient.RandomSqlTaskResponse external = sqlServiceClient.getRandomTask(lvl);
                log.info("Fetched SQL task: id={}, name={}, level={}", external.getId(), external.getName(), external.getLevel());

                return problemRepository.findByExternalIdAndType(external.getId(), ProblemType.SQL)
                        .orElseGet(() -> {
                            Problem problem = new Problem();
                            problem.setName(external.getName());
                            problem.setExternalId(external.getId());
                            problem.setType(ProblemType.SQL);
                            problem.setLevel(Problem.ProblemLevel.valueOf(external.getLevel().toUpperCase()));
                            log.info("Saving new SQL problem: {}", problem);
                            return problemRepository.save(problem);
                        });
            } catch (Exception e) {
                log.warn("Failed to get SQL task for level {}: {}", lvl, e.getMessage());
            }
        }
        throw new RuntimeException("No SQL tasks found at any level (EASY, MEDIUM, HARD)");
    }

    public ProblemResponseDTO getArtefactsOfProblem(Long problemId) {
        return artefactik0Client.getProblem(problemId);
    }
}
