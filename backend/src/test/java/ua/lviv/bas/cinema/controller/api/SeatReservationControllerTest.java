package ua.lviv.bas.cinema.controller.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ua.lviv.bas.cinema.config.security.CustomUserDetails;
import ua.lviv.bas.cinema.domain.cinema.enums.SeatType;
import ua.lviv.bas.cinema.domain.user.User;
import ua.lviv.bas.cinema.dto.booking.response.SeatReservationResponse;
import ua.lviv.bas.cinema.service.booking.SeatReservationService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SeatReservationControllerTest {

    @Mock
    private SeatReservationService seatReservationService;

    @Mock
    private CustomUserDetails customUserDetails;

    @InjectMocks
    private SeatReservationController seatReservationController;

    private SeatReservationResponse createSeatAvailabilityResponse(Long sessionId) {
        SeatReservationResponse.TicketPriceInfo ticketPriceInfo = new SeatReservationResponse.TicketPriceInfo(1L,
                "Adult", new BigDecimal("250.00"), null, null, false, null);

        SeatReservationResponse.SeatInfo seat1 = new SeatReservationResponse.SeatInfo(1L, 1, 1, SeatType.STANDARD, true,
                false, true, List.of(ticketPriceInfo));

        return new SeatReservationResponse(sessionId, "Inception", new BigDecimal("200.00"), "Hall A", 75,
                List.of(seat1));
    }

    @Test
    void getAvailabilityShouldReturnAvailabilitySuccessfully() {
        Long sessionId = 1L;
        SeatReservationResponse availabilityResponse = createSeatAvailabilityResponse(sessionId);

        when(seatReservationService.getAvailability(sessionId)).thenReturn(availabilityResponse);

        SeatReservationResponse response = seatReservationController.getAvailability(sessionId);

        assertThat(response).isNotNull();
        assertThat(response.sessionId()).isEqualTo(sessionId);
        assertThat(response.movieTitle()).isEqualTo("Inception");
        assertThat(response.availableSeats()).isEqualTo(75);
    }

    @Test
    void holdShouldCallServiceSuccessfully() {
        Long sessionId = 1L;
        Long seatId = 10L;
        Long userId = 100L;
        User user = new User();
        user.setId(userId);

        when(customUserDetails.getUser()).thenReturn(user);
        when(customUserDetails.getUserId()).thenReturn(userId);
        doNothing().when(seatReservationService).hold(sessionId, seatId, user);

        seatReservationController.hold(sessionId, seatId, customUserDetails);

        verify(seatReservationService).hold(sessionId, seatId, user);
    }

    @Test
    void cancelShouldCallServiceSuccessfully() {
        Long sessionId = 1L;
        Long seatId = 10L;
        Long userId = 100L;
        User user = new User();
        user.setId(userId);

        when(customUserDetails.getUser()).thenReturn(user);
        when(customUserDetails.getUserId()).thenReturn(userId);
        doNothing().when(seatReservationService).cancel(sessionId, seatId, user);

        seatReservationController.cancel(sessionId, seatId, customUserDetails);

        verify(seatReservationService).cancel(sessionId, seatId, user);
    }
}