package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Person;
import ua.lviv.bas.cinema.domain.enums.PersonRole;
import ua.lviv.bas.cinema.dto.PersonDto;

public class PersonMapperTest {

	private final PersonMapper mapper = Mappers.getMapper(PersonMapper.class);

	@Test
	void toDto_ShouldMapAllFields() {
		Person person = Person.builder().id(1L).name("Anton Bas").role(PersonRole.ACTOR).build();

		PersonDto dto = mapper.toDto(person);

		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getName()).isEqualTo("Anton Bas");
		assertThat(dto.getRole()).isEqualTo(PersonRole.ACTOR);
	}

	@Test
	void toEntity_ShouldMapAllFields() {
		PersonDto dto = PersonDto.builder().id(1L).name("Anton Bas").role(PersonRole.DIRECTOR).build();

		Person person = mapper.toEntity(dto);

		assertThat(person.getId()).isEqualTo(1L);
		assertThat(person.getName()).isEqualTo("Anton Bas");
		assertThat(person.getRole()).isEqualTo(PersonRole.DIRECTOR);
	}

	@Test
	void toDtoList_ShouldMapList() {
		Person person1 = Person.builder().id(1L).name("Anton Bas").build();
		Person person2 = Person.builder().id(2L).name("Bas Anton").build();

		List<PersonDto> dtos = mapper.toDtoList(List.of(person1, person2));

		assertThat(dtos).hasSize(2);
		assertThat(dtos.get(0).getName()).isEqualTo("Anton Bas");
		assertThat(dtos.get(1).getName()).isEqualTo("Bas Anton");
	}
}