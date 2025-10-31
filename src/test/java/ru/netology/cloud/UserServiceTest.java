package ru.netology.cloud;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.repo.UserRepository;
import ru.netology.cloud.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

public class UserServiceTest {

    @Test
    void ensureDemoUser_createsWhenAbsent() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        Mockito.when(repo.findByLogin("demo")).thenReturn(Optional.empty());
        Mockito.when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserService service = new UserService(repo);
        User u = service.ensureDemoUser("demo", "pass");

        assertThat(u.getLogin()).isEqualTo("demo");
        assertThat(u.getPasswordHash()).isNotBlank();
    }

    @Test
    void matchesPassword_returnsTrueForCorrect() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        UserService service = new UserService(repo);
        User u = service.ensureDemoUser("demo", "secret");
        assertThat(service.matchesPassword("secret", u.getPasswordHash())).isTrue();
        assertThat(service.matchesPassword("wrong", u.getPasswordHash())).isFalse();
    }
}


