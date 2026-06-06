package com.codzilla.backend.judge.problem;

import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserService;
import com.codzilla.backend.judge.submission.Submission;
import com.codzilla.backend.judge.submission.SubmissionRepository;
import com.codzilla.backend.judge.client.SqlServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;
    private final UserService userService;
    private final SubmissionRepository submissionRepository;
    private final SqlServiceClient sqlServiceClient;

    // ──────────────────────────────────────────────
    // Создание задач
    // ──────────────────────────────────────────────

    @PostMapping("/algo")
    public ResponseEntity<Problem> createAlgoProblem(
            @RequestBody CreateAlgoProblemRequest request) {
        return ResponseEntity.ok(problemService.createAlgoProblem(request));
    }

    @PostMapping("/sql")
    public ResponseEntity<Problem> registerSqlProblem(
            @RequestBody RegisterSqlProblemRequest request) {
        return ResponseEntity.ok(problemService.registerSqlProblem(request));
    }

    // ──────────────────────────────────────────────
    // Отправка решений
    // ──────────────────────────────────────────────

    @PostMapping(value = "/{id}/submit/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> submitFile(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam int languageId,
            @RequestParam MultipartFile file
    ) throws IOException {
        String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
        UUID userId = userService.getIdByEmail(user.getEmail());
        String result = problemService.submitSolution(userId, id, sourceCode, languageId);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<String> submit(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "62") int languageId,
            @RequestBody String sourceCode) {
        UUID userId = user != null
                ? userService.getIdByEmail(user.getEmail())
                : UUID.randomUUID();
        String result = problemService.submitSolution(userId, id, sourceCode, languageId);
        return ResponseEntity.ok(result);
    }

    // ──────────────────────────────────────────────
    // Статус посылки
    // Для ALGO — из локальной БД
    // Для SQL  — проксируем в SqlService
    // ──────────────────────────────────────────────

    @GetMapping("/submissions/{submissionRef}/status")
    public ResponseEntity<String> getStatus(@PathVariable String submissionRef) {

        // SQL-посылки приходят как "sql:123"
        if (submissionRef.startsWith("sql:")) {
            Long sqlId = Long.parseLong(submissionRef.substring(4));
            SqlServiceClient.SubmissionStatus status = sqlServiceClient.getSubmissionStatus(sqlId);
            return ResponseEntity.ok(status.getStatus() +
                    (status.getVerdict() != null ? ": " + status.getVerdict() : ""));
        }

        // ALGO-посылки — числовой id
        Long id = Long.parseLong(submissionRef);
        return submissionRepository.findById(id)
                .map(sub -> {
                    String details = sub.getResultDetails() != null
                            ? ": " + sub.getResultDetails() : "";
                    return ResponseEntity.ok(sub.getStatus().name() + details);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}