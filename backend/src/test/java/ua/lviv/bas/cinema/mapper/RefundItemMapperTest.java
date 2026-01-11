package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.RefundItem;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.enums.RefundItemStatus;
import ua.lviv.bas.cinema.dto.refund.response.RefundItemResponse;

public class RefundItemMapperTest {

	private RefundItemMapper refundItemMapper;

	@BeforeEach
	void setUp() {
		refundItemMapper = Mappers.getMapper(RefundItemMapper.class);
	}

	@Test
	void toRefundItemResponse_ShouldMapAllFieldsCorrectly() {
		Ticket ticket = Ticket.builder().id(123L).uniqueCode("TICKET-ABC123").build();

		RefundItem refundItem = RefundItem.builder().id(1L).ticket(ticket).ticketPrice(new BigDecimal("300.00"))
				.refundPercentage(new BigDecimal("80.00")).refundAmount(new BigDecimal("240.00"))
				.bonusPointsToDeduct(25).status(RefundItemStatus.APPROVED)
				.createdAt(LocalDateTime.of(2024, 1, 15, 14, 30)).build();

		RefundItemResponse response = refundItemMapper.toRefundItemResponse(refundItem);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getTicketId()).isEqualTo(123L);
		assertThat(response.getTicketCode()).isEqualTo("TICKET-ABC123");
		assertThat(response.getTicketPrice()).isEqualTo(new BigDecimal("300.00"));
		assertThat(response.getRefundPercentage()).isEqualTo(new BigDecimal("80.00"));
		assertThat(response.getRefundAmount()).isEqualTo(new BigDecimal("240.00"));
		assertThat(response.getBonusPointsToDeduct()).isEqualTo(25);
		assertThat(response.getStatus()).isEqualTo(RefundItemStatus.APPROVED);
		assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 15, 14, 30));
	}

	@Test
	void toRefundItemResponse_ShouldHandleNullTicket() {
		RefundItem refundItem = RefundItem.builder().id(2L).ticket(null).ticketPrice(new BigDecimal("200.00"))
				.refundAmount(new BigDecimal("160.00")).bonusPointsToDeduct(0).status(RefundItemStatus.PENDING).build();

		RefundItemResponse response = refundItemMapper.toRefundItemResponse(refundItem);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(2L);
		assertThat(response.getTicketId()).isNull();
		assertThat(response.getTicketCode()).isNull();
		assertThat(response.getTicketPrice()).isEqualTo(new BigDecimal("200.00"));
		assertThat(response.getRefundAmount()).isEqualTo(new BigDecimal("160.00"));
		assertThat(response.getBonusPointsToDeduct()).isZero();
		assertThat(response.getStatus()).isEqualTo(RefundItemStatus.PENDING);
	}

	@Test
	void toRefundItemResponse_ShouldHandleTicketWithNullIdAndCode() {
		Ticket ticket = Ticket.builder().id(null).uniqueCode(null).build();

		RefundItem refundItem = RefundItem.builder().id(3L).ticket(ticket).ticketPrice(new BigDecimal("150.00"))
				.refundAmount(new BigDecimal("120.00")).build();

		RefundItemResponse response = refundItemMapper.toRefundItemResponse(refundItem);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(3L);
		assertThat(response.getTicketId()).isNull();
		assertThat(response.getTicketCode()).isNull();
		assertThat(response.getTicketPrice()).isEqualTo(new BigDecimal("150.00"));
		assertThat(response.getRefundAmount()).isEqualTo(new BigDecimal("120.00"));
	}

	@Test
	void toRefundItemResponse_ShouldHandleNullFields() {
		Ticket ticket = Ticket.builder().id(456L).uniqueCode("TICKET-DEF456").build();

		RefundItem refundItem = RefundItem.builder().id(4L).ticket(ticket).ticketPrice(new BigDecimal("250.00"))
				.refundPercentage(null).refundAmount(null).bonusPointsToDeduct(0).status(RefundItemStatus.PENDING)
				.build();

		RefundItemResponse response = refundItemMapper.toRefundItemResponse(refundItem);

		assertThat(response).isNotNull();
		assertThat(response.getTicketId()).isEqualTo(456L);
		assertThat(response.getTicketCode()).isEqualTo("TICKET-DEF456");
		assertThat(response.getRefundPercentage()).isNull();
		assertThat(response.getRefundAmount()).isNull();
		assertThat(response.getBonusPointsToDeduct()).isZero();
	}

	@Test
	void toRefundItemResponse_ShouldHandleZeroRefundAmount() {
		Ticket ticket = Ticket.builder().id(789L).uniqueCode("TICKET-GHI789").build();

		RefundItem refundItem = RefundItem.builder().id(5L).ticket(ticket).ticketPrice(new BigDecimal("100.00"))
				.refundAmount(BigDecimal.ZERO).bonusPointsToDeduct(10).build();

		RefundItemResponse response = refundItemMapper.toRefundItemResponse(refundItem);

		assertThat(response).isNotNull();
		assertThat(response.getRefundAmount()).isEqualTo(BigDecimal.ZERO);
		assertThat(response.getBonusPointsToDeduct()).isEqualTo(10);
	}

	@Test
	void toRefundItemResponse_ShouldHandleEmptyTicketCode() {
		Ticket ticket = Ticket.builder().id(777L).uniqueCode("").build();

		RefundItem refundItem = RefundItem.builder().id(7L).ticket(ticket).ticketPrice(new BigDecimal("180.00"))
				.refundAmount(new BigDecimal("144.00")).build();

		RefundItemResponse response = refundItemMapper.toRefundItemResponse(refundItem);

		assertThat(response).isNotNull();
		assertThat(response.getTicketCode()).isEmpty();
	}

	@Test
	void toRefundItemResponse_ShouldHandleNullInput() {
		RefundItemResponse response = refundItemMapper.toRefundItemResponse(null);
		assertThat(response).isNull();
	}

	@Test
	void toRefundItemResponse_ShouldMapRefundItemWithoutBuilder() {
		Ticket ticket = new Ticket();
		ticket.setId(888L);
		ticket.setUniqueCode("TICKET-MNO888");

		RefundItem refundItem = new RefundItem();
		refundItem.setId(8L);
		refundItem.setTicket(ticket);
		refundItem.setTicketPrice(new BigDecimal("220.00"));
		refundItem.setRefundPercentage(new BigDecimal("90.00"));
		refundItem.setRefundAmount(new BigDecimal("198.00"));
		refundItem.setBonusPointsToDeduct(15);
		refundItem.setStatus(RefundItemStatus.PROCESSED);
		refundItem.setCreatedAt(LocalDateTime.of(2024, 1, 16, 10, 0));

		RefundItemResponse response = refundItemMapper.toRefundItemResponse(refundItem);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(8L);
		assertThat(response.getTicketId()).isEqualTo(888L);
		assertThat(response.getTicketCode()).isEqualTo("TICKET-MNO888");
		assertThat(response.getTicketPrice()).isEqualTo(new BigDecimal("220.00"));
		assertThat(response.getRefundPercentage()).isEqualTo(new BigDecimal("90.00"));
		assertThat(response.getRefundAmount()).isEqualTo(new BigDecimal("198.00"));
		assertThat(response.getBonusPointsToDeduct()).isEqualTo(15);
		assertThat(response.getStatus()).isEqualTo(RefundItemStatus.PROCESSED);
		assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 16, 10, 0));
	}

	@Test
	void toRefundItemResponse_ShouldMapAllRefundItemStatuses() {
		Ticket ticket = Ticket.builder().id(999L).uniqueCode("TICKET-STATUS").build();

		RefundItem pendingRefund = RefundItem.builder().id(9L).ticket(ticket).ticketPrice(new BigDecimal("300.00"))
				.status(RefundItemStatus.PENDING).build();

		RefundItem approvedRefund = RefundItem.builder().id(10L).ticket(ticket).ticketPrice(new BigDecimal("300.00"))
				.status(RefundItemStatus.APPROVED).build();

		RefundItem rejectedRefund = RefundItem.builder().id(11L).ticket(ticket).ticketPrice(new BigDecimal("300.00"))
				.status(RefundItemStatus.REJECTED).build();

		RefundItem processedRefund = RefundItem.builder().id(12L).ticket(ticket).ticketPrice(new BigDecimal("300.00"))
				.status(RefundItemStatus.PROCESSED).build();

		RefundItem cancelledRefund = RefundItem.builder().id(13L).ticket(ticket).ticketPrice(new BigDecimal("300.00"))
				.status(RefundItemStatus.CANCELLED).build();

		assertThat(refundItemMapper.toRefundItemResponse(pendingRefund).getStatus())
				.isEqualTo(RefundItemStatus.PENDING);
		assertThat(refundItemMapper.toRefundItemResponse(approvedRefund).getStatus())
				.isEqualTo(RefundItemStatus.APPROVED);
		assertThat(refundItemMapper.toRefundItemResponse(rejectedRefund).getStatus())
				.isEqualTo(RefundItemStatus.REJECTED);
		assertThat(refundItemMapper.toRefundItemResponse(processedRefund).getStatus())
				.isEqualTo(RefundItemStatus.PROCESSED);
		assertThat(refundItemMapper.toRefundItemResponse(cancelledRefund).getStatus())
				.isEqualTo(RefundItemStatus.CANCELLED);
	}
}