package ua.lviv.bas.cinema.cinema.controller.admin;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.cinema.dto.hall.request.CinemaHallRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.request.HallLayoutRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.request.SeatLayoutItemRequest;
import ua.lviv.bas.cinema.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.cinema.dto.hall.response.HallLayoutResponse;
import ua.lviv.bas.cinema.exception.core.DuplicateEntityException;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;
import ua.lviv.bas.cinema.exception.domain.hall.SeatHasTicketsException;
import ua.lviv.bas.cinema.cinema.service.CinemaHallService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminCinemaHallControllerTest {

    @Mock
    private CinemaHallService cinemaHallService;

    @InjectMocks
    private AdminCinemaHallController controller;

    private final Long HALL_ID = 1L;
    private final String HALL_NAME = "Test Hall";

    @Test
    void createHallShouldReturnCreatedHall() {
        CinemaHallRequest request = new CinemaHallRequest(HALL_NAME);
        CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 0);

        when(cinemaHallService.createHall(any(CinemaHallRequest.class))).thenReturn(response);

        CinemaHallResponse result = controller.createHall(request);

        assertThat(result).isEqualTo(response);
        verify(cinemaHallService).createHall(request);
    }

    @Test
    void createHallShouldThrowExceptionWhenDuplicateName() {
        CinemaHallRequest request = new CinemaHallRequest("Existing Hall");

        when(cinemaHallService.createHall(any(CinemaHallRequest.class)))
                .thenThrow(new DuplicateEntityException("CinemaHall", "Existing Hall"));

        assertThatThrownBy(() -> controller.createHall(request)).isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    void getHallsShouldReturnList() {
        List<CinemaHallListResponse> response = List.of(new CinemaHallListResponse(1L, "Hall A", 50),
                new CinemaHallListResponse(2L, "Hall B", 30));

        when(cinemaHallService.getHalls()).thenReturn(response);

        List<CinemaHallListResponse> result = controller.getHalls();

        assertThat(result).hasSize(2);
        verify(cinemaHallService).getHalls();
    }

    @Test
    void getHallShouldReturnHall() {
        CinemaHallResponse response = new CinemaHallResponse(HALL_ID, HALL_NAME, 50);

        when(cinemaHallService.getHall(HALL_ID)).thenReturn(response);

        CinemaHallResponse result = controller.getHall(HALL_ID);

        assertThat(result).isEqualTo(response);
        verify(cinemaHallService).getHall(HALL_ID);
    }

    @Test
    void getHallShouldThrowExceptionWhenNotFound() {
        when(cinemaHallService.getHall(999L)).thenThrow(new EntityNotFoundException("Cinema hall", 999L));

        assertThatThrownBy(() -> controller.getHall(999L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateHallShouldReturnRenamedHall() {
        CinemaHallRequest request = new CinemaHallRequest("Updated Hall");
        CinemaHallResponse response = new CinemaHallResponse(HALL_ID, "Updated Hall", 72);

        when(cinemaHallService.updateHall(eq(HALL_ID), any(CinemaHallRequest.class))).thenReturn(response);

        CinemaHallResponse result = controller.updateHall(HALL_ID, request);

        assertThat(result).isEqualTo(response);
        verify(cinemaHallService).updateHall(HALL_ID, request);
    }

    @Test
    void updateHallShouldThrowExceptionWhenNotFound() {
        CinemaHallRequest request = new CinemaHallRequest("Updated Hall");

        when(cinemaHallService.updateHall(eq(999L), any(CinemaHallRequest.class)))
                .thenThrow(new EntityNotFoundException("Cinema hall", 999L));

        assertThatThrownBy(() -> controller.updateHall(999L, request)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteHallShouldCallService() {
        controller.deleteHall(HALL_ID);

        verify(cinemaHallService).deleteHall(HALL_ID);
    }

    @Test
    void deleteHallShouldThrowExceptionWhenNotFound() {
        doThrow(new EntityNotFoundException("Cinema hall", 999L)).when(cinemaHallService).deleteHall(999L);

        assertThatThrownBy(() -> controller.deleteHall(999L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getHallLayoutShouldReturnLayout() {
        HallLayoutResponse response = new HallLayoutResponse(HALL_ID, HALL_NAME, 5, 10, 50, List.of());

        when(cinemaHallService.getHallLayout(HALL_ID)).thenReturn(response);

        HallLayoutResponse result = controller.getHallLayout(HALL_ID);

        assertThat(result).isEqualTo(response);
        verify(cinemaHallService).getHallLayout(HALL_ID);
    }

    @Test
    void getHallLayoutShouldThrowExceptionWhenNotFound() {
        when(cinemaHallService.getHallLayout(999L)).thenThrow(new EntityNotFoundException("Cinema hall", 999L));

        assertThatThrownBy(() -> controller.getHallLayout(999L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void updateLayoutShouldReturnUpdatedLayout() {
        HallLayoutRequest request = new HallLayoutRequest(
                List.of(new SeatLayoutItemRequest(null, 1, 1, SeatType.STANDARD, 0, 0, true)));
        HallLayoutResponse response = new HallLayoutResponse(HALL_ID, HALL_NAME, 1, 1, 1, List.of());

        when(cinemaHallService.updateLayout(eq(HALL_ID), any(HallLayoutRequest.class))).thenReturn(response);

        HallLayoutResponse result = controller.updateLayout(HALL_ID, request);

        assertThat(result).isEqualTo(response);
        verify(cinemaHallService).updateLayout(HALL_ID, request);
    }

    @Test
    void updateLayoutShouldThrowExceptionWhenSeatHasTickets() {
        HallLayoutRequest request = new HallLayoutRequest(List.of());

        when(cinemaHallService.updateLayout(eq(HALL_ID), any(HallLayoutRequest.class)))
                .thenThrow(new SeatHasTicketsException(List.of(42L)));

        assertThatThrownBy(() -> controller.updateLayout(HALL_ID, request))
                .isInstanceOf(SeatHasTicketsException.class);
    }
}
