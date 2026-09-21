package ua.lviv.bas.cinema.cinema.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.Session;
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.request.HallLayoutRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.request.SeatLayoutItemRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.hall.CinemaHallHasSessionsException;
import ua.lviv.bas.cinema.exception.domain.hall.DuplicateSeatPositionException;
import ua.lviv.bas.cinema.exception.domain.hall.SeatHasTicketsException;
import ua.lviv.bas.cinema.cinema.mapper.CinemaHallMapper;
import ua.lviv.bas.cinema.cinema.repository.CinemaHallRepository;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.cinema.repository.projection.CinemaHallListProjection;
import ua.lviv.bas.cinema.audit.service.AuditService;

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
        CinemaHallRequest request = new CinemaHallRequest(HALL_NAME);
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 0);

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
        CinemaHallRequest request = new CinemaHallRequest(HALL_NAME);

        when(hallRepository.existsByName(HALL_NAME)).thenReturn(true);

        assertThatThrownBy(() -> cinemaHallService.createHall(request)).isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    void getHallShouldReturnHall() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 50);

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(hallMapper.toCinemaHallResponse(hall)).thenReturn(response);

        CinemaHallResponse result = cinemaHallService.getHall(HALL_ID);

        assertThat(result.id()).isEqualTo(HALL_ID);
    }

    @Test
    void getHallShouldThrowExceptionWhenNotFound() {
        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cinemaHallService.getHall(HALL_ID)).isInstanceOf(EntityNotFoundException.class);
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
        assertThat(result.getFirst().id()).isEqualTo(HALL_ID);
    }

    @Test
    void updateHallShouldRenameHall() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name("Old Name").build();
        hall.setSessions(List.of());

        CinemaHallRequest request = new CinemaHallRequest("New Name");
        CinemaHallResponse response = new CinemaHallResponse(HALL_ID, "New Name", 0);

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(hallRepository.existsByNameAndIdNot("New Name", HALL_ID)).thenReturn(false);
        when(hallRepository.save(hall)).thenReturn(hall);
        when(hallMapper.toCinemaHallResponse(hall)).thenReturn(response);

        CinemaHallResponse result = cinemaHallService.updateHall(HALL_ID, request);

        assertThat(result.name()).isEqualTo("New Name");
    }

    @Test
    void updateHallShouldThrowExceptionWhenNameExists() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name("Old Name").build();
        hall.setSessions(List.of());
        CinemaHallRequest request = new CinemaHallRequest("Existing Name");

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(hallRepository.existsByNameAndIdNot("Existing Name", HALL_ID)).thenReturn(true);

        assertThatThrownBy(() -> cinemaHallService.updateHall(HALL_ID, request))
                .isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    void updateHallShouldThrowExceptionWhenHallHasFutureSessions() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        Session session = Session.builder().startTime(LocalDateTime.now().plusDays(1)).build();
        hall.setSessions(List.of(session));

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

        assertThatThrownBy(() -> cinemaHallService.updateHall(HALL_ID, new CinemaHallRequest(HALL_NAME)))
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

        assertThatThrownBy(() -> cinemaHallService.deleteHall(HALL_ID)).isInstanceOf(EntityNotFoundException.class);
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
                seats.add(Seat.builder().row(row).number(num).seatType(SeatType.STANDARD).hall(hall)
                        .x((num - 1) * 60).y((row - 1) * 70).build());
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

    @Test
    void updateLayoutShouldAddNewSeatAndRemoveMissingSeat() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        hall.setSessions(List.of());
        Seat existingSeat = Seat.builder().id(10L).row(1).number(1).seatType(SeatType.STANDARD).x(0).y(0).hall(hall)
                .build();
        hall.setSeats(new ArrayList<>(List.of(existingSeat)));

        HallLayoutRequest request = new HallLayoutRequest(
                List.of(new SeatLayoutItemRequest(null, 1, 2, SeatType.VIP, 60, 0, true)));
        HallLayoutResponse response = new HallLayoutResponse(HALL_ID, HALL_NAME, 1, 1, 1, List.of());

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(seatRepository.findTicketedSeatIds(List.of(10L))).thenReturn(List.of());
        when(hallRepository.save(hall)).thenReturn(hall);
        when(hallMapper.toHallLayoutResponse(hall)).thenReturn(response);

        HallLayoutResponse result = cinemaHallService.updateLayout(HALL_ID, request);

        assertThat(result).isEqualTo(response);
        assertThat(hall.getSeats()).hasSize(1);
        assertThat(hall.getSeats().getFirst().getNumber()).isEqualTo(2);
        assertThat(hall.getSeats().getFirst().getSeatType()).isEqualTo(SeatType.VIP);
    }

    @Test
    void updateLayoutShouldUpdateExistingSeatInPlace() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        hall.setSessions(List.of());
        Seat existingSeat = Seat.builder().id(10L).row(1).number(1).seatType(SeatType.STANDARD).x(0).y(0).hall(hall)
                .build();
        hall.setSeats(new ArrayList<>(List.of(existingSeat)));

        HallLayoutRequest request = new HallLayoutRequest(
                List.of(new SeatLayoutItemRequest(10L, 1, 1, SeatType.VIP, 120, 90, false)));
        HallLayoutResponse response = new HallLayoutResponse(HALL_ID, HALL_NAME, 1, 1, 1, List.of());

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(hallRepository.save(hall)).thenReturn(hall);
        when(hallMapper.toHallLayoutResponse(hall)).thenReturn(response);

        cinemaHallService.updateLayout(HALL_ID, request);

        assertThat(hall.getSeats()).hasSize(1);
        Seat updated = hall.getSeats().getFirst();
        assertThat(updated.getSeatType()).isEqualTo(SeatType.VIP);
        assertThat(updated.getX()).isEqualTo(120);
        assertThat(updated.getY()).isEqualTo(90);
        assertThat(updated.isActive()).isFalse();
        verify(seatRepository, never()).findTicketedSeatIds(any());
    }

    @Test
    void updateLayoutShouldThrowWhenRemovingSeatWithTickets() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        hall.setSessions(List.of());
        Seat existingSeat = Seat.builder().id(10L).row(1).number(1).seatType(SeatType.STANDARD).x(0).y(0).hall(hall)
                .build();
        hall.setSeats(new ArrayList<>(List.of(existingSeat)));

        HallLayoutRequest request = new HallLayoutRequest(List.of());

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(seatRepository.findTicketedSeatIds(List.of(10L))).thenReturn(List.of(10L));

        assertThatThrownBy(() -> cinemaHallService.updateLayout(HALL_ID, request))
                .isInstanceOf(SeatHasTicketsException.class);
        verify(hallRepository, never()).save(any());
    }

    @Test
    void updateLayoutShouldThrowWhenRepositioningSeatWithTickets() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        hall.setSessions(List.of());
        Seat existingSeat = Seat.builder().id(10L).row(1).number(1).seatType(SeatType.STANDARD).x(0).y(0).hall(hall)
                .build();
        hall.setSeats(new ArrayList<>(List.of(existingSeat)));

        HallLayoutRequest request = new HallLayoutRequest(
                List.of(new SeatLayoutItemRequest(10L, 2, 1, SeatType.STANDARD, 0, 70, true)));

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));
        when(seatRepository.findTicketedSeatIds(List.of(10L))).thenReturn(List.of(10L));

        assertThatThrownBy(() -> cinemaHallService.updateLayout(HALL_ID, request))
                .isInstanceOf(SeatHasTicketsException.class);
        verify(hallRepository, never()).save(any());
    }

    @Test
    void updateLayoutShouldThrowOnDuplicatePositions() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        hall.setSessions(List.of());
        hall.setSeats(new ArrayList<>());

        HallLayoutRequest request = new HallLayoutRequest(List.of(
                new SeatLayoutItemRequest(null, 1, 1, SeatType.STANDARD, 0, 0, true),
                new SeatLayoutItemRequest(null, 1, 1, SeatType.VIP, 60, 0, true)));

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

        assertThatThrownBy(() -> cinemaHallService.updateLayout(HALL_ID, request))
                .isInstanceOf(DuplicateSeatPositionException.class);
        verify(hallRepository, never()).save(any());
    }

    @Test
    void updateLayoutShouldThrowWhenHallHasFutureSessions() {
        CinemaHall hall = CinemaHall.builder().id(HALL_ID).name(HALL_NAME).build();
        Session session = Session.builder().startTime(LocalDateTime.now().plusDays(1)).build();
        hall.setSessions(List.of(session));

        when(hallRepository.findByIdWithSeats(HALL_ID)).thenReturn(Optional.of(hall));

        assertThatThrownBy(() -> cinemaHallService.updateLayout(HALL_ID, new HallLayoutRequest(List.of())))
                .isInstanceOf(CinemaHallHasSessionsException.class);
    }
}
