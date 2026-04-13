package ua.lviv.bas.cinema.mapper.cinema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallListResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.CinemaHallListProjection;

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

		hallWithSeats = CinemaHall.builder().id(1L).name("Hall A")
				.seats(new ArrayList<>(Arrays.asList(seat1, seat2, seat3))).build();

		hallWithoutSeats = CinemaHall.builder().id(2L).name("Hall B").seats(new ArrayList<>()).build();
	}

	@Test
	void toCinemaHallListResponse_MapsAllFields() {
		CinemaHallListResponse response = cinemaHallMapper.toCinemaHallListResponse(hallWithSeats);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Hall A");
		assertThat(response.capacity()).isEqualTo(3);
	}

	@Test
	void toCinemaHallListResponse_WhenSeatsNull_ReturnsZeroCapacity() {
		CinemaHall hallWithNullSeats = CinemaHall.builder().id(3L).name("Hall C").seats(null).build();

		CinemaHallListResponse response = cinemaHallMapper.toCinemaHallListResponse(hallWithNullSeats);

		assertThat(response.capacity()).isZero();
	}

	@Test
	void toCinemaHallListResponseList_MapsList() {
		List<CinemaHall> halls = Arrays.asList(hallWithSeats, hallWithoutSeats);
		List<CinemaHallListResponse> responses = cinemaHallMapper.toCinemaHallListResponseList(halls);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).id()).isEqualTo(1L);
		assertThat(responses.get(1).id()).isEqualTo(2L);
		assertThat(responses.get(1).capacity()).isZero();
	}

	@Test
	void toCinemaHallListResponseList_WhenInputEmpty_ReturnsEmpty() {
		List<CinemaHallListResponse> responses = cinemaHallMapper.toCinemaHallListResponseList(Collections.emptyList());
		assertThat(responses).isEmpty();
	}

	@Test
	void toCinemaHallListResponseList_WhenInputNull_ReturnsNull() {
		List<CinemaHallListResponse> responses = cinemaHallMapper.toCinemaHallListResponseList(null);
		assertThat(responses).isNull();
	}

	@Test
	void toCinemaHallListResponseFromProjection_ShouldMapAllFields() {
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
	void toCinemaHallListResponseFromProjection_WithNullSeatsCount_ShouldMapZero() {
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
	void toCinemaHallListResponse_WithNullEntity_ReturnsNull() {
		CinemaHallListResponse response = cinemaHallMapper.toCinemaHallListResponse((CinemaHall) null);
		assertThat(response).isNull();
	}

	@Test
	void toCinemaHallListResponse_WithNullProjection_ReturnsNull() {
		CinemaHallListResponse response = cinemaHallMapper.toCinemaHallListResponse((CinemaHallListProjection) null);
		assertThat(response).isNull();
	}
}