package ru.netology.cloud.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.cloud.domain.Token;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByValueAndActiveTrue(String value);
}


