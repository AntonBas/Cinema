package ua.lviv.bas.cinema.service.integration.file;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public ResponseEntity<byte[]> getPosterResponse(String posterFileName) {
        if (posterFileName == null || posterFileName.isBlank()) {
            return ResponseEntity.notFound().build();
        }

        var data = fileStorageService.loadFile(posterFileName, POSTER_SUB_DIRECTORY);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }

        var contentType = fileStorageService.determineContentType(posterFileName);
        MediaType mediaType;
        try {
            mediaType = MediaType.parseMediaType(contentType);
        } catch (Exception e) {
            log.warn("Invalid media type: {}, using default", contentType);
            mediaType = MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok().contentType(mediaType).header(HttpHeaders.CACHE_CONTROL, "max-age=3600").body(data);
    }
}