package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.Genre;
import ua.lviv.bas.cinema.dto.movie.request.GenreRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;

public class GenreMapperTest {

	private GenreMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = Mappers.getMapper(GenreMapper.class);
	}

	@Test
	void toDto_ShouldMapAllFieldsCorrectly() {
		Genre genre = Genre.builder().id(1L).name("Action").build();

		GenreResponse dto = mapper.toDto(genre);

		assertThat(dto).isNotNull().extracting(GenreResponse::getId, GenreResponse::getName).containsExactly(1L,
				"Action");
	}

	@Test
	void toDto_ShouldReturnNull_WhenInputIsNull() {
		GenreResponse dto = mapper.toDto(null);

		assertThat(dto).isNull();
	}

	@Test
	void toDto_ShouldMapGenreWithoutBuilder() {
		Genre genre = new Genre();
		genre.setId(2L);
		genre.setName("Comedy");

		GenreResponse dto = mapper.toDto(genre);

		assertThat(dto).isNotNull().extracting(GenreResponse::getId, GenreResponse::getName).containsExactly(2L,
				"Comedy");
	}

	@Test
	void toDto_ShouldTrimName_WhenNameHasSpaces() {
		Genre genre = Genre.builder().id(3L).name("  Drama  ").build();

		GenreResponse dto = mapper.toDto(genre);

		assertThat(dto.getName()).isEqualTo("  Drama  ");
	}

	@Test
	void toDto_ShouldHandleEmptyName() {
		Genre genre = Genre.builder().id(4L).name("").build();

		GenreResponse dto = mapper.toDto(genre);

		assertThat(dto.getName()).isEmpty();
	}

	@Test
	void toDto_ShouldHandleSpecialCharactersInName() {
		Genre genre = Genre.builder().id(5L).name("Sci-Fi & Fantasy").build();

		GenreResponse dto = mapper.toDto(genre);

		assertThat(dto.getName()).isEqualTo("Sci-Fi & Fantasy");
	}

	@ParameterizedTest
	@ValueSource(longs = { 0L, 1L, 100L, Long.MAX_VALUE })
	void toDto_ShouldHandleDifferentIds(Long id) {
		Genre genre = Genre.builder().id(id).name("Test").build();

		GenreResponse dto = mapper.toDto(genre);

		assertThat(dto.getId()).isEqualTo(id);
	}

	@Test
	void toDtoList_ShouldMapListOfGenres() {
		List<Genre> genres = Arrays.asList(Genre.builder().id(1L).name("Action").build(),
				Genre.builder().id(2L).name("Comedy").build(), Genre.builder().id(3L).name("Drama").build());

		List<GenreResponse> dtos = mapper.toDtoList(genres);

		assertThat(dtos).isNotNull().hasSize(3).extracting(GenreResponse::getName).containsExactly("Action", "Comedy",
				"Drama");
	}

	@Test
	void toDtoList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<GenreResponse> dtos = mapper.toDtoList(Collections.emptyList());

		assertThat(dtos).isNotNull().isEmpty();
	}

	@Test
	void toDtoList_ShouldReturnNull_WhenInputIsNull() {
		List<GenreResponse> dtos = mapper.toDtoList(null);

		assertThat(dtos).isNull();
	}

	@Test
	void toDtoList_ShouldHandleListWithNullElements() {
		List<Genre> genres = Arrays.asList(Genre.builder().id(1L).name("Action").build(), null,
				Genre.builder().id(2L).name("Comedy").build());

		List<GenreResponse> dtos = mapper.toDtoList(genres);

		assertThat(dtos).isNotNull().hasSize(3);

		assertThat(dtos.get(0)).isNotNull();
		assertThat(dtos.get(1)).isNull();
		assertThat(dtos.get(2)).isNotNull();
	}

	@Test
	void toDtoList_ShouldMapSingleElementList() {
		List<Genre> genres = Collections.singletonList(Genre.builder().id(1L).name("Single").build());

		List<GenreResponse> dtos = mapper.toDtoList(genres);

		assertThat(dtos).isNotNull().hasSize(1).first().extracting(GenreResponse::getId, GenreResponse::getName)
				.containsExactly(1L, "Single");
	}

	@Test
	void toEntity_ShouldMapAllFieldsFromRequest() {
		GenreRequest request = GenreRequest.builder().name("Thriller").build();

		Genre genre = mapper.toEntity(request);

		assertThat(genre).isNotNull().extracting(Genre::getName).isEqualTo("Thriller");

		assertThat(genre.getId()).isNull();
	}

	@Test
	void toEntity_ShouldReturnNull_WhenInputIsNull() {
		Genre genre = mapper.toEntity(null);

		assertThat(genre).isNull();
	}

	@Test
	void toEntity_ShouldHandleRequestWithSpaces() {
		GenreRequest request = GenreRequest.builder().name("  Horror  ").build();

		Genre genre = mapper.toEntity(request);

		assertThat(genre.getName()).isEqualTo("  Horror  ");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "\t", "\n" })
	void toEntity_ShouldHandleEmptyOrBlankNames(String name) {
		GenreRequest request = GenreRequest.builder().name(name).build();

		Genre genre = mapper.toEntity(request);

		assertThat(genre.getName()).isEqualTo(name);
	}

	@Test
	void updateGenreFromRequest_ShouldUpdateNameField() {
		Genre existing = Genre.builder().id(1L).name("OldName").build();

		GenreRequest updateRequest = GenreRequest.builder().name("NewName").build();

		mapper.updateGenreFromRequest(updateRequest, existing);

		assertThat(existing).extracting(Genre::getId, Genre::getName).containsExactly(1L, "NewName");
	}

	@Test
	void updateGenreFromRequest_ShouldNotUpdateId() {
		Genre existing = Genre.builder().id(999L).name("OldName").build();

		GenreRequest updateRequest = GenreRequest.builder().name("NewName").build();

		mapper.updateGenreFromRequest(updateRequest, existing);

		assertThat(existing.getId()).isEqualTo(999L);
	}

	@Test
	void updateGenreFromRequest_ShouldNotUpdate_WhenRequestIsNull() {
		Genre existing = Genre.builder().id(1L).name("Original").build();

		mapper.updateGenreFromRequest(null, existing);

		assertThat(existing.getName()).isEqualTo("Original");
	}

	@Test
	void updateGenreFromRequest_ShouldHandleEmptyRequestName() {
		Genre existing = Genre.builder().id(1L).name("Original").build();

		GenreRequest updateRequest = GenreRequest.builder().name("").build();

		mapper.updateGenreFromRequest(updateRequest, existing);

		assertThat(existing.getName()).isEmpty();
	}

	@Test
	void updateGenreFromRequest_ShouldNotChangeOtherFields() {
		Genre existing = Genre.builder().id(1L).name("Original").build();

		GenreRequest updateRequest = GenreRequest.builder().name("Updated").build();

		mapper.updateGenreFromRequest(updateRequest, existing);

		assertThat(existing).hasFieldOrPropertyWithValue("id", 1L).hasFieldOrPropertyWithValue("name", "Updated");
	}

	@Test
	void updateGenreFromRequest_ShouldHandleSameNameUpdate() {
		Genre existing = Genre.builder().id(1L).name("SameName").build();

		GenreRequest updateRequest = GenreRequest.builder().name("SameName").build();

		mapper.updateGenreFromRequest(updateRequest, existing);

		assertThat(existing.getName()).isEqualTo("SameName");
	}

	@Test
	void updateGenreFromRequest_ShouldNotUpdateNullTarget() {
		GenreRequest updateRequest = GenreRequest.builder().name("Test").build();

		assertThatThrownBy(() -> mapper.updateGenreFromRequest(updateRequest, null))
				.isInstanceOf(NullPointerException.class);
	}

	@Test
	void consistencyCheck_ToEntityThenToDto_ShouldReturnSameValues() {
		GenreRequest request = GenreRequest.builder().name("Consistency Test").build();

		Genre entity = mapper.toEntity(request);
		GenreResponse dto = mapper.toDto(entity);

		assertThat(dto.getName()).isEqualTo("Consistency Test");
	}

	@Test
	void updateThenToDto_ShouldReflectChanges() {
		Genre genre = Genre.builder().id(1L).name("Before").build();

		GenreRequest update = GenreRequest.builder().name("After").build();

		mapper.updateGenreFromRequest(update, genre);
		GenreResponse dto = mapper.toDto(genre);

		assertThat(dto.getName()).isEqualTo("After");
	}
}