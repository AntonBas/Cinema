package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatAvailabilityResponse;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.repository.BookedSeatRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatAvailabilityService {

	private final SessionRepository sessionRepository;
	private final SeatRepository seatRepository;
	private final BookedSeatRepository bookedSeatRepository;
	private final TicketTypeRepository ticketTypeRepository;

	public SeatAvailabilityResponse getSeatAvailability(Long sessionId) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

		List<Seat> allSeats = seatRepository.findByHallId(session.getHall().getId());
		List<BookedSeat> bookedSeats = bookedSeatRepository.findBySessionIdAndStatusIn(sessionId,
				List.of(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED));

		List<SeatAvailabilityResponse.SeatInfo> seatInfos = allSeats.stream().map(seat -> {
			boolean isAvailable = isSeatAvailable(seat.getId(), bookedSeats);
			BigDecimal price = calculateSeatPrice(session, seat, null);

			return SeatAvailabilityResponse.SeatInfo.builder().id(seat.getId()).row(seat.getRow())
					.seatNumber(seat.getNumber()).seatType(seat.getSeatType()).available(isAvailable)
					.active(seat.isActive()).price(price).build();
		}).collect(Collectors.toList());

		List<TicketType> allTicketTypes = ticketTypeRepository.findByActiveTrue();
		List<SeatAvailabilityResponse.TicketTypeInfo> ticketTypeInfos = allTicketTypes.stream()
				.map(ticketType -> SeatAvailabilityResponse.TicketTypeInfo.builder().id(ticketType.getId())
						.name(ticketType.getDisplayName()).multiplier(ticketType.getPriceMultiplier())
						.description(ticketType.getDescription()).build())
				.collect(Collectors.toList());

		SeatAvailabilityResponse.SessionInfo sessionInfo = SeatAvailabilityResponse.SessionInfo.builder()
				.id(session.getId()).movieTitle(session.getMovie().getTitle()).basePrice(session.getBasePrice())
				.hallName(session.getHall().getName()).totalSeats(allSeats.size())
				.availableSeats(
						(int) seatInfos.stream().filter(SeatAvailabilityResponse.SeatInfo::getAvailable).count())
				.build();

		return SeatAvailabilityResponse.builder().session(sessionInfo).seats(seatInfos).ticketTypes(ticketTypeInfos)
				.build();
	}

	public void validateSeatAvailability(Long sessionId, Long seatId) {
		boolean isSeatBooked = bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(sessionId, seatId,
				List.of(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED));

		if (isSeatBooked) {
			throw SeatNotAvailableException.forSeatAndSession(seatId, sessionId);
		}

		Seat seat = seatRepository.findById(seatId)
				.orElseThrow(() -> new IllegalArgumentException("Seat not found: " + seatId));

		if (!seat.isActive()) {
			throw SeatNotAvailableException.seatInactive(seatId);
		}
	}

	public BigDecimal calculateSeatPrice(Session session, Seat seat, TicketType ticketType) {
		BigDecimal basePrice = session.getBasePrice();
		BigDecimal seatMultiplier = seat.getSeatType().getPriceMultiplier();
		BigDecimal ticketMultiplier = ticketType != null ? ticketType.getPriceMultiplier() : BigDecimal.ONE;

		return basePrice.multiply(seatMultiplier).multiply(ticketMultiplier);
	}

	public boolean isSeatAvailableForSession(Long sessionId, Long seatId) {
		return !bookedSeatRepository.existsBySessionIdAndSeatIdAndStatusIn(sessionId, seatId,
				List.of(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED));
	}

	public int getAvailableSeatsCount(Long sessionId) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

		Long hallId = session.getHall().getId();
		long totalSeats = seatRepository.countByHallId(hallId);
		long bookedSeats = bookedSeatRepository.countBySessionIdAndStatusIn(sessionId,
				List.of(BookedSeatStatus.PENDING, BookedSeatStatus.CONFIRMED));

		return (int) (totalSeats - bookedSeats);
	}

	private boolean isSeatAvailable(Long seatId, List<BookedSeat> bookedSeats) {
		return bookedSeats.stream().noneMatch(bookedSeat -> bookedSeat.getSeat().getId().equals(seatId));
	}
}