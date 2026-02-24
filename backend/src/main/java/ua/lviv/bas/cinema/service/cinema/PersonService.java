package ua.lviv.bas.cinema.service.cinema;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.domain.projection.PersonProjection;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonHasMoviesException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.mapper.PersonMapper;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@CacheConfig(cacheNames = "persons")
public class PersonService {

	private final PersonRepository personRepository;
	private final MovieRepository movieRepository;
	private final PersonMapper personMapper;

	@CacheEvict(allEntries = true)
	@Transactional
	public PersonResponse createPerson(PersonRequest request) {
		log.info("Creating person: {}", request.getName());

		String personName = request.getName().trim();
		validatePersonUniqueness(personName, request.getRole(), null);

		Person person = personMapper.toPerson(request);
		person.setName(personName);
		Person saved = personRepository.save(person);

		log.debug("Person created with ID: {}", saved.getId());
		return personMapper.toPersonResponse(saved);
	}

	@CacheEvict(allEntries = true)
	@Transactional
	public PersonResponse quickCreatePerson(QuickCreatePersonRequest request) {
		log.info("Quick creating person: {} with role: {}", request.getName(), request.getRole());

		String personName = request.getName().trim();
		validatePersonUniqueness(personName, request.getRole(), null);

		Person person = Person.builder().name(personName).role(request.getRole()).build();

		Person saved = personRepository.save(person);
		log.debug("Person quick-created with ID: {}", saved.getId());
		return personMapper.toPersonResponse(saved);
	}

	@Cacheable(key = "#id")
	public PersonResponse getPersonById(Long id) {
		log.debug("Retrieving person by id: {}", id);

		Person person = findPersonById(id);
		return personMapper.toPersonResponse(person);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(allEntries = true) })
	@Transactional
	public PersonResponse updatePerson(Long id, PersonRequest request) {
		log.info("Updating person with id: {}", id);

		Person existing = findPersonById(id);

		String personName = request.getName().trim();
		validatePersonUniqueness(personName, request.getRole(), id);

		personMapper.updatePersonFromRequest(request, existing);
		existing.setName(personName);
		Person updated = personRepository.save(existing);

		log.debug("Person updated with ID: {}", updated.getId());
		return personMapper.toPersonResponse(updated);
	}

	@Caching(evict = { @CacheEvict(key = "#id"), @CacheEvict(allEntries = true) })
	@Transactional
	public void deletePerson(Long id) {
		log.info("Deleting person with id: {}", id);

		Person person = findPersonById(id);
		checkPersonUsageInMovies(person);
		personRepository.delete(person);

		log.debug("Person deleted with ID: {}", id);
	}

	@Cacheable(key = "'search-' + #name + '-' + #role + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<PersonResponse> searchPersons(String name, PersonRole role, Pageable pageable) {
		log.info("Searching persons: name='{}', role={}", name, role);

		Page<PersonProjection> projections = personRepository.findProjectionsByFilters(name, role, pageable);

		return projections.map(personMapper::toPersonResponse);
	}

	@Cacheable(key = "'popular-' + #name + '-' + #role + '-' + #limit")
	public List<PersonResponse> getPopularPersons(String name, PersonRole role, int limit) {
		log.info("Getting popular persons: name='{}', role={}, limit={}", name, role, limit);

		Page<PersonProjection> page = personRepository.findProjectionsByFilters(name, role, PageRequest.of(0, limit));

		return page.getContent().stream().map(personMapper::toPersonResponse).collect(Collectors.toList());
	}

	@Cacheable(key = "'by-ids-' + #ids.hashCode()")
	public List<PersonResponse> getPersonsByIds(List<Long> ids) {
		log.debug("Retrieving persons by ids: {}", ids);

		if (ids == null || ids.isEmpty()) {
			return List.of();
		}

		List<Person> persons = personRepository.findAllById(ids);
		return personMapper.toPersonResponseList(persons);
	}

	public boolean existsByNameAndRole(String name, PersonRole role) {
		return personRepository.existsByNameAndRole(name.trim(), role);
	}

	private Person findPersonById(Long id) {
		return personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));
	}

	private void validatePersonUniqueness(String name, PersonRole role, Long excludeId) {
		boolean exists = excludeId != null ? personRepository.existsByNameAndRoleAndIdNot(name, role, excludeId)
				: personRepository.existsByNameAndRole(name, role);

		if (exists) {
			throw new DuplicateEntityException("Person", name + " (" + role + ")");
		}
	}

	private void checkPersonUsageInMovies(Person person) {
		long usageCount = movieRepository.countMovieUsageByPersonId(person.getId());

		if (usageCount > 0) {
			throw new PersonHasMoviesException(person.getId(), person.getName(), usageCount);
		}
	}
}