package ua.lviv.bas.cinema.mapper.cinema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import ua.lviv.bas.cinema.domain.cinema.Genre;
import ua.lviv.bas.cinema.domain.cinema.Movie;
import ua.lviv.bas.cinema.domain.cinema.Person;
import ua.lviv.bas.cinema.domain.cinema.enums.AgeRating;
import ua.lviv.bas.cinema.domain.cinema.status.MovieStatus;
import ua.lviv.bas.cinema.dto.movie.request.MovieCreateRequest;
import ua.lviv.bas.cinema.dto.movie.request.MovieUpdateRequest;
import ua.lviv.bas.cinema.dto.movie.response.GenreResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieCardResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieDetailResponse;
import ua.lviv.bas.cinema.dto.movie.response.MovieSessionSearchResponse;
import ua.lviv.bas.cinema.dto.movie.response.PersonResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieCardProjection;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieDetailProjection;
import ua.lviv.bas.cinema.repository.cinema.projection.MovieSessionSearchProjection;

@SpringBootTest(classes = { MovieMapperImpl.class, PersonMapper.class, GenreMapper.class })
public class MovieMapperTest {

	@Autowired
	private MovieMapper mapper;

	@MockitoBean
	private PersonMapper personMapper;

	@MockitoBean
	private GenreMapper genreMapper;

