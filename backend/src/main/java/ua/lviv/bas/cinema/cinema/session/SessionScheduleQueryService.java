package ua.lviv.bas.cinema.cinema.session;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.cinema.session.Session;
import ua.lviv.bas.cinema.cinema.session.dto.SessionScheduleResponse;
import ua.lviv.bas.cinema.cinema.session.SessionMapper;
import ua.lviv.bas.cinema.cinema.session.SessionRepository;
import ua.lviv.bas.cinema.cinema.session.SessionScheduleProjection;
import ua.lviv.bas.cinema.movie.movie.PosterUrlMapper;
import ua.lviv.bas.cinema.cinema.session.SessionSpecification;
import ua.lviv.bas.cinema.common.CacheableList;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class SessionScheduleQueryService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionSpecification sessionSpecification;
    private final PosterUrlMapper posterUrlMapper;

    @Cacheable(value = "sessions", key = "'schedule:' + #searchTerm + ':' + #date + ':' + #movieId")
    List<SessionScheduleResponse> getScheduleWithoutAvailability(String searchTerm, LocalDate date, Long movieId) {
        Specification<Session> spec = sessionSpecification.forSchedule(searchTerm, date, movieId);
        var sessions = sessionRepository.findAll(spec);

        if (sessions.isEmpty()) {
            return new CacheableList<>(List.of());
        }

        var sessionIds = sessions.stream().map(Session::getId).toList();
        var projections = sessionRepository.findScheduleProjectionsByIds(sessionIds)
                .stream()
                .collect(Collectors.toMap(SessionScheduleProjection::getId, p -> p));

        return new CacheableList<>(sessions.stream()
                .map(session -> toScheduleResponse(projections.get(session.getId())))
                .toList());
    }

    private SessionScheduleResponse toScheduleResponse(SessionScheduleProjection projection) {
        return sessionMapper.toSessionScheduleResponse(projection).withMoviePosterUrl(
                posterUrlMapper.toPosterUrl(projection.getMovieId(), projection.getMoviePosterFileName()));
    }
}
