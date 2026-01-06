package ua.lviv.bas.cinema.dto.payment.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "LiqPay callback request")
public class LiqPayCallbackRequest {

	@JsonProperty("payment_id")
	@Schema(description = "LiqPay payment ID", example = "lp_123456789")
	private String paymentId;

	@JsonProperty("order_id")
	@Schema(description = "Order ID", example = "ORDER_ABC123")
	private String orderId;

	@JsonProperty("transaction_id")
	@Schema(description = "Transaction ID", example = "txn_123456")
	private String transactionId;

	@Schema(description = "Status", example = "success")
	private String status;

	@JsonProperty("sender_card_mask")
	@Schema(description = "Masked card number", example = "****4832")
	private String senderCardMask;

	@JsonProperty("err_code")
	@Schema(description = "Error code", example = "error_validation")
	private String errorCode;

	@JsonProperty("err_description")
	@Schema(description = "Error description", example = "Invalid signature")
	private String errorDescription;
}