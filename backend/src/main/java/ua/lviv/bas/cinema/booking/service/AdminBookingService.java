package ua.lviv.bas.cinema.booking.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.booking.domain.status.BookingStatus;
import ua.lviv.bas.cinema.booking.dto.response.AdminBookingDetailsResponse;
import ua.lviv.bas.cinema.booking.dto.response.AdminBookingListResponse;
import ua.lviv.bas.cinema.booking.mapper.BookingMapper;
import ua.lviv.bas.cinema.booking.repository.BookingRepository;
import ua.lviv.bas.cinema.booking.repository.specification.BookingSpecification;
import ua.lviv.bas.cinema.common.SortWhitelist;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.payment.domain.status.PaymentStatus;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminBookingService {

    private static final SortWhitelist BOOKING_SORT = SortWhitelist.of(
            Map.of("createdDate", "createdDate",
                    "finalPrice", "finalPrice",
                    "sessionTime", "session.startTime"),
            Sort.by(Sort.Direction.DESC, "createdDate"),
            Sort.by(Sort.Direction.DESC, "id"));

    private final BookingRepository bookingRepository;
    private final BookingSpecification bookingSpecification;
    private final BookingMapper bookingMapper;

    public Page<AdminBookingListResponse> getBookings(String query, BookingStatus status, PaymentStatus paymentStatus,
                                                      Long sessionId, Long userId, LocalDate dateFrom,
                                                      LocalDate dateTo, Pageable pageable) {
        var spec = bookingSpecification.forAdmin(query, status, paymentStatus, sessionId, userId, dateFrom, dateTo);
        return bookingRepository.findAll(spec, BOOKING_SORT.apply(pageable)).map(bookingMapper::toAdminListResponse);
    }

    public AdminBookingDetailsResponse getBooking(Long id) {
        return bookingRepository.findWithDetailsById(id)
                .map(bookingMapper::toAdminDetailsResponse)
                .orElseThrow(() -> new EntityNotFoundException("Booking", id));
    }
}
