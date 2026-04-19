package com.codzilla.backend.controller.Sandbox.submission;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("submission")
public class SubmissionController {

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @GetMapping("status/")
    ResponseEntity<String> getSubmissionStatus(
            @RequestParam Long id
    ) {
        return ResponseEntity.ok(submissionService.getSubmissionStatus(id).name());
    }
}
