package ua.lviv.bas.cinema.service.booking;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.domain.enums.TicketTypeCategory;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeResponse;
import ua.lviv.bas.cinema.dto.ticket.response.TicketTypeSimpleResponse;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeDuplicateException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeInUseException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeNotFoundException;
import ua.lviv.bas.cinema.exception.domain.tickettype.TicketTypeValidationException;
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

	@Transactional
	public TicketTypeResponse createTicketType(TicketTypeCreateRequest createRequest) {
		log.debug("Creating ticket type: {}", createRequest.getCode());
		validateAgeRange(createRequest.getMinAge(), createRequest.getMaxAge());
		if (ticketTypeRepository.existsByCode(createRequest.getCode())) {
			throw new TicketTypeDuplicateException(createRequest.getCode());
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

	public TicketTypeResponse getTicketTypeByCode(String code) {
		TicketType ticketType = ticketTypeRepository.findByCode(code)
				.orElseThrow(() -> new TicketTypeNotFoundException(code));
		return ticketTypeMapper.toTicketTypeResponse(ticketType);
	}

	public List<TicketTypeResponse> getAllTicketTypes(Boolean active) {
		List<TicketType> ticketTypes;
		if (active == null) {
			ticketTypes = ticketTypeRepository.findAll();
		} else if (active) {
			ticketTypes = ticketTypeRepository.findByActiveTrue();
		} else {
			ticketTypes = ticketTypeRepository.findByActiveFalse();
		}
		return ticketTypes.stream().map(ticketTypeMapper::toTicketTypeResponse).collect(Collectors.toList());
	}

	public List<TicketTypeResponse> getTicketTypesWithFilters(Boolean active, TicketTypeCategory category,
			String search) {
		List<TicketType> ticketTypes;
		if (search != null && !search.trim().isEmpty()) {
			ticketTypes = ticketTypeRepository.findByFilters(active, category, search.trim());
		} else {
			if (active == null && category == null) {
				ticketTypes = ticketTypeRepository.findAll();
			} else if (active == null) {
				ticketTypes = ticketTypeRepository.findByCategory(category);
			} else if (category == null) {
				ticketTypes = active ? ticketTypeRepository.findByActiveTrue()
						: ticketTypeRepository.findByActiveFalse();
			} else {
				ticketTypes = active ? ticketTypeRepository.findByActiveTrueAndCategory(category)
						: ticketTypeRepository.findByActiveFalseAndCategory(category);
			}
		}
		return ticketTypes.stream().map(ticketTypeMapper::toTicketTypeResponse).collect(Collectors.toList());
	}

	public List<TicketTypeSimpleResponse> getSimpleTicketTypes(Boolean active) {
		List<TicketType> ticketTypes;
		if (active == null || active) {
			ticketTypes = ticketTypeRepository.findByActiveTrue();
		} else {
			ticketTypes = ticketTypeRepository.findByActiveFalse();
		}
		return ticketTypes.stream().map(ticketTypeMapper::toTicketTypeSimpleResponse).collect(Collectors.toList());
	}

	@Transactional
	public TicketTypeResponse updateTicketType(Long id, TicketTypeUpdateRequest updateRequest) {
		log.debug("Updating ticket type: {}", id);
		TicketType ticketType = ticketTypeRepository.findById(id)
				.orElseThrow(() -> new TicketTypeNotFoundException(id));
		if (updateRequest.getMinAge() != null || updateRequest.getMaxAge() != null) {
			Integer minAge = updateRequest.getMinAge() != null ? updateRequest.getMinAge() : ticketType.getMinAge();
			Integer maxAge = updateRequest.getMaxAge() != null ? updateRequest.getMaxAge() : ticketType.getMaxAge();
			validateAgeRange(minAge, maxAge);
		}
		ticketTypeMapper.updateTicketTypeFromRequest(ticketType, updateRequest);
		TicketType updated = ticketTypeRepository.save(ticketType);
		return ticketTypeMapper.toTicketTypeResponse(updated);
	}

	@Transactional
	public void deleteTicketType(Long id) {
		TicketType ticketType = ticketTypeRepository.findById(id)
				.orElseThrow(() -> new TicketTypeNotFoundException(id));
		if (isTicketTypeInUse(id)) {
			long ticketCount = ticketRepository.countByTicketTypeId(id);
			throw new TicketTypeInUseException(id,
					"Cannot delete ticket type. It is used in " + ticketCount + " ticket(s)");
		}
		ticketTypeRepository.delete(ticketType);
		log.info("Deleted ticket type: {}", id);
	}

	@Transactional
	public TicketTypeResponse toggleTicketTypeActiveStatus(Long id) {
		TicketType ticketType = ticketTypeRepository.findById(id)
				.orElseThrow(() -> new TicketTypeNotFoundException(id));
		if (ticketType.isActive() && hasActiveTicketsWithType(id)) {
			List<TicketStatus> activeStatuses = List.of(TicketStatus.ACTIVE, TicketStatus.PENDING);
			long activeTicketCount = ticketRepository.countByTicketTypeIdAndStatusIn(id, activeStatuses);
			throw new TicketTypeInUseException(id,
					"Cannot deactivate ticket type. It is used in " + activeTicketCount + " active ticket(s)");
		}
		ticketType.setActive(!ticketType.isActive());
		TicketType updated = ticketTypeRepository.save(ticketType);
		return ticketTypeMapper.toTicketTypeResponse(updated);
	}

	public boolean validateAgeForTicketType(Long ticketTypeId, Integer age) {
		TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
				.orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));
		return isAgeValidForTicketType(ticketType, age);
	}

	public boolean isAgeValidForTicketType(TicketType ticketType, Integer age) {
		if (age == null) {
			return ticketType.getMinAge() == null && ticketType.getMaxAge() == null;
		}
		boolean validMin = ticketType.getMinAge() == null || age >= ticketType.getMinAge();
		boolean validMax = ticketType.getMaxAge() == null || age <= ticketType.getMaxAge();
		return validMin && validMax;
	}

	public boolean existsByCode(String code) {
		return ticketTypeRepository.existsByCode(code);
	}

	public String getFormattedAgeRange(Long ticketTypeId) {
		TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
				.orElseThrow(() -> new TicketTypeNotFoundException(ticketTypeId));
		return formatAgeRange(ticketType.getMinAge(), ticketType.getMaxAge());
	}

	private boolean isTicketTypeInUse(Long ticketTypeId) {
		return ticketRepository.existsByTicketTypeId(ticketTypeId);
	}

	private boolean hasActiveTicketsWithType(Long ticketTypeId) {
		List<TicketStatus> activeStatuses = List.of(TicketStatus.ACTIVE, TicketStatus.PENDING);
		return ticketRepository.existsByTicketTypeIdAndStatusIn(ticketTypeId, activeStatuses);
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

	private String formatAgeRange(Integer minAge, Integer maxAge) {
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
}