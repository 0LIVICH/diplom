package ru.netology.cloud.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloud.domain.FileMetadata;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.repo.FileMetadataRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

@Service
public class FileStorageService {
    private final FileMetadataRepository fileRepository;
    private final Path rootDir;

    public FileStorageService(FileMetadataRepository fileRepository,
                              @Value("${storage.root-dir:./storage}") String rootDir) throws IOException {
        this.fileRepository = fileRepository;
        this.rootDir = Path.of(rootDir).toAbsolutePath().normalize();
        Files.createDirectories(this.rootDir);
    }

    public List<FileMetadata> list(User user) {
        return fileRepository.findAllByUserOrderByCreatedAtDesc(user);
    }

    @Transactional
    public void upload(User user, String filename, MultipartFile file) throws IOException {
        Path userDir = rootDir.resolve(String.valueOf(user.getId()));
        Files.createDirectories(userDir);
        Path target = userDir.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        FileMetadata meta = fileRepository.findByUserAndFilename(user, filename).orElse(new FileMetadata());
        meta.setUser(user);
        meta.setFilename(filename);
        meta.setSize(file.getSize());
        meta.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        meta.setStoragePath(target.toString());
        fileRepository.save(meta);
    }

    public Optional<FileMetadata> get(User user, String filename) {
        return fileRepository.findByUserAndFilename(user, filename);
    }

    @Transactional
    public void delete(User user, String filename) throws IOException {
        fileRepository.findByUserAndFilename(user, filename).ifPresent(meta -> {
            try { Files.deleteIfExists(Path.of(meta.getStoragePath())); } catch (IOException ignored) {}
        });
        fileRepository.deleteByUserAndFilename(user, filename);
    }

    @Transactional
    public void rename(User user, String oldName, String newName) throws IOException {
        FileMetadata meta = fileRepository.findByUserAndFilename(user, oldName)
                .orElseThrow(() -> new IOException("File not found"));
        Path oldPath = Path.of(meta.getStoragePath());
        Path newPath = oldPath.getParent().resolve(newName);
        Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
        meta.setFilename(newName);
        meta.setStoragePath(newPath.toString());
        fileRepository.save(meta);
    }
}


