package com.codzilla.backend.controller.Sandbox.problem;

import com.codzilla.backend.controller.Sandbox.polygon.CreateProblemRequest;

import com.codzilla.backend.controller.Sandbox.submission.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;
    private final SubmissionRepository submissionRepository;

    @PostMapping("/create")
    public ResponseEntity<Problem> createProblem(@RequestBody CreateProblemRequest request) {
        Problem saved = problemService.createProblem(request);
        return ResponseEntity.ok(saved);
    }

    @PostMapping(value = "/{id}/submit/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> submitFile(
            @PathVariable Long id,
            @RequestParam int languageId,
            @RequestParam("file") MultipartFile file) throws IOException {
        String sourceCode = new String(file.getBytes(), StandardCharsets.UTF_8);
        String result = problemService.submitSolution(1L, id, sourceCode, languageId);
        return ResponseEntity.ok(result);
    }


    @PostMapping("/{id}/submit")
    public ResponseEntity<String> submit(
            @PathVariable Long id,
            @RequestParam int languageId,
            @RequestBody String sourceCode) {
        String result = problemService.submitSolution(1L, id, sourceCode, languageId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/submissions/{token}/status")
    public ResponseEntity<String> getStatus(
            @PathVariable String token) {
        return submissionRepository.findByJudge0Token(token)
                .map(sub -> ResponseEntity.ok(sub.getStatus().name() +
                        (sub.getResultDetails() != null ? ": " + sub.getResultDetails() : "")))
                .orElse(ResponseEntity.notFound().build());
    }
}