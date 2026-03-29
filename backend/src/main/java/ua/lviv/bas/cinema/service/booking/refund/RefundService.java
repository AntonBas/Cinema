package ua.lviv.bas.cinema.service.booking.refund;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.config.properties.RefundRules;
import ua.lviv.bas.cinema.domain.bonus.BonusTransactionType;
import ua.lviv.bas.cinema.domain.booking.Refund;
import ua.lviv.bas.cinema.domain.booking.RefundItem;
import ua.lviv.bas.cinema.domain.booking.status.PaymentStatus;
import ua.lviv.bas.cinema.domain.booking.status.RefundItemStatus;
import ua.lviv.bas.cinema.domain.booking.status.RefundStatus;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.dto.payment.response.PaymentResponse;
import ua.lviv.bas.cinema.dto.refund.request.RefundPreviewRequest;
import ua.lviv.bas.cinema.dto.refund.request.RefundRequest;
import ua.lviv.bas.cinema.dto.refund.response.RefundPreviewResponse;
import ua.lviv.bas.cinema.dto.refund.response.RefundResponse;
import ua.lviv.bas.cinema.exception.domain.financial.refund.RefundProcessingException;
import ua.lviv.bas.cinema.exception.domain.financial.refund.TicketNotRefundableException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.mapper.RefundItemMapper;
import ua.lviv.bas.cinema.mapper.RefundMapper;
import ua.lviv.bas.cinema.repository.booking.RefundRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.service.bonus.BonusService;
import ua.lviv.bas.cinema.service.booking.payment.PaymentProcessingService;
import ua.lviv.bas.cinema.service.integration.payment.PaymentGatewayService;
import ua.lviv.bas.cinema.service.shared.NumberGeneratorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {
	private final TicketRepository ticketRepository;
	private final RefundRepository refundRepository;
	private final PaymentProcessingService paymentProcessingService;
	private final PaymentGatewayService paymentGatewayService;
	private final BonusService bonusService;
	private final RefundRules refundRules;
	private final RefundMapper refundMapper;
	private final RefundItemMapper refundItemMapper;
	private final RefundCalculationService calculationService;
	private final RefundValidationService validationService;
	private final NumberGeneratorService numberGenerator;

	@Transactional(readOnly = true)
	public RefundPreviewResponse getRefundPreview(RefundPreviewRequest request, Long userId) {
		Ticket ticket = findActiveTicket(request.ticketId(), userId);

		String validationError = validationService.validateRefund(ticket);
		if (validationError != null) {
			return createNonRefundablePreview(ticket, validationError);
		}

		boolean isPaymentRefundable = checkPaymentRefundable(ticket);
		if (!isPaymentRefundable) {
			return createNonRefundablePreview(ticket, "Payment cannot be refunded via API. Contact support.");
		}

		return calculationService.createPreviewResponse(ticket, refundRules);
	}

	@Transactional
	public RefundResponse processRefund(RefundRequest request, Long userId) {
		Ticket ticket = findActiveTicket(request.ticketId(), userId);

		String validationError = validationService.validateRefund(ticket);
		if (validationError != null) {
			throw new TicketNotRefundableException(validationError);
		}

		boolean isPaymentRefundable = checkPaymentRefundable(ticket);
		if (!isPaymentRefundable) {
			throw new TicketNotRefundableException("Payment cannot be refunded via API. Contact support.");
		}

		LocalDateTime sessionTime = ticket.getBooking().getSession().getStartTime();
		BigDecimal percentage = refundRules.getRefundPercentage(sessionTime);

		BigDecimal refundAmount = calculationService.calculateRefundAmount(ticket.getFinalPrice(), percentage);
		Integer bonusPointsToRefund = calculationService.calculateBonusRefund(ticket.getBonusPointsUsed(), percentage);

		Refund refund = createRefundRecord(ticket, refundAmount, percentage, bonusPointsToRefund, request.reason());

		try {
			paymentProcessingService.refundPayment(refund.getPayment(), refundAmount,
					"Refund for ticket #" + ticket.getUniqueCode());

			if (bonusPointsToRefund > 0) {
				bonusService.createTransaction(bonusService.getOrCreateCard(refund.getUser()), bonusPointsToRefund,
						BonusTransactionType.REFUND_RETURN, "REFUND_TICKET_" + ticket.getId(), null, null, refund);
			}

			ticket.setStatus(TicketStatus.REFUNDED);
			ticket.setRefund(refund);
			ticketRepository.save(ticket);

			return createSuccessResponse(refund);

		} catch (Exception e) {
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

	private boolean checkPaymentRefundable(Ticket ticket) {
		try {
			PaymentResponse paymentStatus = paymentGatewayService
					.getPaymentStatus(ticket.getPayment().getLiqpayPaymentId());
			Boolean refundable = paymentStatus.refundableViaApi();
			return refundable != null ? refundable : paymentStatus.status() == PaymentStatus.SUCCESS;
		} catch (Exception e) {
			return false;
		}
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
				.totalBonusPointsToDeduct(bonusPointsToRefund).reason(reason).status(RefundStatus.PENDING)
				.processedAt(LocalDateTime.now()).processedBy("AUTO_SYSTEM").build();

		BigDecimal safePercentage = percentage.setScale(2, RoundingMode.HALF_UP);

		RefundItem refundItem = RefundItem.builder().refund(refund).ticket(ticket).ticketPrice(ticket.getFinalPrice())
				.refundPercentage(safePercentage).refundAmount(refundAmount).bonusPointsToDeduct(bonusPointsToRefund)
				.status(RefundItemStatus.PENDING).build();

		refund.getItems().add(refundItem);
		return refundRepository.save(refund);
	}

	private RefundResponse createSuccessResponse(Refund refund) {
		RefundResponse response = refundMapper.toRefundResponse(refund);
		return new RefundResponse(response.id(), numberGenerator.generateRefundNumber(refund), response.status(),
				response.totalAmount(), response.totalBonusPointsToDeduct(), response.reason(), response.processedBy(),
				response.processedAt(), response.createdAt(), response.paymentId(), "CARD",
				refund.getItems() != null
						? refund.getItems().stream().map(refundItemMapper::toRefundItemResponse).toList()
						: null,
				"Refund processed successfully", "3-5 business days");
	}
}