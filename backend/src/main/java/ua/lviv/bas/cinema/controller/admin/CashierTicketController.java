package ua.lviv.bas.cinema.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ua.lviv.bas.cinema.dto.ticket.response.TicketCashierResponse;
import ua.lviv.bas.cinema.service.ticket.TicketService;

@Slf4j
@RestController
@RequestMapping("/api/admin/ticket")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CASHIER', 'ADMIN')")
@Tag(name = "Cashier Ticket", description = "Endpoint for cashier to scan and validate tickets")
@SecurityRequirement(name = "bearerAuth")
public class CashierTicketController {

    private final TicketService ticketService;

    @GetMapping("/{uniqueCode}")
    @Operation(summary = "Get ticket info by unique code", description = "Returns ticket details for cashier review before validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketCashierResponse> getTicket(@PathVariable String uniqueCode) {
        return ResponseEntity.ok(ticketService.getTicketForCashier(uniqueCode));
    }

    @PostMapping("/{uniqueCode}/validate")
    @Operation(summary = "Validate and mark ticket as used", description = "Validates ticket and changes status to USED")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ticket validated"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Ticket not found")
    })
    public ResponseEntity<TicketCashierResponse> validateTicket(@PathVariable String uniqueCode) {
        return ResponseEntity.ok(ticketService.validate(uniqueCode));
    }
}