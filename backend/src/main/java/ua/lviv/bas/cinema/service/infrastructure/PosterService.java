package ua.lviv.bas.cinema.service.infrastructure;

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
	private static final String DEFAULT_POSTER_URL = "/images/default-poster.jpg";

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

		byte[] data = fileStorageService.loadFile(posterFileName, POSTER_SUB_DIRECTORY);
		if (data == null) {
			return ResponseEntity.notFound().build();
		}

		String contentType = fileStorageService.determineContentType(posterFileName);

		MediaType mediaType;
		try {
			mediaType = MediaType.parseMediaType(contentType);
		} catch (Exception e) {
			log.warn("Invalid media type: {}, using default", contentType);
			mediaType = MediaType.APPLICATION_OCTET_STREAM;
		}

		return ResponseEntity.ok().contentType(mediaType).header(HttpHeaders.CACHE_CONTROL, "max-age=3600").body(data);
	}

	public String getPosterUrl(Long movieId, String posterFileName) {
		if (posterFileName == null || posterFileName.isBlank()) {
			return DEFAULT_POSTER_URL;
		}
		return "/api/movies/" + movieId + "/poster";
	}

	public boolean hasPoster(String posterFileName) {
		if (posterFileName == null || posterFileName.isBlank()) {
			return false;
		}
		return fileStorageService.fileExists(posterFileName, POSTER_SUB_DIRECTORY);
	}
}