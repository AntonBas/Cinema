package ua.lviv.bas.cinema.movie.person;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.audit.AuditAction;
import ua.lviv.bas.cinema.audit.AuditDetails;
import ua.lviv.bas.cinema.audit.AuditService;
import ua.lviv.bas.cinema.movie.person.Person;
import ua.lviv.bas.cinema.movie.person.PersonRole;
import ua.lviv.bas.cinema.movie.person.dto.PersonRequest;
import ua.lviv.bas.cinema.movie.person.dto.PersonListResponse;
import ua.lviv.bas.cinema.movie.person.dto.PersonResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonHasMoviesException;
import ua.lviv.bas.cinema.movie.person.PersonMapper;
import ua.lviv.bas.cinema.movie.movie.MovieRepository;
import ua.lviv.bas.cinema.movie.person.PersonRepository;
import ua.lviv.bas.cinema.common.UniquenessValidator;
import ua.lviv.bas.cinema.common.FixedOrderPageable;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonService {

    private final PersonRepository personRepository;
    private final MovieRepository movieRepository;
    private final PersonMapper personMapper;
    private final AuditService auditService;

    @CacheEvict(value = "persons", allEntries = true)
    @Transactional
    public PersonResponse createPerson(PersonRequest request) {
        log.info("Creating person: {}", request.name());
        validatePersonUniqueness(request.name(), request.role(), null);

        var person = personMapper.toEntity(request);
        var saved = personRepository.save(person);

        auditService.logChange("Person", saved.getId(), saved.getName(), AuditAction.CREATED, null,
                AuditDetails.of().put("name", saved.getName()).put("role", saved.getRole()).build());
        log.debug("Person created with ID: {}", saved.getId());
        return personMapper.toPersonResponse(saved);
    }

    @Cacheable(value = "persons", key = "{ 'list', #query, #role?.name(), #pageable.pageNumber, #pageable.pageSize, #pageable.sort.toString() }")
    public Page<PersonListResponse> getPersons(String query, PersonRole role, Pageable pageable) {
        log.info("Getting persons: query='{}', role={}, page={}, size={}", query, role, pageable.getPageNumber(),
                pageable.getPageSize());
        String roleStr = role != null ? role.name() : null;
        return personRepository.findPersonsByFilters(query, roleStr, FixedOrderPageable.of(pageable))
                .map(personMapper::toPersonListResponse);
    }

    @CacheEvict(value = "persons", allEntries = true)
    @Transactional
    public PersonResponse updatePerson(Long id, PersonRequest request) {
        log.info("Updating person with id: {}", id);

        var person = personRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Person", id));
        validatePersonUniqueness(request.name(), request.role(), id);
        var oldName = person.getName();
        var oldRole = person.getRole();

        personMapper.updateEntity(request, person);
        var updated = personRepository.save(person);
        auditService.logChange("Person", id, oldName, AuditAction.UPDATED,
                AuditDetails.of().put("name", oldName).put("role", oldRole).build(),
                AuditDetails.of().put("name", updated.getName()).put("role", updated.getRole()).build());

        log.debug("Person updated with ID: {}", updated.getId());
        return personMapper.toPersonResponse(updated);
    }

    @CacheEvict(value = "persons", allEntries = true)
    @Transactional
    public void deletePerson(Long id) {
        log.info("Deleting person with id: {}", id);

        var person = personRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Person", id));
        checkPersonUsageInMovies(person);
        personRepository.delete(person);
        auditService.logChange("Person", id, person.getName(), AuditAction.DELETED,
                AuditDetails.of().put("deleted", person.getName()).build(), null);

        log.debug("Person deleted with ID: {}", id);
    }

    private void validatePersonUniqueness(String name, PersonRole role, Long excludeId) {
        UniquenessValidator.validate(excludeId, () -> personRepository.existsByNameAndRole(name, role),
                id -> personRepository.existsByNameAndRoleAndIdNot(name, role, id),
                () -> new DuplicateEntityException(String.format("Person \"%s\" with role %s already exists", name, role.name().toLowerCase())));
    }

    private void checkPersonUsageInMovies(Person person) {
        long usageCount = movieRepository.countMovieUsageByPersonId(person.getId());
        if (usageCount > 0) {
            throw new PersonHasMoviesException(person.getName(), usageCount);
        }
    }
}