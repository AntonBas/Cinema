package ua.lviv.bas.cinema.booking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.booking.dto.request.BookingCreateRequest;
import ua.lviv.bas.cinema.booking.dto.response.BookingResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingConcurrentModificationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingOperationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.booking.mapper.BookingMapper;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.bonus.service.BonusLedgerService;
import ua.lviv.bas.cinema.audit.service.AuditService;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatReservationRepository seatReservationRepository;
    private final BookingMapper bookingMapper;
    private final BonusLedgerService bonusLedgerService;
    private final BookingCreationService bookingCreationService;
    private final AuditService auditService;
    private final CacheManager cacheManager;
    private final TransactionTemplate transactionTemplate;

    public BookingService(BookingRepository bookingRepository, SeatReservationRepository seatReservationRepository,
            BookingMapper bookingMapper, BonusLedgerService bonusLedgerService,
            BookingCreationService bookingCreationService, AuditService auditService, CacheManager cacheManager,
            PlatformTransactionManager transactionManager) {
        this.bookingRepository = bookingRepository;
        this.seatReservationRepository = seatReservationRepository;
        this.bookingMapper = bookingMapper;
        this.bonusLedgerService = bonusLedgerService;
        this.bookingCreationService = bookingCreationService;
        this.auditService = auditService;
        this.cacheManager = cacheManager;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Caching(evict = {
            @CacheEvict(value = "seatAvailability", key = "#request.sessionId()"),
            @CacheEvict(value = "sessions", allEntries = true)
    })
    public BookingResponse createBooking(BookingCreateRequest request, User user) {
        var created = bookingCreationService.createAndPersist(request, user);
        var saved = bookingRepository.findById(created.getId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", created.getId()));

        if (saved.getBonusPointsUsed() != null && saved.getBonusPointsUsed() > 0) {
            try {
                bonusLedgerService.spendPoints(user.getId(), saved.getBonusPointsUsed(), saved);
            } catch (RuntimeException e) {
                log.warn("Bonus spend failed for booking {}, cancelling the booking instead of leaving it "
                        + "with an unpaid discount", saved.getId(), e);
                cancelAfterBonusSpendFailure(saved.getId());
                throw e;
            }
        }

        log.info("Created booking {} for user {} with {} bonus points used", saved.getId(), user.getId(),
                saved.getBonusPointsUsed());
        auditCreate(saved, user);

        return bookingMapper.toResponse(saved);
    }

    private void cancelAfterBonusSpendFailure(Long bookingId) {
        var sessionId = transactionTemplate.execute(status -> {
            var booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setBonusPointsUsed(0);
            booking.setBonusDiscountAmount(BigDecimal.ZERO);
            booking.setFinalPrice(booking.getTotalPrice());
            booking.getSeatReservations().forEach(sr -> {
                sr.setStatus(ReservationStatus.EXPIRED);
                sr.setBooking(null);
            });
            seatReservationRepository.saveAll(booking.getSeatReservations());
            bookingRepository.saveAndFlush(booking);
            return booking.getSession().getId();
        });
        evictSeatAvailabilityCache(sessionId);
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(Long bookingId, User user) {
        var booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
        return bookingMapper.toResponse(booking);
    }

    public void cancelBooking(Long bookingId, User user) {
        var booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));

        if (!canCancel(booking)) {
            throw BookingValidationException.cannotCancel();
        }

        var oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);

        booking.getSeatReservations().forEach(sr -> {
            sr.setStatus(ReservationStatus.EXPIRED);
            sr.setBooking(null);
        });

        seatReservationRepository.saveAll(booking.getSeatReservations());

        try {
            bookingRepository.saveAndFlush(booking);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BookingConcurrentModificationException(bookingId);
        }

        if (booking.getBonusPointsUsed() != null && booking.getBonusPointsUsed() > 0) {
            bonusLedgerService.refundPoints(booking);
        }

        evictSeatAvailabilityCache(booking.getSession().getId());
        log.info("Cancelled booking {} for user {}", bookingId, user.getId());
        auditCancel(bookingId, oldStatus);
    }

    private void evictSeatAvailabilityCache(Long sessionId) {
        Cache cache = cacheManager.getCache("seatAvailability");
        if (cache != null) {
            cache.evict(sessionId);
        }
    }

    public void confirmBooking(Long bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            log.debug("Booking {} already confirmed, skipping", bookingId);
            return;
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw BookingOperationException.onlyPendingCanBeConfirmed();
        }

        var oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.getSeatReservations().forEach(sr -> sr.setStatus(ReservationStatus.CONFIRMED));

        try {
            bookingRepository.saveAndFlush(booking);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BookingConcurrentModificationException(bookingId);
        }
        evictSeatAvailabilityCache(booking.getSession().getId());
        auditConfirm(bookingId, oldStatus);
    }

    private boolean canCancel(Booking booking) {
        return booking.getStatus() == BookingStatus.PENDING || booking.getStatus() == BookingStatus.CONFIRMED;
    }

    private void auditCreate(Booking booking, User user) {
        Map<String, Object> details = new HashMap<>();
        details.put("userId", user.getId());
        details.put("sessionId", booking.getSession().getId());
        details.put("totalPrice", booking.getTotalPrice());
        details.put("finalPrice", booking.getFinalPrice());
        details.put("bonusPointsUsed", booking.getBonusPointsUsed());
        auditService.logChange("Booking", booking.getId(), "Booking #" + booking.getId(), AuditAction.CREATED, null,
                details);
    }

    private void auditCancel(Long bookingId, BookingStatus oldStatus) {
        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("status", oldStatus);
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("status", BookingStatus.CANCELLED);
        auditService.logChange("Booking", bookingId, "Booking #" + bookingId, AuditAction.CANCELLED, oldDetails,
                newDetails);
    }

    private void auditConfirm(Long bookingId, BookingStatus oldStatus) {
        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("status", oldStatus);
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("status", BookingStatus.CONFIRMED);
        auditService.logChange("Booking", bookingId, "Booking #" + bookingId, AuditAction.CONFIRMED, oldDetails,
                newDetails);
    }
}