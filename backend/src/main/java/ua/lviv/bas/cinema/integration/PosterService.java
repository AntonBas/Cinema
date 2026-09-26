package ua.lviv.bas.cinema.integration;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PosterService {

    private final FileStorageService fileStorageService;

    private static final String POSTER_SUB_DIRECTORY = "posters";

    public String uploadPoster(MultipartFile posterFile) {
        return fileStorageService.storeFile(posterFile, POSTER_SUB_DIRECTORY);
    }

    public void deletePoster(String posterFileName) {
        fileStorageService.deleteFile(posterFileName, POSTER_SUB_DIRECTORY);
    }

    public Optional<PosterImage> loadPoster(String posterFileName) {
        if (posterFileName == null || posterFileName.isBlank()) {
            return Optional.empty();
        }

        var data = fileStorageService.loadFile(posterFileName, POSTER_SUB_DIRECTORY);
        if (data == null) {
            return Optional.empty();
        }

        var contentType = fileStorageService.determineContentType(posterFileName);
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            log.warn("Invalid media type: {}, using default", contentType);
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return Optional.of(new PosterImage(data, mediaType));
    }
}