package ua.lviv.bas.cinema.service.booking.creation;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.status.CinemaSessionStatus;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;

@Service
@RequiredArgsConstructor
public class BookingValidator {

	@Value("${booking.session-too-close-minutes:30}")
	private int sessionTooCloseMinutes;

	public void validateSessionForBooking(Session session) {
		if (session.getStatus() != CinemaSessionStatus.SCHEDULED) {
			throw BookingValidationException.sessionNotAvailable();
		}

		if (session.getStartTime().isBefore(LocalDateTime.now())) {
			throw BookingValidationException.sessionAlreadyStarted();
		}

		if (session.getStartTime().isBefore(LocalDateTime.now().plusMinutes(sessionTooCloseMinutes))) {
			throw BookingValidationException.sessionTooClose();
		}
	}

	public boolean canBookingBeCancelled(Booking booking) {
		return booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED;
	}
}