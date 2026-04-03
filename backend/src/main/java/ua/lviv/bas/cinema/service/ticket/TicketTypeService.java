package ua.lviv.bas.cinema.service.ticket;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import ua.lviv.bas.cinema.domain.audit.AuditAction;
import ua.lviv.bas.cinema.domain.ticket.TicketStatus;
import ua.lviv.bas.cinema.domain.ticket.TicketType;
import ua.lviv.bas.cinema.domain.ticket.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.ticketType.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticketType.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticketType.response.TicketTypeAdminResponse;
import ua.lviv.bas.cinema.dto.ticketType.response.TicketTypeUserResponse;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeValidationException;
import ua.lviv.bas.cinema.mapper.ticket.TicketTypeMapper;
import ua.lviv.bas.cinema.repository.ticket.TicketRepository;
import ua.lviv.bas.cinema.repository.ticket.TicketTypeRepository;
import ua.lviv.bas.cinema.repository.ticket.projection.TicketTypeAdminProjection;
import ua.lviv.bas.cinema.repository.ticket.projection.TicketTypeUserProjection;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "ticket-types")
public class TicketTypeService {

	private final TicketTypeRepository ticketTypeRepository;
	private final TicketRepository ticketRepository;
	private final TicketTypeMapper ticketTypeMapper;
	private final AuditService auditService;

	@CacheEvict(allEntries = true)
	@Transactional
	public TicketTypeAdminResponse createTicketType(TicketTypeCreateRequest request) {
		log.info("Creating ticket type: {}", request.displayName());

		validateAgeRange(request.minAge(), request.maxAge());

		if (ticketTypeRepository.existsByDisplayName(request.displayName())) {
			throw new TicketTypeDuplicateException(request.displayName());
		}

		TicketType ticketType = ticketTypeMapper.toTicketType(request);
		TicketType saved = ticketTypeRepository.save(ticketType);

		log.debug("Ticket type created with ID: {}", saved.getId());

		Map<String, Object> details = new HashMap<>();
		details.put("displayName", request.displayName());
		details.put("category", request.category());
		details.put("priceMultiplier", request.priceMultiplier());

		auditService.logChange("TicketType", saved.getId(), saved.getDisplayName(), AuditAction.CREATED, null, details);

		return ticketTypeMapper.toTicketTypeResponse(saved);
	}

	@Cacheable(key = "#id")
	public TicketTypeAdminResponse getTicketTypeById(Long id) {
		log.debug("Retrieving ticket type by id: {}", id);
		return ticketTypeMapper.toTicketTypeResponse(findTicketTypeById(id));
	}

