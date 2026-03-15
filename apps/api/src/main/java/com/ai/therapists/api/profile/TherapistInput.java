package com.ai.therapists.api.profile;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record TherapistInput(
        @NotBlank String fullName,
        @NotNull RoleType role,
        String location,
        @NotEmpty List<String> audiences,
        @NotEmpty List<String> areasOfSupport,
        String approach,
        @NotNull SessionFormat sessionFormat,
        List<String> expectations,
        @NotNull ContactMethod contactMethod,
        @NotBlank String contactValue
) {}
