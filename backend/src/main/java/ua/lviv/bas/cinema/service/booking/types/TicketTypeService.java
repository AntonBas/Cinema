package ua.lviv.bas.cinema.service.booking.types;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import ua.lviv.bas.cinema.dto.common.PageResponse;
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
@CacheConfig(cacheNames = "ticket-types")
public class TicketTypeService {

	private final TicketTypeRepository ticketTypeRepository;
	private final TicketRepository ticketRepository;
	private final TicketTypeMapper ticketTypeMapper;
	private final TicketTypeValidationService validationService;

	@CacheEvict(allEntries = true)
	@Transactional
	public TicketTypeResponse createTicketType(TicketTypeCreateRequest request) {
		log.info("Creating ticket type: {}", request.displayName());

		validationService.validateAgeRange(request.minAge(), request.maxAge());

		if (ticketTypeRepository.existsByDisplayName(request.displayName())) {
			throw new TicketTypeDuplicateException(request.displayName());
		}

		TicketType ticketType = ticketTypeMapper.toTicketType(request);
		TicketType saved = ticketTypeRepository.save(ticketType);

		log.debug("Ticket type created with ID: {}", saved.getId());
		return ticketTypeMapper.toTicketTypeResponse(saved);
	}

	@Cacheable(key = "#id")
	public TicketTypeResponse getTicketTypeById(Long id) {
		log.debug("Retrieving ticket type by id: {}", id);
		return ticketTypeMapper.toTicketTypeResponse(findTicketTypeById(id));
	}

	@Cacheable(key = "'admin-' + #active + '-' + #category + '-' + #search + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public PageResponse<TicketTypeResponse> getTicketTypesForAdmin(Boolean active, TicketTypeCategory category,
			String search, Pageable pageable) {
		log.info("Getting ticket types for admin with filters - active: {}, category: {}, search: {}", active, category,
				search);

		Page<TicketTypeAdminProjection> projections = ticketTypeRepository.findAdminProjections(active, category,
				search, pageable);
		Page<TicketTypeResponse> responsePage = projections.map(ticketTypeMapper::toTicketTypeResponse);
		return PageResponse.from(responsePage);
	}

	@Cacheable(key = "'user-active'")
	public List<TicketTypeUserResponse> getActiveTicketTypesForUser() {
		log.debug("Getting active ticket types for user");

		List<TicketTypeUserProjection> projections = ticketTypeRepository.findUserProjections();
		return projections.stream().map(ticketTypeMapper::toTicketTypeUserResponse).collect(Collectors.toList());
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(key = "'user-active'"), @CacheEvict(allEntries = true) })
	@Transactional
	public TicketTypeResponse updateTicketType(Long id, TicketTypeUpdateRequest request) {
		log.info("Updating ticket type with id: {}", id);

		TicketType ticketType = findTicketTypeById(id);

		if (request.displayName() != null && !request.displayName().equals(ticketType.getDisplayName())
				&& ticketTypeRepository.existsByDisplayNameAndIdNot(request.displayName(), id)) {
			throw new TicketTypeDuplicateException(request.displayName());
		}

		if (request.minAge() != null || request.maxAge() != null) {
			Integer minAge = request.minAge() != null ? request.minAge() : ticketType.getMinAge();
			Integer maxAge = request.maxAge() != null ? request.maxAge() : ticketType.getMaxAge();
			validationService.validateAgeRange(minAge, maxAge);
		}

		ticketTypeMapper.updateTicketTypeFromRequest(ticketType, request);
		TicketType updated = ticketTypeRepository.save(ticketType);

		log.debug("Ticket type updated with ID: {}", updated.getId());
		return ticketTypeMapper.toTicketTypeResponse(updated);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(key = "'user-active'"), @CacheEvict(allEntries = true) })
	@Transactional
	public void deleteTicketType(Long id) {
		log.info("Deleting ticket type with id: {}", id);

		TicketType ticketType = findTicketTypeById(id);

		if (hasFutureTickets(id)) {
			throw new TicketTypeInUseException(id,
					"Cannot delete ticket type. It is used in " + countFutureTickets(id) + " future ticket(s)");
		}

		ticketTypeRepository.delete(ticketType);
		log.debug("Ticket type deleted with ID: {}", id);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(key = "'user-active'"), @CacheEvict(allEntries = true) })
	@Transactional
	public TicketTypeResponse toggleTicketTypeActiveStatus(Long id) {
		log.info("Toggling active status for ticket type with id: {}", id);

		TicketType ticketType = findTicketTypeById(id);

		if (ticketType.isActive() && hasFutureTickets(id)) {
			throw new TicketTypeInUseException(id,
					"Cannot deactivate ticket type. It is used in " + countFutureTickets(id) + " future ticket(s)");
		}

		ticketType.setActive(!ticketType.isActive());
		TicketType updated = ticketTypeRepository.save(ticketType);

		log.debug("Ticket type status toggled to: {} for ID: {}", updated.isActive(), id);
		return ticketTypeMapper.toTicketTypeResponse(updated);
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