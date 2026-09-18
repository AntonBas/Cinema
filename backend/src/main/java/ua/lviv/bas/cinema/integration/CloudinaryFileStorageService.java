package ua.lviv.bas.cinema.integration;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;
import ua.lviv.bas.cinema.exception.infrastructure.UnsupportedFileTypeException;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
@Profile("prod")
@RequiredArgsConstructor
public class CloudinaryFileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/gif",
            "image/webp");
    private static final Pattern VERSION_SEGMENT = Pattern.compile("^v\\d+/");

    private final Cloudinary cloudinary;
    private final RestTemplate restTemplate;

    @Override
    public String storeFile(MultipartFile file, String subDirectory) {
        if (file == null || file.isEmpty()) {
            log.warn("Attempted to store null or empty file");
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            log.warn("Rejected file upload with unsupported content type: {}", contentType);
            throw new UnsupportedFileTypeException(contentType);
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap("folder", subDirectory));
            String url = (String) result.get("secure_url");
            log.info("File uploaded to Cloudinary: {}", url);
            return url;
        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary in directory: {}", subDirectory, e);
            throw new ExternalServiceException("Cloudinary", e);
        }
    }

    @Override
    public byte[] loadFile(String fileUrl, String subDirectory) {
        if (fileUrl == null || fileUrl.isBlank()) {
            log.warn("Attempted to load file with null or blank name");
            return null;
        }

        try {
            return restTemplate.getForObject(fileUrl, byte[].class);
        } catch (RestClientException e) {
            log.warn("File not found or error loading from Cloudinary: {}", fileUrl);
            return null;
        }
    }

    @Override
    public void deleteFile(String fileUrl, String subDirectory) {
        String publicId = extractPublicId(fileUrl);
        if (publicId == null) {
            log.warn("Invalid Cloudinary URL for deletion: {}", fileUrl);
            return;
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("File deleted from Cloudinary: {}", publicId);
        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary: {}", publicId, e);
        }
    }

    @Override
    public String determineContentType(String fileUrl) {
        if (fileUrl == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        String lower = fileUrl.toLowerCase();
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

    static String extractPublicId(String secureUrl) {
        if (secureUrl == null) {
            return null;
        }

        int uploadIndex = secureUrl.indexOf("/upload/");
        if (uploadIndex == -1) {
            return null;
        }

        String afterUpload = secureUrl.substring(uploadIndex + "/upload/".length());
        afterUpload = VERSION_SEGMENT.matcher(afterUpload).replaceFirst("");

        int lastDot = afterUpload.lastIndexOf('.');
        return lastDot == -1 ? afterUpload : afterUpload.substring(0, lastDot);
    }
}
