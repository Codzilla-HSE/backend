package com.codzilla.backend.judge.problem;

import com.codzilla.backend.PreMatch.MatchRoom.Match;
import com.codzilla.backend.PreMatch.MatchRoom.MatchService;
import com.codzilla.backend.PreMatch.model.Category;
import com.codzilla.backend.PreMatch.model.Language;
import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserService;

import com.codzilla.backend.judge.client.SqlServiceClient;
import com.codzilla.backend.judge.submission.SubmissionRepository;
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
    private final MatchService matchService;

    // ── Создание задач ────────────────────────────────────────────

    // POST /problems/algo — создать алго-задачу (регистрирует в Artefactik0)
    @PostMapping("/algo")
    public ResponseEntity<Problem> createAlgoProblem(
            @RequestBody CreateAlgoProblemRequest request) {
        return ResponseEntity.ok(problemService.createAlgoProblem(request));
    }

    // POST /problems/sql — зарегистрировать SQL-задачу (уже созданную в SqlService)
    @PostMapping("/sql")
    public ResponseEntity<Problem> registerSqlProblem(
            @RequestBody RegisterSqlProblemRequest request) {
        return ResponseEntity.ok(problemService.registerSqlProblem(request));
    }

    // ── Отправка решений ──────────────────────────────────────────

    // POST /problems/{id}/submit — отправить код (тип задачи определяется автоматически)
//    @PostMapping("/{id}/submit")
//    public ResponseEntity<String> submit(
//            @AuthenticationPrincipal User user,
//            @PathVariable Long id,
//            @RequestParam(defaultValue = "62") int languageId,
//            @RequestBody String sourceCode) {
//        UUID userId = user != null
//                ? userService.getIdByEmail(user.getEmail())
//                : UUID.randomUUID();
//        return ResponseEntity.ok(problemService.submitSolution(userId, id, sourceCode, languageId));
//    }

    // POST /problems/{id}/submit/file — отправить файл с кодом
//    @PostMapping(value = "/{id}/submit/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<String> submitFile(
//            @AuthenticationPrincipal User user,
//            @PathVariable Long id,
//            @RequestParam int languageId,
//            @RequestParam MultipartFile file
//
//    ) throws IOException {
//        String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
//        UUID userId = userService.getIdByEmail(user.getEmail());
//        return ResponseEntity.ok(
//                problemService.submitSolution(userId,  id, sourceCode, languageId));
//    }

    @PostMapping(value = "/submit/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> submitFileByMatch(
            @AuthenticationPrincipal User user,
            @RequestParam UUID matchId,
            @RequestParam MultipartFile file
    ) throws IOException {
        Match match = matchService.getMatchById(matchId);
        if (match == null) {
            return ResponseEntity.badRequest().body("Match not found");
        }

        // Читаем файл
        String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
        log.info("Received file for match {}: size={}, content (first 100 chars): {}",
                matchId, sourceCode.length(), sourceCode.substring(0, Math.min(100, sourceCode.length())));

        if (sourceCode == null || sourceCode.isBlank()) {
            return ResponseEntity.badRequest().body("Source code is empty");
        }

        Long problemId = match.getProblem().getId();
        String languageStr = match.getOptions().get(Category.Language);
        int languageId = Enum.valueOf(Language.class, languageStr).getValue();
        UUID userId = user.getId();

        String result = problemService.submitSolution(userId, matchId  , problemId, sourceCode, languageId);
        return ResponseEntity.ok(result);
    }

    // ── Статус посылки ────────────────────────────────────────────

    // GET /problems/submissions/{ref}/status
    // ref = "123"      → ALGO-посылка, смотрим в локальной БД
    // ref = "sql:123"  → SQL-посылка, проксируем в SqlService
    @GetMapping("/submissions/{submissionRef}/status")
    public ResponseEntity<String> getStatus(@PathVariable String submissionRef) {

        if (submissionRef.startsWith("sql:")) {
            Long sqlId = Long.parseLong(submissionRef.substring(4));
            SqlServiceClient.SubmissionStatus status =
                    sqlServiceClient.getSubmissionStatus(sqlId);
            String verdict = status.getVerdict() != null ? ": " + status.getVerdict() : "";
            return ResponseEntity.ok(status.getStatus() + verdict);
        }

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