package ua.lviv.bas.cinema.service.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;

public class FileStorageServiceTest {

	@TempDir
	Path tempDir;

	private FileStorageService fileStorageService;

	@BeforeEach
	void setUp() throws Exception {
		fileStorageService = new FileStorageService();

		var field = FileStorageService.class.getDeclaredField("uploadDir");
		field.setAccessible(true);
		field.set(fileStorageService, tempDir.toString());
	}

	@Test
	void storeFile_Success() throws IOException {
		MockMultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test content".getBytes());

		String result = fileStorageService.storeFile(file, "test");

		assertThat(result).isNotNull();
		assertThat(result).endsWith(".jpg");
		assertThat(Files.exists(tempDir.resolve("test").resolve(result))).isTrue();
	}

	@Test
	void storeFile_WhenFileNull_ShouldReturnNull() {
		String result = fileStorageService.storeFile(null, "test");

		assertThat(result).isNull();
	}

	@Test
	void storeFile_WhenFileEmpty_ShouldReturnNull() {
		MockMultipartFile file = new MockMultipartFile("empty", new byte[0]);

		String result = fileStorageService.storeFile(file, "test");

		assertThat(result).isNull();
	}

	@Test
	void storeFile_WhenIOException_ShouldThrowException() throws IOException {
		MockMultipartFile file = mock(MockMultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getOriginalFilename()).thenReturn("test.jpg");
		when(file.getBytes()).thenThrow(new IOException("Test"));

		assertThatThrownBy(() -> fileStorageService.storeFile(file, "test"))
				.isInstanceOf(ExternalServiceException.class);
	}

	@Test
	void loadFile_Success() throws IOException {
		Path subDir = tempDir.resolve("test");
		Files.createDirectories(subDir);
		Files.write(subDir.resolve("test.txt"), "content".getBytes());

		byte[] result = fileStorageService.loadFile("test.txt", "test");

		assertThat(result).isEqualTo("content".getBytes());
	}

	@Test
	void loadFile_WhenFileNotFound_ShouldReturnNull() {
		byte[] result = fileStorageService.loadFile("nonexistent.txt", "test");

		assertThat(result).isNull();
	}

	@Test
	void deleteFile_Success() throws IOException {
		Path subDir = tempDir.resolve("test");
		Files.createDirectories(subDir);
		Files.write(subDir.resolve("test.txt"), "content".getBytes());

		fileStorageService.deleteFile("test.txt", "test");

		assertThat(Files.exists(subDir.resolve("test.txt"))).isFalse();
	}

	@Test
	void determineContentType_Success() {
		assertThat(fileStorageService.determineContentType("test.jpg")).isEqualTo("image/jpeg");
		assertThat(fileStorageService.determineContentType("test.png")).isEqualTo("image/png");
		assertThat(fileStorageService.determineContentType("test.unknown")).isEqualTo("application/octet-stream");
	}

	@Test
	void fileExists_Success() throws IOException {
		Path subDir = tempDir.resolve("test");
		Files.createDirectories(subDir);
		Files.write(subDir.resolve("test.txt"), "content".getBytes());

		boolean result = fileStorageService.fileExists("test.txt", "test");

		assertThat(result).isTrue();
	}

	@Test
	void fileExists_WhenFileNotFound_ShouldReturnFalse() {
		boolean result = fileStorageService.fileExists("nonexistent.txt", "test");

		assertThat(result).isFalse();
	}
}