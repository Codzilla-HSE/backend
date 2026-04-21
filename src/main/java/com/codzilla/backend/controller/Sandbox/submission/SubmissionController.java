package com.codzilla.backend.controller.Sandbox.submission;

import com.codzilla.backend.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/my-submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionRepository submissionRepository;

    private final Map<UUID, ConcurrentLinkedQueue<DeferredResult<ResponseEntity<List<SubmissionResponseDTO>>>>> waiters = new ConcurrentHashMap<>();

    @GetMapping
    public DeferredResult<ResponseEntity<List<SubmissionResponseDTO>>> getUserSubmissions(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastUpdate) {

        DeferredResult<ResponseEntity<List<SubmissionResponseDTO>>> output = new DeferredResult<>(20000L);

        if (user == null) {
            output.setResult(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
            return output;
        }

        UUID userId = user.getId();

        output.onTimeout(() -> output.setResult(ResponseEntity.status(HttpStatus.NOT_MODIFIED).build()));

        Optional<Submission> latestSub = submissionRepository.findFirstByUserIdOrderByUpdatedAtDesc(userId);
        boolean hasUpdates = lastUpdate == null ||
                (latestSub.isPresent() && latestSub.get().getUpdatedAt().isAfter(lastUpdate));

        if (hasUpdates) {
            output.setResult(ResponseEntity.ok(fetchUserSubmissions(userId)));
            return output;
        }

        waiters.computeIfAbsent(userId, k -> new ConcurrentLinkedQueue<>()).add(output);

        output.onCompletion(() -> {
            ConcurrentLinkedQueue<DeferredResult<ResponseEntity<List<SubmissionResponseDTO>>>> queue = waiters.get(userId);
            if (queue != null) {
                queue.remove(output);
            }
        });

        return output;
    }

    @EventListener
    public void onSubmissionUpdated(SubmissionUpdatedEvent event) {
        ConcurrentLinkedQueue<DeferredResult<ResponseEntity<List<SubmissionResponseDTO>>>> queue = waiters.get(event.userId());

        if (queue != null && !queue.isEmpty()) {
            List<SubmissionResponseDTO> freshData = fetchUserSubmissions(event.userId());

            DeferredResult<ResponseEntity<List<SubmissionResponseDTO>>> result;
            while ((result = queue.poll()) != null) {
                result.setResult(ResponseEntity.ok(freshData));
            }
        }
    }

    private List<SubmissionResponseDTO> fetchUserSubmissions(UUID userId) {
        return submissionRepository
                .findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(SubmissionResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }
}