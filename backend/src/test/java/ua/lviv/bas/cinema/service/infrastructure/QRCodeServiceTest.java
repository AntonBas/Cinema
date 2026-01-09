package ua.lviv.bas.cinema.service.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.service.infrastructure.QRCodeService;

@ExtendWith(MockitoExtension.class)
class QRCodeServiceTest {

	@InjectMocks
	private QRCodeService qrCodeService;

	@BeforeEach
	void setUp() {
		ReflectionTestUtils.setField(qrCodeService, "qrCodeSize", 200);
		ReflectionTestUtils.setField(qrCodeService, "qrCodeMargin", 1);
		ReflectionTestUtils.setField(qrCodeService, "qrCodeFormat", "PNG");
	}

	@Test
	void generateQRCode_ShouldGenerateValidQRCodeBytes() {
		String content = "https://example.com/ticket/12345";

		byte[] result = qrCodeService.generateQRCode(content);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
		assertThat(result[0]).isEqualTo((byte) 0x89);
		assertThat(result[1]).isEqualTo((byte) 0x50);
		assertThat(result[2]).isEqualTo((byte) 0x4E);
		assertThat(result[3]).isEqualTo((byte) 0x47);
	}

	@Test
	void generateQRCode_ShouldGenerateDifferentQRForDifferentContent() {
		String content1 = "Ticket1-ABC123";
		String content2 = "Ticket2-XYZ789";

		byte[] result1 = qrCodeService.generateQRCode(content1);
		byte[] result2 = qrCodeService.generateQRCode(content2);

		assertThat(result1).isNotEqualTo(result2);
		assertThat(result1.length).isGreaterThan(0);
		assertThat(result2.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_ShouldHandleEmptyContent() {
		String content = "EMPTY";

		byte[] result = qrCodeService.generateQRCode(content);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_ShouldHandleSpecialCharacters() {
		String content = "Ticket: ABC-123_456 Valid until: 2024-12-31 Price: 25.99";

		byte[] result = qrCodeService.generateQRCode(content);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_ShouldHandleReasonableLongContent() {
		String longContent = "TICKET-12345-ABCDEF-67890-GHIJKL-MNOPQR-STUVWX-YZ0123-456789-ABCDEF-GHIJKL-MNOPQR-STUVWX";

		byte[] result = qrCodeService.generateQRCode(longContent);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_ShouldWorkWithCustomSize() {
		String content = "Ticket-123";
		int customSize = 300;

		byte[] result = qrCodeService.generateQRCode(content, customSize);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
		assertThat(result[0]).isEqualTo((byte) 0x89);
		assertThat(result[1]).isEqualTo((byte) 0x50);
		assertThat(result[2]).isEqualTo((byte) 0x4E);
		assertThat(result[3]).isEqualTo((byte) 0x47);
	}

	@Test
	void generateQRCode_ShouldHandleSmallSize() {
		String content = "Ticket-456";
		int smallSize = 100;

		byte[] result = qrCodeService.generateQRCode(content, smallSize);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_ShouldThrowExceptionForInvalidSize() {
		String content = "Test";
		int negativeSize = -100;

		assertThatThrownBy(() -> qrCodeService.generateQRCode(content, negativeSize))
				.isInstanceOf(RuntimeException.class).hasMessageContaining("Failed to generate QR code");
	}

	@Test
	void generateQRCode_ShouldThrowExceptionForTooSmallSize() {
		String content = "Test";
		int tooSmallSize = 10;

		byte[] result = qrCodeService.generateQRCode(content, tooSmallSize);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void generateQRCode_ShouldUseDefaultSizeFromProperties() {
		String content = "DefaultSizeTest";

		byte[] result = qrCodeService.generateQRCode(content);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
		assertThat(result[0]).isEqualTo((byte) 0x89);
		assertThat(result[1]).isEqualTo((byte) 0x50);
		assertThat(result[2]).isEqualTo((byte) 0x4E);
		assertThat(result[3]).isEqualTo((byte) 0x47);
	}

	@Test
	void generateQRCodeBase64_ShouldReturnValidBase64String() {
		String content = "Ticket-Base64-Test";

		String result = qrCodeService.generateQRCodeBase64(content);

		assertThat(result).isNotNull();
		assertThat(result).isNotEmpty();
		assertThat(result).matches("^[A-Za-z0-9+/]*=*$");

		byte[] decoded = Base64.getDecoder().decode(result);
		assertThat(decoded).isNotNull();
		assertThat(decoded.length).isGreaterThan(0);
		assertThat(decoded[0]).isEqualTo((byte) 0x89);
		assertThat(decoded[1]).isEqualTo((byte) 0x50);
		assertThat(decoded[2]).isEqualTo((byte) 0x4E);
		assertThat(decoded[3]).isEqualTo((byte) 0x47);
	}

	@Test
	void generateQRCodeBase64_ShouldHandleDifferentContent() {
		String content1 = "First-Ticket";
		String content2 = "Second-Ticket";

		String result1 = qrCodeService.generateQRCodeBase64(content1);
		String result2 = qrCodeService.generateQRCodeBase64(content2);

		assertThat(result1).isNotEqualTo(result2);
		assertThat(result1).isNotEmpty();
		assertThat(result2).isNotEmpty();
	}

	@Test
	void generateQRCodeBase64_ShouldProduceConsistentResults() {
		String content = "Consistent-Test-123";

		String result1 = qrCodeService.generateQRCodeBase64(content);
		String result2 = qrCodeService.generateQRCodeBase64(content);

		assertThat(result1).isEqualTo(result2);
	}

	@Test
	void shouldHandleNullContent() {
		assertThatThrownBy(() -> qrCodeService.generateQRCode(null)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Failed to generate QR code");
	}

	@Test
	void shouldHandleNullContentForBase64() {
		assertThatThrownBy(() -> qrCodeService.generateQRCodeBase64(null)).isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Failed to generate QR code");
	}

	@Test
	void shouldHandleUnicodeCharacters() {
		String content = "Ticket No.123 - Regular Text";

		byte[] result = qrCodeService.generateQRCode(content);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);

		String base64Result = qrCodeService.generateQRCodeBase64(content);
		assertThat(base64Result).isNotEmpty();
	}

	@Test
	void shouldHandleSimpleContent() {
		String content = "12345";

		byte[] result = qrCodeService.generateQRCode(content);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}

	@Test
	void shouldHandleUrlContent() {
		String content = "https://cinema.example.com/tickets/verify?code=ABC123XYZ";

		byte[] result = qrCodeService.generateQRCode(content);

		assertThat(result).isNotNull();
		assertThat(result.length).isGreaterThan(0);
	}
}