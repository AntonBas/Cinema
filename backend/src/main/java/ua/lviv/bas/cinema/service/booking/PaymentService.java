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

		Booking booking = bookingRepository.findByIdAndUserId(request.bookingId(), user.getId())
				.orElseThrow(() -> new BookingNotFoundException(request.bookingId()));

		validateBookingForPayment(booking);

		Optional<Payment> existingPayment = paymentRepository.findByBookingId(booking.getId());
		if (existingPayment.isPresent() && existingPayment.get().getStatus().isActive()) {
			log.warn("Payment already in progress for booking {}", booking.getId());
			throw PaymentProcessingException.paymentInProgress();
		}

		Payment payment = Payment.builder().booking(booking).amount(booking.getFinalPrice())
				.status(PaymentStatus.PENDING).liqpayOrderId(numberGenerator.generateLiqpayOrderId()).build();

		Payment savedPayment = paymentRepository.save(payment);
		log.info("Created payment {} for booking {}", savedPayment.getId(), booking.getId());

		Map<String, Object> details = new HashMap<>();
		details.put("bookingId", booking.getId());
		details.put("amount", payment.getAmount());
		details.put("status", PaymentStatus.PENDING);

		auditService.logChange("Payment", savedPayment.getId(), "Payment #" + savedPayment.getId(), AuditAction.CREATED,
				null, details);

		return buildPaymentResponse(savedPayment);
	}

	@Transactional(readOnly = true)
	public PaymentResponse getPaymentStatus(Long paymentId, User user) {
		Payment payment = paymentRepository.findByIdWithDetails(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException(paymentId));

		if (!payment.getBooking().getUser().getId().equals(user.getId())) {
			throw new PaymentAccessDeniedException(paymentId, user.getId());
		}

		return buildPaymentResponse(payment);
	}

	public PaymentResponse retryPayment(Long paymentId, User user) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException(paymentId));

		if (!payment.getBooking().getUser().getId().equals(user.getId())) {
			throw new PaymentAccessDeniedException(paymentId, user.getId());
		}

		if (!payment.getStatus().canBeRetried()) {
			throw InvalidPaymentStatusException.notFailed(payment.getStatus());
		}

		validateBookingForPayment(payment.getBooking());

		payment.setStatus(PaymentStatus.PENDING);
		payment.setLiqpayOrderId(numberGenerator.generateLiqpayOrderId());

		Payment savedPayment = paymentRepository.save(payment);
		log.info("Retried payment {} for booking {}", paymentId, payment.getBooking().getId());

		Map<String, Object> details = new HashMap<>();
		details.put("paymentId", paymentId);
		details.put("status", PaymentStatus.PENDING);

		auditService.logChange("Payment", paymentId, "Payment #" + paymentId, AuditAction.RETRY, null, details);

		return buildPaymentResponse(savedPayment);
	}

	public void processSuccessfulPayment(Payment payment, Map<String, String> callbackData) {
		PaymentStatus oldStatus = payment.getStatus();

		payment.setStatus(PaymentStatus.SUCCESS);
		payment.setPaymentTime(LocalDateTime.now());
		payment.setLiqpayPaymentId(callbackData.get("payment_id"));
		payment.setLiqpayTransactionId(callbackData.get("transaction_id"));
		payment.setLiqpaySenderCardMask(callbackData.get("sender_card_mask"));

		bookingService.confirmBooking(payment.getBooking().getId());
		List<Ticket> tickets = ticketService.createTicketsForBooking(payment.getBooking(), payment);

		Integer pointsToAccrue = bonusService.calculatePoints(payment.getBooking().getFinalPrice());
		if (pointsToAccrue != null && pointsToAccrue > 0) {
			bonusService.accruePoints(payment.getBooking().getUser().getId(), pointsToAccrue, payment.getBooking(),
					payment);
		}

		sendPaymentSuccessEmail(payment, payment.getBooking(), tickets);

		log.info("Payment {} completed successfully", payment.getId());

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("status", oldStatus);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("status", PaymentStatus.SUCCESS);

		auditService.logChange("Payment", payment.getId(), "Payment #" + payment.getId(), AuditAction.SUCCESS,
				oldDetails, newDetails);
	}

	public void processFailedPayment(Payment payment, Map<String, String> callbackData) {
		PaymentStatus oldStatus = payment.getStatus();

		payment.setStatus(PaymentStatus.FAILED);
		payment.setLiqpayErrorCode(callbackData.get("err_code"));
		payment.setLiqpayErrorDescription(callbackData.get("err_description"));

		sendPaymentFailedEmail(payment, payment.getBooking());
		log.warn("Payment {} failed: {}", payment.getId(), callbackData.get("err_description"));

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("status", oldStatus);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("status", PaymentStatus.FAILED);
		newDetails.put("errorCode", callbackData.get("err_code"));
		newDetails.put("errorDescription", callbackData.get("err_description"));

		auditService.logChange("Payment", payment.getId(), "Payment #" + payment.getId(), AuditAction.FAILED,
				oldDetails, newDetails);
	}

	public void refundPayment(Payment payment, BigDecimal amount, String description) {
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

		try {
			String refundData = paymentGatewayService.prepareRefundData(payment.getLiqpayPaymentId(),
					payment.getLiqpayOrderId(), amount, description);

			paymentGatewayService.processRefund(refundData);

			log.info("Refund initiated for payment {}: amount={}, description={}", payment.getId(), amount,
					description);

			PaymentStatus oldStatus = payment.getStatus();
			PaymentStatus newStatus = amount.compareTo(payment.getAmount()) == 0 ? PaymentStatus.REFUNDED
					: PaymentStatus.PARTIALLY_REFUNDED;

			payment.setStatus(newStatus);
			paymentRepository.save(payment);

			sendRefundEmail(payment, amount, description);

			Map<String, Object> oldDetails = new HashMap<>();
			oldDetails.put("status", oldStatus);

			Map<String, Object> newDetails = new HashMap<>();
			newDetails.put("status", newStatus);
			newDetails.put("refundAmount", amount);
			newDetails.put("description", description);

			auditService.logChange("Payment", payment.getId(), "Payment #" + payment.getId(), AuditAction.REFUND,
					oldDetails, newDetails);

		} catch (PaymentProcessingException e) {
			log.error("Payment processing failed for refund payment {}", payment.getId(), e);
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error during refund processing for payment {}", payment.getId(), e);
			throw new PaymentProcessingException("Failed to process refund: " + e.getMessage(), e);
		}
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

	private void sendPaymentSuccessEmail(Payment payment, Booking booking, List<Ticket> tickets) {
		try {
			String sessionTime = formatSessionTime(booking);
			String seatsInfo = extractSeatsInfo(booking);
			String bookingNumber = numberGenerator.generateBookingNumber(booking);

			emailService.sendTicketsEmail(booking.getUser().getEmail(), bookingNumber,
					booking.getSession().getMovie().getTitle(), sessionTime, booking.getSession().getHall().getName(),
					payment.getAmount(), "Credit card", seatsInfo);

			log.debug("Sent payment success email to {}", booking.getUser().getEmail());
		} catch (Exception e) {
			log.error("Failed to send payment success email for booking {}", booking.getId(), e);
		}
	}

	private void sendPaymentFailedEmail(Payment payment, Booking booking) {
		try {
			String sessionTime = formatSessionTime(booking);
			String errorDescription = payment.getLiqpayErrorDescription() != null ? payment.getLiqpayErrorDescription()
					: "Payment error";
			String bookingNumber = numberGenerator.generateBookingNumber(booking);

			emailService.sendPaymentFailedEmail(booking.getUser().getEmail(), bookingNumber,
					booking.getSession().getMovie().getTitle(), sessionTime, errorDescription);

			log.debug("Sent payment failed email to {}", booking.getUser().getEmail());
		} catch (Exception e) {
			log.error("Failed to send payment failed email for booking {}", booking.getId(), e);
		}
	}

	private void sendRefundEmail(Payment payment, BigDecimal amount, String description) {
		try {
			Booking booking = payment.getBooking();
			String sessionTime = formatSessionTime(booking);
			String seatsInfo = extractSeatsInfo(booking);
			String bookingNumber = numberGenerator.generateBookingNumber(booking);

			emailService.sendRefundEmail(booking.getUser().getEmail(), bookingNumber,
					booking.getSession().getMovie().getTitle(), sessionTime, booking.getSession().getHall().getName(),
					amount, seatsInfo, description);

			log.debug("Sent refund email to {}", booking.getUser().getEmail());
		} catch (Exception e) {
			log.error("Failed to send refund email for payment {}", payment.getId(), e);
		}
	}

	private PaymentResponse buildPaymentResponse(Payment payment) {
		Booking booking = payment.getBooking();
		return new PaymentResponse(numberGenerator.generateBookingNumber(booking),
				booking.getSession().getMovie().getTitle(), booking.getSession().getStartTime(),
				booking.getSession().getHall().getName(), payment.getAmount(), payment.getStatus(),
				payment.getPaymentTime(), payment.getLiqpaySenderCardMask(), payment.getLiqpayErrorDescription());
	}

	private String formatSessionTime(Booking booking) {
		return dateTimeFormatter.formatStandard(booking.getSession().getStartTime());
	}

	private String extractSeatsInfo(Booking booking) {
		return booking.getSeatReservations().stream()
				.map(seat -> String.format("Row %d, Seat %d", seat.getSeat().getRow(), seat.getSeat().getNumber()))
				.collect(Collectors.joining(", "));
	}
}