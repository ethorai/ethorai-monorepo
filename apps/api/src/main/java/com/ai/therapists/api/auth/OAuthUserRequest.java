package com.ai.therapists.api.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OAuthUserRequest(
        @NotBlank @Email String email,
        String name,
        @NotBlank String provider,
        @NotBlank String providerAccountId
) {}
