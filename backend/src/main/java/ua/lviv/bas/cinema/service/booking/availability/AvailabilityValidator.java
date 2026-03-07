package ua.lviv.bas.cinema.service.booking.availability;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;

@Service
@RequiredArgsConstructor
public class AvailabilityValidator {

	private final SeatReservationRepository seatReservationRepository;
	private final SeatRepository seatRepository;

	public void validateSeat(Long sessionId, Long seatId) {
		Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new SeatNotFoundException(seatId));

		if (!seat.isActive()) {
			throw SeatNotAvailableException.seatInactive(seatId);
		}

		if (isSeatReserved(sessionId, seatId)) {
			throw SeatNotAvailableException.forSeatAndSession(seatId, sessionId);
		}
	}

	public boolean isSeatAvailable(Long sessionId, Long seatId) {
		return !isSeatReserved(sessionId, seatId);
	}

	public SeatAvailabilityCheck getSeatAvailabilityStatus(Long sessionId, Long seatId) {
		boolean isReserved = seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(sessionId, seatId,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

		ReservationStatus status = null;
		if (isReserved) {
			status = seatReservationRepository.findStatusBySessionIdAndSeatId(sessionId, seatId).orElse(null);
		}

		return new SeatAvailabilityCheck(!isReserved, status);
	}

	private boolean isSeatReserved(Long sessionId, Long seatId) {
		return seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(sessionId, seatId,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));
	}

	public record SeatAvailabilityCheck(boolean available, ReservationStatus status) {
		public boolean isTemporarilyReserved() {
			return status == ReservationStatus.PENDING;
		}

		public boolean isConfirmed() {
			return status == ReservationStatus.CONFIRMED;
		}
	}
}