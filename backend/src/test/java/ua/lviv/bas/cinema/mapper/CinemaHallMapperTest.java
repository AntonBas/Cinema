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
import ua.lviv.bas.cinema.domain.enums.SeatType;
import ua.lviv.bas.cinema.dto.cinemaHall.response.CinemaHallResponse;

class CinemaHallMapperTest {

	private CinemaHallMapper cinemaHallMapper;
	private CinemaHall cinemaHallWithSeats;
	private CinemaHall cinemaHallWithMixedSeats;
	private CinemaHall cinemaHallEmptySeats;
	private CinemaHall cinemaHallNullSeats;

	@BeforeEach
	void setUp() {
		cinemaHallMapper = Mappers.getMapper(CinemaHallMapper.class);

		cinemaHallWithSeats = CinemaHall.builder().id(1L).name("Hall A").build();

		Seat seat1 = Seat.builder().id(1L).row(1).number(1).active(true).hall(cinemaHallWithSeats).build();
		Seat seat2 = Seat.builder().id(2L).row(1).number(2).active(true).hall(cinemaHallWithSeats).build();
		Seat seat3 = Seat.builder().id(3L).row(2).number(1).active(true).hall(cinemaHallWithSeats).build();
		cinemaHallWithSeats.setSeats(Arrays.asList(seat1, seat2, seat3));

		cinemaHallWithMixedSeats = CinemaHall.builder().id(2L).name("Hall B").build();

		Seat seat4 = Seat.builder().id(4L).row(1).number(1).active(true).hall(cinemaHallWithMixedSeats).build();
		Seat seat5 = Seat.builder().id(5L).row(1).number(2).active(false).hall(cinemaHallWithMixedSeats).build();
		Seat seat6 = Seat.builder().id(6L).row(2).number(1).active(true).hall(cinemaHallWithMixedSeats).build();
		cinemaHallWithMixedSeats.setSeats(Arrays.asList(seat4, seat5, seat6));

		cinemaHallEmptySeats = CinemaHall.builder().id(3L).name("Hall C").build();
		cinemaHallEmptySeats.setSeats(Collections.emptyList());

		cinemaHallNullSeats = CinemaHall.builder().id(4L).name("Hall D").build();
		cinemaHallNullSeats.setSeats(null);
	}

