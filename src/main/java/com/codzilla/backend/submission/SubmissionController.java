package com.codzilla.backend.submission;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;

    @PostMapping("/upload")
    public ResponseEntity<String> upload(
            @RequestParam Long problemId,
            @RequestParam int languageId,
            @RequestParam MultipartFile file) throws Exception {
        String submissionUuid = submissionService.upload(problemId, languageId, file);
        return ResponseEntity.ok(submissionUuid);
    }
}