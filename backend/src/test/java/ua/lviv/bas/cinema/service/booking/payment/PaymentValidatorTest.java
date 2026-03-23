package ua.lviv.bas.cinema.service.booking.payment;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.exception.domain.booking.SessionTooCloseException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentProcessingException;

public class PaymentValidatorTest {

	private PaymentValidator paymentValidator;

	@BeforeEach
	void setUp() {
		paymentValidator = new PaymentValidator();
		ReflectionTestUtils.setField(paymentValidator, "sessionTooCloseMinutes", 30);
	}

	@Test
	void validateBookingForPayment_Success() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(2));

		Seat seat = new Seat();
		seat.setRow(1);
		seat.setNumber(1);

		SeatReservation seatReservation = new SeatReservation();
		seatReservation.setSeat(seat);
		seatReservation.setStatus(ReservationStatus.PENDING);

		Booking booking = new Booking();
		booking.setStatus(BookingStatus.PENDING);
		booking.setExpiresAt(LocalDateTime.now().plusHours(1));
		booking.setSession(session);
		booking.setSeatReservations(Arrays.asList(seatReservation));

		assertDoesNotThrow(() -> paymentValidator.validateBookingForPayment(booking));
	}

	@Test
	void validateBookingForPayment_WhenBookingNotPending_ShouldThrowException() {
		Booking booking = new Booking();
		booking.setStatus(BookingStatus.CONFIRMED);

		assertThatThrownBy(() -> paymentValidator.validateBookingForPayment(booking))
				.isInstanceOf(PaymentProcessingException.class)
				.hasMessageContaining("Booking is not in PENDING status");
	}

	@Test
	void validateBookingForPayment_WhenBookingExpired_ShouldThrowException() {
		Booking booking = new Booking();
		booking.setStatus(BookingStatus.PENDING);
		booking.setExpiresAt(LocalDateTime.now().minusMinutes(10));

		assertThatThrownBy(() -> paymentValidator.validateBookingForPayment(booking))
				.isInstanceOf(PaymentProcessingException.class).hasMessageContaining("Booking has expired");
	}

	@Test
	void validateBookingForPayment_WhenSessionTooClose_ShouldThrowException() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusMinutes(20));

		Booking booking = new Booking();
		booking.setStatus(BookingStatus.PENDING);
		booking.setExpiresAt(LocalDateTime.now().plusHours(1));
		booking.setSession(session);

		assertThatThrownBy(() -> paymentValidator.validateBookingForPayment(booking))
				.isInstanceOf(SessionTooCloseException.class);
	}

	@Test
	void validateBookingForPayment_WhenSeatsNoLongerAvailable_ShouldThrowException() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(2));

		Seat seat = new Seat();
		seat.setRow(1);
		seat.setNumber(1);

		SeatReservation seatReservation = new SeatReservation();
		seatReservation.setSeat(seat);
		seatReservation.setStatus(ReservationStatus.CONFIRMED);

		Booking booking = new Booking();
		booking.setStatus(BookingStatus.PENDING);
		booking.setExpiresAt(LocalDateTime.now().plusHours(1));
		booking.setSession(session);
		booking.setSeatReservations(Arrays.asList(seatReservation));

		assertThatThrownBy(() -> paymentValidator.validateBookingForPayment(booking))
				.isInstanceOf(PaymentProcessingException.class)
				.hasMessageContaining("Some seats are no longer available");
	}

	@Test
	void validateBookingForPayment_WithMultipleSeatsSomeUnavailable() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(2));

		Seat seat1 = new Seat();
		seat1.setRow(1);
		seat1.setNumber(1);

		Seat seat2 = new Seat();
		seat2.setRow(1);
		seat2.setNumber(2);

		SeatReservation seatReservation1 = new SeatReservation();
		seatReservation1.setSeat(seat1);
		seatReservation1.setStatus(ReservationStatus.PENDING);

		SeatReservation seatReservation2 = new SeatReservation();
		seatReservation2.setSeat(seat2);
		seatReservation2.setStatus(ReservationStatus.CONFIRMED);

		Booking booking = new Booking();
		booking.setStatus(BookingStatus.PENDING);
		booking.setExpiresAt(LocalDateTime.now().plusHours(1));
		booking.setSession(session);
		booking.setSeatReservations(Arrays.asList(seatReservation1, seatReservation2));

		assertThatThrownBy(() -> paymentValidator.validateBookingForPayment(booking))
				.isInstanceOf(PaymentProcessingException.class)
				.hasMessageContaining("Some seats are no longer available");
	}

	@Test
	void validateBookingForPayment_WithEmptySeatsList() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(2));

		Booking booking = new Booking();
		booking.setStatus(BookingStatus.PENDING);
		booking.setExpiresAt(LocalDateTime.now().plusHours(1));
		booking.setSession(session);
		booking.setSeatReservations(Collections.emptyList());

		assertDoesNotThrow(() -> paymentValidator.validateBookingForPayment(booking));
	}

	@Test
	void validateBookingForPayment_WithNullSeatsList() {
		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(2));

		Booking booking = new Booking();
		booking.setStatus(BookingStatus.PENDING);
		booking.setExpiresAt(LocalDateTime.now().plusHours(1));
		booking.setSession(session);
		booking.setSeatReservations(null);

		assertThatThrownBy(() -> paymentValidator.validateBookingForPayment(booking))
				.isInstanceOf(NullPointerException.class);
	}
}