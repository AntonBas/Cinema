package ua.lviv.bas.cinema.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
    @Operation(summary = "Handle LiqPay callback", description = "Receives payment status updates from LiqPay")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Callback processed successfully"),
            @ApiResponse(responseCode = "400", description = "Missing required parameters")
    })
    public String handleCallback(@RequestParam String data, @RequestParam String signature) {
        log.info("POST /api/liqpay/callback");

        if (data == null || signature == null) {
            log.error("Missing required parameters: data or signature is null");
            return "Missing required parameters";
        }

        paymentStatusService.handleCallback(data, signature);
        return "OK";
    }
}