package ua.lviv.bas.cinema.service.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;

class FileStorageServiceTest {

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
	void storeFile_ShouldStoreFileAndReturnFileName() throws IOException {
		MultipartFile file = new MockMultipartFile("test.jpg", "original-test.jpg", "image/jpeg",
				"test content".getBytes());

		String fileName = fileStorageService.storeFile(file, "test");

		assertThat(fileName).isNotNull();
		assertThat(fileName).endsWith(".jpg");
		assertThat(Files.exists(tempDir.resolve("test").resolve(fileName))).isTrue();
	}

	@Test
	void storeFile_WithNullFile_ShouldReturnNull() {
		String fileName = fileStorageService.storeFile(null, "test");
		assertThat(fileName).isNull();
	}

	@Test
	void storeFile_WithEmptyFile_ShouldReturnNull() {
		MultipartFile file = new MockMultipartFile("empty", new byte[0]);
		String fileName = fileStorageService.storeFile(file, "test");
		assertThat(fileName).isNull();
	}

	@Test
	void storeFile_WithNoExtension_ShouldUseDefaultExtension() throws IOException {
		MultipartFile file = new MockMultipartFile("test", "test", "image/jpeg", "content".getBytes());

		String fileName = fileStorageService.storeFile(file, "test");
		assertThat(fileName).endsWith(".jpg");
	}

	@Test
	void storeFile_WhenIOException_ShouldThrowException() throws IOException {
		MultipartFile file = mock(MultipartFile.class);
		when(file.isEmpty()).thenReturn(false);
		when(file.getOriginalFilename()).thenReturn("test.jpg");
		when(file.getBytes()).thenThrow(new IOException("Test exception"));

		assertThatThrownBy(() -> fileStorageService.storeFile(file, "test"))
				.isInstanceOf(ExternalServiceException.class);
	}

	@Test
	void loadFile_ShouldReturnFileContent() throws IOException {
		Path subDir = tempDir.resolve("test");
		Files.createDirectories(subDir);
		Path file = subDir.resolve("test.txt");
		Files.write(file, "content".getBytes());

		byte[] content = fileStorageService.loadFile("test.txt", "test");
		assertThat(content).isEqualTo("content".getBytes());
	}

	@Test
	void loadFile_WhenFileNotFound_ShouldReturnNull() {
		byte[] content = fileStorageService.loadFile("nonexistent.txt", "test");
		assertThat(content).isNull();
	}

	@Test
	void loadFile_WithNullFileName_ShouldReturnNull() {
		byte[] content = fileStorageService.loadFile(null, "test");
		assertThat(content).isNull();
	}

	@Test
	void loadFile_WithBlankFileName_ShouldReturnNull() {
		byte[] content = fileStorageService.loadFile("   ", "test");
		assertThat(content).isNull();
	}

	@Test
	void deleteFile_ShouldDeleteFile() throws IOException {
		Path subDir = tempDir.resolve("test");
		Files.createDirectories(subDir);
		Path file = subDir.resolve("test.txt");
		Files.write(file, "content".getBytes());

		fileStorageService.deleteFile("test.txt", "test");
		assertThat(Files.exists(file)).isFalse();
	}

	@Test
	void deleteFile_WhenFileNotExists_ShouldDoNothing() {
		fileStorageService.deleteFile("nonexistent.txt", "test");
	}

	@Test
	void deleteFile_WithNullFileName_ShouldDoNothing() {
		fileStorageService.deleteFile(null, "test");
	}

	@Test
	void determineContentType_ShouldReturnCorrectMimeType() {
		assertThat(fileStorageService.determineContentType("test.jpg")).isEqualTo("image/jpeg");
		assertThat(fileStorageService.determineContentType("test.jpeg")).isEqualTo("image/jpeg");
		assertThat(fileStorageService.determineContentType("test.png")).isEqualTo("image/png");
		assertThat(fileStorageService.determineContentType("test.gif")).isEqualTo("image/gif");
		assertThat(fileStorageService.determineContentType("test.webp")).isEqualTo("image/webp");
		assertThat(fileStorageService.determineContentType("test.unknown")).isEqualTo("application/octet-stream");
		assertThat(fileStorageService.determineContentType(null)).isEqualTo("application/octet-stream");
	}

	@Test
	void fileExists_ShouldReturnTrueWhenFileExists() throws IOException {
		Path subDir = tempDir.resolve("test");
		Files.createDirectories(subDir);
		Path file = subDir.resolve("test.txt");
		Files.write(file, "content".getBytes());

		boolean exists = fileStorageService.fileExists("test.txt", "test");
		assertThat(exists).isTrue();
	}

	@Test
	void fileExists_ShouldReturnFalseWhenFileNotExists() {
		boolean exists = fileStorageService.fileExists("nonexistent.txt", "test");
		assertThat(exists).isFalse();
	}

	@Test
	void fileExists_WithNullFileName_ShouldReturnFalse() {
		boolean exists = fileStorageService.fileExists(null, "test");
		assertThat(exists).isFalse();
	}

	@Test
	void fileExists_WithBlankFileName_ShouldReturnFalse() {
		boolean exists = fileStorageService.fileExists("   ", "test");
		assertThat(exists).isFalse();
	}

	@AfterEach
	void tearDown() throws IOException {
		if (Files.exists(tempDir)) {
			Files.walk(tempDir).sorted((a, b) -> -a.compareTo(b)).forEach(path -> {
				try {
					Files.deleteIfExists(path);
				} catch (IOException e) {
				}
			});
		}
	}
}