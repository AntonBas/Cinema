package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.gson.Gson;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookedSeatStatus;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.request.LiqPayCallbackRequest;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.SessionTooCloseException;
import ua.lviv.bas.cinema.exception.domain.payment.InvalidPaymentStatusException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.exception.domain.payment.PaymentProcessingException;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.PaymentRepository;
import ua.lviv.bas.cinema.service.notification.EmailService;
import ua.lviv.bas.cinema.service.user.BonusService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final BookingRepository bookingRepository;
	private final EmailService emailService;
	private final TicketService ticketService;
	private final BonusService bonusService;
	private final BookingService bookingService;

	@Value("${payment.liqpay.public_key}")
	private String liqpayPublicKey;

	@Value("${payment.liqpay.private_key}")
	private String liqpayPrivateKey;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	@Value("${payment.liqpay.callback_url}")
	private String liqpayCallbackUrl;

	@Value("${payment.liqpay.sandbox_mode:true}")
	private boolean sandboxMode;

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
	private final Gson gson = new Gson();

	public PaymentResponse createPayment(PaymentCreateRequest request, User user) {
		log.info("Creating payment for booking {} by user {}", request.getBookingId(), user.getId());
		Booking booking = bookingRepository.findByIdAndUserId(request.getBookingId(), user.getId())
				.orElseThrow(() -> new BookingNotFoundException(request.getBookingId()));
		validateBookingForPayment(booking);
		Optional<Payment> existingPayment = paymentRepository.findByBookingId(booking.getId());
		if (existingPayment.isPresent() && (existingPayment.get().getStatus() == PaymentStatus.PENDING
				|| existingPayment.get().getStatus() == PaymentStatus.PROCESSING)) {
			log.warn("Payment already in progress for booking {}", booking.getId());
			throw PaymentProcessingException.paymentInProgress();
		}
		BigDecimal finalAmount = booking.getFinalPrice();
		String liqpayOrderId = generateLiqpayOrderId();
		Payment payment = Payment.builder().booking(booking).amount(finalAmount).status(PaymentStatus.PENDING)
				.liqpayOrderId(liqpayOrderId).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
		Payment savedPayment = paymentRepository.save(payment);
		log.info("Created payment {} for booking {}", savedPayment.getId(), booking.getId());
		return buildPaymentResponse(savedPayment);
	}

	public PaymentLiqPayDataResponse preparePaymentData(Long paymentId, User user) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException(paymentId));
		if (!payment.getBooking().getUser().getId().equals(user.getId())) {
			throw new PaymentAccessDeniedException(paymentId, user.getId());
		}
		if (payment.getStatus() != PaymentStatus.PENDING) {
			throw InvalidPaymentStatusException.notPending(payment.getStatus());
		}
		return prepareLiqPayPaymentData(payment);
	}

	public void processLiqPayCallback(LiqPayCallbackRequest callbackRequest) {
		log.info("Processing LiqPay callback with data length: {}",
				callbackRequest.getData() != null ? callbackRequest.getData().length() : 0);
		try {
			String calculatedSignature = generateLiqPaySignature(callbackRequest.getData());
			if (!calculatedSignature.equals(callbackRequest.getSignature())) {
				log.error("Invalid LiqPay signature! Received: {}, Calculated: {}", callbackRequest.getSignature(),
						calculatedSignature);
				throw new PaymentProcessingException("Invalid LiqPay signature");
			}

			Map<String, String> decodedData = decodeLiqPayData(callbackRequest.getData());
			String orderId = decodedData.get("order_id");
			String status = decodedData.get("status");
			log.info("Decoded LiqPay callback: orderId={}, status={}", orderId, status);

			Payment payment = paymentRepository.findByLiqpayOrderId(orderId)
					.orElseThrow(() -> new PaymentNotFoundException(orderId));

			String statusLower = status.toLowerCase();
			switch (statusLower) {
			case "success":
			case "sandbox":
				handleSuccessfulPayment(payment, decodedData);
				break;
			case "failure":
			case "error":
				handleFailedPayment(payment, decodedData);
				break;
			case "wait_secure":
				handlePendingPayment(payment);
				break;
			default:
				log.warn("Unknown payment status: {}", status);
				payment.setStatus(PaymentStatus.FAILED);
			}

			updatePaymentWithCallbackData(payment, decodedData);
			paymentRepository.save(payment);
		} catch (Exception e) {
			log.error("Failed to process LiqPay callback", e);
			throw new PaymentProcessingException("Failed to process callback: " + e.getMessage());
		}
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
		if (payment.getStatus() != PaymentStatus.FAILED) {
			throw InvalidPaymentStatusException.notFailed(payment.getStatus());
		}
		payment.setStatus(PaymentStatus.PENDING);
		payment.setLiqpayOrderId(generateLiqpayOrderId());
		payment.setUpdatedAt(LocalDateTime.now());
		Booking booking = payment.getBooking();
		booking.setStatus(BookingStatus.PENDING);
		booking.setExpiresAt(LocalDateTime.now().plusMinutes(20));
		Payment savedPayment = paymentRepository.save(payment);
		bookingRepository.save(booking);
		log.info("Retried payment {} for booking {}", paymentId, booking.getId());
		return buildPaymentResponse(savedPayment);
	}

	@Transactional
	public void refundPayment(Payment payment, BigDecimal amount, String description) {
		log.info("Processing refund for payment {}: amount={}, description={}", payment.getId(), amount, description);
		if (payment.getStatus() != PaymentStatus.SUCCESS) {
			throw PaymentProcessingException
					.refundFailed(String.format("Cannot refund payment with status: %s", payment.getStatus()));
		}
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw PaymentProcessingException.refundFailed("Refund amount must be positive");
		}
		if (amount.compareTo(payment.getAmount()) > 0) {
			throw PaymentProcessingException.refundFailed(
					String.format("Refund amount %s exceeds payment amount %s", amount, payment.getAmount()));
		}
		try {
			String refundOrderId = "REF_" + UUID.randomUUID().toString().substring(0, 16).toUpperCase() + "_"
					+ System.currentTimeMillis() % 10000;
			Map<String, Object> refundParams = new LinkedHashMap<>();
			refundParams.put("public_key", liqpayPublicKey);
			refundParams.put("version", "3");
			refundParams.put("action", "refund");
			refundParams.put("amount", amount.setScale(2, RoundingMode.HALF_UP).toString());
			refundParams.put("currency", "UAH");
			refundParams.put("description", description);
			refundParams.put("order_id", refundOrderId);
			if (payment.getLiqpayPaymentId() != null) {
				refundParams.put("payment_id", payment.getLiqpayPaymentId());
			}
			log.info("Refund request prepared for LiqPay: orderId={}, amount={}, paymentId={}", refundOrderId, amount,
					payment.getLiqpayPaymentId());
			payment.setUpdatedAt(LocalDateTime.now());
			paymentRepository.save(payment);
			sendRefundEmail(payment, amount, description);
			log.info("Refund processed successfully: paymentId={}, refundOrderId={}, amount={}", payment.getId(),
					refundOrderId, amount);
		} catch (Exception e) {
			log.error("Refund processing failed for payment {}", payment.getId(), e);
			throw new PaymentProcessingException("Failed to process refund", e);
		}
	}

	@Transactional(readOnly = true)
	public PaymentResponse getUserPaymentByBookingId(Long bookingId, User user) {
		Payment payment = paymentRepository.findByBookingIdAndUserId(bookingId, user.getId()).orElseThrow(
				() -> new PaymentNotFoundException(String.format("Payment for booking %d not found", bookingId)));
		return buildPaymentResponse(payment);
	}

	private void validateBookingForPayment(Booking booking) {
		log.debug("Validating booking {} for payment", booking.getId());
		if (booking.getStatus() != BookingStatus.PENDING) {
			throw PaymentProcessingException.bookingNotPending();
		}
		if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw PaymentProcessingException.bookingExpired();
		}
		if (booking.getSession().getStartTime().isBefore(LocalDateTime.now().plusMinutes(30))) {
			throw new SessionTooCloseException(booking.getSession().getStartTime());
		}
		boolean allSeatsAvailable = booking.getBookedSeats().stream()
				.allMatch(seat -> seat.getStatus() == BookedSeatStatus.PENDING);
		if (!allSeatsAvailable) {
			throw PaymentProcessingException.seatsNoLongerAvailable();
		}
		log.debug("Booking {} validated successfully for payment", booking.getId());
	}

	private PaymentLiqPayDataResponse prepareLiqPayPaymentData(Payment payment) {
		try {
			Booking booking = payment.getBooking();
			Session session = booking.getSession();
			Map<String, Object> params = new LinkedHashMap<>();
			params.put("public_key", liqpayPublicKey);
			params.put("version", "3");
			params.put("action", "pay");
			params.put("amount", payment.getAmount().setScale(2, RoundingMode.HALF_UP).toString());
			params.put("currency", "UAH");
			params.put("description", String.format("Tickets for %s, hall %s, %s", session.getMovie().getTitle(),
					session.getHall().getName(), session.getStartTime().format(DATE_FORMATTER)));
			params.put("order_id", payment.getLiqpayOrderId());
			String resultUrl = frontendUrl + "/booking/success?bookingId=" + booking.getId() + "&paymentId="
					+ payment.getId();
			params.put("result_url", resultUrl);
			params.put("server_url", liqpayCallbackUrl);
			params.put("language", "uk");
			params.put("sender_first_name", booking.getUser().getFirstName());
			params.put("sender_last_name", booking.getUser().getLastName());
			params.put("sender_email", booking.getUser().getEmail());
			if (sandboxMode) {
				params.put("sandbox", "1");
			}
			String jsonData = gson.toJson(params);
			String data = Base64.getEncoder().encodeToString(jsonData.getBytes());
			String signature = generateLiqPaySignature(data);
			log.info("=== LIQPAY PAYMENT DATA ===");
			log.info("Payment ID: {}", payment.getId());
			log.info("Order ID: {}", payment.getLiqpayOrderId());
			log.info("Callback URL (server_url): {}", liqpayCallbackUrl);
			log.info("Redirect URL (result_url): {}", resultUrl);
			log.info("Sandbox mode: {}", sandboxMode);
			log.info("=== END LIQPAY DATA ===");
			return PaymentLiqPayDataResponse.builder().data(data).signature(signature)
					.paymentUrl("https://www.liqpay.ua/api/3/checkout").liqpayOrderId(payment.getLiqpayOrderId())
					.build();
		} catch (Exception e) {
			log.error("Failed to prepare LiqPay payment data for payment {}", payment.getId(), e);
			throw new PaymentProcessingException("Failed to prepare payment data");
		}
	}

	private void handleSuccessfulPayment(Payment payment, Map<String, String> decodedData) {
		log.info("Processing successful payment {} for order {}", payment.getId(), decodedData.get("order_id"));
		payment.setStatus(PaymentStatus.SUCCESS);
		payment.setPaymentTime(LocalDateTime.now());
		payment.setLiqpayPaymentId(decodedData.get("payment_id"));
		payment.setLiqpayTransactionId(decodedData.get("transaction_id"));
		payment.setLiqpaySenderCardMask(decodedData.get("sender_card_mask"));
		bookingService.confirmBooking(payment.getBooking().getId());
		List<Ticket> tickets = ticketService.createTicketsForBooking(payment.getBooking(), payment);
		Integer pointsToAccrue = bonusService.calculateAccruedPointsForAmount(payment.getBooking().getFinalPrice());
		if (pointsToAccrue != null && pointsToAccrue > 0) {
			bonusService.accrueBonusPointsForPayment(payment.getBooking().getUser().getId(), pointsToAccrue,
					payment.getBooking(), payment);
		}
		ticketService.sendTicketsToUser(payment.getBooking());
		sendPaymentSuccessEmail(payment, payment.getBooking(), tickets);
		log.info("Payment {} completed successfully", payment.getId());
	}

	private void handleFailedPayment(Payment payment, Map<String, String> decodedData) {
		log.warn("Payment {} failed: errorCode={}, errorDescription={}", payment.getId(), decodedData.get("err_code"),
				decodedData.get("err_description"));
		payment.setStatus(PaymentStatus.FAILED);
		payment.setLiqpayErrorCode(decodedData.get("err_code"));
		payment.setLiqpayErrorDescription(decodedData.get("err_description"));
		sendPaymentFailedEmail(payment, payment.getBooking());
	}

	private void handlePendingPayment(Payment payment) {
		log.debug("Payment {} is waiting for secure processing", payment.getId());
		payment.setStatus(PaymentStatus.PROCESSING);
	}

	private String generateLiqPaySignature(String data) {
		try {
			String str = liqpayPrivateKey + data + liqpayPrivateKey;
			MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
			byte[] digest = sha1.digest(str.getBytes());
			return Base64.getEncoder().encodeToString(digest);
		} catch (Exception e) {
			log.error("Failed to generate LiqPay signature", e);
			throw new PaymentProcessingException("Failed to generate payment signature");
		}
	}

	private String generateLiqpayOrderId() {
		return "ORD_" + UUID.randomUUID().toString().substring(0, 16).toUpperCase() + "_"
				+ System.currentTimeMillis() % 10000;
	}

	private void updatePaymentWithCallbackData(Payment payment, Map<String, String> decodedData) {
		payment.setLiqpayPaymentId(decodedData.get("payment_id"));
		payment.setLiqpayTransactionId(decodedData.get("transaction_id"));
		payment.setLiqpayErrorCode(decodedData.get("err_code"));
		payment.setLiqpayErrorDescription(decodedData.get("err_description"));
		payment.setLiqpaySenderCardMask(decodedData.get("sender_card_mask"));
		payment.setUpdatedAt(LocalDateTime.now());
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> decodeLiqPayData(String data) {
		try {
			byte[] decodedBytes = Base64.getDecoder().decode(data);
			String decodedString = new String(decodedBytes);
			Map<String, Object> rawMap = gson.fromJson(decodedString, Map.class);

			Map<String, String> result = new HashMap<>();
			for (Map.Entry<String, Object> entry : rawMap.entrySet()) {
				if (entry.getValue() != null) {
					result.put(entry.getKey(), entry.getValue().toString());
				}
			}

			return result;
		} catch (Exception e) {
			log.error("Failed to decode LiqPay data: {}", data, e);
			throw new PaymentProcessingException("Invalid LiqPay callback data");
		}
	}

	private void sendPaymentSuccessEmail(Payment payment, Booking booking, List<Ticket> tickets) {
		try {
			User user = booking.getUser();
			Session session = booking.getSession();
			String seatsInfo = booking.getBookedSeats().stream()
					.map(seat -> String.format("Row %d, Seat %d", seat.getSeat().getRow(), seat.getSeat().getNumber()))
					.collect(java.util.stream.Collectors.joining(", "));
			emailService.sendTicketsEmail(user.getEmail(), booking.getId().toString(), session.getMovie().getTitle(),
					session.getStartTime().format(DATE_FORMATTER), session.getHall().getName(), payment.getAmount(),
					"Credit card", seatsInfo);
			log.debug("Sent payment success email to {}", user.getEmail());
		} catch (Exception e) {
			log.error("Failed to send payment success email for booking {}", booking.getId(), e);
		}
	}

	private void sendPaymentFailedEmail(Payment payment, Booking booking) {
		try {
			User user = booking.getUser();
			Session session = booking.getSession();
			String errorDescription = payment.getLiqpayErrorDescription() != null ? payment.getLiqpayErrorDescription()
					: "Payment error";
			emailService.sendPaymentFailedEmail(user.getEmail(), booking.getId().toString(),
					session.getMovie().getTitle(), session.getStartTime().format(DATE_FORMATTER), errorDescription);
			log.debug("Sent payment failed email to {}", user.getEmail());
		} catch (Exception e) {
			log.error("Failed to send payment failed email for booking {}", booking.getId(), e);
		}
	}

	private void sendRefundEmail(Payment payment, BigDecimal amount, String description) {
		try {
			Booking booking = payment.getBooking();
			User user = booking.getUser();
			Session session = booking.getSession();
			String seatsInfo = booking.getBookedSeats().stream()
					.map(seat -> String.format("Row %d, Seat %d", seat.getSeat().getRow(), seat.getSeat().getNumber()))
					.collect(java.util.stream.Collectors.joining(", "));
			emailService.sendRefundEmail(user.getEmail(), booking.getId().toString(), session.getMovie().getTitle(),
					session.getStartTime().format(DATE_FORMATTER), session.getHall().getName(), amount, seatsInfo,
					description);
			log.debug("Sent refund email to {}", user.getEmail());
		} catch (Exception e) {
			log.error("Failed to send refund email for payment {}", payment.getId(), e);
		}
	}

	private PaymentResponse buildPaymentResponse(Payment payment) {
		Booking booking = payment.getBooking();
		return PaymentResponse.builder().id(payment.getId()).bookingId(booking.getId())
				.bookingNumber(generateBookingNumber(booking)).userEmail(booking.getUser().getEmail())
				.movieTitle(booking.getSession().getMovie().getTitle()).sessionTime(booking.getSession().getStartTime())
				.hallName(booking.getSession().getHall().getName()).amount(payment.getAmount())
				.finalAmount(payment.getAmount()).status(payment.getStatus()).liqpayOrderId(payment.getLiqpayOrderId())
				.liqpayPaymentId(payment.getLiqpayPaymentId()).paymentTime(payment.getPaymentTime())
				.errorCode(payment.getLiqpayErrorCode()).errorDescription(payment.getLiqpayErrorDescription())
				.senderCardMask(payment.getLiqpaySenderCardMask()).createdAt(payment.getCreatedAt())
				.updatedAt(payment.getUpdatedAt()).build();
	}

	private String generateBookingNumber(Booking booking) {
		return String.format("BK-%d-%05d", booking.getCreatedAt().getYear(), booking.getId());
	}
}