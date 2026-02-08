package ua.lviv.bas.cinema.service.booking.availability;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.dto.cinemaHall.response.SeatReservationResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.service.shared.PriceCalculatorService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatReservationService {
	private final SessionRepository sessionRepository;
	private final SeatRepository seatRepository;
	private final SeatReservationRepository bookedSeatRepository;
	private final TicketTypeRepository ticketTypeRepository;
	private final PriceCalculatorService priceCalculator;
	private final AvailabilityValidator availabilityValidator;

	public SeatReservationResponse getSeatAvailability(Long sessionId) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		List<Seat> allSeats = seatRepository.findByHallId(session.getHall().getId());
		List<SeatReservation> bookedSeats = bookedSeatRepository.findBySessionIdAndStatusIn(sessionId,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

		List<SeatReservationResponse.SeatInfo> seatInfos = allSeats.stream()
				.map(seat -> buildSeatInfo(seat, bookedSeats, session)).collect(Collectors.toList());

		int availableSeatsCount = (int) seatInfos.stream().filter(SeatReservationResponse.SeatInfo::getAvailable)
				.count();

		return SeatReservationResponse.builder().sessionId(session.getId()).movieTitle(session.getMovie().getTitle())
				.basePrice(session.getBasePrice()).hallName(session.getHall().getName())
				.availableSeats(availableSeatsCount).seats(seatInfos).build();
	}

	public void validateSeatAvailability(Long sessionId, Long seatId) {
		availabilityValidator.validateSeat(sessionId, seatId);
	}

	public boolean isSeatAvailableForSession(Long sessionId, Long seatId) {
		return availabilityValidator.isSeatAvailable(sessionId, seatId);
	}

	public int getAvailableSeatsCount(Long sessionId) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		Long hallId = session.getHall().getId();
		long totalSeats = seatRepository.countByHallId(hallId);
		long bookedSeats = bookedSeatRepository.countBySessionIdAndStatusIn(sessionId,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

		return (int) (totalSeats - bookedSeats);
	}

	private SeatReservationResponse.SeatInfo buildSeatInfo(Seat seat, List<SeatReservation> bookedSeats, Session session) {
		boolean isBooked = bookedSeats.stream().anyMatch(bs -> bs.getSeat().getId().equals(seat.getId()));
		boolean isTemporary = bookedSeats.stream().anyMatch(
				bs -> bs.getSeat().getId().equals(seat.getId()) && bs.getStatus() == ReservationStatus.PENDING);

		List<SeatReservationResponse.TicketPriceInfo> ticketPrices = calculateTicketPrices(seat, session);

		return SeatReservationResponse.SeatInfo.builder().id(seat.getId()).row(seat.getRow())
				.seatNumber(seat.getNumber()).seatType(seat.getSeatType().name())
				.available(!isBooked && seat.isActive()).temporarilyReserved(isTemporary).active(seat.isActive())
				.ticketPrices(ticketPrices).build();
	}

	private List<SeatReservationResponse.TicketPriceInfo> calculateTicketPrices(Seat seat, Session session) {
		List<TicketType> activeTicketTypes = ticketTypeRepository.findByActiveTrue();

		return activeTicketTypes.stream().map(ticketType -> {
			BigDecimal price = priceCalculator.calculateSeatPrice(session, seat, ticketType);
			return SeatReservationResponse.TicketPriceInfo.builder().ticketTypeId(ticketType.getId())
					.ticketTypeName(ticketType.getDisplayName()).finalPrice(price).build();
		}).collect(Collectors.toList());
	}
}