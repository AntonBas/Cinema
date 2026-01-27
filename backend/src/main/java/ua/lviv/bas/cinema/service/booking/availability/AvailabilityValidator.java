package ua.lviv.bas.cinema.service.booking.availability;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.repository.BookedSeatRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;

@Service
@RequiredArgsConstructor
public class AvailabilityValidator {
	private final BookedSeatRepository bookedSeatRepository;
	private final SeatRepository seatRepository;

	public void validateSeat(Long sessionId, Long seatId) {
		if (isSeatBooked(sessionId, seatId)) {
			throw SeatNotAvailableException.forSeatAndSession(seatId, sessionId);
		}

		Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new SeatNotFoundException(seatId));

		if (!seat.isActive()) {
			throw SeatNotAvailableException.seatInactive(seatId);
		}
	}

	public boolean isSeatAvailable(Long sessionId, Long seatId) {
		return !isSeatBooked(sessionId, seatId);
	}

	private boolean isSeatBooked(Long sessionId, Long seatId) {
		return bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(sessionId, seatId,
				List.of(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED));
	}
}