package ru.netology.cloud.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.dto.ErrorResponse;
import ru.netology.cloud.dto.LoginRequest;
import ru.netology.cloud.dto.LoginResponse;
import ru.netology.cloud.service.TokenService;
import ru.netology.cloud.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping
public class AuthController {
    private final UserService userService;
    private final TokenService tokenService;

    public AuthController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
        // ensure demo user exists for quick start
        this.userService.ensureDemoUser("user", "password");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userService.findByLogin(request.getLogin())
                .filter(u -> userService.matchesPassword(request.getPassword(), u.getPasswordHash()))
                .<ResponseEntity<?>>map(u -> ResponseEntity.ok(new LoginResponse(tokenService.issueToken(u))))
                .orElseGet(() -> ResponseEntity.badRequest().body(new ErrorResponse("Bad credentials", 400)));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("auth-token") String token) {
        tokenService.revoke(token);
        return ResponseEntity.ok().build();
    }
}


