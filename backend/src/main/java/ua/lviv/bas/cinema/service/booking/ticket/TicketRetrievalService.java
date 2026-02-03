package ua.lviv.bas.cinema.service.booking.ticket;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.Ticket;
import ua.lviv.bas.cinema.domain.User;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.domain.specification.TicketSpecification;
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
	private final TicketSpecification ticketSpecification;

	public TicketResponse getTicketById(Long ticketId, User user) {
		Ticket ticket = ticketRepository.findByIdAndUserIdAndStatus(ticketId, user.getId(), TicketStatus.ACTIVE)
				.orElseThrow(() -> TicketValidationException.notFound());

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

	public Page<TicketResponse> getUserTickets(User user, TicketFilterRequest filter, Pageable pageable) {
		TicketFilterRequest userFilter = TicketFilterRequest.builder().userId(user.getId()).status(filter.getStatus())
				.purchaseDateFrom(filter.getPurchaseDateFrom()).purchaseDateTo(filter.getPurchaseDateTo())
				.sessionDateFrom(filter.getSessionDateFrom()).sessionDateTo(filter.getSessionDateTo())
				.movieId(filter.getMovieId()).build();

		Specification<Ticket> spec = ticketSpecification.build(userFilter);
		Page<Ticket> tickets = ticketRepository.findAll(spec, pageable);

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