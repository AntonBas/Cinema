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
		Ticket ticket = ticketRepository.findById(ticketId)
				.orElseThrow(() -> new TicketNotFoundException("Ticket not found with id: " + ticketId));

		if (!ticket.getUser().getId().equals(user.getId())) {
			throw TicketValidationException.notFound();
		}

		return toTicketResponse(ticket);
	}

	public TicketResponse getTicketByCode(String ticketCode, User user) {
		Ticket ticket = ticketRepository.findByUniqueCode(ticketCode)
				.orElseThrow(() -> new TicketNotFoundException("Ticket not found with code: " + ticketCode));

		if (!ticket.getUser().getId().equals(user.getId())) {
			throw TicketValidationException.notFound();
		}

		return toTicketResponse(ticket);
	}

	public Page<TicketResponse> getUserTickets(User user, TicketStatus status, String search, Pageable pageable) {
		Page<Ticket> tickets;

		if (status != null) {
			switch (status) {
			case ACTIVE:
				tickets = ticketRepository.findActiveByUserId(user.getId(), search, pageable);
				break;
			case USED:
				tickets = ticketRepository.findUsedByUserId(user.getId(), search, pageable);
				break;
			case REFUNDED:
				tickets = ticketRepository.findRefundedByUserId(user.getId(), search, pageable);
				break;
			default:
				tickets = ticketRepository.findAllByUserId(user.getId(), search, pageable);
				break;
			}
		} else {
			tickets = ticketRepository.findAllByUserId(user.getId(), search, pageable);
		}

		return tickets.map(this::toTicketResponse);
	}

	public Page<TicketResponse> getUpcomingTickets(User user, String search, Pageable pageable) {
		LocalDateTime now = LocalDateTime.now();
		Page<Ticket> tickets = ticketRepository.findUpcomingByUserId(user.getId(), now, search, pageable);
		return tickets.map(this::toTicketResponse);
	}

	private TicketResponse toTicketResponse(Ticket ticket) {
		TicketResponse response = ticketMapper.toTicketResponse(ticket);
		response.setQrCodeUrl(generateQrCodeUrl(ticket.getUniqueCode()));
		return response;
	}

	private String generateQrCodeUrl(String ticketCode) {
		return "/api/tickets/" + ticketCode + "/qr";
	}
}