package ru.netology.cloud.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.netology.cloud.domain.FileMetadata;
import ru.netology.cloud.domain.User;

import java.util.List;
import java.util.Optional;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> findAllByUserOrderByCreatedAtDesc(User user);
    Optional<FileMetadata> findByUserAndFilename(User user, String filename);
    void deleteByUserAndFilename(User user, String filename);
}


