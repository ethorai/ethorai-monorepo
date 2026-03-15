package com.ai.therapists.api.generation;

import com.ai.therapists.api.page.GeneratedPageResponse;
import com.ai.therapists.api.profile.TherapistInput;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class GenerationController {

    private final GenerationOrchestrator orchestrator;

    @PostMapping("/generate")
    public ResponseEntity<GeneratedPageResponse> generate(@Valid @RequestBody TherapistInput input) {
        GeneratedPageResponse response = orchestrator.execute(input);

        return ResponseEntity
                .created(URI.create("/api/pages/" + response.pageId()))
                .body(response);
    }
}
