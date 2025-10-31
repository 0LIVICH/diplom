package ru.netology.cloud.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.netology.cloud.domain.FileMetadata;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.repo.FileMetadataRepository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    private final FileMetadataRepository fileRepository;
    private final Path rootDir;

    public FileStorageService(FileMetadataRepository fileRepository,
                              @Value("${storage.root-dir:./storage}") String rootDir) throws IOException {
        this.fileRepository = fileRepository;
        this.rootDir = Path.of(rootDir).toAbsolutePath().normalize();
        Files.createDirectories(this.rootDir);
        log.info("File storage initialized at: {}", this.rootDir);
    }

    public List<FileMetadata> list(User user) {
        log.debug("Listing files for user: {}", user.getLogin());
        return fileRepository.findAllByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void upload(User user, String filename, long size, String contentType, InputStream inputStream) throws IOException {
        log.info("Uploading file: {} (size: {}, type: {}) for user: {}", filename, size, contentType, user.getLogin());
        
        Path userDir = rootDir.resolve(String.valueOf(user.getId()));
        Files.createDirectories(userDir);
        Path target = userDir.resolve(filename);
        
        // Сначала сохраняем метаданные в БД (в рамках транзакции)
        FileMetadata meta = fileRepository.findByUserAndFilename(user, filename).orElse(new FileMetadata());
        meta.setUser(user);
        meta.setFilename(filename);
        meta.setSize(size);
        meta.setContentType(contentType != null ? contentType : "application/octet-stream");
        meta.setStoragePath(target.toString());
        fileRepository.save(meta);
        
        // Потом копируем файл на диск (если эта операция упадет, транзакция откатится)
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        log.info("File uploaded successfully: {}", filename);
    }

    public Optional<FileMetadata> get(User user, String filename) {
        log.debug("Getting file metadata: {} for user: {}", filename, user.getLogin());
        return fileRepository.findByUserAndFilename(user, filename);
    }

    @Transactional
    public void delete(User user, String filename) throws IOException {
        log.info("Deleting file: {} for user: {}", filename, user.getLogin());
        FileMetadata meta = fileRepository.findByUserAndFilename(user, filename)
                .orElseThrow(() -> new IOException("File not found"));
        
        // Сначала удаляем из БД (в рамках транзакции)
        fileRepository.deleteByUserAndFilename(user, filename);
        
        // Потом удаляем файл с диска (если эта операция упадет, транзакция уже зафиксирована, но это ок - файл останется на диске, можно очистить позже)
        try {
            Files.deleteIfExists(Path.of(meta.getStoragePath()));
            log.info("File deleted successfully: {}", filename);
        } catch (IOException e) {
            log.warn("Failed to delete file from disk: {}, but metadata removed from DB", filename, e);
            throw e;
        }
    }

    @Transactional
    public void rename(User user, String oldName, String newName) throws IOException {
        log.info("Renaming file: {} -> {} for user: {}", oldName, newName, user.getLogin());
        FileMetadata meta = fileRepository.findByUserAndFilename(user, oldName)
                .orElseThrow(() -> new IOException("File not found"));
        Path oldPath = Path.of(meta.getStoragePath());
        Path newPath = oldPath.getParent().resolve(newName);
        
        // Сначала обновляем в БД (в рамках транзакции)
        meta.setFilename(newName);
        meta.setStoragePath(newPath.toString());
        fileRepository.save(meta);
        
        // Потом перемещаем файл (если эта операция упадет, транзакция откатится)
        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File renamed successfully: {} -> {}", oldName, newName);
    }
}


