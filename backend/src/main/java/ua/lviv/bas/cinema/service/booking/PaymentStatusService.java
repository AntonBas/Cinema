package ua.lviv.bas.cinema.service.booking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentNotFoundException;
import ua.lviv.bas.cinema.repository.booking.PaymentRepository;
import ua.lviv.bas.cinema.service.integration.payment.PaymentGatewayService;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStatusService {

	private final PaymentRepository paymentRepository;
	private final PaymentService paymentService;
	private final PaymentGatewayService paymentGatewayService;

	@Transactional(readOnly = true)
	public PaymentLiqPayDataResponse preparePaymentData(Long paymentId) {
		var payment = paymentRepository.findById(paymentId).orElseThrow(() -> new PaymentNotFoundException(paymentId));
		return paymentGatewayService.prepareLiqPayPaymentData(payment);
	}

	@Transactional
	public void handleCallback(String data, String signature) {
		var decodedData = paymentGatewayService.processCallback(data, signature);

		var orderId = decodedData.get("order_id");
		var status = decodedData.get("status");

		var payment = paymentRepository.findByLiqpayOrderId(orderId)
				.orElseThrow(() -> new PaymentNotFoundException(orderId));

		switch (status.toLowerCase()) {
		case "success":
		case "sandbox":
			paymentService.processSuccess(payment, decodedData);
			break;
		case "failure":
		case "error":
			paymentService.processFailure(payment, decodedData);
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
}