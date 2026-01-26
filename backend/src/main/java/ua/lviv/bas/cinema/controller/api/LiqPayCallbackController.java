package ua.lviv.bas.cinema.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.dto.payment.request.LiqPayCallbackRequest;
import ua.lviv.bas.cinema.service.booking.PaymentService;

@Slf4j
@RestController
@RequestMapping("/api/liqpay")
@RequiredArgsConstructor
@Tag(name = "LiqPay Callback", description = "Callback endpoint for LiqPay payment gateway")
public class LiqPayCallbackController {

	private final PaymentService paymentService;

	@PostMapping("/callback")
	@Operation(summary = "LiqPay callback endpoint", description = "Receives payment notifications from LiqPay")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Callback processed successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid callback data") })
	public ResponseEntity<String> handleLiqPayCallback(@RequestParam("data") String data,
			@RequestParam("signature") String signature) {

		log.info("=== LIQPAY CALLBACK RECEIVED ===");
		log.info("Data length: {}", data != null ? data.length() : 0);
		log.info("Signature present: {}", signature != null && !signature.isEmpty());

		LiqPayCallbackRequest callbackRequest = new LiqPayCallbackRequest();
		callbackRequest.setData(data);
		callbackRequest.setSignature(signature);

		paymentService.processLiqPayCallback(callbackRequest);
		return ResponseEntity.ok("OK");
	}
}