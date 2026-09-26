package ua.lviv.bas.cinema.booking.controller.admin;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.dto.response.AdminBookingDetailsResponse;
import ua.lviv.bas.cinema.booking.dto.response.AdminBookingListResponse;
import ua.lviv.bas.cinema.booking.service.AdminBookingService;
import ua.lviv.bas.cinema.common.PageResponse;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@Tag(name = "Admin Booking Management", description = "Endpoints for browsing bookings and their payments")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @GetMapping
    @Operation(summary = "Get bookings with filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter or sort property"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    public PageResponse<AdminBookingListResponse> getBookings(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @PageableDefault Pageable pageable) {
        return PageResponse.from(adminBookingService.getBookings(query, status, paymentStatus, sessionId, userId,
                dateFrom, dateTo, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking details with tickets and payment")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Booking found"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Booking not found")
    })
    public AdminBookingDetailsResponse getBooking(@PathVariable Long id) {
        return adminBookingService.getBooking(id);
    }
}
