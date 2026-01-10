package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
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

	@Value("${payment.liqpay.server_url}")
	private String liqpayServerUrl;

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
		log.info("Processing LiqPay callback: orderId={}, status={}", callbackRequest.getOrderId(),
				callbackRequest.getStatus());

		Payment payment = paymentRepository.findByLiqpayOrderId(callbackRequest.getOrderId())
				.orElseThrow(() -> new PaymentNotFoundException(callbackRequest.getOrderId()));

		Booking booking = payment.getBooking();
		String status = callbackRequest.getStatus().toLowerCase();

		switch (status) {
		case "success":
			handleSuccessfulPayment(payment, booking, callbackRequest);
			break;
		case "failure":
		case "error":
			handleFailedPayment(payment, booking, callbackRequest);
			break;
		case "wait_secure":
			handlePendingPayment(payment);
			break;
		case "sandbox":
			log.info("Sandbox payment detected for order {}", callbackRequest.getOrderId());
			handleSuccessfulPayment(payment, booking, callbackRequest);
			break;
		default:
			log.warn("Unknown payment status: {}", status);
			payment.setStatus(PaymentStatus.FAILED);
		}

		updatePaymentWithCallbackData(payment, callbackRequest);
		paymentRepository.save(payment);
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
			params.put("result_url", frontendUrl + "/booking/" + booking.getId() + "/success");
			params.put("server_url", liqpayServerUrl);
			params.put("language", "uk");
			params.put("sender_first_name", booking.getUser().getFirstName());
			params.put("sender_last_name", booking.getUser().getLastName());
			params.put("sender_email", booking.getUser().getEmail());

			String jsonData = gson.toJson(params);
			String data = Base64.getEncoder().encodeToString(jsonData.getBytes());
			String signature = generateLiqPaySignature(data);

			log.debug("Prepared LiqPay data for payment {}: orderId={}", payment.getId(), payment.getLiqpayOrderId());

			return PaymentLiqPayDataResponse.builder().data(data).signature(signature)
					.paymentUrl("https://www.liqpay.ua/api/3/checkout").liqpayOrderId(payment.getLiqpayOrderId())
					.build();

		} catch (Exception e) {
			log.error("Failed to prepare LiqPay payment data for payment {}", payment.getId(), e);
			throw new PaymentProcessingException("Failed to prepare payment data");
		}
	}

	private void handleSuccessfulPayment(Payment payment, Booking booking, LiqPayCallbackRequest callbackRequest) {
		log.info("Processing successful payment {} for booking {}", payment.getId(), booking.getId());

		payment.setStatus(PaymentStatus.SUCCESS);
		payment.setPaymentTime(LocalDateTime.now());
		payment.setLiqpayPaymentId(callbackRequest.getPaymentId());
		payment.setLiqpayTransactionId(callbackRequest.getTransactionId());
		payment.setLiqpaySenderCardMask(callbackRequest.getSenderCardMask());

		bookingService.confirmBooking(booking.getId());

		List<Ticket> tickets = ticketService.createTicketsForBooking(booking, payment);

		Integer pointsToAccrue = bonusService.calculateAccruedPointsForAmount(booking.getFinalPrice());
		if (pointsToAccrue != null && pointsToAccrue > 0) {
			bonusService.accrueBonusPointsForPayment(booking.getUser().getId(), pointsToAccrue, booking, payment);
		}

		ticketService.sendTicketsToUser(booking);
		sendPaymentSuccessEmail(payment, booking, tickets);

		log.info("Payment {} completed successfully for booking {}", payment.getId(), booking.getId());
	}

	private void handleFailedPayment(Payment payment, Booking booking, LiqPayCallbackRequest callbackRequest) {
		log.warn("Payment {} failed for booking {}: errorCode={}, errorDescription={}", payment.getId(),
				booking.getId(), callbackRequest.getErrorCode(), callbackRequest.getErrorDescription());

		payment.setStatus(PaymentStatus.FAILED);
		payment.setLiqpayErrorCode(callbackRequest.getErrorCode());
		payment.setLiqpayErrorDescription(callbackRequest.getErrorDescription());

		sendPaymentFailedEmail(payment, booking);
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

	private void updatePaymentWithCallbackData(Payment payment, LiqPayCallbackRequest callbackRequest) {
		payment.setLiqpayPaymentId(callbackRequest.getPaymentId());
		payment.setLiqpayTransactionId(callbackRequest.getTransactionId());
		payment.setLiqpayErrorCode(callbackRequest.getErrorCode());
		payment.setLiqpayErrorDescription(callbackRequest.getErrorDescription());
		payment.setLiqpaySenderCardMask(callbackRequest.getSenderCardMask());
		payment.setUpdatedAt(LocalDateTime.now());
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