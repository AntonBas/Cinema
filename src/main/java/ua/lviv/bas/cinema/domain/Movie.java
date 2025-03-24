package ua.lviv.bas.cinema.domain;

import java.time.LocalDate;
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

	@Lob
	private String posterImage;

	@Enumerated(EnumType.STRING)
	private AgeRating ageRating;

	@OneToMany(mappedBy = "movie")
	private List<Session> sessions;

	public Movie() {
	}

	public Movie(String title, String description, int durationMinutes, String genre, String director,
			LocalDate releaseDate, String posterImage, AgeRating ageRating, List<Session> sessions) {
		this.title = title;
		this.description = description;
		this.durationMinutes = durationMinutes;
		this.genre = genre;
		this.director = director;
		this.releaseDate = releaseDate;
		this.posterImage = posterImage;
		this.ageRating = ageRating;
		this.sessions = sessions;
	}

	public Movie(Integer id, String title, String description, int durationMinutes, String genre, String director,
			LocalDate releaseDate, String posterImage, AgeRating ageRating, List<Session> sessions) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.durationMinutes = durationMinutes;
		this.genre = genre;
		this.director = director;
		this.releaseDate = releaseDate;
		this.posterImage = posterImage;
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

	public String getPosterImage() {
		return posterImage;
	}

	public void setPosterImage(String posterImage) {
		this.posterImage = posterImage;
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
		return Objects.hash(ageRating, description, director, durationMinutes, genre, id, posterImage, releaseDate,
				sessions, title);
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
				&& Objects.equals(genre, other.genre) && Objects.equals(id, other.id)
				&& Objects.equals(posterImage, other.posterImage) && Objects.equals(releaseDate, other.releaseDate)
				&& Objects.equals(sessions, other.sessions) && Objects.equals(title, other.title);
	}

	@Override
	public String toString() {
		return "Movie [id=" + id + ", title=" + title + ", description=" + description + ", durationMinutes="
				+ durationMinutes + ", genre=" + genre + ", director=" + director + ", releaseDate=" + releaseDate
				+ ", posterImage=" + posterImage + ", ageRating=" + ageRating + ", sessions=" + sessions + "]";
	}

}
