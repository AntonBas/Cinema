package ua.lviv.bas.cinema.service.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

@ExtendWith(MockitoExtension.class)
public class PosterServiceTest {

	@Mock
	private FileStorageService fileStorageService;

	@InjectMocks
	private PosterService posterService;

	@Test
	void uploadPoster_Success() {
		MockMultipartFile file = new MockMultipartFile("poster.jpg", "poster.jpg", "image/jpeg", new byte[10]);

		when(fileStorageService.storeFile(file, "posters")).thenReturn("uuid.jpg");

		String result = posterService.uploadPoster(file);

		assertThat(result).isEqualTo("uuid.jpg");
	}

	@Test
	void deletePoster_Success() {
		posterService.deletePoster("test.jpg");

		verify(fileStorageService).deleteFile("test.jpg", "posters");
	}

	@Test
	void getPosterResponse_Success() {
		String fileName = "test.jpg";
		byte[] fileData = new byte[] { 1, 2, 3 };

		when(fileStorageService.loadFile(fileName, "posters")).thenReturn(fileData);
		when(fileStorageService.determineContentType(fileName)).thenReturn("image/jpeg");

		ResponseEntity<byte[]> result = posterService.getPosterResponse(fileName);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(result.getBody()).isEqualTo(fileData);
	}

	@Test
	void getPosterResponse_WhenFileNotFound() {
		String fileName = "nonexistent.jpg";

		when(fileStorageService.loadFile(fileName, "posters")).thenReturn(null);

		ResponseEntity<byte[]> result = posterService.getPosterResponse(fileName);

		assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void getPosterUrl_Success() {
		String result = posterService.getPosterUrl(1L, "poster.jpg");

		assertThat(result).isEqualTo("/api/movies/1/poster");
	}

	@Test
	void getPosterUrl_WhenNoPoster() {
		String result = posterService.getPosterUrl(1L, null);

		assertThat(result).isEqualTo("/images/default-poster.jpg");
	}

	@Test
	void hasPoster_Success() {
		String fileName = "test.jpg";

		when(fileStorageService.fileExists(fileName, "posters")).thenReturn(true);

		boolean result = posterService.hasPoster(fileName);

		assertThat(result).isTrue();
	}
}