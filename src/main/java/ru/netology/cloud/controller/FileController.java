package ru.netology.cloud.controller;

import jakarta.servlet.http.HttpServletRequest;
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
        storageService.upload(current(request), filename, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> delete(@RequestParam("filename") String filename,
                                    HttpServletRequest request) throws IOException {
        storageService.delete(current(request), filename);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/file")
    public ResponseEntity<?> download(@RequestParam("filename") String filename,
                                      HttpServletRequest request) throws IOException {
        FileMetadata meta = storageService.get(current(request), filename)
                .orElse(null);
        if (meta == null) {
            return ResponseEntity.status(400).body(new ErrorResponse("File not found", 400));
        }
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
        String newName = body.get("name");
        if (newName == null || newName.isBlank()) {
            return ResponseEntity.status(400).body(new ErrorResponse("New name required", 400));
        }
        storageService.rename(current(request), filename, newName);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseEntity<?> list(@RequestParam(value = "limit", required = false) Integer limit,
                                  HttpServletRequest request) {
        List<FileMetadata> files = storageService.list(current(request));
        if (limit != null && limit > 0 && limit < files.size()) {
            files = files.subList(0, limit);
        }
        return ResponseEntity.ok(files.stream().map(f -> Map.of(
                "filename", f.getFilename(),
                "size", f.getSize()
        )).collect(Collectors.toList()));
    }
}


