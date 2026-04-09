package ua.lviv.bas.cinema.service.cinema;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.cinema.Person;
import ua.lviv.bas.cinema.domain.cinema.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonListResponse;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonHasMoviesException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.mapper.cinema.PersonMapper;
import ua.lviv.bas.cinema.repository.cinema.MovieRepository;
import ua.lviv.bas.cinema.repository.cinema.PersonRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonService {

	private final PersonRepository personRepository;
	private final MovieRepository movieRepository;
	private final PersonMapper personMapper;

	@CacheEvict(value = "persons", allEntries = true)
	@Transactional
	public PersonResponse createPerson(PersonRequest request) {
		log.info("Creating person: {}", request.name());
		validatePersonUniqueness(request.name(), request.role(), null);

		var person = personMapper.toPerson(request);
		var saved = personRepository.save(person);

		log.debug("Person created with ID: {}", saved.getId());
		return personMapper.toPersonResponse(saved);
	}

	@CacheEvict(value = "persons", allEntries = true)
	@Transactional
	public PersonResponse quickCreatePerson(QuickCreatePersonRequest request) {
		log.info("Quick creating person: {} with role: {}", request.name(), request.role());
		validatePersonUniqueness(request.name(), request.role(), null);

		var person = Person.builder().name(request.name()).role(request.role()).build();
		var saved = personRepository.save(person);

		log.debug("Person created with ID: {}", saved.getId());
		return personMapper.toPersonResponse(saved);
	}

	@Cacheable(value = "persons", key = "'list-' + #query + '-' + #role + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
	public Page<PersonListResponse> getPersons(String query, PersonRole role, Pageable pageable) {
		log.info("Getting persons: query='{}', role={}", query, role);
		return personRepository.findPersonsByFilters(query, role, pageable).map(personMapper::toPersonListResponse);
	}

	@CacheEvict(value = "persons", allEntries = true)
	@Transactional
	public PersonResponse updatePerson(Long id, PersonRequest request) {
		log.info("Updating person with id: {}", id);

		var person = personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));
		validatePersonUniqueness(request.name(), request.role(), id);

		personMapper.updatePersonFromRequest(request, person);
		var updated = personRepository.save(person);

		log.debug("Person updated with ID: {}", updated.getId());
		return personMapper.toPersonResponse(updated);
	}

	@CacheEvict(value = "persons", allEntries = true)
	@Transactional
	public void deletePerson(Long id) {
		log.info("Deleting person with id: {}", id);

		var person = personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));
		checkPersonUsageInMovies(person);
		personRepository.delete(person);

		log.debug("Person deleted with ID: {}", id);
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