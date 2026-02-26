package ua.lviv.bas.cinema.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import ua.lviv.bas.cinema.domain.CinemaHall;
import ua.lviv.bas.cinema.domain.Seat;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;
import ua.lviv.bas.cinema.dto.cinemaHall.response.HallLayoutResponse;

public class CinemaHallMapperTest {

	private CinemaHallMapper cinemaHallMapper;
	private CinemaHall cinemaHall;
	private CinemaHall cinemaHallWithSeats;
	private CinemaHall cinemaHallEmptySeats;
	private CinemaHall cinemaHallNullSeats;

	@BeforeEach
	void setUp() {
		cinemaHallMapper = Mappers.getMapper(CinemaHallMapper.class);

		cinemaHall = CinemaHall.builder().id(1L).name("Hall A").build();

		cinemaHallWithSeats = CinemaHall.builder().id(2L).name("Hall B").build();

		Seat seat1 = Seat.builder().id(1L).row(1).number(1).active(true).build();
		Seat seat2 = Seat.builder().id(2L).row(1).number(2).active(true).build();
		Seat seat3 = Seat.builder().id(3L).row(2).number(1).active(false).build();
		cinemaHallWithSeats.setSeats(Arrays.asList(seat1, seat2, seat3));

		cinemaHallEmptySeats = CinemaHall.builder().id(3L).name("Hall C").build();
		cinemaHallEmptySeats.setSeats(Collections.emptyList());

		cinemaHallNullSeats = CinemaHall.builder().id(4L).name("Hall D").build();
		cinemaHallNullSeats.setSeats(null);
	}

	@Test
	void toCinemaHallResponseShouldMapAllFields() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(cinemaHall);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getName()).isEqualTo("Hall A");
		assertThat(response.getCapacity()).isZero();
	}

	@Test
	void toCinemaHallResponseShouldCalculateCapacity() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(cinemaHallWithSeats);

		assertThat(response.getCapacity()).isEqualTo(3);
	}

	@Test
	void toCinemaHallResponseShouldReturnZeroCapacityForEmptySeats() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(cinemaHallEmptySeats);

		assertThat(response.getCapacity()).isZero();
	}

	@Test
	void toCinemaHallResponseShouldReturnZeroCapacityForNullSeats() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(cinemaHallNullSeats);

		assertThat(response.getCapacity()).isZero();
	}

	@Test
	void toCinemaHallResponseShouldHandleNullInput() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(null);

		assertThat(response).isNull();
	}

	@Test
	void toCinemaHallResponseListShouldMapList() {
		List<CinemaHall> halls = Arrays.asList(cinemaHall, cinemaHallWithSeats);

		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(halls);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).getId()).isEqualTo(1L);
		assertThat(responses.get(1).getId()).isEqualTo(2L);
	}

	@Test
	void toCinemaHallResponseListShouldReturnEmptyListForEmptyInput() {
		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(Collections.emptyList());

		assertThat(responses).isEmpty();
	}

	@Test
	void toCinemaHallResponseListShouldReturnNullForNullInput() {
		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(null);

		assertThat(responses).isNull();
	}

	@Test
	void toCinemaHallResponseListShouldHandleListWithNullElement() {
		List<CinemaHall> halls = Arrays.asList(cinemaHall, null);

		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(halls);

		assertThat(responses).hasSize(2);
		assertThat(responses.get(0)).isNotNull();
		assertThat(responses.get(1)).isNull();
	}

	@Test
	void toHallLayoutResponseShouldMapAllFields() {
		HallLayoutResponse response = cinemaHallMapper.toHallLayoutResponse(cinemaHallWithSeats);

		assertThat(response).isNotNull();
		assertThat(response.getHallId()).isEqualTo(2L);
		assertThat(response.getHallName()).isEqualTo("Hall B");
		assertThat(response.getTotalSeats()).isEqualTo(3);
	}

	@Test
	void toHallLayoutResponseShouldHandleNullInput() {
		HallLayoutResponse response = cinemaHallMapper.toHallLayoutResponse(null);

		assertThat(response).isNull();
	}

	@Test
	void toHallLayoutResponseShouldReturnZeroTotalSeatsForEmptySeats() {
		HallLayoutResponse response = cinemaHallMapper.toHallLayoutResponse(cinemaHallEmptySeats);

		assertThat(response.getTotalSeats()).isZero();
	}
}