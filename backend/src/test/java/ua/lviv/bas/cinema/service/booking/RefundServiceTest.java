package ua.lviv.bas.cinema.service.booking;

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
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.Payment;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.mapper.booking.RefundItemMapper;
import ua.lviv.bas.cinema.mapper.booking.RefundMapper;
import ua.lviv.bas.cinema.repository.booking.RefundRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.integration.payment.PaymentGatewayService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@ExtendWith(MockitoExtension.class)
public class RefundServiceTest {

	@Mock
	private TicketRepository ticketRepository;
	@Mock
	private RefundRepository refundRepository;
	@Mock
	private PaymentService paymentService;
	@Mock
	private PaymentGatewayService paymentGatewayService;
	@Mock
	private BonusService bonusService;
	@Mock
	private RefundRules refundRules;
	@Mock
	private RefundMapper refundMapper;
	@Mock
	private RefundItemMapper refundItemMapper;
	@Mock
	private NumberGeneratorService numberGenerator;
	@Mock
	private AuditService auditService;

	@InjectMocks
	private RefundService refundService;

	private User testUser;
	private Ticket testTicket;
	private Payment testPayment;
	private Session testSession;
	private RefundPreviewRequest previewRequest;
	private Refund testRefund;

	private static final Long USER_ID = 1L;
	private static final Long TICKET_ID = 2L;
	private static final BigDecimal TICKET_PRICE = new BigDecimal("100.00");
	private static final BigDecimal REFUND_AMOUNT = new BigDecimal("70.00");
	private static final BigDecimal PERCENTAGE = new BigDecimal("70.00");
	private static final Integer BONUS_POINTS_USED = 50;
	private static final Integer BONUS_POINTS_TO_REFUND = 35;

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
		testTicket.setOriginalPrice(TICKET_PRICE);
		testTicket.setUniqueCode("TKT-123456");
		testTicket.setStatus(TicketStatus.ACTIVE);
		testTicket.setPayment(testPayment);
		testTicket.setBonusPointsUsed(BONUS_POINTS_USED);
		testTicket.setPurchaseTime(LocalDateTime.now().minusHours(1));

		testRefund = new Refund();
		testRefund.setId(1L);
		testRefund.setUser(testUser);
		testRefund.setPayment(testPayment);
		testRefund.setTotalAmount(REFUND_AMOUNT);
		testRefund.setTotalBonusPointsToDeduct(BONUS_POINTS_TO_REFUND);

		previewRequest = new RefundPreviewRequest(TICKET_ID);
	}

	@Test
	void getRefundPreview_Success() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);
		when(refundRules.getRefundPercentage(testSession.getStartTime())).thenReturn(PERCENTAGE);
		when(refundRules.getPolicyName(testSession.getStartTime())).thenReturn("Standard Refund");
		when(refundRules.getPolicyDescription(testSession.getStartTime())).thenReturn("70% refund before 3 hours");

		PaymentResponse paymentResponse = new PaymentResponse(1L, 1L, "BK-2024-00123", "test@example.com", "Test Movie",
				testSession.getStartTime(), "Hall 1", TICKET_PRICE, TICKET_PRICE, PaymentStatus.SUCCESS, "ORD123",
				"PAY123", null, null, null, null, null, true);
		when(paymentGatewayService.getPaymentStatus(testPayment.getLiqpayPaymentId())).thenReturn(paymentResponse);

		RefundPreviewResponse response = refundService.getRefundPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.ticketId()).isEqualTo(TICKET_ID);
		assertThat(response.isRefundable()).isTrue();
		assertThat(response.refundPercentage()).isEqualTo(PERCENTAGE);
		assertThat(response.refundAmount()).isEqualTo(REFUND_AMOUNT);
		assertThat(response.bonusPointsToRefund()).isEqualTo(BONUS_POINTS_TO_REFUND);
	}

	@Test
	void getRefundPreview_WhenTicketNotActive_ShouldReturnNonRefundable() {
		testTicket.setStatus(TicketStatus.REFUNDED);

		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));

		RefundPreviewResponse response = refundService.getRefundPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.isRefundable()).isFalse();
		assertThat(response.nonRefundableReason()).contains("Ticket is not active");
	}

	@Test
	void getRefundPreview_WhenRefundNotAvailable_ShouldReturnNonRefundable() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(false);

		RefundPreviewResponse response = refundService.getRefundPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.isRefundable()).isFalse();
		assertThat(response.nonRefundableReason()).contains("Refund is not available for this session");
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
		RefundResponse refundResponse = new RefundResponse(1L, "RF-2024-00001", "PROCESSED", REFUND_AMOUNT,
				BONUS_POINTS_TO_REFUND, "Reason", "System", LocalDateTime.now(), LocalDateTime.now(), 1L, "CARD",
				Collections.emptyList(), "Message", "3-5 days");

		when(refundRepository.findByUserIdOrderByCreatedDateDesc(USER_ID)).thenReturn(List.of(testRefund));
		when(refundMapper.toRefundResponse(testRefund)).thenReturn(refundResponse);
		when(numberGenerator.generateRefundNumber(testRefund)).thenReturn("RF-2024-00001");

		List<RefundResponse> responses = refundService.getUserRefunds(USER_ID);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).id()).isEqualTo(1L);
	}

	@Test
	void getUserRefunds_WhenNoRefunds_ShouldReturnEmpty() {
		when(refundRepository.findByUserIdOrderByCreatedDateDesc(USER_ID)).thenReturn(Collections.emptyList());

		List<RefundResponse> responses = refundService.getUserRefunds(USER_ID);

		assertThat(responses).isEmpty();
	}
}