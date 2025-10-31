package ru.netology.cloud.service;

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
    private final TokenRepository tokenRepository;
    private final long ttlMinutes;

    public TokenService(TokenRepository tokenRepository,
                        @Value("${security.token.ttl-minutes:120}") long ttlMinutes) {
        this.tokenRepository = tokenRepository;
        this.ttlMinutes = ttlMinutes;
    }

    @Transactional
    public String issueToken(User user) {
        Token token = new Token();
        token.setUser(user);
        token.setValue(UUID.randomUUID().toString());
        token.setExpiresAt(Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES));
        token.setActive(true);
        tokenRepository.save(token);
        return token.getValue();
    }

    public Optional<User> authenticate(String tokenValue) {
        return tokenRepository.findByValueAndActiveTrue(tokenValue)
                .filter(t -> t.getExpiresAt().isAfter(Instant.now()))
                .map(Token::getUser);
    }

    @Transactional
    public void revoke(String tokenValue) {
        tokenRepository.findByValueAndActiveTrue(tokenValue)
                .ifPresent(t -> {
                    t.setActive(false);
                    tokenRepository.save(t);
                });
    }
}


