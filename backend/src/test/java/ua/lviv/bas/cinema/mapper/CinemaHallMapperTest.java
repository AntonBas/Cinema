package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.HallLayoutResponse;

public class CinemaHallMapperTest {

	private CinemaHallMapperImpl cinemaHallMapper;
	private CinemaHall hallWithSeats;
	private CinemaHall hallWithoutSeats;

	@BeforeEach
	void setUp() {
		cinemaHallMapper = new CinemaHallMapperImpl();

		Seat seat1 = Seat.builder().id(1L).row(1).number(1).build();
		Seat seat2 = Seat.builder().id(2L).row(1).number(2).build();
		Seat seat3 = Seat.builder().id(3L).row(2).number(1).build();

		hallWithSeats = CinemaHall.builder().id(1L).name("Hall A").seats(Arrays.asList(seat1, seat2, seat3)).build();

		hallWithoutSeats = CinemaHall.builder().id(2L).name("Hall B").seats(Collections.emptyList()).build();
	}

	@Test
	void toCinemaHallResponse_MapsAllFields() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(hallWithSeats);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Hall A");
		assertThat(response.capacity()).isEqualTo(3);
	}

	@Test
	void toCinemaHallResponse_WhenSeatsNull_ReturnsZeroCapacity() {
		hallWithSeats.setSeats(null);
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(hallWithSeats);

		assertThat(response.capacity()).isZero();
	}

	@Test
	void toCinemaHallResponseList_MapsList() {
		List<CinemaHall> halls = Arrays.asList(hallWithSeats, hallWithoutSeats);
		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(halls);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).id()).isEqualTo(1L);
		assertThat(responses.get(1).id()).isEqualTo(2L);
		assertThat(responses.get(1).capacity()).isZero();
	}

	@Test
	void toCinemaHallResponseList_WhenInputEmpty_ReturnsEmpty() {
		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(Collections.emptyList());
		assertThat(responses).isEmpty();
	}

	@Test
	void toCinemaHallResponseList_WhenInputNull_ReturnsNull() {
		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(null);
		assertThat(responses).isNull();
	}

	@Test
	void toHallLayoutResponse_MapsAllFields() {
		HallLayoutResponse response = cinemaHallMapper.toHallLayoutResponse(hallWithSeats);

		assertThat(response.hallId()).isEqualTo(1L);
		assertThat(response.hallName()).isEqualTo("Hall A");
		assertThat(response.totalSeats()).isEqualTo(3);
	}

	@Test
	void toHallLayoutResponse_WhenInputNull_ReturnsNull() {
		HallLayoutResponse response = cinemaHallMapper.toHallLayoutResponse(null);
		assertThat(response).isNull();
	}
}