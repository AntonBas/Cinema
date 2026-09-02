package ua.lviv.bas.cinema.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.lviv.bas.cinema.config.properties.RefundPolicyConfig;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.service.booking.RefundService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
@Tag(name = "Refund", description = "Ticket refund operations")
@SecurityRequirement(name = "bearerAuth")
public class RefundController {

    private final RefundService refundService;
    private final RefundPolicyConfig refundPolicyConfig;

    @RateLimit(value = 3, duration = 10, key = "user")
    @PostMapping
    @Operation(summary = "Refund a ticket")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or ticket not refundable"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PreAuthorize("isAuthenticated()")
    public RefundResponse refund(@Valid @RequestBody RefundRequest request,
                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        log.info("POST /api/refunds - user: {}, ticket: {}", user.getId(), request.ticketId());
        return refundService.refund(request, user.getId());
    }

    @PostMapping("/preview")
    @Operation(summary = "Preview a ticket refund",
            description = "Calculates the refund amount, fee and policy details without processing the refund")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund preview calculated successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    @PreAuthorize("isAuthenticated()")
    public RefundPreviewResponse preview(@Valid @RequestBody RefundPreviewRequest request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        var user = userDetails.getUser();
        log.info("POST /api/refunds/preview - user: {}, ticket: {}", user.getId(), request.ticketId());
        return refundService.getPreview(request, user.getId());
    }

    @GetMapping("/policy")
    @Operation(summary = "Get refund policy", description = "Returns the refund rules, processing time and contact email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Refund policy retrieved successfully")
    })
    public ResponseEntity<Map<String, Object>> getPolicy() {
        return ResponseEntity.ok(Map.of(
                "rules", refundPolicyConfig.getRules(),
                "processingTime", refundPolicyConfig.getProcessingTime(),
                "contactEmail", refundPolicyConfig.getContactEmail()
        ));
    }
}