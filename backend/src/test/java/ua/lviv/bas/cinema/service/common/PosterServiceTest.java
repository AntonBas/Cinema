package ua.lviv.bas.cinema.service.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import ua.lviv.bas.cinema.service.infrastructure.FileStorageService;

@ExtendWith(MockitoExtension.class)
class PosterServiceTest {

	@Mock
	private FileStorageService fileStorageService;

	@InjectMocks
	private PosterService posterService;

	@Test
	void uploadPoster_ShouldCallFileStorageService() {
		MultipartFile file = new MockMultipartFile("poster.jpg", "poster.jpg", "image/jpeg", new byte[10]);

		when(fileStorageService.storeFile(file, "posters")).thenReturn("uuid.jpg");
		String result = posterService.uploadPoster(file);

		assertThat(result).isEqualTo("uuid.jpg");
		verify(fileStorageService).storeFile(file, "posters");
	}

	@Test
	void uploadPoster_WithNullFile_ShouldReturnNull() {
		String result = posterService.uploadPoster(null);
		assertThat(result).isNull();
	}

	@Test
	void deletePoster_ShouldCallFileStorageService() {
		String fileName = "test.jpg";
		posterService.deletePoster(fileName);
		verify(fileStorageService).deleteFile(fileName, "posters");
	}

	@Test
	void deletePoster_WithNullFileName_ShouldDoNothing() {
		posterService.deletePoster(null);
	}

	@Test
	void deletePoster_WithBlankFileName_ShouldDoNothing() {
		posterService.deletePoster("   ");
	}

	@Test
	void getPosterResponse_ShouldReturnResponseEntity() {
		String fileName = "test.jpg";
		byte[] fileData = new byte[] { 1, 2, 3 };

		when(fileStorageService.loadFile(fileName, "posters")).thenReturn(fileData);
		when(fileStorageService.determineContentType(fileName)).thenReturn("image/jpeg");

		ResponseEntity<byte[]> response = posterService.getPosterResponse(fileName);

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEqualTo(fileData);
		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_JPEG);
		assertThat(response.getHeaders().getCacheControl()).isEqualTo("max-age=3600");
	}

	@Test
	void getPosterResponse_WhenFileNotFound_ShouldReturnNotFound() {
		String fileName = "nonexistent.jpg";
		when(fileStorageService.loadFile(fileName, "posters")).thenReturn(null);

		ResponseEntity<byte[]> response = posterService.getPosterResponse(fileName);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void getPosterResponse_WithNullFileName_ShouldReturnNotFound() {
		ResponseEntity<byte[]> response = posterService.getPosterResponse(null);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void getPosterResponse_WithBlankFileName_ShouldReturnNotFound() {
		ResponseEntity<byte[]> response = posterService.getPosterResponse("   ");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void getPosterUrl_ShouldReturnCorrectUrl() {
		Long movieId = 1L;
		String fileName = "poster.jpg";

		String url = posterService.getPosterUrl(movieId, fileName);
		assertThat(url).isEqualTo("/api/movies/1/poster");
	}

	@Test
	void getPosterUrl_WithNullFileName_ShouldReturnDefaultUrl() {
		Long movieId = 1L;
		String url = posterService.getPosterUrl(movieId, null);
		assertThat(url).isEqualTo("/images/default-poster.jpg");
	}

	@Test
	void getPosterUrl_WithBlankFileName_ShouldReturnDefaultUrl() {
		Long movieId = 1L;
		String url = posterService.getPosterUrl(movieId, "   ");
		assertThat(url).isEqualTo("/images/default-poster.jpg");
	}

	@Test
	void hasPoster_ShouldCallFileStorageService() {
		String fileName = "test.jpg";
		when(fileStorageService.fileExists(fileName, "posters")).thenReturn(true);

		boolean result = posterService.hasPoster(fileName);
		assertThat(result).isTrue();
		verify(fileStorageService).fileExists(fileName, "posters");
	}

	@Test
	void hasPoster_WithNullFileName_ShouldReturnFalse() {
		boolean result = posterService.hasPoster(null);
		assertThat(result).isFalse();
	}

	@Test
	void hasPoster_WithBlankFileName_ShouldReturnFalse() {
		boolean result = posterService.hasPoster("   ");
		assertThat(result).isFalse();
	}

	@Test
	void getPosterResponse_ShouldSetCorrectHeadersForPng() {
		String fileName = "test.png";
		byte[] fileData = new byte[10];

		when(fileStorageService.loadFile(fileName, "posters")).thenReturn(fileData);
		when(fileStorageService.determineContentType(fileName)).thenReturn("image/png");

		ResponseEntity<byte[]> response = posterService.getPosterResponse(fileName);

		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_PNG);
		assertThat(response.getHeaders().getCacheControl()).isEqualTo("max-age=3600");
	}

	@Test
	void getPosterResponse_WhenLoadFileReturnsNull_ShouldReturnNotFound() {
		String fileName = "test.jpg";
		when(fileStorageService.loadFile(fileName, "posters")).thenReturn(null);

		ResponseEntity<byte[]> response = posterService.getPosterResponse(fileName);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void getPosterResponse_ShouldSetCorrectHeadersForGif() {
		String fileName = "test.gif";
		byte[] fileData = new byte[10];

		when(fileStorageService.loadFile(fileName, "posters")).thenReturn(fileData);
		when(fileStorageService.determineContentType(fileName)).thenReturn("image/gif");

		ResponseEntity<byte[]> response = posterService.getPosterResponse(fileName);

		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.IMAGE_GIF);
		assertThat(response.getHeaders().getCacheControl()).isEqualTo("max-age=3600");
	}

	@Test
	void getPosterResponse_ShouldSetCorrectHeadersForUnknownType() {
		String fileName = "test.unknown";
		byte[] fileData = new byte[10];

		when(fileStorageService.loadFile(fileName, "posters")).thenReturn(fileData);
		when(fileStorageService.determineContentType(fileName)).thenReturn("application/octet-stream");

		ResponseEntity<byte[]> response = posterService.getPosterResponse(fileName);

		assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_OCTET_STREAM);
		assertThat(response.getHeaders().getCacheControl()).isEqualTo("max-age=3600");
	}
}