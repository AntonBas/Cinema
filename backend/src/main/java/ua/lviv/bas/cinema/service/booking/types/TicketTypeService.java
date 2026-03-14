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
	public TicketTypeResponse createTicketType(TicketTypeCreateRequest createRequest) {
		log.debug("Creating ticket type: {}", createRequest.getDisplayName());

		validationService.validateAgeRange(createRequest.getMinAge(), createRequest.getMaxAge());

		if (ticketTypeRepository.existsByDisplayName(createRequest.getDisplayName())) {
			throw new TicketTypeDuplicateException(createRequest.getDisplayName());
		}

		TicketType ticketType = ticketTypeMapper.toTicketType(createRequest);
		TicketType saved = ticketTypeRepository.save(ticketType);
		return ticketTypeMapper.toTicketTypeResponse(saved);
	}

	public TicketTypeResponse getTicketTypeById(Long id) {
		TicketType ticketType = ticketTypeRepository.findById(id)
				.orElseThrow(() -> new TicketTypeNotFoundException(id));
		return ticketTypeMapper.toTicketTypeResponse(ticketType);
	}

	public Page<TicketTypeAdminProjection> getTicketTypesForAdmin(Boolean active, TicketTypeCategory category,
			String search, Pageable pageable) {
		return ticketTypeRepository.findAdminProjections(active, category, search, pageable);
	}

	public List<TicketTypeUserResponse> getActiveTicketTypesForUser() {
		List<TicketTypeUserProjection> projections = ticketTypeRepository.findUserProjections();
		return projections.stream().map(ticketTypeMapper::toTicketTypeUserResponse).collect(Collectors.toList());
	}

	@Transactional
	public TicketTypeResponse updateTicketType(Long id, TicketTypeUpdateRequest updateRequest) {
		log.debug("Updating ticket type: {}", id);

		TicketType ticketType = ticketTypeRepository.findById(id)
				.orElseThrow(() -> new TicketTypeNotFoundException(id));

		if (updateRequest.getDisplayName() != null
				&& !updateRequest.getDisplayName().equals(ticketType.getDisplayName())
				&& ticketTypeRepository.existsByDisplayNameAndIdNot(updateRequest.getDisplayName(), id)) {
			throw new TicketTypeDuplicateException(updateRequest.getDisplayName());
		}

		if (updateRequest.getMinAge() != null || updateRequest.getMaxAge() != null) {
			Integer minAge = updateRequest.getMinAge() != null ? updateRequest.getMinAge() : ticketType.getMinAge();
			Integer maxAge = updateRequest.getMaxAge() != null ? updateRequest.getMaxAge() : ticketType.getMaxAge();
			validationService.validateAgeRange(minAge, maxAge);
		}

		ticketTypeMapper.updateTicketTypeFromRequest(ticketType, updateRequest);
		TicketType updated = ticketTypeRepository.save(ticketType);
		return ticketTypeMapper.toTicketTypeResponse(updated);
	}

	@Transactional
	public void deleteTicketType(Long id) {
		TicketType ticketType = ticketTypeRepository.findById(id)
				.orElseThrow(() -> new TicketTypeNotFoundException(id));

		if (hasFutureTicketsWithType(id)) {
			long ticketCount = countFutureTicketsWithType(id);
			throw new TicketTypeInUseException(id,
					"Cannot delete ticket type. It is used in " + ticketCount + " future ticket(s)");
		}

		ticketTypeRepository.delete(ticketType);
		log.info("Deleted ticket type: {}", id);
	}

	@Transactional
	public TicketTypeResponse toggleTicketTypeActiveStatus(Long id) {
		TicketType ticketType = ticketTypeRepository.findById(id)
				.orElseThrow(() -> new TicketTypeNotFoundException(id));

		if (ticketType.isActive() && hasFutureTicketsWithType(id)) {
			long activeTicketCount = countFutureTicketsWithType(id);
			throw new TicketTypeInUseException(id,
					"Cannot deactivate ticket type. It is used in " + activeTicketCount + " future ticket(s)");
		}

		ticketType.setActive(!ticketType.isActive());
		TicketType updated = ticketTypeRepository.save(ticketType);
		return ticketTypeMapper.toTicketTypeResponse(updated);
	}

	public boolean validateAgeForTicketType(Long ticketTypeId, Integer age) {
		TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
				.orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));
		return validationService.isAgeValidForTicketType(ticketType, age);
	}

	private boolean hasFutureTicketsWithType(Long ticketTypeId) {
		return countFutureTicketsWithType(ticketTypeId) > 0;
	}

	private long countFutureTicketsWithType(Long ticketTypeId) {
		LocalDateTime now = LocalDateTime.now();
		List<TicketStatus> activeStatuses = List.of(TicketStatus.ACTIVE);
		return ticketRepository.countByTicketTypeIdAndStatusInAndBookingSessionStartTimeAfter(ticketTypeId,
				activeStatuses, now);
	}
}