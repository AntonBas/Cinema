package ua.lviv.bas.cinema.controller.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.service.ticket.TicketService;

@Slf4j
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Tickets", description = "Ticket management APIs")
@SecurityRequirement(name = "bearerAuth")
public class TicketController {

    private final TicketService ticketService;

    @RateLimit(value = 20, duration = 1, key = "user")
    @GetMapping
    @Operation(summary = "Get user tickets")
    public PageResponse<TicketResponse> getTickets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) String movieTitle,
            @PageableDefault Pageable pageable
    ) {
        var user = userDetails.getUser();
        var page = ticketService.getTickets(user, status, movieTitle, pageable);
        return PageResponse.from(page);
    }

    @RateLimit(value = 30, duration = 1, key = "user")
    @GetMapping("/code/{ticketCode}")
    @Operation(summary = "Get ticket by code")
    public TicketResponse getTicket(@PathVariable String ticketCode,
                                    @AuthenticationPrincipal CustomUserDetails userDetails) {

        var user = userDetails.getUser();
        log.info("GET /api/tickets/code/{} - user: {}", ticketCode, user.getId());
        return ticketService.getTicket(ticketCode, user);
    }

    @RateLimit(value = 20, duration = 1, key = "user")
    @GetMapping(value = "/code/{ticketCode}/qr", produces = MediaType.IMAGE_PNG_VALUE)
    @Operation(summary = "Get ticket QR code")
    public byte[] getQR(@PathVariable String ticketCode) {
        log.info("GET /api/tickets/code/{}/qr", ticketCode);
        return ticketService.generateQR(ticketCode);
    }

    @RateLimit(value = 20, duration = 1)
    @PostMapping("/code/{ticketCode}/validate")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Validate ticket")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public void validate(@PathVariable String ticketCode) {
        log.info("POST /api/tickets/code/{}/validate", ticketCode);
        ticketService.validate(ticketCode);
    }
}