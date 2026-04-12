package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.status.BookingStatus;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.SessionTooCloseException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.InvalidPaymentStatusException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.repository.booking.BookingRepository;
import ua.lviv.bas.cinema.repository.booking.PaymentRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.integration.payment.PaymentGatewayService;
import ua.lviv.bas.cinema.service.notification.EmailService;
import ua.lviv.bas.cinema.service.shared.DateTimeFormatterService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;
import ua.lviv.bas.cinema.service.ticket.TicketService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final BookingRepository bookingRepository;
	private final PaymentGatewayService paymentGatewayService;
	private final TicketService ticketService;
	private final BonusService bonusService;
	private final NumberGeneratorService numberGenerator;
	private final BookingService bookingService;
	private final AuditService auditService;
	private final EmailService emailService;
	private final DateTimeFormatterService dateTimeFormatter;

	@Value("${booking.session-too-close-minutes:30}")
	private int sessionTooCloseMinutes;

	public PaymentResponse createPayment(PaymentCreateRequest request, User user) {
		log.info("Creating payment for booking {} by user {}", request.bookingId(), user.getId());

		var booking = bookingRepository.findByIdAndUserId(request.bookingId(), user.getId())
				.orElseThrow(() -> new BookingNotFoundException(request.bookingId()));

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
				.orElseThrow(() -> new PaymentNotFoundException(paymentId));

		if (!payment.getBooking().getUser().getId().equals(user.getId())) {
			throw new PaymentAccessDeniedException(paymentId, user.getId());
		}

		return buildPaymentResponse(payment);
	}

	public PaymentResponse retryPayment(Long paymentId, User user) {
		var payment = paymentRepository.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));

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

		payment.setStatus(PaymentStatus.SUCCESS);
		payment.setPaymentTime(LocalDateTime.now());
		payment.setLiqpayPaymentId(callbackData.get("payment_id"));
		payment.setLiqpayTransactionId(callbackData.get("transaction_id"));
		payment.setLiqpaySenderCardMask(callbackData.get("sender_card_mask"));

		bookingService.confirmBooking(payment.getBooking().getId());
		var tickets = ticketService.createTicketsForBooking(payment.getBooking(), payment);

		var pointsToAccrue = bonusService.calculateAccrualPoints(payment.getBooking().getFinalPrice());
		if (pointsToAccrue != null && pointsToAccrue > 0) {
			bonusService.accruePointsForPayment(payment.getBooking().getUser().getId(), pointsToAccrue,
					payment.getBooking(), payment);
		}

		sendSuccessEmail(payment, payment.getBooking(), tickets);
		log.info("Payment {} completed successfully", payment.getId());
		auditSuccess(payment, oldStatus);
	}

	public void processFailure(Payment payment, Map<String, String> callbackData) {
		var oldStatus = payment.getStatus();

		payment.setStatus(PaymentStatus.FAILED);
		payment.setLiqpayErrorCode(callbackData.get("err_code"));
		payment.setLiqpayErrorDescription(callbackData.get("err_description"));

		sendFailureEmail(payment, payment.getBooking());
		log.warn("Payment {} failed: {}", payment.getId(), callbackData.get("err_description"));
		auditFailure(payment, oldStatus, callbackData);
	}

	public void refund(Payment payment, BigDecimal amount, String description) {
		if (payment.getStatus() != PaymentStatus.SUCCESS) {
			throw PaymentProcessingException.refundFailed("Cannot refund payment with status: " + payment.getStatus());
		}

		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw PaymentProcessingException.refundFailed("Refund amount must be positive");
		}

		if (amount.compareTo(payment.getAmount()) > 0) {
			throw PaymentProcessingException.refundFailed(
					String.format("Refund amount %s exceeds payment amount %s", amount, payment.getAmount()));
		}

		if (payment.getLiqpayPaymentId() == null || payment.getLiqpayPaymentId().isEmpty()) {
			throw PaymentProcessingException.refundFailed("Missing LiqPay payment ID for refund");
		}

		if (payment.getLiqpayOrderId() == null || payment.getLiqpayOrderId().isEmpty()) {
			throw PaymentProcessingException.refundFailed("Missing LiqPay order ID for refund");
		}

		var refundData = paymentGatewayService.prepareRefundData(payment.getLiqpayPaymentId(),
				payment.getLiqpayOrderId(), amount, description);

		paymentGatewayService.processRefund(refundData);

		log.info("Refund initiated for payment {}: amount={}, description={}", payment.getId(), amount, description);

		var oldStatus = payment.getStatus();
		var newStatus = amount.compareTo(payment.getAmount()) == 0 ? PaymentStatus.REFUNDED
				: PaymentStatus.PARTIALLY_REFUNDED;

		payment.setStatus(newStatus);
		paymentRepository.save(payment);

		sendRefundEmail(payment, amount, description);
		auditRefund(payment, oldStatus, newStatus, amount, description);
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

	private void sendSuccessEmail(Payment payment, Booking booking, List<Ticket> tickets) {
		try {
			var sessionTime = dateTimeFormatter.formatStandard(booking.getSession().getStartTime());
			var seatsInfo = extractSeatsInfo(booking);
			var bookingNumber = numberGenerator.generateBookingNumber(booking);

			emailService.sendTicketsEmail(booking.getUser().getEmail(), bookingNumber,
					booking.getSession().getMovie().getTitle(), sessionTime, booking.getSession().getHall().getName(),
					payment.getAmount(), "Credit card", seatsInfo);

			log.debug("Sent payment success email to {}", booking.getUser().getEmail());
		} catch (Exception e) {
			log.error("Failed to send payment success email for booking {}", booking.getId(), e);
		}
	}

	private void sendFailureEmail(Payment payment, Booking booking) {
		try {
			var sessionTime = dateTimeFormatter.formatStandard(booking.getSession().getStartTime());
			var errorDescription = payment.getLiqpayErrorDescription() != null ? payment.getLiqpayErrorDescription()
					: "Payment error";
			var bookingNumber = numberGenerator.generateBookingNumber(booking);

			emailService.sendPaymentFailedEmail(booking.getUser().getEmail(), bookingNumber,
					booking.getSession().getMovie().getTitle(), sessionTime, errorDescription);

			log.debug("Sent payment failed email to {}", booking.getUser().getEmail());
		} catch (Exception e) {
			log.error("Failed to send payment failed email for booking {}", booking.getId(), e);
		}
	}

	private void sendRefundEmail(Payment payment, BigDecimal amount, String description) {
		try {
			var booking = payment.getBooking();
			var sessionTime = dateTimeFormatter.formatStandard(booking.getSession().getStartTime());
			var seatsInfo = extractSeatsInfo(booking);
			var bookingNumber = numberGenerator.generateBookingNumber(booking);

			emailService.sendRefundEmail(booking.getUser().getEmail(), bookingNumber,
					booking.getSession().getMovie().getTitle(), sessionTime, booking.getSession().getHall().getName(),
					amount, seatsInfo, description);

			log.debug("Sent refund email to {}", booking.getUser().getEmail());
		} catch (Exception e) {
			log.error("Failed to send refund email for payment {}", payment.getId(), e);
		}
	}

	private PaymentResponse buildPaymentResponse(Payment payment) {
		var booking = payment.getBooking();
		return new PaymentResponse(payment.getId(), numberGenerator.generateBookingNumber(booking),
				booking.getSession().getMovie().getTitle(), booking.getSession().getStartTime(),
				booking.getSession().getHall().getName(), payment.getAmount(), payment.getStatus(),
				payment.getPaymentTime(), payment.getLiqpaySenderCardMask(), payment.getLiqpayErrorDescription());
	}

	private String extractSeatsInfo(Booking booking) {
		return booking.getSeatReservations().stream()
				.map(seat -> String.format("Row %d, Seat %d", seat.getSeat().getRow(), seat.getSeat().getNumber()))
				.collect(Collectors.joining(", "));
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

	private void auditRefund(Payment payment, PaymentStatus oldStatus, PaymentStatus newStatus, BigDecimal amount,
			String description) {
		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("status", oldStatus);
		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("status", newStatus);
		newDetails.put("refundAmount", amount);
		newDetails.put("description", description);
		auditService.logChange("Payment", payment.getId(), "Payment #" + payment.getId(), AuditAction.REFUND,
				oldDetails, newDetails);
	}
}