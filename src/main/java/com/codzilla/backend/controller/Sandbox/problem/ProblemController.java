package com.codzilla.backend.controller.Sandbox.problem;

import com.codzilla.backend.User.User;
import com.codzilla.backend.User.UserRepository;
import com.codzilla.backend.User.UserService;
import com.codzilla.backend.controller.Sandbox.polygon.CreateProblemRequest;

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


    @PostMapping("/create")
    public ResponseEntity<Problem> createProblem(@RequestBody CreateProblemRequest request) {
        Problem saved = problemService.createProblem(request);
        return ResponseEntity.ok(saved);
    }

    @PostMapping(value = "submit/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> submitFile(
            @AuthenticationPrincipal User user,
            @RequestParam Long problemId,
            @RequestParam int languageId,
            @RequestParam MultipartFile file
    ) throws IOException {
        String sourceCode = new String(
                file.getBytes(),
                StandardCharsets.UTF_8
        );
        log.info("File content: {}", sourceCode);
        UUID userId = userService.getIdByEmail(user.getEmail());
        String result = problemService.submitSolution(
                userId,
                problemId,
                sourceCode,
                languageId
        );
        return ResponseEntity.ok(result);
    }
}