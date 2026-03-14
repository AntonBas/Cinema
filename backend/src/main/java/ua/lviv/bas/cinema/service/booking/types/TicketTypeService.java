package ua.lviv.bas.cinema.service.booking.types;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.domain.projection.TicketTypeAdminProjection;
import ua.lviv.bas.cinema.domain.projection.TicketTypeUserProjection;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeUserResponse;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.mapper.TicketTypeMapper;
import ua.lviv.bas.cinema.repository.TicketRepository;
import ua.lviv.bas.cinema.repository.TicketTypeRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketTypeService {

	private final TicketTypeRepository ticketTypeRepository;
	private final TicketRepository ticketRepository;
	private final TicketTypeMapper ticketTypeMapper;
	private final TicketTypeValidationService validationService;

	@Transactional
	public TicketTypeResponse createTicketType(TicketTypeCreateRequest request) {
		validationService.validateAgeRange(request.getMinAge(), request.getMaxAge());

		if (ticketTypeRepository.existsByDisplayName(request.getDisplayName())) {
			throw new TicketTypeDuplicateException(request.getDisplayName());
		}

		TicketType ticketType = ticketTypeMapper.toTicketType(request);
		return ticketTypeMapper.toTicketTypeResponse(ticketTypeRepository.save(ticketType));
	}

	public TicketTypeResponse getTicketTypeById(Long id) {
		return ticketTypeMapper.toTicketTypeResponse(findTicketTypeById(id));
	}

	public Page<TicketTypeResponse> getTicketTypesForAdmin(Boolean active, TicketTypeCategory category, String search,
			Pageable pageable) {
		Page<TicketTypeAdminProjection> projections = ticketTypeRepository.findAdminProjections(active, category,
				search, pageable);
		return projections.map(ticketTypeMapper::toTicketTypeResponse);
	}

	public List<TicketTypeUserResponse> getActiveTicketTypesForUser() {
		List<TicketTypeUserProjection> projections = ticketTypeRepository.findUserProjections();
		return projections.stream().map(ticketTypeMapper::toTicketTypeUserResponse).collect(Collectors.toList());
	}

	@Transactional
	public TicketTypeResponse updateTicketType(Long id, TicketTypeUpdateRequest request) {
		TicketType ticketType = findTicketTypeById(id);

		if (request.getDisplayName() != null && !request.getDisplayName().equals(ticketType.getDisplayName())
				&& ticketTypeRepository.existsByDisplayNameAndIdNot(request.getDisplayName(), id)) {
			throw new TicketTypeDuplicateException(request.getDisplayName());
		}

		if (request.getMinAge() != null || request.getMaxAge() != null) {
			Integer minAge = request.getMinAge() != null ? request.getMinAge() : ticketType.getMinAge();
			Integer maxAge = request.getMaxAge() != null ? request.getMaxAge() : ticketType.getMaxAge();
			validationService.validateAgeRange(minAge, maxAge);
		}

		ticketTypeMapper.updateTicketTypeFromRequest(ticketType, request);
		return ticketTypeMapper.toTicketTypeResponse(ticketTypeRepository.save(ticketType));
	}

	@Transactional
	public void deleteTicketType(Long id) {
		TicketType ticketType = findTicketTypeById(id);

		if (hasFutureTickets(id)) {
			throw new TicketTypeInUseException(id,
					"Cannot delete ticket type. It is used in " + countFutureTickets(id) + " future ticket(s)");
		}

		ticketTypeRepository.delete(ticketType);
	}

	@Transactional
	public TicketTypeResponse toggleTicketTypeActiveStatus(Long id) {
		TicketType ticketType = findTicketTypeById(id);

		if (ticketType.isActive() && hasFutureTickets(id)) {
			throw new TicketTypeInUseException(id,
					"Cannot deactivate ticket type. It is used in " + countFutureTickets(id) + " future ticket(s)");
		}

		ticketType.setActive(!ticketType.isActive());
		return ticketTypeMapper.toTicketTypeResponse(ticketTypeRepository.save(ticketType));
	}

	private TicketType findTicketTypeById(Long id) {
		return ticketTypeRepository.findById(id).orElseThrow(() -> new TicketTypeNotFoundException(id));
	}

	private boolean hasFutureTickets(Long ticketTypeId) {
		return countFutureTickets(ticketTypeId) > 0;
	}

	private long countFutureTickets(Long ticketTypeId) {
		return ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(ticketTypeId,
				List.of(TicketStatus.ACTIVE), LocalDateTime.now());
	}
}