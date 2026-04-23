package ua.lviv.bas.cinema.service.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;
import ua.lviv.bas.cinema.exception.domain.booking.SeatNotAvailableException;
import ua.lviv.bas.cinema.exception.domain.cinema.SessionNotFoundException;
import ua.lviv.bas.cinema.exception.domain.hall.SeatNotFoundException;
import ua.lviv.bas.cinema.mapper.booking.SeatReservationMapper;
import ua.lviv.bas.cinema.repository.booking.SeatReservationRepository;
import ua.lviv.bas.cinema.repository.cinema.SeatRepository;
import ua.lviv.bas.cinema.repository.cinema.SessionRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketTypeRepository;
import ua.lviv.bas.cinema.service.shared.PriceCalculatorService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Value("${booking.temp-hold-minutes:5}")
    private int tempHoldMinutes;

    @Cacheable(value = "seatAvailability", key = "#sessionId")
    public SeatReservationResponse getAvailability(Long sessionId) {
        log.debug("Fetching seat availability for session: {}", sessionId);

        var session = sessionRepository.findById(sessionId).orElseThrow(() -> new SessionNotFoundException(sessionId));

        var allSeats = seatRepository.findByHallId(session.getHall().getId());
        var activeTicketTypes = ticketTypeRepository.findByActiveTrue();

        var bookedSeatData = seatReservationRepository.findBookedSeatIds(session.getHall().getId(), sessionId,
                ReservationStatus.ACTIVE_STATUSES);

        Map<Long, ReservationStatus> seatStatusMap = bookedSeatData.stream().filter(data -> data[1] != null)
                .collect(Collectors.toMap(data -> (Long) data[0], data -> (ReservationStatus) data[1],
                        (existing, replacement) -> existing));

        var seatInfos = allSeats.stream().map(seat -> buildSeatInfo(seat, seatStatusMap, session, activeTicketTypes))
                .toList();

        int availableSeatsCount = (int) seatInfos.stream().filter(SeatReservationResponse.SeatInfo::available).count();

        return seatReservationMapper.toResponse(session, seatInfos, availableSeatsCount);
    }

    @CacheEvict(value = {"seatAvailability", "availableSeatsCount"}, key = "#sessionId")
    @Transactional
    public void hold(Long sessionId, Long seatId, User user) {
        log.info("Creating temporary hold for seat {} in session {} by user {}", seatId, sessionId, user.getId());

        var session = sessionRepository.findById(sessionId).orElseThrow(() -> new SessionNotFoundException(sessionId));

        var seat = seatRepository.findByIdWithLock(seatId).orElseThrow(() -> new SeatNotFoundException(seatId));

        validateSeat(sessionId, seatId);

        var reservation = SeatReservation.builder().seat(seat).session(session).ticketType(null).seatPrice(null)
                .status(ReservationStatus.PENDING).reservedUntil(LocalDateTime.now().plusMinutes(tempHoldMinutes))
                .reservedByUser(user).build();

        seatReservationRepository.save(reservation);
        log.info("Temporary hold created for seat {} in session {} by user {}", seatId, sessionId, user.getId());
    }

    @CacheEvict(value = {"seatAvailability", "availableSeatsCount"}, key = "#sessionId")
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

    public void validateAvailability(Long sessionId, Long seatId) {
        validateSeat(sessionId, seatId);
    }

    public boolean isAvailable(Long sessionId, Long seatId) {
        return !isReserved(sessionId, seatId);
    }

    @Cacheable(value = "availableSeatsCount", key = "#sessionId")
    public int getAvailableCount(Long sessionId) {
        var session = sessionRepository.findById(sessionId).orElseThrow(() -> new SessionNotFoundException(sessionId));

        long totalSeats = seatRepository.countByHallId(session.getHall().getId());
        long bookedSeats = seatReservationRepository.countBySessionIdAndStatusIn(sessionId,
                ReservationStatus.ACTIVE_STATUSES);

        return (int) (totalSeats - bookedSeats);
    }

    public SeatAvailabilityStatus getStatus(Long sessionId, Long seatId) {
        var statuses = seatReservationRepository.findStatusesBySessionIdAndSeatId(sessionId, seatId);

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

        return new SeatAvailabilityStatus(!isReserved, status);
    }

    private void validateSeat(Long sessionId, Long seatId) {
        var seat = seatRepository.findById(seatId).orElseThrow(() -> new SeatNotFoundException(seatId));

        if (!seat.isActive()) {
            throw SeatNotAvailableException.seatInactive(seatId);
        }

        if (isReserved(sessionId, seatId)) {
            throw SeatNotAvailableException.forSeatAndSession(seatId, sessionId);
        }
    }

    private boolean isReserved(Long sessionId, Long seatId) {
        return seatReservationRepository.existsBySessionIdAndSeatIdAndStatusIn(sessionId, seatId,
                ReservationStatus.ACTIVE_STATUSES);
    }

    private SeatReservationResponse.SeatInfo buildSeatInfo(Seat seat, Map<Long, ReservationStatus> seatStatusMap,
                                                           Session session, List<TicketType> activeTicketTypes) {

        var status = seatStatusMap.get(seat.getId());
        boolean isBooked = status != null;
        boolean temporarilyReserved = status == ReservationStatus.PENDING;
        boolean available = !isBooked && seat.isActive();

        var ticketPrices = activeTicketTypes.stream().map(ticketType -> {
            var price = priceCalculator.calculateSeatPrice(session, seat, ticketType);
            return seatReservationMapper.toTicketPriceInfo(ticketType, price);
        }).toList();

        return seatReservationMapper.toSeatInfo(seat, available, temporarilyReserved, ticketPrices);
    }

    public record SeatAvailabilityStatus(boolean available, ReservationStatus status) {
        public boolean isTemporarilyReserved() {
            return status == ReservationStatus.PENDING;
        }

        public boolean isConfirmed() {
            return status == ReservationStatus.CONFIRMED;
        }
    }
}