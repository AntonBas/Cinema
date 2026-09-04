package ua.lviv.bas.cinema.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.payment.dto.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.financial.payment.PaymentAccessDeniedException;
import ua.lviv.bas.cinema.payment.repository.PaymentRepository;
import ua.lviv.bas.cinema.payment.service.PaymentGatewayService;
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

        var payment = paymentRepository.findByLiqpayOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Payment", orderId));

        switch (status == null ? "" : status.toLowerCase()) {
        case "success":
        case "sandbox":
            paymentService.processSuccess(payment, decodedData);
            break;
        case "failure":
        case "error":
            paymentService.processFailure(payment, decodedData);
            break;
        case "wait_secure":
            paymentService.markProcessing(payment);
            break;
        default:
            log.warn("Unknown payment status: {} for payment {}", status, payment.getId());
            paymentService.processFailure(payment, decodedData);
        }
    }
}