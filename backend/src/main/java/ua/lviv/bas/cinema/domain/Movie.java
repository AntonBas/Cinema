package ua.lviv.bas.cinema.domain;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

@Entity
@Table(name = "movies", indexes = { @Index(name = "idx_movie_title", columnList = "title"),
		@Index(name = "idx_movie_status", columnList = "status"),
		@Index(name = "idx_movie_release_date", columnList = "release_date"),
		@Index(name = "idx_movie_slug", columnList = "slug"),
		@Index(name = "idx_movie_active_dates", columnList = "release_date, end_showing_date") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Movie title is required")
	@Size(max = 255, message = "Title must be less than 255 characters")
	@Column(nullable = false)
	private String title;

	@NotBlank(message = "Slug is required")
	@Pattern(regexp = "^[a-z0-9-]+$", message = "Slug can only contain lowercase letters, numbers and hyphens")
	@Column(nullable = false, unique = true)
	private String slug;

	@NotBlank(message = "Trailer URL is required")
	@URL(message = "Trailer must be a valid URL")
	@Column(nullable = false)
	private String trailerUrl;

	@NotBlank(message = "Description is required")
	@Size(max = 1000, message = "Description must be less than 1000 characters")
	@Column(nullable = false, columnDefinition = "TEXT")
	private String description;

	@NotNull(message = "Duration is required")
	@Min(value = 1, message = "Duration must be at least 1 minute")
	@Column(nullable = false, name = "duration_minutes")
	private Integer durationMinutes;

	@NotNull(message = "Release date is required")
	@Column(nullable = false, name = "release_date")
	private LocalDate releaseDate;

	@NotNull(message = "End showing date is required")
	@Column(nullable = false, name = "end_showing_date")
	private LocalDate endShowingDate;

	@NotNull(message = "Movie status is required")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private MovieStatus status;

	@NotBlank(message = "Poster file name is required")
	@Column(nullable = false, name = "poster_file_name")
	private String posterFileName;

	@NotNull(message = "Age rating is required")
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "age_rating")
	private AgeRating ageRating;

	@OneToMany(mappedBy = "movie", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
	@Builder.Default
	private Set<Session> sessions = new HashSet<>();

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinTable(name = "movie_cast", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "persons_id"))
	@Builder.Default
	private Set<Person> cast = new HashSet<>();

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinTable(name = "movie_directors", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "persons_id"))
	@Builder.Default
	private Set<Person> directors = new HashSet<>();

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinTable(name = "movie_screenwriters", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "persons_id"))
	@Builder.Default
	private Set<Person> screenwriters = new HashSet<>();

	@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
	@Builder.Default
	private Set<Genre> genres = new HashSet<>();

	@AssertTrue(message = "End showing date must be after release date")
	public boolean isEndDateAfterReleaseDate() {
		if (releaseDate == null || endShowingDate == null) {
			return true;
		}
		return endShowingDate.isAfter(releaseDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Movie))
			return false;
		Movie other = (Movie) obj;
		return Objects.equals(id, other.id);
	}

	@Override
	public String toString() {
		return "Movie{" + "id=" + id + ", title='" + title + '\'' + ", slug='" + slug + '\'' + ", releaseDate="
				+ releaseDate + ", status=" + status + ", ageRating=" + ageRating + '}';
	}
}