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
	void toGenreResponse_ShouldMapAllFieldsCorrectly() {
		Genre genre = Genre.builder().id(1L).name("Action").build();

		GenreResponse response = mapper.toGenreResponse(genre);

		assertThat(response).isNotNull().extracting(GenreResponse::getId, GenreResponse::getName).containsExactly(1L,
				"Action");
	}

	@Test
	void toGenreResponse_ShouldReturnNull_WhenInputIsNull() {
		GenreResponse response = mapper.toGenreResponse(null);

		assertThat(response).isNull();
	}

	@Test
	void toGenreResponse_ShouldMapGenreWithoutBuilder() {
		Genre genre = new Genre();
		genre.setId(2L);
		genre.setName("Comedy");

		GenreResponse response = mapper.toGenreResponse(genre);

		assertThat(response).isNotNull().extracting(GenreResponse::getId, GenreResponse::getName).containsExactly(2L,
				"Comedy");
	}

	@Test
	void toGenreResponse_ShouldTrimName_WhenNameHasSpaces() {
		Genre genre = Genre.builder().id(3L).name("  Drama  ").build();

		GenreResponse response = mapper.toGenreResponse(genre);

		assertThat(response.getName()).isEqualTo("  Drama  ");
	}

	@Test
	void toGenreResponse_ShouldHandleEmptyName() {
		Genre genre = Genre.builder().id(4L).name("").build();

		GenreResponse response = mapper.toGenreResponse(genre);

		assertThat(response.getName()).isEmpty();
	}

	@Test
	void toGenreResponse_ShouldHandleSpecialCharactersInName() {
		Genre genre = Genre.builder().id(5L).name("Sci-Fi & Fantasy").build();

		GenreResponse response = mapper.toGenreResponse(genre);

		assertThat(response.getName()).isEqualTo("Sci-Fi & Fantasy");
	}

	@ParameterizedTest
	@ValueSource(longs = { 0L, 1L, 100L, Long.MAX_VALUE })
	void toGenreResponse_ShouldHandleDifferentIds(Long id) {
		Genre genre = Genre.builder().id(id).name("Test").build();

		GenreResponse response = mapper.toGenreResponse(genre);

		assertThat(response.getId()).isEqualTo(id);
	}

	@Test
	void toGenreResponseList_ShouldMapListOfGenres() {
		List<Genre> genres = Arrays.asList(Genre.builder().id(1L).name("Action").build(),
				Genre.builder().id(2L).name("Comedy").build(), Genre.builder().id(3L).name("Drama").build());

		List<GenreResponse> responses = mapper.toGenreResponseList(genres);

		assertThat(responses).isNotNull().hasSize(3).extracting(GenreResponse::getName).containsExactly("Action",
				"Comedy", "Drama");
	}

	@Test
	void toGenreResponseList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<GenreResponse> responses = mapper.toGenreResponseList(Collections.emptyList());

		assertThat(responses).isNotNull().isEmpty();
	}

	@Test
	void toGenreResponseList_ShouldReturnNull_WhenInputIsNull() {
		List<GenreResponse> responses = mapper.toGenreResponseList(null);

		assertThat(responses).isNull();
	}

	@Test
	void toGenreResponseList_ShouldHandleListWithNullElements() {
		List<Genre> genres = Arrays.asList(Genre.builder().id(1L).name("Action").build(), null,
				Genre.builder().id(2L).name("Comedy").build());

		List<GenreResponse> responses = mapper.toGenreResponseList(genres);

		assertThat(responses).isNotNull().hasSize(3);

		assertThat(responses.get(0)).isNotNull();
		assertThat(responses.get(1)).isNull();
		assertThat(responses.get(2)).isNotNull();
	}

	@Test
	void toGenreResponseList_ShouldMapSingleElementList() {
		List<Genre> genres = Collections.singletonList(Genre.builder().id(1L).name("Single").build());

		List<GenreResponse> responses = mapper.toGenreResponseList(genres);

		assertThat(responses).isNotNull().hasSize(1).first().extracting(GenreResponse::getId, GenreResponse::getName)
				.containsExactly(1L, "Single");
	}

	@Test
	void toGenre_ShouldMapAllFieldsFromRequest() {
		GenreRequest request = GenreRequest.builder().name("Thriller").build();

		Genre genre = mapper.toGenre(request);

		assertThat(genre).isNotNull().extracting(Genre::getName).isEqualTo("Thriller");

		assertThat(genre.getId()).isNull();
	}

	@Test
	void toGenre_ShouldReturnNull_WhenInputIsNull() {
		Genre genre = mapper.toGenre(null);

		assertThat(genre).isNull();
	}

	@Test
	void toGenre_ShouldHandleRequestWithSpaces() {
		GenreRequest request = GenreRequest.builder().name("  Horror  ").build();

		Genre genre = mapper.toGenre(request);

		assertThat(genre.getName()).isEqualTo("  Horror  ");
	}

	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = { " ", "\t", "\n" })
	void toGenre_ShouldHandleEmptyOrBlankNames(String name) {
		GenreRequest request = GenreRequest.builder().name(name).build();

		Genre genre = mapper.toGenre(request);

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
	void consistencyCheck_ToGenreThenToGenreResponse_ShouldReturnSameValues() {
		GenreRequest request = GenreRequest.builder().name("Consistency Test").build();

		Genre entity = mapper.toGenre(request);
		GenreResponse response = mapper.toGenreResponse(entity);

		assertThat(response.getName()).isEqualTo("Consistency Test");
	}

	@Test
	void updateGenreFromRequestThenToGenreResponse_ShouldReflectChanges() {
		Genre genre = Genre.builder().id(1L).name("Before").build();

		GenreRequest update = GenreRequest.builder().name("After").build();

		mapper.updateGenreFromRequest(update, genre);
		GenreResponse response = mapper.toGenreResponse(genre);

		assertThat(response.getName()).isEqualTo("After");
	}
}