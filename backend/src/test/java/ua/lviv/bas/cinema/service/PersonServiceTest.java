package ua.lviv.bas.cinema.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.movie.PersonDto;
import ua.lviv.bas.cinema.dto.movie.PersonRequest;
import ua.lviv.bas.cinema.dto.shared.PageResponse;
import ua.lviv.bas.cinema.dto.shared.QuickCreatePersonDto;
import ua.lviv.bas.cinema.exception.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.PersonNotFoundException;
import ua.lviv.bas.cinema.mapper.PersonMapper;
import ua.lviv.bas.cinema.repository.PersonRepository;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

	@Mock
	private PersonRepository personRepository;

	@Mock
	private PersonMapper personMapper;

	@InjectMocks
	private PersonService personService;

	@Test
	void createPerson_ShouldReturnSavedDto() {
		PersonRequest request = new PersonRequest("Anton Bas", PersonRole.ACTOR);
		Person person = Person.builder().name("Anton Bas").role(PersonRole.ACTOR).build();
		Person savedPerson = Person.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();
		PersonDto dto = PersonDto.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(false);
		when(personMapper.toEntity(request)).thenReturn(person);
		when(personRepository.save(person)).thenReturn(savedPerson);
		when(personMapper.toDto(savedPerson)).thenReturn(dto);

		PersonDto result = personService.createPerson(request);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Anton Bas");
		assertThat(result.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void createPerson_WhenDuplicate_ShouldThrowException() {
		PersonRequest request = new PersonRequest("Anton Bas", PersonRole.ACTOR);
		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(true);

		assertThatThrownBy(() -> personService.createPerson(request)).isInstanceOf(DuplicateEntityException.class)
				.hasMessageContaining("already exists");
	}

	@Test
	void getPersonById_WhenExists_ShouldReturnDto() {
		Person person = Person.builder().id(1L).name("Anton Bas").build();
		PersonDto dto = PersonDto.builder().id(1L).name("Anton Bas").build();

		when(personRepository.findById(1L)).thenReturn(Optional.of(person));
		when(personMapper.toDto(person)).thenReturn(dto);

		PersonDto result = personService.getPersonById(1L);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Anton Bas");
	}

	@Test
	void getPersonById_WhenNotFound_ShouldThrowException() {
		when(personRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> personService.getPersonById(1L)).isInstanceOf(PersonNotFoundException.class)
				.hasMessageContaining("Person not found");
	}

	@Test
	void updatePerson_ShouldMapAndSave() {
		PersonRequest request = new PersonRequest("Updated Name", PersonRole.DIRECTOR);
		Person existing = Person.builder().id(1L).name("Old Name").role(PersonRole.ACTOR).build();
		Person updated = Person.builder().id(1L).name("Updated Name").role(PersonRole.DIRECTOR).build();
		PersonDto dto = PersonDto.builder().id(1L).name("Updated Name").role(PersonRole.DIRECTOR).build();

		when(personRepository.findById(1L)).thenReturn(Optional.of(existing));
		doAnswer(invocation -> {
			PersonRequest req = invocation.getArgument(0);
			Person target = invocation.getArgument(1);
			target.setName(req.getName());
			target.setRole(req.getRole());
			return null;
		}).when(personMapper).updatePersonFromRequest(request, existing);
		when(personRepository.save(existing)).thenReturn(updated);
		when(personMapper.toDto(updated)).thenReturn(dto);

		PersonDto result = personService.updatePerson(1L, request);

		assertThat(result.getName()).isEqualTo("Updated Name");
		assertThat(result.getRole()).isEqualTo(PersonRole.DIRECTOR);
		verify(personMapper).updatePersonFromRequest(request, existing);
	}

	@Test
	void updatePerson_WhenNotFound_ShouldThrowException() {
		PersonRequest request = new PersonRequest("Test", PersonRole.ACTOR);
		when(personRepository.findById(1L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> personService.updatePerson(1L, request)).isInstanceOf(PersonNotFoundException.class);
	}

	@Test
	void deletePerson_WhenExists_ShouldDelete() {
		when(personRepository.existsById(1L)).thenReturn(true);

		personService.deletePerson(1L);

		verify(personRepository).deleteById(1L);
	}

	@Test
	void deletePerson_WhenNotExists_ShouldThrowException() {
		when(personRepository.existsById(1L)).thenReturn(false);

		assertThatThrownBy(() -> personService.deletePerson(1L)).isInstanceOf(PersonNotFoundException.class);
	}

	@Test
	void quickCreate_ShouldReturnSavedDto() {
		QuickCreatePersonDto dto = new QuickCreatePersonDto("Anton Bas", PersonRole.ACTOR);
		Person person = Person.builder().name("Anton Bas").role(PersonRole.ACTOR).build();
		Person saved = Person.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();
		PersonDto mappedDto = PersonDto.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(false);
		when(personMapper.toEntity(dto)).thenReturn(person);
		when(personRepository.save(person)).thenReturn(saved);
		when(personMapper.toDto(saved)).thenReturn(mappedDto);

		PersonDto result = personService.quickCreate(dto);

		assertThat(result.getId()).isEqualTo(1L);
		assertThat(result.getName()).isEqualTo("Anton Bas");
	}

	@Test
	void quickCreate_WhenDuplicate_ShouldThrowException() {
		QuickCreatePersonDto dto = new QuickCreatePersonDto("Anton Bas", PersonRole.ACTOR);
		when(personRepository.existsByNameAndRole("Anton Bas", PersonRole.ACTOR)).thenReturn(true);

		assertThatThrownBy(() -> personService.quickCreate(dto)).isInstanceOf(DuplicateEntityException.class);
	}

	@Test
	void searchPersons_ShouldReturnPagedResponse() {
		Person person = Person.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		PersonDto dto = PersonDto.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		Page<Person> page = new PageImpl<>(List.of(person));

		when(personRepository.searchPersons(eq("Anton"), eq(PersonRole.ACTOR), any(Pageable.class))).thenReturn(page);
		when(personMapper.toDto(person)).thenReturn(dto);

		PageResponse<PersonDto> result = personService.searchPersons("Anton", PersonRole.ACTOR, 0, 10);

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getName()).isEqualTo("Anton Bas");
		assertThat(result.getCurrentPage()).isEqualTo(0);
		assertThat(result.getTotalElements()).isEqualTo(1);
	}

}
