package ua.lviv.bas.cinema.service.booking.ticket;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.lviv.bas.cinema.domain.ticket.Ticket;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.ticket.request.TicketFilterRequest;
import ua.lviv.bas.cinema.dto.ticketType.response.TicketResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketValidationException;
import ua.lviv.bas.cinema.mapper.ticket.TicketMapper;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.repository.ticket.specification.TicketSpecification;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "tickets")
public class TicketRetrievalService {

	private final TicketRepository ticketRepository;
	private final TicketSpecification ticketSpecification;
	private final TicketMapper ticketMapper;

	@Cacheable(key = "#ticketId + '-' + #user.id")
	public TicketResponse getTicketById(Long ticketId, User user) {
		return ticketRepository.findByIdAndUserIdAndStatus(ticketId, user.getId(), TicketStatus.ACTIVE)
				.map(this::toTicketResponse).orElseThrow(TicketValidationException::notFound);
	}

	@Cacheable(key = "#ticketCode + '-' + #user.id")
	public TicketResponse getTicketByCode(String ticketCode, User user) {
		Ticket ticket = ticketRepository.findByUniqueCode(ticketCode)
				.orElseThrow(() -> new TicketNotFoundException("Ticket not found with code: " + ticketCode));

		if (!ticket.getUser().getId().equals(user.getId())) {
			throw TicketValidationException.notFound();
		}

		return toTicketResponse(ticket);
	}

	@Cacheable(key = "'user:' + #user.id + '-status:' + #filter.status() + '-movie:' + #filter.movieTitle() + '-page:' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<TicketResponse> getUserTickets(User user, TicketFilterRequest filter, Pageable pageable) {
		Specification<Ticket> spec = ticketSpecification.buildForUser(user.getId(), filter.status(),
				filter.movieTitle());

		Page<Ticket> tickets = ticketRepository.findAll(spec, pageable);

		return tickets.map(this::toTicketResponse);
	}

	private TicketResponse toTicketResponse(Ticket ticket) {
		TicketResponse response = ticketMapper.toTicketResponse(ticket);
		String qrCodeUrl = generateQrCodeUrl(ticket.getUniqueCode());
		return new TicketResponse(response.id(), response.ticketCode(), qrCodeUrl, response.status(),
				response.purchaseTime(), response.price(), response.ticketType(), response.movieTitle(),
				response.sessionTime(), response.hallName(), response.row(), response.seatNumber());
	}

	private String generateQrCodeUrl(String ticketCode) {
		return "/api/tickets/" + ticketCode + "/qr";
	}
}