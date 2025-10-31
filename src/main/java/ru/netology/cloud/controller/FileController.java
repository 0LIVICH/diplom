package ru.netology.cloud.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.netology.cloud.domain.FileMetadata;
import ru.netology.cloud.domain.User;
import ru.netology.cloud.dto.ErrorResponse;
import ru.netology.cloud.security.AuthInterceptor;
import ru.netology.cloud.service.FileStorageService;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(FileController.class);
    private final FileStorageService storageService;

    public FileController(FileStorageService storageService) {
        this.storageService = storageService;
    }

    private User current(HttpServletRequest request) {
        return (User) request.getAttribute(AuthInterceptor.ATTR_USER);
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(@RequestParam("filename") String filename,
                                    @RequestPart("file") MultipartFile file,
                                    HttpServletRequest request) throws IOException {
        log.info("Upload request for file: {} from user: {}", filename, current(request).getLogin());
        
        // Извлекаем данные из DTO (MultipartFile) в контроллере
        long size = file.getSize();
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        
        storageService.upload(current(request), filename, size, contentType, file.getInputStream());
        log.info("File upload completed: {}", filename);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> delete(@RequestParam("filename") String filename,
                                    HttpServletRequest request) throws IOException {
        log.info("Delete request for file: {} from user: {}", filename, current(request).getLogin());
        storageService.delete(current(request), filename);
        log.info("File deleted successfully: {}", filename);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file")
    public ResponseEntity<?> download(@RequestParam("filename") String filename,
                                      HttpServletRequest request) throws IOException {
        log.info("Download request for file: {} from user: {}", filename, current(request).getLogin());
        FileMetadata meta = storageService.get(current(request), filename)
                .orElse(null);
        if (meta == null) {
            log.warn("File not found: {} for user: {}", filename, current(request).getLogin());
            return ResponseEntity.status(400).body(new ErrorResponse("File not found", 400));
        }
        log.debug("Serving file: {} (size: {})", filename, meta.getSize());
        FileSystemResource resource = new FileSystemResource(new File(meta.getStoragePath()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(meta.getContentType()))
                .contentLength(meta.getSize())
                .body(resource);
    }

    @PutMapping("/file")
    public ResponseEntity<?> rename(@RequestParam("filename") String filename,
                                    @RequestBody Map<String, String> body,
                                    HttpServletRequest request) throws IOException {
        log.info("Rename request for file: {} from user: {}", filename, current(request).getLogin());
        String newName = body.get("name");
        if (newName == null || newName.isBlank()) {
            log.warn("Invalid rename request: new name is empty");
            return ResponseEntity.status(400).body(new ErrorResponse("New name required", 400));
        }
        storageService.rename(current(request), filename, newName);
        log.info("File renamed successfully: {} -> {}", filename, newName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam(value = "limit", required = false) Integer limit,
                                  HttpServletRequest request) {
        log.info("List request from user: {} (limit: {})", current(request).getLogin(), limit);
        
        // Получаем доменные объекты из сервиса
        List<FileMetadata> files = storageService.list(current(request));
        
        // Применяем limit
        if (limit != null && limit > 0 && limit < files.size()) {
            files = files.subList(0, limit);
        }
        
        // Преобразуем доменные объекты в DTO в контроллере
        List<Map<String, Object>> result = files.stream().<Map<String, Object>>map(f -> {
            return Map.of(
                    "filename", f.getFilename(),
                    "size", f.getSize()
            );
        }).collect(Collectors.toList());
        
        log.debug("Returning {} files", result.size());
        return ResponseEntity.ok(result);
    }
}


