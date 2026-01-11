package ua.lviv.bas.cinema.service.booking;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.RefundRules;
import ua.lviv.bas.cinema.domain.BookedSeat;
import ua.lviv.bas.cinema.domain.Refund;
import ua.lviv.bas.cinema.domain.RefundItem;
import ua.lviv.bas.cinema.domain.Ticket;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefundService {

	private final TicketRepository ticketRepository;
	private final RefundRepository refundRepository;
	private final PaymentService paymentService;
	private final BonusService bonusService;
	private final RefundRules refundRules;
	private final RefundMapper refundMapper;
	private final RefundItemMapper refundItemMapper;

	@Transactional(readOnly = true)
	public RefundPreviewResponse getRefundPreview(RefundPreviewRequest request, Long userId) {
		Ticket ticket = findActiveTicket(request.getTicketId(), userId);

		String validationError = validateRefund(ticket);
		if (validationError != null) {
			return createNonRefundablePreview(ticket, validationError);
		}

		return createPreviewResponse(ticket);
	}

	public RefundResponse processRefund(RefundRequest request, Long userId) {
		Ticket ticket = findActiveTicket(request.getTicketId(), userId);

		String validationError = validateRefund(ticket);
		if (validationError != null) {
			throw new TicketNotRefundableException(validationError);
		}

		LocalDateTime sessionTime = ticket.getBooking().getSession().getStartTime();
		BigDecimal percentage = refundRules.getRefundPercentage(sessionTime);
		BigDecimal refundAmount = calculateRefundAmount(ticket.getFinalPrice(), percentage);
		Integer bonusPointsToRefund = calculateBonusRefund(ticket.getBonusPointsUsed(), percentage);

		Refund refund = createRefundRecord(ticket, refundAmount, percentage, bonusPointsToRefund, request.getReason());

		try {
			paymentService.refundPayment(refund.getPayment(), refundAmount,
					"Refund for ticket #" + ticket.getUniqueCode());

			if (bonusPointsToRefund > 0) {
				bonusService.createBonusTransaction(bonusService.findOrCreateBonusCard(refund.getUser()),
						bonusPointsToRefund, ua.lviv.bas.cinema.domain.enums.BonusTransactionType.REFUND_RETURN,
						"REFUND_TICKET_" + ticket.getId(), null, null, refund);
			}

			ticket.setStatus(TicketStatus.REFUNDED);
			ticket.setRefund(refund);
			ticketRepository.save(ticket);

			log.info("Refund processed: refundId={}, ticketId={}", refund.getId(), ticket.getId());
			return createSuccessResponse(refund);

		} catch (Exception e) {
			log.error("Refund processing failed: refundId={}", refund.getId(), e);
			refund.setStatus(RefundStatus.REJECTED);
			refundRepository.save(refund);
			throw new RefundProcessingException("Refund processing failed", e);
		}
	}

	@Transactional(readOnly = true)
	public List<RefundResponse> getUserRefunds(Long userId) {
		List<Refund> refunds = refundRepository.findByUserIdOrderByCreatedAtDesc(userId);
		return refunds.stream().map(this::createSuccessResponse).toList();
	}

	private Ticket findActiveTicket(Long ticketId, Long userId) {
		return ticketRepository.findByIdAndUserIdAndStatus(ticketId, userId, TicketStatus.ACTIVE).orElseThrow(
				() -> new TicketNotFoundException("Ticket not found or not active. Ticket ID: " + ticketId));
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

	private RefundPreviewResponse createPreviewResponse(Ticket ticket) {
		LocalDateTime sessionTime = ticket.getBooking().getSession().getStartTime();
		BigDecimal percentage = refundRules.getRefundPercentage(sessionTime);
		BigDecimal refundAmount = calculateRefundAmount(ticket.getFinalPrice(), percentage);
		BigDecimal feeAmount = ticket.getFinalPrice().subtract(refundAmount);

		String seatInfo = "N/A";
		if (ticket.getBooking().getBookedSeats() != null && !ticket.getBooking().getBookedSeats().isEmpty()) {
			BookedSeat bookedSeat = ticket.getBooking().getBookedSeats().get(0);
			seatInfo = String.format("Row %d, Seat %d", bookedSeat.getSeat().getRow(),
					bookedSeat.getSeat().getNumber());
		}

		return RefundPreviewResponse.builder().ticketId(ticket.getId()).ticketCode(ticket.getUniqueCode())
				.movieTitle(ticket.getBooking().getSession().getMovie().getTitle()).sessionTime(sessionTime)
				.hallName(ticket.getBooking().getSession().getHall().getName()).seatInfo(seatInfo)
				.originalPrice(ticket.getOriginalPrice()).finalPrice(ticket.getFinalPrice()).refundAmount(refundAmount)
				.refundPercentage(percentage).feeAmount(feeAmount)
				.feePercentage(BigDecimal.valueOf(100).subtract(percentage))
				.bonusPointsUsed(ticket.getBonusPointsUsed())
				.bonusPointsToRefund(calculateBonusRefund(ticket.getBonusPointsUsed(), percentage))
				.policyName(refundRules.getPolicyName(sessionTime))
				.policyDescription(refundRules.getPolicyDescription(sessionTime)).isRefundable(true)
				.refundDeadline(sessionTime.minusMinutes(30)).remainingTime(formatRemainingTime(sessionTime))
				.purchaseTime(ticket.getPurchaseTime().toString()).ticketType(ticket.getTicketType().getDisplayName())
				.build();
	}

	private RefundPreviewResponse createNonRefundablePreview(Ticket ticket, String reason) {
		return RefundPreviewResponse.builder().ticketId(ticket.getId()).ticketCode(ticket.getUniqueCode())
				.movieTitle(ticket.getBooking().getSession().getMovie().getTitle())
				.sessionTime(ticket.getBooking().getSession().getStartTime()).isRefundable(false)
				.nonRefundableReason(reason).build();
	}

	private Refund createRefundRecord(Ticket ticket, BigDecimal refundAmount, BigDecimal percentage,
			Integer bonusPointsToRefund, String reason) {
		Refund refund = Refund.builder().payment(ticket.getPayment()).user(ticket.getUser()).totalAmount(refundAmount)
				.totalBonusPointsToDeduct(bonusPointsToRefund).reason(reason).status(RefundStatus.PROCESSED)
				.processedAt(LocalDateTime.now()).processedBy("AUTO_SYSTEM").build();

		RefundItem refundItem = RefundItem.builder().refund(refund).ticket(ticket).ticketPrice(ticket.getFinalPrice())
				.refundPercentage(percentage).refundAmount(refundAmount).bonusPointsToDeduct(bonusPointsToRefund)
				.status(RefundItemStatus.PROCESSED).build();

		refund.getItems().add(refundItem);
		return refundRepository.save(refund);
	}

	private RefundResponse createSuccessResponse(Refund refund) {
		RefundResponse response = refundMapper.toResponse(refund);
		response.setRefundNumber(generateRefundNumber(refund.getId()));
		response.setPaymentMethod("CARD");
		response.setMessage("Refund processed successfully");
		response.setEstimatedRefundTime("3-5 business days");

		if (refund.getItems() != null) {
			response.setItems(refund.getItems().stream().map(refundItemMapper::toResponse).toList());
		}

		return response;
	}

	private String generateRefundNumber(Long refundId) {
		return String.format("RF-%04d-%06d", LocalDateTime.now().getYear(), refundId);
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
}