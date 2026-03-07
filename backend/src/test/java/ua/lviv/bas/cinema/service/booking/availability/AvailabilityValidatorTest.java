package ua.lviv.bas.cinema.service.booking.availability;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;

@ExtendWith(MockitoExtension.class)
public class AvailabilityValidatorTest {

	@Mock
	private SeatReservationRepository seatReservationRepository;

	@Mock
	private SeatRepository seatRepository;

	@InjectMocks
	private AvailabilityValidator availabilityValidator;

	private static final Long SESSION_ID = 1L;
	private static final Long AVAILABLE_SEAT_ID = 2L;
	private static final Long BOOKED_SEAT_ID = 3L;
	private static final Long PENDING_SEAT_ID = 5L;
	private static final Long INACTIVE_SEAT_ID = 4L;
	private static final Long NON_EXISTENT_SEAT_ID = 999L;

	private Seat availableSeat;
	private Seat inactiveSeat;
	private Seat bookedSeat;

	@BeforeEach
	void setUp() {
		availableSeat = new Seat();
		availableSeat.setId(AVAILABLE_SEAT_ID);
		availableSeat.setActive(true);

		inactiveSeat = new Seat();
		inactiveSeat.setId(INACTIVE_SEAT_ID);
		inactiveSeat.setActive(false);

		bookedSeat = new Seat();
		bookedSeat.setId(BOOKED_SEAT_ID);
		bookedSeat.setActive(true);
	}

	@Test
	void validateSeat_WhenSeatAvailable_ShouldNotThrowException() {
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, AVAILABLE_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(false);
		when(seatRepository.findById(AVAILABLE_SEAT_ID)).thenReturn(Optional.of(availableSeat));

		availabilityValidator.validateSeat(SESSION_ID, AVAILABLE_SEAT_ID);
	}

	@Test
	void validateSeat_WhenSeatBooked_ShouldThrowSeatNotAvailableException() {
		when(seatRepository.findById(BOOKED_SEAT_ID)).thenReturn(Optional.of(bookedSeat));
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, BOOKED_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(true);

		assertThatThrownBy(() -> availabilityValidator.validateSeat(SESSION_ID, BOOKED_SEAT_ID))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void validateSeat_WhenSeatNotFound_ShouldThrowSeatNotFoundException() {
		when(seatRepository.findById(NON_EXISTENT_SEAT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> availabilityValidator.validateSeat(SESSION_ID, NON_EXISTENT_SEAT_ID))
				.isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void isSeatAvailable_WhenSeatNotBooked_ShouldReturnTrue() {
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, AVAILABLE_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(false);

		boolean result = availabilityValidator.isSeatAvailable(SESSION_ID, AVAILABLE_SEAT_ID);

		assertThat(result).isTrue();
	}

	@Test
	void isSeatAvailable_WhenSeatBooked_ShouldReturnFalse() {
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, BOOKED_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(true);

		boolean result = availabilityValidator.isSeatAvailable(SESSION_ID, BOOKED_SEAT_ID);

		assertThat(result).isFalse();
	}

	@Test
	void getSeatAvailabilityStatus_WhenSeatAvailable_ShouldReturnAvailableTrueAndStatusNull() {
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, AVAILABLE_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(false);

		AvailabilityValidator.SeatAvailabilityCheck result = availabilityValidator.getSeatAvailabilityStatus(SESSION_ID,
				AVAILABLE_SEAT_ID);

		assertThat(result.available()).isTrue();
		assertThat(result.status()).isNull();
		assertThat(result.isTemporarilyReserved()).isFalse();
		assertThat(result.isConfirmed()).isFalse();
	}

	@Test
	void getSeatAvailabilityStatus_WhenSeatConfirmed_ShouldReturnAvailableFalseAndStatusConfirmed() {
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, BOOKED_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(true);
		when(seatReservationRepository.findStatusBySessionIdAndSeatId(SESSION_ID, BOOKED_SEAT_ID))
				.thenReturn(Optional.of(ReservationStatus.CONFIRMED));

		AvailabilityValidator.SeatAvailabilityCheck result = availabilityValidator.getSeatAvailabilityStatus(SESSION_ID,
				BOOKED_SEAT_ID);

		assertThat(result.available()).isFalse();
		assertThat(result.status()).isEqualTo(ReservationStatus.CONFIRMED);
		assertThat(result.isTemporarilyReserved()).isFalse();
		assertThat(result.isConfirmed()).isTrue();
	}

	@Test
	void getSeatAvailabilityStatus_WhenSeatPending_ShouldReturnAvailableFalseAndStatusPending() {
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, PENDING_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(true);
		when(seatReservationRepository.findStatusBySessionIdAndSeatId(SESSION_ID, PENDING_SEAT_ID))
				.thenReturn(Optional.of(ReservationStatus.PENDING));

		AvailabilityValidator.SeatAvailabilityCheck result = availabilityValidator.getSeatAvailabilityStatus(SESSION_ID,
				PENDING_SEAT_ID);

		assertThat(result.available()).isFalse();
		assertThat(result.status()).isEqualTo(ReservationStatus.PENDING);
		assertThat(result.isTemporarilyReserved()).isTrue();
		assertThat(result.isConfirmed()).isFalse();
	}

	@Test
	void getSeatAvailabilityStatus_WhenSeatReservedButStatusNotFound_ShouldReturnAvailableFalseAndStatusNull() {
		when(seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, BOOKED_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(true);
		when(seatReservationRepository.findStatusBySessionIdAndSeatId(SESSION_ID, BOOKED_SEAT_ID))
				.thenReturn(Optional.empty());

		AvailabilityValidator.SeatAvailabilityCheck result = availabilityValidator.getSeatAvailabilityStatus(SESSION_ID,
				BOOKED_SEAT_ID);

		assertThat(result.available()).isFalse();
		assertThat(result.status()).isNull();
		assertThat(result.isTemporarilyReserved()).isFalse();
		assertThat(result.isConfirmed()).isFalse();
	}
}