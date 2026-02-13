package ua.lviv.bas.cinema.service.booking.ticket;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.domain.projection.TicketInfoProjection;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.TicketMapper;
import ua.lviv.bas.cinema.repository.TicketRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketRetrievalService {

	private final TicketRepository ticketRepository;
	private final TicketMapper ticketMapper;

	public TicketResponse getTicketById(Long ticketId, User user) {
		return ticketRepository.findByIdAndUserIdAndStatus(ticketId, user.getId(), TicketStatus.ACTIVE)
				.map(this::toTicketResponse).orElseThrow(TicketValidationException::notFound);
	}

	public TicketResponse getTicketByCode(String ticketCode, User user) {
		return ticketRepository.findByUniqueCode(ticketCode)
				.filter(ticket -> ticket.getUser().getId().equals(user.getId())).map(this::toTicketResponse)
				.orElseThrow(() -> new TicketNotFoundException("Ticket not found with code: " + ticketCode));
	}

	public Page<TicketResponse> getUserTickets(User user, TicketFilterRequest filter, Pageable pageable) {
		LocalDateTime purchaseFrom = filter.getPurchaseDateFrom() != null ? filter.getPurchaseDateFrom().atStartOfDay()
				: null;
		LocalDateTime purchaseTo = filter.getPurchaseDateTo() != null ? filter.getPurchaseDateTo().atTime(23, 59, 59)
				: null;
		LocalDateTime sessionFrom = filter.getSessionDateFrom() != null ? filter.getSessionDateFrom().atStartOfDay()
				: null;
		LocalDateTime sessionTo = filter.getSessionDateTo() != null ? filter.getSessionDateTo().atTime(23, 59, 59)
				: null;

		Page<TicketInfoProjection> projections = ticketRepository.findUserTickets(user.getId(), filter.getStatus(),
				filter.getMovieId(), purchaseFrom, purchaseTo, sessionFrom, sessionTo, pageable);

		return projections.map(this::toTicketResponse);
	}

	private TicketResponse toTicketResponse(Ticket ticket) {
		TicketResponse response = ticketMapper.toTicketResponse(ticket);
		response.setQrCodeUrl(generateQrCodeUrl(ticket.getUniqueCode()));
		return response;
	}

	private TicketResponse toTicketResponse(TicketInfoProjection projection) {
		TicketResponse response = ticketMapper.toTicketResponse(projection);
		response.setQrCodeUrl(generateQrCodeUrl(projection.getUniqueCode()));
		return response;
	}

	private String generateQrCodeUrl(String ticketCode) {
		return "/api/tickets/" + ticketCode + "/qr";
	}
}