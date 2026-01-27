package ua.lviv.bas.cinema.service.shared;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Refund;

@Service
public class NumberGeneratorService {

	public String generateBookingNumber(Booking booking) {
		return String.format("BK-%d-%05d", booking.getCreatedAt().getYear(), booking.getId());
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
		return String.format("RF-%04d-%06d", LocalDateTime.now().getYear(), refund.getId());
	}

	public String generatePaymentOrderId() {
		return "PAY_" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
	}
}