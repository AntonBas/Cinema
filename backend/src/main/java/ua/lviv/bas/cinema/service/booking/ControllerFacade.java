package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BookingStatus;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.booking.request.BookingCreateRequest;
import ua.lviv.bas.cinema.dto.booking.response.BookingResponse;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;
import ua.lviv.bas.cinema.dto.payment.request.PaymentCreateRequest;
import ua.lviv.bas.cinema.dto.payment.response.PaymentLiqPayDataResponse;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.service.booking.availability.SeatReservationService;
import ua.lviv.bas.cinema.service.booking.creation.BookingCreationService;
import ua.lviv.bas.cinema.service.booking.management.BookingManagementService;
import ua.lviv.bas.cinema.service.booking.payment.PaymentProcessingService;
import ua.lviv.bas.cinema.service.booking.payment.PaymentStatusService;
import ua.lviv.bas.cinema.service.booking.refund.RefundService;
import ua.lviv.bas.cinema.service.booking.ticket.TicketRetrievalService;
import ua.lviv.bas.cinema.service.booking.ticket.TicketService;
import ua.lviv.bas.cinema.service.booking.types.TicketTypeService;

@Service
@RequiredArgsConstructor
public class ControllerFacade {

	private final BookingCreationService bookingCreationService;
	private final BookingManagementService bookingManagementService;
	private final SeatReservationService seatAvailabilityService;
	private final PaymentProcessingService paymentProcessingService;
	private final PaymentStatusService paymentStatusService;
	private final TicketService ticketService;
	private final TicketRetrievalService ticketRetrievalService;
	private final TicketTypeService ticketTypeService;
	private final RefundService refundService;

	@Transactional
	public BookingResponse createBooking(BookingCreateRequest request, User user) {
		return bookingCreationService.createBooking(request, user);
	}

	@Transactional(readOnly = true)
	public BookingResponse getBookingById(Long bookingId, User user) {
		return bookingManagementService.getBookingById(bookingId, user);
	}

	@Transactional(readOnly = true)
	public Page<BookingResponse> getUserBookings(Long userId, BookingStatus status, Pageable pageable) {
		return bookingManagementService.getUserBookings(userId, status, pageable);
	}

	@Transactional
	public void cancelBooking(Long bookingId, User user) {
		bookingManagementService.cancelBooking(bookingId, user);
	}

	@Transactional(readOnly = true)
	public Integer getAvailableBonusPointsForBooking(Long userId, BigDecimal bookingTotalPrice) {
		return bookingManagementService.getAvailableBonusPointsForBooking(userId, bookingTotalPrice);
	}

	@Transactional(readOnly = true)
	public SeatReservationResponse getSeatAvailability(Long sessionId) {
		return seatAvailabilityService.getSeatAvailability(sessionId);
	}

	@Transactional(readOnly = true)
	public int getAvailableSeatsCount(Long sessionId) {
		return seatAvailabilityService.getAvailableSeatsCount(sessionId);
	}

	@Transactional(readOnly = true)
	public void validateSeatAvailability(Long sessionId, Long seatId) {
		seatAvailabilityService.validateSeatAvailability(sessionId, seatId);
	}

	@Transactional(readOnly = true)
	public boolean isSeatAvailableForSession(Long sessionId, Long seatId) {
		return seatAvailabilityService.isSeatAvailableForSession(sessionId, seatId);
	}

	@Transactional
	public PaymentResponse createPayment(PaymentCreateRequest request, User user) {
		return paymentProcessingService.createPayment(request, user);
	}

	@Transactional(readOnly = true)
	public PaymentResponse getPaymentStatus(Long paymentId, User user) {
		return paymentProcessingService.getPaymentStatus(paymentId, user);
	}

	@Transactional(readOnly = true)
	public PaymentLiqPayDataResponse preparePaymentData(Long paymentId) {
		return paymentStatusService.preparePaymentData(paymentId);
	}

	@Transactional
	public PaymentResponse retryPayment(Long paymentId, User user) {
		return paymentProcessingService.retryPayment(paymentId, user);
	}

	@Transactional(readOnly = true)
	public TicketResponse getTicketById(Long ticketId, User user) {
		return ticketRetrievalService.getTicketById(ticketId, user);
	}

	@Transactional(readOnly = true)
	public TicketResponse getTicketByCode(String ticketCode, User user) {
		return ticketRetrievalService.getTicketByCode(ticketCode, user);
	}

	@Transactional
	public void validateTicket(String ticketCode) {
		ticketService.validateTicket(ticketCode);
	}

	@Transactional(readOnly = true)
	public byte[] generateTicketQRCode(String ticketCode) {
		return ticketService.generateTicketQRCode(ticketCode);
	}

	@Transactional(readOnly = true)
	public boolean isTicketValid(String ticketCode) {
		return ticketService.isTicketValid(ticketCode);
	}

	@Transactional(readOnly = true)
	public TicketStatus checkTicketStatus(String ticketCode) {
		return ticketService.checkTicketStatus(ticketCode);
	}

	@Transactional
	public RefundResponse processRefund(RefundRequest request, Long userId) {
		return refundService.processRefund(request, userId);
	}

	@Transactional(readOnly = true)
	public List<RefundResponse> getUserRefunds(Long userId) {
		return refundService.getUserRefunds(userId);
	}

	@Transactional
	public TicketTypeResponse createTicketType(TicketTypeCreateRequest createRequest) {
		return ticketTypeService.createTicketType(createRequest);
	}

	@Transactional(readOnly = true)
	public TicketTypeResponse getTicketTypeById(Long id) {
		return ticketTypeService.getTicketTypeById(id);
	}

	@Transactional(readOnly = true)
	public TicketTypeResponse getTicketTypeByCode(String code) {
		return ticketTypeService.getTicketTypeByCode(code);
	}

	@Transactional(readOnly = true)
	public List<TicketTypeSimpleResponse> getActiveTicketTypesForDropdown() {
		return ticketTypeService.getActiveTicketTypesForDropdown();
	}

	@Transactional
	public TicketTypeResponse updateTicketType(Long id, TicketTypeUpdateRequest updateRequest) {
		return ticketTypeService.updateTicketType(id, updateRequest);
	}

	@Transactional
	public void deleteTicketType(Long id) {
		ticketTypeService.deleteTicketType(id);
	}

	@Transactional
	public TicketTypeResponse toggleTicketTypeActiveStatus(Long id) {
		return ticketTypeService.toggleTicketTypeActiveStatus(id);
	}

	@Transactional(readOnly = true)
	public boolean validateAgeForTicketType(Long ticketTypeId, Integer age) {
		return ticketTypeService.validateAgeForTicketType(ticketTypeId, age);
	}

	@Transactional(readOnly = true)
	public boolean existsByCode(String code) {
		return ticketTypeService.existsByCode(code);
	}
}