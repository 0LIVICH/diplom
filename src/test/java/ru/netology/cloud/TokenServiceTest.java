package ru.netology.cloud;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.netology.cloud.domain.Token;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.repo.TokenRepository;
import ru.netology.cloud.service.TokenService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenServiceTest {

    @Test
    void issueAndAuthenticate() {
        TokenRepository repo = Mockito.mock(TokenRepository.class);
        TokenService service = new TokenService(repo, 60);
        User u = new User();
        u.setLogin("user");

        String value = service.issueToken(u);
        Token stored = new Token();
        stored.setUser(u);
        stored.setValue(value);
        stored.setActive(true);
        stored.setExpiresAt(java.time.Instant.now().plusSeconds(3600));
        Mockito.when(repo.findByValueAndActiveTrue(value)).thenReturn(Optional.of(stored));

        assertThat(service.authenticate(value)).contains(u);
    }
}


