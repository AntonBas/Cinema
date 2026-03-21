package ua.lviv.bas.cinema.service.booking.reservation;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.mapper.SeatReservationMapper;
import ua.lviv.bas.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.service.shared.PriceCalculatorService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatReservationService {

	private final SessionRepository sessionRepository;
	private final SeatRepository seatRepository;
	private final SeatReservationRepository seatReservationRepository;
	private final TicketTypeRepository ticketTypeRepository;
	private final PriceCalculatorService priceCalculator;
	private final ReservationValidator availabilityValidator;
	private final SeatReservationMapper seatReservationMapper;

	@Value("${booking.temp-hold-minutes:5}")
	private int tempHoldMinutes;

	@Cacheable(value = "seatAvailability", key = "#sessionId")
	public SeatReservationResponse getSeatAvailability(Long sessionId) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		List<Seat> allSeats = seatRepository.findByHallId(session.getHall().getId());
		List<TicketType> activeTicketTypes = ticketTypeRepository.findByActiveTrue();

		List<ReservationStatus> statuses = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);
		List<Object[]> bookedSeatData = seatReservationRepository.findBookedSeatIds(session.getHall().getId(),
				sessionId, statuses);

		Map<Long, Boolean> bookedSeatMap = bookedSeatData.stream().collect(
				Collectors.toMap(data -> (Long) data[0], data -> (Boolean) data[1], (existing, replacement) -> {
					log.warn("Duplicate seat ID {} found with values {} and {}, using first value", existing, existing,
							replacement);
					return existing;
				}));

		List<SeatReservationResponse.SeatInfo> seatInfos = allSeats.stream()
				.map(seat -> buildSeatInfo(seat, bookedSeatMap, sessionId, session, activeTicketTypes))
				.collect(Collectors.toList());

		int availableSeatsCount = (int) seatInfos.stream().filter(SeatReservationResponse.SeatInfo::available).count();

		return seatReservationMapper.toResponse(session, seatInfos, availableSeatsCount);
	}

	@Transactional
	public void temporaryHoldSeat(Long sessionId, Long seatId, User user) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new SeatNotFoundException(seatId));

		availabilityValidator.validateSeat(sessionId, seatId);

		SeatReservation reservation = SeatReservation.builder().seat(seat).session(session).ticketType(null)
				.seatPrice(null).status(ReservationStatus.PENDING).reservedAt(LocalDateTime.now())
				.reservedUntil(LocalDateTime.now().plusMinutes(tempHoldMinutes)).reservedByUser(user).build();

		seatReservationRepository.save(reservation);

		log.info("Temporary hold for seat {} in session {} by user {}", seatId, sessionId, user.getId());
	}

	private SeatReservationResponse.SeatInfo buildSeatInfo(Seat seat, Map<Long, Boolean> bookedSeatMap, Long sessionId,
			Session session, List<TicketType> activeTicketTypes) {

		boolean isBooked = bookedSeatMap.getOrDefault(seat.getId(), false);
		ReservationValidator.SeatAvailabilityCheck check = availabilityValidator.getSeatAvailabilityStatus(sessionId,
				seat.getId());

		boolean isAvailable = !isBooked && !check.isTemporarilyReserved() && seat.isActive();

		List<SeatReservationResponse.TicketPriceInfo> ticketPrices = activeTicketTypes.stream().map(ticketType -> {
			BigDecimal price = priceCalculator.calculateSeatPrice(session, seat, ticketType);
			return seatReservationMapper.toTicketPriceInfo(ticketType, price);
		}).collect(Collectors.toList());

		return seatReservationMapper.toSeatInfo(seat, isAvailable, check.isTemporarilyReserved(), ticketPrices);
	}

	public void validateSeatAvailability(Long sessionId, Long seatId) {
		availabilityValidator.validateSeat(sessionId, seatId);
	}

	public boolean isSeatAvailableForSession(Long sessionId, Long seatId) {
		return availabilityValidator.isSeatAvailable(sessionId, seatId);
	}

	@Cacheable(value = "availableSeatsCount", key = "#sessionId")
	public int getAvailableSeatsCount(Long sessionId) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		Long hallId = session.getHall().getId();
		long totalSeats = seatRepository.countByHallId(hallId);
		long bookedSeats = seatReservationRepository.countBySessionIdAndStatusIn(sessionId,
				List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED));

		return (int) (totalSeats - bookedSeats);
	}
}