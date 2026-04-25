package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
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
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.exception.domain.financial.refund.TicketNotRefundableException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.mapper.booking.RefundItemMapper;
import ua.lviv.bas.cinema.mapper.booking.RefundMapper;
import ua.lviv.bas.cinema.repository.booking.RefundRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.common.NumberGeneratorService;

@ExtendWith(MockitoExtension.class)
public class RefundServiceTest {

	@Mock
	private TicketRepository ticketRepository;
	@Mock
	private RefundRepository refundRepository;
	@Mock
	private PaymentService paymentService;
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
		testUser = User.builder().id(USER_ID).email("test@example.com").build();

		Movie movie = Movie.builder().title("Test Movie").build();

		CinemaHall hall = CinemaHall.builder().name("Hall 1").build();

		testSession = Session.builder().startTime(LocalDateTime.now().plusHours(3)).movie(movie).hall(hall).build();

		Seat seat = Seat.builder().row(5).number(10).build();

		SeatReservation seatReservation = SeatReservation.builder().seat(seat).build();

		Booking booking = Booking.builder().session(testSession).seatReservations(List.of(seatReservation)).build();

		testPayment = Payment.builder().id(1L).liqpayPaymentId("PAY123").status(PaymentStatus.SUCCESS).build();

		TicketType ticketType = TicketType.builder().displayName("Standard").build();

		testTicket = Ticket.builder().id(TICKET_ID).user(testUser).booking(booking).ticketType(ticketType)
				.finalPrice(TICKET_PRICE).originalPrice(TICKET_PRICE).uniqueCode("TKT-123456")
				.status(TicketStatus.ACTIVE).payment(testPayment).bonusPointsUsed(BONUS_POINTS_USED)
				.purchaseTime(LocalDateTime.now().minusHours(1)).build();

		testRefund = Refund.builder().id(1L).user(testUser).payment(testPayment).totalAmount(REFUND_AMOUNT)
				.totalBonusPointsToDeduct(BONUS_POINTS_TO_REFUND).build();

		previewRequest = new RefundPreviewRequest(TICKET_ID);
	}

	@Test
	void getPreviewShouldSucceed() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);
		when(refundRules.getRefundPercentage(testSession.getStartTime())).thenReturn(PERCENTAGE);
		when(refundRules.getPolicyName(testSession.getStartTime())).thenReturn("Standard Refund");
		when(refundRules.getPolicyDescription(testSession.getStartTime())).thenReturn("70% refund before 3 hours");

		RefundPreviewResponse response = refundService.getPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.ticketId()).isEqualTo(TICKET_ID);
		assertThat(response.isRefundable()).isTrue();
		assertThat(response.refundPercentage()).isEqualTo(PERCENTAGE);
		assertThat(response.refundAmount()).isEqualTo(REFUND_AMOUNT);
		assertThat(response.bonusPointsToRefund()).isEqualTo(BONUS_POINTS_TO_REFUND);
	}

	@Test
	void getPreviewWhenPaymentNotSuccessShouldReturnNonRefundable() {
		testPayment.setStatus(PaymentStatus.PENDING);

		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);

		RefundPreviewResponse response = refundService.getPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.isRefundable()).isFalse();
		assertThat(response.nonRefundableReason()).contains("Payment cannot be refunded via API");
	}

	@Test
	void getPreviewWhenTicketNotActiveShouldReturnNonRefundable() {
		testTicket.setStatus(TicketStatus.REFUNDED);

		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));

		RefundPreviewResponse response = refundService.getPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.isRefundable()).isFalse();
		assertThat(response.nonRefundableReason()).contains("Ticket is not active");
	}

	@Test
	void getPreviewWhenRefundNotAvailableShouldReturnNonRefundable() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(false);

		RefundPreviewResponse response = refundService.getPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.isRefundable()).isFalse();
		assertThat(response.nonRefundableReason()).contains("Refund is not available for this session");
	}

	@Test
	void getPreviewWhenTicketNotFoundShouldThrowException() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> refundService.getPreview(previewRequest, USER_ID))
				.isInstanceOf(TicketNotFoundException.class);
	}

	@Test
	void refundShouldSucceed() {
		RefundRequest refundRequest = new RefundRequest(TICKET_ID, "Test reason");

		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);
		when(refundRules.getRefundPercentage(testSession.getStartTime())).thenReturn(PERCENTAGE);
		when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);
		when(bonusService.getOrCreateCard(testUser)).thenReturn(null);
		when(numberGenerator.generateRefundNumber(testRefund)).thenReturn("RF-2024-00001");

		RefundResponse mockResponse = new RefundResponse(1L, "RF-2024-00001", "PROCESSED", REFUND_AMOUNT,
				BONUS_POINTS_TO_REFUND, "Test reason", "System", LocalDateTime.now(), LocalDateTime.now(), 1L, "CARD",
				Collections.emptyList(), "Refund processed successfully", "3-5 business days");

		when(refundMapper.toResponse(testRefund)).thenReturn(mockResponse);

		RefundResponse response = refundService.refund(refundRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		verify(paymentService).refund(eq(testPayment), eq(REFUND_AMOUNT), any(String.class));
		verify(ticketRepository).save(testTicket);
		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.REFUNDED);
	}

	@Test
	void refundWhenTicketNotRefundableShouldThrowException() {
		RefundRequest refundRequest = new RefundRequest(TICKET_ID, "Test reason");
		testTicket.setStatus(TicketStatus.REFUNDED);

		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));

		assertThatThrownBy(() -> refundService.refund(refundRequest, USER_ID))
				.isInstanceOf(TicketNotRefundableException.class);
	}

	@Test
	void getRefundsShouldSucceed() {
		RefundResponse mockResponse = new RefundResponse(1L, "RF-2024-00001", "PROCESSED", REFUND_AMOUNT,
				BONUS_POINTS_TO_REFUND, "Reason", "System", LocalDateTime.now(), LocalDateTime.now(), 1L, "CARD",
				Collections.emptyList(), "Refund processed successfully", "3-5 business days");

		when(refundRepository.findByUserIdOrderByCreatedDateDesc(USER_ID)).thenReturn(List.of(testRefund));
		when(refundMapper.toResponse(testRefund)).thenReturn(mockResponse);
		when(numberGenerator.generateRefundNumber(testRefund)).thenReturn("RF-2024-00001");

		List<RefundResponse> responses = refundService.getRefunds(USER_ID);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).id()).isEqualTo(1L);
	}

	@Test
	void getRefundsWhenNoRefundsShouldReturnEmpty() {
		when(refundRepository.findByUserIdOrderByCreatedDateDesc(USER_ID)).thenReturn(Collections.emptyList());

		List<RefundResponse> responses = refundService.getRefunds(USER_ID);

		assertThat(responses).isEmpty();
	}
}