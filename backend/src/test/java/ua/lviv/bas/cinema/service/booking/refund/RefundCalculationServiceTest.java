package ua.lviv.bas.cinema.service.booking.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.domain.booking.Booking;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;

@ExtendWith(MockitoExtension.class)
public class RefundCalculationServiceTest {

	@Mock
	private RefundRules refundRules;

	private RefundCalculationService refundCalculationService;

	@BeforeEach
	void setUp() {
		refundCalculationService = new RefundCalculationService();
	}

	@Test
	void calculateRefundAmount_Success() {
		BigDecimal price = new BigDecimal("100.00");
		BigDecimal percentage = new BigDecimal("70.00");

		BigDecimal result = refundCalculationService.calculateRefundAmount(price, percentage);

		assertThat(result).isEqualByComparingTo("70.00");
	}

	@Test
	void calculateRefundAmount_Rounding() {
		BigDecimal price = new BigDecimal("99.99");
		BigDecimal percentage = new BigDecimal("33.33");

		BigDecimal result = refundCalculationService.calculateRefundAmount(price, percentage);

		assertThat(result).isEqualByComparingTo("33.33");
	}

	@Test
	void calculateRefundAmount_ZeroPrice() {
		BigDecimal price = BigDecimal.ZERO;
		BigDecimal percentage = new BigDecimal("70.00");

		BigDecimal result = refundCalculationService.calculateRefundAmount(price, percentage);

		assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void calculateRefundAmount_ZeroPercentage() {
		BigDecimal price = new BigDecimal("100.00");
		BigDecimal percentage = BigDecimal.ZERO;

		BigDecimal result = refundCalculationService.calculateRefundAmount(price, percentage);

		assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
	}

	@Test
	void calculateRefundAmount_FullRefund() {
		BigDecimal price = new BigDecimal("100.00");
		BigDecimal percentage = new BigDecimal("100.00");

		BigDecimal result = refundCalculationService.calculateRefundAmount(price, percentage);

		assertThat(result).isEqualByComparingTo("100.00");
	}

	@Test
	void calculateBonusRefund_Success() {
		Integer bonusPointsUsed = 100;
		BigDecimal percentage = new BigDecimal("70.00");

		Integer result = refundCalculationService.calculateBonusRefund(bonusPointsUsed, percentage);

		assertThat(result).isEqualTo(70);
	}

	@Test
	void calculateBonusRefund_NoBonusPoints() {
		Integer bonusPointsUsed = 0;
		BigDecimal percentage = new BigDecimal("70.00");

		Integer result = refundCalculationService.calculateBonusRefund(bonusPointsUsed, percentage);

		assertThat(result).isEqualTo(0);
	}

	@Test
	void calculateBonusRefund_NullBonusPoints() {
		BigDecimal percentage = new BigDecimal("70.00");

		Integer result = refundCalculationService.calculateBonusRefund(null, percentage);

		assertThat(result).isEqualTo(0);
	}

	@Test
	void calculateBonusRefund_FullRefund() {
		Integer bonusPointsUsed = 100;
		BigDecimal percentage = new BigDecimal("100.00");

		Integer result = refundCalculationService.calculateBonusRefund(bonusPointsUsed, percentage);

		assertThat(result).isEqualTo(100);
	}

	@Test
	void formatRemainingTime_HoursAndMinutes() {
		LocalDateTime futureTime = LocalDateTime.now().plusHours(2).plusMinutes(30);

		String result = refundCalculationService.formatRemainingTime(futureTime);

		assertThat(result).contains("hours");
		assertThat(result).contains("minutes");
	}

	@Test
	void formatRemainingTime_OnlyHours() {
		LocalDateTime futureTime = LocalDateTime.now().plusHours(3);

		String result = refundCalculationService.formatRemainingTime(futureTime);

		assertThat(result).contains("hours");
	}

	@Test
	void formatRemainingTime_OnlyMinutes() {
		LocalDateTime futureTime = LocalDateTime.now().plusMinutes(45);

		String result = refundCalculationService.formatRemainingTime(futureTime);

		assertThat(result).contains("minutes");
	}

	@Test
	void createPreviewResponse_Success() {
		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall 1");

		Session session = new Session();
		session.setMovie(movie);
		session.setHall(hall);
		session.setStartTime(LocalDateTime.now().plusHours(3));

		Seat seat = new Seat();
		seat.setRow(5);
		seat.setNumber(10);

		SeatReservation seatReservation = new SeatReservation();
		seatReservation.setSeat(seat);

		Booking booking = new Booking();
		booking.setSession(session);
		booking.setSeatReservations(java.util.Arrays.asList(seatReservation));

		TicketType ticketType = new TicketType();
		ticketType.setDisplayName("Standard");

		Ticket ticket = new Ticket();
		ticket.setId(1L);
		ticket.setBooking(booking);
		ticket.setTicketType(ticketType);
		ticket.setOriginalPrice(new BigDecimal("150.00"));
		ticket.setFinalPrice(new BigDecimal("100.00"));
		ticket.setUniqueCode("TKT-123456");
		ticket.setPurchaseTime(LocalDateTime.now().minusHours(1));
		ticket.setBonusPointsUsed(50);

		when(refundRules.getRefundPercentage(session.getStartTime())).thenReturn(new BigDecimal("70.00"));
		when(refundRules.getPolicyName(session.getStartTime())).thenReturn("Partial Refund");
		when(refundRules.getPolicyDescription(session.getStartTime()))
				.thenReturn("70% refund if cancelled 3+ hours before");

		RefundPreviewResponse response = refundCalculationService.createPreviewResponse(ticket, refundRules);

		assertThat(response).isNotNull();
		assertThat(response.ticketId()).isEqualTo(1L);
		assertThat(response.ticketCode()).isEqualTo("TKT-123456");
		assertThat(response.movieTitle()).isEqualTo("Test Movie");
		assertThat(response.hallName()).isEqualTo("Hall 1");
		assertThat(response.seatInfo()).isEqualTo("Row 5, Seat 10");
		assertThat(response.originalPrice()).isEqualByComparingTo("150.00");
		assertThat(response.finalPrice()).isEqualByComparingTo("100.00");
		assertThat(response.refundPercentage()).isEqualByComparingTo("70.00");
		assertThat(response.refundAmount()).isEqualByComparingTo("70.00");
		assertThat(response.isRefundable()).isTrue();
		assertThat(response.bonusPointsUsed()).isEqualTo(50);
		assertThat(response.bonusPointsToRefund()).isEqualTo(35);
		assertThat(response.policyName()).isEqualTo("Partial Refund");
		assertThat(response.policyDescription()).isEqualTo("70% refund if cancelled 3+ hours before");
		assertThat(response.ticketType()).isEqualTo("Standard");
	}
}