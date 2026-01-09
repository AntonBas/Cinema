package ua.lviv.bas.cinema.controller.admin;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.service.booking.BookingService;

@Slf4j
@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@Tag(name = "Admin Bookings", description = "Administrative APIs for managing cinema bookings")
@SecurityRequirement(name = "bearerAuth")
public class AdminBookingController {

	private final BookingService bookingService;

	@GetMapping
	@Operation(summary = "Get all bookings", description = "Retrieves a paginated list of all bookings in the system")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<Page<BookingResponse>> getAllBookings(@PageableDefault(size = 20) Pageable pageable,
			@RequestParam(required = false) BookingStatus status) {

		log.info("Admin fetching all bookings with status: {}", status);
		Page<BookingResponse> bookings = bookingService.getAllBookings(pageable, status);
		return ResponseEntity.ok(bookings);
	}

	@GetMapping("/list")
	@Operation(summary = "Get all bookings list", description = "Retrieves a list of all bookings without pagination")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<List<BookingResponse>> getAllBookingsList() {
		log.info("Admin fetching all bookings list");
		List<BookingResponse> bookings = bookingService.getAllBookings();
		return ResponseEntity.ok(bookings);
	}

	@GetMapping("/{bookingId}")
	@Operation(summary = "Get booking details", description = "Retrieves detailed information about a specific booking")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Booking details retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Booking not found"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<BookingResponse> getBooking(@PathVariable Long bookingId) {

		log.info("Admin fetching booking ID: {}", bookingId);
		BookingResponse response = bookingService.getBookingById(bookingId);
		return ResponseEntity.ok(response);
	}

	@PostMapping("/{bookingId}/confirm")
	@Operation(summary = "Confirm a booking", description = "Manually confirms a pending booking")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Booking confirmed successfully"),
			@ApiResponse(responseCode = "404", description = "Booking not found"),
			@ApiResponse(responseCode = "400", description = "Booking cannot be confirmed"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<Void> confirmBooking(@PathVariable Long bookingId) {

		log.info("Admin confirming booking ID: {}", bookingId);
		bookingService.confirmBooking(bookingId);
		return ResponseEntity.ok().build();
	}

	@PostMapping("/{bookingId}/expire")
	@Operation(summary = "Expire a booking", description = "Manually expires a pending booking")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Booking expired successfully"),
			@ApiResponse(responseCode = "404", description = "Booking not found"),
			@ApiResponse(responseCode = "400", description = "Booking cannot be expired"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<Void> expireBooking(@PathVariable Long bookingId) {

		log.info("Admin expiring booking ID: {}", bookingId);
		bookingService.expireBooking(bookingId);
		return ResponseEntity.ok().build();
	}

	@DeleteMapping("/{bookingId}")
	@Operation(summary = "Cancel a booking", description = "Manually cancels a booking as administrator")
	@ApiResponses(value = { @ApiResponse(responseCode = "204", description = "Booking cancelled successfully"),
			@ApiResponse(responseCode = "404", description = "Booking not found"),
			@ApiResponse(responseCode = "400", description = "Booking cannot be cancelled"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<Void> cancelBooking(@PathVariable Long bookingId) {

		log.info("Admin cancelling booking ID: {}", bookingId);
		bookingService.cancelBooking(bookingId);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/session/{sessionId}")
	@Operation(summary = "Get bookings for session", description = "Retrieves all bookings for a specific cinema session")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
			@ApiResponse(responseCode = "404", description = "Session not found"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<List<BookingResponse>> getSessionBookings(@PathVariable Long sessionId) {

		log.info("Admin fetching bookings for session ID: {}", sessionId);
		List<BookingResponse> bookings = bookingService.getSessionBookings(sessionId);
		return ResponseEntity.ok(bookings);
	}

	@GetMapping("/user/{userId}")
	@Operation(summary = "Get user bookings", description = "Retrieves all bookings for a specific user")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Bookings retrieved successfully"),
			@ApiResponse(responseCode = "403", description = "Admin access required") })
	@PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
	public ResponseEntity<List<BookingResponse>> getUserBookings(@PathVariable Long userId,
			@RequestParam(required = false) BookingStatus status) {

		log.info("Admin fetching bookings for user ID: {} with status: {}", userId, status);
		List<BookingResponse> bookings = bookingService.getUserBookings(userId, status);
		return ResponseEntity.ok(bookings);
	}
}