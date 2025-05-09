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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
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
	private String slug;

	@Column(nullable = false)
	private double price;

	@Column(nullable = false)
	private String trailer;

	@Column(nullable = false)
	private String description;

	@Column(nullable = false)
	private String production;

	@Column(nullable = false, name = "duration_minutes")
	private int durationMinutes;

	@Column(nullable = false)
	private String director;

	@Column(nullable = false, name = "release_year")
	private int releaseYear;

	@Column(nullable = false, name = "release_date")
	private LocalDate releaseDate;

	@Column(nullable = false, name = "end_showing_date")
	private LocalDate endShowingDate;

	@Column(nullable = false)
	private String screenwriter;

	@Column(nullable = false, name = "main_cast", columnDefinition = "TEXT")
	private String mainCast;

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

	@ManyToMany
	@JoinTable(name = "movie-genres", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
	private List<Genre> genres;

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

	public Movie(String title, String slug, double price, String trailer, String description, String production,
			int durationMinutes, String director, int releaseYear, LocalDate releaseDate, LocalDate endShowingDate,
			String screenwriter, String mainCast, MovieStatus status, byte[] posterImage, String posterImagePath,
			AgeRating ageRating, List<Session> sessions, List<Genre> genres) {
		this.title = title;
		this.slug = slug;
		this.price = price;
		this.trailer = trailer;
		this.description = description;
		this.production = production;
		this.durationMinutes = durationMinutes;
		this.director = director;
		this.releaseYear = releaseYear;
		this.releaseDate = releaseDate;
		this.endShowingDate = endShowingDate;
		this.screenwriter = screenwriter;
		this.mainCast = mainCast;
		this.status = status;
		this.posterImage = posterImage;
		this.posterImagePath = posterImagePath;
		this.ageRating = ageRating;
		this.sessions = sessions;
		this.genres = genres;
	}

	public Movie(Integer id, String title, String slug, double price, String trailer, String description,
			String production, int durationMinutes, String director, int releaseYear, LocalDate releaseDate,
			LocalDate endShowingDate, String screenwriter, String mainCast, MovieStatus status, byte[] posterImage,
			String posterImagePath, AgeRating ageRating, List<Session> sessions, List<Genre> genres) {
		this.id = id;
		this.title = title;
		this.slug = slug;
		this.price = price;
		this.trailer = trailer;
		this.description = description;
		this.production = production;
		this.durationMinutes = durationMinutes;
		this.director = director;
		this.releaseYear = releaseYear;
		this.releaseDate = releaseDate;
		this.endShowingDate = endShowingDate;
		this.screenwriter = screenwriter;
		this.mainCast = mainCast;
		this.status = status;
		this.posterImage = posterImage;
		this.posterImagePath = posterImagePath;
		this.ageRating = ageRating;
		this.sessions = sessions;
		this.genres = genres;
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

	public String getSlug() {
		return slug;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public String getTrailer() {
		return trailer;
	}

	public void setTrailer(String trailer) {
		this.trailer = trailer;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProduction() {
		return production;
	}

	public void setProduction(String production) {
		this.production = production;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public String getDirector() {
		return director;
	}

	public void setDirector(String director) {
		this.director = director;
	}

	public int getReleaseYear() {
		return releaseYear;
	}

	public void setReleaseYear(int releaseYear) {
		this.releaseYear = releaseYear;
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

	public String getScreenwriter() {
		return screenwriter;
	}

	public void setScreenwriter(String screenwriter) {
		this.screenwriter = screenwriter;
	}

	public String getMainCast() {
		return mainCast;
	}

	public void setMainCast(String mainCast) {
		this.mainCast = mainCast;
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

	public List<Genre> getGenres() {
		return genres;
	}

	public void setGenres(List<Genre> genres) {
		this.genres = genres;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(posterImage);
		result = prime * result + Objects.hash(ageRating, description, director, durationMinutes, endShowingDate,
				genres, id, mainCast, posterImagePath, price, production, releaseDate, releaseYear, screenwriter,
				sessions, slug, status, title, trailer);
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
				&& Objects.equals(endShowingDate, other.endShowingDate) && Objects.equals(genres, other.genres)
				&& Objects.equals(id, other.id) && Objects.equals(mainCast, other.mainCast)
				&& Arrays.equals(posterImage, other.posterImage)
				&& Objects.equals(posterImagePath, other.posterImagePath)
				&& Double.doubleToLongBits(price) == Double.doubleToLongBits(other.price)
				&& Objects.equals(production, other.production) && Objects.equals(releaseDate, other.releaseDate)
				&& releaseYear == other.releaseYear && Objects.equals(screenwriter, other.screenwriter)
				&& Objects.equals(sessions, other.sessions) && Objects.equals(slug, other.slug)
				&& status == other.status && Objects.equals(title, other.title)
				&& Objects.equals(trailer, other.trailer);
	}

	@Override
	public String toString() {
		return "Movie [id=" + id + ", title=" + title + ", slug=" + slug + ", price=" + price + ", trailer=" + trailer
				+ ", description=" + description + ", production=" + production + ", durationMinutes=" + durationMinutes
				+ ", director=" + director + ", releaseYear=" + releaseYear + ", releaseDate=" + releaseDate
				+ ", endShowingDate=" + endShowingDate + ", screenwriter=" + screenwriter + ", mainCast=" + mainCast
				+ ", status=" + status + ", posterImage=" + Arrays.toString(posterImage) + ", posterImagePath="
				+ posterImagePath + ", ageRating=" + ageRating + ", sessions=" + sessions + ", genres=" + genres + "]";
	}

}