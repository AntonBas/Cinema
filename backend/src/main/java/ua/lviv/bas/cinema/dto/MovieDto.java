package ua.lviv.bas.cinema.dto;

import java.time.LocalDate;
import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.lviv.bas.cinema.domain.enums.AgeRating;
import ua.lviv.bas.cinema.domain.enums.MovieStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDto {

    private Long id;

    @NotBlank(message = "Movie title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;

    @NotBlank(message = "Slug is required")
    @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug can only contain lowercase letters, numbers and hyphens")
    private String slug;

    @NotBlank(message = "Trailer URL is required")
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "Trailer must be a valid URL")
    private String trailer;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must be less than 1000 characters")
    private String description;

    @NotBlank(message = "Production company is required")
    @Size(max = 255, message = "Production must be less than 255 characters")
    private String production;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    @NotNull(message = "Release date is required")
    @PastOrPresent(message = "Release date cannot be in the future")
    private LocalDate releaseDate;

    @NotNull(message = "End showing date is required")
    @Future(message = "End showing date must be in the future")
    private LocalDate endShowingDate;

    @NotNull(message = "Movie status is required")
    private MovieStatus status;

    private String posterFileName;

    private MultipartFile posterFile;

    @NotNull(message = "Age rating is required")
    private AgeRating ageRating;

    private Set<Long> sessionIds;
    private Set<Long> castIds;
    private Set<Long> directorIds;
    private Set<Long> screenwriterIds;
    private Set<Long> genreIds;

    private Integer releaseYear;
    private Boolean isCurrentlyShowing;
    private Boolean isUpcoming;

    public String getPosterUrl() {
        if (posterFileName == null || posterFileName.isEmpty()) {
            return null;
        }
        return "/api/movies/" + (id != null ? id : "temp") + "/poster";
    }
}