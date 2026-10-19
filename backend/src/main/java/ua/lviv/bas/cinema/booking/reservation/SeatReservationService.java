package ua.lviv.bas.cinema.booking.reservation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.cinema.hall.Seat;
import ua.lviv.bas.cinema.cinema.Session;
import ua.lviv.bas.cinema.ticket.type.TicketType;
import ua.lviv.bas.cinema.user.User;
import ua.lviv.bas.cinema.booking.reservation.dto.SeatReservationResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.booking.exception.SeatNotAvailableException;
import ua.lviv.bas.cinema.cinema.hall.SeatRepository;
import ua.lviv.bas.cinema.cinema.SessionRepository;
import ua.lviv.bas.cinema.ticket.type.TicketTypeRepository;
import ua.lviv.bas.cinema.booking.PriceCalculatorService;
import ua.lviv.bas.cinema.cinema.CinemaSessionStatus;
import ua.lviv.bas.cinema.booking.exception.BookingValidationException;
import ua.lviv.bas.cinema.common.CinemaTime;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final CacheManager cacheManager;

    @Value("${booking.temp-hold-minutes:5}")
    private int tempHoldMinutes;

    @Cacheable(value = "sessionIds", key = "#publicId")
    public Long resolveSessionId(UUID publicId) {
        return sessionRepository.findByPublicId(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Session", publicId)).getId();
    }

    public SeatReservationResponse getAvailability(Long sessionId) {
        var availability = loadCachedAvailability(sessionId);
        return availability.sessionTime().isAfter(CinemaTime.now()) ? availability : closedForBooking(availability);
    }

    private SeatReservationResponse loadCachedAvailability(Long sessionId) {
        Cache cache = cacheManager.getCache("seatAvailability");
        if (cache == null) {
            return buildAvailability(sessionId);
        }
        return cache.get(sessionId, () -> buildAvailability(sessionId));
    }

    private SeatReservationResponse buildAvailability(Long sessionId) {
        log.debug("Fetching seat availability for session: {}", sessionId);

        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session", sessionId));

        var allSeats = seatRepository.findByHallId(session.getHall().getId());
        var activeTicketTypes = ticketTypeRepository.findByActiveTrue();

        var bookedSeatData = seatReservationRepository.findBookedSeatStatuses(session.getHall().getId(), sessionId,
                ReservationStatus.ACTIVE_STATUSES);

        Map<Long, ReservationStatus> seatStatusMap = new HashMap<>();
        bookedSeatData.forEach(data -> seatStatusMap.put(data.seatId(), data.status()));

        boolean sessionOpen = session.getStatus() == CinemaSessionStatus.SCHEDULED;
        var seatInfos = allSeats.stream()
                .map(seat -> buildSeatInfo(seat, seatStatusMap, session, activeTicketTypes, sessionOpen))
                .toList();

        int availableSeatsCount = (int) seatInfos.stream().filter(SeatReservationResponse.SeatInfo::available).count();

        return seatReservationMapper.toResponse(session, seatInfos, availableSeatsCount);
    }

    private SeatReservationResponse closedForBooking(SeatReservationResponse availability) {
        var seats = availability.seats().stream()
                .map(seat -> new SeatReservationResponse.SeatInfo(seat.id(), seat.row(), seat.seatNumber(),
                        seat.seatType(), seat.x(), seat.y(), false, seat.temporarilyReserved(), seat.active(),
                        seat.ticketPrices()))
                .toList();
        return new SeatReservationResponse(availability.sessionId(), availability.movieTitle(),
                availability.basePrice(), availability.hallName(), availability.sessionTime(), 0, seats);
    }

    @Transactional
    public Seat lockSeat(Long seatId) {
        return seatRepository.findByIdWithLock(seatId).orElseThrow(() -> new EntityNotFoundException("Seat", seatId));
    }

    @CacheEvict(value = "seatAvailability", key = "#sessionId")
    @Transactional
    public SeatReservation hold(Long sessionId, Long seatId, User user) {
        var session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session", sessionId));
        validateSessionOpenForHolds(session);
        var seat = lockSeat(seatId);

        return holdLockedSeat(session, seat, user);
    }

    private void validateSessionOpenForHolds(Session session) {
        if (session.getStatus() != CinemaSessionStatus.SCHEDULED) {
            throw BookingValidationException.sessionNotAvailable();
        }
        if (session.getStartTime().isBefore(CinemaTime.now())) {
            throw BookingValidationException.sessionAlreadyStarted();
        }
    }

    @Transactional
    public SeatReservation holdLockedSeat(Session session, Seat seat, User user) {
        log.info("Creating temporary hold for seat {} in session {} by user {}", seat.getId(), session.getId(),
                user.getId());

        var saved = seatReservationRepository.save(newHold(session, seat, user));
        log.info("Temporary hold created for seat {} in session {} by user {}", seat.getId(), session.getId(),
                user.getId());
        return saved;
    }

    public SeatReservation newHold(Session session, Seat seat, User user) {
        validateSeat(seat, session);
        return SeatReservation.builder().seat(seat).session(session).ticketType(null).seatPrice(null)
                .status(ReservationStatus.PENDING).reservedUntil(Instant.now().plus(Duration.ofMinutes(tempHoldMinutes)))
                .reservedByUser(user).build();
    }

    @CacheEvict(value = "seatAvailability", key = "#sessionId")
    @Transactional
    public void cancel(Long sessionId, Long seatId, User user) {
        log.info("Cancelling temporary hold for seat {} in session {} by user {}", seatId, sessionId, user.getId());

        var reservation = seatReservationRepository
                .findBySessionIdAndSeatIdAndStatusAndReservedByUserId(sessionId, seatId, ReservationStatus.PENDING,
                        user.getId())
                .orElseThrow(() -> new SeatNotAvailableException("No active hold found for seat " + seatId));

        seatReservationRepository.delete(reservation);
        log.info("Temporary hold cancelled for seat {} in session {} by user {}", seatId, sessionId, user.getId());
    }

    @Transactional
    public void releaseReservations(List<SeatReservation> seatReservations, Long sessionId) {
        seatReservations.forEach(sr -> {
            sr.setStatus(ReservationStatus.EXPIRED);
            sr.setBooking(null);
        });
        seatReservationRepository.saveAll(seatReservations);
        evictAvailabilityCache(sessionId);
    }

    public void evictAvailabilityCache(Long sessionId) {
        Cache cache = cacheManager.getCache("seatAvailability");
        if (cache != null) {
            cache.evict(sessionId);
        }
    }

    public Map<Long, Integer> getAvailableSeatsBatch(List<Long> sessionIds) {
        if (sessionIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> totalSeatsMap = seatReservationRepository.findTotalSeatsBySessionIds(sessionIds)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Number) arr[1]).intValue()));

        Map<Long, Integer> bookedMap = seatReservationRepository
                .findBookedCountBySessionIds(sessionIds, ReservationStatus.ACTIVE_STATUSES)
                .stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0],
                        arr -> ((Number) arr[1]).intValue()));

        return sessionIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> totalSeatsMap.getOrDefault(id, 0) - bookedMap.getOrDefault(id, 0)));
    }

    private void validateSeat(Seat seat, Session session) {
        if (!seat.isActive()) {
            throw SeatNotAvailableException.seatInactive(seat.getId());
        }

        if (!seat.getHall().getId().equals(session.getHall().getId())) {
            throw SeatNotAvailableException.seatNotInSessionHall(seat.getId(), session.getId());
        }

        seatReservationRepository.deleteExpiredHolds(session.getId(), seat.getId());

        if (isReserved(session.getId(), seat.getId())) {
            throw SeatNotAvailableException.forSeatAndSession(seat.getId(), session.getId());
        }
    }

    private boolean isReserved(Long sessionId, Long seatId) {
        return seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(sessionId, seatId,
                ReservationStatus.ACTIVE_STATUSES);
    }

    private SeatReservationResponse.SeatInfo buildSeatInfo(Seat seat, Map<Long, ReservationStatus> seatStatusMap,
                                                           Session session, List<TicketType> activeTicketTypes,
                                                           boolean sessionOpen) {

        var status = seatStatusMap.get(seat.getId());
        boolean isBooked = status != null;
        boolean temporarilyReserved = status == ReservationStatus.PENDING;
        boolean available = sessionOpen && !isBooked && seat.isActive();

        var ticketPrices = activeTicketTypes.stream().map(ticketType -> {
            var price = priceCalculator.calculateSeatPrice(session, seat, ticketType);
            return seatReservationMapper.toTicketPriceInfo(ticketType, price);
        }).toList();

        return seatReservationMapper.toSeatInfo(seat, available, temporarilyReserved, ticketPrices);
    }

}