package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.PaymentMethod;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.PaymentProcessingException;
import ua.lviv.bas.cinema.mapper.PaymentMapper;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.PaymentRepository;
import ua.lviv.bas.cinema.service.notification.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

	private final PaymentRepository paymentRepository;
	private final BookingRepository bookingRepository;
	private final PaymentMapper paymentMapper;
	private final EmailService emailService;
	private final TicketService ticketService;
	private final BookingService bookingService;

	@Value("${payment.liqpay.public_key}")
	private String liqpayPublicKey;

	@Value("${payment.liqpay.private_key}")
	private String liqpayPrivateKey;

	@Value("${app.frontend.url}")
	private String frontendUrl;

	@Value("${payment.bonus.point_value:1.0}")
	private BigDecimal bonusPointValue;

	public PaymentResponse createPayment(PaymentCreateRequest request, User user) {
		Booking booking = bookingRepository.findByIdAndUserId(request.getBookingId(), user.getId())
				.orElseThrow(() -> new BookingNotFoundException(request.getBookingId()));

		validateBookingForPayment(booking);

		BigDecimal totalAmount = bookingService.calculateTotalPrice(booking);
		BigDecimal bonusDiscount = calculateBonusDiscount(request.getBonusPointsToUse());
		BigDecimal finalAmount = totalAmount.subtract(bonusDiscount).max(BigDecimal.ZERO);

		Payment payment = Payment.builder().booking(booking).amount(totalAmount)
				.bonusPointsUsed(request.getBonusPointsToUse()).paymentMethod(request.getPaymentMethod())
				.status(PaymentStatus.PENDING).transactionId(generateTransactionId()).build();

		Payment savedPayment = paymentRepository.save(payment);

		booking.setStatus(BookingStatus.CONFIRMED);
		bookingRepository.save(booking);

		String paymentUrl = null;
		String paymentFormData = null;

		if (request.getPaymentMethod() == PaymentMethod.CARD) {
			Map<String, String> paymentData = prepareLiqPayPayment(savedPayment, finalAmount);
			paymentUrl = paymentData.get("paymentUrl");
			paymentFormData = paymentData.get("paymentFormData");
		}

		PaymentResponse response = paymentMapper.toPaymentResponse(savedPayment);
		response.setFinalAmount(finalAmount);
		response.setPaymentUrl(paymentUrl);
		return response;
	}

	public void processPaymentCallback(String transactionId, String status, Map<String, String> callbackData) {
		Payment payment = paymentRepository.findByTransactionId(transactionId)
				.orElseThrow(() -> new PaymentProcessingException("Payment not found"));

		if (!verifyPaymentSignature(callbackData)) {
			throw new PaymentProcessingException("Invalid payment signature");
		}

		Booking booking = payment.getBooking();

		if ("success".equalsIgnoreCase(status)) {
			payment.setStatus(PaymentStatus.COMPLETED);
			payment.setPaymentTime(LocalDateTime.now());

			booking.setStatus(BookingStatus.CONFIRMED);
			bookingService.confirmBooking(booking.getId());

			ticketService.createTicketsForBooking(booking, payment);

			sendPaymentSuccessEmail(booking.getUser(), booking, payment);
			ticketService.sendTicketsToUser(booking);

		} else if ("failure".equalsIgnoreCase(status) || "error".equalsIgnoreCase(status)) {
			payment.setStatus(PaymentStatus.FAILED);
			booking.setStatus(BookingStatus.PENDING);

			sendPaymentFailedEmail(booking.getUser(), booking, callbackData.get("err_description"));
		} else if ("wait_secure".equalsIgnoreCase(status)) {
			payment.setStatus(PaymentStatus.PROCESSING);
		}

		paymentRepository.save(payment);
		bookingRepository.save(booking);
	}

	public PaymentResponse getPaymentStatus(Long paymentId, User user) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentProcessingException("Payment not found"));

		if (!payment.getBooking().getUser().getId().equals(user.getId())) {
			throw new PaymentProcessingException("Access denied to payment");
		}

		PaymentResponse response = paymentMapper.toPaymentResponse(payment);
		response.setFinalAmount(calculateFinalAmount(payment));

		return response;
	}

	public void retryPayment(Long paymentId, User user) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentProcessingException("Payment not found"));

		if (!payment.getBooking().getUser().getId().equals(user.getId())) {
			throw new PaymentProcessingException("Access denied to payment");
		}

		if (payment.getStatus() != PaymentStatus.FAILED) {
			throw new PaymentProcessingException("Only failed payments can be retried");
		}

		payment.setStatus(PaymentStatus.PENDING);
		payment.setTransactionId(generateTransactionId());
		paymentRepository.save(payment);

		Booking booking = payment.getBooking();
		booking.setStatus(BookingStatus.PENDING);
		bookingRepository.save(booking);
	}

	private void validateBookingForPayment(Booking booking) {
		if (booking.getStatus() != BookingStatus.PENDING) {
			throw PaymentProcessingException.bookingNotPending();
		}

		if (booking.getExpiresAt().isBefore(LocalDateTime.now())) {
			throw PaymentProcessingException.bookingExpired();
		}

		paymentRepository.findByBookingId(booking.getId()).ifPresent(existingPayment -> {
			if (existingPayment.getStatus() == PaymentStatus.PENDING
					|| existingPayment.getStatus() == PaymentStatus.PROCESSING) {
				throw PaymentProcessingException.paymentInProgress();
			}
		});
	}

	private BigDecimal calculateBonusDiscount(Integer bonusPoints) {
		if (bonusPoints == null || bonusPoints == 0) {
			return BigDecimal.ZERO;
		}
		return bonusPointValue.multiply(BigDecimal.valueOf(bonusPoints));
	}

	private BigDecimal calculateFinalAmount(Payment payment) {
		BigDecimal bonusDiscount = calculateBonusDiscount(payment.getBonusPointsUsed());
		return payment.getAmount().subtract(bonusDiscount).max(BigDecimal.ZERO);
	}

	private String generateTransactionId() {
		return "txn_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
	}

	private Map<String, String> prepareLiqPayPayment(Payment payment, BigDecimal finalAmount) {
		Map<String, String> params = new HashMap<>();
		params.put("public_key", liqpayPublicKey);
		params.put("version", "3");
		params.put("action", "pay");
		params.put("amount", finalAmount.toString());
		params.put("currency", "UAH");
		params.put("description", "Payment for booking " + payment.getBooking().getId());
		params.put("order_id", payment.getTransactionId());
		params.put("result_url", frontendUrl + "/booking/" + payment.getBooking().getId() + "/success");
		params.put("server_url", frontendUrl + "/api/payments/callback/liqpay");
		params.put("language", "uk");

		String data = Base64.getEncoder().encodeToString(new com.google.gson.Gson().toJson(params).getBytes());
		String signature = generateLiqPaySignature(data);

		Map<String, String> result = new HashMap<>();
		result.put("paymentUrl", "https://www.liqpay.ua/api/3/checkout");
		result.put("paymentFormData", "data=" + data + "&signature=" + signature);

		return result;
	}

	private String generateLiqPaySignature(String data) {
		try {
			String str = liqpayPrivateKey + data + liqpayPrivateKey;
			java.security.MessageDigest sha1 = java.security.MessageDigest.getInstance("SHA-1");
			byte[] digest = sha1.digest(str.getBytes());
			return Base64.getEncoder().encodeToString(digest);
		} catch (Exception e) {
			throw new PaymentProcessingException("Failed to generate payment signature");
		}
	}

	private boolean verifyPaymentSignature(Map<String, String> callbackData) {
		String receivedSignature = callbackData.get("signature");
		String data = callbackData.get("data");

		if (receivedSignature == null || data == null) {
			return false;
		}

		String expectedSignature = generateLiqPaySignature(data);
		return expectedSignature.equals(receivedSignature);
	}

	private void sendPaymentSuccessEmail(User user, Booking booking, Payment payment) {
		try {
			String bookingNumber = bookingService.generateBookingNumber(booking);
			String movieTitle = booking.getSession().getMovie().getTitle();
			String sessionTime = bookingService.formatDateTime(booking.getSession().getStartTime());
			String hallName = booking.getSession().getHall().getName();
			BigDecimal amount = calculateFinalAmount(payment);
			String paymentMethod = payment.getPaymentMethod().toString();

			StringBuilder seatInfo = new StringBuilder();
			for (BookedSeat bookedSeat : booking.getBookedSeats()) {
				if (seatInfo.length() > 0)
					seatInfo.append(", ");
				seatInfo.append(String.format("Row %d Seat %d", bookedSeat.getSeat().getRow(),
						bookedSeat.getSeat().getNumber()));
			}

			String qrCodeUrl = frontendUrl + "/api/tickets/" + booking.getId() + "/qr";

			emailService.sendPaymentSuccessWithTickets(user.getEmail(), bookingNumber, movieTitle, sessionTime,
					hallName, amount, paymentMethod, qrCodeUrl, seatInfo.toString());
		} catch (Exception e) {
			log.error("Failed to send payment success email: {}", e.getMessage());
		}
	}

	private void sendPaymentFailedEmail(User user, Booking booking, String errorMessage) {
		try {
			String bookingNumber = bookingService.generateBookingNumber(booking);
			String movieTitle = booking.getSession().getMovie().getTitle();
			String sessionTime = bookingService.formatDateTime(booking.getSession().getStartTime());

			emailService.sendPaymentFailed(user.getEmail(), bookingNumber, movieTitle, sessionTime,
					errorMessage != null ? errorMessage : "Payment failed");
		} catch (Exception e) {
			log.error("Failed to send payment failed email: {}", e.getMessage());
		}
	}
}