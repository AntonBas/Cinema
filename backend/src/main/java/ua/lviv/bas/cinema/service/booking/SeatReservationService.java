package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SeatNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.mapper.booking.SeatReservationMapper;
import ua.lviv.bas.cinema.repository.booking.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.cinema.SeatRepository;
import ua.lviv.bas.cinema.repository.cinema.SessionRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketTypeRepository;
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
	private final SeatReservationMapper seatReservationMapper;

	@Value("${booking.temp-hold-minutes:5}")
	private int tempHoldMinutes;

	@Cacheable(value = "seatAvailability", key = "#sessionId")
	public SeatReservationResponse getSeatAvailability(Long sessionId) {
		log.debug("Fetching seat availability for session: {}", sessionId);
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		List<Seat> allSeats = seatRepository.findByHallId(session.getHall().getId());
		List<TicketType> activeTicketTypes = ticketTypeRepository.findByActiveTrue();

		List<Object[]> bookedSeatData = seatReservationRepository.findBookedSeatIds(session.getHall().getId(),
				sessionId, ReservationStatus.ACTIVE_STATUSES);

		Map<Long, ReservationStatus> seatStatusMap = bookedSeatData.stream().filter(data -> data[1] != null)
				.collect(Collectors.toMap(data -> (Long) data[0], data -> (ReservationStatus) data[1],
						(existing, replacement) -> existing));

		List<SeatReservationResponse.SeatInfo> seatInfos = allSeats.stream()
				.map(seat -> buildSeatInfo(seat, seatStatusMap, session, activeTicketTypes))
				.collect(Collectors.toList());

		int availableSeatsCount = (int) seatInfos.stream().filter(SeatReservationResponse.SeatInfo::available).count();

		return seatReservationMapper.toResponse(session, seatInfos, availableSeatsCount);
	}

	@Transactional
	@Caching(evict = { @CacheEvict(value = "seatAvailability", key = "#sessionId"),
			@CacheEvict(value = "availableSeatsCount", key = "#sessionId") })
	public void temporaryHoldSeat(Long sessionId, Long seatId, User user) {
		log.info("Creating temporary hold for seat {} in session {} by user {}", seatId, sessionId, user.getId());

		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		Seat seat = seatRepository.findByIdWithLock(seatId).orElseThrow(() -> new SeatNotFoundException(seatId));

		validateSeat(sessionId, seatId);

		SeatReservation reservation = SeatReservation.builder().seat(seat).session(session).ticketType(null)
				.seatPrice(null).status(ReservationStatus.PENDING)
				.reservedUntil(LocalDateTime.now().plusMinutes(tempHoldMinutes)).reservedByUser(user).build();

		seatReservationRepository.save(reservation);

		log.info("Temporary hold created for seat {} in session {} by user {}", seatId, sessionId, user.getId());
	}

	@Transactional
	@Caching(evict = { @CacheEvict(value = "seatAvailability", key = "#sessionId"),
			@CacheEvict(value = "availableSeatsCount", key = "#sessionId") })
	public void cancelTemporaryHold(Long sessionId, Long seatId, User user) {
		log.info("Cancelling temporary hold for seat {} in session {} by user {}", seatId, sessionId, user.getId());

		SeatReservation reservation = seatReservationRepository
				.findBySessionIdAndSeatIdAndStatusAndReservedByUserId(sessionId, seatId, ReservationStatus.PENDING,
						user.getId())
				.orElseThrow(() -> new SeatNotAvailableException("No active hold found for seat " + seatId));

		seatReservationRepository.delete(reservation);
		log.info("Temporary hold cancelled for seat {} in session {} by user {}", seatId, sessionId, user.getId());
	}

	public void validateSeatAvailability(Long sessionId, Long seatId) {
		validateSeat(sessionId, seatId);
	}

	public boolean isSeatAvailableForSession(Long sessionId, Long seatId) {
		return !isSeatReserved(sessionId, seatId);
	}

	@Cacheable(value = "availableSeatsCount", key = "#sessionId")
	public int getAvailableSeatsCount(Long sessionId) {
		Session session = sessionRepository.findById(sessionId)
				.orElseThrow(() -> new SessionNotFoundException(sessionId));

		Long hallId = session.getHall().getId();
		long totalSeats = seatRepository.countByHallId(hallId);
		long bookedSeats = seatReservationRepository.countBySessionIdAndStatusIn(sessionId,
				ReservationStatus.ACTIVE_STATUSES);

		return (int) (totalSeats - bookedSeats);
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

	private void validateSeat(Long sessionId, Long seatId) {
		Seat seat = seatRepository.findById(seatId).orElseThrow(() -> new SeatNotFoundException(seatId));

		if (!seat.isActive()) {
			throw SeatNotAvailableException.seatInactive(seatId);
		}

		if (isSeatReserved(sessionId, seatId)) {
			throw SeatNotAvailableException.forSeatAndSession(seatId, sessionId);
		}
	}

	private boolean isSeatReserved(Long sessionId, Long seatId) {
		return seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(sessionId, seatId,
				ReservationStatus.ACTIVE_STATUSES);
	}

	private SeatReservationResponse.SeatInfo buildSeatInfo(Seat seat, Map<Long, ReservationStatus> seatStatusMap,
			Session session, List<TicketType> activeTicketTypes) {

		ReservationStatus status = seatStatusMap.get(seat.getId());
		boolean isBooked = status != null;
		boolean isTemporarilyReserved = status == ReservationStatus.PENDING;
		boolean isAvailable = !isBooked && seat.isActive();

		List<SeatReservationResponse.TicketPriceInfo> ticketPrices = activeTicketTypes.stream().map(ticketType -> {
			BigDecimal price = priceCalculator.calculateSeatPrice(session, seat, ticketType);
			return seatReservationMapper.toTicketPriceInfo(ticketType, price);
		}).collect(Collectors.toList());

		return seatReservationMapper.toSeatInfo(seat, isAvailable, isTemporarilyReserved, ticketPrices);
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