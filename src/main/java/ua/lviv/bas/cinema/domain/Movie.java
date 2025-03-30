package ua.lviv.bas.cinema.domain;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieCategory;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

@Entity
@Table(name = "movies")
public class Movie {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false, name = "duration_minutes")
	private int durationMinutes;

	@Column(nullable = false)
	private String genre;

	@Column(nullable = false)
	private String director;

	@Column(nullable = false, name = "release_date")
	private LocalDate releaseDate;

	@Column(nullable = false, name = "end_showing_date")
	private LocalDate endShowingDate;

	@Enumerated(EnumType.STRING)
	private MovieStatus status;

	@Lob
	@Column(columnDefinition = "LONGBLOB")
	private byte[] posterImage;

	private String posterImagePath;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "age_rating")
	private AgeRating ageRating;

	@OneToMany(mappedBy = "movie")
	private List<Session> sessions;

	@Transient
	public MovieCategory getCategory() {
		LocalDate now = LocalDate.now();
		if (now.isBefore(releaseDate)) {
			return MovieCategory.UPCOMING;
		} else if (now.isAfter(endShowingDate)) {
			return MovieCategory.ARCHIVED;
		} else {
			return MovieCategory.CURRENT;
		}
	}

	public Movie() {
	}

	public Movie(String title, String description, int durationMinutes, String genre, String director,
			LocalDate releaseDate, LocalDate endShowingDate, MovieStatus status, byte[] posterImage,
			String posterImagePath, AgeRating ageRating, List<Session> sessions) {
		this.title = title;
		this.description = description;
		this.durationMinutes = durationMinutes;
		this.genre = genre;
		this.director = director;
		this.releaseDate = releaseDate;
		this.endShowingDate = endShowingDate;
		this.status = status;
		this.posterImage = posterImage;
		this.posterImagePath = posterImagePath;
		this.ageRating = ageRating;
		this.sessions = sessions;
	}

	public Movie(Integer id, String title, String description, int durationMinutes, String genre, String director,
			LocalDate releaseDate, LocalDate endShowingDate, MovieStatus status, byte[] posterImage,
			String posterImagePath, AgeRating ageRating, List<Session> sessions) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.durationMinutes = durationMinutes;
		this.genre = genre;
		this.director = director;
		this.releaseDate = releaseDate;
		this.endShowingDate = endShowingDate;
		this.status = status;
		this.posterImage = posterImage;
		this.posterImagePath = posterImagePath;
		this.ageRating = ageRating;
		this.sessions = sessions;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public LocalDate getReleaseDate() {
		return releaseDate;
	}

	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
	}

	public LocalDate getEndShowingDate() {
		return endShowingDate;
	}

	public void setEndShowingDate(LocalDate endShowingDate) {
		this.endShowingDate = endShowingDate;
	}

	public MovieStatus getStatus() {
		return status;
	}

	public void setStatus(MovieStatus status) {
		this.status = status;
	}

	public byte[] getPosterImage() {
		return posterImage;
	}

	public void setPosterImage(byte[] posterImage) {
		this.posterImage = posterImage;
	}

	public String getPosterImagePath() {
		return posterImagePath;
	}

	public void setPosterImagePath(String posterImagePath) {
		this.posterImagePath = posterImagePath;
	}

	public AgeRating getAgeRating() {
		return ageRating;
	}

	public void setAgeRating(AgeRating ageRating) {
		this.ageRating = ageRating;
	}

	public List<Session> getSessions() {
		return sessions;
	}

	public void setSessions(List<Session> sessions) {
		this.sessions = sessions;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(posterImage);
		result = prime * result + Objects.hash(ageRating, description, director, durationMinutes, endShowingDate, genre,
				id, posterImagePath, releaseDate, sessions, status, title);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Movie other = (Movie) obj;
		return ageRating == other.ageRating && Objects.equals(description, other.description)
				&& Objects.equals(director, other.director) && durationMinutes == other.durationMinutes
				&& Objects.equals(endShowingDate, other.endShowingDate) && Objects.equals(genre, other.genre)
				&& Objects.equals(id, other.id) && Arrays.equals(posterImage, other.posterImage)
				&& Objects.equals(posterImagePath, other.posterImagePath)
				&& Objects.equals(releaseDate, other.releaseDate) && Objects.equals(sessions, other.sessions)
				&& status == other.status && Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		return "Movie [id=" + id + ", title=" + title + ", description=" + description + ", durationMinutes="
				+ durationMinutes + ", genre=" + genre + ", director=" + director + ", releaseDate=" + releaseDate
				+ ", endShowingDate=" + endShowingDate + ", status=" + status + ", posterImage="
				+ Arrays.toString(posterImage) + ", posterImagePath=" + posterImagePath + ", ageRating=" + ageRating
				+ ", sessions=" + sessions + "]";
	}

}
