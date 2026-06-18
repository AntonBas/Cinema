package ua.lviv.bas.cinema.service.integration.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            log.warn("Attempted to store null or empty file");
            return null;
        }

        try {
            String originalFileName = file.getOriginalFilename();
            String extension = Optional.ofNullable(originalFileName)
                    .filter(name -> name.contains("."))
                    .map(name -> name.substring(name.lastIndexOf(".")))
                    .orElse(".jpg");

            String fileName = UUID.randomUUID() + extension;

            Path basePath = Paths.get(uploadDir).toRealPath();
            Path uploadPath = basePath.resolve(sanitize(subDirectory));

            if (!uploadPath.startsWith(basePath)) {
                log.error("Path traversal attempt detected during store: {}", subDirectory);
                throw new ExternalServiceException("File Storage",
                        new SecurityException("Invalid upload directory"));
            }

            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(sanitize(fileName));
            Files.write(filePath, file.getBytes());

            log.info("File stored successfully: {}", fileName);
            return fileName;
        } catch (IOException e) {
            log.error("Failed to store file in directory: {}", subDirectory, e);
            throw new ExternalServiceException("File Storage", e);
        }
    }

    public byte[] loadFile(String fileName, String subDirectory) {
        if (fileName == null || fileName.isBlank()) {
            log.warn("Attempted to load file with null or blank name");
            return null;
        }

        try {
            Path basePath = Paths.get(uploadDir).toRealPath();
            Path filePath = basePath.resolve(sanitize(subDirectory)).resolve(sanitize(fileName));

            if (!filePath.toRealPath().startsWith(basePath)) {
                log.warn("Path traversal attempt detected during load: {}", fileName);
                return null;
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.warn("File not found or error loading: {}", fileName);
            return null;
        }
    }

    public void deleteFile(String fileName, String subDirectory) {
        if (fileName == null || fileName.isBlank()) {
            log.warn("Attempted to delete file with null or blank name");
            return;
        }

        try {
            Path basePath = Paths.get(uploadDir).toRealPath();
            Path filePath = basePath.resolve(sanitize(subDirectory)).resolve(sanitize(fileName));

            if (!filePath.startsWith(basePath)) {
                log.warn("Path traversal attempt detected during delete: {}", fileName);
                return;
            }

            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("File deleted successfully: {}", fileName);
            } else {
                log.warn("File not found for deletion: {}", fileName);
            }
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", fileName);
        }
    }

    public String determineContentType(String fileName) {
        if (fileName == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String lower = sanitize(fileName).toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".gif")) {
            return "image/gif";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        } else {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
    }

    public boolean fileExists(String fileName, String subDirectory) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }

        try {
            Path basePath = Paths.get(uploadDir).toRealPath();
            Path filePath = basePath.resolve(sanitize(subDirectory)).resolve(sanitize(fileName));
            return filePath.startsWith(basePath) && Files.exists(filePath);
        } catch (IOException e) {
            return false;
        }
    }

    private String sanitize(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("..", "")
                .replace("/", "")
                .replace("\\", "")
                .replace("\0", "");
    }
}