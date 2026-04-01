package ua.lviv.bas.cinema.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.service.booking.PaymentStatusService;

@Slf4j
@RestController
@RequestMapping("/api/liqpay")
@RequiredArgsConstructor
@Tag(name = "LiqPay Callback", description = "Callback endpoint for LiqPay payment gateway")
public class LiqPayCallbackController {
	private final PaymentStatusService paymentStatusService;

	@PostMapping("/callback")
	@Operation(summary = "LiqPay callback endpoint", description = "Receives payment notifications from LiqPay")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Callback processed successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid callback data") })
	public ResponseEntity<String> handleLiqPayCallback(HttpServletRequest request) {
		String data = request.getParameter("data");
		String signature = request.getParameter("signature");

		log.info("Processing LiqPay callback - data: {}, signature: {}", data, signature);

		if (data == null || signature == null) {
			log.error("Missing required parameters: data or signature is null");
			return ResponseEntity.badRequest().body("Missing required parameters");
		}

		paymentStatusService.handleLiqPayCallback(data, signature);
		return ResponseEntity.ok("OK");
	}
}