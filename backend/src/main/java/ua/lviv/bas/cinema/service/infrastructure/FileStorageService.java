package ua.lviv.bas.cinema.service.infrastructure;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;

@Slf4j
@Service
public class FileStorageService {

	@Value("${app.upload.dir:uploads}")
	private String uploadDir;

	public String storeFile(MultipartFile file, String subDirectory) {
		if (file == null || file.isEmpty()) {
			return null;
		}

		try {
			String originalFileName = file.getOriginalFilename();
			String extension = Optional.ofNullable(originalFileName).filter(name -> name.contains("."))
					.map(name -> name.substring(name.lastIndexOf("."))).orElse(".jpg");

			String fileName = UUID.randomUUID() + extension;
			Path uploadPath = Paths.get(uploadDir, subDirectory);
			Files.createDirectories(uploadPath);

			Path filePath = uploadPath.resolve(fileName);
			Files.write(filePath, file.getBytes());

			log.info("File stored successfully: {}", fileName);
			return fileName;
		} catch (IOException e) {
			log.error("Failed to store file", e);
			throw new ExternalServiceException("File Storage", e);
		}
	}

	public byte[] loadFile(String fileName, String subDirectory) {
		if (fileName == null || fileName.isBlank()) {
			return null;
		}

		try {
			Path path = Paths.get(uploadDir, subDirectory, fileName);
			if (!Files.exists(path)) {
				log.warn("File not found: {}", fileName);
				return null;
			}

			return Files.readAllBytes(path);
		} catch (IOException e) {
			log.error("Error loading file: {}", fileName, e);
			throw new ExternalServiceException("File Storage", e);
		}
	}

	public void deleteFile(String fileName, String subDirectory) {
		if (fileName == null || fileName.isBlank()) {
			return;
		}

		try {
			Path path = Paths.get(uploadDir, subDirectory, fileName);
			if (Files.exists(path)) {
				Files.delete(path);
				log.info("File deleted successfully: {}", fileName);
			}
		} catch (IOException e) {
			log.error("Failed to delete file: {}", fileName, e);
		}
	}

	public String determineContentType(String fileName) {
		if (fileName == null) {
			return MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}

		String lower = fileName.toLowerCase();
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

		Path path = Paths.get(uploadDir, subDirectory, fileName);
		return Files.exists(path);
	}
}