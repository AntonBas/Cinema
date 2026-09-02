package ua.lviv.bas.cinema.cinema.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.cinema.domain.Seat;
import ua.lviv.bas.cinema.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.cinema.dto.hall.response.SeatResponse;
import ua.lviv.bas.cinema.cinema.mapper.SeatMapper;
import ua.lviv.bas.cinema.cinema.repository.SeatRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    private final Long SEAT_ID = 1L;
    private Seat seat;
    private SeatResponse response;

    @BeforeEach
    void setUp() {
        seat = new Seat();
        seat.setId(SEAT_ID);
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

        SeatResponse result = seatService.updateSeatType(SEAT_ID, SeatType.VIP);

        assertThat(result).isEqualTo(response);
        assertThat(seat.getSeatType()).isEqualTo(SeatType.VIP);
        verify(seatRepository).save(seat);
    }

    @Test
    void setSeatActiveStatus_ShouldUpdateStatus() {
        seat.setActive(false);

        when(seatRepository.findById(SEAT_ID)).thenReturn(Optional.of(seat));
        when(seatRepository.save(seat)).thenReturn(seat);
        when(seatMapper.toSeatResponse(seat)).thenReturn(response);

        SeatResponse result = seatService.setSeatActiveStatus(SEAT_ID, true);

        assertThat(result).isEqualTo(response);
        assertThat(seat.isActive()).isTrue();
        verify(seatRepository).save(seat);
    }
}