package ua.lviv.bas.cinema.service.integration.file;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PosterServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private PosterService posterService;

    @Test
    void uploadPosterSuccess() {
        MockMultipartFile file = new MockMultipartFile("poster.jpg", "poster.jpg", "image/jpeg", new byte[10]);
        when(fileStorageService.storeFile(file, "posters")).thenReturn("uuid.jpg");

        String result = posterService.uploadPoster(file);

        assertThat(result).isEqualTo("uuid.jpg");
    }

    @Test
    void deletePosterSuccess() {
        posterService.deletePoster("test.jpg");
        verify(fileStorageService).deleteFile("test.jpg", "posters");
    }

    @Test
    void getPosterResponseSuccess() {
        String fileName = "test.jpg";
        byte[] fileData = new byte[]{1, 2, 3};
        when(fileStorageService.loadFile(fileName, "posters")).thenReturn(fileData);
        when(fileStorageService.determineContentType(fileName)).thenReturn("image/jpeg");

        ResponseEntity<byte[]> result = posterService.getPosterResponse(fileName);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(fileData);
    }

    @Test
    void getPosterResponseWhenFileNotFound() {
        String fileName = "nonexistent.jpg";
        when(fileStorageService.loadFile(fileName, "posters")).thenReturn(null);

        ResponseEntity<byte[]> result = posterService.getPosterResponse(fileName);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}