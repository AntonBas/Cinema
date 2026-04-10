package ua.lviv.bas.cinema.mapper.booking;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.booking.RefundItem;
import ua.lviv.bas.cinema.domain.booking.status.RefundItemStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;

public class RefundItemMapperTest {

	private final RefundItemMapper mapper = Mappers.getMapper(RefundItemMapper.class);

	@Test
	void toResponse() {
		var ticket = Ticket.builder().id(123L).uniqueCode("TICKET-ABC123").build();
		var refundItem = RefundItem.builder().id(1L).ticket(ticket).ticketPrice(new BigDecimal("300.00"))
				.refundAmount(new BigDecimal("240.00")).status(RefundItemStatus.APPROVED).build();

		refundItem.setCreatedDate(LocalDateTime.now());
		refundItem.setLastModifiedDate(LocalDateTime.now());

		var response = mapper.toResponse(refundItem);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.ticketId()).isEqualTo(123L);
		assertThat(response.ticketCode()).isEqualTo("TICKET-ABC123");
		assertThat(response.ticketPrice()).isEqualTo(new BigDecimal("300.00"));
		assertThat(response.refundAmount()).isEqualTo(new BigDecimal("240.00"));
		assertThat(response.status()).isEqualTo("APPROVED");
	}

	@Test
	void toResponseWithNullTicket() {
		var refundItem = RefundItem.builder().id(2L).ticket(null).ticketPrice(new BigDecimal("200.00"))
				.refundAmount(new BigDecimal("160.00")).status(RefundItemStatus.PENDING).build();

		var response = mapper.toResponse(refundItem);

		assertThat(response.id()).isEqualTo(2L);
		assertThat(response.ticketId()).isNull();
		assertThat(response.ticketCode()).isNull();
		assertThat(response.ticketPrice()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.refundAmount()).isEqualTo(new BigDecimal("160.00"));
		assertThat(response.status()).isEqualTo("PENDING");
	}

	@Test
	void toResponseWithNullTicketProperties() {
		var ticket = Ticket.builder().id(null).uniqueCode(null).build();
		var refundItem = RefundItem.builder().id(3L).ticket(ticket).ticketPrice(new BigDecimal("150.00")).build();

		var response = mapper.toResponse(refundItem);

		assertThat(response.id()).isEqualTo(3L);
		assertThat(response.ticketId()).isNull();
		assertThat(response.ticketCode()).isNull();
		assertThat(response.ticketPrice()).isEqualTo(new BigDecimal("150.00"));
	}

	@Test
	void toResponseWithNull() {
		var response = mapper.toResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toResponseWithDifferentStatus() {
		var ticket = Ticket.builder().id(999L).uniqueCode("TICKET-STATUS").build();
		var refundItem = RefundItem.builder().id(9L).ticket(ticket).ticketPrice(new BigDecimal("300.00"))
				.status(RefundItemStatus.PROCESSED).build();

		var response = mapper.toResponse(refundItem);

		assertThat(response.id()).isEqualTo(9L);
		assertThat(response.ticketId()).isEqualTo(999L);
		assertThat(response.ticketCode()).isEqualTo("TICKET-STATUS");
		assertThat(response.status()).isEqualTo("PROCESSED");
	}
}