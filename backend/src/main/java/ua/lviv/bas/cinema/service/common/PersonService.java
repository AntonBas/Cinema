package ua.lviv.bas.cinema.service.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.BooleanBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
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
		if (personRepository.existsByNameAndRole(request.getName(), request.getRole())) {
			throw new DuplicateEntityException("Person", request.getName() + " (" + request.getRole() + ")");
		}
		Person person = personMapper.toEntity(request);
		Person saved = personRepository.save(person);
		log.debug("Person created with ID: {}", saved.getId());
		return personMapper.toDto(saved);
	}

	@Transactional(readOnly = true)
	public PersonResponse getPersonById(Long id) {
		log.debug("Retrieving person by id: {}", id);
		return personRepository.findById(id).map(personMapper::toDto)
				.orElseThrow(() -> new PersonNotFoundException(id));
	}

	@Transactional
	public PersonResponse updatePerson(Long id, PersonRequest request) {
		log.info("Updating person with id: {}", id);
		Person existing = personRepository.findById(id).orElseThrow(() -> new PersonNotFoundException(id));
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
	public List<PersonResponse> getAllPersons() {
		log.debug("Retrieving all persons");
		return personMapper.toDtoList(personRepository.findAll());
	}

	@Transactional
	public PersonResponse quickCreate(QuickCreatePersonRequest dto) {
		log.info("Quick creating person: {} with role: {}", dto.getName(), dto.getRole());
		if (personRepository.existsByNameAndRole(dto.getName(), dto.getRole())) {
			throw new DuplicateEntityException("Person", dto.getName() + " (" + dto.getRole() + ")");
		}
		Person person = personMapper.toEntity(dto);
		Person saved = personRepository.save(person);
		log.debug("Person quick-created with ID: {}", saved.getId());
		return personMapper.toDto(saved);
	}

	private void checkPersonUsageInMovies(Person person) {
		List<String> usedInMovies = new ArrayList<>();

		List<Movie> actorMovies = movieRepository.findByActorsId(person.getId());
		if (!actorMovies.isEmpty()) {
			usedInMovies
					.add("actor in: " + actorMovies.stream().map(Movie::getTitle).collect(Collectors.joining(", ")));
		}

		List<Movie> directorMovies = movieRepository.findByDirectorsId(person.getId());
		if (!directorMovies.isEmpty()) {
			usedInMovies.add(
					"director in: " + directorMovies.stream().map(Movie::getTitle).collect(Collectors.joining(", ")));
		}

		List<Movie> screenwriterMovies = movieRepository.findByScreenwritersId(person.getId());
		if (!screenwriterMovies.isEmpty()) {
			usedInMovies.add("screenwriter in: "
					+ screenwriterMovies.stream().map(Movie::getTitle).collect(Collectors.joining(", ")));
		}

		if (!usedInMovies.isEmpty()) {
			throw new PersonHasMoviesException(person.getId());
		}
	}

	@Transactional(readOnly = true)
	public PageResponse<PersonResponse> searchPersons(String query, PersonRole role, int page, int size) {
		log.info("Searching persons: query='{}', role={}, page={}, size={}", query, role, page, size);

		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

		BooleanBuilder predicate = new BooleanBuilder();
		QPerson person = QPerson.person;

		if (query != null && !query.trim().isEmpty()) {
			String searchQuery = query.trim().toLowerCase();
			predicate.and(person.name.toLowerCase().contains(searchQuery));
		}

		if (role != null) {
			predicate.and(person.role.eq(role));
		}

		Page<Person> personPage = personRepository.findAll(predicate, pageable);

		log.debug("Found {} persons for query '{}'", personPage.getTotalElements(), query);

		return PageResponse.of(personPage, this::toPersonResponse);
	}

	private PersonResponse toPersonResponse(Person person) {
		return PersonResponse.builder().id(person.getId()).name(person.getName()).role(person.getRole()).build();
	}
}