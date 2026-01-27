package ua.lviv.bas.cinema.service.booking.ticket;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.TicketMapper;
import ua.lviv.bas.cinema.repository.TicketRepository;

@Slf4j
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

	public List<TicketResponse> getUserTickets(User user, TicketStatus status) {
		List<Ticket> tickets;
		if (status != null) {
			tickets = ticketRepository.findByUserIdAndStatusOrderByPurchaseTimeDesc(user.getId(), status);
		} else {
			tickets = ticketRepository.findByUserIdOrderByPurchaseTimeDesc(user.getId());
		}

		return tickets.stream().map(this::toTicketResponse).collect(Collectors.toList());
	}

	public List<TicketResponse> getUpcomingTickets(User user) {
		LocalDateTime now = LocalDateTime.now();
		List<Ticket> tickets = ticketRepository.findUpcomingTickets(user.getId(), now);
		return tickets.stream().map(this::toTicketResponse).collect(Collectors.toList());
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