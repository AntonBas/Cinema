package ua.lviv.bas.cinema.booking;

import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ua.lviv.bas.cinema.audit.AuditAction;
import ua.lviv.bas.cinema.audit.AuditService;
import ua.lviv.bas.cinema.bonus.BonusLedgerService;
import ua.lviv.bas.cinema.booking.dto.BookingCreateRequest;
import ua.lviv.bas.cinema.booking.dto.BookingResponse;
import ua.lviv.bas.cinema.booking.reservation.ReservationStatus;
import ua.lviv.bas.cinema.booking.reservation.SeatReservationService;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.booking.exception.BookingConcurrentModificationException;
import ua.lviv.bas.cinema.booking.exception.BookingOperationException;
import ua.lviv.bas.cinema.booking.exception.BookingValidationException;
import ua.lviv.bas.cinema.user.User;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final BonusLedgerService bonusLedgerService;
    private final BookingCreationService bookingCreationService;
    private final SeatReservationService seatReservationService;
    private final AuditService auditService;
    private final TransactionTemplate transactionTemplate;

    public BookingService(BookingRepository bookingRepository, BookingMapper bookingMapper,
            BonusLedgerService bonusLedgerService, BookingCreationService bookingCreationService,
            SeatReservationService seatReservationService, AuditService auditService,
            PlatformTransactionManager transactionManager) {
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.bonusLedgerService = bonusLedgerService;
        this.bookingCreationService = bookingCreationService;
        this.seatReservationService = seatReservationService;
        this.auditService = auditService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public BookingResponse createBooking(BookingCreateRequest request, User user) {
        var created = bookingCreationService.createAndPersist(request, user);
        var bookingId = created.getId();

        if (created.getBonusPointsUsed() != null && created.getBonusPointsUsed() > 0) {
            try {
                transactionTemplate.executeWithoutResult(status -> bonusLedgerService.spendPoints(user.getId(),
                        created.getBonusPointsUsed(), findBooking(bookingId)));
            } catch (RuntimeException e) {
                log.warn("Bonus spend failed for booking {}, cancelling the booking instead of leaving it "
                        + "with an unpaid discount", bookingId, e);
                cancelAfterBonusSpendFailure(bookingId);
                throw e;
            }
        }

        return transactionTemplate.execute(status -> {
            var saved = bookingRepository.findWithDetailsById(bookingId)
                    .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
            log.info("Created booking {} for user {} with {} bonus points used", saved.getId(), user.getId(),
                    saved.getBonusPointsUsed());
            auditCreate(saved, user);
            seatReservationService.evictAvailabilityCache(saved.getSession().getId());
            return bookingMapper.toResponse(saved);
        });
    }

    private Booking findBooking(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
    }

    private void cancelAfterBonusSpendFailure(Long bookingId) {
        transactionTemplate.executeWithoutResult(status -> {
            var booking = findBooking(bookingId);
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setBonusPointsUsed(0);
            booking.setBonusDiscountAmount(BigDecimal.ZERO);
            booking.setFinalPrice(booking.getTotalPrice());
            seatReservationService.releaseReservations(booking.getSeatReservations(), booking.getSession().getId());
            bookingRepository.saveAndFlush(booking);
        });
    }

    @Transactional(readOnly = true)
    public BookingResponse getBooking(UUID publicId, User user) {
        var booking = bookingRepository.findByPublicIdAndUserId(publicId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", publicId));
        return bookingMapper.toResponse(booking);
    }

    public void cancelBooking(UUID publicId, User user) {
        var booking = bookingRepository.findByPublicIdAndUserId(publicId, user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", publicId));
        var bookingId = booking.getId();

        if (!canCancel(booking)) {
            throw BookingValidationException.cannotCancel();
        }

        var oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);

        seatReservationService.releaseReservations(booking.getSeatReservations(), booking.getSession().getId());

        try {
            bookingRepository.saveAndFlush(booking);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new BookingConcurrentModificationException(bookingId);
        }

        if (booking.getBonusPointsUsed() != null && booking.getBonusPointsUsed() > 0) {
            bonusLedgerService.refundPoints(booking);
        }

        log.info("Cancelled booking {} for user {}", bookingId, user.getId());
        auditCancel(bookingId, oldStatus);
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
        seatReservationService.evictAvailabilityCache(booking.getSession().getId());
        auditConfirm(bookingId, oldStatus);
    }

    public void markRefunded(Long bookingId) {
        var booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Booking", bookingId));
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            log.debug("Booking {} is {}, not marking as refunded", bookingId, booking.getStatus());
            return;
        }

        var oldStatus = booking.getStatus();
        booking.setStatus(BookingStatus.REFUNDED);
        bookingRepository.save(booking);
        log.info("Booking {} marked as refunded after all its tickets were refunded", bookingId);
        auditRefunded(bookingId, oldStatus);
    }

    private boolean canCancel(Booking booking) {
        return booking.getStatus() == BookingStatus.PENDING;
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

    private void auditRefunded(Long bookingId, BookingStatus oldStatus) {
        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("status", oldStatus);
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("status", BookingStatus.REFUNDED);
        auditService.logChange("Booking", bookingId, "Booking #" + bookingId, AuditAction.REFUND, oldDetails,
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