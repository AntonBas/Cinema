package ua.lviv.bas.cinema.service.integration.qr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.exception.domain.technical.QRCodeGenerationException;

public class QRCodeServiceTest {

	private QRCodeService qrCodeService;

	@BeforeEach
	void setUp() {
		qrCodeService = new QRCodeService();
		ReflectionTestUtils.setField(qrCodeService, "qrCodeSize", 200);
		ReflectionTestUtils.setField(qrCodeService, "qrCodeMargin", 1);
		ReflectionTestUtils.setField(qrCodeService, "qrCodeFormat", "PNG");
	}

	@Test
	void generateQRCode_Success() {
		byte[] result = qrCodeService.generateQRCode("https://example.com");

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_WithCustomSize() {
		byte[] result = qrCodeService.generateQRCode("https://example.com", 250);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_WhenContentIsEmpty_ShouldThrowException() {
		assertThatThrownBy(() -> qrCodeService.generateQRCode("")).isInstanceOf(QRCodeGenerationException.class);
	}

	@Test
	void generateQRCodeBase64_Success() {
		String result = qrCodeService.generateQRCodeBase64("https://example.com");

		assertThat(result).isNotNull();
		assertThat(result).isNotEmpty();
	}

	@Test
	void generateQRCode_WhenContentIsNull_ShouldThrowException() {
		assertThatThrownBy(() -> qrCodeService.generateQRCode(null)).isInstanceOf(QRCodeGenerationException.class);
	}

	@Test
	void generateQRCode_WithVeryLongContent() {
		String longContent = "https://example.com/ticket/" + "a".repeat(100);

		byte[] result = qrCodeService.generateQRCode(longContent);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_WithSpecialCharacters() {
		String content = "Ticket №: ABC-123_456!@#$%^&*()";

		byte[] result = qrCodeService.generateQRCode(content);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_WithSmallSize() {
		byte[] result = qrCodeService.generateQRCode("test", 100);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_WithDifferentContent_ShouldProduceDifferentResults() {
		byte[] result1 = qrCodeService.generateQRCode("content1");
		byte[] result2 = qrCodeService.generateQRCode("content2");

		assertThat(result1).isNotNull();
		assertThat(result2).isNotNull();
		assertThat(result1).isNotEqualTo(result2);
	}
}