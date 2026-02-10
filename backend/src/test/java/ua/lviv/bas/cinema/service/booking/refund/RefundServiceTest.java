package ua.lviv.bas.cinema.service.booking.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Refund;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.SeatReservation;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.PaymentStatus;
import ua.lviv.bas.cinema.domain.enums.RefundStatus;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.mapper.RefundItemMapper;
import ua.lviv.bas.cinema.mapper.RefundMapper;
import ua.lviv.bas.cinema.repository.RefundRepository;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@ExtendWith(MockitoExtension.class)
public class RefundServiceTest {

	@Mock
	private TicketRepository ticketRepository;

	@Mock
	private RefundRepository refundRepository;

	@Mock
	private RefundRules refundRules;

	@Mock
	private RefundMapper refundMapper;

	@Mock
	private RefundItemMapper refundItemMapper;

	@Mock
	private RefundCalculationService calculationService;

	@Mock
	private RefundValidationService validationService;

	@Mock
	private NumberGeneratorService numberGenerator;

	@InjectMocks
	private RefundService refundService;

	private User testUser;
	private Ticket testTicket;
	private Payment testPayment;
	private Session testSession;
	private RefundPreviewRequest previewRequest;

	private static final Long USER_ID = 1L;
	private static final Long TICKET_ID = 2L;
	private static final BigDecimal TICKET_PRICE = new BigDecimal("100.00");

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(USER_ID);
		testUser.setEmail("test@example.com");

		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall 1");

		testSession = new Session();
		testSession.setStartTime(LocalDateTime.now().plusHours(3));
		testSession.setMovie(movie);
		testSession.setHall(hall);

		Seat seat = new Seat();
		seat.setRow(5);
		seat.setNumber(10);

		SeatReservation seatReservation = new SeatReservation();
		seatReservation.setSeat(seat);

		Booking booking = new Booking();
		booking.setSession(testSession);
		booking.setSeatReservations(List.of(seatReservation));

		testPayment = new Payment();
		testPayment.setId(1L);
		testPayment.setLiqpayPaymentId("PAY123");
		testPayment.setStatus(PaymentStatus.SUCCESS);

		TicketType ticketType = new TicketType();
		ticketType.setDisplayName("Standard");

		testTicket = new Ticket();
		testTicket.setId(TICKET_ID);
		testTicket.setUser(testUser);
		testTicket.setBooking(booking);
		testTicket.setTicketType(ticketType);
		testTicket.setFinalPrice(TICKET_PRICE);
		testTicket.setUniqueCode("TKT-123456");
		testTicket.setStatus(TicketStatus.ACTIVE);
		testTicket.setPayment(testPayment);
		testTicket.setBonusPointsUsed(50);

		previewRequest = new RefundPreviewRequest();
		previewRequest.setTicketId(TICKET_ID);
	}

	@Test
	void getRefundPreview_Success() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(validationService.validateRefund(testTicket)).thenReturn(null);

		RefundPreviewResponse response = refundService.getRefundPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.getTicketId()).isEqualTo(TICKET_ID);
		assertThat(response.getIsRefundable()).isFalse();
		assertThat(response.getNonRefundableReason()).contains("Contact support");
	}

	@Test
	void getRefundPreview_WhenTicketNotRefundable() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(validationService.validateRefund(testTicket)).thenReturn("Refund is not available for this session");

		RefundPreviewResponse response = refundService.getRefundPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.getIsRefundable()).isFalse();
		assertThat(response.getNonRefundableReason()).contains("Refund is not available");
	}

	@Test
	void getRefundPreview_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> refundService.getRefundPreview(previewRequest, USER_ID))
				.isInstanceOf(TicketNotFoundException.class);
	}

	@Test
	void getUserRefunds_Success() {
		Refund testRefund = new Refund();
		testRefund.setId(1L);
		testRefund.setUser(testUser);
		testRefund.setTotalAmount(new BigDecimal("70.00"));
		testRefund.setStatus(RefundStatus.PROCESSED);

		List<Refund> refunds = List.of(testRefund);
		when(refundRepository.findByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(refunds);

		RefundResponse refundResponse = new RefundResponse();
		when(refundMapper.toRefundResponse(testRefund)).thenReturn(refundResponse);
		when(numberGenerator.generateRefundNumber(testRefund)).thenReturn("RF-2024-00001");

		List<RefundResponse> responses = refundService.getUserRefunds(USER_ID);

		assertThat(responses).hasSize(1);
	}

	@Test
	void getUserRefunds_WhenNoRefunds() {
		when(refundRepository.findByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(Collections.emptyList());

		List<RefundResponse> responses = refundService.getUserRefunds(USER_ID);

		assertThat(responses).isEmpty();
	}
}