package com.codzilla.backend.controller.Sandbox.problem;

import com.codzilla.backend.controller.Sandbox.polygon.CreateProblemRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemService problemService;

    @PostMapping("/create")
    public ResponseEntity<Problem> createProblem(@RequestBody CreateProblemRequest request) {
        Problem saved = problemService.createProblem(request);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<String> submit(
            @PathVariable Long id,
            @RequestParam int languageId,
            @RequestBody String sourceCode) {
        String result = problemService.submitSolution(id, sourceCode, languageId);
        return ResponseEntity.ok(result);
    }
}