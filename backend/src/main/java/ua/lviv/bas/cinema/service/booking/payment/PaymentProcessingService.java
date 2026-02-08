package ua.lviv.bas.cinema.service.booking.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.payment.InvalidPaymentStatusException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.PaymentRepository;
import ua.lviv.bas.cinema.service.booking.management.BookingManagementService;
import ua.lviv.bas.cinema.service.booking.ticket.TicketService;
import ua.lviv.bas.cinema.service.integration.payment.PaymentGatewayService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;
import ua.lviv.bas.cinema.service.user.BonusService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentProcessingService {
	private final PaymentRepository paymentRepository;
	private final BookingRepository bookingRepository;
	private final PaymentValidator paymentValidator;
	private final PaymentNotificationService notificationService;
	private final PaymentGatewayService paymentGatewayService;
	private final TicketService ticketService;
	private final BonusService bonusService;
	private final NumberGeneratorService numberGenerator;
	private final BookingManagementService bookingManagementService;

	public PaymentResponse createPayment(PaymentCreateRequest request, User user) {
		log.info("Creating payment for booking {} by user {}", request.getBookingId(), user.getId());

		Booking booking = bookingRepository.findByIdAndUserId(request.getBookingId(), user.getId())
				.orElseThrow(() -> new BookingNotFoundException(request.getBookingId()));

		paymentValidator.validateBookingForPayment(booking);

		Optional<Payment> existingPayment = paymentRepository.findByBookingId(booking.getId());
		if (existingPayment.isPresent() && existingPayment.get().getStatus().isActive()) {
			log.warn("Payment already in progress for booking {}", booking.getId());
			throw PaymentProcessingException.paymentInProgress();
		}

		Payment payment = Payment.builder().booking(booking).amount(booking.getFinalPrice())
				.status(PaymentStatus.PENDING).liqpayOrderId(numberGenerator.generateLiqpayOrderId())
				.createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();

		Payment savedPayment = paymentRepository.save(payment);
		log.info("Created payment {} for booking {}", savedPayment.getId(), booking.getId());

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

	public void processSuccessfulPayment(Payment payment, Map<String, String> callbackData) {
		payment.setStatus(PaymentStatus.SUCCESS);
		payment.setPaymentTime(LocalDateTime.now());
		payment.setLiqpayPaymentId(callbackData.get("payment_id"));
		payment.setLiqpayTransactionId(callbackData.get("transaction_id"));
		payment.setLiqpaySenderCardMask(callbackData.get("sender_card_mask"));

		bookingManagementService.confirmBooking(payment.getBooking().getId());
		List<Ticket> tickets = ticketService.createTicketsForBooking(payment.getBooking(), payment);

		Integer pointsToAccrue = bonusService.calculatePoints(payment.getBooking().getFinalPrice());
		if (pointsToAccrue != null && pointsToAccrue > 0) {
			bonusService.accruePoints(payment.getBooking().getUser().getId(), pointsToAccrue, payment.getBooking(),
					payment);
		}

		notificationService.sendPaymentSuccessEmail(payment, payment.getBooking(), tickets);

		log.info("Payment {} completed successfully", payment.getId());
	}

	public void processFailedPayment(Payment payment, Map<String, String> callbackData) {
		payment.setStatus(PaymentStatus.FAILED);
		payment.setLiqpayErrorCode(callbackData.get("err_code"));
		payment.setLiqpayErrorDescription(callbackData.get("err_description"));

		notificationService.sendPaymentFailedEmail(payment, payment.getBooking());
		log.warn("Payment {} failed: {}", payment.getId(), callbackData.get("err_description"));
	}

	@Transactional
	public PaymentResponse retryPayment(Long paymentId, User user) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException(paymentId));

		if (!payment.getBooking().getUser().getId().equals(user.getId())) {
			throw new PaymentAccessDeniedException(paymentId, user.getId());
		}

		if (!payment.getStatus().canBeRetried()) {
			throw InvalidPaymentStatusException.notFailed(payment.getStatus());
		}

		paymentValidator.validateBookingForPayment(payment.getBooking());

		payment.setStatus(PaymentStatus.PENDING);
		payment.setLiqpayOrderId(numberGenerator.generateLiqpayOrderId());
		payment.setUpdatedAt(LocalDateTime.now());

		Payment savedPayment = paymentRepository.save(payment);
		log.info("Retried payment {} for booking {}", paymentId, payment.getBooking().getId());

		return buildPaymentResponse(savedPayment);
	}

	@Transactional
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
			log.info("=== STARTING REFUND PROCESS ===");
			log.info("Payment ID: {}", payment.getId());
			log.info("LiqPay Payment ID: {}", payment.getLiqpayPaymentId());
			log.info("LiqPay Order ID: {}", payment.getLiqpayOrderId());
			log.info("Refund amount: {} (Original: {})", amount, payment.getAmount());
			log.info("Description: {}", description);

			String refundData = paymentGatewayService.prepareRefundData(payment.getLiqpayPaymentId(),
					payment.getLiqpayOrderId(), amount, description);

			paymentGatewayService.processRefund(refundData);

			log.info("Refund initiated for payment {}: amount={}, description={}", payment.getId(), amount,
					description);

			if (amount.compareTo(payment.getAmount()) == 0) {
				payment.setStatus(PaymentStatus.REFUNDED);
				log.info("Payment {} fully refunded", payment.getId());
			} else {
				payment.setStatus(PaymentStatus.PARTIALLY_REFUNDED);
				log.info("Payment {} partially refunded ({} of {})", payment.getId(), amount, payment.getAmount());
			}

			payment.setUpdatedAt(LocalDateTime.now());
			paymentRepository.save(payment);

			notificationService.sendRefundEmail(payment, amount, description);

			log.info("Refund processed successfully: paymentId={}, amount={}", payment.getId(), amount);

		} catch (PaymentProcessingException e) {
			log.error("Payment processing failed for refund payment {}", payment.getId(), e);
			throw e;
		} catch (Exception e) {
			log.error("Unexpected error during refund processing for payment {}", payment.getId(), e);
			throw new PaymentProcessingException("Failed to process refund: " + e.getMessage(), e);
		}
	}

	private PaymentResponse buildPaymentResponse(Payment payment) {
		Booking booking = payment.getBooking();
		return PaymentResponse.builder().id(payment.getId()).bookingId(booking.getId())
				.bookingNumber(numberGenerator.generateBookingNumber(booking)).userEmail(booking.getUser().getEmail())
				.movieTitle(booking.getSession().getMovie().getTitle()).sessionTime(booking.getSession().getStartTime())
				.hallName(booking.getSession().getHall().getName()).amount(payment.getAmount())
				.finalAmount(payment.getAmount()).status(payment.getStatus()).liqpayOrderId(payment.getLiqpayOrderId())
				.liqpayPaymentId(payment.getLiqpayPaymentId()).paymentTime(payment.getPaymentTime())
				.errorCode(payment.getLiqpayErrorCode()).errorDescription(payment.getLiqpayErrorDescription())
				.senderCardMask(payment.getLiqpaySenderCardMask()).createdAt(payment.getCreatedAt())
				.updatedAt(payment.getUpdatedAt()).build();
	}
}