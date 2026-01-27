package ua.lviv.bas.cinema.service.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Refund;

@ExtendWith(MockitoExtension.class)
public class NumberGeneratorServiceTest {

	@Mock
	private Booking booking;

	@Mock
	private Refund refund;

	private final NumberGeneratorService numberGeneratorService = new NumberGeneratorService();

	@Test
	void generateBookingNumber_ShouldReturnCorrectFormat() {
		when(booking.getCreatedAt()).thenReturn(LocalDateTime.of(2024, 1, 15, 14, 30));
		when(booking.getId()).thenReturn(12345L);

		String result = numberGeneratorService.generateBookingNumber(booking);

		assertThat(result).isEqualTo("BK-2024-12345");
	}

	@Test
	void generateTicketCode_ShouldStartWithTKT() {
		String result = numberGeneratorService.generateTicketCode();
		assertThat(result).startsWith("TKT-");
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
	}

	@Test
	void generatePaymentOrderId_ShouldStartWithPAY() {
		String result = numberGeneratorService.generatePaymentOrderId();
		assertThat(result).startsWith("PAY_");
	}
}