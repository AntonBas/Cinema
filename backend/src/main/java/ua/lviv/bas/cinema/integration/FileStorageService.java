package ua.lviv.bas.cinema.integration;

import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface FileStorageService {

    String storeFile(MultipartFile file, String subDirectory);

    byte[] loadFile(String fileName, String subDirectory);

    void deleteFile(String fileName, String subDirectory);

    String determineContentType(String fileName);

    Optional<String> publicUrl(String fileName, int maxWidth);
}
