package ua.lviv.bas.cinema.service.booking.management;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.BookingStatus;
import ua.lviv.bas.cinema.domain.booking.status.ReservationStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingOperationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.mapper.booking.BookingMapper;
import ua.lviv.bas.cinema.repository.booking.BookingRepository;
import ua.lviv.bas.cinema.repository.booking.SeatReservationRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.booking.creation.BookingValidator;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingManagementService {
	private final BookingRepository bookingRepository;
	private final SeatReservationRepository seatReservationRepository;
	private final BookingMapper bookingMapper;
	private final BookingValidator bookingValidator;
	private final BonusService bonusService;
	private final AuditService auditService;

	public BookingResponse getBookingById(Long bookingId, User user) {
		Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
				.orElseThrow(() -> new BookingNotFoundException(bookingId));
		return bookingMapper.toBookingResponse(booking);
	}

	public Page<BookingResponse> getUserBookings(Long userId, BookingStatus status, Pageable pageable) {
		Page<Booking> bookings;
		if (status != null) {
			bookings = bookingRepository.findByUserIdAndStatusOrderByCreatedDateDesc(userId, status, pageable);
		} else {
			bookings = bookingRepository.findByUserIdOrderByCreatedDateDesc(userId, pageable);
		}
		return bookings.map(bookingMapper::toBookingResponse);
	}

	@Transactional
	public void cancelBooking(Long bookingId, User user) {
		Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
				.orElseThrow(() -> new BookingNotFoundException(bookingId));

		if (!bookingValidator.canBookingBeCancelled(booking)) {
			throw BookingValidationException.cannotCancel();
		}

		BookingStatus oldStatus = booking.getStatus();
		booking.setStatus(BookingStatus.CANCELLED);

		booking.getSeatReservations().forEach(sr -> {
			sr.setStatus(ReservationStatus.EXPIRED);
			sr.setBooking(null);
		});

		seatReservationRepository.saveAll(booking.getSeatReservations());

		if (booking.getBonusPointsUsed() != null && booking.getBonusPointsUsed() > 0) {
			bonusService.refundPoints(booking);
		}

		bookingRepository.save(booking);
		log.info("Cancelled booking {} for user {}", bookingId, user.getId());

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("status", oldStatus);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("status", BookingStatus.CANCELLED);

		auditService.logChange("Booking", bookingId, "Booking #" + bookingId, AuditAction.CANCELLED, oldDetails,
				newDetails);
	}

	@Transactional
	public void confirmBooking(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException(bookingId));

		if (booking.getStatus() != BookingStatus.PENDING) {
			throw BookingOperationException.onlyPendingCanBeConfirmed();
		}

		BookingStatus oldStatus = booking.getStatus();
		booking.setStatus(BookingStatus.CONFIRMED);
		booking.getSeatReservations().forEach(sr -> sr.setStatus(ReservationStatus.CONFIRMED));
		bookingRepository.save(booking);

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("status", oldStatus);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("status", BookingStatus.CONFIRMED);

		auditService.logChange("Booking", bookingId, "Booking #" + bookingId, AuditAction.CONFIRMED, oldDetails,
				newDetails);
	}

	@Transactional(readOnly = true)
	public Integer getAvailableBonusPointsForBooking(Long userId, BigDecimal bookingTotalPrice) {
		return bonusService.getAvailablePoints(userId, bookingTotalPrice);
	}
}