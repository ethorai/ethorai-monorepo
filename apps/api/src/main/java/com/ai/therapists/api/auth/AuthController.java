package com.ai.therapists.api.auth;

import jakarta.validation.Valid;
import org.jooq.DSLContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ai.therapists.api.jooq.Tables.APP_USER;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final DSLContext dsl;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthController(DSLContext dsl) {
        this.dsl = dsl;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest req) {
        boolean exists = dsl.fetchExists(
                dsl.selectOne().from(APP_USER).where(APP_USER.EMAIL.eq(req.email()))
        );
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        String hash = passwordEncoder.encode(req.password());

        dsl.insertInto(APP_USER)
                .set(APP_USER.NAME, req.name())
                .set(APP_USER.EMAIL, req.email())
                .set(APP_USER.PASSWORD_HASH, hash)
                .execute();

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
