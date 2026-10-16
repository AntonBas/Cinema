package ua.lviv.bas.cinema.payment.gateway;

import java.util.Map;

public record PaymentGatewayCheckResult(PaymentGatewayStatus status, Map<String, String> rawData) {
}
