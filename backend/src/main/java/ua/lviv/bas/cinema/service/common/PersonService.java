package ua.lviv.bas.cinema.service.common;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.QMovie;
import ua.lviv.bas.cinema.domain.QPerson;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonHasMoviesException;
import ua.lviv.bas.cinema.exception.domain.cinema.PersonNotFoundException;
import ua.lviv.bas.cinema.mapper.PersonMapper;
import ua.lviv.bas.cinema.repository.MovieRepository;
import ua.lviv.bas.cinema.repository.PersonRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

	private final PersonRepository personRepository;
	private final PersonMapper personMapper;
	private final MovieRepository movieRepository;

	@Transactional
	public PersonResponse createPerson(PersonRequest request) {
		log.info("Creating person: {}", request.getName());

		validatePersonUniqueness(request.getName(), request.getRole(), null);

		Person person = personMapper.toEntity(request);
		Person saved = personRepository.save(person);

		log.debug("Person created with ID: {}", saved.getId());
		return personMapper.toDto(saved);
	}

	@Transactional
	public PersonResponse quickCreatePerson(QuickCreatePersonRequest request) {
		log.info("Quick creating person: {} with role: {}", request.getName(), request.getRole());

		validatePersonUniqueness(request.getName(), request.getRole(), null);

		Person person = Person.builder().name(request.getName()).role(request.getRole()).build();

		Person saved = personRepository.save(person);

		log.debug("Person quick-created with ID: {}", saved.getId());
		return personMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public PersonResponse getPersonById(Long id) {
		log.debug("Retrieving person by id: {}", id);

		Person person = personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));

		return personMapper.toDto(person);
	}

	@Transactional
	public PersonResponse updatePerson(Long id, PersonRequest request) {
		log.info("Updating person with id: {}", id);

		Person existing = personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));

		validatePersonUniqueness(request.getName(), request.getRole(), id);

		personMapper.updatePersonFromRequest(request, existing);
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

	@Transactional(readOnly = true)
	public PageResponse<PersonResponse> getPersonsByRole(PersonRole role, Pageable pageable) {
		log.info("Getting persons by role: {}", role);

		Page<Person> persons = personRepository.findByRole(role, pageable);
		return PageResponse.of(persons, personMapper::toDto);
	}

	@Transactional(readOnly = true)
	public PageResponse<PersonResponse> searchPersons(String query, PersonRole role, Pageable pageable) {
		log.info("Searching persons: query='{}', role={}", query, role);

		QPerson qPerson = QPerson.person;
		BooleanBuilder predicate = new BooleanBuilder();

		if (query != null && !query.trim().isEmpty()) {
			String searchQuery = query.trim().toLowerCase();
			predicate.and(qPerson.name.toLowerCase().contains(searchQuery));
		}

		if (role != null) {
			predicate.and(qPerson.role.eq(role));
		}

		Page<Person> personPage = personRepository.findAll(predicate, pageable);

		log.debug("Found {} persons", personPage.getTotalElements());

		return PageResponse.of(personPage, personMapper::toDto);
	}

	@Transactional(readOnly = true)
	public List<PersonResponse> getPersonsByIds(List<Long> ids) {
		List<Person> persons = personRepository.findAllById(ids);
		return personMapper.toDtoList(persons);
	}

	@Transactional(readOnly = true)
	public boolean existsById(Long id) {
		return personRepository.existsById(id);
	}

	@Transactional(readOnly = true)
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
		QMovie qMovie = QMovie.movie;
		BooleanBuilder predicate = new BooleanBuilder();
		predicate.or(qMovie.actors.any().id.eq(person.getId())).or(qMovie.directors.any().id.eq(person.getId()))
				.or(qMovie.screenwriters.any().id.eq(person.getId()));

		if (movieRepository.exists(predicate)) {
			throw new PersonHasMoviesException(person.getId(), person.getName());
		}
	}
}