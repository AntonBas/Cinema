package ua.lviv.bas.cinema.service.common;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Refund;

@Service
public class NumberGeneratorService {

	public static String generateBookingNumberStatic(Booking booking) {
		if (booking.getId() == null) {
			throw new IllegalStateException("Booking ID is required");
		}
		if (booking.getCreatedDate() == null) {
			throw new IllegalStateException("Booking createdDate is required");
		}
		var year = booking.getCreatedDate().getYear();
		return String.format("BK-%d-%05d", year, booking.getId());
	}

	public String generateBookingNumber(Booking booking) {
		return generateBookingNumberStatic(booking);
	}

	public String generateTicketCode() {
		var uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
		return "TKT-" + uuid;
	}

	public String generateLiqpayOrderId() {
		return "ORD_" + UUID.randomUUID().toString().substring(0, 16).toUpperCase() + "_"
				+ System.currentTimeMillis() % 10000;
	}

	public String generateRefundNumber(Refund refund) {
		if (refund.getId() == null) {
			throw new IllegalStateException("Refund ID is required");
		}
		return String.format("RF-%04d-%06d", LocalDateTime.now().getYear(), refund.getId());
	}

	public String generatePaymentOrderId() {
		return "PAY_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
	}
}