package ua.lviv.bas.cinema.payment.service;

import java.util.Map;

public record PaymentGatewayCheckResult(PaymentGatewayStatus status, Map<String, String> rawData) {
}