	@Cacheable(key = "'admin-' + #active + '-' + #category + '-' + #search + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public PageResponse<TicketTypeAdminResponse> getTicketTypesForAdmin(Boolean active, TicketTypeCategory category,
			String search, Pageable pageable) {
		log.info("Getting ticket types for admin with filters - active: {}, category: {}, search: {}", active, category,
				search);

		Page<TicketTypeAdminProjection> projections = ticketTypeRepository.findAdminProjections(active, category,
				search, pageable);
		Page<TicketTypeAdminResponse> responsePage = projections.map(ticketTypeMapper::toTicketTypeResponse);
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
	public TicketTypeAdminResponse updateTicketType(Long id, TicketTypeUpdateRequest request) {
		log.info("Updating ticket type with id: {}", id);

		TicketType ticketType = findTicketTypeById(id);
		String oldName = ticketType.getDisplayName();

		if (request.displayName() != null && !request.displayName().equals(ticketType.getDisplayName())
				&& ticketTypeRepository.existsByDisplayNameAndIdNot(request.displayName(), id)) {
			throw new TicketTypeDuplicateException(request.displayName());
		}

		if (request.minAge() != null || request.maxAge() != null) {
			Integer minAge = request.minAge() != null ? request.minAge() : ticketType.getMinAge();
			Integer maxAge = request.maxAge() != null ? request.maxAge() : ticketType.getMaxAge();
			validateAgeRange(minAge, maxAge);
		}

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("displayName", ticketType.getDisplayName());
		oldDetails.put("priceMultiplier", ticketType.getPriceMultiplier());
		oldDetails.put("minAge", ticketType.getMinAge());
		oldDetails.put("maxAge", ticketType.getMaxAge());
		oldDetails.put("active", ticketType.isActive());

		ticketTypeMapper.updateTicketTypeFromRequest(ticketType, request);
		TicketType updated = ticketTypeRepository.save(ticketType);

		log.debug("Ticket type updated with ID: {}", updated.getId());

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("displayName", updated.getDisplayName());
		newDetails.put("priceMultiplier", updated.getPriceMultiplier());
		newDetails.put("minAge", updated.getMinAge());
		newDetails.put("maxAge", updated.getMaxAge());
		newDetails.put("active", updated.isActive());

		auditService.logChange("TicketType", id, oldName, AuditAction.UPDATED, oldDetails, newDetails);

		return ticketTypeMapper.toTicketTypeResponse(updated);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(key = "'user-active'"), @CacheEvict(allEntries = true) })
	@Transactional
	public void deleteTicketType(Long id) {
		log.info("Deleting ticket type with id: {}", id);

		TicketType ticketType = findTicketTypeById(id);
		String ticketTypeName = ticketType.getDisplayName();

		if (hasFutureTickets(id)) {
			throw new TicketTypeInUseException(id,
					"Cannot delete ticket type. It is used in " + countFutureTickets(id) + " future ticket(s)");
		}

		ticketTypeRepository.delete(ticketType);
		log.debug("Ticket type deleted with ID: {}", id);

		Map<String, Object> details = new HashMap<>();
		details.put("deleted", ticketTypeName);

		auditService.logChange("TicketType", id, ticketTypeName, AuditAction.DELETED, details, null);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(key = "'user-active'"), @CacheEvict(allEntries = true) })
	@Transactional
	public TicketTypeAdminResponse toggleTicketTypeActiveStatus(Long id) {
		log.info("Toggling active status for ticket type with id: {}", id);

		TicketType ticketType = findTicketTypeById(id);
		boolean oldStatus = ticketType.isActive();

		if (ticketType.isActive() && hasFutureTickets(id)) {
			throw new TicketTypeInUseException(id,
					"Cannot deactivate ticket type. It is used in " + countFutureTickets(id) + " future ticket(s)");
		}

		ticketType.setActive(!ticketType.isActive());
		TicketType updated = ticketTypeRepository.save(ticketType);

		log.debug("Ticket type status toggled to: {} for ID: {}", updated.isActive(), id);

		Map<String, Object> oldDetails = new HashMap<>();
		oldDetails.put("active", oldStatus);

		Map<String, Object> newDetails = new HashMap<>();
		newDetails.put("active", updated.isActive());

		auditService.logChange("TicketType", id, ticketType.getDisplayName(), AuditAction.TOGGLE_STATUS, oldDetails,
				newDetails);

		return ticketTypeMapper.toTicketTypeResponse(updated);
	}

	private void validateAgeRange(Integer minAge, Integer maxAge) {
		if (minAge != null && maxAge != null && minAge > maxAge) {
			throw TicketTypeValidationException.invalidAgeRange(minAge, maxAge);
		}
		if (minAge != null && (minAge < 0 || minAge > 100)) {
			throw TicketTypeValidationException.invalidAgeValue("minAge", minAge);
		}
		if (maxAge != null && (maxAge < 0 || maxAge > 100)) {
			throw TicketTypeValidationException.invalidAgeValue("maxAge", maxAge);
		}
	}

	public boolean isAgeValidForTicketType(TicketType ticketType, Integer age) {
		if (age == null) {
			return ticketType.getMinAge() == null && ticketType.getMaxAge() == null;
		}
		boolean validMin = ticketType.getMinAge() == null || age >= ticketType.getMinAge();
		boolean validMax = ticketType.getMaxAge() == null || age <= ticketType.getMaxAge();
		return validMin && validMax;
	}

	public String formatAgeRange(Integer minAge, Integer maxAge) {
		if (minAge == null && maxAge == null) {
			return "No age restrictions";
		}
		if (minAge != null && maxAge != null) {
			return minAge + "-" + maxAge + " years";
		}
		if (minAge != null) {
			return "From " + minAge + " years";
		}
		return "Up to " + maxAge + " years";
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