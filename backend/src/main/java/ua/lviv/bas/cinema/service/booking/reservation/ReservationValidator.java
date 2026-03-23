package ua.lviv.bas.cinema.service.booking.reservation;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationValidator {

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
		List<ReservationStatus> statuses = seatReservationRepository.findStatusesBySessionIdAndSeatId(sessionId,
				seatId);

		boolean isReserved = !statuses.isEmpty();
		ReservationStatus status = null;

		if (isReserved) {
			if (statuses.contains(ReservationStatus.CONFIRMED)) {
				status = ReservationStatus.CONFIRMED;
			} else {
				status = ReservationStatus.PENDING;
			}
			if (statuses.size() > 1) {
				log.warn("Multiple reservations for seat {} in session {}: {}", seatId, sessionId, statuses);
			}
		}

		return new SeatAvailabilityCheck(!isReserved, status);
	}

	public void validateReservationNotExpired(SeatReservation reservation, Long seatId, Long sessionId) {
		if (reservation.getReservedUntil().isBefore(LocalDateTime.now())) {
			seatReservationRepository.delete(reservation);
			throw SeatNotAvailableException.forSeatAndSession(seatId, sessionId);
		}
	}

	private boolean isSeatReserved(Long sessionId, Long seatId) {
		return seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(sessionId, seatId,
				ReservationStatus.ACTIVE_STATUSES);
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