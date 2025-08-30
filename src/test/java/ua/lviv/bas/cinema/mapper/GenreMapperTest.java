package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.GenreDto;

public class GenreMapperTest {

	private final GenreMapper mapper = Mappers.getMapper(GenreMapper.class);

	@Test
	void toDto_ShouldMapAllFields() {
		Genre genre = Genre.builder().id(1L).name("Action").build();

		GenreDto dto = mapper.toDto(genre);

		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getName()).isEqualTo("Action");
	}

	@Test
	void toEntity_ShouldMapAllFields() {
		GenreDto dto = GenreDto.builder().id(1L).name("Action").build();

		Genre genre = mapper.toEntity(dto);

		assertThat(genre.getId()).isEqualTo(1L);
		assertThat(genre.getName()).isEqualTo("Action");
	}

	@Test
	void toDtoList_ShouldMapList() {
		Genre genre1 = Genre.builder().id(1L).name("Action").build();
		Genre genre2 = Genre.builder().id(2L).name("Comedy").build();

		List<GenreDto> dtos = mapper.toDtoList(List.of(genre1, genre2));

		assertThat(dtos).hasSize(2);
		assertThat(dtos.get(0).getName()).isEqualTo("Action");
		assertThat(dtos.get(1).getName()).isEqualTo("Comedy");
	}
}