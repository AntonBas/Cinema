package ua.lviv.bas.cinema.service.common;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Refund;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NumberGeneratorServiceTest {

	@Mock
	private Booking booking;

	@Mock
	private Refund refund;

	private final NumberGeneratorService numberGeneratorService = new NumberGeneratorService();

	@Test
	void generateBookingNumber_ShouldReturnCorrectFormat() {
		when(booking.getCreatedDate()).thenReturn(LocalDateTime.of(2024, 1, 15, 14, 30));
		when(booking.getId()).thenReturn(12345L);

		String result = numberGeneratorService.generateBookingNumber(booking);

		assertThat(result).isEqualTo("BK-2024-12345");
	}

	@Test
	void generateBookingNumber_WhenIdIsNull_ShouldThrowException() {
		when(booking.getId()).thenReturn(null);

		assertThatThrownBy(() -> numberGeneratorService.generateBookingNumber(booking))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Booking ID is required");
	}

	@Test
	void generateBookingNumber_WhenCreatedDateIsNull_ShouldThrowException() {
		when(booking.getId()).thenReturn(12345L);
		when(booking.getCreatedDate()).thenReturn(null);

		assertThatThrownBy(() -> numberGeneratorService.generateBookingNumber(booking))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Booking createdDate is required");
	}

	@Test
	void generateTicketCode_ShouldStartWithTKT() {
		String result = numberGeneratorService.generateTicketCode();
		assertThat(result).startsWith("TKT-");
		assertThat(result).hasSize(4 + 12);
	}

	@Test
	void generateLiqpayOrderId_ShouldStartWithORD() {
		String result = numberGeneratorService.generateLiqpayOrderId();
		assertThat(result).startsWith("ORD_");
	}

	@Test
	void generateRefundNumber_ShouldStartWithRF() {
		when(refund.getId()).thenReturn(98765L);
		String result = numberGeneratorService.generateRefundNumber(refund);
		assertThat(result).startsWith("RF-");
		assertThat(result).contains(String.valueOf(LocalDateTime.now().getYear()));
	}

	@Test
	void generateRefundNumber_WhenIdIsNull_ShouldThrowException() {
		when(refund.getId()).thenReturn(null);

		assertThatThrownBy(() -> numberGeneratorService.generateRefundNumber(refund))
				.isInstanceOf(IllegalStateException.class).hasMessageContaining("Refund ID is required");
	}
}