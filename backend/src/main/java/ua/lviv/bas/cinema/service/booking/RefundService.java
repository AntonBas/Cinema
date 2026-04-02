package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.booking.RefundItem;
import ua.lviv.bas.cinema.domain.booking.SeatReservation;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.booking.status.RefundItemStatus;
import ua.lviv.bas.cinema.domain.booking.status.RefundStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.exception.domain.financial.refund.RefundProcessingException;
import ua.lviv.bas.cinema.exception.domain.financial.refund.TicketNotRefundableException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.mapper.booking.RefundItemMapper;
import ua.lviv.bas.cinema.mapper.booking.RefundMapper;
import ua.lviv.bas.cinema.repository.booking.RefundRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {

	private final TicketRepository ticketRepository;
	private final RefundRepository refundRepository;
	private final PaymentService paymentService;
	private final BonusService bonusService;
	private final RefundRules refundRules;
	private final RefundMapper refundMapper;
	private final RefundItemMapper refundItemMapper;
	private final NumberGeneratorService numberGenerator;
	private final AuditService auditService;

	@Transactional(readOnly = true)
	public RefundPreviewResponse getRefundPreview(RefundPreviewRequest request, Long userId) {
		Ticket ticket = findActiveTicket(request.ticketId(), userId);

		String validationError = validateRefund(ticket);
		if (validationError != null) {
			return createNonRefundablePreview(ticket, validationError);
		}

		boolean isPaymentRefundable = checkPaymentRefundable(ticket);
		if (!isPaymentRefundable) {
			return createNonRefundablePreview(ticket, "Payment cannot be refunded via API. Contact support.");
		}

		return createPreviewResponse(ticket);
	}

	@Transactional
	public RefundResponse processRefund(RefundRequest request, Long userId) {
		Ticket ticket = findActiveTicket(request.ticketId(), userId);

		String validationError = validateRefund(ticket);
		if (validationError != null) {
			throw new TicketNotRefundableException(validationError);
		}

		boolean isPaymentRefundable = checkPaymentRefundable(ticket);
		if (!isPaymentRefundable) {
			throw new TicketNotRefundableException("Payment cannot be refunded via API. Contact support.");
		}

		LocalDateTime sessionTime = ticket.getBooking().getSession().getStartTime();
		BigDecimal percentage = refundRules.getRefundPercentage(sessionTime);

		BigDecimal refundAmount = calculateRefundAmount(ticket.getFinalPrice(), percentage);
		Integer bonusPointsToRefund = calculateBonusRefund(ticket.getBonusPointsUsed(), percentage);

		Refund refund = createRefundRecord(ticket, refundAmount, percentage, bonusPointsToRefund, request.reason());

		try {
			paymentService.refundPayment(refund.getPayment(), refundAmount,
					"Refund for ticket #" + ticket.getUniqueCode());

			if (bonusPointsToRefund != null && bonusPointsToRefund > 0) {
				bonusService.createTransaction(bonusService.getOrCreateCard(refund.getUser()), bonusPointsToRefund,
						BonusTransactionType.REFUND_RETURN, "REFUND_TICKET_" + ticket.getId());
			}

			ticket.setStatus(TicketStatus.REFUNDED);
			ticket.setRefund(refund);
			ticketRepository.save(ticket);

			Map<String, Object> details = new HashMap<>();
			details.put("ticketId", ticket.getId());
			details.put("refundAmount", refundAmount);
			details.put("percentage", percentage);
			details.put("bonusPointsToRefund", bonusPointsToRefund);

			auditService.logChange("Refund", refund.getId(), "Refund #" + refund.getId(), AuditAction.CREATED, null,
					details);

			return buildRefundResponse(refund);

		} catch (Exception e) {
			refund.setStatus(RefundStatus.REJECTED);
			refundRepository.save(refund);

			Map<String, Object> errorDetails = new HashMap<>();
			errorDetails.put("error", e.getMessage());

			auditService.logChange("Refund", refund.getId(), "Refund #" + refund.getId(), AuditAction.REJECTED, null,
					errorDetails);

			throw new RefundProcessingException("Refund processing failed", e);
		}
	}

	@Transactional(readOnly = true)
	public List<RefundResponse> getUserRefunds(Long userId) {
		List<Refund> refunds = refundRepository.findByUserIdOrderByCreatedDateDesc(userId);
		return refunds.stream().map(this::buildRefundResponse).collect(Collectors.toList());
	}

	private String validateRefund(Ticket ticket) {
		if (ticket.getStatus() != TicketStatus.ACTIVE) {
			return "Ticket is not active. Current status: " + ticket.getStatus();
		}

		LocalDateTime sessionTime = ticket.getBooking().getSession().getStartTime();
		if (!refundRules.isRefundable(sessionTime)) {
			return "Refund is not available for this session";
		}

		if (ticket.getRefund() != null) {
			return "Ticket has already been refunded";
		}

		if (sessionTime.isBefore(LocalDateTime.now())) {
			return "Session has already started or finished";
		}

		return null;
	}

	private BigDecimal calculateRefundAmount(BigDecimal price, BigDecimal percentage) {
		return price.multiply(percentage).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
	}

	private Integer calculateBonusRefund(Integer bonusPointsUsed, BigDecimal percentage) {
		if (bonusPointsUsed == null || bonusPointsUsed == 0) {
			return 0;
		}
		return (int) (bonusPointsUsed * percentage.doubleValue() / 100);
	}

	private String formatRemainingTime(LocalDateTime sessionTime) {
		long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), sessionTime);
		long minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), sessionTime) % 60;

		if (hours > 0 && minutes > 0) {
			return String.format("%d hours %d minutes", hours, minutes);
		} else if (hours > 0) {
			return String.format("%d hours", hours);
		} else if (minutes > 0) {
			return String.format("%d minutes", minutes);
		} else {
			return "Less than a minute";
		}
	}

	private RefundPreviewResponse createPreviewResponse(Ticket ticket) {
		LocalDateTime sessionTime = ticket.getBooking().getSession().getStartTime();
		BigDecimal percentage = refundRules.getRefundPercentage(sessionTime);
		BigDecimal refundAmount = calculateRefundAmount(ticket.getFinalPrice(), percentage);
		BigDecimal feeAmount = ticket.getFinalPrice().subtract(refundAmount);

		String seatInfo = "N/A";
		if (ticket.getBooking().getSeatReservations() != null && !ticket.getBooking().getSeatReservations().isEmpty()) {
			SeatReservation bookedSeat = ticket.getBooking().getSeatReservations().get(0);
			seatInfo = String.format("Row %d, Seat %d", bookedSeat.getSeat().getRow(),
					bookedSeat.getSeat().getNumber());
		}

		return new RefundPreviewResponse(ticket.getId(), ticket.getUniqueCode(),
				ticket.getBooking().getSession().getMovie().getTitle(), sessionTime,
				ticket.getBooking().getSession().getHall().getName(), seatInfo, ticket.getOriginalPrice(),
				ticket.getFinalPrice(), refundAmount, percentage, feeAmount,
				BigDecimal.valueOf(100).subtract(percentage), ticket.getBonusPointsUsed(),
				calculateBonusRefund(ticket.getBonusPointsUsed(), percentage), refundRules.getPolicyName(sessionTime),
				refundRules.getPolicyDescription(sessionTime), true, null, sessionTime.minusMinutes(30),
				formatRemainingTime(sessionTime), ticket.getPurchaseTime().toString(),
				ticket.getTicketType().getDisplayName());
	}

	private Ticket findActiveTicket(Long ticketId, Long userId) {
		return ticketRepository.findByIdAndUserIdAndStatus(ticketId, userId, TicketStatus.ACTIVE).orElseThrow(
				() -> new TicketNotFoundException("Ticket not found or not active. Ticket ID: " + ticketId));
	}

	private boolean checkPaymentRefundable(Ticket ticket) {
		return ticket.getPayment().getStatus() == PaymentStatus.SUCCESS;
	}

	private RefundPreviewResponse createNonRefundablePreview(Ticket ticket, String reason) {
		return new RefundPreviewResponse(ticket.getId(), ticket.getUniqueCode(),
				ticket.getBooking().getSession().getMovie().getTitle(), ticket.getBooking().getSession().getStartTime(),
				null, null, null, null, null, null, null, null, null, null, null, null, false, reason, null, null, null,
				null);
	}

	private Refund createRefundRecord(Ticket ticket, BigDecimal refundAmount, BigDecimal percentage,
			Integer bonusPointsToRefund, String reason) {
		Refund refund = Refund.builder().payment(ticket.getPayment()).user(ticket.getUser()).totalAmount(refundAmount)
				.totalBonusPointsToDeduct(bonusPointsToRefund).reason(reason).status(RefundStatus.PENDING).build();

		RefundItem refundItem = RefundItem.builder().refund(refund).ticket(ticket).ticketPrice(ticket.getFinalPrice())
				.refundPercentage(percentage.setScale(2, RoundingMode.HALF_UP)).refundAmount(refundAmount)
				.bonusPointsToDeduct(bonusPointsToRefund).status(RefundItemStatus.PENDING).build();

		refund.getItems().add(refundItem);
		return refundRepository.save(refund);
	}

	private RefundResponse buildRefundResponse(Refund refund) {
		RefundResponse response = refundMapper.toRefundResponse(refund);
		return new RefundResponse(
				response.id(), numberGenerator.generateRefundNumber(refund), response.status(), response.totalAmount(),
				response.totalBonusPointsToDeduct(), response.reason(), response.processedBy(), response.processedAt(),
				response.createdAt(), response.paymentId(), "CARD", refund.getItems() != null ? refund.getItems()
						.stream().map(refundItemMapper::toRefundItemResponse).collect(Collectors.toList()) : null,
				"Refund processed successfully", "3-5 business days");
	}
}