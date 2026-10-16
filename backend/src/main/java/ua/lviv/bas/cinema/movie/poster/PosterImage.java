package ua.lviv.bas.cinema.movie.poster;

import org.springframework.http.MediaType;

public record PosterImage(byte[] data, MediaType mediaType) {
}
