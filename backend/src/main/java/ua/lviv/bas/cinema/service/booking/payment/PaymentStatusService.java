package ua.lviv.bas.cinema.service.booking.payment;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.request.LiqPayCallbackRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.repository.booking.PaymentRepository;
import ua.lviv.bas.cinema.service.integration.payment.PaymentGatewayService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStatusService {
	private final PaymentRepository paymentRepository;
	private final PaymentProcessingService paymentProcessingService;
	private final PaymentGatewayService paymentGatewayService;

	public PaymentLiqPayDataResponse preparePaymentData(Long paymentId) {
		Payment payment = paymentRepository.findById(paymentId)
				.orElseThrow(() -> new PaymentNotFoundException(paymentId));

		return paymentGatewayService.prepareLiqPayPaymentData(payment);
	}

	@Transactional
	public void handleLiqPayCallback(String data, String signature) {
		Map<String, String> decodedData = paymentGatewayService.processCallback(data, signature);

		String orderId = decodedData.get("order_id");
		String status = decodedData.get("status");

		Payment payment = paymentRepository.findByLiqpayOrderId(orderId)
				.orElseThrow(() -> new PaymentNotFoundException(orderId));

		switch (status.toLowerCase()) {
		case "success":
		case "sandbox":
			paymentProcessingService.processSuccessfulPayment(payment, decodedData);
			break;
		case "failure":
		case "error":
			paymentProcessingService.processFailedPayment(payment, decodedData);
			break;
		case "wait_secure":
			payment.setStatus(PaymentStatus.PROCESSING);
			paymentRepository.save(payment);
			break;
		default:
			log.warn("Unknown payment status: {} for payment {}", status, payment.getId());
			payment.setStatus(PaymentStatus.FAILED);
			paymentRepository.save(payment);
		}
	}

	@Transactional
	public void handleLiqPayCallback(LiqPayCallbackRequest callbackRequest) {
		handleLiqPayCallback(callbackRequest.data(), callbackRequest.signature());
	}
}