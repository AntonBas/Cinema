package ua.lviv.bas.cinema.service.booking;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.TicketType;
import ua.lviv.bas.cinema.domain.enums.TicketStatus;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeCreateRequest;
import ua.lviv.bas.cinema.dto.ticket.request.TicketTypeUpdateRequest;
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
	public TicketType createTicketType(TicketTypeCreateRequest createRequest) {
		log.debug("Creating ticket type: {}", createRequest.getCode());

		validateAgeRange(createRequest.getMinAge(), createRequest.getMaxAge());

		if (ticketTypeRepository.existsByCode(createRequest.getCode())) {
			throw new TicketTypeDuplicateException(createRequest.getCode());
		}

		TicketType ticketType = ticketTypeMapper.toEntity(createRequest);
		return ticketTypeRepository.save(ticketType);
	}

	public TicketType getTicketTypeById(Long id) {
		return ticketTypeRepository.findById(id).orElseThrow(() -> new TicketTypeNotFoundException(id));
	}

	public TicketType getTicketTypeByCode(String code) {
		return ticketTypeRepository.findByCode(code).orElseThrow(() -> new TicketTypeNotFoundException(code));
	}

	public List<TicketType> getAllTicketTypes() {
		return ticketTypeRepository.findAll();
	}

	public List<TicketType> getAllActiveTicketTypes() {
		return ticketTypeRepository.findByActiveTrue();
	}

	@Transactional
	public TicketType updateTicketType(Long id, TicketTypeUpdateRequest updateRequest) {
		log.debug("Updating ticket type: {}", id);

		TicketType ticketType = getTicketTypeById(id);

		if (updateRequest.getMinAge() != null || updateRequest.getMaxAge() != null) {
			Integer minAge = updateRequest.getMinAge() != null ? updateRequest.getMinAge() : ticketType.getMinAge();
			Integer maxAge = updateRequest.getMaxAge() != null ? updateRequest.getMaxAge() : ticketType.getMaxAge();
			validateAgeRange(minAge, maxAge);
		}

		ticketTypeMapper.updateEntity(ticketType, updateRequest);
		return ticketTypeRepository.save(ticketType);
	}

	@Transactional
	public void deleteTicketType(Long id) {
		TicketType ticketType = getTicketTypeById(id);

		if (isTicketTypeInUse(id)) {
			long ticketCount = ticketRepository.countByTicketTypeId(id);
			throw new TicketTypeInUseException(id,
					"Cannot delete ticket type. It is used in " + ticketCount + " ticket(s)");
		}

		ticketTypeRepository.delete(ticketType);
		log.info("Deleted ticket type: {}", id);
	}

	@Transactional
	public TicketType toggleTicketTypeActiveStatus(Long id) {
		TicketType ticketType = getTicketTypeById(id);

		if (ticketType.isActive() && hasActiveTicketsWithType(id)) {
			List<TicketStatus> activeStatuses = List.of(TicketStatus.ACTIVE, TicketStatus.RESERVED);
			long activeTicketCount = ticketRepository.countByTicketTypeIdAndStatusIn(id, activeStatuses);
			throw new TicketTypeInUseException(id,
					"Cannot deactivate ticket type. It is used in " + activeTicketCount + " active ticket(s)");
		}

		ticketType.setActive(!ticketType.isActive());
		return ticketTypeRepository.save(ticketType);
	}

	public boolean validateAgeForTicketType(Long ticketTypeId, Integer age) {
		TicketType ticketType = getTicketTypeById(ticketTypeId);
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
		TicketType ticketType = getTicketTypeById(ticketTypeId);
		return formatAgeRange(ticketType.getMinAge(), ticketType.getMaxAge());
	}

	private boolean isTicketTypeInUse(Long ticketTypeId) {
		return ticketRepository.existsByTicketTypeId(ticketTypeId);
	}

	private boolean hasActiveTicketsWithType(Long ticketTypeId) {
		List<TicketStatus> activeStatuses = List.of(TicketStatus.ACTIVE, TicketStatus.RESERVED);
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