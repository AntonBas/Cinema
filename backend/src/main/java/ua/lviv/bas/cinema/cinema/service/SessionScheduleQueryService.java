package ua.lviv.bas.cinema.cinema.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.cinema.mapper.SessionMapper;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.cinema.repository.projection.SessionScheduleProjection;
import ua.lviv.bas.cinema.cinema.repository.specification.SessionSpecification;
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
                .map(session -> sessionMapper.toSessionScheduleResponse(projections.get(session.getId())))
                .toList());
    }
}
