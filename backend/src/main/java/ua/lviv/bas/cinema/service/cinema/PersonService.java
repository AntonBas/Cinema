package ua.lviv.bas.cinema.service.cinema;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
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
public class PersonService {

	private final PersonRepository personRepository;
	private final PersonMapper personMapper;
	private final MovieRepository movieRepository;

	@Transactional
	public PersonResponse createPerson(PersonRequest request) {
		log.info("Creating person: {}", request.getName());

		String personName = request.getName().trim();
		validatePersonUniqueness(personName, request.getRole(), null);

		Person person = personMapper.toEntity(request);
		person.setName(personName);
		Person saved = personRepository.save(person);

		log.debug("Person created with ID: {}", saved.getId());
		return personMapper.toDto(saved);
	}

	@Transactional
	public PersonResponse quickCreatePerson(QuickCreatePersonRequest request) {
		log.info("Quick creating person: {} with role: {}", request.getName(), request.getRole());

		String personName = request.getName().trim();
		validatePersonUniqueness(personName, request.getRole(), null);

		Person person = Person.builder().name(personName).role(request.getRole()).build();

		Person saved = personRepository.save(person);

		log.debug("Person quick-created with ID: {}", saved.getId());
		return personMapper.toDto(saved);
	}

	public PersonResponse getPersonById(Long id) {
		log.debug("Retrieving person by id: {}", id);

		Person person = personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));

		return personMapper.toDto(person);
	}

	@Transactional
	public PersonResponse updatePerson(Long id, PersonRequest request) {
		log.info("Updating person with id: {}", id);

		Person existing = personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));

		String personName = request.getName().trim();
		validatePersonUniqueness(personName, request.getRole(), id);

		personMapper.updatePersonFromRequest(request, existing);
		existing.setName(personName);
		Person updated = personRepository.save(existing);

		log.debug("Person updated with ID: {}", updated.getId());
		return personMapper.toDto(updated);
	}

	@Transactional
	public void deletePerson(Long id) {
		log.info("Deleting person with id: {}", id);

		Person person = personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));

		checkPersonUsageInMovies(person);
		personRepository.delete(person);

		log.debug("Person deleted with ID: {}", id);
	}

	public Page<PersonResponse> getPersonsByRole(PersonRole role, Pageable pageable) {
		log.info("Getting persons by role: {}", role);

		Page<Person> persons = personRepository.findByRole(role, pageable);
		return persons.map(personMapper::toDto);
	}

	public Page<PersonResponse> searchPersons(String query, PersonRole role, Pageable pageable) {
		log.info("Searching persons: query='{}', role={}", query, role);

		Page<Person> personPage = personRepository.searchByNameAndRole(StringUtils.hasText(query) ? query.trim() : null,
				role, pageable);

		log.debug("Found {} persons", personPage.getTotalElements());
		return personPage.map(personMapper::toDto);
	}

	public List<PersonResponse> getPersonsByIds(List<Long> ids) {
		log.debug("Retrieving persons by ids: {}", ids);

		if (ids == null || ids.isEmpty()) {
			return List.of();
		}

		List<Person> persons = personRepository.findAllById(ids);
		return personMapper.toDtoList(persons);
	}

	public List<PersonResponse> getPersons() {
		log.debug("Retrieving all persons");
		return personMapper.toDtoList(personRepository.findAll());
	}

	public boolean existsById(Long id) {
		return personRepository.existsById(id);
	}

	public boolean existsByNameAndRole(String name, PersonRole role) {
		return personRepository.existsByNameAndRole(name.trim(), role);
	}

	public long countByRole(PersonRole role) {
		return personRepository.countByRole(role);
	}

	private void validatePersonUniqueness(String name, PersonRole role, Long excludeId) {
		boolean exists;

		if (excludeId != null) {
			exists = personRepository.existsByNameAndRoleAndIdNot(name, role, excludeId);
		} else {
			exists = personRepository.existsByNameAndRole(name, role);
		}

		if (exists) {
			throw new DuplicateEntityException("Person", name + " (" + role + ")");
		}
	}

	private void checkPersonUsageInMovies(Person person) {
		long actorCount = movieRepository.countByActorsId(person.getId());
		long directorCount = movieRepository.countByDirectorsId(person.getId());
		long screenwriterCount = movieRepository.countByScreenwritersId(person.getId());

		long totalUsage = actorCount + directorCount + screenwriterCount;

		if (totalUsage > 0) {
			throw new PersonHasMoviesException(person.getId(), person.getName(), actorCount, directorCount,
					screenwriterCount);
		}
	}
}