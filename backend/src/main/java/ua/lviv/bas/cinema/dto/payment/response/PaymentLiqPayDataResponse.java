package ua.lviv.bas.cinema.dto.payment.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "LiqPay payment data for frontend")
public class PaymentLiqPayDataResponse {

	@Schema(description = "Encoded data for LiqPay", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
	private String data;

	@Schema(description = "Signature for LiqPay", example = "wBqNnqJ5k8D6eB3vQ2sR7tY9uI0oP4aZ1x...")
	private String signature;

	@Schema(description = "Payment URL", example = "https://www.liqpay.ua/api/3/checkout")
	private String paymentUrl;

	@Schema(description = "LiqPay order ID", example = "ORDER_ABC123")
	private String liqpayOrderId;
}