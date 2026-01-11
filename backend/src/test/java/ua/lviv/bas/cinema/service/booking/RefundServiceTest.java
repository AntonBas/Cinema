package ua.lviv.bas.cinema.service.booking;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.config.RefundRules;
import ua.lviv.bas.cinema.domain.BonusCard;
import ua.lviv.bas.cinema.domain.BonusTransaction;
import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Payment;
import ua.lviv.bas.cinema.domain.Refund;
import ua.lviv.bas.cinema.domain.RefundItem;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.BonusTransactionType;
import ua.lviv.bas.cinema.domain.enums.RefundItemStatus;
import ua.lviv.bas.cinema.domain.enums.RefundStatus;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.exception.domain.refund.RefundProcessingException;
import ua.lviv.bas.cinema.exception.domain.refund.TicketNotRefundableException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.mapper.RefundItemMapper;
import ua.lviv.bas.cinema.mapper.RefundMapper;
import ua.lviv.bas.cinema.repository.RefundRepository;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.service.user.BonusService;

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

	@InjectMocks
	private RefundService refundService;

	private User testUser;
	private Ticket testTicket;
	private Payment testPayment;
	private BookedSeat testBookedSeat;
	private TicketType testTicketType;
	private RefundPreviewRequest previewRequest;
	private RefundRequest refundRequest;
	private Refund testRefund;
	private RefundItem testRefundItem;
	private Session testSession;
	private final Long USER_ID = 1L;
	private final Long TICKET_ID = 2L;
	private final Long PAYMENT_ID = 3L;
	private final Long REFUND_ID = 4L;
	private final BigDecimal TICKET_PRICE = new BigDecimal("150.00");
	private final BigDecimal FINAL_PRICE = new BigDecimal("100.00");
	private final String TICKET_CODE = "TKT-123456";

	@BeforeEach
	void setUp() {
		testUser = new User();
		testUser.setId(USER_ID);
		testUser.setEmail("test@example.com");

		testTicketType = new TicketType();
		testTicketType.setId(1L);
		testTicketType.setDisplayName("Standard");

		testBookedSeat = new BookedSeat();
		testBookedSeat.setId(1L);
		Seat seat = new Seat();
		seat.setRow(5);
		seat.setNumber(10);
		testBookedSeat.setSeat(seat);

		testPayment = new Payment();
		testPayment.setId(PAYMENT_ID);

		testSession = new Session();
		testSession.setStartTime(LocalDateTime.now().plusHours(3));
		Movie movie = new Movie();
		movie.setTitle("Test Movie");
		testSession.setMovie(movie);
		CinemaHall hall = new CinemaHall();
		hall.setName("Hall 1");
		testSession.setHall(hall);

		Booking booking = new Booking();
		booking.setSession(testSession);
		booking.setBookedSeats(Arrays.asList(testBookedSeat));

		testTicket = new Ticket();
		testTicket.setId(TICKET_ID);
		testTicket.setUser(testUser);
		testTicket.setBooking(booking);
		testTicket.setTicketType(testTicketType);
		testTicket.setOriginalPrice(TICKET_PRICE);
		testTicket.setFinalPrice(FINAL_PRICE);
		testTicket.setUniqueCode(TICKET_CODE);
		testTicket.setStatus(TicketStatus.ACTIVE);
		testTicket.setPurchaseTime(LocalDateTime.now().minusHours(1));
		testTicket.setPayment(testPayment);
		testTicket.setBonusPointsUsed(50);

		previewRequest = new RefundPreviewRequest();
		previewRequest.setTicketId(TICKET_ID);

		refundRequest = new RefundRequest();
		refundRequest.setTicketId(TICKET_ID);
		refundRequest.setReason("Change of plans");

		testRefundItem = new RefundItem();
		testRefundItem.setId(1L);
		testRefundItem.setTicket(testTicket);
		testRefundItem.setRefundAmount(new BigDecimal("70.00"));
		testRefundItem.setStatus(RefundItemStatus.PROCESSED);

		testRefund = new Refund();
		testRefund.setId(REFUND_ID);
		testRefund.setUser(testUser);
		testRefund.setPayment(testPayment);
		testRefund.setTotalAmount(new BigDecimal("70.00"));
		testRefund.setStatus(RefundStatus.PROCESSED);
		testRefund.setItems(Arrays.asList(testRefundItem));
	}

	@Test
	void getRefundPreview_Success() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);
		when(refundRules.getRefundPercentage(testSession.getStartTime())).thenReturn(new BigDecimal("70.00"));
		when(refundRules.getPolicyName(testSession.getStartTime())).thenReturn("Standard Refund Policy");
		when(refundRules.getPolicyDescription(testSession.getStartTime()))
				.thenReturn("70% refund if cancelled 3+ hours before");

		RefundPreviewResponse response = refundService.getRefundPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.getTicketId()).isEqualTo(TICKET_ID);
		assertThat(response.getTicketCode()).isEqualTo(TICKET_CODE);
		assertThat(response.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(response.getHallName()).isEqualTo("Hall 1");
		assertThat(response.getSeatInfo()).isEqualTo("Row 5, Seat 10");
		assertThat(response.getOriginalPrice()).isEqualByComparingTo(TICKET_PRICE);
		assertThat(response.getFinalPrice()).isEqualByComparingTo(FINAL_PRICE);
		assertThat(response.getRefundPercentage()).isEqualByComparingTo("70.00");
		assertThat(response.getIsRefundable()).isTrue();
		assertThat(response.getBonusPointsUsed()).isEqualTo(50);
		assertThat(response.getBonusPointsToRefund()).isEqualTo(35);
		verify(ticketRepository).findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE);
	}

	@Test
	void getRefundPreview_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> refundService.getRefundPreview(previewRequest, USER_ID))
				.isInstanceOf(TicketNotFoundException.class);

		verify(ticketRepository).findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE);
	}

	@Test
	void getRefundPreview_WhenTicketNotRefundable() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(false);

		RefundPreviewResponse response = refundService.getRefundPreview(previewRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.getIsRefundable()).isFalse();
		assertThat(response.getNonRefundableReason()).contains("Refund is not available");
		verify(ticketRepository).findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE);
	}

	@Test
	void getRefundPreview_WhenTicketNotActive_ShouldThrowException() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> refundService.getRefundPreview(previewRequest, USER_ID))
				.isInstanceOf(TicketNotFoundException.class).hasMessageContaining("Ticket not found or not active");

		verify(ticketRepository).findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE);
	}

	@Test
	void processRefund_Success() {
		BigDecimal refundPercentage = new BigDecimal("70.00");
		BigDecimal refundAmount = new BigDecimal("70.00");
		Integer bonusPointsToRefund = 35;

		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		lenient().when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);
		lenient().when(refundRules.getRefundPercentage(testSession.getStartTime())).thenReturn(refundPercentage);
		when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);
		when(ticketRepository.save(testTicket)).thenReturn(testTicket);

		BonusCard bonusCard = new BonusCard();
		bonusCard.setId(1L);
		when(bonusService.findOrCreateBonusCard(testUser)).thenReturn(bonusCard);
		when(bonusService.createBonusTransaction(eq(bonusCard), eq(bonusPointsToRefund),
				eq(BonusTransactionType.REFUND_RETURN), anyString(), any(), any(), any(Refund.class)))
				.thenReturn(new BonusTransaction());

		RefundResponse refundResponse = new RefundResponse();
		refundResponse.setId(REFUND_ID);
		when(refundMapper.toRefundResponse(testRefund)).thenReturn(refundResponse);

		RefundResponse response = refundService.processRefund(refundRequest, USER_ID);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(REFUND_ID);

		verify(ticketRepository).findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE);
		verify(refundRepository).save(any(Refund.class));
		verify(paymentService).refundPayment(eq(testPayment), eq(refundAmount), anyString());
		verify(bonusService).createBonusTransaction(any(BonusCard.class), eq(bonusPointsToRefund),
				eq(BonusTransactionType.REFUND_RETURN), anyString(), any(), any(), any(Refund.class));
		verify(ticketRepository).save(testTicket);
		assertThat(testTicket.getStatus()).isEqualTo(TicketStatus.REFUNDED);
		assertThat(testTicket.getRefund()).isEqualTo(testRefund);
	}

	@Test
	void processRefund_WhenTicketNotFound_ShouldThrowException() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> refundService.processRefund(refundRequest, USER_ID))
				.isInstanceOf(TicketNotFoundException.class);

		verify(ticketRepository).findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE);
		verify(refundRepository, never()).save(any());
		verify(paymentService, never()).refundPayment(any(), any(), any());
	}

	@Test
	void processRefund_WhenTicketNotRefundable_ShouldThrowException() {
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		lenient().when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(false);

		assertThatThrownBy(() -> refundService.processRefund(refundRequest, USER_ID))
				.isInstanceOf(TicketNotRefundableException.class);

		verify(ticketRepository).findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE);
		verify(refundRepository, never()).save(any());
		verify(paymentService, never()).refundPayment(any(), any(), any());
	}

	@Test
	void processRefund_WhenTicketAlreadyRefunded_ShouldThrowException() {
		testTicket.setRefund(new Refund());
		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		lenient().when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);

		assertThatThrownBy(() -> refundService.processRefund(refundRequest, USER_ID))
				.isInstanceOf(TicketNotRefundableException.class);

		verify(ticketRepository).findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE);
		verify(refundRepository, never()).save(any());
		verify(paymentService, never()).refundPayment(any(), any(), any());
	}

	@Test
	void processRefund_WhenSessionAlreadyStarted_ShouldThrowException() {
		LocalDateTime pastSessionTime = LocalDateTime.now().minusHours(1);
		testSession.setStartTime(pastSessionTime);

		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));

		lenient().when(refundRules.isRefundable(pastSessionTime)).thenReturn(true);

		assertThatThrownBy(() -> refundService.processRefund(refundRequest, USER_ID))
				.isInstanceOf(TicketNotRefundableException.class);

		verify(ticketRepository).findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE);
		verify(refundRepository, never()).save(any());
		verify(paymentService, never()).refundPayment(any(), any(), any());
	}

	@Test
	void processRefund_WhenPaymentRefundFails_ShouldRollback() {
		BigDecimal refundPercentage = new BigDecimal("70.00");
		BigDecimal refundAmount = new BigDecimal("70.00");

		when(ticketRepository.findByIdAndUserIdAndStatus(TICKET_ID, USER_ID, TicketStatus.ACTIVE))
				.thenReturn(Optional.of(testTicket));
		lenient().when(refundRules.isRefundable(testSession.getStartTime())).thenReturn(true);
		lenient().when(refundRules.getRefundPercentage(testSession.getStartTime())).thenReturn(refundPercentage);
		when(refundRepository.save(any(Refund.class))).thenReturn(testRefund);

		doThrow(new RuntimeException("Payment service error")).when(paymentService).refundPayment(eq(testPayment),
				eq(refundAmount), anyString());

		assertThatThrownBy(() -> refundService.processRefund(refundRequest, USER_ID))
				.isInstanceOf(RefundProcessingException.class);

		verify(refundRepository, times(2)).save(any(Refund.class));
		assertThat(testRefund.getStatus()).isEqualTo(RefundStatus.REJECTED);
		verify(ticketRepository, never()).save(any());
	}

	@Test
	void getUserRefunds_Success() {
		List<Refund> refunds = Arrays.asList(testRefund);
		when(refundRepository.findByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(refunds);

		RefundResponse refundResponse = new RefundResponse();
		refundResponse.setId(REFUND_ID);
		when(refundMapper.toRefundResponse(testRefund)).thenReturn(refundResponse);

		List<RefundResponse> responses = refundService.getUserRefunds(USER_ID);

		assertThat(responses).hasSize(1);
		assertThat(responses.get(0).getId()).isEqualTo(REFUND_ID);
		verify(refundRepository).findByUserIdOrderByCreatedAtDesc(USER_ID);
	}

	@Test
	void getUserRefunds_WhenNoRefunds() {
		when(refundRepository.findByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(Collections.emptyList());

		List<RefundResponse> responses = refundService.getUserRefunds(USER_ID);

		assertThat(responses).isEmpty();
		verify(refundRepository).findByUserIdOrderByCreatedAtDesc(USER_ID);
	}

	@Test
	void calculateRefundAmount_Success() {
		BigDecimal price = new BigDecimal("100.00");
		BigDecimal percentage = new BigDecimal("70.00");

		BigDecimal result = invokePrivateCalculateRefundAmount(price, percentage);

		assertThat(result).isEqualByComparingTo("70.00");
	}

	@Test
	void calculateRefundAmount_Rounding() {
		BigDecimal price = new BigDecimal("99.99");
		BigDecimal percentage = new BigDecimal("33.33");

		BigDecimal result = invokePrivateCalculateRefundAmount(price, percentage);

		assertThat(result).isEqualByComparingTo("33.33");
	}

	@Test
	void calculateBonusRefund_Success() {
		Integer bonusPointsUsed = 100;
		BigDecimal percentage = new BigDecimal("70.00");

		Integer result = invokePrivateCalculateBonusRefund(bonusPointsUsed, percentage);

		assertThat(result).isEqualTo(70);
	}

	@Test
	void calculateBonusRefund_NoBonusPoints() {
		Integer bonusPointsUsed = 0;
		BigDecimal percentage = new BigDecimal("70.00");

		Integer result = invokePrivateCalculateBonusRefund(bonusPointsUsed, percentage);

		assertThat(result).isEqualTo(0);
	}

	@Test
	void calculateBonusRefund_NullBonusPoints() {
		BigDecimal percentage = new BigDecimal("70.00");

		Integer result = invokePrivateCalculateBonusRefund(null, percentage);

		assertThat(result).isEqualTo(0);
	}

	@Test
	void generateRefundNumber_Success() {
		Long refundId = 123L;
		String result = invokePrivateGenerateRefundNumber(refundId);

		assertThat(result).isNotNull();
		assertThat(result).contains("RF-");
		assertThat(result).contains("123");
	}

	@Test
	void formatRemainingTime_Success() {
		LocalDateTime futureTime = LocalDateTime.now().plusHours(2).plusMinutes(30);
		String result = invokePrivateFormatRemainingTime(futureTime);

		assertThat(result).isNotNull();
		assertThat(result).contains("hours");
		assertThat(result).contains("minutes");
	}

	@Test
	void formatRemainingTime_OnlyHours() {
		LocalDateTime futureTime = LocalDateTime.now().plusHours(3);
		String result = invokePrivateFormatRemainingTime(futureTime);

		assertThat(result).isNotNull();
		assertThat(result).contains("hours");
	}

	@Test
	void formatRemainingTime_OnlyMinutes() {
		LocalDateTime futureTime = LocalDateTime.now().plusMinutes(45);
		String result = invokePrivateFormatRemainingTime(futureTime);

		assertThat(result).isNotNull();
		assertThat(result).contains("minutes");
	}

	private BigDecimal invokePrivateCalculateRefundAmount(BigDecimal price, BigDecimal percentage) {
		try {
			var method = RefundService.class.getDeclaredMethod("calculateRefundAmount", BigDecimal.class,
					BigDecimal.class);
			method.setAccessible(true);
			return (BigDecimal) method.invoke(refundService, price, percentage);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Integer invokePrivateCalculateBonusRefund(Integer bonusPointsUsed, BigDecimal percentage) {
		try {
			var method = RefundService.class.getDeclaredMethod("calculateBonusRefund", Integer.class, BigDecimal.class);
			method.setAccessible(true);
			return (Integer) method.invoke(refundService, bonusPointsUsed, percentage);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String invokePrivateGenerateRefundNumber(Long refundId) {
		try {
			var method = RefundService.class.getDeclaredMethod("generateRefundNumber", Long.class);
			method.setAccessible(true);
			return (String) method.invoke(refundService, refundId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String invokePrivateFormatRemainingTime(LocalDateTime sessionTime) {
		try {
			var method = RefundService.class.getDeclaredMethod("formatRemainingTime", LocalDateTime.class);
			method.setAccessible(true);
			return (String) method.invoke(refundService, sessionTime);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}