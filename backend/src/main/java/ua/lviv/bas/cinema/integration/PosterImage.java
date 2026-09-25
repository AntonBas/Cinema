package ua.lviv.bas.cinema.integration;

import org.springframework.http.MediaType;

public record PosterImage(byte[] data, MediaType mediaType) {
}
