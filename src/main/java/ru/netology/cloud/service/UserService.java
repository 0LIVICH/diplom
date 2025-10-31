package ru.netology.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.repo.UserRepository;

import java.util.Optional;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        log.info("UserService initialized");
    }

    public Optional<User> findByLogin(String login) {
        log.debug("Finding user by login: {}", login);
        return userRepository.findByLogin(login);
    }

    public boolean matchesPassword(String rawPassword, String hash) {
        boolean matches = passwordEncoder.matches(rawPassword, hash);
        log.debug("Password match result: {}", matches);
        return matches;
    }

    @Transactional
    public User ensureDemoUser(String login, String rawPassword) {
        log.debug("Ensuring demo user exists: {}", login);
        return userRepository.findByLogin(login).orElseGet(() -> {
            log.info("Creating demo user: {}", login);
            User u = new User();
            u.setLogin(login);
            u.setPasswordHash(passwordEncoder.encode(rawPassword));
            return userRepository.save(u);
        });
    }
}


