package ua.lviv.bas.cinema.cinema.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.dto.session.response.SessionScheduleResponse;
import ua.lviv.bas.cinema.cinema.mapper.SessionMapper;
import ua.lviv.bas.cinema.cinema.repository.SessionRepository;
import ua.lviv.bas.cinema.cinema.repository.projection.SessionScheduleProjection;
import ua.lviv.bas.cinema.cinema.repository.specification.SessionSpecification;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SessionScheduleQueryServiceTest {

    @Mock
    private SessionRepository sessionRepository;
    @Mock
    private SessionMapper sessionMapper;
    @Mock
    private SessionSpecification sessionSpecification;
    @Mock
    private Specification<Session> specification;
    @Mock
    private SessionScheduleProjection projection;

    @InjectMocks
    private SessionScheduleQueryService sessionScheduleQueryService;

    private static final Long SESSION_ID = 1L;

    @BeforeEach
    void setUp() {
        when(sessionSpecification.forSchedule(any(), any(), any())).thenReturn(specification);
    }

    @Test
    void getScheduleWithoutAvailabilityWhenNoSessionsShouldReturnEmptyList() {
        when(sessionRepository.findAll(specification)).thenReturn(List.of());

        List<SessionScheduleResponse> result =
                sessionScheduleQueryService.getScheduleWithoutAvailability("term", LocalDate.now(), 1L);

        assertThat(result).isEmpty();
    }

    @Test
    void getScheduleWithoutAvailabilityShouldMapProjectionsToResponses() {
        Session session = Session.builder().id(SESSION_ID).build();
        SessionScheduleResponse response = new SessionScheduleResponse(SESSION_ID, null, null, null, null, null,
                null, null, null, null, null, null, null);

        when(sessionRepository.findAll(specification)).thenReturn(List.of(session));
        when(projection.getId()).thenReturn(SESSION_ID);
        when(sessionRepository.findScheduleProjectionsByIds(anyList())).thenReturn(List.of(projection));
        when(sessionMapper.toSessionScheduleResponse(projection)).thenReturn(response);

        List<SessionScheduleResponse> result =
                sessionScheduleQueryService.getScheduleWithoutAvailability(null, null, null);

        assertThat(result).containsExactly(response);
    }
}
