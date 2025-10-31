package ru.netology.cloud.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.netology.cloud.dto.ErrorResponse;
import ru.netology.cloud.dto.LoginRequest;
import ru.netology.cloud.dto.LoginResponse;
import ru.netology.cloud.service.TokenService;
import ru.netology.cloud.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
        // ensure demo user exists for quick start
        this.userService.ensureDemoUser("user", "password");
        log.info("AuthController initialized");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getLogin());
        return userService.findByLogin(request.getLogin())
                .filter(u -> userService.matchesPassword(request.getPassword(), u.getPasswordHash()))
                .<ResponseEntity<?>>map(u -> {
                    String token = tokenService.issueToken(u);
                    log.info("Login successful for user: {}", request.getLogin());
                    return ResponseEntity.ok(new LoginResponse(token));
                })
                .orElseGet(() -> {
                    log.warn("Login failed for user: {} - bad credentials", request.getLogin());
                    return ResponseEntity.badRequest().body(new ErrorResponse("Bad credentials", 400));
                });
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("auth-token") String token) {
        log.info("Logout request for token");
        tokenService.revoke(token);
        log.info("Logout successful");
        return ResponseEntity.ok().build();
    }
}


