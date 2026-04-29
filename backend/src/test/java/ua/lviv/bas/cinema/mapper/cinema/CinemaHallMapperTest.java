package ua.lviv.bas.cinema.mapper.cinema;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.CinemaHallListProjection;

import java.util.ArrayList;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = CinemaHallMapperImpl.class)
public class CinemaHallMapperTest {

    @Autowired
    private CinemaHallMapper cinemaHallMapper;

    @MockitoBean
    private SeatMapper seatMapper;

    private CinemaHall hallWithSeats;

    @BeforeEach
    void setUp() {
        Seat seat1 = Seat.builder().id(1L).row(1).number(1).build();
        Seat seat2 = Seat.builder().id(2L).row(1).number(2).build();
        Seat seat3 = Seat.builder().id(3L).row(2).number(1).build();

        hallWithSeats = CinemaHall.builder().id(1L).name("Hall A")
                .seats(new ArrayList<>(Arrays.asList(seat1, seat2, seat3))).build();
    }

    @Test
    void toCinemaHallListResponseMapsAllFields() {
        CinemaHallListResponse response = cinemaHallMapper.toCinemaHallListResponse(hallWithSeats);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Hall A");
        assertThat(response.capacity()).isEqualTo(3);
    }

    @Test
    void toCinemaHallListResponseWhenSeatsNullReturnsZeroCapacity() {
        CinemaHall hallWithNullSeats = CinemaHall.builder().id(3L).name("Hall C").seats(null).build();

        CinemaHallListResponse response = cinemaHallMapper.toCinemaHallListResponse(hallWithNullSeats);

        assertThat(response.capacity()).isZero();
    }

    @Test
    void toCinemaHallListResponseFromProjectionShouldMapAllFields() {
        CinemaHallListProjection projection = new CinemaHallListProjection() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getName() {
                return "Hall A";
            }

            @Override
            public Long getSeatsCount() {
                return 5L;
            }
        };

        CinemaHallListResponse response = cinemaHallMapper.toCinemaHallListResponse(projection);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Hall A");
        assertThat(response.capacity()).isEqualTo(5);
    }

    @Test
    void toCinemaHallListResponseFromProjectionWithNullSeatsCountShouldMapZero() {
        CinemaHallListProjection projection = new CinemaHallListProjection() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public String getName() {
                return "Hall A";
            }

            @Override
            public Long getSeatsCount() {
                return null;
            }
        };

        CinemaHallListResponse response = cinemaHallMapper.toCinemaHallListResponse(projection);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Hall A");
        assertThat(response.capacity()).isZero();
    }

    @Test
    void toCinemaHallListResponseWithNullEntityReturnsNull() {
        CinemaHallListResponse response = cinemaHallMapper.toCinemaHallListResponse((CinemaHall) null);
        assertThat(response).isNull();
    }

    @Test
    void toCinemaHallListResponseWithNullProjectionReturnsNull() {
        CinemaHallListResponse response = cinemaHallMapper.toCinemaHallListResponse((CinemaHallListProjection) null);
        assertThat(response).isNull();
    }
}