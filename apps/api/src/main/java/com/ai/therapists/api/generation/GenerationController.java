package com.ai.therapists.api.generation;

import com.ai.therapists.api.profile.TherapistInput;
import com.ai.therapists.api.security.SecurityContextHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationOrchestrator orchestrator;

    @PostMapping("/generate")
    public ResponseEntity<GenerationJobResponse> generate(@Valid @RequestBody TherapistInput input) {
        UUID userId = SecurityContextHelper.currentUserId();
        GenerationJobResponse job = orchestrator.submitAsync(input, userId);

        return ResponseEntity
                .accepted()
                .location(URI.create("/api/generate/status/" + job.jobId()))
                .body(job);
    }

    @GetMapping("/generate/status/{jobId}")
    public ResponseEntity<GenerationJobResponse> getStatus(@PathVariable UUID jobId) {
        GenerationJobResponse job = orchestrator.getJobStatus(jobId);
        return ResponseEntity.ok(job);
    }
}
