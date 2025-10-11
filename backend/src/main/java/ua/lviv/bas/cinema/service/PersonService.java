package ua.lviv.bas.cinema.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.dao.PersonRepository;
import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.PageResponse;
import ua.lviv.bas.cinema.dto.PersonDto;
import ua.lviv.bas.cinema.dto.QuickCreatePersonDto;
import ua.lviv.bas.cinema.exception.DuplicateEntityException;
import ua.lviv.bas.cinema.mapper.PersonMapper;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersonService {

	private final PersonRepository personRepository;
	private final PersonMapper personMapper;

	public List<PersonDto> getAllPersons() {
		log.debug("Getting all persons");
		return personMapper.toDtoList(personRepository.findAll());
	}

	public Optional<PersonDto> getPersonById(Long id) {
		log.debug("Getting person by id: {}", id);
		return personRepository.findById(id).map(personMapper::toDto);
	}

	public PersonDto savePerson(Person person) {
		log.info("Saving person: {}", person.getName());
		Person savedPerson = personRepository.save(person);
		return personMapper.toDto(savedPerson);
	}

	public void deletePerson(Long id) {
		log.info("Deleting person with id: {}", id);
		personRepository.deleteById(id);
	}

	public Optional<Person> findEntityById(Long id) {
		log.debug("Finding person entity by id: {}", id);
		return personRepository.findById(id);
	}

	@Transactional
	public PersonDto quickCreate(QuickCreatePersonDto dto) {
		log.info("Quick creating person: {} with role: {}", dto.getName(), dto.getRole());

		if (personRepository.existsByNameAndRole(dto.getName(), dto.getRole())) {
			throw new DuplicateEntityException(
					"Person with name '" + dto.getName() + "' and role '" + dto.getRole() + "' already exists");
		}

		Person person = personMapper.toEntity(dto);
		Person savedPerson = personRepository.save(person);
		return personMapper.toDto(savedPerson);
	}

	@Transactional(readOnly = true)
	public PageResponse<PersonDto> searchPersons(String query, PersonRole role, int page, int size) {
		log.info("Searching persons: query='{}', role={}, page={}, size={}", query, role, page, size);

		Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());

		Page<Person> personPage = personRepository.searchPersons(query, role, pageable);

		log.debug("Search found {} results for query: '{}'", personPage.getTotalElements(), query);

		return toPageResponse(personPage);
	}

	private PageResponse<PersonDto> toPageResponse(Page<Person> personPage) {
		List<PersonDto> content = personPage.getContent().stream().map(personMapper::toDto).toList();

		return new PageResponse<>(content, personPage.getNumber(), personPage.getTotalPages(),
				personPage.getTotalElements(), personPage.getSize());
	}
}