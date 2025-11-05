package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;

public class GenreMapperTest {

	private final GenreMapper mapper = Mappers.getMapper(GenreMapper.class);

	@Test
	void toDto_ShouldMapAllFields() {
		Genre genre = Genre.builder().id(1L).name("Action").build();

		GenreResponse dto = mapper.toDto(genre);

		assertThat(dto).isNotNull();
		assertThat(dto.getId()).isEqualTo(1L);
		assertThat(dto.getName()).isEqualTo("Action");
	}

	@Test
	void toEntity_ShouldMapAllFields_FromRequest() {
		GenreRequest request = GenreRequest.builder().name("Thriller").build();

		Genre genre = mapper.toEntity(request);

		assertThat(genre).isNotNull();
		assertThat(genre.getName()).isEqualTo("Thriller");
		assertThat(genre.getId()).isNull();
	}

	@Test
	void toDtoList_ShouldMapList() {
		Genre genre1 = Genre.builder().id(1L).name("Action").build();
		Genre genre2 = Genre.builder().id(2L).name("Comedy").build();

		List<GenreResponse> dtos = mapper.toDtoList(List.of(genre1, genre2));

		assertThat(dtos).hasSize(2);
		assertThat(dtos.get(0).getName()).isEqualTo("Action");
		assertThat(dtos.get(1).getName()).isEqualTo("Comedy");
	}

	@Test
	void updateGenreFromRequest_ShouldUpdateFields() {
		Genre existing = Genre.builder().id(1L).name("OldName").build();

		GenreRequest updateRequest = GenreRequest.builder().name("NewName").build();

		mapper.updateGenreFromRequest(updateRequest, existing);

		assertThat(existing.getId()).isEqualTo(1L);
		assertThat(existing.getName()).isEqualTo("NewName");
	}
}
