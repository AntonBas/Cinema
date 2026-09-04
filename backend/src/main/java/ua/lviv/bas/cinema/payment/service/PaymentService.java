package ua.lviv.bas.cinema.payment.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.booking.domain.Booking;
import ua.lviv.bas.cinema.payment.domain.Payment;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;
import ua.lviv.bas.cinema.booking.domain.status.ReservationStatus;
import ua.lviv.bas.cinema.user.domain.User;
import ua.lviv.bas.cinema.payment.dto.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.payment.dto.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.SessionTooCloseException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.InvalidPaymentStatusException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.audit.service.AuditService;
import ua.lviv.bas.cinema.notification.EmailService;
import ua.lviv.bas.cinema.common.DateTimeFormatterService;
import ua.lviv.bas.cinema.common.NumberGeneratorService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class PaymentService {

    private static final List<PaymentStatus> ACTIVE_STATUSES = Arrays.stream(PaymentStatus.values())
            .filter(PaymentStatus::isActive).toList();

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final NumberGeneratorService numberGenerator;
    private final PaymentSuccessOrchestrator paymentSuccessOrchestrator;
    private final AuditService auditService;
    private final EmailService emailService;
    private final DateTimeFormatterService dateTimeFormatter;
    private final TransactionTemplate requiresNewTransactionTemplate;

    @Value("${booking.session-too-close-minutes:30}")
    private int sessionTooCloseMinutes;

    public PaymentService(PaymentRepository paymentRepository, BookingRepository bookingRepository,
            NumberGeneratorService numberGenerator, PaymentSuccessOrchestrator paymentSuccessOrchestrator,
            AuditService auditService, EmailService emailService, DateTimeFormatterService dateTimeFormatter,
            PlatformTransactionManager transactionManager) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
        this.numberGenerator = numberGenerator;
        this.paymentSuccessOrchestrator = paymentSuccessOrchestrator;
        this.auditService = auditService;
        this.emailService = emailService;
        this.dateTimeFormatter = dateTimeFormatter;
        this.requiresNewTransactionTemplate = new TransactionTemplate(transactionManager);
        this.requiresNewTransactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    public PaymentResponse createPayment(PaymentCreateRequest request, User user) {
        log.info("Creating payment for booking {} by user {}", request.bookingId(), user.getId());

        var booking = bookingRepository.findByIdAndUserId(request.bookingId(), user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Booking", request.bookingId()));

        validateBookingForPayment(booking);

        Optional<Payment> existingPayment = paymentRepository.findByBookingId(booking.getId());
        if (existingPayment.isPresent() && existingPayment.get().getStatus().isActive()) {
            log.info("Returning existing active payment {} for booking {}", existingPayment.get().getId(),
                    booking.getId());
            return buildPaymentResponse(existingPayment.get());
        }

        var payment = Payment.builder().booking(booking).amount(booking.getFinalPrice()).status(PaymentStatus.PENDING)
                .liqpayOrderId(numberGenerator.generateLiqpayOrderId()).build();

        var saved = paymentRepository.save(payment);
        log.info("Created payment {} for booking {}", saved.getId(), booking.getId());
        auditCreate(saved, booking);

        return buildPaymentResponse(saved);
    }

    @Transactional(readOnly = true)
    public PaymentResponse getPayment(Long paymentId, User user) {
        var payment = paymentRepository.findByIdWithDetails(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", paymentId));

        if (!payment.getBooking().getUser().getId().equals(user.getId())) {
            throw new PaymentAccessDeniedException(paymentId, user.getId());
        }

        return buildPaymentResponse(payment);
    }

    public PaymentResponse retryPayment(Long paymentId, User user) {
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", paymentId));

        if (!payment.getBooking().getUser().getId().equals(user.getId())) {
            throw new PaymentAccessDeniedException(paymentId, user.getId());
        }

        if (!payment.getStatus().canBeRetried()) {
            throw InvalidPaymentStatusException.notFailed(payment.getStatus());
        }

        validateBookingForPayment(payment.getBooking());

        payment.setStatus(PaymentStatus.PENDING);
        payment.setLiqpayOrderId(numberGenerator.generateLiqpayOrderId());

        var saved = paymentRepository.save(payment);
        log.info("Retried payment {} for booking {}", paymentId, payment.getBooking().getId());
        auditRetry(paymentId);

        return buildPaymentResponse(saved);
    }

    public void processSuccess(Payment payment, Map<String, String> callbackData) {
        var oldStatus = payment.getStatus();

        boolean flipped = requiresNewTransactionTemplate.execute(status -> {
            int updated = paymentRepository.updateStatusIfCurrentIn(payment.getId(), ACTIVE_STATUSES,
                    PaymentStatus.SUCCESS);
            if (updated == 0) {
                return false;
            }
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setPaymentTime(LocalDateTime.now());
            payment.setLiqpayPaymentId(callbackData.get("payment_id"));
            payment.setLiqpayTransactionId(callbackData.get("transaction_id"));
            payment.setLiqpaySenderCardMask(callbackData.get("sender_card_mask"));
            paymentRepository.save(payment);
            return true;
        });

        if (!flipped) {
            log.warn("Payment {} already processed (status={}), ignoring duplicate callback", payment.getId(),
                    oldStatus);
            return;
        }

        log.info("Payment {} marked SUCCESS", payment.getId());
        auditSuccess(payment, oldStatus);

        try {
            paymentSuccessOrchestrator.handle(payment);
            log.info("Payment {} completed successfully", payment.getId());
        } catch (RuntimeException e) {
            log.error(
                    "Post-payment processing failed for payment {} (booking {}) after status was already committed "
                            + "as SUCCESS - tickets/booking confirmation/bonus accrual may be incomplete, manual "
                            + "reconciliation required",
                    payment.getId(), payment.getBooking().getId(), e);
        }
    }

    public void processFailure(Payment payment, Map<String, String> callbackData) {
        var oldStatus = payment.getStatus();

        int updated = paymentRepository.updateStatusIfCurrentIn(payment.getId(), ACTIVE_STATUSES,
                PaymentStatus.FAILED);
        if (updated == 0) {
            log.warn("Payment {} already processed (status={}), ignoring duplicate callback", payment.getId(),
                    oldStatus);
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setLiqpayErrorCode(callbackData.get("err_code"));
        payment.setLiqpayErrorDescription(callbackData.get("err_description"));

        sendFailureEmail(payment, payment.getBooking());
        log.warn("Payment {} failed: {}", payment.getId(), callbackData.get("err_description"));
        auditFailure(payment, oldStatus, callbackData);
    }

    private void validateBookingForPayment(Booking booking) {
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw PaymentProcessingException.bookingNotPending();
        }
        if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw PaymentProcessingException.bookingExpired();
        }
        if (booking.getSession().getStartTime().isBefore(LocalDateTime.now().plusMinutes(sessionTooCloseMinutes))) {
            throw new SessionTooCloseException(booking.getSession().getStartTime());
        }
        boolean allSeatsAvailable = booking.getSeatReservations().stream()
                .allMatch(seat -> seat.getStatus() == ReservationStatus.CONFIRMED);
        if (!allSeatsAvailable) {
            throw PaymentProcessingException.seatsNoLongerAvailable();
        }
    }

    private void sendFailureEmail(Payment payment, Booking booking) {
        emailService.sendSafely("send payment failed email", booking.getId(), () -> {
            var sessionTime = dateTimeFormatter.formatStandard(booking.getSession().getStartTime());
            var errorDescription = payment.getLiqpayErrorDescription() != null ? payment.getLiqpayErrorDescription()
                    : "Payment error";
            var bookingNumber = numberGenerator.generateBookingNumber(booking);

            emailService.sendPaymentFailedEmail(booking.getUser().getEmail(), bookingNumber,
                    booking.getSession().getMovie().getTitle(), sessionTime, errorDescription);

            log.debug("Sent payment failed email to {}", booking.getUser().getEmail());
        });
    }

    private PaymentResponse buildPaymentResponse(Payment payment) {
        var booking = payment.getBooking();
        return new PaymentResponse(payment.getId(), numberGenerator.generateBookingNumber(booking),
                booking.getSession().getMovie().getTitle(), booking.getSession().getStartTime(),
                booking.getSession().getHall().getName(), payment.getAmount(), payment.getStatus(),
                payment.getPaymentTime(), payment.getLiqpaySenderCardMask(), payment.getLiqpayErrorDescription());
    }

    private void auditCreate(Payment payment, Booking booking) {
        Map<String, Object> details = new HashMap<>();
        details.put("bookingId", booking.getId());
        details.put("amount", payment.getAmount());
        details.put("status", PaymentStatus.PENDING);
        auditService.logChange("Payment", payment.getId(), "Payment #" + payment.getId(), AuditAction.CREATED, null,
                details);
    }

    private void auditRetry(Long paymentId) {
        Map<String, Object> details = new HashMap<>();
        details.put("paymentId", paymentId);
        details.put("status", PaymentStatus.PENDING);
        auditService.logChange("Payment", paymentId, "Payment #" + paymentId, AuditAction.RETRY, null, details);
    }

    private void auditSuccess(Payment payment, PaymentStatus oldStatus) {
        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("status", oldStatus);
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("status", PaymentStatus.SUCCESS);
        auditService.logChange("Payment", payment.getId(), "Payment #" + payment.getId(), AuditAction.SUCCESS,
                oldDetails, newDetails);
    }

    private void auditFailure(Payment payment, PaymentStatus oldStatus, Map<String, String> callbackData) {
        Map<String, Object> oldDetails = new HashMap<>();
        oldDetails.put("status", oldStatus);
        Map<String, Object> newDetails = new HashMap<>();
        newDetails.put("status", PaymentStatus.FAILED);
        newDetails.put("errorCode", callbackData.get("err_code"));
        newDetails.put("errorDescription", callbackData.get("err_description"));
        auditService.logChange("Payment", payment.getId(), "Payment #" + payment.getId(), AuditAction.FAILED,
                oldDetails, newDetails);
    }
}