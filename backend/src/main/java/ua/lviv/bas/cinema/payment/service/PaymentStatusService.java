package ua.lviv.bas.cinema.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.payment.dto.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.user.domain.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentStatusService {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final PaymentGatewayService paymentGatewayService;

    @Transactional(readOnly = true)
    public PaymentLiqPayDataResponse preparePaymentData(Long paymentId, User user) {
        var payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", paymentId));

        if (!payment.getBooking().getUser().getId().equals(user.getId())) {
            throw new PaymentAccessDeniedException(paymentId, user.getId());
        }

        return paymentGatewayService.prepareLiqPayPaymentData(payment);
    }

    @Transactional
    public void handleCallback(String data, String signature) {
        var decodedData = paymentGatewayService.processCallback(data, signature);

        var orderId = decodedData.get("order_id");
        var status = decodedData.get("status");

        log.info("Received LiqPay callback for order {} with status {}", orderId, status);

        var payment = paymentRepository.findByLiqpayOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", orderId));

        if (status == null || status.isBlank()) {
            log.warn("LiqPay callback for payment {} has no status, ignoring it", payment.getId());
            return;
        }

        switch (status.toLowerCase()) {
            case "success", "sandbox" -> paymentService.processSuccess(payment, decodedData);
            case "failure", "error" -> paymentService.processFailure(payment, decodedData);
            default -> {
                log.info("LiqPay reported intermediate status {} for payment {}, marking it PROCESSING", status,
                        payment.getId());
                paymentService.markProcessing(payment);
            }
        }
    }
}