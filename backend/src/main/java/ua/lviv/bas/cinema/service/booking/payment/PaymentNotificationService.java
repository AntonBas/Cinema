package ua.lviv.bas.cinema.service.booking.payment;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.service.notification.EmailService;
import ua.lviv.bas.cinema.service.shared.DateTimeFormatterService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentNotificationService {
	private final EmailService emailService;
	private final DateTimeFormatterService dateTimeFormatter;
	private final NumberGeneratorService numberGenerator;

	public void sendPaymentSuccessEmail(Payment payment, Booking booking, List<Ticket> tickets) {
		try {
			String sessionTime = formatSessionTime(booking);
			String seatsInfo = extractSeatsInfo(booking);
			String bookingNumber = numberGenerator.generateBookingNumber(booking);

			emailService.sendTicketsEmail(booking.getUser().getEmail(), bookingNumber,
					booking.getSession().getMovie().getTitle(), sessionTime, booking.getSession().getHall().getName(),
					payment.getAmount(), "Credit card", seatsInfo);

			log.debug("Sent payment success email to {}", booking.getUser().getEmail());
		} catch (Exception e) {
			log.error("Failed to send payment success email for booking {}", booking.getId(), e);
		}
	}

	public void sendPaymentFailedEmail(Payment payment, Booking booking) {
		try {
			String sessionTime = formatSessionTime(booking);
			String errorDescription = payment.getLiqpayErrorDescription() != null ? payment.getLiqpayErrorDescription()
					: "Payment error";
			String bookingNumber = numberGenerator.generateBookingNumber(booking);

			emailService.sendPaymentFailedEmail(booking.getUser().getEmail(), bookingNumber,
					booking.getSession().getMovie().getTitle(), sessionTime, errorDescription);

			log.debug("Sent payment failed email to {}", booking.getUser().getEmail());
		} catch (Exception e) {
			log.error("Failed to send payment failed email for booking {}", booking.getId(), e);
		}
	}

	public void sendRefundEmail(Payment payment, BigDecimal amount, String description) {
		try {
			Booking booking = payment.getBooking();
			String sessionTime = formatSessionTime(booking);
			String seatsInfo = extractSeatsInfo(booking);
			String bookingNumber = numberGenerator.generateBookingNumber(booking);

			emailService.sendRefundEmail(booking.getUser().getEmail(), bookingNumber,
					booking.getSession().getMovie().getTitle(), sessionTime, booking.getSession().getHall().getName(),
					amount, seatsInfo, description);

			log.debug("Sent refund email to {}", booking.getUser().getEmail());
		} catch (Exception e) {
			log.error("Failed to send refund email for payment {}", payment.getId(), e);
		}
	}

	private String formatSessionTime(Booking booking) {
		return dateTimeFormatter.formatStandard(booking.getSession().getStartTime());
	}

	private String extractSeatsInfo(Booking booking) {
		return booking.getSeatReservations().stream()
				.map(seat -> String.format("Row %d, Seat %d", seat.getSeat().getRow(), seat.getSeat().getNumber()))
				.collect(java.util.stream.Collectors.joining(", "));
	}
}