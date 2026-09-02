package ua.lviv.bas.cinema.ticket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.audit.domain.AuditAction;
import ua.lviv.bas.cinema.ticket.domain.Ticket;
import ua.lviv.bas.cinema.ticket.domain.TicketStatus;
import ua.lviv.bas.cinema.ticket.domain.TicketType;
import ua.lviv.bas.cinema.ticket.domain.TicketTypeCategory;
import ua.lviv.bas.cinema.ticket.dto.request.TicketTypeRequest;
import ua.lviv.bas.cinema.ticket.dto.response.TicketTypeResponse;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.ticket.TicketTypeValidationException;
import ua.lviv.bas.cinema.ticket.mapper.TicketTypeMapper;
import ua.lviv.bas.cinema.ticket.repository.TicketRepository;
import ua.lviv.bas.cinema.ticket.repository.TicketTypeRepository;
import ua.lviv.bas.cinema.ticket.repository.specification.TicketSpecification;
import ua.lviv.bas.cinema.common.UniquenessValidator;
import ua.lviv.bas.cinema.audit.service.AuditDetails;
import ua.lviv.bas.cinema.audit.service.AuditService;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final TicketRepository ticketRepository;
    private final TicketTypeMapper ticketTypeMapper;
    private final AuditService auditService;
    private final TicketSpecification ticketSpecification;

    @CacheEvict(value = "ticketTypes", allEntries = true)
    @Transactional
    public TicketTypeResponse createTicketType(TicketTypeRequest request) {
        log.info("Creating ticket type: {}", request.displayName());
        validateAgeRange(request.minAge(), request.maxAge());
        validateTicketTypeUniqueness(request.displayName(), null);

        var ticketType = ticketTypeMapper.toTicketType(request);
        var saved = ticketTypeRepository.save(ticketType);

        log.debug("Ticket type created with ID: {}", saved.getId());
        auditCreate(saved, request);

        return ticketTypeMapper.toTicketTypeResponse(saved);
    }

    @Cacheable(value = "ticketTypes", key = "'list-' + #active + '-' + #category + '-' + #query + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<TicketTypeResponse> getTicketTypes(Boolean active, TicketTypeCategory category, String query,
                                                   Pageable pageable) {
        log.info("Getting ticket types: active={}, category={}, query={}, page={}, size={}", active, category, query,
                pageable.getPageNumber(), pageable.getPageSize());
        var page = ticketTypeRepository.findProjectionsByFilters(active, category, query, pageable);
        return page.map(ticketTypeMapper::toTicketTypeResponse);
    }

    @CacheEvict(value = "ticketTypes", allEntries = true)
    @Transactional
    public TicketTypeResponse updateTicketType(Long id, TicketTypeRequest request) {
        log.info("Updating ticket type with id: {}", id);

        var ticketType = findTicketTypeById(id);
        String oldName = ticketType.getDisplayName();

        if (request.displayName() != null && !request.displayName().equals(ticketType.getDisplayName())) {
            validateTicketTypeUniqueness(request.displayName(), id);
        }

        if (request.minAge() != null || request.maxAge() != null) {
            Integer minAge = request.minAge() != null ? request.minAge() : ticketType.getMinAge();
            Integer maxAge = request.maxAge() != null ? request.maxAge() : ticketType.getMaxAge();
            validateAgeRange(minAge, maxAge);
        }

        var oldDetails = captureDetails(ticketType);
        ticketTypeMapper.updateTicketTypeFromRequest(request, ticketType);
        var updated = ticketTypeRepository.save(ticketType);

        log.debug("Ticket type updated with ID: {}", updated.getId());
        auditUpdate(id, oldName, oldDetails, updated);

        return ticketTypeMapper.toTicketTypeResponse(updated);
    }

    @CacheEvict(value = "ticketTypes", allEntries = true)
    @Transactional
    public void deleteTicketType(Long id) {
        log.info("Deleting ticket type with id: {}", id);

        var ticketType = findTicketTypeById(id);
        String ticketTypeName = ticketType.getDisplayName();

        if (hasFutureTickets(id)) {
            throw new TicketTypeInUseException(id,
                    "Cannot delete ticket type. It is used in " + countFutureTickets(id) + " future ticket(s)");
        }

        ticketTypeRepository.delete(ticketType);
        log.debug("Ticket type deleted with ID: {}", id);
        auditDelete(id, ticketTypeName);
    }

    @CacheEvict(value = "ticketTypes", allEntries = true)
    @Transactional
    public TicketTypeResponse toggleActiveStatus(Long id) {
        log.info("Toggling active status for ticket type with id: {}", id);

        var ticketType = findTicketTypeById(id);
        boolean oldStatus = ticketType.isActive();

        if (ticketType.isActive() && hasFutureTickets(id)) {
            throw new TicketTypeInUseException(id,
                    "Cannot deactivate ticket type. It is used in " + countFutureTickets(id) + " future ticket(s)");
        }

        ticketType.setActive(!ticketType.isActive());
        var updated = ticketTypeRepository.save(ticketType);

        log.debug("Ticket type status toggled to: {} for ID: {}", updated.isActive(), id);
        auditToggleStatus(id, ticketType.getDisplayName(), oldStatus, updated.isActive());

        return ticketTypeMapper.toTicketTypeResponse(updated);
    }

    private TicketType findTicketTypeById(Long id) {
        return ticketTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ticket type", id));
    }

    private void validateTicketTypeUniqueness(String displayName, Long excludeId) {
        UniquenessValidator.validate(excludeId, () -> ticketTypeRepository.existsByDisplayName(displayName),
                id -> ticketTypeRepository.existsByDisplayNameAndIdNot(displayName, id),
                () -> new TicketTypeDuplicateException(displayName));
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

    private boolean hasFutureTickets(Long ticketTypeId) {
        return countFutureTickets(ticketTypeId) > 0;
    }

    private long countFutureTickets(Long ticketTypeId) {
        Specification<Ticket> spec = Specification
                .where(ticketSpecification.hasStatus(TicketStatus.ACTIVE))
                .and(ticketSpecification.hasTicketTypeId(ticketTypeId))
                .and(ticketSpecification.sessionStartTimeAfter(LocalDateTime.now()));

        return ticketRepository.count(spec);
    }

    private Map<String, Object> captureDetails(TicketType ticketType) {
        return AuditDetails.of().put("displayName", ticketType.getDisplayName())
                .put("priceMultiplier", ticketType.getPriceMultiplier()).put("minAge", ticketType.getMinAge())
                .put("maxAge", ticketType.getMaxAge()).put("active", ticketType.isActive()).build();
    }

    private void auditCreate(TicketType ticketType, TicketTypeRequest request) {
        var details = AuditDetails.of().put("displayName", request.displayName())
                .put("category", request.category()).put("priceMultiplier", request.priceMultiplier()).build();
        auditService.logChange("TicketType", ticketType.getId(), ticketType.getDisplayName(), AuditAction.CREATED, null,
                details);
    }

    private void auditUpdate(Long id, String oldName, Map<String, Object> oldDetails, TicketType updated) {
        Map<String, Object> newDetails = captureDetails(updated);
        auditService.logChange("TicketType", id, oldName, AuditAction.UPDATED, oldDetails, newDetails);
    }

    private void auditDelete(Long id, String name) {
        var details = AuditDetails.of().put("deleted", name).build();
        auditService.logChange("TicketType", id, name, AuditAction.DELETED, details, null);
    }

    private void auditToggleStatus(Long id, String name, boolean oldStatus, boolean newStatus) {
        var oldDetails = AuditDetails.of().put("active", oldStatus).build();
        var newDetails = AuditDetails.of().put("active", newStatus).build();
        auditService.logChange("TicketType", id, name, AuditAction.TOGGLE_STATUS, oldDetails, newDetails);
    }
}