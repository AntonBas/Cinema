package ua.lviv.bas.cinema.service.booking.creation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.CinemaSessionStatus;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;

public class BookingValidatorTest {

	private BookingValidator bookingValidator;

	@BeforeEach
	void setUp() {
		bookingValidator = new BookingValidator();
		ReflectionTestUtils.setField(bookingValidator, "sessionTooCloseMinutes", 30);
	}

	@Test
	void validateSessionForBooking_Success() {
		Session session = new Session();
		session.setStatus(CinemaSessionStatus.SCHEDULED);
		session.setStartTime(LocalDateTime.now().plusHours(2));

		bookingValidator.validateSessionForBooking(session);
	}

	@Test
	void validateSessionForBooking_WhenSessionNotScheduled_ShouldThrowException() {
		Session session = new Session();
		session.setStatus(CinemaSessionStatus.CANCELLED);
		session.setStartTime(LocalDateTime.now().plusHours(2));

		BookingValidationException exception = assertThrows(BookingValidationException.class,
				() -> bookingValidator.validateSessionForBooking(session));

		assertThat(exception.getMessage()).contains("not available");
	}

	@Test
	void validateSessionForBooking_WhenSessionAlreadyStarted_ShouldThrowException() {
		Session session = new Session();
		session.setStatus(CinemaSessionStatus.SCHEDULED);
		session.setStartTime(LocalDateTime.now().minusHours(1));

		BookingValidationException exception = assertThrows(BookingValidationException.class,
				() -> bookingValidator.validateSessionForBooking(session));

		assertThat(exception.getMessage()).contains("already started");
	}

	@Test
	void validateSessionForBooking_WhenSessionTooClose_ShouldThrowException() {
		Session session = new Session();
		session.setStatus(CinemaSessionStatus.SCHEDULED);
		session.setStartTime(LocalDateTime.now().plusMinutes(20));

		BookingValidationException exception = assertThrows(BookingValidationException.class,
				() -> bookingValidator.validateSessionForBooking(session));

		assertThat(exception.getMessage()).contains("starts in less than 30 minutes");
	}

	@Test
	void canBookingBeCancelled_WhenPending_ShouldReturnTrue() {
		Booking booking = new Booking();
		booking.setStatus(BookingStatus.PENDING);

		boolean result = bookingValidator.canBookingBeCancelled(booking);

		assertThat(result).isTrue();
	}

	@Test
	void canBookingBeCancelled_WhenConfirmed_ShouldReturnTrue() {
		Booking booking = new Booking();
		booking.setStatus(BookingStatus.CONFIRMED);

		boolean result = bookingValidator.canBookingBeCancelled(booking);

		assertThat(result).isTrue();
	}

	@Test
	void canBookingBeCancelled_WhenExpired_ShouldReturnFalse() {
		Booking booking = new Booking();
		booking.setStatus(BookingStatus.EXPIRED);

		boolean result = bookingValidator.canBookingBeCancelled(booking);

		assertThat(result).isFalse();
	}

	@Test
	void canBookingBeCancelled_WhenCancelled_ShouldReturnFalse() {
		Booking booking = new Booking();
		booking.setStatus(BookingStatus.CANCELLED);

		boolean result = bookingValidator.canBookingBeCancelled(booking);

		assertThat(result).isFalse();
	}
}