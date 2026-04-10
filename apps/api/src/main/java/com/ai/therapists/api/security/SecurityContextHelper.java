package com.ai.therapists.api.security;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

public final class SecurityContextHelper {

    private SecurityContextHelper() {}

    /**
     * Returns the authenticated userId (JWT 'sub' claim) as a UUID.
     * Throws if no authentication is present — should never happen behind the JWT filter.
     */
    public static UUID currentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) {
            throw new IllegalStateException("No authenticated user in security context");
        }
        return UUID.fromString(auth.getName());
    }
}
