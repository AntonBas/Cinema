package ua.lviv.bas.cinema.cinema.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.cinema.dto.hall.response.SeatResponse;
import ua.lviv.bas.cinema.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;
import ua.lviv.bas.cinema.exception.core.EntityNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SeatServiceTest {

    @Mock
    private SeatRepository seatRepository;

    @Mock
    private SeatMapper seatMapper;

    @InjectMocks
    private SeatService seatService;

    private final Long HALL_ID = 100L;
    private final Long OTHER_HALL_ID = 200L;
    private final Long SEAT_ID = 1L;
    private Seat seat;
    private SeatResponse response;

    @BeforeEach
    void setUp() {
        var hall = new CinemaHall();
        hall.setId(HALL_ID);

        seat = new Seat();
        seat.setId(SEAT_ID);
        seat.setHall(hall);
        int ROW = 1;
        seat.setRow(ROW);
        int NUMBER = 5;
        seat.setNumber(NUMBER);
        seat.setSeatType(SeatType.STANDARD);
        seat.setActive(true);

        response = new SeatResponse(SEAT_ID, ROW, NUMBER, SeatType.STANDARD, true);
    }

    @Test
    void updateSeatType_ShouldUpdateType() {
        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
        when(seatRepository.save(seat)).thenReturn(seat);
        when(seatMapper.toSeatResponse(seat)).thenReturn(response);

        SeatResponse result = seatService.updateSeatType(HALL_ID, SEAT_ID, SeatType.VIP);

        assertThat(result).isEqualTo(response);
        assertThat(seat.getSeatType()).isEqualTo(SeatType.VIP);
        verify(seatRepository).save(seat);
    }

    @Test
    void updateSeatType_WhenSeatBelongsToDifferentHall_ShouldThrowNotFound() {
        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> seatService.updateSeatType(OTHER_HALL_ID, SEAT_ID, SeatType.VIP))
                .isInstanceOf(EntityNotFoundException.class);

        verify(seatRepository, never()).save(seat);
    }

    @Test
    void setSeatActiveStatus_ShouldUpdateStatus() {
        seat.setActive(false);

        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
        when(seatRepository.save(seat)).thenReturn(seat);
        when(seatMapper.toSeatResponse(seat)).thenReturn(response);

        SeatResponse result = seatService.setSeatActiveStatus(HALL_ID, SEAT_ID, true);

        assertThat(result).isEqualTo(response);
        assertThat(seat.isActive()).isTrue();
        verify(seatRepository).save(seat);
    }

    @Test
    void setSeatActiveStatus_WhenSeatBelongsToDifferentHall_ShouldThrowNotFound() {
        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));

        assertThatThrownBy(() -> seatService.setSeatActiveStatus(OTHER_HALL_ID, SEAT_ID, true))
                .isInstanceOf(EntityNotFoundException.class);

        verify(seatRepository, never()).save(seat);
    }
}