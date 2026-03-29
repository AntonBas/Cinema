package ua.lviv.bas.cinema.service.shared;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Refund;

@Service
public class NumberGeneratorService {

	public static String generateBookingNumberStatic(Booking booking) {
		if (booking.getId() == null) {
			throw new IllegalStateException("Booking ID is required to generate booking number");
		}
		if (booking.getCreatedAt() == null) {
			throw new IllegalStateException("Booking createdAt is required to generate booking number");
		}
		return String.format("BK-%d-%05d", booking.getCreatedAt().getYear(), booking.getId());
	}

	public String generateBookingNumber(Booking booking) {
		return generateBookingNumberStatic(booking);
	}

	public String generateTicketCode() {
		String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
		return "TKT-" + uuid;
	}

	public String generateLiqpayOrderId() {
		return "ORD_" + UUID.randomUUID().toString().substring(0, 16).toUpperCase() + "_"
				+ System.currentTimeMillis() % 10000;
	}

	public String generateRefundNumber(Refund refund) {
		if (refund.getId() == null) {
			throw new IllegalStateException("Refund ID is required to generate refund number");
		}
		return String.format("RF-%04d-%06d", LocalDateTime.now().getYear(), refund.getId());
	}

	public String generatePaymentOrderId() {
		return "PAY_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
	}
}