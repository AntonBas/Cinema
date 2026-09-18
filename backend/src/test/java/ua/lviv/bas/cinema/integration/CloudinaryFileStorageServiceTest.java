package ua.lviv.bas.cinema.integration;

import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ua.lviv.bas.cinema.exception.infrastructure.ExternalServiceException;
import ua.lviv.bas.cinema.exception.infrastructure.UnsupportedFileTypeException;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CloudinaryFileStorageServiceTest {

    private Cloudinary cloudinary;
    private Uploader uploader;
    private RestTemplate restTemplate;
    private CloudinaryFileStorageService service;

    @BeforeEach
    void setUp() {
        cloudinary = mock(Cloudinary.class);
        uploader = mock(Uploader.class);
        restTemplate = mock(RestTemplate.class);
        when(cloudinary.uploader()).thenReturn(uploader);

        service = new CloudinaryFileStorageService(cloudinary, restTemplate);
    }

    @Test
    void storeFile_Success() throws IOException {
        MockMultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test content".getBytes());
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of("secure_url", "https://res.cloudinary.com/demo/image/upload/v1699999999/posters/abc123.jpg"));

        String result = service.storeFile(file, "posters");

        assertThat(result).isEqualTo("https://res.cloudinary.com/demo/image/upload/v1699999999/posters/abc123.jpg");
    }

    @Test
    void storeFile_WhenFileNull_ShouldReturnNull() {
        assertThat(service.storeFile(null, "posters")).isNull();
    }

    @Test
    void storeFile_WhenFileEmpty_ShouldReturnNull() {
        MockMultipartFile file = new MockMultipartFile("empty", new byte[0]);

        assertThat(service.storeFile(file, "posters")).isNull();
    }

    @Test
    void storeFile_WhenContentTypeNotAllowed_ShouldThrowUnsupportedFileTypeException() {
        MockMultipartFile file = new MockMultipartFile("test.html", "test.html", "text/html",
                "<script>alert(1)</script>".getBytes());

        assertThatThrownBy(() -> service.storeFile(file, "posters"))
                .isInstanceOf(UnsupportedFileTypeException.class);
    }

    @Test
    void storeFile_WhenUploadIOException_ShouldThrowExternalServiceException() throws IOException {
        MockMultipartFile file = new MockMultipartFile("test.jpg", "test.jpg", "image/jpeg", "test content".getBytes());
        when(uploader.upload(any(byte[].class), anyMap())).thenThrow(new IOException("network down"));

        assertThatThrownBy(() -> service.storeFile(file, "posters"))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void loadFile_Success() {
        String url = "https://res.cloudinary.com/demo/image/upload/v1699999999/posters/abc123.jpg";
        byte[] data = {1, 2, 3};
        when(restTemplate.getForObject(url, byte[].class)).thenReturn(data);

        byte[] result = service.loadFile(url, "posters");

        assertThat(result).isEqualTo(data);
    }

    @Test
    void loadFile_WhenFileUrlBlank_ShouldReturnNull() {
        assertThat(service.loadFile("", "posters")).isNull();
    }

    @Test
    void loadFile_WhenRestClientException_ShouldReturnNull() {
        String url = "https://res.cloudinary.com/demo/image/upload/v1699999999/posters/missing.jpg";
        when(restTemplate.getForObject(url, byte[].class)).thenThrow(new RestClientException("404"));

        assertThat(service.loadFile(url, "posters")).isNull();
    }

    @Test
    void deleteFile_Success() throws IOException {
        service.deleteFile("https://res.cloudinary.com/demo/image/upload/v1699999999/posters/abc123.jpg", "posters");

        verify(uploader).destroy(eq("posters/abc123"), anyMap());
    }

    @Test
    void deleteFile_WhenInvalidUrl_ShouldSkipDeletion() throws IOException {
        service.deleteFile("not-a-cloudinary-url", "posters");

        verify(uploader, never()).destroy(any(), anyMap());
    }

    @Test
    void deleteFile_WhenIOException_ShouldNotThrow() throws IOException {
        when(uploader.destroy(any(), anyMap())).thenThrow(new IOException("network down"));

        service.deleteFile("https://res.cloudinary.com/demo/image/upload/v1699999999/posters/abc123.jpg", "posters");
    }

    @Test
    void determineContentType_Success() {
        assertThat(service.determineContentType("https://res.cloudinary.com/demo/posters/abc.jpg")).isEqualTo("image/jpeg");
        assertThat(service.determineContentType("https://res.cloudinary.com/demo/posters/abc.png")).isEqualTo("image/png");
        assertThat(service.determineContentType("https://res.cloudinary.com/demo/posters/abc.unknown"))
                .isEqualTo("application/octet-stream");
        assertThat(service.determineContentType(null)).isEqualTo("application/octet-stream");
    }

    @ParameterizedTest
    @CsvSource({
            "https://res.cloudinary.com/demo/image/upload/v1699999999/posters/abc123.jpg, posters/abc123",
            "https://res.cloudinary.com/demo/image/upload/posters/abc123.png, posters/abc123",
            "https://res.cloudinary.com/demo/image/upload/v1/abc123.jpeg, abc123",
    })
    void extractPublicId_VariousUrlShapes(String url, String expectedPublicId) {
        assertThat(CloudinaryFileStorageService.extractPublicId(url)).isEqualTo(expectedPublicId);
    }

    @Test
    void extractPublicId_WhenMalformedOrNull_ShouldReturnNull() {
        assertThat(CloudinaryFileStorageService.extractPublicId("https://example.com/no-upload-segment.jpg")).isNull();
        assertThat(CloudinaryFileStorageService.extractPublicId(null)).isNull();
    }
}
