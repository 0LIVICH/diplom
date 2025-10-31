package ru.netology.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloud.domain.Token;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.repo.TokenRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {
    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private final TokenRepository tokenRepository;
    private final long ttlMinutes;

    public TokenService(TokenRepository tokenRepository,
                        @Value("${security.token.ttl-minutes:120}") long ttlMinutes) {
        this.tokenRepository = tokenRepository;
        this.ttlMinutes = ttlMinutes;
        log.info("TokenService initialized with TTL: {} minutes", ttlMinutes);
    }

    @Transactional
    public String issueToken(User user) {
        log.debug("Issuing token for user: {}", user.getLogin());
        Token token = new Token();
        token.setUser(user);
        token.setValue(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES));
        token.setActive(true);
        tokenRepository.save(token);
        log.debug("Token issued for user: {}", user.getLogin());
        return token.getValue();
    }

    public Optional<User> authenticate(String tokenValue) {
        log.debug("Authenticating token");
        Optional<User> user = tokenRepository.findByValueAndActiveTrue(tokenValue)
                .filter(t -> {
                    boolean valid = t.getExpiresAt().isAfter(Instant.now());
                    if (!valid) {
                        log.debug("Token expired");
                    }
                    return valid;
                })
                .map(Token::getUser);
        if (user.isEmpty()) {
            log.debug("Token authentication failed");
        }
        return user;
    }

    @Transactional
    public void revoke(String tokenValue) {
        log.debug("Revoking token");
        tokenRepository.findByValueAndActiveTrue(tokenValue)
                .ifPresent(t -> {
                    t.setActive(false);
                    tokenRepository.save(t);
                    log.debug("Token revoked");
                });
    }
}


