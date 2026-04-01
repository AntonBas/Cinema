package ua.lviv.bas.cinema.mapper.cinema;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ua.lviv.bas.cinema.domain.cinema.CinemaHall;
import ua.lviv.bas.cinema.domain.cinema.Seat;
import ua.lviv.bas.cinema.dto.hall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.repository.cinema.projection.CinemaHallProjection;

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
	void toCinemaHallResponseFromProjection_ShouldMapAllFields() {
		CinemaHallProjection projection = new CinemaHallProjection() {
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

		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponseFromProjection(projection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Hall A");
		assertThat(response.capacity()).isEqualTo(5);
	}

	@Test
	void toCinemaHallResponseFromProjection_WithNullSeatsCount_ShouldMapZero() {
		CinemaHallProjection projection = new CinemaHallProjection() {
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

		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponseFromProjection(projection);

		assertThat(response.id()).isEqualTo(1L);
		assertThat(response.name()).isEqualTo("Hall A");
		assertThat(response.capacity()).isZero();
	}

	@Test
	void toCinemaHallResponseListFromProjection_ShouldMapList() {
		CinemaHallProjection projection1 = new CinemaHallProjection() {
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

		CinemaHallProjection projection2 = new CinemaHallProjection() {
			@Override
			public Long getId() {
				return 2L;
			}

			@Override
			public String getName() {
				return "Hall B";
			}

			@Override
			public Long getSeatsCount() {
				return 3L;
			}
		};

		List<CinemaHallResponse> responses = cinemaHallMapper
				.toCinemaHallResponseListFromProjection(Arrays.asList(projection1, projection2));

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).id()).isEqualTo(1L);
		assertThat(responses.get(0).name()).isEqualTo("Hall A");
		assertThat(responses.get(0).capacity()).isEqualTo(5);
		assertThat(responses.get(1).id()).isEqualTo(2L);
		assertThat(responses.get(1).name()).isEqualTo("Hall B");
		assertThat(responses.get(1).capacity()).isEqualTo(3);
	}

	@Test
	void toCinemaHallResponseListFromProjection_WhenInputEmpty_ReturnsEmpty() {
		List<CinemaHallResponse> responses = cinemaHallMapper
				.toCinemaHallResponseListFromProjection(Collections.emptyList());
		assertThat(responses).isEmpty();
	}

	@Test
	void toCinemaHallResponseListFromProjection_WhenInputNull_ReturnsNull() {
		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseListFromProjection(null);
		assertThat(responses).isNull();
	}

	@Test
	void toCinemaHallResponse_WithNullEntity_ReturnsNull() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse((CinemaHall) null);
		assertThat(response).isNull();
	}

	@Test
	void toCinemaHallResponseFromProjection_WithNullProjection_ReturnsNull() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponseFromProjection(null);
		assertThat(response).isNull();
	}
}