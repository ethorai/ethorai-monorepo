package com.ai.therapists.api.auth;

import com.ai.therapists.api.security.JwtService;
import jakarta.validation.Valid;
import org.jooq.DSLContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.ai.therapists.api.jooq.Tables.APP_USER;
import static com.ai.therapists.api.jooq.Tables.OAUTH_ACCOUNT;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DSLContext dsl;
    private final JwtService jwtService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${internal.secret:}")
    private String internalSecret;

    public AuthController(DSLContext dsl, JwtService jwtService) {
        this.dsl = dsl;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        boolean exists = dsl.fetchExists(
                dsl.selectOne().from(APP_USER).where(APP_USER.EMAIL.eq(req.email()))
        );
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        dsl.insertInto(APP_USER)
                .set(APP_USER.NAME, req.name())
                .set(APP_USER.EMAIL, req.email())
                .set(APP_USER.PASSWORD_HASH, passwordEncoder.encode(req.password()))
                .execute();

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        var record = dsl.selectFrom(APP_USER)
                .where(APP_USER.EMAIL.eq(req.email()))
                .fetchOptional()
                .orElse(null);

        if (record == null || record.getPasswordHash() == null
                || !passwordEncoder.matches(req.password(), record.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = jwtService.generateToken(record.getId());
        return ResponseEntity.ok(new LoginResponse(token, record.getId().toString()));
    }

    @PostMapping("/oauth")
    public ResponseEntity<LoginResponse> oauthCallback(
            @RequestHeader("X-Internal-Secret") String secret,
            @Valid @RequestBody OAuthUserRequest req) {

        if (internalSecret.isBlank() || !internalSecret.equals(secret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var existing = dsl.selectFrom(APP_USER)
                .where(APP_USER.EMAIL.eq(req.email()))
                .fetchOptional();

        UUID userId;
        if (existing.isPresent()) {
            userId = existing.get().getId();
        } else {
            userId = dsl.insertInto(APP_USER)
                    .set(APP_USER.EMAIL, req.email())
                    .set(APP_USER.NAME, req.name())
                    .returning(APP_USER.ID)
                    .fetchOne(APP_USER.ID);
        }

        dsl.insertInto(OAUTH_ACCOUNT)
                .set(OAUTH_ACCOUNT.USER_ID, userId)
                .set(OAUTH_ACCOUNT.PROVIDER, req.provider())
                .set(OAUTH_ACCOUNT.PROVIDER_ACCOUNT_ID, req.providerAccountId())
                .onConflict(OAUTH_ACCOUNT.PROVIDER, OAUTH_ACCOUNT.PROVIDER_ACCOUNT_ID)
                .doNothing()
                .execute();

        String token = jwtService.generateToken(userId);
        return ResponseEntity.ok(new LoginResponse(token, userId.toString()));
    }
}
