package ru.netology.cloud.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.repo.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    public boolean matchesPassword(String rawPassword, String hash) {
        return passwordEncoder.matches(rawPassword, hash);
    }

    @Transactional
    public User ensureDemoUser(String login, String rawPassword) {
        return userRepository.findByLogin(login).orElseGet(() -> {
            User u = new User();
            u.setLogin(login);
            u.setPasswordHash(passwordEncoder.encode(rawPassword));
            return userRepository.save(u);
        });
    }
}


