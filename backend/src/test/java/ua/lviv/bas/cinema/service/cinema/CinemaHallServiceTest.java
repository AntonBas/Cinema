package ua.lviv.bas.cinema.service.cinema;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.domain.cinema.Session;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.domain.hall.CinemaHallHasSessionsException;
import ua.lviv.bas.cinema.exception.domain.hall.CinemaHallNotFoundException;
import ua.lviv.bas.cinema.mapper.cinema.CinemaHallMapper;
import ua.lviv.bas.cinema.repository.cinema.CinemaHallRepository;
import ua.lviv.bas.cinema.repository.cinema.SeatRepository;
import ua.lviv.bas.cinema.repository.cinema.projection.CinemaHallListProjection;
import ua.lviv.bas.cinema.service.integration.audit.AuditService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CinemaHallServiceTest {

    @Mock
    private CinemaHallRepository hallRepository;
    @Mock
    private SeatRepository seatRepository;
    @Mock
    private CinemaHallMapper hallMapper;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private CinemaHallService cinemaHallService;

    private final Long HALL_ID = 1L;
    private final String HALL_NAME = "Hall A";

    @Test
    void createHallShouldSaveNewHall() {
        CinemaHallRequest request = new CinemaHallRequest(HALL_NAME, 5, 10, SeatType.STANDARD, null);
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 5, 10, SeatType.STANDARD, null, 50);

        when(hallRepository.existsByName(HALL_NAME)).thenReturn(false);
        when(hallRepository.save(any(CinemaHall.class))).thenReturn(hall);
        when(hallMapper.toCinemaHallResponse(hall)).thenReturn(response);

        CinemaHallResponse result = cinemaHallService.createHall(request);

        assertThat(result.id()).isEqualTo(HALL_ID);
        assertThat(result.name()).isEqualTo(HALL_NAME);
        verify(auditService).logChange(any(), any(), any(), any(), any(), any());
    }

    @Test
    void createHallShouldThrowExceptionWhenNameExists() {
        CinemaHallRequest request = new CinemaHallRequest(HALL_NAME, null, null, null, null);

        when(hallRepository.existsByName(HALL_NAME)).thenReturn(true);

        assertThatThrownBy(() -> cinemaHallService.createHall(request)).isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    void getHallShouldReturnHall() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 5, 10, SeatType.STANDARD, null, 50);

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(hallMapper.toCinemaHallResponse(hall)).thenReturn(response);

        CinemaHallResponse result = cinemaHallService.getHall(HALL_ID);

        assertThat(result.id()).isEqualTo(HALL_ID);
    }

    @Test
    void getHallShouldThrowExceptionWhenNotFound() {
        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaHallService.getHall(HALL_ID)).isInstanceOf(CinemaHallNotFoundException.class);
    }

    @Test
    void getHallsShouldReturnList() {
        CinemaHallListProjection projection = new CinemaHallListProjection() {
            @Override
            public Long getId() {
                return HALL_ID;
            }

            @Override
            public String getName() {
                return HALL_NAME;
            }

            @Override
            public Long getSeatsCount() {
                return 50L;
            }
        };

        CinemaHallListResponse response = new CinemaHallListResponse(HALL_ID, HALL_NAME, 50);

        when(hallRepository.findAllProjected()).thenReturn(List.of(projection));
        when(hallMapper.toCinemaHallListResponse(projection)).thenReturn(response);

        List<CinemaHallListResponse> result = cinemaHallService.getHalls();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(HALL_ID);
    }

    @Test
    void updateHallShouldUpdateNameOnly() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name("Old Name").build();
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= 5; row++) {
            for (int num = 1; num <= 10; num++) {
                seats.add(Seat.builder().row(row).number(num).seatType(SeatType.STANDARD).hall(hall).build());
            }
        }
        hall.setSeats(seats);
        hall.setSessions(List.of());

        CinemaHallRequest request = new CinemaHallRequest("New Name", 5, 10, SeatType.STANDARD, null);
        CinemaHallResponse response = new CinemaHallResponse(HALL_ID, "New Name", 5, 10, SeatType.STANDARD, null, 50);

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(hallRepository.existsByNameAndIdNot("New Name", HALL_ID)).thenReturn(false);
        when(hallRepository.save(hall)).thenReturn(hall);
        when(hallMapper.toCinemaHallResponse(hall)).thenReturn(response);

        CinemaHallResponse result = cinemaHallService.updateHall(HALL_ID, request);

        assertThat(result.name()).isEqualTo("New Name");
        verify(seatRepository, never()).deleteByHallId(any());
    }

    @Test
    void updateHallShouldUpdateLayoutWhenChanged() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        hall.setSeats(new ArrayList<>());
        hall.setSessions(List.of());

        CinemaHallRequest request = new CinemaHallRequest(HALL_NAME, 5, 10, SeatType.STANDARD, null);
        CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 5, 10, SeatType.STANDARD, null, 50);

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(hallRepository.save(hall)).thenReturn(hall);
        when(hallMapper.toCinemaHallResponse(hall)).thenReturn(response);
        when(seatRepository.hasTicketsForHall(HALL_ID)).thenReturn(false);

        CinemaHallResponse result = cinemaHallService.updateHall(HALL_ID, request);

        assertThat(result.name()).isEqualTo(HALL_NAME);
        verify(hallRepository).flush();
    }

    @Test
    void updateHallShouldThrowExceptionWhenNameExists() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name("Old Name").build();
        hall.setSessions(List.of());
        CinemaHallRequest request = new CinemaHallRequest("Existing Name", null, null, null, null);

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(hallRepository.existsByNameAndIdNot("Existing Name", HALL_ID)).thenReturn(true);

        assertThatThrownBy(() -> cinemaHallService.updateHall(HALL_ID, request))
                .isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    void updateHallShouldThrowExceptionWhenHallHasFutureSessions() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).build();
        Session session = Session.builder().startTime(LocalDateTime.now().plusDays(1)).build();
        hall.setSessions(List.of(session));

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

        assertThatThrownBy(
                () -> cinemaHallService.updateHall(HALL_ID, new CinemaHallRequest(null, null, null, null, null)))
                .isInstanceOf(CinemaHallHasSessionsException.class);
    }

    @Test
    void deleteHallShouldDeleteHall() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        hall.setSessions(List.of());

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

        cinemaHallService.deleteHall(HALL_ID);

        verify(hallRepository).delete(hall);
    }

    @Test
    void deleteHallShouldThrowExceptionWhenNotFound() {
        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaHallService.deleteHall(HALL_ID)).isInstanceOf(CinemaHallNotFoundException.class);
    }

    @Test
    void deleteHallShouldThrowExceptionWhenHallHasFutureSessions() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        Session session = Session.builder().startTime(LocalDateTime.now().plusDays(1)).build();
        hall.setSessions(List.of(session));

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

        assertThatThrownBy(() -> cinemaHallService.deleteHall(HALL_ID))
                .isInstanceOf(CinemaHallHasSessionsException.class);
    }

    @Test
    void getHallLayoutShouldReturnLayout() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        List<Seat> seats = new ArrayList<>();
        for (int row = 1; row <= 5; row++) {
            for (int num = 1; num <= 10; num++) {
                seats.add(Seat.builder().row(row).number(num).seatType(SeatType.STANDARD).hall(hall).build());
            }
        }
        hall.setSeats(seats);
        HallLayoutResponse response = new HallLayoutResponse(HALL_ID, HALL_NAME, 5, 10, 50, List.of());

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(hallMapper.toHallLayoutResponse(hall)).thenReturn(response);

        HallLayoutResponse result = cinemaHallService.getHallLayout(HALL_ID);

        assertThat(result.hallId()).isEqualTo(HALL_ID);
        assertThat(result.hallName()).isEqualTo(HALL_NAME);
    }

    @Test
    void getHallEntityShouldReturnHall() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).build();

        when(hallRepository.findById(HALL_ID)).thenReturn(Optional.of(hall));

        CinemaHall result = cinemaHallService.getHallEntity(HALL_ID);

        assertThat(result.getId()).isEqualTo(HALL_ID);
    }
}