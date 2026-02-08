package ua.lviv.bas.cinema.service.booking.payment;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.exception.domain.booking.SessionTooCloseException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentProcessingException;

@Service
@RequiredArgsConstructor
public class PaymentValidator {

	@Value("${booking.session-too-close-minutes:30}")
	private int sessionTooCloseMinutes;

	public void validateBookingForPayment(Booking booking) {
		if (booking.getStatus() != BookingStatus.PENDING) {
			throw PaymentProcessingException.bookingNotPending();
		}

		if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw PaymentProcessingException.bookingExpired();
		}

		if (booking.getSession().getStartTime().isBefore(LocalDateTime.now().plusMinutes(sessionTooCloseMinutes))) {
			throw new SessionTooCloseException(booking.getSession().getStartTime());
		}

		boolean allSeatsAvailable = booking.getBookedSeats().stream()
				.allMatch(seat -> seat.getStatus() == ReservationStatus.PENDING);

		if (!allSeatsAvailable) {
			throw PaymentProcessingException.seatsNoLongerAvailable();
		}
	}
}