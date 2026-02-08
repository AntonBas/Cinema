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
import ua.lviv.bas.cinema.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;

@ExtendWith(MockitoExtension.class)
public class AvailabilityValidatorTest {

	@Mock
	private SeatReservationRepository bookedSeatRepository;

	@Mock
	private SeatRepository seatRepository;

	@InjectMocks
	private AvailabilityValidator availabilityValidator;

	private static final Long SESSION_ID = 1L;
	private static final Long AVAILABLE_SEAT_ID = 2L;
	private static final Long BOOKED_SEAT_ID = 3L;
	private static final Long INACTIVE_SEAT_ID = 4L;
	private static final Long NON_EXISTENT_SEAT_ID = 999L;

	private Seat availableSeat;
	private Seat inactiveSeat;

	@BeforeEach
	void setUp() {
		availableSeat = new Seat();
		availableSeat.setId(AVAILABLE_SEAT_ID);
		availableSeat.setActive(true);

		inactiveSeat = new Seat();
		inactiveSeat.setId(INACTIVE_SEAT_ID);
		inactiveSeat.setActive(false);
	}

	@Test
	void validateSeat_WhenSeatAvailable_ShouldNotThrowException() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, AVAILABLE_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(false);
		when(seatRepository.findById(AVAILABLE_SEAT_ID)).thenReturn(Optional.of(availableSeat));

		availabilityValidator.validateSeat(SESSION_ID, AVAILABLE_SEAT_ID);
	}

	@Test
	void validateSeat_WhenSeatBooked_ShouldThrowSeatNotAvailableException() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, BOOKED_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(true);

		assertThatThrownBy(() -> availabilityValidator.validateSeat(SESSION_ID, BOOKED_SEAT_ID))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void validateSeat_WhenSeatInactive_ShouldThrowSeatNotAvailableException() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, INACTIVE_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(false);
		when(seatRepository.findById(INACTIVE_SEAT_ID)).thenReturn(Optional.of(inactiveSeat));

		assertThatThrownBy(() -> availabilityValidator.validateSeat(SESSION_ID, INACTIVE_SEAT_ID))
				.isInstanceOf(SeatNotAvailableException.class);
	}

	@Test
	void validateSeat_WhenSeatNotFound_ShouldThrowSeatNotFoundException() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, NON_EXISTENT_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(false);
		when(seatRepository.findById(NON_EXISTENT_SEAT_ID)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> availabilityValidator.validateSeat(SESSION_ID, NON_EXISTENT_SEAT_ID))
				.isInstanceOf(SeatNotFoundException.class);
	}

	@Test
	void isSeatAvailable_WhenSeatNotBooked_ShouldReturnTrue() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, AVAILABLE_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(false);

		boolean result = availabilityValidator.isSeatAvailable(SESSION_ID, AVAILABLE_SEAT_ID);

		assertThat(result).isTrue();
	}

	@Test
	void isSeatAvailable_WhenSeatBooked_ShouldReturnFalse() {
		when(bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(SESSION_ID, BOOKED_SEAT_ID,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED))).thenReturn(true);

		boolean result = availabilityValidator.isSeatAvailable(SESSION_ID, BOOKED_SEAT_ID);

		assertThat(result).isFalse();
	}
}