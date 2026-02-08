package ua.lviv.bas.cinema.domain.projection;

import java.time.LocalDate;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movie_card_projection")
@Immutable
public class MovieCardProjection {

	@Id
	private Long id;

	@Column(nullable = false, length = 50)
	private String title;

	@Column(nullable = false, unique = true, length = 100)
	private String slug;

	@Column(nullable = false, name = "poster_file_name", length = 100)
	private String posterFileName;

	@Column(nullable = false, name = "duration_minutes")
	private Integer durationMinutes;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "age_rating", length = 20)
	private AgeRating ageRating;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MovieStatus status;

	@Column(nullable = false, name = "release_date")
	private LocalDate releaseDate;

	@Column(nullable = false, name = "end_showing_date")
	private LocalDate endShowingDate;
}