package ua.lviv.bas.cinema.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
	@ResponseStatus(HttpStatus.OK)
	@Operation(summary = "LiqPay callback")
	@ApiResponse(responseCode = "200", description = "Callback processed successfully")
	public String handleCallback(HttpServletRequest request) {
		var data = request.getParameter("data");
		var signature = request.getParameter("signature");

		log.info("POST /api/liqpay/callback");

		if (data == null || signature == null) {
			log.error("Missing required parameters: data or signature is null");
			return "Missing required parameters";
		}

		paymentStatusService.handleCallback(data, signature);
		return "OK";
	}
}