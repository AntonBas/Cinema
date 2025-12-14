package ua.lviv.bas.cinema.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.validator.constraints.URL;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = { "sessions", "actors", "directors", "screenwriters", "genres" })
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(name = "movies", indexes = { @Index(name = "idx_movie_title", columnList = "title"),
		@Index(name = "idx_movie_status", columnList = "status"),
		@Index(name = "idx_movie_release_date", columnList = "release_date"),
		@Index(name = "idx_movie_slug", columnList = "slug"),
		@Index(name = "idx_movie_active_dates", columnList = "release_date, end_showing_date") })
public class Movie {

	@Id
	@EqualsAndHashCode.Include
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 50)
	@Column(nullable = false)
	private String title;

	@NotBlank
	@Pattern(regexp = "^[a-z0-9-]+$")
	@Column(nullable = false, unique = true)
	private String slug;

	@NotBlank
	@URL
	@Column(nullable = false)
	private String trailerUrl;

	@NotBlank
	@Size(max = 1000)
	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@NotNull
	@Min(value = 1)
	@Column(nullable = false, name = "duration_minutes")
	private Integer durationMinutes;

	@NotNull
	@Column(nullable = false, name = "release_date")
	private LocalDate releaseDate;

	@NotNull
	@Column(nullable = false, name = "end_showing_date")
	private LocalDate endShowingDate;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MovieStatus status;

	@NotBlank
	@Column(nullable = false, name = "poster_file_name")
	private String posterFileName;

	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "age_rating")
	private AgeRating ageRating;

	@OneToMany(mappedBy = "movie", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@Builder.Default
	private Set<Session> sessions = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "movie_cast", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
	@Builder.Default
	private Set<Person> actors = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "movie_directors", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
	@Builder.Default
	private Set<Person> directors = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "movie_screenwriters", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "person_id"))
	@Builder.Default
	private Set<Person> screenwriters = new HashSet<>();

	@ManyToMany
	@JoinTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
	@Builder.Default
	private Set<Genre> genres = new HashSet<>();

	@AssertTrue
	public boolean isEndDateAfterReleaseDate() {
		if (releaseDate == null || endShowingDate == null) {
			return true;
		}
		return endShowingDate.isAfter(releaseDate);
	}
}