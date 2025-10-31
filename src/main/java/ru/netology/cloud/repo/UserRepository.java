package ru.netology.cloud.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.cloud.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);
}