	@Test
	public void toMovieCardResponseFromMovie_ShouldMapAllFields() {
		Movie movie = Movie.builder().id(1L).title("Test Movie").slug("test-movie").durationMinutes(120)
				.ageRating(AgeRating.PEGI_12).status(MovieStatus.CURRENT).build();

		MovieCardResponse response = mapper.toMovieCardResponse(movie);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.slug()).isEqualTo("test-movie");
		assertThat(response.title()).isEqualTo("Test Movie");
		assertThat(response.durationMinutes()).isEqualTo(120);
		assertThat(response.ageRating()).isEqualTo(AgeRating.PEGI_12);
		assertThat(response.status()).isEqualTo(MovieStatus.CURRENT);
		assertThat(response.posterUrl()).isEqualTo("/api/movies/1/poster");
	}

	@Test
	public void toMovieCardResponseFromProjection_ShouldMapAllFields() {
		MovieCardProjection projection = new MovieCardProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getSlug() {
				return "test-movie";
			}

			@Override
			public String getTitle() {
				return "Test Movie";
			}

			@Override
			public String getPosterFileName() {
				return "poster.jpg";
			}

			@Override
			public Integer getDurationMinutes() {
				return 120;
			}

			@Override
			public AgeRating getAgeRating() {
				return AgeRating.PEGI_12;
			}

			@Override
			public MovieStatus getStatus() {
				return MovieStatus.CURRENT;
			}

			@Override
			public LocalDate getReleaseDate() {
				return LocalDate.of(2024, 1, 15);
			}

			@Override
			public LocalDate getEndShowingDate() {
				return LocalDate.of(2024, 3, 15);
			}
		};

		MovieCardResponse response = mapper.toMovieCardResponse(projection);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.slug()).isEqualTo("test-movie");
		assertThat(response.title()).isEqualTo("Test Movie");
		assertThat(response.durationMinutes()).isEqualTo(120);
		assertThat(response.ageRating()).isEqualTo(AgeRating.PEGI_12);
		assertThat(response.status()).isEqualTo(MovieStatus.CURRENT);
		assertThat(response.posterUrl()).isEqualTo("/api/movies/1/poster");
	}

	@Test
	public void toMovieDetailResponseFromMovie_ShouldMapAllFields() {
		Genre genre = Genre.builder().id(1L).name("Action").build();
		Person actor = Person.builder().id(1L).name("Actor Name").build();
		Person director = Person.builder().id(2L).name("Director Name").build();
		Person screenwriter = Person.builder().id(3L).name("Screenwriter Name").build();

		Set<Genre> genres = new HashSet<>();
		genres.add(genre);

		Set<Person> actors = new HashSet<>();
		actors.add(actor);

		Set<Person> directors = new HashSet<>();
		directors.add(director);

		Set<Person> screenwriters = new HashSet<>();
		screenwriters.add(screenwriter);

		GenreResponse genreResponse = new GenreResponse(1L, "Action", 0);
		PersonResponse actorResponse = new PersonResponse(1L, "Actor Name", null, 0);
		PersonResponse directorResponse = new PersonResponse(2L, "Director Name", null, 0);
		PersonResponse screenwriterResponse = new PersonResponse(3L, "Screenwriter Name", null, 0);

		when(genreMapper.toGenreResponse(genre)).thenReturn(genreResponse);
		when(personMapper.toPersonResponse(actor)).thenReturn(actorResponse);
		when(personMapper.toPersonResponse(director)).thenReturn(directorResponse);
		when(personMapper.toPersonResponse(screenwriter)).thenReturn(screenwriterResponse);

		Movie movie = Movie.builder().id(1L).title("Test Movie").slug("test-movie")
				.trailerUrl("https://youtube.com/watch?v=123").description("Test Description").durationMinutes(120)
				.releaseDate(LocalDate.of(2024, 1, 15)).endShowingDate(LocalDate.of(2024, 3, 15))
				.ageRating(AgeRating.PEGI_12).status(MovieStatus.CURRENT).posterFileName("poster.jpg").genres(genres)
				.actors(actors).directors(directors).screenwriters(screenwriters).build();

		MovieDetailResponse response = mapper.toMovieDetailResponse(movie);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Test Movie");
		assertThat(response.slug()).isEqualTo("test-movie");
		assertThat(response.trailerUrl()).isEqualTo("https://youtube.com/watch?v=123");
		assertThat(response.description()).isEqualTo("Test Description");
		assertThat(response.durationMinutes()).isEqualTo(120);
		assertThat(response.releaseDate()).isEqualTo(LocalDate.of(2024, 1, 15));
		assertThat(response.endShowingDate()).isEqualTo(LocalDate.of(2024, 3, 15));
		assertThat(response.ageRating()).isEqualTo(AgeRating.PEGI_12);
		assertThat(response.status()).isEqualTo(MovieStatus.CURRENT);
		assertThat(response.posterFileName()).isEqualTo("poster.jpg");
		assertThat(response.posterUrl()).isEqualTo("/api/movies/1/poster");
		assertThat(response.genres()).hasSize(1);
		assertThat(response.actors()).hasSize(1);
		assertThat(response.directors()).hasSize(1);
		assertThat(response.screenwriters()).hasSize(1);
	}

	@Test
	public void toMovieDetailResponseFromProjection_ShouldMapBasicFields() {
		MovieDetailProjection projection = new MovieDetailProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getTitle() {
				return "Test Movie";
			}

			@Override
			public String getSlug() {
				return "test-movie";
			}

			@Override
			public String getTrailerUrl() {
				return "https://youtube.com/watch?v=123";
			}

			@Override
			public String getDescription() {
				return "Test Description";
			}

			@Override
			public Integer getDurationMinutes() {
				return 120;
			}

			@Override
			public LocalDate getReleaseDate() {
				return LocalDate.of(2024, 1, 15);
			}

			@Override
			public LocalDate getEndShowingDate() {
				return LocalDate.of(2024, 3, 15);
			}

			@Override
			public AgeRating getAgeRating() {
				return AgeRating.PEGI_12;
			}

			@Override
			public MovieStatus getStatus() {
				return MovieStatus.CURRENT;
			}

			@Override
			public String getPosterFileName() {
				return "poster.jpg";
			}
		};

		MovieDetailResponse response = mapper.toMovieDetailResponse(projection);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Test Movie");
		assertThat(response.slug()).isEqualTo("test-movie");
		assertThat(response.trailerUrl()).isEqualTo("https://youtube.com/watch?v=123");
		assertThat(response.description()).isEqualTo("Test Description");
		assertThat(response.durationMinutes()).isEqualTo(120);
		assertThat(response.releaseDate()).isEqualTo(LocalDate.of(2024, 1, 15));
		assertThat(response.endShowingDate()).isEqualTo(LocalDate.of(2024, 3, 15));
		assertThat(response.ageRating()).isEqualTo(AgeRating.PEGI_12);
		assertThat(response.status()).isEqualTo(MovieStatus.CURRENT);
		assertThat(response.posterFileName()).isEqualTo("poster.jpg");
		assertThat(response.posterUrl()).isEqualTo("/api/movies/1/poster");
		assertThat(response.genres()).isNull();
		assertThat(response.actors()).isNull();
		assertThat(response.directors()).isNull();
		assertThat(response.screenwriters()).isNull();
	}

	@Test
	public void toMovieSessionSearchResponse_ShouldMapFields() {
		MovieSessionSearchProjection projection = new MovieSessionSearchProjection() {
			@Override
			public Long getId() {
				return 1L;
			}

			@Override
			public String getTitle() {
				return "Test Movie";
			}

			@Override
			public Integer getDurationMinutes() {
				return 120;
			}
		};

		MovieSessionSearchResponse response = mapper.toMovieSessionSearchResponse(projection);

		assertThat(response).isNotNull();
		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.title()).isEqualTo("Test Movie");
		assertThat(response.durationMinutes()).isEqualTo(120);
	}

	@Test
	public void toMovie_ShouldMapCreateRequestToEntity() {
		MovieCreateRequest request = MovieCreateRequest.builder().title("New Movie")
				.trailerUrl("https://youtube.com/watch?v=123").description("Description").durationMinutes(120)
				.releaseDate(LocalDate.of(2024, 1, 15)).endShowingDate(LocalDate.of(2024, 3, 15))
				.ageRating(AgeRating.PEGI_12).genreIds(List.of(1L, 2L)).actorIds(List.of(1L, 2L))
				.directorIds(List.of(3L)).screenwriterIds(List.of(4L)).build();

		Movie movie = mapper.toMovie(request);

		assertThat(movie).isNotNull();
		assertThat(movie.getId()).isNull();
		assertThat(movie.getSlug()).isNull();
		assertThat(movie.getStatus()).isNull();
		assertThat(movie.getTitle()).isEqualTo("New Movie");
		assertThat(movie.getTrailerUrl()).isEqualTo("https://youtube.com/watch?v=123");
		assertThat(movie.getDescription()).isEqualTo("Description");
		assertThat(movie.getDurationMinutes()).isEqualTo(120);
		assertThat(movie.getReleaseDate()).isEqualTo(LocalDate.of(2024, 1, 15));
		assertThat(movie.getEndShowingDate()).isEqualTo(LocalDate.of(2024, 3, 15));
		assertThat(movie.getAgeRating()).isEqualTo(AgeRating.PEGI_12);
		assertThat(movie.getPosterFileName()).isNull();
		assertThat(movie.getGenres()).isNotNull().isEmpty();
		assertThat(movie.getActors()).isNotNull().isEmpty();
		assertThat(movie.getDirectors()).isNotNull().isEmpty();
		assertThat(movie.getScreenwriters()).isNotNull().isEmpty();
	}

	@Test
	public void updateMovieFromRequest_ShouldUpdateFields() {
		Movie movie = Movie.builder().id(1L).title("Old Title").description("Old Description").durationMinutes(100)
				.build();

		MovieUpdateRequest request = MovieUpdateRequest.builder().title("New Title").description("New Description")
				.durationMinutes(120).build();

		mapper.updateMovieFromRequest(request, movie);

		assertThat(movie.getId()).isEqualTo(1L);
		assertThat(movie.getTitle()).isEqualTo("New Title");
		assertThat(movie.getDescription()).isEqualTo("New Description");
		assertThat(movie.getDurationMinutes()).isEqualTo(120);
	}

	@Test
	public void updateMovieFromRequest_WithNullFields_ShouldIgnoreNull() {
		Movie movie = Movie.builder().id(1L).title("Old Title").description("Old Description").durationMinutes(100)
				.build();

		MovieUpdateRequest request = MovieUpdateRequest.builder().title(null).description(null).durationMinutes(null)
				.build();

		mapper.updateMovieFromRequest(request, movie);

		assertThat(movie.getId()).isEqualTo(1L);
		assertThat(movie.getTitle()).isEqualTo("Old Title");
		assertThat(movie.getDescription()).isEqualTo("Old Description");
		assertThat(movie.getDurationMinutes()).isEqualTo(100);
	}

	@Test
	public void updateMovieFromRequest_WithNullRequest_ShouldNotChange() {
		Movie movie = Movie.builder().id(1L).title("Old Title").description("Old Description").build();

		mapper.updateMovieFromRequest(null, movie);

		assertThat(movie.getTitle()).isEqualTo("Old Title");
		assertThat(movie.getDescription()).isEqualTo("Old Description");
	}

	@Test
	public void toMovieCardResponse_WithNullMovie_ShouldReturnNull() {
		MovieCardResponse response = mapper.toMovieCardResponse((Movie) null);
		assertThat(response).isNull();
	}

	@Test
	public void toMovieCardResponse_WithNullProjection_ShouldReturnNull() {
		MovieCardResponse response = mapper.toMovieCardResponse((MovieCardProjection) null);
		assertThat(response).isNull();
	}

	@Test
	public void toMovieDetailResponse_WithNullMovie_ShouldReturnNull() {
		MovieDetailResponse response = mapper.toMovieDetailResponse((Movie) null);
		assertThat(response).isNull();
	}

	@Test
	public void toMovieDetailResponse_WithNullProjection_ShouldReturnNull() {
		MovieDetailResponse response = mapper.toMovieDetailResponse((MovieDetailProjection) null);
		assertThat(response).isNull();
	}

	@Test
	public void toMovieSessionSearchResponse_WithNullProjection_ShouldReturnNull() {
		MovieSessionSearchResponse response = mapper.toMovieSessionSearchResponse(null);
		assertThat(response).isNull();
	}

	@Test
	public void toMovie_WithNullRequest_ShouldReturnNull() {
		Movie movie = mapper.toMovie((MovieCreateRequest) null);
		assertThat(movie).isNull();
	}
}