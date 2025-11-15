package ua.lviv.bas.cinema.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.lviv.bas.cinema.domain.Movie;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.request.PersonRequest;
import ua.lviv.bas.cinema.dto.movie.request.QuickCreatePersonRequest;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.exception.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.PersonHasMoviesException;
import ua.lviv.bas.cinema.exception.PersonNotFoundException;
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
			throw new DuplicateEntityException(
					"Person with name '" + request.getName() + "' and role '" + request.getRole() + "' already exists");
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
				.orElseThrow(() -> new PersonNotFoundException("Person not found with id: " + id));
	}

	@Transactional
	public PersonResponse updatePerson(Long id, PersonRequest request) {
		log.info("Updating person with id: {}", id);
		Person existing = personRepository.findById(id)
				.orElseThrow(() -> new PersonNotFoundException("Person not found with id: " + id));
		personMapper.updatePersonFromRequest(request, existing);
		Person updated = personRepository.save(existing);
		log.debug("Person updated with ID: {}", updated.getId());
		return personMapper.toDto(updated);
	}

	@Transactional
	public void deletePerson(Long id) {
		log.info("Deleting person with id: {}", id);

		Person person = personRepository.findById(id)
				.orElseThrow(() -> new PersonNotFoundException("Person not found with id: " + id));

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
			throw new DuplicateEntityException(
					"Person with name '" + dto.getName() + "' and role '" + dto.getRole() + "' already exists");
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
			throw new PersonHasMoviesException(String.format("Cannot delete person '%s' because they are used as %s",
					person.getName(), String.join("; ", usedInMovies)));
		}
	}

	@Transactional(readOnly = true)
	public PageResponse<PersonResponse> searchPersons(String query, PersonRole role, int page, int size) {
		log.info("Searching persons: query='{}', role={}, page={}, size={}", query, role, page, size);
		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
		Page<Person> personPage = personRepository.searchPersons(query, role, pageable);
		log.debug("Found {} persons for query '{}'", personPage.getTotalElements(), query);
		return toPageResponse(personPage);
	}

	private PageResponse<PersonResponse> toPageResponse(Page<Person> personPage) {
		List<PersonResponse> content = personPage.getContent().stream().map(personMapper::toDto).toList();
		return new PageResponse<>(content, personPage.getNumber(), personPage.getTotalPages(),
				personPage.getTotalElements(), personPage.getSize());
	}
}