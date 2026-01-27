package ua.lviv.bas.cinema.service.booking.refund;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ua.lviv.bas.cinema.config.RefundRules;
import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Booking;
import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.domain.Session;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
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
	void formatRemainingTime_LessThanMinute() {
		LocalDateTime futureTime = LocalDateTime.now().plusSeconds(30);

		String result = refundCalculationService.formatRemainingTime(futureTime);

		assertThat(result).isEqualTo("Less than a minute");
	}

	@Test
	void createPreviewResponse_Success() {
		User user = new User();
		user.setId(1L);

		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall 1");

		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(3));
		session.setMovie(movie);
		session.setHall(hall);

		Seat seat = new Seat();
		seat.setRow(5);
		seat.setNumber(10);

		BookedSeat bookedSeat = new BookedSeat();
		bookedSeat.setSeat(seat);

		Booking booking = new Booking();
		booking.setSession(session);
		booking.setBookedSeats(Arrays.asList(bookedSeat));

		TicketType ticketType = new TicketType();
		ticketType.setDisplayName("Standard");

		Ticket ticket = new Ticket();
		ticket.setId(1L);
		ticket.setUser(user);
		ticket.setBooking(booking);
		ticket.setTicketType(ticketType);
		ticket.setOriginalPrice(new BigDecimal("150.00"));
		ticket.setFinalPrice(new BigDecimal("100.00"));
		ticket.setUniqueCode("TKT-123456");
		ticket.setStatus(TicketStatus.ACTIVE);
		ticket.setPurchaseTime(LocalDateTime.now().minusHours(1));
		ticket.setBonusPointsUsed(50);

		when(refundRules.getRefundPercentage(session.getStartTime())).thenReturn(new BigDecimal("70.00"));
		when(refundRules.getPolicyName(session.getStartTime())).thenReturn("Partial Refund");
		when(refundRules.getPolicyDescription(session.getStartTime()))
				.thenReturn("70% refund if cancelled 3+ hours before");

		RefundPreviewResponse response = refundCalculationService.createPreviewResponse(ticket, refundRules);

		assertThat(response).isNotNull();
		assertThat(response.getTicketId()).isEqualTo(1L);
		assertThat(response.getTicketCode()).isEqualTo("TKT-123456");
		assertThat(response.getMovieTitle()).isEqualTo("Test Movie");
		assertThat(response.getHallName()).isEqualTo("Hall 1");
		assertThat(response.getSeatInfo()).isEqualTo("Row 5, Seat 10");
		assertThat(response.getOriginalPrice()).isEqualByComparingTo("150.00");
		assertThat(response.getFinalPrice()).isEqualByComparingTo("100.00");
		assertThat(response.getRefundPercentage()).isEqualByComparingTo("70.00");
		assertThat(response.getFeePercentage()).isEqualByComparingTo("30.00");
		assertThat(response.getIsRefundable()).isTrue();
		assertThat(response.getBonusPointsUsed()).isEqualTo(50);
		assertThat(response.getBonusPointsToRefund()).isEqualTo(35);
		assertThat(response.getPolicyName()).isEqualTo("Partial Refund");
		assertThat(response.getPolicyDescription()).isEqualTo("70% refund if cancelled 3+ hours before");
	}

	@Test
	void createPreviewResponse_WithEmptySeats() {
		Movie movie = new Movie();
		movie.setTitle("Test Movie");

		CinemaHall hall = new CinemaHall();
		hall.setName("Hall 1");

		Session session = new Session();
		session.setStartTime(LocalDateTime.now().plusHours(3));
		session.setMovie(movie);
		session.setHall(hall);

		Booking booking = new Booking();
		booking.setSession(session);
		booking.setBookedSeats(Arrays.asList());

		Ticket ticket = new Ticket();
		ticket.setId(1L);
		ticket.setBooking(booking);
		ticket.setFinalPrice(new BigDecimal("100.00"));
		ticket.setUniqueCode("TKT-123456");
		ticket.setPurchaseTime(LocalDateTime.now().minusHours(1));

		TicketType ticketType = new TicketType();
		ticketType.setDisplayName("Standard");
		ticket.setTicketType(ticketType);

		when(refundRules.getRefundPercentage(session.getStartTime())).thenReturn(new BigDecimal("70.00"));
		when(refundRules.getPolicyName(session.getStartTime())).thenReturn("Partial Refund");
		when(refundRules.getPolicyDescription(session.getStartTime()))
				.thenReturn("70% refund if cancelled 3+ hours before");

		RefundPreviewResponse response = refundCalculationService.createPreviewResponse(ticket, refundRules);

		assertThat(response.getSeatInfo()).isEqualTo("N/A");
	}
}