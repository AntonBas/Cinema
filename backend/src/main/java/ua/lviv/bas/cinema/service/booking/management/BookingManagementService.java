package ua.lviv.bas.cinema.service.booking.management;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.ReservationStatus;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.exception.domain.booking.BookingNotFoundException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingOperationException;
import ua.lviv.bas.cinema.exception.domain.booking.BookingValidationException;
import ua.lviv.bas.cinema.mapper.BookingMapper;
import ua.lviv.bas.cinema.repository.BookingRepository;
import ua.lviv.bas.cinema.repository.SeatReservationRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.booking.creation.BookingValidator;

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

	public BookingResponse getBookingById(Long bookingId, User user) {
		Booking booking = bookingRepository.findByIdAndUserId(bookingId, user.getId())
				.orElseThrow(() -> new BookingNotFoundException(bookingId));
		return bookingMapper.toBookingResponse(booking);
	}

	public Page<BookingResponse> getUserBookings(Long userId, BookingStatus status, Pageable pageable) {
		Page<Booking> bookings;
		if (status != null) {
			bookings = bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
		} else {
			bookings = bookingRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
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
	}

	@Transactional
	public void confirmBooking(Long bookingId) {
		Booking booking = bookingRepository.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException(bookingId));

		if (booking.getStatus() != BookingStatus.PENDING) {
			throw BookingOperationException.onlyPendingCanBeConfirmed();
		}

		booking.setStatus(BookingStatus.CONFIRMED);
		booking.getSeatReservations().forEach(sr -> sr.setStatus(ReservationStatus.CONFIRMED));
		bookingRepository.save(booking);
	}

	@Transactional(readOnly = true)
	public Integer getAvailableBonusPointsForBooking(Long userId, BigDecimal bookingTotalPrice) {
		return bonusService.getAvailablePoints(userId, bookingTotalPrice);
	}
}