	@Test
	void toCinemaHallResponse_ShouldMapAllFieldsCorrectly_WhenHallHasSeats() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(cinemaHallWithSeats);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(1L);
		assertThat(response.getName()).isEqualTo("Hall A");
		assertThat(response.getCapacity()).isEqualTo(3);
	}

	@Test
	void toCinemaHallResponse_ShouldCountAllSeatsForCapacity_RegardlessOfActiveStatus() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(cinemaHallWithMixedSeats);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(2L);
		assertThat(response.getName()).isEqualTo("Hall B");
		assertThat(response.getCapacity()).isEqualTo(3);
	}

	@Test
	void toCinemaHallResponse_ShouldReturnZeroCapacity_WhenSeatsListIsEmpty() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(cinemaHallEmptySeats);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(3L);
		assertThat(response.getName()).isEqualTo("Hall C");
		assertThat(response.getCapacity()).isZero();
	}

	@Test
	void toCinemaHallResponse_ShouldReturnZeroCapacity_WhenSeatsListIsNull() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(cinemaHallNullSeats);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(4L);
		assertThat(response.getName()).isEqualTo("Hall D");
		assertThat(response.getCapacity()).isZero();
	}

	@Test
	void toCinemaHallResponse_ShouldHandleNullInput() {
		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(null);

		assertThat(response).isNull();
	}

	@Test
	void toCinemaHallResponse_ShouldMapHallWithoutBuilder() {
		CinemaHall hall = new CinemaHall();
		hall.setId(5L);
		hall.setName("Hall E");
		hall.setSeats(Collections.emptyList());

		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(hall);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(5L);
		assertThat(response.getName()).isEqualTo("Hall E");
		assertThat(response.getCapacity()).isZero();
	}

	@Test
	void toCinemaHallResponse_ShouldCountAllSeats_EvenWhenInactive() {
		CinemaHall hall = CinemaHall.builder().id(6L).name("Hall F").build();

		Seat seat1 = Seat.builder().id(10L).row(1).number(1).active(false).hall(hall).build();
		Seat seat2 = Seat.builder().id(11L).row(1).number(2).active(false).hall(hall).build();
		hall.setSeats(Arrays.asList(seat1, seat2));

		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(hall);

		assertThat(response).isNotNull();
		assertThat(response.getId()).isEqualTo(6L);
		assertThat(response.getName()).isEqualTo("Hall F");
		assertThat(response.getCapacity()).isEqualTo(2);
	}

	@Test
	void toCinemaHallResponseList_ShouldMapListOfHalls() {
		List<CinemaHall> halls = Arrays.asList(cinemaHallWithSeats, cinemaHallWithMixedSeats);

		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(halls);

		assertThat(responses).isNotNull().hasSize(2);

		assertThat(responses.get(0))
				.extracting(CinemaHallResponse::getId, CinemaHallResponse::getName, CinemaHallResponse::getCapacity)
				.containsExactly(1L, "Hall A", 3);

		assertThat(responses.get(1))
				.extracting(CinemaHallResponse::getId, CinemaHallResponse::getName, CinemaHallResponse::getCapacity)
				.containsExactly(2L, "Hall B", 3);
	}

	@Test
	void toCinemaHallResponseList_ShouldReturnEmptyList_WhenInputIsEmpty() {
		List<CinemaHall> halls = Collections.emptyList();

		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(halls);

		assertThat(responses).isNotNull().isEmpty();
	}

	@Test
	void toCinemaHallResponseList_ShouldReturnNull_WhenInputIsNull() {
		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(null);

		assertThat(responses).isNull();
	}

	@Test
	void toCinemaHallResponseList_ShouldHandleListWithNullElements() {
		List<CinemaHall> halls = Arrays.asList(cinemaHallWithSeats, null, cinemaHallEmptySeats);

		List<CinemaHallResponse> responses = cinemaHallMapper.toCinemaHallResponseList(halls);

		assertThat(responses).isNotNull().hasSize(3);

		assertThat(responses.get(0)).isNotNull();
		assertThat(responses.get(1)).isNull();
		assertThat(responses.get(2)).isNotNull();
	}

	@Test
	void toCinemaHallResponse_ShouldMapHallWithSpecialCharactersInName() {
		CinemaHall hall = CinemaHall.builder().id(7L).name("Hall & Center (VIP)").build();
		hall.setSeats(Collections.emptyList());

		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(hall);

		assertThat(response).isNotNull();
		assertThat(response.getName()).isEqualTo("Hall & Center (VIP)");
	}

	@Test
	void toCinemaHallResponse_ShouldCalculateCapacityCorrectlyForComplexCase() {
		CinemaHall hall = CinemaHall.builder().id(8L).name("Complex Hall").build();

		List<Seat> seats = Arrays.asList(Seat.builder().id(20L).row(1).number(1).active(true).hall(hall).build(),
				Seat.builder().id(21L).row(1).number(2).active(false).hall(hall).build(),
				Seat.builder().id(22L).row(2).number(1).active(true).hall(hall).build(),
				Seat.builder().id(23L).row(2).number(2).active(true).hall(hall).build(),
				Seat.builder().id(24L).row(3).number(1).active(false).hall(hall).build());
		hall.setSeats(seats);

		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(hall);

		assertThat(response.getCapacity()).isEqualTo(5);
	}

	@Test
	void toCinemaHallResponse_ShouldHandleSeatWithDifferentTypes() {
		CinemaHall hall = CinemaHall.builder().id(9L).name("Mixed Types Hall").build();

		List<Seat> seats = Arrays.asList(
				Seat.builder().id(30L).row(1).number(1).seatType(SeatType.STANDARD).active(true).hall(hall).build(),
				Seat.builder().id(31L).row(1).number(2).seatType(SeatType.VIP).active(true).hall(hall).build(),
				Seat.builder().id(32L).row(2).number(1).seatType(SeatType.COUPLE).active(false).hall(hall).build());
		hall.setSeats(seats);

		CinemaHallResponse response = cinemaHallMapper.toCinemaHallResponse(hall);

		assertThat(response.getCapacity()).isEqualTo(3);
	}
}