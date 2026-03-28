package ua.lviv.bas.cinema.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.service.booking.creation.BookingCreationService;
import ua.lviv.bas.cinema.service.booking.management.BookingManagementService;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "APIs for managing cinema bookings")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {
	private final BookingCreationService bookingCreationService;
	private final BookingManagementService bookingManagementService;

	@PostMapping
	@Operation(summary = "Create a new booking", description = "Creates a new booking for a cinema session with selected seats and ticket types")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Booking created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data or seat not available"),
			@ApiResponse(responseCode = "404", description = "Session or seat not found"),
			@ApiResponse(responseCode = "409", description = "Booking conflict or session not available") })
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingCreateRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Creating new booking for user ID: {}, session ID: {}", user.getId(), request.sessionId());
		BookingResponse response = bookingCreationService.createBooking(request, user);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@GetMapping("/{bookingId}")
	@Operation(summary = "Get booking details", description = "Retrieves detailed information about a specific booking")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Booking details retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Booking not found"),
			@ApiResponse(responseCode = "403", description = "Access denied to booking") })
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<BookingResponse> getBooking(@PathVariable Long bookingId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Fetching booking ID: {} for user ID: {}", bookingId, user.getId());
		BookingResponse response = bookingManagementService.getBookingById(bookingId, user);
		return ResponseEntity.ok(response);
	}

	@DeleteMapping("/{bookingId}")
	@Operation(summary = "Cancel a booking", description = "Cancels a pending or confirmed booking if allowed by business rules")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Booking cancelled successfully"),
			@ApiResponse(responseCode = "400", description = "Booking cannot be cancelled"),
			@ApiResponse(responseCode = "404", description = "Booking not found"),
			@ApiResponse(responseCode = "403", description = "Access denied to booking") })
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		User user = userDetails.getUser();
		log.info("Cancelling booking ID: {} for user ID: {}", bookingId, user.getId());
		bookingManagementService.cancelBooking(bookingId, user);
		return ResponseEntity.noContent().build();
	}
}