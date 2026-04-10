package ua.lviv.bas.cinema.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.ratelimit.RateLimit;
import ua.lviv.bas.cinema.config.security.user.CustomUserDetails;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.service.booking.BookingService;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "APIs for managing cinema bookings")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

	private final BookingService bookingService;

	@RateLimit(value = 5, duration = 1, key = "user")
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@Operation(summary = "Create a new booking")
	@ApiResponses(value = { @ApiResponse(responseCode = "201", description = "Booking created successfully"),
			@ApiResponse(responseCode = "400", description = "Invalid request data"),
			@ApiResponse(responseCode = "404", description = "Session or seat not found"),
			@ApiResponse(responseCode = "409", description = "Seat not available") })
	@PreAuthorize("isAuthenticated()")
	public BookingResponse createBooking(@Valid @RequestBody BookingCreateRequest request,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		var user = userDetails.getUser();
		log.info("POST /api/bookings - user: {}, session: {}", user.getId(), request.sessionId());
		return bookingService.createBooking(request, user);
	}

	@RateLimit(value = 20, duration = 1, key = "user")
	@GetMapping("/{bookingId}")
	@Operation(summary = "Get booking details")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Booking retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Booking not found") })
	@PreAuthorize("isAuthenticated()")
	public BookingResponse getBooking(@PathVariable Long bookingId,
			@AuthenticationPrincipal CustomUserDetails userDetails) {
		var user = userDetails.getUser();
		log.info("GET /api/bookings/{} - user: {}", bookingId, user.getId());
		return bookingService.getBooking(bookingId, user);
	}

	@RateLimit(value = 3, duration = 1, key = "user")
	@DeleteMapping("/{bookingId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@Operation(summary = "Cancel a booking")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Booking cancelled successfully"),
			@ApiResponse(responseCode = "400", description = "Booking cannot be cancelled"),
			@ApiResponse(responseCode = "404", description = "Booking not found") })
	@PreAuthorize("isAuthenticated()")
	public void cancelBooking(@PathVariable Long bookingId, @AuthenticationPrincipal CustomUserDetails userDetails) {
		var user = userDetails.getUser();
		log.info("DELETE /api/bookings/{} - user: {}", bookingId, user.getId());
		bookingService.cancelBooking(bookingId, user);
	}
}