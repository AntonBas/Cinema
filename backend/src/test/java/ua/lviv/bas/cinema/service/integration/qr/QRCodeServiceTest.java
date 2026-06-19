package ua.lviv.bas.cinema.service.integration.qr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import ua.lviv.bas.cinema.exception.domain.technical.QRCodeGenerationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class QRCodeServiceTest {

    private QRCodeService qrCodeService;

    @BeforeEach
    void setUp() {
        qrCodeService = new QRCodeService();
        ReflectionTestUtils.setField(qrCodeService, "qrCodeMargin", 1);
        ReflectionTestUtils.setField(qrCodeService, "qrCodeFormat", "PNG");
    }

    @Test
    void generateQRCodeSuccess() {
        byte[] result = qrCodeService.generateQRCode("https://example.com", 200);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCodeWithCustomSize() {
        byte[] result = qrCodeService.generateQRCode("https://example.com", 250);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCodeWhenContentIsEmptyShouldThrowException() {
        assertThatThrownBy(() -> qrCodeService.generateQRCode("", 200))
                .isInstanceOf(QRCodeGenerationException.class);
    }

    @Test
    void generateQRCodeWhenContentIsNullShouldThrowException() {
        assertThatThrownBy(() -> qrCodeService.generateQRCode(null, 200))
                .isInstanceOf(QRCodeGenerationException.class);
    }

    @Test
    void generateQRCodeWithVeryLongContent() {
        String longContent = "https://example.com/ticket/" + "a".repeat(100);

        byte[] result = qrCodeService.generateQRCode(longContent, 200);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCodeWithSpecialCharacters() {
        String content = "Ticket №: ABC-123_456!@#$%^&*()";

        byte[] result = qrCodeService.generateQRCode(content, 200);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCodeWithSmallSize() {
        byte[] result = qrCodeService.generateQRCode("test", 100);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);
    }

    @Test
    void generateQRCodeWithDifferentContentShouldProduceDifferentResults() {
        byte[] result1 = qrCodeService.generateQRCode("content1", 200);
        byte[] result2 = qrCodeService.generateQRCode("content2", 200);

        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1).isNotEqualTo(result2);
    }
}